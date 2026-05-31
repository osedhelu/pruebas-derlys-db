package com.derlys.ui;

import com.derlys.model.Lote;
import com.derlys.model.LoteReporte;
import com.derlys.model.MovimientoLote;
import com.derlys.repository.HistorialLoteRepository;
import com.derlys.repository.LoteRepository;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.sql.Connection;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class HistorialLoteScreen extends JFrame {

    private static final NumberFormat DINERO = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));

    private final HistorialLoteRepository historialRepo;
    private final LoteRepository loteRepo;
    private final JFrame pantallaAnterior;
    private final DefaultTableModel tablaModelo;
    private final JLabel lblResumen = new JLabel(" ", JLabel.CENTER);
    private final JComboBox<Lote> comboLote;

    public HistorialLoteScreen(Connection conn, JFrame pantallaAnterior) {
        this.pantallaAnterior = pantallaAnterior;
        historialRepo = new HistorialLoteRepository(conn);
        loteRepo = new LoteRepository(conn);
        List<Lote> lotes = loteRepo.listar();

        setTitle("Historial del lote — Administrador");
        setSize(900, 500);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));

        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.setBorder(new EmptyBorder(8, 12, 4, 12));

        JPanel filtro = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        filtro.add(new JLabel("Lote:"));
        comboLote = new JComboBox<>(lotes.toArray(new Lote[0]));
        comboLote.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    javax.swing.JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Lote l) {
                    setText(l.codigoLote() + " — " + l.raza());
                }
                return this;
            }
        });
        comboLote.addActionListener(e -> cargarHistorial());

        JButton btnActualizar = new JButton("Actualizar");
        btnActualizar.addActionListener(e -> cargarHistorial());

        JButton btnVolver = new JButton("← Volver al menú");
        btnVolver.addActionListener(e -> volver());

        filtro.add(comboLote);
        filtro.add(btnActualizar);
        filtro.add(btnVolver);

        cabecera.add(filtro, BorderLayout.CENTER);
        cabecera.add(lblResumen, BorderLayout.SOUTH);
        cabecera.add(
                new JLabel("Ventas, compras, muertes, preventas y más — ordenado del más reciente al más antiguo", JLabel.CENTER),
                BorderLayout.NORTH);
        add(cabecera, BorderLayout.NORTH);

        String[] columnas = {"Fecha", "Tipo", "Descripción", "Cantidad", "Monto", "Quién", "Estado"};
        tablaModelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        add(new JScrollPane(new JTable(tablaModelo)), BorderLayout.CENTER);

        if (!lotes.isEmpty()) {
            cargarHistorial();
        }
    }

    private void cargarHistorial() {
        try {
            Lote lote = (Lote) comboLote.getSelectedItem();
            if (lote == null) {
                return;
            }

            LoteReporte resumen = loteRepo.obtenerReporteDetallado(lote.id());
            if (resumen != null) {
                lblResumen.setText(String.format(
                        "%s | Iniciales: %d | Salidas: %d | Apartados: %d | Disponibles: %d | Días de vida: %d",
                        resumen.codigoLote(),
                        resumen.cantidadInicial(),
                        resumen.totalSalidas(),
                        resumen.totalApartado(),
                        resumen.disponibleVenta(),
                        resumen.diasVida()));
            }

            tablaModelo.setRowCount(0);
            List<MovimientoLote> movimientos = historialRepo.listarHistorial(lote.id());
            if (movimientos.isEmpty()) {
                tablaModelo.addRow(new Object[] {"—", "—", "Sin movimientos registrados", "—", "—", "—", "—"});
                return;
            }

            for (MovimientoLote m : movimientos) {
                tablaModelo.addRow(new Object[] {
                    m.fecha(),
                    m.tipo(),
                    m.descripcion(),
                    m.cantidad() > 0 ? m.cantidad() : "—",
                    m.monto() != null ? formatear(m.monto()) : "—",
                    m.responsable(),
                    m.estado() != null ? m.estado() : "—"
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static String formatear(double valor) {
        return DINERO.format(valor);
    }

    private void volver() {
        dispose();
        pantallaAnterior.setVisible(true);
    }
}
