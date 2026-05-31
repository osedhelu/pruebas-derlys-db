package com.derlys.ui;

import com.derlys.repository.AuthRepository;
import java.sql.Connection;
import javax.swing.Box;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import com.derlys.model.Usuario;
import java.sql.SQLException;

public class LoginScreen {

    private final AuthRepository authRepo;

    public LoginScreen(Connection coom) {
        this.authRepo = new AuthRepository(coom);
    }

    public void run() throws SQLException{

        JTextField campoUsuario = new JTextField(15);
        JPasswordField campoContrasena = new JPasswordField(15);


        Object[] formulario = {
            "Nombre de Usuario / Email:", campoUsuario,
            Box.createVerticalStrut(10), // Un pequeño espacio de separación visual de 10px
            "Contraseña:", campoContrasena
        };


        int opcion = JOptionPane.showConfirmDialog(
                null,
                formulario,
                "Iniciar Sesión - Granja Derlys",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );


        if (opcion == JOptionPane.OK_OPTION) {
            String usuario = campoUsuario.getText();

            String contrasena = new String(campoContrasena.getPassword());


            if (usuario.isEmpty() || contrasena.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Por favor, completa todos los campos.", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                Usuario user = this.authRepo.login(usuario,contrasena);
                
                 
                var pantallaPrincipal = new MenuPrincipalScreen(user);
                pantallaPrincipal.setVisible(true);
            }
        } else {
            System.out.println("El usuario canceló el inicio de sesión.");
        }
    }
    
}
