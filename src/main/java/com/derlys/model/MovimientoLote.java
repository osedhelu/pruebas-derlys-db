package com.derlys.model;

import java.time.LocalDateTime;

public record MovimientoLote(
        LocalDateTime fecha,
        String tipo,
        String descripcion,
        int cantidad,
        Double monto,
        String responsable,
        String estado) {
}
