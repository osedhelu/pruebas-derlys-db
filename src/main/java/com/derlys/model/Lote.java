package com.derlys.model;

import java.time.LocalDate;

public record Lote(
        Integer id,
        String codigoLote,
        LocalDate fechaEntrada,
        Integer cantidadInicial,
        String raza,
        String estado,
        String observaciones) {

}
