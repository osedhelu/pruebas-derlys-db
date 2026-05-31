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
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

public class ClientesScreen extends JFrame {

    private final UsuarioRepository clienteRepo;
    private final JFrame menuPrincipal;
    private final JTable tabla;
    private final DefaultTableModel tablaModelo;

    private final JTextField campoNombreCrear = new JTextField(18);
    private final JTextField campoUsernameCrear = new JTextField(18);
    private final JTextField campoTelefonoCrear = new JTextField(18);
    private final JTextField campoEmailCrear = new JTextField(18);
    private final JPasswordField campoPasswordCrear = new JPasswordField(12);

    private final JLabel lblClienteSeleccionado = new JLabel("↑ Selecciona un cliente en la tabla", JLabel.CENTER);
    private final JTextField campoNombreEditar = new JTextField(18);
    private final JTextField campoUsernameEditar = new JTextField(18);
    private final JTextField campoTelefonoEditar = new JTextField(18);
    private final JTextField campoEmailEditar = new JTextField(18);
    private final JPasswordField campoPasswordEditar = new JPasswordField(12);
    private final JButton btnActualizar = new JButton("Guardar cambios");
    private final JButton btnEliminar = new JButton("Eliminar cliente");

    private Integer clienteEnEdicion;

    public ClientesScreen(Connection conn, JFrame menuPrincipal) {
        this.menuPrincipal = menuPrincipal;
        clienteRepo = new UsuarioRepository(conn);

        setTitle("Clientes");
        setSize(780, 520);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));

        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.setBorder(new EmptyBorder(8, 12, 4, 12));
        cabecera.add(
                new JLabel("Nombre = quién es · Usuario = login · Teléfono = contacto", JLabel.CENTER),
                BorderLayout.CENTER);
        JButton btnVolverMenu = new JButton("← Volver al menú");
        btnVolverMenu.addActionListener(e -> volverAlMenu());
        cabecera.add(btnVolverMenu, BorderLayout.EAST);
        add(cabecera, BorderLayout.NORTH);

        String[] columnas = {"ID", "Nombre", "Usuario", "Teléfono", "Email"};
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

        JPanel tablaPanel = new JPanel(new BorderLayout());
        tablaPanel.setBorder(new TitledBorder("Clientes registrados"));
        tablaPanel.add(new JScrollPane(tabla), BorderLayout.CENTER);
        add(tablaPanel, BorderLayout.CENTER);

        JTabbedPane subPestanas = new JTabbedPane();

        JPanel panelCrear = new JPanel(new BorderLayout(8, 8));
        panelCrear.setBorder(new EmptyBorder(8, 8, 8, 8));

        JPanel formCrear = new JPanel(new GridLayout(5, 2, 10, 10));
        formCrear.setBorder(new TitledBorder("Datos del nuevo cliente"));
        formCrear.add(new JLabel("Nombre (quién es):"));
        formCrear.add(campoNombreCrear);
        formCrear.add(new JLabel("Usuario (login):"));
        formCrear.add(campoUsernameCrear);
        formCrear.add(new JLabel("Teléfono:"));
        formCrear.add(campoTelefonoCrear);
        formCrear.add(new JLabel("Email (opcional):"));
        formCrear.add(campoEmailCrear);
        formCrear.add(new JLabel("Contraseña:"));
        formCrear.add(campoPasswordCrear);

        JButton btnCrear = new JButton("Crear cliente");
        btnCrear.addActionListener(e -> crearCliente());

        panelCrear.add(formCrear, BorderLayout.CENTER);
        panelCrear.add(btnCrear, BorderLayout.SOUTH);
        subPestanas.addTab("1. Crear cliente", panelCrear);

        JPanel panelEditar = new JPanel(new BorderLayout(8, 8));
        panelEditar.setBorder(new EmptyBorder(8, 8, 8, 8));

        lblClienteSeleccionado.setBorder(new EmptyBorder(4, 4, 8, 4));

        JPanel formEditar = new JPanel(new GridLayout(5, 2, 10, 10));
        formEditar.setBorder(new TitledBorder("Editar cliente seleccionado"));
        formEditar.add(new JLabel("Nombre (quién es):"));
        formEditar.add(campoNombreEditar);
        formEditar.add(new JLabel("Usuario (login):"));
        formEditar.add(campoUsernameEditar);
        formEditar.add(new JLabel("Teléfono:"));
        formEditar.add(campoTelefonoEditar);
        formEditar.add(new JLabel("Email (opcional):"));
        formEditar.add(campoEmailEditar);
        formEditar.add(new JLabel("Nueva contraseña (opcional):"));
        formEditar.add(campoPasswordEditar);

        btnActualizar.setEnabled(false);
        btnEliminar.setEnabled(false);
        btnActualizar.addActionListener(e -> actualizarCliente());
        btnEliminar.addActionListener(e -> eliminar());

        JPanel botonesEditar = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
        botonesEditar.add(btnActualizar);
        botonesEditar.add(btnEliminar);

        panelEditar.add(lblClienteSeleccionado, BorderLayout.NORTH);
        panelEditar.add(formEditar, BorderLayout.CENTER);
        panelEditar.add(botonesEditar, BorderLayout.SOUTH);
        subPestanas.addTab("2. Editar / eliminar", panelEditar);

        add(subPestanas, BorderLayout.SOUTH);
        cargarClientes();
        actualizarPanelEditar();
    }

    private void cargarClientes() {
        tablaModelo.setRowCount(0);
        for (Usuario c : clienteRepo.listarClientes()) {
            tablaModelo.addRow(new Object[] {
                c.id(),
                c.nombre(),
                c.username(),
                nuloVacio(c.telefono()),
                nuloVacio(c.email())
            });
        }
        actualizarPanelEditar();
    }

    private static String nuloVacio(String s) {
        return s == null ? "" : s;
    }

    private void cargarSeleccionEnFormulario() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            clienteEnEdicion = null;
            actualizarPanelEditar();
            return;
        }
        clienteEnEdicion = (Integer) tablaModelo.getValueAt(fila, 0);
        Usuario c = clienteRepo.buscarPorId(clienteEnEdicion);
        if (c == null) {
            clienteEnEdicion = null;
            actualizarPanelEditar();
            return;
        }
        campoNombreEditar.setText(c.nombre());
        campoUsernameEditar.setText(c.username());
        campoTelefonoEditar.setText(nuloVacio(c.telefono()));
        campoEmailEditar.setText(nuloVacio(c.email()));
        campoPasswordEditar.setText("");
        actualizarPanelEditar();
    }

    private void actualizarPanelEditar() {
        if (clienteEnEdicion == null) {
            lblClienteSeleccionado.setText("↑ Selecciona un cliente en la tabla de arriba");
            btnActualizar.setEnabled(false);
            btnEliminar.setEnabled(false);
            return;
        }
        lblClienteSeleccionado.setText("Editando cliente ID: " + clienteEnEdicion);
        btnActualizar.setEnabled(true);
        btnEliminar.setEnabled(true);
    }

    private void crearCliente() {
        try {
            String nombre = campoNombreCrear.getText().trim();
            String username = campoUsernameCrear.getText().trim();
            String telefono = campoTelefonoCrear.getText().trim();
            String email = campoEmailCrear.getText().trim();
            String password = new String(campoPasswordCrear.getPassword());

            if (nombre.isEmpty() || username.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nombre y usuario (login) son obligatorios.");
                return;
            }
            if (telefono.isEmpty()) {
                JOptionPane.showMessageDialog(this, "El teléfono es obligatorio para contactar al cliente.");
                return;
            }
            if (password.isBlank()) {
                JOptionPane.showMessageDialog(this, "La contraseña es obligatoria al crear.");
                return;
            }

            clienteRepo.crearCliente(nombre, username, email, telefono, password);

            campoNombreCrear.setText("");
            campoUsernameCrear.setText("");
            campoTelefonoCrear.setText("");
            campoEmailCrear.setText("");
            campoPasswordCrear.setText("");
            cargarClientes();
            JOptionPane.showMessageDialog(this, "Cliente creado correctamente.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void actualizarCliente() {
        if (clienteEnEdicion == null) {
            JOptionPane.showMessageDialog(this, "Selecciona un cliente en la tabla (pestaña Editar).");
            return;
        }
        try {
            String nombre = campoNombreEditar.getText().trim();
            String username = campoUsernameEditar.getText().trim();
            String telefono = campoTelefonoEditar.getText().trim();
            String email = campoEmailEditar.getText().trim();
            String password = new String(campoPasswordEditar.getPassword());

            if (nombre.isEmpty() || username.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nombre y usuario (login) son obligatorios.");
                return;
            }
            if (telefono.isEmpty()) {
                JOptionPane.showMessageDialog(this, "El teléfono es obligatorio.");
                return;
            }

            String pass = password.isBlank() ? null : password;
            clienteRepo.actualizarCliente(clienteEnEdicion, nombre, username, email, telefono, pass);

            cargarClientes();
            JOptionPane.showMessageDialog(this, "Cliente actualizado.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminar() {
        if (clienteEnEdicion == null) {
            JOptionPane.showMessageDialog(this, "Selecciona un cliente en la tabla.");
            return;
        }

        int confirmar = JOptionPane.showConfirmDialog(
                this, "¿Eliminar al cliente seleccionado?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirmar != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            clienteRepo.eliminarCliente(clienteEnEdicion);
            clienteEnEdicion = null;
            tabla.clearSelection();
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
