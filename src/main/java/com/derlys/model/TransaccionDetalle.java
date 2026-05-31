package com.derlys.model;

import java.time.LocalDateTime;

public record TransaccionDetalle(
        Integer id,
        LocalDateTime fecha,
        String tipoNombre,
        Integer cantidadUnidades,
        String descripcion,
        Double montoReferencia) {
}
