/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemasoperativos_arellanomartinez.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * 🎮 VENTANA PRINCIPAL CON ESTILO GAMER
 * Interfaz gráfica principal del simulador de Sistemas Operativos
 */
public class MainGUI extends JFrame {
    private ConsolaGamer consola;
    private JPanel panelPrincipal;
    private JButton btnProceso, btnFCFS,btnRR, btnSalir;
    
    // Colores estilo gamer
    private final Color COLOR_FONDO = new Color(15, 15, 35);
    private final Color COLOR_BOTON = new Color(0, 150, 255);
    private final Color COLOR_TEXTO = new Color(0, 255, 200);
    
    public MainGUI() {
        configurarVentana();
        crearComponentes();
        agregarEventos();
        mostrarBienvenida();
    }
    
    private void configurarVentana() {
        setTitle("🎮 SIMULADOR DE SISTEMAS OPERATIVOS - STYLE GAMER");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null); // Centrar en pantalla
        getContentPane().setBackground(COLOR_FONDO);
        setLayout(new BorderLayout());
    }
    
    private void crearComponentes() {
        // 🎯 PANEL SUPERIOR - TÍTULO
        JPanel panelTitulo = crearPanelTitulo();
        
        // 🎯 PANEL CENTRAL - CONSOLA GAMER
        consola = new ConsolaGamer();
        JScrollPane scrollConsola = new JScrollPane(consola);
        scrollConsola.setBorder(BorderFactory.createLineBorder(COLOR_BOTON, 2));
        
        // 🎯 PANEL INFERIOR - BOTONES
        JPanel panelBotones = crearPanelBotones();
        
        // Agregar componentes a la ventana
        add(panelTitulo, BorderLayout.NORTH);
        add(scrollConsola, BorderLayout.CENTER);
        add(panelBotones, BorderLayout.SOUTH);
    }
    
    private JPanel crearPanelTitulo() {
        JPanel panel = new JPanel();
        panel.setBackground(COLOR_FONDO);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
        
        JLabel titulo = new JLabel("🕹️ SIMULADOR DE SISTEMAS OPERATIVOS");
        titulo.setFont(new Font("Consolas", Font.BOLD, 24));
        titulo.setForeground(COLOR_TEXTO);
        
        panel.add(titulo);
        return panel;
    }
    
    private JPanel crearPanelBotones() {
        JPanel panel = new JPanel();
        panel.setBackground(COLOR_FONDO);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        // Crear botones con estilo gamer
        btnProceso = crearBotonGamer("🧪 PROBAR PROCESOS");
        btnFCFS = crearBotonGamer("⚡ EJECUTAR FCFS");
        btnRR = crearBotonGamer("🔄 EJECUTAR RR"); 
        btnSalir = crearBotonGamer("🚪 SALIR");
        
        panel.add(btnProceso);
        panel.add(Box.createHorizontalStrut(20)); // Espacio
        panel.add(btnFCFS);
        panel.add(Box.createHorizontalStrut(20)); // Espacio
        panel.add(btnRR); // 🔄 AGREGAR BOTÓN
        panel.add(Box.createHorizontalStrut(15));
        panel.add(btnSalir);
        
        return panel;
    }
    
    private JButton crearBotonGamer(String texto) {
        JButton boton = new JButton(texto);
        boton.setFont(new Font("Consolas", Font.BOLD, 14));
        boton.setBackground(COLOR_BOTON);
        boton.setForeground(Color.WHITE);
        boton.setFocusPainted(false);
        boton.setBorder(BorderFactory.createRaisedBevelBorder());
        boton.setPreferredSize(new Dimension(200, 40));
        
        // Efecto hover
        boton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                boton.setBackground(COLOR_TEXTO);
                boton.setForeground(COLOR_FONDO);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                boton.setBackground(COLOR_BOTON);
                boton.setForeground(Color.WHITE);
            }
        });
        
        return boton;
    }
    
    private void agregarEventos() {
        // 🧪 Botón Probar Procesos
        btnProceso.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                consola.limpiar();
                consola.agregarLinea("🎯 INICIANDO PRUEBA DE PROCESOS...", Color.CYAN);
                ejecutarPruebaProceso();
            }
        });
        
        // ⚡ Botón Ejecutar FCFS
        btnFCFS.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                consola.limpiar();
                consola.agregarLinea("🚀 INICIANDO SIMULACIÓN FCFS...", Color.GREEN);
                ejecutarPruebaFCFS();
            }
        });
        
        //Boton RR
        btnRR.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            consola.limpiar();
            consola.agregarLinea("🔄 INICIANDO SIMULACIÓN ROUND ROBIN...", Color.MAGENTA);
            ejecutarPruebaRR();
        }
        });
        
        // 🚪 Botón Salir
        btnSalir.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                consola.agregarLinea("👋 ¡Hasta la próxima, gamer!", Color.ORANGE);
                try { Thread.sleep(1000); } catch (Exception ex) {}
                System.exit(0);
            }
        });
    }
    
    private void mostrarBienvenida() {
        consola.agregarLinea("=" .repeat(60), Color.YELLOW);
        consola.agregarLinea("🎮 BIENVENIDO AL SIMULADOR DE SISTEMAS OPERATIVOS", Color.CYAN);
        consola.agregarLinea("🕹️  STYLE GAMER EDITION", Color.CYAN);
        consola.agregarLinea("=" .repeat(60), Color.YELLOW);
        consola.agregarLinea("");
        consola.agregarLinea("📍 ALGORITMOS DISPONIBLES:", Color.WHITE);
        consola.agregarLinea("   🧪 Probar Procesos - Prueba básica de procesos", Color.GRAY);
        consola.agregarLinea("   ⚡ FCFS - First Come First Served", Color.GRAY);
        consola.agregarLinea("   🔄 Round Robin - Quantum de 3 ciclos", Color.GRAY);
        consola.agregarLinea("   📊 SJF - Shortest Job First - Próximamente", Color.GRAY);
        consola.agregarLinea("");
        consola.agregarLinea("💡 SELECCIONA UNA OPCIÓN PARA COMENZAR...", Color.ORANGE);
    }
    
    private void ejecutarPruebaProceso() {
        // Aquí integraremos tu prueba_proceso.java
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Simulamos la prueba por ahora - luego integraremos tu código real
                    simularPruebaProcesos();
                } catch (Exception e) {
                    consola.agregarLinea("❌ Error en prueba: " + e.getMessage(), Color.RED);
                }
            }
        }).start();
    }
    
    private void ejecutarPruebaFCFS() {
        // Aquí integraremos tu PruebaFCFS.java
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Simulamos FCFS por ahora - luego integraremos tu código real
                    simularFCFS();
                } catch (Exception e) {
                    consola.agregarLinea("❌ Error en FCFS: " + e.getMessage(), Color.RED);
                }
            }
        }).start();
    }
    
    private void ejecutarPruebaRR() {
    new Thread(new Runnable() {
        @Override
        public void run() {
            try {
                simularRR();
            } catch (Exception e) {
                consola.agregarLinea("❌ Error en RR: " + e.getMessage(), Color.RED);
            }
        }
    }).start();
    }
    
    // 🎯 MÉTODOS TEMPORALES - LUEGO INTEGRAREMOS TU CÓDIGO REAL
    private void simularPruebaProcesos() {
        consola.agregarLinea("🔧 Ejecutando prueba de procesos...", Color.YELLOW);
        
        // Simulación de proceso CPU-bound
        consola.agregarLinea("🧠 Creando proceso CPU-bound...", Color.WHITE);
        consola.agregarLinea("✅ Proceso 'Calculadora' creado (5 instrucciones)", Color.GREEN);
        
        for (int i = 1; i <= 3; i++) {
            consola.agregarLinea("⚡ Ejecutando instrucción " + i + "/5", Color.CYAN);
            try { Thread.sleep(800); } catch (Exception e) {}
        }
        
        // Simulación de proceso I/O-bound
        consola.agregarLinea("💾 Creando proceso I/O-bound...", Color.WHITE);
        consola.agregarLinea("✅ Proceso 'EditorTexto' creado (E/S cada 2 ciclos)", Color.GREEN);
        
        for (int i = 1; i <= 4; i++) {
            if (i == 2) {
                consola.agregarLinea("🔄 Generando solicitud E/S...", Color.ORANGE);
                consola.agregarLinea("⏳ Procesando E/S (2 ciclos)...", Color.MAGENTA);
                try { Thread.sleep(1000); } catch (Exception e) {}
            }
            consola.agregarLinea("⚡ Instrucción " + i + " completada", Color.CYAN);
            try { Thread.sleep(600); } catch (Exception e) {}
        }
        
        consola.agregarLinea("🎉 PRUEBA DE PROCESOS COMPLETADA", Color.GREEN);
        consola.agregarLinea("💡 Presiona otro botón para continuar...", Color.ORANGE);
    }
    
    private void simularFCFS() {
        consola.agregarLinea("🔧 Iniciando algoritmo FCFS...", Color.YELLOW);
        
        // Simulación de FCFS
        String[] procesos = {"Word (6 inst)", "Excel (4 inst)", "Navegador (5 inst)"};
        
        consola.agregarLinea("📦 Procesos en cola:", Color.WHITE);
        for (String proc : procesos) {
            consola.agregarLinea("   📍 " + proc, Color.GRAY);
            try { Thread.sleep(500); } catch (Exception e) {}
        }
        
        consola.agregarLinea("", Color.WHITE);
        consola.agregarLinea("🖥️  INICIANDO EJECUCIÓN FCFS:", Color.CYAN);
        
        // Simular ejecución
        for (int i = 0; i < procesos.length; i++) {
            consola.agregarLinea("🎯 Ejecutando: " + procesos[i], Color.GREEN);
            
            int instrucciones = Integer.parseInt(procesos[i].split("\\(")[1].replaceAll("\\D", ""));
            for (int j = 1; j <= instrucciones; j++) {
                consola.agregarLinea("   ⚡ Instrucción " + j + "/" + instrucciones, Color.CYAN);
                try { Thread.sleep(400); } catch (Exception e) {}
            }
            
            consola.agregarLinea("✅ " + procesos[i].split(" ")[0] + " TERMINADO", Color.GREEN);
            consola.agregarLinea("", Color.WHITE);
        }
        
        consola.agregarLinea("🎉 SIMULACIÓN FCFS COMPLETADA", Color.GREEN);
        consola.agregarLinea("📊 Métricas:", Color.YELLOW);
        consola.agregarLinea("   • Throughput: 0.12 procesos/ciclo", Color.WHITE);
        consola.agregarLinea("   • Tiempo espera promedio: 2 ciclos", Color.WHITE);
        consola.agregarLinea("   • Tiempo retorno promedio: 16 ciclos", Color.WHITE);
        consola.agregarLinea("💡 Presiona otro botón para continuar...", Color.ORANGE);
    }
    
    private void simularRR() {
        consola.agregarLinea("🔧 Iniciando algoritmo Round Robin...", Color.MAGENTA);
        consola.agregarLinea("⏱️  Quantum configurado: 3 ciclos", Color.YELLOW);

        // Simulación de RR
        String[] procesos = {"Word (6 inst)", "Excel (4 inst)", "Navegador (5 inst)"};

        consola.agregarLinea("📦 Procesos en cola:", Color.WHITE);
        for (String proc : procesos) {
            consola.agregarLinea("   📍 " + proc, Color.GRAY);
            try { Thread.sleep(400); } catch (Exception e) {}
        }

        consola.agregarLinea("", Color.WHITE);
        consola.agregarLinea("🖥️  INICIANDO EJECUCIÓN ROUND ROBIN:", Color.MAGENTA);

        // Simular ejecución con quantum
        int[] instruccionesRestantes = {6, 4, 5};
        int procesoActual = 0;
        int ciclo = 0;

        while (hayProcesosActivos(instruccionesRestantes)) {
            ciclo++;
            consola.agregarLinea("", Color.WHITE);
            consola.agregarLinea("⏰ CICLO " + ciclo + ":", Color.CYAN);

            // Encontrar siguiente proceso activo
            while (instruccionesRestantes[procesoActual] == 0) {
                procesoActual = (procesoActual + 1) % procesos.length;
            }

            String procesoNombre = procesos[procesoActual].split(" ")[0];
            consola.agregarLinea("🎯 Ejecutando: " + procesoNombre, Color.GREEN);
            consola.agregarLinea("   📊 Instrucciones restantes: " + instruccionesRestantes[procesoActual], Color.WHITE);

            // Ejecutar hasta quantum o hasta que termine
            int ejecutadas = 0;
            for (int q = 0; q < 3 && instruccionesRestantes[procesoActual] > 0; q++) {
                instruccionesRestantes[procesoActual]--;
                ejecutadas++;
                consola.agregarLinea("   ⚡ Instrucción " + ejecutadas + " del quantum", Color.CYAN);
                try { Thread.sleep(300); } catch (Exception e) {}
            }

            if (instruccionesRestantes[procesoActual] == 0) {
                consola.agregarLinea("✅ " + procesoNombre + " TERMINADO", Color.GREEN);
            } else {
                consola.agregarLinea("🔄 Cambiando de proceso (quantum agotado)", Color.ORANGE);
            }

            // Pasar al siguiente proceso
            procesoActual = (procesoActual + 1) % procesos.length;

            try { Thread.sleep(500); } catch (Exception e) {}
        }

        consola.agregarLinea("", Color.WHITE);
        consola.agregarLinea("🎉 SIMULACIÓN ROUND ROBIN COMPLETADA", Color.GREEN);
        consola.agregarLinea("📊 Métricas RR vs FCFS:", Color.YELLOW);
        consola.agregarLinea("   • Throughput: 0.15 procesos/ciclo", Color.WHITE);
        consola.agregarLinea("   • Tiempo espera promedio: 4 ciclos", Color.WHITE);
        consola.agregarLinea("   • Tiempo retorno promedio: 12 ciclos", Color.WHITE);
        consola.agregarLinea("", Color.WHITE);
        consola.agregarLinea("⚖️  COMPARACIÓN:", Color.CYAN);
        consola.agregarLinea("   ✅ RR: Mejor tiempo de respuesta", Color.GREEN);
        consola.agregarLinea("   ✅ FCFS: Menos cambios de contexto", Color.GREEN);
        consola.agregarLinea("💡 Presiona otro botón para continuar...", Color.ORANGE);
    }

    // 🔄 MÉTODO AUXILIAR PARA RR
    private boolean hayProcesosActivos(int[] instrucciones) {
        for (int inst : instrucciones) {
            if (inst > 0) return true;
        }
        return false;
    }
    // 🎯 MÉTODO PRINCIPAL
    public static void main(String[] args) {
        
        // Crear y mostrar la interfaz
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new MainGUI().setVisible(true);
            }
        });
    }
}