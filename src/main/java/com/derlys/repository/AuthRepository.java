
package com.derlys.repository;

import java.sql.Connection;
import com.derlys.model.Usuario;


import java.sql.SQLException;
public class AuthRepository extends UsuarioRepository {
    

    public AuthRepository(Connection conexion) {
        super(conexion);
    
    }
    
    public Usuario login(String email, String contrasena) throws SQLException {
        Usuario user = this.buscar(email);
        if(user == null) {
            throw new RuntimeException("Este usuario no exite: " + email);
        }
        else if(!user.passwordHash().equals(contrasena)){
            throw new RuntimeException("La contraseña es incorrecta: " + email);
        }
        return user;
       
        
    }
    
    
}
