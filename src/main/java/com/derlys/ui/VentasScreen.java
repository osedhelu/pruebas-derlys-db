package com.derlys.ui;

import com.derlys.model.LoteReporte;
import com.derlys.model.PreventaDetalle;
import com.derlys.model.PreventaEstados;
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
import java.util.ArrayList;
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
import javax.swing.border.TitledBorder;
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
    private List<PreventaDetalle> preventasCargadas = new ArrayList<>();
    private DefaultTableModel tablaPreventas;
    private JTable tablaPreventasView;

    private JComboBox<Usuario> comboCliente;
    private JComboBox<LoteReporte> comboLotePreventa;
    private JTextField campoCantidadPreventa;

    private JLabel lblPreventaParaCobro;
    private JTextField campoMontoPreventa;
    private JTextField campoDescPreventa;
    private JButton btnCobrarPreventa;

    private JLabel lblPreventaParaEstado;
    private JComboBox<String> comboEstadoPreventa;
    private JTextField campoMontoEntrega;
    private JTextField campoNotasEntrega;
    private JButton btnCambiarEstado;

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
        setSize(980, 640);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane pestanas = new JTabbedPane();
        pestanas.add("Preventas", panelPreventas());
        pestanas.add("Venta directa", panelVenta());
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

        JLabel instrucciones = new JLabel(
                "Tabla con teléfono para llamar al cliente. Abajo: apartar, cobrar o cambiar estado (listo, mora, entregado…).",
                JLabel.CENTER);
        panel.add(instrucciones, BorderLayout.NORTH);

        String[] columnas = {"ID", "Cliente", "Teléfono", "Lote", "Cant.", "A cobrar ($)", "Fecha", "Estado"};
        tablaPreventas = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaPreventasView = new JTable(tablaPreventas);
        tablaPreventasView.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                actualizarInfoPreventaSeleccionada();
            }
        });
        JPanel tablaPanel = new JPanel(new BorderLayout());
        tablaPanel.setBorder(new TitledBorder("Preventas registradas"));
        tablaPanel.add(new JScrollPane(tablaPreventasView), BorderLayout.CENTER);
        panel.add(tablaPanel, BorderLayout.CENTER);

        JTabbedPane subPestanas = new JTabbedPane();

        // --- Pestaña 1: solo apartar (nueva preventa) ---
        comboCliente = new JComboBox<>();
        comboLotePreventa = crearComboLotes();
        campoCantidadPreventa = new JTextField(10);

        JPanel panelApartar = new JPanel(new BorderLayout(8, 8));
        panelApartar.setBorder(new EmptyBorder(8, 8, 8, 8));

        JPanel formApartar = new JPanel(new GridLayout(3, 2, 10, 10));
        formApartar.setBorder(new TitledBorder("Datos de la nueva preventa"));
        formApartar.add(new JLabel("Cliente:"));
        formApartar.add(comboCliente);
        formApartar.add(new JLabel("Lote:"));
        formApartar.add(comboLotePreventa);
        formApartar.add(new JLabel("Pollos a apartar:"));
        formApartar.add(campoCantidadPreventa);

        JButton btnApartar = new JButton("Registrar preventa (apartar)");
        btnApartar.addActionListener(e -> registrarPreventa());

        panelApartar.add(formApartar, BorderLayout.CENTER);
        panelApartar.add(btnApartar, BorderLayout.SOUTH);
        subPestanas.addTab("1. Apartar pollos (nueva)", panelApartar);

        // --- Pestaña 2: solo cobrar (preventa seleccionada en tabla) ---
        JPanel panelCobrar = new JPanel(new BorderLayout(8, 8));
        panelCobrar.setBorder(new EmptyBorder(8, 8, 8, 8));

        lblPreventaParaCobro = new JLabel("↑ Selecciona una preventa PENDIENTE en la tabla", JLabel.CENTER);
        lblPreventaParaCobro.setBorder(new EmptyBorder(4, 4, 8, 4));

        campoMontoPreventa = new JTextField(12);
        campoDescPreventa = new JTextField(28);
        campoDescPreventa.setText("Venta por preventa — pago del cliente");

        JPanel formCobrar = new JPanel(new GridLayout(2, 2, 10, 10));
        formCobrar.setBorder(new TitledBorder("Datos de la venta al cobrar"));
        formCobrar.add(new JLabel("Monto que pagó ($):"));
        formCobrar.add(campoMontoPreventa);
        formCobrar.add(new JLabel("Descripción:"));
        formCobrar.add(campoDescPreventa);

        btnCobrarPreventa = new JButton("Cobrar preventa seleccionada");
        btnCobrarPreventa.setEnabled(false);
        btnCobrarPreventa.addActionListener(e -> completarPreventaSeleccionada());

        panelCobrar.add(lblPreventaParaCobro, BorderLayout.NORTH);
        panelCobrar.add(formCobrar, BorderLayout.CENTER);
        panelCobrar.add(btnCobrarPreventa, BorderLayout.SOUTH);
        subPestanas.addTab("2. Cobrar preventa", panelCobrar);

        JPanel panelEstado = new JPanel(new BorderLayout(8, 8));
        panelEstado.setBorder(new EmptyBorder(8, 8, 8, 8));

        lblPreventaParaEstado = new JLabel("↑ Selecciona una preventa en la tabla", JLabel.CENTER);
        lblPreventaParaEstado.setBorder(new EmptyBorder(4, 4, 8, 4));

        comboEstadoPreventa = new JComboBox<>();
        for (String codigo : PreventaEstados.MANUALES) {
            comboEstadoPreventa.addItem(codigo);
        }
        comboEstadoPreventa.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    javax.swing.JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof String codigo) {
                    setText(PreventaEstados.etiqueta(codigo));
                }
                return this;
            }
        });

        comboEstadoPreventa.addActionListener(e -> actualizarCamposEntregaVisibles());

        campoMontoEntrega = new JTextField(12);
        campoNotasEntrega = new JTextField(28);
        campoNotasEntrega.setToolTipText("Ej: 30 pollos, pesaron 45 kg, $8.500/kg");

        JPanel formEstado = new JPanel(new GridLayout(3, 2, 10, 10));
        formEstado.setBorder(new TitledBorder("Estado y monto al entregar"));
        formEstado.add(new JLabel("Estado:"));
        formEstado.add(comboEstadoPreventa);
        formEstado.add(new JLabel("Monto a cobrar ($):"));
        formEstado.add(campoMontoEntrega);
        formEstado.add(new JLabel("Notas (peso, detalle):"));
        formEstado.add(campoNotasEntrega);

        btnCambiarEstado = new JButton("Guardar estado / monto");
        btnCambiarEstado.setEnabled(false);
        btnCambiarEstado.addActionListener(e -> cambiarEstadoPreventa());

        panelEstado.add(lblPreventaParaEstado, BorderLayout.NORTH);
        panelEstado.add(formEstado, BorderLayout.CENTER);
        panelEstado.add(btnCambiarEstado, BorderLayout.SOUTH);
        subPestanas.addTab("3. Cambiar estado", panelEstado);

        panel.add(subPestanas, BorderLayout.SOUTH);
        actualizarInfoPreventaSeleccionada();

        return panel;
    }

    private void actualizarInfoPreventaSeleccionada() {
        actualizarInfoPreventaCobro();
        actualizarInfoPreventaEstado();
    }

    private static String textoTelefono(PreventaDetalle p) {
        String tel = p.clienteTelefono();
        if (tel == null || tel.isBlank()) {
            return "sin teléfono";
        }
        return tel;
    }

    private static String formatearMonto(Double monto) {
        if (monto == null || monto <= 0) {
            return "—";
        }
        if (monto == Math.rint(monto)) {
            return String.format("$%,.0f", monto);
        }
        return String.format("$%,.2f", monto);
    }

    private void actualizarCamposEntregaVisibles() {
        String sel = (String) comboEstadoPreventa.getSelectedItem();
        boolean entregado = PreventaEstados.ENTREGADO.equalsIgnoreCase(sel);
        campoMontoEntrega.setEnabled(entregado);
        campoNotasEntrega.setEnabled(entregado);
    }

    private void actualizarInfoPreventaCobro() {
        PreventaDetalle p = preventaSeleccionada();
        if (p == null) {
            lblPreventaParaCobro.setText("↑ Selecciona una fila en la tabla de arriba");
            btnCobrarPreventa.setEnabled(false);
            campoMontoPreventa.setText("");
            return;
        }
        if (!PreventaEstados.puedeCobrarse(p.estado())) {
            lblPreventaParaCobro.setText(String.format(
                    "Preventa #%d | %s | %s — estado: %s (ya no se puede cobrar aquí)",
                    p.id(),
                    p.clienteNombre(),
                    textoTelefono(p),
                    PreventaEstados.etiqueta(p.estado())));
            btnCobrarPreventa.setEnabled(false);
            campoMontoPreventa.setText("");
            return;
        }
        StringBuilder info = new StringBuilder(String.format(
                "Cobrar #%d | %s | Tel: %s | Lote %s | %d pollos | %s",
                p.id(),
                p.clienteNombre(),
                textoTelefono(p),
                p.codigoLote(),
                p.cantidadApartada(),
                PreventaEstados.etiqueta(p.estado())));
        if (p.montoACobrar() != null && p.montoACobrar() > 0) {
            info.append("\n→ Monto acordado al entregar: ").append(formatearMonto(p.montoACobrar()));
            if (p.notasEntrega() != null && !p.notasEntrega().isBlank()) {
                info.append(" (").append(p.notasEntrega()).append(")");
            }
            campoMontoPreventa.setText(String.valueOf(p.montoACobrar()));
        } else {
            campoMontoPreventa.setText("");
        }
        lblPreventaParaCobro.setText(info.toString());
        btnCobrarPreventa.setEnabled(true);
    }

    private void actualizarInfoPreventaEstado() {
        PreventaDetalle p = preventaSeleccionada();
        if (p == null) {
            lblPreventaParaEstado.setText("↑ Selecciona una preventa en la tabla");
            btnCambiarEstado.setEnabled(false);
            campoMontoEntrega.setText("");
            campoNotasEntrega.setText("");
            return;
        }
        String estado = p.estado() == null ? "" : p.estado().toLowerCase();
        StringBuilder info = new StringBuilder(String.format(
                "#%d | %s | Tel: %s | %d pollos | Estado: %s",
                p.id(), p.clienteNombre(), textoTelefono(p), p.cantidadApartada(),
                PreventaEstados.etiqueta(p.estado())));
        if (p.montoACobrar() != null && p.montoACobrar() > 0) {
            info.append(" | A cobrar: ").append(formatearMonto(p.montoACobrar()));
        }
        lblPreventaParaEstado.setText(info.toString());

        if (PreventaEstados.estaCobrada(estado)) {
            seleccionarEstadoEnCombo(PreventaEstados.ENTREGADO);
            campoMontoEntrega.setText("");
            campoNotasEntrega.setText("");
            campoMontoEntrega.setEnabled(false);
            campoNotasEntrega.setEnabled(false);
            btnCambiarEstado.setEnabled(true);
            return;
        }

        if (PreventaEstados.ENTREGADO.equals(estado)) {
            seleccionarEstadoEnCombo(PreventaEstados.ENTREGADO);
            if (p.montoACobrar() != null && p.montoACobrar() > 0) {
                campoMontoEntrega.setText(String.valueOf(p.montoACobrar()));
            } else {
                campoMontoEntrega.setText("");
            }
            campoNotasEntrega.setText(p.notasEntrega() != null ? p.notasEntrega() : "");
        } else {
            seleccionarEstadoEnCombo(estado);
            campoMontoEntrega.setText("");
            campoNotasEntrega.setText("");
        }
        actualizarCamposEntregaVisibles();
        btnCambiarEstado.setEnabled(true);
    }

    private void seleccionarEstadoEnCombo(String codigoEstado) {
        if (codigoEstado == null) {
            return;
        }
        for (int i = 0; i < comboEstadoPreventa.getItemCount(); i++) {
            if (codigoEstado.equalsIgnoreCase(comboEstadoPreventa.getItemAt(i))) {
                comboEstadoPreventa.setSelectedIndex(i);
                return;
            }
        }
    }

    private void cambiarEstadoPreventa() {
        try {
            PreventaDetalle p = preventaSeleccionada();
            if (p == null) {
                JOptionPane.showMessageDialog(this, "Selecciona una preventa en la tabla.");
                return;
            }
            String nuevo = (String) comboEstadoPreventa.getSelectedItem();
            if (nuevo == null) {
                return;
            }

            Double montoEntrega = null;
            String notas = campoNotasEntrega.getText().trim();
            if (PreventaEstados.ENTREGADO.equalsIgnoreCase(nuevo)) {
                try {
                    montoEntrega = Double.parseDouble(campoMontoEntrega.getText().trim());
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Indica cuánto debe pagar el cliente (monto numérico).\n"
                                    + "Ej: pesaron 45 kg a $8.500 → escribe el total.");
                    return;
                }
                if (montoEntrega <= 0) {
                    JOptionPane.showMessageDialog(this, "El monto a cobrar debe ser mayor que 0.");
                    return;
                }
            }

            if (nuevo.equalsIgnoreCase(p.estado())) {
                if (PreventaEstados.ENTREGADO.equalsIgnoreCase(nuevo)) {
                    preventaRepo.actualizarDatosEntrega(p.id(), montoEntrega, notas);
                    refrescarDatos();
                    JOptionPane.showMessageDialog(
                            this,
                            "Monto a cobrar actualizado: " + formatearMonto(montoEntrega));
                    return;
                }
                JOptionPane.showMessageDialog(this, "La preventa ya tiene ese estado.");
                return;
            }

            String msg = "¿Cambiar preventa #" + p.id() + " (" + p.clienteNombre() + ")?\n"
                    + "De: " + PreventaEstados.etiqueta(p.estado()) + "\n"
                    + "A: " + PreventaEstados.etiqueta(nuevo);
            if (PreventaEstados.ENTREGADO.equalsIgnoreCase(nuevo)) {
                msg += "\n\nMonto a cobrar: " + formatearMonto(montoEntrega);
                if (!notas.isBlank()) {
                    msg += "\nNotas: " + notas;
                }
            }
            int confirmar = JOptionPane.showConfirmDialog(this, msg, "Confirmar", JOptionPane.YES_NO_OPTION);
            if (confirmar != JOptionPane.YES_OPTION) {
                return;
            }

            preventaRepo.cambiarEstado(p.id(), nuevo, montoEntrega, notas);
            refrescarDatos();
            String ok = "Estado: " + PreventaEstados.etiqueta(nuevo);
            if (PreventaEstados.ENTREGADO.equalsIgnoreCase(nuevo)) {
                ok += "\nQuedó registrado a cobrar: " + formatearMonto(montoEntrega);
            }
            JOptionPane.showMessageDialog(this, ok);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel panelVenta() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        panel.add(
                new JLabel("Venta sin preventa previa (el cliente no apartó antes)", JLabel.CENTER),
                BorderLayout.NORTH);

        comboLoteVenta = crearComboLotes();
        campoCantidadVenta = new JTextField(8);
        campoMontoVenta = new JTextField(12);
        campoDescVenta = new JTextField(25);
        campoDescVenta.setText("Venta de pollos - Pago en efectivo");

        JPanel form = new JPanel(new GridLayout(4, 2, 10, 10));
        form.setBorder(new TitledBorder("Registrar venta"));
        form.add(new JLabel("Lote:"));
        form.add(comboLoteVenta);
        form.add(new JLabel("Cantidad de pollos:"));
        form.add(campoCantidadVenta);
        form.add(new JLabel("Monto total ($):"));
        form.add(campoMontoVenta);
        form.add(new JLabel("Descripción:"));
        form.add(campoDescVenta);

        JButton btnVender = new JButton("Registrar venta directa");
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
        Integer lotePreventaId = idLoteSeleccionado(comboLotePreventa);
        Integer loteVentaId = idLoteSeleccionado(comboLoteVenta);
        Integer clienteId = idClienteSeleccionado();

        lotesDisponibles = loteRepo.listarReporteDetallado();

        comboLotePreventa.setModel(new DefaultComboBoxModel<>(lotesDisponibles.toArray(new LoteReporte[0])));
        comboLoteVenta.setModel(new DefaultComboBoxModel<>(lotesDisponibles.toArray(new LoteReporte[0])));
        seleccionarLote(comboLotePreventa, lotePreventaId);
        seleccionarLote(comboLoteVenta, loteVentaId);

        var clientes = usuarioRepo.listarClientes();
        comboCliente.setModel(new DefaultComboBoxModel<>(clientes.toArray(new Usuario[0])));
        comboCliente.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    javax.swing.JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Usuario u) {
                    String contacto = u.telefono() != null && !u.telefono().isBlank()
                            ? u.telefono()
                            : (u.email() != null ? u.email() : "");
                    setText(u.nombre() + (contacto.isBlank() ? "" : " — " + contacto));
                }
                return this;
            }
        });
        seleccionarCliente(clienteId);

        cargarPreventas();
    }

    private static Integer idLoteSeleccionado(JComboBox<LoteReporte> combo) {
        Object item = combo.getSelectedItem();
        return item instanceof LoteReporte lote ? lote.id() : null;
    }

    private static void seleccionarLote(JComboBox<LoteReporte> combo, Integer loteId) {
        if (loteId == null) {
            return;
        }
        for (int i = 0; i < combo.getItemCount(); i++) {
            LoteReporte lote = combo.getItemAt(i);
            if (lote != null && lote.id().equals(loteId)) {
                combo.setSelectedIndex(i);
                return;
            }
        }
    }

    private Integer idClienteSeleccionado() {
        Object item = comboCliente.getSelectedItem();
        return item instanceof Usuario u ? u.id() : null;
    }

    private void seleccionarCliente(Integer clienteId) {
        if (clienteId == null) {
            return;
        }
        for (int i = 0; i < comboCliente.getItemCount(); i++) {
            Usuario u = comboCliente.getItemAt(i);
            if (u != null && u.id().equals(clienteId)) {
                comboCliente.setSelectedIndex(i);
                return;
            }
        }
    }

    private void cargarPreventas() {
        preventasCargadas = preventaRepo.listarDetalle();
        tablaPreventas.setRowCount(0);
        for (PreventaDetalle p : preventasCargadas) {
            String tel = p.clienteTelefono();
            tablaPreventas.addRow(new Object[] {
                p.id(),
                p.clienteNombre(),
                tel == null || tel.isBlank() ? "—" : tel,
                p.codigoLote(),
                p.cantidadApartada(),
                formatearMonto(p.montoACobrar()),
                p.fechaApartado(),
                PreventaEstados.etiqueta(p.estado())
            });
        }
        actualizarInfoPreventaSeleccionada();
    }

    private PreventaDetalle preventaSeleccionada() {
        int fila = tablaPreventasView.getSelectedRow();
        if (fila < 0 || fila >= preventasCargadas.size()) {
            return null;
        }
        return preventasCargadas.get(fila);
    }

    private void completarPreventaSeleccionada() {
        try {
            PreventaDetalle p = preventaSeleccionada();
            if (p == null) {
                JOptionPane.showMessageDialog(this, "Selecciona una preventa pendiente en la tabla de arriba.");
                return;
            }
            if (!PreventaEstados.puedeCobrarse(p.estado())) {
                JOptionPane.showMessageDialog(
                        this,
                        "Esa preventa no se puede cobrar (estado: "
                                + PreventaEstados.etiqueta(p.estado())
                                + ").");
                return;
            }

            double monto = Double.parseDouble(campoMontoPreventa.getText().trim());
            String descripcion = campoDescPreventa.getText().trim();

            int confirmar = JOptionPane.showConfirmDialog(
                    this,
                    "¿Cobrar preventa #" + p.id() + "?\n"
                            + "Cliente: " + p.clienteNombre() + "\n"
                            + "Teléfono: " + textoTelefono(p) + "\n"
                            + "Lote: " + p.codigoLote() + "\n"
                            + "Pollos: " + p.cantidadApartada() + "\n"
                            + "Monto: $" + monto,
                    "Confirmar venta",
                    JOptionPane.YES_NO_OPTION);
            if (confirmar != JOptionPane.YES_OPTION) {
                return;
            }

            preventaRepo.completarPreventa(p.id(), vendedor.id(), monto, descripcion);

            campoMontoPreventa.setText("");
            refrescarDatos();
            JOptionPane.showMessageDialog(
                    this,
                    "Preventa cobrada.\n"
                            + "Estado: Cobrada\n"
                            + (PreventaEstados.ENTREGADO.equalsIgnoreCase(p.estado())
                                    ? "Los pollos ya estaban entregados.\n"
                                    : "Puedes marcar «Entregado» en Cambiar estado si aún no se entregaron.\n")
                            + "Venta registrada por "
                            + p.cantidadApartada()
                            + " pollos.");
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Monto inválido.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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

            int loteId = lote.id();
            int clienteIdSel = cliente.id();
            preventaRepo.crear(clienteIdSel, loteId, cantidad);
            campoCantidadPreventa.setText("");
            refrescarDatos();
            seleccionarLote(comboLotePreventa, loteId);
            seleccionarCliente(clienteIdSel);
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

            int loteId = lote.id();
            transaccionRepo.crear(loteId, vendedor.id(), "VENTA", 1, cantidad, descripcion, monto);

            campoCantidadVenta.setText("");
            campoMontoVenta.setText("");
            refrescarDatos();
            seleccionarLote(comboLoteVenta, loteId);
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
