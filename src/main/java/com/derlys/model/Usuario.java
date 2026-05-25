package com.derlys.model;

import java.time.LocalDateTime;

public record Usuario(
        Integer id,
        String nombre,
        String email,
        String passwordHash,
        Integer rolId,
        LocalDateTime fechaCreacion
) {}