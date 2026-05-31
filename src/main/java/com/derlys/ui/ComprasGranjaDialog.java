package com.derlys.ui;

import com.derlys.model.Lote;
import com.derlys.model.Usuario;
import com.derlys.repository.LoteRepository;
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

public class ComprasGranjaDialog extends JDialog {

    private enum ConceptoCompra {
        VITAMINAS("Vitaminas / medicinas", "GASTO_OPERATIVO", 3, "Compra de vitaminas para el lote"),
        BEBEDEROS("Bebederos nuevos", "INVERSION_ACTIVO", 4, "Compra de bebederos nuevos"),
        ABANICO("Abanico / ventilador", "INVERSION_ACTIVO", 4, "Compra de abanico para el galpón"),
        ALIMENTO("Alimento", "GASTO_OPERATIVO", 3, "Compra de alimento para el lote"),
        OTRO_INSUMO("Otro insumo operativo", "GASTO_OPERATIVO", 3, "Compra de insumo para el lote"),
        OTRO_EQUIPO("Otro equipo", "INVERSION_ACTIVO", 4, "Compra de equipo para el lote");

        final String etiqueta;
        final String tipoNombre;
        final int tipoId;
        final String descripcionSugerida;

        ConceptoCompra(String etiqueta, String tipoNombre, int tipoId, String descripcionSugerida) {
            this.etiqueta = etiqueta;
            this.tipoNombre = tipoNombre;
            this.tipoId = tipoId;
            this.descripcionSugerida = descripcionSugerida;
        }
    }

    private final TransaccionRepository transaccionRepo;
    private final Usuario granjero;
    private final DefaultTableModel tablaModelo;
    private final JComboBox<Lote> comboLote;
    private final JComboBox<ConceptoCompra> comboConcepto;
    private final JTextField campoDescripcion = new JTextField(28);
    private final JTextField campoMonto = new JTextField(10);
    private final JTextField campoUnidades = new JTextField(6);

    public ComprasGranjaDialog(JFrame padre, Connection conn, Usuario granjero, Integer loteIdPreseleccionado) {
        super(padre, "Compras e insumos — Granja", true);
        this.granjero = granjero;
        transaccionRepo = new TransaccionRepository(conn);

        var loteRepo = new LoteRepository(conn);
        var lotes = loteRepo.listar();

        comboLote = new JComboBox<>(lotes.toArray(new Lote[0]));
        comboLote.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    javax.swing.JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Lote lote) {
                    setText(lote.codigoLote() + " — " + lote.raza());
                }
                return this;
            }
        });
        if (loteIdPreseleccionado != null) {
            for (int i = 0; i < comboLote.getItemCount(); i++) {
                if (comboLote.getItemAt(i).id().equals(loteIdPreseleccionado)) {
                    comboLote.setSelectedIndex(i);
                    break;
                }
            }
        }
        comboLote.addActionListener(e -> cargarCompras());

        comboConcepto = new JComboBox<>(ConceptoCompra.values());
        comboConcepto.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    javax.swing.JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof ConceptoCompra c) {
                    setText(c.etiqueta);
                }
                return this;
            }
        });
        comboConcepto.addActionListener(e -> aplicarSugerencia());
        campoUnidades.setText("0");

        setSize(700, 480);
        setLocationRelativeTo(padre);
        setLayout(new BorderLayout(8, 8));

        add(new JLabel("Registra compras de insumos y equipos por lote (queda en contabilidad)", JLabel.CENTER),
                BorderLayout.NORTH);

        String[] columnas = {"Fecha", "Tipo", "Descripción", "Monto ref.", "Unidades"};
        tablaModelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        add(new JScrollPane(new JTable(tablaModelo)), BorderLayout.CENTER);

        JPanel form = new JPanel(new GridLayout(5, 2, 8, 8));
        form.setBorder(new EmptyBorder(8, 12, 4, 12));
        form.add(new JLabel("Lote:"));
        form.add(comboLote);
        form.add(new JLabel("Qué compraste:"));
        form.add(comboConcepto);
        form.add(new JLabel("Descripción:"));
        form.add(campoDescripcion);
        form.add(new JLabel("Monto pagado ($):"));
        form.add(campoMonto);
        form.add(new JLabel("Unidades (0 si no aplica):"));
        form.add(campoUnidades);

        JButton btnRegistrar = new JButton("Registrar compra");
        btnRegistrar.addActionListener(e -> registrar());

        JButton btnVolver = new JButton("← Volver");
        btnVolver.addActionListener(e -> dispose());

        JPanel abajo = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        abajo.add(btnRegistrar);
        abajo.add(btnVolver);

        JPanel sur = new JPanel(new BorderLayout());
        sur.add(form, BorderLayout.CENTER);
        sur.add(abajo, BorderLayout.SOUTH);
        add(sur, BorderLayout.SOUTH);

        aplicarSugerencia();
        cargarCompras();
    }

    private void aplicarSugerencia() {
        ConceptoCompra concepto = (ConceptoCompra) comboConcepto.getSelectedItem();
        if (concepto != null) {
            campoDescripcion.setText(concepto.descripcionSugerida);
        }
    }

    private Lote loteSeleccionado() {
        return (Lote) comboLote.getSelectedItem();
    }

    private void cargarCompras() {
        tablaModelo.setRowCount(0);
        Lote lote = loteSeleccionado();
        if (lote == null) {
            return;
        }
        for (var t : transaccionRepo.listarComprasPorLote(lote.id())) {
            tablaModelo.addRow(new Object[] {
                t.fecha(),
                t.tipoNombre(),
                t.descripcion(),
                t.montoReferencia() != null ? t.montoReferencia() : "-",
                t.cantidadUnidades()
            });
        }
    }

    private void registrar() {
        try {
            Lote lote = loteSeleccionado();
            if (lote == null) {
                JOptionPane.showMessageDialog(this, "Selecciona un lote.");
                return;
            }

            ConceptoCompra concepto = (ConceptoCompra) comboConcepto.getSelectedItem();
            String descripcion = campoDescripcion.getText().trim();
            double monto = Double.parseDouble(campoMonto.getText().trim());
            int unidades = Integer.parseInt(campoUnidades.getText().trim());

            if (descripcion.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Escribe una descripción.");
                return;
            }
            if (monto <= 0) {
                JOptionPane.showMessageDialog(this, "El monto debe ser mayor que 0.");
                return;
            }
            if (unidades < 0) {
                JOptionPane.showMessageDialog(this, "Las unidades no pueden ser negativas.");
                return;
            }

            transaccionRepo.crear(
                    lote.id(),
                    granjero.id(),
                    concepto.tipoNombre,
                    concepto.tipoId,
                    unidades,
                    descripcion,
                    monto);

            campoMonto.setText("");
            campoUnidades.setText("0");
            cargarCompras();
            JOptionPane.showMessageDialog(
                    this,
                    "Compra registrada en " + lote.codigoLote() + ".\nEl administrador la verá en Finanzas.");
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Monto o unidades inválidos.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
