package com.derlys.ui;

import com.derlys.model.LoteReporte;
import com.derlys.repository.LoteRepository;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.sql.Connection;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class ReporteLotesScreen extends JFrame {

    private final LoteRepository loteRepo;
    private final JFrame pantallaAnterior;
    private final DefaultTableModel tablaModelo;
    private final JLabel lblTotales = new JLabel(" ", JLabel.CENTER);

    public ReporteLotesScreen(Connection conn, JFrame pantallaAnterior) {
        this.pantallaAnterior = pantallaAnterior;
        loteRepo = new LoteRepository(conn);

        setTitle("Reporte detallado de lotes");
        setSize(920, 480);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));

        lblTotales.setBorder(new EmptyBorder(10, 12, 0, 12));
        add(lblTotales, BorderLayout.NORTH);

        String[] columnas = {
            "Código",
            "Pollos iniciales",
            "Fecha entrada",
            "Días de vida",
            "Días p/ sacrificio",
            "Salidas",
            "Apartados",
            "Disponible venta"
        };
        tablaModelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        add(new JScrollPane(new JTable(tablaModelo)), BorderLayout.CENTER);

        JButton btnActualizar = new JButton("Actualizar");
        btnActualizar.addActionListener(e -> cargarReporte());

        JButton btnVolver = new JButton("Volver");
        btnVolver.addActionListener(e -> volver());

        JPanel abajo = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        abajo.add(btnActualizar);
        abajo.add(btnVolver);
        add(abajo, BorderLayout.SOUTH);

        cargarReporte();
    }

    private void cargarReporte() {
        try {
            tablaModelo.setRowCount(0);
            int totalInicial = 0;
            int totalSalidas = 0;
            int totalApartado = 0;
            int totalDisponible = 0;

            for (LoteReporte r : loteRepo.listarReporteDetallado()) {
                tablaModelo.addRow(new Object[] {
                    r.codigoLote(),
                    r.cantidadInicial(),
                    r.fechaEntrada(),
                    r.diasVida(),
                    r.diasParaSacrificio(),
                    r.totalSalidas(),
                    r.totalApartado(),
                    r.disponibleVenta()
                });
                totalInicial += r.cantidadInicial();
                totalSalidas += r.totalSalidas();
                totalApartado += r.totalApartado();
                totalDisponible += r.disponibleVenta();
            }

            lblTotales.setText(String.format(
                    "Totales → Pollos: %d | Salidas: %d | Apartados: %d | Disponibles para venta: %d",
                    totalInicial, totalSalidas, totalApartado, totalDisponible));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void volver() {
        dispose();
        pantallaAnterior.setVisible(true);
    }
}
