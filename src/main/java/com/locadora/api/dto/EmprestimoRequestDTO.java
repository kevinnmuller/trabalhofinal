package com.locadora.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EmprestimoRequestDTO {
    @NotNull
    private Long usuarioId;

    @NotNull
    private Long itemId;
}
