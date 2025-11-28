package com.locadora.api.service;

import com.locadora.api.dto.EmprestimoResponseDTO;
import com.locadora.api.exception.BadRequestException;
import com.locadora.api.exception.ConflictException;
import com.locadora.api.exception.NotFoundException;
import com.locadora.api.model.Emprestimo;
import com.locadora.api.model.Item;
import com.locadora.api.model.Usuario;
import com.locadora.api.repository.EmprestimoRepository;
import com.locadora.api.repository.ItemRepository;
import com.locadora.api.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class EmprestimoService {

    private final EmprestimoRepository emprestimoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ItemRepository itemRepository;

    private final int DIAS_EMPRESTIMO = 7;       // duração padrão
    private final double MULTA_DIARIA = 2.0;     // valor definido pelo professor

    public EmprestimoService(EmprestimoRepository emprestimoRepository,
                             UsuarioRepository usuarioRepository,
                             ItemRepository itemRepository) {
        this.emprestimoRepository = emprestimoRepository;
        this.usuarioRepository = usuarioRepository;
        this.itemRepository = itemRepository;
    }

    public Optional<Emprestimo> buscarPorId(Long id) {
        return emprestimoRepository.findById(id);
    }

    public List<Emprestimo> listarTodos() {
        return emprestimoRepository.findAll();
    }

    public Emprestimo emprestar(Long usuarioId, Long itemId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));

        if (usuario.getDividaTotal() > 0) {
            throw new ConflictException("Usuário com dívidas não pode emprestar.");
        }

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item não encontrado"));

        long emprestados = emprestimoRepository.countByItemIdAndFinalizadoFalse(itemId);

        if (emprestados >= item.getQuantidadeEstoque()) {
            throw new ConflictException("Item sem estoque disponível");
        }

        Emprestimo emprestimo = new Emprestimo();
        emprestimo.setUsuario(usuario);
        emprestimo.setItem(item);
        emprestimo.setDataEmprestimo(LocalDate.now());
        emprestimo.setDataPrevistaDevolucao(LocalDate.now().plusDays(DIAS_EMPRESTIMO));
        emprestimo.setRenovacoes(0);
        emprestimo.setFinalizado(false);

        return emprestimoRepository.save(emprestimo);
    }

    public Emprestimo renovar(Long emprestimoId) {
        Emprestimo emprestimo = buscarPorId(emprestimoId)
                .orElseThrow(() -> new NotFoundException("Empréstimo não encontrado"));

        if (emprestimo.isFinalizado())
            throw new BadRequestException("Empréstimo já finalizado.");

        if (emprestimo.getRenovacoes() >= 2)
            throw new ConflictException("Limite de renovações atingido.");

        // opcional: bloquear renovação se houver dívida do usuário
        if (emprestimo.getUsuario().getDividaTotal() > 0)
            throw new ConflictException("Usuário com dívidas não pode renovar.");

        emprestimo.setRenovacoes(emprestimo.getRenovacoes() + 1);
        emprestimo.setDataPrevistaDevolucao(
                emprestimo.getDataPrevistaDevolucao().plusDays(DIAS_EMPRESTIMO)
        );

        return emprestimoRepository.save(emprestimo);
    }

    public Emprestimo devolver(Long emprestimoId) {
        Emprestimo emprestimo = buscarPorId(emprestimoId)
                .orElseThrow(() -> new NotFoundException("Empréstimo não encontrado"));

        if (emprestimo.isFinalizado())
            throw new BadRequestException("Este empréstimo já foi finalizado.");

        LocalDate hoje = LocalDate.now();

        // validação: devolução não pode ser anterior à data do empréstimo
        if (hoje.isBefore(emprestimo.getDataEmprestimo()))
            throw new BadRequestException("Data de devolução inválida.");

        emprestimo.setDataDevolucao(hoje);
        emprestimo.setFinalizado(true);

        long atraso = Math.max(0,
                ChronoUnit.DAYS.between(
                        emprestimo.getDataPrevistaDevolucao(),
                        emprestimo.getDataDevolucao()
                )
        );

        double multa = atraso * MULTA_DIARIA;
        emprestimo.setMulta(multa);

        if (multa > 0) {
            Usuario usuario = emprestimo.getUsuario();
            usuario.setDividaTotal(usuario.getDividaTotal() + multa);
            usuarioRepository.save(usuario);
        }

        return emprestimoRepository.save(emprestimo);
    }

    // utilitário para retornar DTO
    public EmprestimoResponseDTO toDTO(Emprestimo e) {
        return new EmprestimoResponseDTO(
                e.getId(),
                e.getUsuario() != null ? e.getUsuario().getId() : null,
                e.getItem() != null ? e.getItem().getId() : null,
                e.getDataEmprestimo(),
                e.getDataPrevistaDevolucao(),
                e.getDataDevolucao(),
                e.getRenovacoes(),
                e.getMulta(),
                e.isFinalizado()
        );
    }
}
