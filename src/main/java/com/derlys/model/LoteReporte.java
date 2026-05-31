package com.derlys.model;

import java.time.LocalDate;

public record LoteReporte(
        Integer id,
        String codigoLote,
        Integer cantidadInicial,
        LocalDate fechaEntrada,
        Integer diasVida,
        Integer diasParaSacrificio,
        Integer totalSalidas,
        Integer totalApartado,
        Integer disponibleVenta) {
}
