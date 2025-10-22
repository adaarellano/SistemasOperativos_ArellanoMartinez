/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemasoperativos_arellanomartinez.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import sistemasoperativos_arellanomartinez.Controller.Engine;
import sistemasoperativos_arellanomartinez.Planificador.*;
import sistemasoperativos_arellanomartinez.Simulador.Proceso;
import sistemasoperativos_arellanomartinez.Simulador.*;
import sistemasoperativos_arellanomartinez.Controller.*;
import edd.ListaSimple;

/**
 * VENTANA PRINCIPAL CON ESTILO GAMER
 * Interfaz gráfica principal del simulador de Sistemas Operativos
 * @author Day
 */
public class MainGUI extends JFrame {
    private ConsolaGamer consola;
    private JButton btnIniciar, btnSalir, btnAgregarProceso, guardar, cargar;
    private JSlider sliderVelocidad;
    private JComboBox<String> selectorAlgoritmo;
    private Engine motorSimulacionActual;
    private ListaSimple procesosParaSimular;
    
    // Colores estilo gamer
    private final Color COLOR_FONDO = new Color(15, 15, 35);
    private final Color COLOR_BOTON = new Color(0, 150, 255);
    private final Color COLOR_TEXTO = new Color(0, 255, 200);
    
    // --- NUEVOS COMPONENTES PARA EL DASHBOARD ---
    private DefaultListModel<String> modeloListaListos;
    private JList<String> listaListos;
    
    private DefaultListModel<String> modeloListaBloqueados;
    private JList<String> listaBloqueados;

    private JTextArea areaInfoProceso;
    private JLabel labelProcesoCPU;
    
    public MainGUI() {
        this.procesosParaSimular = new ListaSimple(); // <-- INICIALIZA LA LISTA
        configurarVentana();
        crearComponentes();
        agregarEventos();
        mostrarBienvenida();
        
    }
    
