package com.derlys.ui;

import com.derlys.model.LoteFinanciero;
import com.derlys.repository.FinanzasRepository;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.sql.Connection;
import java.text.NumberFormat;
import java.util.Locale;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class ReporteFinancieroScreen extends JFrame {

    private static final NumberFormat DINERO = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));

    private final FinanzasRepository finanzasRepo;
    private final JFrame pantallaAnterior;
    private final DefaultTableModel tablaModelo;
    private final JLabel lblResumen = new JLabel(" ", JLabel.CENTER);

    public ReporteFinancieroScreen(Connection conn, JFrame pantallaAnterior) {
        this.pantallaAnterior = pantallaAnterior;
        finanzasRepo = new FinanzasRepository(conn);

        setTitle("Rentabilidad por lote — Administrador");
        setSize(900, 480);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));

        JPanel norte = new JPanel(new BorderLayout());
        lblResumen.setBorder(new EmptyBorder(10, 12, 4, 12));
        norte.add(lblResumen, BorderLayout.NORTH);
        norte.add(
                new JLabel(
                        "Ingresos = ventas en contabilidad | Gastos = alimento, medicinas y equipos",
                        JLabel.CENTER),
                BorderLayout.SOUTH);
        add(norte, BorderLayout.NORTH);

        String[] columnas = {
            "Código lote", "Pollos vendidos", "Ingresos", "Gastos", "Resultado", "Estado"
        };
        tablaModelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable tabla = new JTable(tablaModelo);
        tabla.getColumnModel().getColumn(5).setCellRenderer(new EstadoRenderer());
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        JButton btnActualizar = new JButton("Actualizar");
        btnActualizar.addActionListener(e -> cargar());

        JButton btnVolver = new JButton("Volver");
        btnVolver.addActionListener(e -> volver());

        JPanel abajo = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        abajo.add(btnActualizar);
        abajo.add(btnVolver);
        add(abajo, BorderLayout.SOUTH);

        cargar();
    }

    private void cargar() {
        try {
            tablaModelo.setRowCount(0);
            double totalIngresos = 0;
            double totalGastos = 0;
            int lotesPerdida = 0;
            int lotesGanancia = 0;

            for (LoteFinanciero f : finanzasRepo.listarRentabilidadPorLote()) {
                tablaModelo.addRow(new Object[] {
                    f.codigoLote(),
                    f.pollosVendidos(),
                    formatear(f.ingresos()),
                    formatear(f.gastos()),
                    formatear(f.resultado()),
                    f.estado()
                });
                totalIngresos += f.ingresos();
                totalGastos += f.gastos();
                if ("PÉRDIDA".equals(f.estado())) {
                    lotesPerdida++;
                } else if ("GANANCIA".equals(f.estado())) {
                    lotesGanancia++;
                }
            }

            double resultadoGlobal = totalIngresos - totalGastos;
            String estadoGlobal = resultadoGlobal < 0 ? "PÉRDIDA global" : (resultadoGlobal > 0 ? "GANANCIA global" : "Equilibrio");

            lblResumen.setText(String.format(
                    "%s | Ingresos: %s | Gastos: %s | Resultado: %s | Lotes en ganancia: %d | Lotes en pérdida: %d",
                    estadoGlobal,
                    formatear(totalIngresos),
                    formatear(totalGastos),
                    formatear(resultadoGlobal),
                    lotesGanancia,
                    lotesPerdida));
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

    private static class EstadoRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (!isSelected && value != null) {
                String estado = value.toString();
                if ("PÉRDIDA".equals(estado)) {
                    c.setBackground(new Color(255, 220, 220));
                } else if ("GANANCIA".equals(estado)) {
                    c.setBackground(new Color(220, 255, 220));
                } else {
                    c.setBackground(Color.WHITE);
                }
            }
            return c;
        }
    }
}
