package com.derlys.ui;

import com.derlys.model.Usuario;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.sql.Connection;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class MenuPrincipalScreen extends JFrame {

    private final Usuario usuario;
    private final Connection conn;

    public MenuPrincipalScreen(Usuario usuario, Connection conn) {
        this.usuario = usuario;
        this.conn = conn;

        setTitle("El Buen Pollo - " + usuario.nombre());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(new JLabel("Bienvenido, " + usuario.nombre(), JLabel.CENTER), BorderLayout.CENTER);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 12));
        switch (usuario.rolId()) {
            case 1 -> {
                boton(botones, "Módulo Granja", this::abrirGranja);
                boton(botones, "Módulo Ventas", this::abrirVentas);
                boton(botones, "Módulo Clientes", this::abrirClientes);
                boton(botones, "Reportes operativos", this::abrirReportes);
                boton(botones, "Finanzas / Rentabilidad", this::abrirFinanzas);
                boton(botones, "Saldos contables", this::abrirSaldosCuentas);
            }
            case 2 -> boton(botones, "Módulo Granja", this::abrirGranja);
            case 3 -> {
                boton(botones, "Módulo Ventas", this::abrirVentas);
                boton(botones, "Módulo Clientes", this::abrirClientes);
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
        setVisible(false);
        new GranjaScreen(usuario, conn, this).setVisible(true);
    }

    private void abrirVentas() {
        setVisible(false);
        new VentasScreen(usuario, conn, this).setVisible(true);
    }

    private void abrirClientes() {
        setVisible(false);
        new ClientesScreen(conn, this).setVisible(true);
    }

    private void abrirReportes() {
        setVisible(false);
        new ReporteLotesScreen(conn, this).setVisible(true);
    }

    private void abrirFinanzas() {
        setVisible(false);
        new ReporteFinancieroScreen(conn, this).setVisible(true);
    }

    private void abrirSaldosCuentas() {
        setVisible(false);
        new ReporteCuentasScreen(conn, this).setVisible(true);
    }

    private void moduloPendiente(String nombre) {
        JOptionPane.showMessageDialog(this, "Módulo " + nombre + " en construcción.");
    }
}
