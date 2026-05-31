package com.derlys.model;

public record CuentaSaldo(
        String codigo,
        String nombre,
        String naturaleza,
        double totalDebe,
        double totalHaber,
        double saldo) {
}
