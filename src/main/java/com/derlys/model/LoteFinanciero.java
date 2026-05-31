package com.derlys.model;

public record LoteFinanciero(
        Integer id,
        String codigoLote,
        double ingresos,
        double gastos,
        double resultado,
        int pollosVendidos,
        String estado) {
}
