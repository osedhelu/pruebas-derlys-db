package com.derlys.model;

import java.time.LocalDateTime;
import java.util.List;

public record Usuario(
        Integer id,
        String nombre,
        String username,
        String email,
        String telefono,
        String passwordHash,
        Integer rolId,
        LocalDateTime fechaCreacion) {

    public void print() {
        System.out.println("-----------------------------------");
        System.out.println("ID             = " + this.id());
        System.out.println("Nombre         = " + this.nombre());
        System.out.println("Usuario        = " + this.username());
        System.out.println("Email          = " + this.email());
        System.out.println("Teléfono       = " + this.telefono());
        System.out.println("Rol ID         = " + this.rolId());
        System.out.println("Fecha Creación = " + this.fechaCreacion());
    }

   
    public static void printAll(List<Usuario> usuarios) {
        if (usuarios == null || usuarios.isEmpty()) {
            System.out.println("No hay usuarios registrados para mostrar.");
            return;
        }
        System.out.println("========== LISTA DE USUARIOS ==========");
        for (Usuario usuario : usuarios) {
            usuario.print(); 
        }
        System.out.println("=======================================");
    }
}
