package com.derlys.ui;

import com.derlys.model.Usuario;
import com.derlys.repository.AuthRepository;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.sql.Connection;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class LoginScreen extends JFrame {

    private final Connection conn;
    private final AuthRepository authRepo;
    private final JTextField campoUsuario = new JTextField(20);
    private final JPasswordField campoContrasena = new JPasswordField(20);

    public LoginScreen(Connection conn) {
        this.conn = conn;
        authRepo = new AuthRepository(conn);

        setTitle("Iniciar Sesión - Granja Derlys");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel form = new JPanel(new GridLayout(2, 2, 8, 8));
        form.setBorder(new EmptyBorder(16, 16, 8, 16));
        form.add(new JLabel("Usuario:"));
        form.add(campoUsuario);
        form.add(new JLabel("Contraseña:"));
        form.add(campoContrasena);

        JButton btnLogin = new JButton("Iniciar sesión");
        btnLogin.addActionListener(e -> intentarLogin());

        JPanel abajo = new JPanel(new FlowLayout(FlowLayout.CENTER));
        abajo.add(btnLogin);

        setLayout(new BorderLayout());
        add(form, BorderLayout.CENTER);
        add(abajo, BorderLayout.SOUTH);
        pack();
    }

    private void intentarLogin() {
        String usuario = campoUsuario.getText().trim();
        String contrasena = new String(campoContrasena.getPassword());

        if (usuario.isEmpty() || contrasena.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Completa usuario y contraseña.");
            return;
        }

        try {
            Usuario user = authRepo.login(usuario, contrasena);
            dispose();
            new MenuPrincipalScreen(user, conn).setVisible(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            campoContrasena.setText("");
        }
    }
}
