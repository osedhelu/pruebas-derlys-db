package com.derlys.ui;

import com.derlys.model.Usuario;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.sql.Connection;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;

public class GranjaScreen extends JFrame {

    private final Usuario usuarioActivo;

    // Aprovechamos de pasarle la conexión como venías haciendo para que no se pierda el flujo de la DB
    public GranjaScreen(Usuario usuario) {
        this.usuarioActivo = usuario;

        setTitle("Gestión de Lotes - Granja Derlys");
        setSize(450, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); 
        
        // 1. Configuramos el diseño principal de la ventana
        setLayout(new BorderLayout());

   
        
     

        // 3. CONFIGURACIÓN DE LOS BOTONES (Abajo)
        JButton btnCrear = new JButton("Crear Lote");
        JButton btnListar = new JButton("Listar Lotes");

        // Creamos un panel con diseño horizontal (FlowLayout) para agrupar los botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        panelBotones.add(btnCrear);
        panelBotones.add(btnListar);

        // Añadimos el panel de botones en la zona inferior
        add(panelBotones, BorderLayout.SOUTH);

        // 4. ACCIONES DE LOS BOTONES (ActionListeners)
        btnCrear.addActionListener(e -> {
            // Aquí llamarías a tu lógica o interfaz para crear un lote
            System.out.println("Clic en Crear Lote. Usuario: " + usuarioActivo.nombre());
            
            // Ejemplo: lanzar un JOptionPane rápido para probar:
            // var loteRepo = new LoteRepository(this.connection);
            // loteRepo.crearLote(500, "Ross 308");
        });

        btnListar.addActionListener(e -> {
            // Aquí llamarías a la consulta que hicimos para listar los lotes en la DB
            System.out.println("Clic en Listar Lotes. Conectando a SQLite...");
            
            // Ejemplo:
            // var loteRepo = new LoteRepository(this.connection);
            // var todos = loteRepo.obtenerTodosLosLotes();
            // Lote.printAll(todos);
        });
    }
}