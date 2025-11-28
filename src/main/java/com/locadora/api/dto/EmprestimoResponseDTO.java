package com.locadora.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class EmprestimoResponseDTO {
    private Long id;
    private Long usuarioId;
    private Long itemId;
    private LocalDate dataEmprestimo;
    private LocalDate dataPrevistaDevolucao;
    private LocalDate dataDevolucao;
    private int renovacoes;
    private double multa;
    private boolean finalizado;
}
