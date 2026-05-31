package com.derlys.ui;

import com.derlys.model.Lote;
import com.derlys.model.Usuario;
import com.derlys.repository.LoteRepository;
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

public class GranjaScreen extends JFrame {

    private final LoteRepository loteRepo;
    private final JFrame menuPrincipal;
    private final DefaultTableModel tablaModelo;
    private final JTextField campoCantidad = new JTextField(8);
    private final JTextField campoRaza = new JTextField(12);

    public GranjaScreen(Usuario usuario, Connection conn, JFrame menuPrincipal) {
        this.menuPrincipal = menuPrincipal;
        loteRepo = new LoteRepository(conn);

        setTitle("Granja - " + usuario.nombre());
        setSize(700, 420);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));

        add(new JLabel("Lotes registrados", JLabel.CENTER), BorderLayout.NORTH);

        String[] columnas = {"ID", "Código", "Fecha", "Cantidad", "Raza", "Estado"};
        tablaModelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable tabla = new JTable(tablaModelo);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel form = new JPanel(new GridLayout(2, 2, 8, 8));
        form.setBorder(new EmptyBorder(8, 12, 4, 12));
        form.add(new JLabel("Cantidad de pollos:"));
        form.add(campoCantidad);
        form.add(new JLabel("Raza (opcional):"));
        campoRaza.setText("Ross 308");
        form.add(campoRaza);

        JButton btnCrear = new JButton("Crear lote");
        btnCrear.addActionListener(e -> crearLote());

        JButton btnActualizar = new JButton("Actualizar lista");
        btnActualizar.addActionListener(e -> cargarLotes());

        JButton btnVolver = new JButton("Volver");
        btnVolver.addActionListener(e -> volverAlMenu());

        JPanel abajo = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        abajo.add(btnCrear);
        abajo.add(btnActualizar);
        abajo.add(btnVolver);

        JPanel sur = new JPanel(new BorderLayout());
        sur.add(form, BorderLayout.CENTER);
        sur.add(abajo, BorderLayout.SOUTH);
        add(sur, BorderLayout.SOUTH);

        cargarLotes();
    }

    private void cargarLotes() {
        tablaModelo.setRowCount(0);
        for (Lote lote : loteRepo.listar()) {
            tablaModelo.addRow(new Object[] {
                lote.id(),
                lote.codigoLote(),
                lote.fechaEntrada(),
                lote.cantidadInicial(),
                lote.raza(),
                lote.estado()
            });
        }
    }

    private void crearLote() {
        try {
            int cantidad = Integer.parseInt(campoCantidad.getText().trim());
            if (cantidad <= 0) {
                JOptionPane.showMessageDialog(this, "La cantidad debe ser mayor que 0.");
                return;
            }

            Lote creado = loteRepo.crearLote(cantidad, campoRaza.getText());
            if (creado == null) {
                JOptionPane.showMessageDialog(this, "No se pudo crear el lote.");
                return;
            }

            campoCantidad.setText("");
            cargarLotes();
            JOptionPane.showMessageDialog(
                    this, "Lote creado: " + creado.codigoLote(), "Éxito", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Escribe un número válido en cantidad.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void volverAlMenu() {
        dispose();
        menuPrincipal.setVisible(true);
    }
}
