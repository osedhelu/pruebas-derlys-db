package com.derlys.model;

public record AsientoContable(
        Integer id,
        Integer transaccionId,
        Integer cuentaId,
        Double debe,
        Double haber
) {}