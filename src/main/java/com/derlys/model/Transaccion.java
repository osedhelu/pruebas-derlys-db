package com.derlys.model;

import java.time.LocalDateTime;
import java.util.List;

public record Transaccion(Integer id,
        LocalDateTime fecha,
        String descripcion,
        Integer loteId,
        Integer usuarioId,
        Integer tipoMovimientoId,
        Integer cantidadUnidades,
        String comprobanteUrl) {

    public static void imprimirDetalle(Transaccion t) {
        if (t == null) {
            System.out.println("La transacción es nula.");
            return;
        }
        System.out.println("=========================================");
        System.out.printf("  TRANSMISIÓN ID: %d%n", t.id());
        System.out.println("=========================================");
        System.out.printf("• Fecha:             %s%n", t.fecha() != null ? t.fecha() : "N/A");
        System.out.printf("• Descripción:       %s%n", t.descripcion() != null ? t.descripcion() : "Sin descripción");
        System.out.printf("• ID Lote:           %d%n", t.loteId());
        System.out.printf("• ID Usuario:        %d%n", t.usuarioId());
        System.out.printf("• ID Tipo Movimiento:%d%n", t.tipoMovimientoId());
        System.out.printf("• Cantidad Unidades: %d%n", t.cantidadUnidades());
        System.out.printf("• URL Comprobante:   %s%n", t.comprobanteUrl() != null ? t.comprobanteUrl() : "N/A");
        System.out.println("=========================================\n");
    }

    public static void imprimirLista(List<Transaccion> lista) {
        if (lista == null || lista.isEmpty()) {
            System.out.println("No hay transacciones para mostrar.");
            return;
        }
        
        System.out.println("<<< INICIO DE LA LISTA DE TRANSACCIONES >>>");
        // Usamos un stream o un bucle for-each para llamar al método anterior por cada elemento
        lista.forEach(Transaccion::imprimirDetalle);
        System.out.println("<<< FIN DE LA LISTA (Total: " + lista.size() + " ítems) >>>");
    }
}
