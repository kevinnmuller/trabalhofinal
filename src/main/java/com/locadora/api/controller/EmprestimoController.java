package com.locadora.api.controller;

import com.locadora.api.dto.EmprestimoRequestDTO;
import com.locadora.api.dto.EmprestimoResponseDTO;
import com.locadora.api.model.Emprestimo;
import com.locadora.api.service.EmprestimoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/emprestimos")
public class EmprestimoController {

    private final EmprestimoService emprestimoService;

    public EmprestimoController(EmprestimoService emprestimoService) {
        this.emprestimoService = emprestimoService;
    }

    @PostMapping
    public ResponseEntity<EmprestimoResponseDTO> criarEmprestimo(@Valid @RequestBody EmprestimoRequestDTO req) {
        Emprestimo e = emprestimoService.emprestar(req.getUsuarioId(), req.getItemId());
        EmprestimoResponseDTO resp = emprestimoService.toDTO(e);
        return ResponseEntity.status(201).body(resp);
    }

    @PostMapping("/{id}/renovar")
    public ResponseEntity<EmprestimoResponseDTO> renovar(@PathVariable Long id) {
        Emprestimo e = emprestimoService.renovar(id);
        return ResponseEntity.ok(emprestimoService.toDTO(e));
    }

    @PostMapping("/{id}/devolver")
    public ResponseEntity<EmprestimoResponseDTO> devolver(@PathVariable Long id) {
        Emprestimo e = emprestimoService.devolver(id);
        return ResponseEntity.ok(emprestimoService.toDTO(e));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmprestimoResponseDTO> buscar(@PathVariable Long id) {
        Emprestimo e = emprestimoService.buscarPorId(id)
                .orElseThrow(() -> new com.locadora.api.exception.NotFoundException("Empréstimo não encontrado"));
        return ResponseEntity.ok(emprestimoService.toDTO(e));
    }
}
