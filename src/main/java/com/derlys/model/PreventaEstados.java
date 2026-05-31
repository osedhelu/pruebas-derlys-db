package com.derlys.model;

import java.util.List;
import java.util.Set;

public final class PreventaEstados {

    public static final String PENDIENTE = "pendiente";
    public static final String LISTO = "listo";
    public static final String MORA = "mora";
    public static final String COMPLETADA = "completada";
    public static final String ENTREGADO = "entregado";

    /** Estados que el vendedor puede asignar manualmente (no incluye completada: eso es al cobrar). */
    public static final List<String> MANUALES = List.of(PENDIENTE, LISTO, MORA, ENTREGADO);

    /** Apartados que aún reservan pollos en el lote. */
    public static final Set<String> RESERVA_STOCK = Set.of(PENDIENTE, LISTO, MORA);

    private PreventaEstados() {}

    public static String etiqueta(String estado) {
        if (estado == null) {
            return "—";
        }
        return switch (estado.toLowerCase()) {
            case PENDIENTE -> "Pendiente";
            case LISTO -> "Listo (avisar cliente)";
            case MORA -> "En mora";
            case COMPLETADA -> "Cobrada";
            case ENTREGADO -> "Entregado";
            default -> estado;
        };
    }

    public static boolean reservaStock(String estado) {
        return estado != null && RESERVA_STOCK.contains(estado.toLowerCase());
    }

    public static boolean puedeCobrarse(String estado) {
        if (estado == null) {
            return false;
        }
        String e = estado.toLowerCase();
        return PENDIENTE.equals(e) || LISTO.equals(e) || MORA.equals(e);
    }
}
