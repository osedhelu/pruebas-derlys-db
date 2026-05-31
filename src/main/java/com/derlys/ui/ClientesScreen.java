package com.derlys.ui;

import com.derlys.model.Usuario;
import com.derlys.repository.UsuarioRepository;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.sql.Connection;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class ClientesScreen extends JFrame {

    private final UsuarioRepository clienteRepo;
    private final JFrame menuPrincipal;
    private final JTable tabla;
    private final DefaultTableModel tablaModelo;
    private final JTextField campoNombre = new JTextField(18);
    private final JTextField campoEmail = new JTextField(18);
    private final JTextField campoPassword = new JTextField(12);
    private Integer clienteEnEdicion;

    public ClientesScreen(Connection conn, JFrame menuPrincipal) {
        this.menuPrincipal = menuPrincipal;
        clienteRepo = new UsuarioRepository(conn);

        setTitle("Clientes");
        setSize(620, 420);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));

        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.setBorder(new EmptyBorder(8, 12, 4, 12));
        cabecera.add(new JLabel("Crear, editar o eliminar clientes (rol cliente)"), BorderLayout.CENTER);
        JButton btnVolverMenu = new JButton("← Volver al menú");
        btnVolverMenu.addActionListener(e -> volverAlMenu());
        cabecera.add(btnVolverMenu, BorderLayout.EAST);
        add(cabecera, BorderLayout.NORTH);

        String[] columnas = {"ID", "Nombre", "Email"};
        tablaModelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabla = new JTable(tablaModelo);
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarSeleccionEnFormulario();
            }
        });
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel form = new JPanel(new GridLayout(3, 2, 8, 8));
        form.setBorder(new EmptyBorder(8, 12, 4, 12));
        form.add(new JLabel("Nombre:"));
        form.add(campoNombre);
        form.add(new JLabel("Email:"));
        form.add(campoEmail);
        form.add(new JLabel("Contraseña (opcional al editar):"));
        form.add(campoPassword);

        JButton btnNuevo = new JButton("Nuevo");
        btnNuevo.addActionListener(e -> limpiarFormulario());

        JButton btnGuardar = new JButton("Guardar");
        btnGuardar.addActionListener(e -> guardar());

        JButton btnEliminar = new JButton("Eliminar");
        btnEliminar.addActionListener(e -> eliminar());

        JPanel abajo = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
        abajo.add(btnNuevo);
        abajo.add(btnGuardar);
        abajo.add(btnEliminar);

        JPanel sur = new JPanel(new BorderLayout());
        sur.add(form, BorderLayout.CENTER);
        sur.add(abajo, BorderLayout.SOUTH);
        add(sur, BorderLayout.SOUTH);

        cargarClientes();
    }

    private void cargarClientes() {
        tablaModelo.setRowCount(0);
        for (Usuario c : clienteRepo.listarClientes()) {
            tablaModelo.addRow(new Object[] {c.id(), c.nombre(), c.email()});
        }
    }

    private void cargarSeleccionEnFormulario() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            return;
        }
        clienteEnEdicion = (Integer) tablaModelo.getValueAt(fila, 0);
        campoNombre.setText(String.valueOf(tablaModelo.getValueAt(fila, 1)));
        campoEmail.setText(String.valueOf(tablaModelo.getValueAt(fila, 2)));
        campoPassword.setText("");
    }

    private void limpiarFormulario() {
        clienteEnEdicion = null;
        tabla.clearSelection();
        campoNombre.setText("");
        campoEmail.setText("");
        campoPassword.setText("");
    }

    private void guardar() {
        try {
            String nombre = campoNombre.getText().trim();
            String email = campoEmail.getText().trim();
            String password = campoPassword.getText();

            if (nombre.isEmpty() || email.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nombre y email son obligatorios.");
                return;
            }

            if (clienteEnEdicion == null) {
                if (password == null || password.isBlank()) {
                    JOptionPane.showMessageDialog(this, "La contraseña es obligatoria al crear un cliente.");
                    return;
                }
                clienteRepo.crearCliente(nombre, email, password.trim());
                JOptionPane.showMessageDialog(this, "Cliente creado correctamente.");
            } else {
                clienteRepo.actualizarCliente(clienteEnEdicion, nombre, email, password);
                JOptionPane.showMessageDialog(this, "Cliente actualizado.");
            }

            limpiarFormulario();
            cargarClientes();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminar() {
        if (clienteEnEdicion == null) {
            JOptionPane.showMessageDialog(this, "Selecciona un cliente de la tabla.");
            return;
        }

        int confirmar = JOptionPane.showConfirmDialog(
                this, "¿Eliminar al cliente seleccionado?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirmar != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            clienteRepo.eliminarCliente(clienteEnEdicion);
            limpiarFormulario();
            cargarClientes();
            JOptionPane.showMessageDialog(this, "Cliente eliminado.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void volverAlMenu() {
        dispose();
        menuPrincipal.setVisible(true);
    }
}
