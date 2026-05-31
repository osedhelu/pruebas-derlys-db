package com.derlys.model;

import java.time.LocalDateTime;

public record PreventaDetalle(
        Integer id,
        String clienteNombre,
        String codigoLote,
        Integer cantidadApartada,
        LocalDateTime fechaApartado,
        String estado) {
}
