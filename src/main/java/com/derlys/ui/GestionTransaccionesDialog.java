package com.derlys.ui;

import com.derlys.model.TipoMovimiento;
import com.derlys.model.Usuario;
import com.derlys.repository.TipoMovimientoRepository;
import com.derlys.repository.TransaccionRepository;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.sql.Connection;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class GestionTransaccionesDialog extends JDialog {

    private final TransaccionRepository transaccionRepo;
    private final int loteId;
    private final Usuario usuario;
    private final DefaultTableModel tablaModelo;
    private final JComboBox<TipoMovimiento> comboTipo;
    private final JTextField campoCantidad = new JTextField(6);
    private final JTextField campoMonto = new JTextField(10);
    private final JTextField campoDescripcion = new JTextField(20);
    private final JLabel lblMonto = new JLabel("Monto ($) — ventas/gastos:");

    public GestionTransaccionesDialog(
            JFrame padre, Connection conn, int loteId, String codigoLote, Usuario usuario) {
        super(padre, "Transacciones - " + codigoLote, true);
        this.loteId = loteId;
        this.usuario = usuario;
        transaccionRepo = new TransaccionRepository(conn);

        var tipoRepo = new TipoMovimientoRepository(conn);
        comboTipo = new JComboBox<>(tipoRepo.listar().toArray(new TipoMovimiento[0]));
        comboTipo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    javax.swing.JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof TipoMovimiento tipo) {
                    setText(tipo.nombre() + " — " + tipo.descripcion());
                }
                return this;
            }
        });
        comboTipo.addActionListener(e -> actualizarCampoMonto());
        seleccionarTipoPorDefecto("MUERTE");
        campoCantidad.setText("1");

        setSize(640, 450);
        setLocationRelativeTo(padre);
        setLayout(new BorderLayout(8, 8));

        add(new JLabel("Movimientos del lote " + codigoLote, JLabel.CENTER), BorderLayout.NORTH);

        String[] columnas = {"Fecha", "Tipo", "Cantidad", "Descripción"};
        tablaModelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        add(new JScrollPane(new JTable(tablaModelo)), BorderLayout.CENTER);

        JPanel form = new JPanel(new GridLayout(4, 2, 8, 8));
        form.setBorder(new EmptyBorder(8, 12, 4, 12));
        form.add(new JLabel("Tipo de movimiento:"));
        form.add(comboTipo);
        form.add(new JLabel("Cantidad (pollos/unidades):"));
        form.add(campoCantidad);
        form.add(lblMonto);
        form.add(campoMonto);
        form.add(new JLabel("Descripción:"));
        form.add(campoDescripcion);

        JButton btnRegistrar = new JButton("Registrar");
        btnRegistrar.addActionListener(e -> registrar());

        JButton btnActualizar = new JButton("Actualizar lista");
        btnActualizar.addActionListener(e -> cargarTransacciones());

        JButton btnVolver = new JButton("← Volver");
        btnVolver.addActionListener(e -> dispose());

        JPanel abajo = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
        abajo.add(btnRegistrar);
        abajo.add(btnActualizar);
        abajo.add(btnVolver);

        JPanel sur = new JPanel(new BorderLayout());
        sur.add(form, BorderLayout.CENTER);
        sur.add(abajo, BorderLayout.SOUTH);
        add(sur, BorderLayout.SOUTH);

        actualizarCampoMonto();
        cargarTransacciones();
    }

    private void actualizarCampoMonto() {
        boolean requiere = requiereMonto();
        lblMonto.setEnabled(requiere);
        campoMonto.setEnabled(requiere);
        if (!requiere) {
            campoMonto.setText("");
        }
    }

    private boolean requiereMonto() {
        TipoMovimiento tipo = (TipoMovimiento) comboTipo.getSelectedItem();
        if (tipo == null) {
            return false;
        }
        String nombre = tipo.nombre();
        return "VENTA".equals(nombre) || "GASTO_OPERATIVO".equals(nombre) || "INVERSION_ACTIVO".equals(nombre);
    }

    private void seleccionarTipoPorDefecto(String nombre) {
        for (int i = 0; i < comboTipo.getItemCount(); i++) {
            if (comboTipo.getItemAt(i).nombre().equalsIgnoreCase(nombre)) {
                comboTipo.setSelectedIndex(i);
                return;
            }
        }
    }

    private void cargarTransacciones() {
        tablaModelo.setRowCount(0);
        for (var t : transaccionRepo.listarPorLote(loteId)) {
            tablaModelo.addRow(new Object[] {
                t.fecha(), t.tipoNombre(), t.cantidadUnidades(), t.descripcion()
            });
        }
    }

    private boolean permiteCantidadCero() {
        TipoMovimiento tipo = (TipoMovimiento) comboTipo.getSelectedItem();
        if (tipo == null) {
            return false;
        }
        String n = tipo.nombre();
        return "GASTO_OPERATIVO".equals(n) || "INVERSION_ACTIVO".equals(n);
    }

    private void registrar() {
        try {
            int cantidad = Integer.parseInt(campoCantidad.getText().trim());
            if (cantidad < 0 || (cantidad == 0 && !permiteCantidadCero())) {
                JOptionPane.showMessageDialog(this, "Indica una cantidad válida (0 solo en gastos/equipos).");
                return;
            }

            String descripcion = campoDescripcion.getText().trim();
            if (descripcion.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Escribe una descripción.");
                return;
            }

            TipoMovimiento tipo = (TipoMovimiento) comboTipo.getSelectedItem();
            Double monto = null;
            if (requiereMonto()) {
                monto = Double.parseDouble(campoMonto.getText().trim());
                if (monto <= 0) {
                    JOptionPane.showMessageDialog(this, "El monto debe ser mayor que 0 para ventas y gastos.");
                    return;
                }
            }

            transaccionRepo.crear(loteId, usuario.id(), tipo.nombre(), tipo.id(), cantidad, descripcion, monto);

            campoCantidad.setText("");
            campoMonto.setText("");
            campoDescripcion.setText("");
            cargarTransacciones();
            JOptionPane.showMessageDialog(this, "Transacción registrada correctamente.");
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Cantidad o monto inválido.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