    private void configurarVentana() {
        setTitle("🎮 SIMULADOR DE SISTEMAS OPERATIVOS");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 800);
        setLocationRelativeTo(null); // Centrar en pantalla
        getContentPane().setBackground(COLOR_FONDO);
        setLayout(new BorderLayout());
    }
    
    private void crearComponentes() {
        // 🎯 PANEL SUPERIOR - TÍTULO
        JPanel panelTitulo = crearPanelTitulo();
        
        // --- NUEVO: Panel principal para el dashboard ---
        JPanel panelDashboard = new JPanel(new GridLayout(1, 3, 10, 10)); // 1 fila, 3 columnas
        panelDashboard.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panelDashboard.setBackground(COLOR_FONDO);

        // Columna 1: Cola de Listos
        panelDashboard.add(crearPanelCola("Procesos Listos"));
        
        // Columna 2: CPU y Cola de Bloqueados
        JPanel panelCentro = new JPanel(new BorderLayout(10, 10));
        panelCentro.setBackground(COLOR_FONDO);
        panelCentro.add(crearPanelCPU(), BorderLayout.NORTH);
        panelCentro.add(crearPanelCola("Procesos Bloqueados"));
        panelDashboard.add(panelCentro);

        // Columna 3: Información del PCB y Consola de Logs
        JPanel panelDerecha = new JPanel(new BorderLayout(10, 10));
        panelDerecha.setOpaque(false);
        panelDerecha.add(crearPanelInfoPCB(), BorderLayout.NORTH);

        // 🎯 PANEL CENTRAL - CONSOLA GAMER
        consola = new ConsolaGamer();
        JScrollPane scrollConsola = new JScrollPane(consola);
        scrollConsola.setBorder(BorderFactory.createLineBorder(COLOR_BOTON, 2));
        panelDerecha.add(scrollConsola, BorderLayout.CENTER);
        panelDashboard.add(panelDerecha);
        
        // 🎯 PANEL INFERIOR - BOTONES 
        JPanel panelBotones = crearPanelBotones();
        
        // Agregar componentes a la ventana
        add(panelTitulo, BorderLayout.NORTH);
        add(scrollConsola, BorderLayout.CENTER);
        add(panelBotones, BorderLayout.SOUTH);
    }
    
    private JPanel crearPanelCola(String titulo) {
        // Inicialización del modelo y la lista
        DefaultListModel<String> modelo = new DefaultListModel<>();
        JList<String> lista = new JList<>(modelo);

        lista.setBackground(new Color(20, 20, 40));
        lista.setForeground(Color.WHITE);
        lista.setFont(new Font("Consolas", Font.PLAIN, 14));

        // Asigna las variables de instancia de la clase
        if (titulo.contains("Listos")) {
            this.modeloListaListos = modelo;
            this.listaListos = lista;
        } else {
            this.modeloListaBloqueados = modelo;
            this.listaBloqueados = lista;
        }

        JPanel panel = new JPanel(new BorderLayout());
        JLabel labelTitulo = new JLabel(titulo, SwingConstants.CENTER);
        labelTitulo.setForeground(COLOR_TEXTO);
        labelTitulo.setFont(new Font("Consolas", Font.BOLD, 16));

        panel.add(labelTitulo, BorderLayout.NORTH);
        panel.add(new JScrollPane(lista), BorderLayout.CENTER);
        panel.setBorder(BorderFactory.createLineBorder(COLOR_BOTON, 1));
        panel.setOpaque(false);

        return panel;
    }

    private JPanel crearPanelCPU() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GREEN), "CPU en Ejecución", 0, 0, new Font("Consolas", Font.BOLD, 16), Color.GREEN));

        labelProcesoCPU = new JLabel("LIBRE", SwingConstants.CENTER);
        labelProcesoCPU.setFont(new Font("Consolas", Font.BOLD, 20));
        labelProcesoCPU.setForeground(Color.WHITE);

        panel.add(labelProcesoCPU, BorderLayout.CENTER);
        panel.setPreferredSize(new Dimension(100, 120));

        return panel;
    }

        private JPanel crearPanelInfoPCB() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(COLOR_BOTON), "Info del Proceso (PCB)", 0, 0, new Font("Consolas", Font.BOLD, 16), COLOR_TEXTO));

        areaInfoProceso = new JTextArea("Selecciona un proceso...");
        areaInfoProceso.setEditable(false);
        areaInfoProceso.setBackground(new Color(20, 20, 40));
        areaInfoProceso.setForeground(Color.WHITE);
        areaInfoProceso.setFont(new Font("Monospaced", Font.PLAIN, 12));

        panel.add(new JScrollPane(areaInfoProceso), BorderLayout.CENTER);
        panel.setPreferredSize(new Dimension(100, 120));

        return panel;
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
        JPanel panel = new JPanel(new GridLayout(2, 0, 10, 10));
        panel.setBackground(COLOR_FONDO);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Botones de control de procesos
        btnAgregarProceso = crearBotonGamer("Añadir Proceso");
        guardar = crearBotonGamer("Guardar");
        cargar = crearBotonGamer("Cargar archivo");
        btnSalir = crearBotonGamer("🚪 SALIR");

        // Botón para iniciar la simulación
        btnIniciar = crearBotonGamer("▶ Iniciar Simulación");

        // Menú desplegable para seleccionar el algoritmo
        selectorAlgoritmo = new JComboBox<>();
        selectorAlgoritmo.addItem("FCFS");
        selectorAlgoritmo.addItem("Round Robin");
        selectorAlgoritmo.addItem("SPN");
        selectorAlgoritmo.addItem("SRT");
        selectorAlgoritmo.addItem("HRRN");
        selectorAlgoritmo.addItem("Feedback");
        selectorAlgoritmo.setFont(new Font("Consolas", Font.BOLD, 14));
        
        panel.add(btnSalir);
        panel.add(btnAgregarProceso);
        panel.add(guardar);
        panel.add(cargar);
        panel.add(selectorAlgoritmo);
        panel.add(btnIniciar);
        
        //PARA LA VELOCIDAD 
        JPanel panelVelocidad = new JPanel(new BorderLayout(5, 0));
        panelVelocidad.setBackground(COLOR_FONDO);
        JLabel etiquetaVelocidad = new JLabel("Velocidad:", SwingConstants.CENTER);
        etiquetaVelocidad.setForeground(Color.WHITE);
        panelVelocidad.add(etiquetaVelocidad, BorderLayout.WEST);
        
        sliderVelocidad = new JSlider(JSlider.HORIZONTAL, 10, 1000, 100);
        sliderVelocidad.setMajorTickSpacing(200);
        sliderVelocidad.setPaintTicks(true);
        sliderVelocidad.setBackground(COLOR_FONDO); // Color de los ticks
        panel.add(sliderVelocidad, BorderLayout.CENTER);
        return panel;
    }
    
    private JButton crearBotonGamer(String texto) {
        JButton boton = new JButton(texto);
        boton.setFont(new Font("Consolas", Font.BOLD, 14));
        boton.setBackground(COLOR_BOTON);
        boton.setForeground(Color.WHITE);
        boton.setFocusPainted(false);
        boton.setBorder(BorderFactory.createRaisedBevelBorder());
        boton.setPreferredSize(new Dimension(150, 40));
        
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
        btnIniciar.addActionListener(e -> {
        // Si no hay una simulación activa, la iniciamos
        if (motorSimulacionActual == null || !motorSimulacionActual.isSimulacionActiva()) {
            ejecutarSimulacion();
        }
        });
        
        // --- Evento del menú desplegable ---
        selectorAlgoritmo.addActionListener(e -> {
            // Solo cambia el algoritmo si la simulación YA está en curso
            if (motorSimulacionActual != null && motorSimulacionActual.isSimulacionActiva()) {
                Planificador nuevoPlanificador = crearPlanificadorDesdeSelector();
                motorSimulacionActual.setPlanificador(nuevoPlanificador);
            }
        });
        
        btnAgregarProceso.addActionListener(e -> mostrarFormularioProceso());
        guardar.addActionListener(e -> guardarConfiguracion());
        cargar.addActionListener(e -> cargarConfiguracion());
        btnSalir.addActionListener(e -> {
            consola.agregarLinea("👋 ¡Hasta la próxima, gamer!", Color.ORANGE);
            try { Thread.sleep(1000); } catch (Exception ex) {}
            System.exit(0);
        });
        sliderVelocidad.addChangeListener(e -> {
        JSlider source = (JSlider) e.getSource();
        if (!source.getValueIsAdjusting()) { // Solo actualiza cuando el usuario suelta el slider
            int nuevaDuracion = source.getValue();
            Reloj.setCycleDurationMs(nuevaDuracion);
            consola.agregarLinea("⚙️ Velocidad del ciclo ajustada a " + nuevaDuracion + " ms.", Color.GRAY);
        }
        });
        
    }
    
    // 1. Añade este nuevo método auxiliar
    private Planificador crearPlanificadorDesdeSelector() {
        String seleccion = (String) selectorAlgoritmo.getSelectedItem();
        switch (seleccion) {
            case "Round Robin": return new RR(3); // Quantum de 3
            case "SPN": return new SPN();
            case "SRT": return new SRT();
            case "HRRN": return new HRRN();
            case "Feedback": return new Feedback();
            case "FCFS":
            default:
                return new FCFS();
        }
    }
    
    
    
    
    
    private void mostrarBienvenida() {
        consola.agregarLinea("=" .repeat(60), Color.YELLOW);
        consola.agregarLinea("🎮 BIENVENIDO AL SIMULADOR DE SISTEMAS OPERATIVOS", Color.CYAN);
        consola.agregarLinea("🕹️  STYLE GAMER EDITION", Color.CYAN);
        consola.agregarLinea("=" .repeat(60), Color.YELLOW);
        consola.agregarLinea("");
        }
    
    private void ejecutarSimulacion() {
        if (motorSimulacionActual != null && motorSimulacionActual.isSimulacionActiva()) {
        motorSimulacionActual.detenerSimulacion();
        }
        if (this.procesosParaSimular.sizeLista() == 0) {
        consola.agregarLinea("⚠️ No hay procesos. Añade o carga procesos antes de simular.", Color.ORANGE);
        return;
        }
        
        // Obtenemos el planificador inicial desde el selector que el usuario eligió.
        Planificador planificadorInicial = crearPlanificadorDesdeSelector();
        new Thread(() -> {
            try {
                // 1. Crear el motor pasándole el planificador y la consola
                motorSimulacionActual = new Engine(planificadorInicial, this.consola);
                SwingUtilities.invokeLater(() -> {
                    consola.limpiar();
                    consola.agregarLinea("🚀 INICIANDO SIMULACIÓN: " + planificadorInicial.getNombreAlgoritmo(), Color.GREEN);
                    consola.agregarLinea("📦 Cargando " + this.procesosParaSimular.sizeLista() + " procesos definidos por el usuario...", Color.WHITE);
                    consola.agregarSeparador();
                });

                // --- ESTE BLOQUE AHORA USA TU LISTA ---
                for (int i = 0; i < this.procesosParaSimular.sizeLista(); i++) {
                Proceso pOriginal = (Proceso) this.procesosParaSimular.get(i);
                Proceso pCopia = new Proceso(pOriginal.getName(), pOriginal.getTotalInstructions(), pOriginal.isCpuBound(), pOriginal.getProximaExcepcionES(), pOriginal.getDuracionES(), i);
                motorSimulacionActual.agregarProceso(pCopia);
            }
            
                motorSimulacionActual.iniciarSimulacion();
            // --- BUCLE DE ACTUALIZACIÓN EN TIEMPO REAL ---
                while (motorSimulacionActual.isSimulacionActiva()) {
                actualizarDashboard();
                try {
                    Thread.sleep(100); 
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
                }
                // Pequeña pausa final para asegurar la última actualización
                Thread.sleep(200);
                actualizarDashboard();
            
            } catch (Exception e) {
                // Este es tu bloque catch, déjalo como está
                SwingUtilities.invokeLater(() -> {
                    consola.agregarLinea("❌ Error inesperado: " + e.getMessage(), Color.RED);
                });
                e.printStackTrace();
            } finally {
                if (motorSimulacionActual != null) {
                    motorSimulacionActual.detenerSimulacion();

                    // Se formatea el string con los resultados finales
                    final String resultados = String.format(
                        "📊 MÉTRICAS FINALES (%s):\n" +
                        "   • Throughput: %.4f procesos/ciclo\n" +
                        "   • Utilización de CPU: %.2f%%\n" +
                        "   • Tiempo de Retorno Promedio: %.2f ciclos\n" +
                        "   • Tiempo de Espera Promedio: %.2f ciclos",
                        // --- CORRECCIÓN 4: Usamos planificadorInicial de nuevo ---
                        motorSimulacionActual.getPlanificador().getNombreAlgoritmo(),
                        motorSimulacionActual.getThroughput(),
                        motorSimulacionActual.getUtilizacionCPU(),
                        motorSimulacionActual.getTiempoRetornoPromedio(),
                        motorSimulacionActual.getTiempoEsperaPromedio()
                    );

                    // Se actualiza la GUI de forma segura
                    SwingUtilities.invokeLater(() -> {
                        consola.agregarSeparador();
                        consola.agregarLinea("🎉 SIMULACIÓN COMPLETADA", Color.GREEN);
                        consola.agregarLinea(resultados, Color.YELLOW);
                        consola.agregarLinea("💡 Puedes cambiar el algoritmo y volver a iniciar.", Color.ORANGE);
                    });
                }
            }
        }).start();
    }
    
    private void mostrarFormularioProceso() {
    // Componentes del formulario
    JTextField nombreField = new JTextField();
    JTextField instruccionesField = new JTextField();
    JCheckBox esCpuBoundCheck = new JCheckBox("CPU Bound (ignora E/S)");
    JTextField ciclosExcepcionField = new JTextField("0");
    JTextField duracionESField = new JTextField("0");

    // Lógica para habilitar/deshabilitar campos de E/S
    esCpuBoundCheck.addActionListener(e -> {
        boolean esCpuBound = esCpuBoundCheck.isSelected();
        ciclosExcepcionField.setEnabled(!esCpuBound);
        duracionESField.setEnabled(!esCpuBound);
    });
    esCpuBoundCheck.setSelected(true);
    ciclosExcepcionField.setEnabled(false);
    duracionESField.setEnabled(false);

    // Panel con todos los componentes
    JPanel panelForm = new JPanel(new GridLayout(0, 2, 5, 5));
    panelForm.add(new JLabel("Nombre del Proceso:"));
    panelForm.add(nombreField);
    panelForm.add(new JLabel("Total de Instrucciones:"));
    panelForm.add(instruccionesField);
    panelForm.add(esCpuBoundCheck);
    panelForm.add(new JLabel()); // Espacio en blanco
    panelForm.add(new JLabel("Ciclos para generar E/S:"));
    panelForm.add(ciclosExcepcionField);
    panelForm.add(new JLabel("Duración de E/S (ciclos):"));
    panelForm.add(duracionESField);

    int resultado = JOptionPane.showConfirmDialog(this, panelForm, "Añadir Nuevo Proceso",
                                                  JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

    if (resultado == JOptionPane.OK_OPTION) {
        try {
            String nombre = nombreField.getText();
            if (nombre.trim().isEmpty()) {
                throw new IllegalArgumentException("El nombre no puede estar vacío.");
            }
            int instrucciones = Integer.parseInt(instruccionesField.getText());
            boolean esCpuBound = esCpuBoundCheck.isSelected();
            int ciclosES = esCpuBound ? 0 : Integer.parseInt(ciclosExcepcionField.getText());
            int duracionES = esCpuBound ? 0 : Integer.parseInt(duracionESField.getText());

            Proceso nuevoProceso = new Proceso(nombre, instrucciones, esCpuBound, ciclosES, duracionES, 0);
            this.procesosParaSimular.insertFinal(nuevoProceso);
            
            consola.agregarLinea("✅ Proceso '" + nombre + "' añadido a la lista. Total: " + procesosParaSimular.sizeLista(), Color.CYAN);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error en los datos: " + e.getMessage(), "Error de Entrada", JOptionPane.ERROR_MESSAGE);
        }
    }
}
    
    private void guardarConfiguracion() {
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setDialogTitle("Guardar Configuración");
    if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
        File archivo = fileChooser.getSelectedFile();
        try (Writer writer = new FileWriter(archivo)) {
            // Preparamos el objeto de configuración
            ConfiguracionSimulacion config = new ConfiguracionSimulacion();
            config.duracionCicloMs = Reloj.getCycleDurationMs();
            
            // Convertimos tu ListaSimple a una List temporal para Gson
            List<ProcesoData> dataList = new ArrayList<>();
            for (int i = 0; i < procesosParaSimular.sizeLista(); i++) {
                dataList.add(new ProcesoData((Proceso) procesosParaSimular.get(i)));
            }
            config.procesos = dataList;

            // Guardamos el objeto completo como JSON
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(config, writer);
            consola.agregarLinea("💾 Configuración guardada en: " + archivo.getAbsolutePath(), Color.CYAN);
        } catch (IOException e) {
            consola.agregarLinea("❌ Error al guardar: " + e.getMessage(), Color.RED);
        }
    }
}

    private void cargarConfiguracion() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Cargar Configuración");
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File archivo = fileChooser.getSelectedFile();
            try (Reader reader = new FileReader(archivo)) {
                Gson gson = new Gson();
                ConfiguracionSimulacion config = gson.fromJson(reader, ConfiguracionSimulacion.class);

                // Cargamos la duración del ciclo
                Reloj.setCycleDurationMs(config.duracionCicloMs);
                // (Opcional) Actualizar la posición del slider para que refleje el valor cargado
                // sliderVelocidad.setValue(config.duracionCicloMs);

                // Limpiamos la lista actual y la llenamos con los datos cargados
                // Esto respeta la restricción de NO usar ArrayList en tu lógica principal
                this.procesosParaSimular.clear(); // Necesitarás un método clear() en tu ListaSimple
                for (ProcesoData data : config.procesos) {
                    Proceso p = new Proceso(data.nombre, data.totalInstructions, data.isCpuBound, data.ciclosExcepcionES, data.duracionES, 0);
                    this.procesosParaSimular.insertFinal(p);
                }
                consola.agregarLinea("✅ " + config.procesos.size() + " procesos y configuración cargados.", Color.CYAN);
            } catch (Exception e) {
                consola.agregarLinea("❌ Error al cargar el archivo: " + e.getMessage(), Color.RED);
            }
        }
        }
    
    private void actualizarDashboard() {
    // Nos aseguramos de que la actualización ocurra en el hilo de la interfaz (EDT)
    SwingUtilities.invokeLater(() -> {
        if (motorSimulacionActual == null) return;

        // --- Actualizar Cola de Listos ---
        modeloListaListos.clear();
        ListaSimple procesosListos = motorSimulacionActual.getProcesosListos();
        for (int i = 0; i < procesosListos.sizeLista(); i++) {
            Proceso p = (Proceso) procesosListos.get(i);
            modeloListaListos.addElement(p.getId() + " - " + p.getName());
        }

        // --- Actualizar Cola de Bloqueados ---
        modeloListaBloqueados.clear();
        ListaSimple procesosBloqueados = motorSimulacionActual.getProcesosBloqueados();
        for (int i = 0; i < procesosBloqueados.sizeLista(); i++) {
            Proceso p = (Proceso) procesosBloqueados.get(i);
            modeloListaBloqueados.addElement(p.getId() + " - " + p.getName());
        }

        // --- Actualizar Panel de CPU ---
        Proceso enCpu = motorSimulacionActual.getProcesoEjecutandoActual();
        if (enCpu != null) {
            labelProcesoCPU.setText(enCpu.getId() + " - " + enCpu.getName());
            
            // --- Actualizar Panel de Info (PCB) ---
            String info = String.format(
                "ID: %s\n" +
                "Nombre: %s\n" +
                "Estado: %s\n" +
                "PC: %d / %d\n" +
                "--------------------\n" +
                "Tiempo Llegada: %d\n" +
                "Tiempo Inicio Ejec.: %d\n",
                enCpu.getId(),
                enCpu.getName(),
                enCpu.getState().toString(),
                enCpu.getPc(),
                enCpu.getTotalInstructions(),
                enCpu.getTiempoLlegada(),
                enCpu.getTiempoInicioEjecucion()
            );
            areaInfoProceso.setText(info);

        } else {
            labelProcesoCPU.setText("LIBRE");
            areaInfoProceso.setText("La CPU está libre. No hay proceso en ejecución.");
        }
    });
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