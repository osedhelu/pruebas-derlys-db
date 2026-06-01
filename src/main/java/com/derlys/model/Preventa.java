package com.derlys.model;

import java.time.LocalDateTime;

public record Preventa(
        Integer id,
        Integer clienteId,
        Integer loteId,
        Integer cantidadApartada,
        LocalDateTime fechaApartado,
        String estado,
        Double montoACobrar,
        String notasEntrega) {
}