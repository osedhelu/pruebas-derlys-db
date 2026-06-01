package com.derlys.model;

import java.time.LocalDate;
import java.util.List;

public record Lote(
        Integer id,
        String codigoLote,
        LocalDate fechaEntrada,
        Integer cantidadInicial,
        String raza,
        String estado,
        String observaciones,
        Double costoInicial) {

    public void print() {
        System.out.println("-----------------------------------");
        System.out.println("ID            = " + this.id());
        System.out.println("Código Lote   = " + this.codigoLote());
        System.out.println("Fecha Entrada = " + this.fechaEntrada());
        System.out.println("Estado        = " + this.estado());
        System.out.println("Raza          = " + this.raza());
        System.out.println("Cantidad      = " + this.cantidadInicial());
        System.out.println("Costo inicial = " + this.costoInicial());
    }

    // NUEVO MÉTODO: Recibe la lista e imprime cada lote usando el método print() de arriba
    public static void printAll(List<Lote> lotes) {
        if (lotes == null || lotes.isEmpty()) {
            System.out.println("No hay lotes para mostrar.");
            return;
        }
        System.out.println("========== LISTA DE LOTES ==========");
        for (Lote lote : lotes) {
            lote.print();
        }
        System.out.println("====================================");
    }

}
