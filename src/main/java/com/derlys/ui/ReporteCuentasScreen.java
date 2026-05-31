package com.derlys.ui;

import com.derlys.model.CuentaSaldo;
import com.derlys.model.Lote;
import com.derlys.repository.ContabilidadRepository;
import com.derlys.repository.LoteRepository;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.sql.Connection;
import java.text.NumberFormat;
import java.util.ArrayList;
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

public class ReporteCuentasScreen extends JFrame {

    private static final NumberFormat DINERO = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));

    private final ContabilidadRepository contabilidadRepo;
    private final JFrame pantallaAnterior;
    private final DefaultTableModel tablaModelo;
    private final JLabel lblFiltro = new JLabel(" ", JLabel.CENTER);
    private final List<Lote> lotes;
    private JComboBox<FiltroLote> comboLote;

    public ReporteCuentasScreen(Connection conn, JFrame pantallaAnterior) {
        this.pantallaAnterior = pantallaAnterior;
        contabilidadRepo = new ContabilidadRepository(conn);
        lotes = new LoteRepository(conn).listar();
        List<FiltroLote> opcionesFiltro = crearOpcionesFiltro();

        setTitle("Plan de cuentas — Saldos");
        setSize(780, 480);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));

        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.setBorder(new EmptyBorder(8, 12, 4, 12));
        cabecera.add(new JLabel("Saldos por cuenta (debe, haber y saldo)", JLabel.CENTER), BorderLayout.NORTH);

        JPanel filtroPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        filtroPanel.add(new JLabel("Ver movimientos de:"));
        comboLote = new JComboBox<>(opcionesFiltro.toArray(new FiltroLote[0]));
        comboLote.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    javax.swing.JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof FiltroLote f) {
                    setText(f.etiqueta());
                }
                return this;
            }
        });
        comboLote.addActionListener(e -> cargarSaldos());
        filtroPanel.add(comboLote);

        JButton btnActualizar = new JButton("Actualizar");
        btnActualizar.addActionListener(e -> cargarSaldos());
        filtroPanel.add(btnActualizar);

        JButton btnVolver = new JButton("← Volver al menú");
        btnVolver.addActionListener(e -> volver());

        filtroPanel.add(btnVolver);
        cabecera.add(filtroPanel, BorderLayout.CENTER);
        cabecera.add(lblFiltro, BorderLayout.SOUTH);
        add(cabecera, BorderLayout.NORTH);

        String[] columnas = {"Código", "Cuenta", "Naturaleza", "Total debe", "Total haber", "Saldo"};
        tablaModelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        add(new JScrollPane(new JTable(tablaModelo)), BorderLayout.CENTER);

        cargarSaldos();
    }

    private List<FiltroLote> crearOpcionesFiltro() {
        List<FiltroLote> opciones = new ArrayList<>();
        opciones.add(new FiltroLote(null, "Todos los lotes"));
        for (Lote lote : lotes) {
            opciones.add(new FiltroLote(lote.id(), lote.codigoLote()));
        }
        return opciones;
    }

    private void cargarSaldos() {
        try {
            FiltroLote filtro = (FiltroLote) comboLote.getSelectedItem();
            Integer loteId = filtro != null ? filtro.loteId() : null;

            lblFiltro.setText(filtro != null ? "Mostrando: " + filtro.etiqueta() : "");

            tablaModelo.setRowCount(0);
            double sumaDebe = 0;
            double sumaHaber = 0;

            for (CuentaSaldo c : contabilidadRepo.listarSaldosPorCuenta(loteId)) {
                tablaModelo.addRow(new Object[] {
                    c.codigo(),
                    c.nombre(),
                    c.naturaleza(),
                    formatear(c.totalDebe()),
                    formatear(c.totalHaber()),
                    formatear(c.saldo())
                });
                sumaDebe += c.totalDebe();
                sumaHaber += c.totalHaber();
            }

            tablaModelo.addRow(new Object[] {
                "", "TOTALES", "", formatear(sumaDebe), formatear(sumaHaber), formatear(sumaDebe - sumaHaber)
            });
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

    private record FiltroLote(Integer loteId, String etiqueta) {
    }
}
