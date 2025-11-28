package com.locadora.api.controller;

import com.locadora.api.dto.DisponibilidadeDTO;
import com.locadora.api.exception.NotFoundException;
import com.locadora.api.model.Item;
import com.locadora.api.repository.EmprestimoRepository;
import com.locadora.api.service.ItemService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/itens")
public class ItemController {

    private final ItemService itemService;
    private final EmprestimoRepository emprestimoRepository;

    public ItemController(ItemService itemService, EmprestimoRepository emprestimoRepository) {
        this.itemService = itemService;
        this.emprestimoRepository = emprestimoRepository;
    }

    @PostMapping
    public ResponseEntity<?> cadastrar(@Valid @RequestBody Item item) {
        Item salvo = itemService.salvar(item);
        return ResponseEntity.status(201).body(salvo);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Item> buscar(@PathVariable Long id) {
        Item item = itemService.buscarPorId(id)
                .orElseThrow(() -> new NotFoundException("Item não encontrado"));
        return ResponseEntity.ok(item);
    }

    @GetMapping("/{id}/disponibilidade")
    public ResponseEntity<DisponibilidadeDTO> disponibilidade(@PathVariable Long id) {
        Item item = itemService.buscarPorId(id)
                .orElseThrow(() -> new NotFoundException("Item não encontrado"));

        long emprestados = emprestimoRepository.countByItemIdAndFinalizadoFalse(id);
        int disponiveis = item.getQuantidadeEstoque() - (int) emprestados;

        DisponibilidadeDTO dto = new DisponibilidadeDTO(
                item.getId(),
                item.getQuantidadeEstoque(),
                (int) emprestados,
                disponiveis
        );

        return ResponseEntity.ok(dto);
    }
}
