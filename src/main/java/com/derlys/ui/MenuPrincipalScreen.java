package com.derlys.ui;

import com.derlys.model.Usuario;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;

public class MenuPrincipalScreen extends JFrame {

    private final Usuario usuarioLogueado;

    public MenuPrincipalScreen(Usuario usuario) {
   
        this.usuarioLogueado = usuario;

        setTitle("El Buen Pollo " + usuario.nombre());
        setSize(450, 150); // Ampliamos un poco el ancho para que quepan bien los botones
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); 

        // 1. Layout Principal
        setLayout(new BorderLayout());

        // 2. Encabezado con datos del Usuario Logueado
        JTextArea areaTexto = new JTextArea();
        areaTexto.setEditable(false); 
        areaTexto.setFont(new Font("Monospaced", Font.PLAIN, 14)); 
        
        String datos = String.format("""
             
                                     Bienvenido
           
             """
                );
        areaTexto.setText(datos);
        add(areaTexto, BorderLayout.CENTER);

    
        // 3. Panel Inferior para los Botones de los Módulos
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 15));

        // 4. VALIDACIÓN DE ROLES (Control de Acceso Dinámico)
        int rol = usuarioLogueado.rolId();

        if (rol == 1) { // 1 | ADMINISTRADOR: Acceso Total (4 botones)
            JButton btnGranja = new JButton("Módulo Granja");
            JButton btnVentas = new JButton("Módulo Ventas");
            JButton btnClientes = new JButton("Módulo Clientes");
            JButton btnReportes = new JButton("Reportes");

            panelBotones.add(btnGranja);
            panelBotones.add(btnVentas);
            panelBotones.add(btnClientes);
            panelBotones.add(btnReportes);

            // Asignar acciones a los botones del Admin
            btnGranja.addActionListener(e -> abrirModuloGranja());
            btnVentas.addActionListener(e -> abrirModuloVentas());
            btnClientes.addActionListener(e -> abrirModuloClientes());
            btnReportes.addActionListener(e -> JOptionPane.showMessageDialog(this, "Abriendo Reportes..."));

        } else if (rol == 2) { // 2 | GRANJERO: Solo módulo de granja
            JButton btnGranja = new JButton("Módulo Granja");
            panelBotones.add(btnGranja);

            btnGranja.addActionListener(e -> abrirModuloGranja());

        } else if (rol == 3) { // 3 | VENDEDOR: Ventas y Clientes
            JButton btnVentas = new JButton("Módulo Ventas");
            JButton btnClientes = new JButton("Módulo Clientes");
            
            panelBotones.add(btnVentas);
            panelBotones.add(btnClientes);

            btnVentas.addActionListener(e -> abrirModuloVentas());
            btnClientes.addActionListener(e -> abrirModuloClientes());

        } else { // 4 | CLIENTE o cualquier otro rol no configurado
            JTextArea txtInfo = new JTextArea("\n No tienes módulos asignados para tu rol.");
            txtInfo.setEditable(false);
            panelBotones.add(txtInfo);
        }

        // Añadimos el contenedor de botones abajo
        add(panelBotones, BorderLayout.SOUTH);
    }

    // =========================================================================
    // MÉTODOS AUXILIARES PARA CONTROLAR LA NAVEGACIÓN ENTRE PANTALLAS
    // =========================================================================
    
    private void abrirModuloGranja() {
        var PantallaGranja = new GranjaScreen(usuarioLogueado);
        PantallaGranja.setVisible(true);
    }

    private void abrirModuloVentas() {
        System.out.println("Navegando al Módulo de Ventas...");
        // Aquí instanciarías tu VentasScreen cuando la crees
    }

    private void abrirModuloClientes() {
        System.out.println("Navegando al Módulo de Clientes...");
        // Aquí instanciarías tu ClientesScreen cuando la crees
    }
}