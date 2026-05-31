package com.derlys.ui;

import com.derlys.model.Usuario;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class MenuPrincipalScreen extends JFrame {

    private final Usuario usuario;

    public MenuPrincipalScreen(Usuario usuario) {
        this.usuario = usuario;

        setTitle("El Buen Pollo - " + usuario.nombre());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(new JLabel("Bienvenido, " + usuario.nombre(), JLabel.CENTER), BorderLayout.CENTER);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 12));
        switch (usuario.rolId()) {
            case 1 -> {
                boton(botones, "Módulo Granja", this::abrirGranja);
                boton(botones, "Módulo Ventas", () -> moduloPendiente("Ventas"));
                boton(botones, "Módulo Clientes", () -> moduloPendiente("Clientes"));
                boton(botones, "Reportes", () -> JOptionPane.showMessageDialog(this, "Abriendo Reportes..."));
            }
            case 2 -> {
                boton(botones, "Módulo Granja", this::abrirGranja);
            }
            case 3 -> {
                boton(botones, "Módulo Ventas", () -> moduloPendiente("Ventas"));
                boton(botones, "Módulo Clientes", () -> moduloPendiente("Clientes"));
            }
            default -> botones.add(new JLabel("No tienes módulos asignados para tu rol."));
        }

        add(botones, BorderLayout.SOUTH);
        pack();
    }

    private void boton(JPanel panel, String texto, Runnable accion) {
        JButton btn = new JButton(texto);
        btn.addActionListener(e -> accion.run());
        panel.add(btn);
    }

    private void abrirGranja() {
        new GranjaScreen(usuario).setVisible(true);
    }

    private void moduloPendiente(String nombre) {
        JOptionPane.showMessageDialog(this, "Módulo " + nombre + " en construcción.");
    }
}
