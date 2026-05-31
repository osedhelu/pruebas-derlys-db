
package com.derlys.repository;

import java.sql.Connection;
import com.derlys.model.Usuario;

import java.sql.SQLException;

public class AuthRepository extends UsuarioRepository {

    public AuthRepository(Connection conexion) {
        super(conexion);

    }

    public Usuario login(String username, String contrasena) throws SQLException {
        Usuario user = buscar(username);
        if (user == null) {
            throw new RuntimeException("Usuario no encontrado: " + username);
        }
        String guardada = user.passwordHash();
        if (guardada == null || !guardada.equals(contrasena)) {
            throw new RuntimeException("Contraseña incorrecta");
        }
        return user;
    }

}
