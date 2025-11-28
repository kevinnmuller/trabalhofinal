package com.locadora.api.controller;

import com.locadora.api.exception.NotFoundException;
import com.locadora.api.model.Usuario;
import com.locadora.api.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping
    public ResponseEntity<?> cadastrar(@Valid @RequestBody Usuario usuario) {
        Usuario salvo = usuarioService.salvar(usuario);
        return ResponseEntity.status(201).body(salvo);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Usuario> buscar(@PathVariable Long id) {
        Usuario usuario = usuarioService.buscarPorId(id)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
        return ResponseEntity.ok(usuario);
    }

    @GetMapping("/{id}/dividas")
    public ResponseEntity<?> consultarDividas(@PathVariable Long id) {
        Usuario usuario = usuarioService.buscarPorId(id)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));

        return ResponseEntity.ok(Map.of(
                "usuarioId", usuario.getId(),
                "dividaTotal", usuario.getDividaTotal()
        ));
    }

    @PostMapping("/{id}/quitar")
    public ResponseEntity<?> quitar(@PathVariable Long id) {
        Usuario usuario = usuarioService.buscarPorId(id)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));

        usuario.setDividaTotal(0.0);
        usuarioService.salvar(usuario);

        return ResponseEntity.ok(Map.of(
                "message", "Dívida quitada com sucesso!",
                "usuarioId", usuario.getId(),
                "dividaTotal", usuario.getDividaTotal()
        ));
    }
}
