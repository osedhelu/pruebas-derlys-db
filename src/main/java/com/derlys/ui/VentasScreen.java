package com.derlys.ui;

import com.derlys.model.LoteReporte;
import com.derlys.model.Usuario;
import com.derlys.repository.LoteRepository;
import com.derlys.repository.PreventaRepository;
import com.derlys.repository.TransaccionRepository;
import com.derlys.repository.UsuarioRepository;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.sql.Connection;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class VentasScreen extends JFrame {

    private final Usuario vendedor;
    private final Connection conn;
    private final JFrame menuPrincipal;
    private final LoteRepository loteRepo;
    private final PreventaRepository preventaRepo;
    private final TransaccionRepository transaccionRepo;
    private final UsuarioRepository usuarioRepo;

    private List<LoteReporte> lotesDisponibles;
    private DefaultTableModel tablaPreventas;

    private JComboBox<Usuario> comboCliente;
    private JComboBox<LoteReporte> comboLotePreventa;
    private JTextField campoCantidadPreventa;

    private JComboBox<LoteReporte> comboLoteVenta;
    private JTextField campoCantidadVenta;
    private JTextField campoMontoVenta;
    private JTextField campoDescVenta;

    public VentasScreen(Usuario vendedor, Connection conn, JFrame menuPrincipal) {
        this.vendedor = vendedor;
        this.conn = conn;
        this.menuPrincipal = menuPrincipal;
        loteRepo = new LoteRepository(conn);
        preventaRepo = new PreventaRepository(conn);
        transaccionRepo = new TransaccionRepository(conn);
        usuarioRepo = new UsuarioRepository(conn);

        setTitle("Ventas - " + vendedor.nombre());
        setSize(820, 520);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane pestanas = new JTabbedPane();
        pestanas.add("Preventas (apartar pollos)", panelPreventas());
        pestanas.add("Vender pollos", panelVenta());
        add(pestanas, BorderLayout.CENTER);

        JPanel abajo = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        JButton btnActualizar = new JButton("Actualizar todo");
        btnActualizar.addActionListener(e -> refrescarDatos());
        JButton btnVolver = new JButton("Volver");
        btnVolver.addActionListener(e -> volver());
        abajo.add(btnActualizar);
        abajo.add(btnVolver);
        add(abajo, BorderLayout.SOUTH);

        refrescarDatos();
    }

    private JPanel panelPreventas() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(new EmptyBorder(8, 8, 8, 8));

        String[] columnas = {"Cliente", "Lote", "Cantidad", "Fecha", "Estado"};
        tablaPreventas = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        panel.add(new JScrollPane(new JTable(tablaPreventas)), BorderLayout.CENTER);

        comboCliente = new JComboBox<>();
        comboLotePreventa = crearComboLotes();
        campoCantidadPreventa = new JTextField(8);

        JPanel form = new JPanel(new GridLayout(3, 2, 8, 8));
        form.add(new JLabel("Cliente:"));
        form.add(comboCliente);
        form.add(new JLabel("Lote:"));
        form.add(comboLotePreventa);
        form.add(new JLabel("Pollos a apartar:"));
        form.add(campoCantidadPreventa);

        JButton btnApartar = new JButton("Registrar preventa");
        btnApartar.addActionListener(e -> registrarPreventa());

        JPanel sur = new JPanel(new BorderLayout());
        sur.add(form, BorderLayout.CENTER);
        sur.add(btnApartar, BorderLayout.SOUTH);
        panel.add(sur, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel panelVenta() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        comboLoteVenta = crearComboLotes();
        campoCantidadVenta = new JTextField(8);
        campoMontoVenta = new JTextField(12);
        campoDescVenta = new JTextField(25);
        campoDescVenta.setText("Venta de pollos - Pago en efectivo");

        JPanel form = new JPanel(new GridLayout(4, 2, 10, 10));
        form.add(new JLabel("Lote:"));
        form.add(comboLoteVenta);
        form.add(new JLabel("Cantidad de pollos:"));
        form.add(campoCantidadVenta);
        form.add(new JLabel("Monto total ($):"));
        form.add(campoMontoVenta);
        form.add(new JLabel("Descripción:"));
        form.add(campoDescVenta);

        JButton btnVender = new JButton("Registrar venta");
        btnVender.addActionListener(e -> registrarVenta());

        panel.add(form, BorderLayout.CENTER);
        panel.add(btnVender, BorderLayout.SOUTH);
        return panel;
    }

    private JComboBox<LoteReporte> crearComboLotes() {
        JComboBox<LoteReporte> combo = new JComboBox<>();
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    javax.swing.JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof LoteReporte lote) {
                    setText(lote.codigoLote() + " — disponibles: " + lote.disponibleVenta());
                }
                return this;
            }
        });
        return combo;
    }

    private void refrescarDatos() {
        lotesDisponibles = loteRepo.listarReporteDetallado();

        comboLotePreventa.setModel(new DefaultComboBoxModel<>(lotesDisponibles.toArray(new LoteReporte[0])));
        comboLoteVenta.setModel(new DefaultComboBoxModel<>(lotesDisponibles.toArray(new LoteReporte[0])));

        var clientes = usuarioRepo.listarClientes();
        comboCliente.setModel(new DefaultComboBoxModel<>(clientes.toArray(new Usuario[0])));
        comboCliente.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    javax.swing.JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Usuario u) {
                    setText(u.nombre() + " (" + u.email() + ")");
                }
                return this;
            }
        });

        cargarPreventas();
    }

    private void cargarPreventas() {
        tablaPreventas.setRowCount(0);
        for (var p : preventaRepo.listarDetalle()) {
            tablaPreventas.addRow(new Object[] {
                p.clienteNombre(),
                p.codigoLote(),
                p.cantidadApartada(),
                p.fechaApartado(),
                p.estado()
            });
        }
    }

    private void registrarPreventa() {
        try {
            if (comboCliente.getItemCount() == 0) {
                JOptionPane.showMessageDialog(this, "No hay clientes registrados (rol cliente en usuarios).");
                return;
            }
            if (comboLotePreventa.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(this, "No hay lotes disponibles.");
                return;
            }

            Usuario cliente = (Usuario) comboCliente.getSelectedItem();
            LoteReporte lote = (LoteReporte) comboLotePreventa.getSelectedItem();
            int cantidad = Integer.parseInt(campoCantidadPreventa.getText().trim());

            if (cantidad <= 0) {
                JOptionPane.showMessageDialog(this, "La cantidad debe ser mayor que 0.");
                return;
            }
            if (cantidad > lote.disponibleVenta()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Solo hay " + lote.disponibleVenta() + " pollos disponibles en " + lote.codigoLote() + ".");
                return;
            }

            preventaRepo.crear(cliente.id(), lote.id(), cantidad);
            campoCantidadPreventa.setText("");
            refrescarDatos();
            JOptionPane.showMessageDialog(
                    this,
                    cliente.nombre() + " apartó " + cantidad + " pollos del lote " + lote.codigoLote());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Cantidad inválida.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void registrarVenta() {
        try {
            if (comboLoteVenta.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(this, "Selecciona un lote.");
                return;
            }

            LoteReporte lote = (LoteReporte) comboLoteVenta.getSelectedItem();
            int cantidad = Integer.parseInt(campoCantidadVenta.getText().trim());
            double monto = Double.parseDouble(campoMontoVenta.getText().trim());
            String descripcion = campoDescVenta.getText().trim();

            if (cantidad <= 0 || monto <= 0) {
                JOptionPane.showMessageDialog(this, "Cantidad y monto deben ser mayores que 0.");
                return;
            }
            if (descripcion.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Escribe una descripción.");
                return;
            }
            if (cantidad > lote.disponibleVenta()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Solo hay " + lote.disponibleVenta() + " pollos disponibles en " + lote.codigoLote() + ".");
                return;
            }

            transaccionRepo.crear(
                    lote.id(), vendedor.id(), "VENTA", 1, cantidad, descripcion, monto);

            campoCantidadVenta.setText("");
            campoMontoVenta.setText("");
            refrescarDatos();
            JOptionPane.showMessageDialog(
                    this, "Venta registrada: " + cantidad + " pollos del lote " + lote.codigoLote());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Cantidad o monto inválido.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void volver() {
        dispose();
        menuPrincipal.setVisible(true);
    }
}
