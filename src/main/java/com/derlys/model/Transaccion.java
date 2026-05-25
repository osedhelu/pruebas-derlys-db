package com.derlys.model;

import java.time.LocalDateTime;

public record Transaccion(Integer id,
        LocalDateTime fecha,
        String descripcion,
        Integer loteId,
        Integer usuarioId,
        Integer tipoMovimientoId,
        Integer cantidadUnidades,
        String comprobanteUrl) {

}
