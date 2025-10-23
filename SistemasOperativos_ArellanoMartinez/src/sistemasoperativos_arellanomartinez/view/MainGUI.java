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
import sistemasoperativos_arellanomartinez.Controller.Engine;
import sistemasoperativos_arellanomartinez.Planificador.*;
import sistemasoperativos_arellanomartinez.Simulador.Proceso;
import sistemasoperativos_arellanomartinez.Simulador.*;
import sistemasoperativos_arellanomartinez.Controller.*;
import edd.ListaSimple;

/**
 * VENTANA PRINCIPAL
 * Interfaz grafica principal
 * @author Day y Ada
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
    
    //
    private DefaultListModel<String> modeloListaListos;
    private JList<String> listaListos;
    
    private DefaultListModel<String> modeloListaBloqueados;
    private JList<String> listaBloqueados;
    
    private DefaultListModel<String> modeloListaListosSus, modeloListaBloqueadosSus;
    private JList<String> listaListosSus, listaBloqueadosSus;

    private JTextArea areaInfoProceso;
    private JLabel labelProcesoCPU;
    private JLabel labelModoOperacion;
    private JLabel labelCiclos;
    
    public MainGUI() {
        this.procesosParaSimular = new ListaSimple(); // INICIALIZA LA LISTA
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
        // panel superior titulo
        JPanel panelTitulo = crearPanelTitulo();
        
        // Panel principal para el dashboard 
        JPanel panelDashboard = new JPanel(new GridLayout(1, 3, 10, 10)); // 1 fila, 3 columnas
        panelDashboard.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panelDashboard.setBackground(COLOR_FONDO);

        // --- Columna 1: Listos y Listos-Suspendidos ---
        JPanel panelIzquierda = new JPanel(new GridLayout(2, 1, 10, 10));
        panelIzquierda.setOpaque(false);
        panelIzquierda.add(crearPanelCola("Procesos Listos"));
        panelIzquierda.add(crearPanelCola("Listos-Suspendidos"));
        panelDashboard.add(panelIzquierda);

        // --- Columna 2: CPU, Bloqueados y Bloqueados-Suspendidos ---
        JPanel panelCentro = new JPanel(new BorderLayout(10, 10));
        panelCentro.setOpaque(false);
        panelCentro.add(crearPanelCPU(), BorderLayout.NORTH);
        JPanel panelColasBloqueados = new JPanel(new GridLayout(2, 1, 10, 10));
        panelColasBloqueados.setOpaque(false);
        panelColasBloqueados.add(crearPanelCola("Procesos Bloqueados"));
        panelColasBloqueados.add(crearPanelCola("Bloqueados-Suspendidos"));
        panelCentro.add(panelColasBloqueados, BorderLayout.CENTER);
        panelDashboard.add(panelCentro);

        // -- Columna Derecha del Dashboard --
        panelDashboard.add(crearPanelInfoPCB());
    

        // panel central
        consola = new ConsolaGamer();
        JScrollPane scrollConsola = new JScrollPane(consola);
        scrollConsola.setBorder(BorderFactory.createLineBorder(COLOR_BOTON, 2));
        
        
        // panel inferior donde estan los botones
        JPanel panelBotones = crearPanelBotones();
        
        // 5. Divisor (JSplitPane) para la parte inferior
        JSplitPane splitPaneInferior = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollConsola, panelBotones);
        splitPaneInferior.setResizeWeight(0.8); // La consola ocupa el 70% del espacio
        splitPaneInferior.setBorder(null);
        
        // Agregar componentes a la ventana
        add(panelTitulo, BorderLayout.NORTH);
        add(panelDashboard, BorderLayout.CENTER);
        add(splitPaneInferior, BorderLayout.SOUTH);
    }
    
    private JPanel crearPanelCola(String titulo) {
        // Inicializacion del modelo y la lista
        DefaultListModel<String> modelo = new DefaultListModel<>();
        JList<String> lista = new JList<>(modelo);

        lista.setBackground(new Color(20, 20, 40));
        lista.setForeground(Color.WHITE);
        lista.setFont(new Font("Consolas", Font.PLAIN, 14));

        if (titulo.contains("Listos-Suspendidos")) {
            this.modeloListaListosSus = modelo;
            this.listaListosSus = lista;
        } else if (titulo.contains("Listos")) {
            this.modeloListaListos = modelo;
            this.listaListos = lista;
        } else if (titulo.contains("Bloqueados-Suspendidos")) {
            this.modeloListaBloqueadosSus = modelo;
            this.listaBloqueadosSus = lista;
        } else if (titulo.contains("Bloqueados")) {
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
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GREEN), "CPU en Ejecucion", 0, 0, new Font("Consolas", Font.BOLD, 16), Color.GREEN));
        
        labelCiclos = new JLabel("Ciclo: 0", SwingConstants.CENTER);
        labelCiclos.setFont(new Font("Consolas", Font.BOLD, 16));
        labelCiclos.setForeground(Color.YELLOW);
        panel.add(labelCiclos, BorderLayout.NORTH); // Lo ponemos arriba
        
        labelProcesoCPU = new JLabel("LIBRE", SwingConstants.CENTER);
        labelProcesoCPU.setFont(new Font("Consolas", Font.BOLD, 20));
        labelProcesoCPU.setForeground(Color.WHITE);
        
        labelModoOperacion = new JLabel("Modo: Kernel (SO)", SwingConstants.CENTER);
        labelModoOperacion.setFont(new Font("Consolas", Font.PLAIN, 12));
        labelModoOperacion.setForeground(Color.ORANGE);
        
        panel.add(labelModoOperacion, BorderLayout.SOUTH); // Lo ponemos abajo
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
        JPanel panel = new JPanel(new GridLayout(3, 0, 5, 5));
        panel.setBackground(COLOR_FONDO);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        
        // Botones de control de procesos
        btnAgregarProceso = crearBotonGamer("Añadir Proceso");
        guardar = crearBotonGamer("Guardar");
        cargar = crearBotonGamer("Cargar archivo");
        btnSalir = crearBotonGamer("🚪 SALIR");
        

        // Boton para iniciar la simulacion
        btnIniciar = crearBotonGamer("▶ Iniciar Simulacion");

        // Menu desplegable para seleccionar el algoritmo
        selectorAlgoritmo = new JComboBox<>(new String[]{"FCFS", "Round Robin", "SPN", "SRT", "HRRN", "Feedback"});
        selectorAlgoritmo.setFont(new Font("Consolas", Font.BOLD, 12));
       
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
        boton.setFont(new Font("Consolas", Font.BOLD, 12));
        boton.setBackground(COLOR_BOTON);
        boton.setForeground(Color.WHITE);
        boton.setFocusPainted(false);
        boton.setBorder(BorderFactory.createRaisedBevelBorder());
        boton.setPreferredSize(new Dimension(140, 35));
        
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
        // Si no hay una simulacion activa, la iniciamos
        if (motorSimulacionActual == null || !motorSimulacionActual.isSimulacionActiva()) {
            ejecutarSimulacion();
            btnIniciar.setText("⏹ Detener Simulación");
            btnIniciar.setBackground(Color.RED);
        } else {
            // Si la simulación ya está activa, la detenemos
            motorSimulacionActual.detenerSimulacion();
            btnIniciar.setText("▶ Iniciar Simulación");
            btnIniciar.setBackground(COLOR_BOTON);
        }
        });
        
        // --- Evento del menu desplegable ---
        selectorAlgoritmo.addActionListener(e -> {
            // Solo cambia el algoritmo si la simulacion ya esta en curso
            if (motorSimulacionActual != null && motorSimulacionActual.isSimulacionActiva()) {
                Planificador nuevoPlanificador = crearPlanificadorDesdeSelector();
                motorSimulacionActual.setPlanificador(nuevoPlanificador);
            }
        });
        
        btnAgregarProceso.addActionListener(e -> mostrarFormularioProceso());
        guardar.addActionListener(e -> guardarConfiguracion());
        cargar.addActionListener(e -> cargarConfiguracion());
        btnSalir.addActionListener(e -> {
            consola.agregarLinea("👋 ¡Hasta la proxima!", Color.ORANGE);
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
    
    // 1. Añade este nuevo metodo auxiliar
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
        consola.agregarLinea("No hay procesos. Añade o carga procesos antes de simular.", Color.ORANGE);
        return;
        }
        
        // Obtenemos el planificador inicial desde el selector que el usuario eligio.
        Planificador planificadorInicial = crearPlanificadorDesdeSelector();
        ListaSimple jobPool = new ListaSimple();
        for (int i = 0; i < this.procesosParaSimular.sizeLista(); i++) {
            Proceso p = (Proceso) this.procesosParaSimular.get(i);
            // Creamos una nueva instancia para no afectar la lista original
            jobPool.insertFinal(new Proceso(p.getName(), p.getTotalInstructions(), p.isCpuBound(), p.getProximaExcepcionES(), p.getDuracionES(), 0));
        }
        
        new Thread(() -> {
            try {
                // 1. Crear el motor pasandole el planificador y la consola
                motorSimulacionActual = new Engine(planificadorInicial, this.consola, jobPool);
                SwingUtilities.invokeLater(() -> {
                    consola.limpiar();
                    consola.agregarLinea("🚀 INICIANDO SIMULACIÓN: " + planificadorInicial.getNombreAlgoritmo(), Color.GREEN);
                    consola.agregarLinea("🗳️ " + jobPool.sizeLista() + " procesos enviados al pool de trabajos.", Color.WHITE);
                    consola.agregarSeparador();
                });
            
                motorSimulacionActual.iniciarSimulacion();
                while (motorSimulacionActual.isSimulacionActiva()) {
                actualizarDashboard();
                try {
                    Thread.sleep(100); 
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
                }
                // Pequeña pausa final para asegurar la ultima actualizacion
                Thread.sleep(200);
                actualizarDashboard();
            
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    consola.agregarLinea("Error inesperado: " + e.getMessage(), Color.RED);
                });
                e.printStackTrace();
            } finally {
                if (motorSimulacionActual != null) {
                    motorSimulacionActual.detenerSimulacion();

                    // Se formatea el string con los resultados finales
                    final String resultados = String.format(
                        "📊 METRICAS FINALES (%s):\n" +
                        "   • Throughput: %.4f procesos/ciclo\n" +
                        "   • Utilizacion de CPU: %.2f%%\n" +
                        "   • Tiempo de Retorno Promedio: %.2f ciclos\n" +
                        "   • Tiempo de Espera Promedio: %.2f ciclos",
                        // --- CORRECCION 4: Usamos planificadorInicial de nuevo ---
                        motorSimulacionActual.getPlanificador().getNombreAlgoritmo(),
                        motorSimulacionActual.getThroughput(),
                        motorSimulacionActual.getUtilizacionCPU(),
                        motorSimulacionActual.getTiempoRetornoPromedio(),
                        motorSimulacionActual.getTiempoEsperaPromedio()
                    );

                    // Se actualiza la GUI de forma segura
                    SwingUtilities.invokeLater(() -> {
                        consola.agregarSeparador();
                        consola.agregarLinea("SIMULACION COMPLETADA", Color.GREEN);
                        consola.agregarLinea(resultados, Color.YELLOW);
                        consola.agregarLinea("Puedes cambiar el algoritmo y volver a iniciar.", Color.ORANGE);
                        btnIniciar.setText("▶ Iniciar Simulación");
                        btnIniciar.setBackground(COLOR_BOTON);
                    
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

    // Logica para habilitar/deshabilitar campos de E/S
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
    panelForm.add(new JLabel("Duracion de E/S (ciclos):"));
    panelForm.add(duracionESField);

    int resultado = JOptionPane.showConfirmDialog(this, panelForm, "Agregar Nuevo Proceso",
                                                  JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

    if (resultado == JOptionPane.OK_OPTION) {
        try {
            String nombre = nombreField.getText();
            if (nombre.trim().isEmpty()) {
                throw new IllegalArgumentException("El nombre no puede estar vacio.");
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
    fileChooser.setDialogTitle("Guardar Configuracion");
    if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
        File archivo = fileChooser.getSelectedFile();
        try (Writer writer = new FileWriter(archivo)) {
            // Preparamos el objeto de configuracion
            ConfiguracionSimulacion config = new ConfiguracionSimulacion();
            config.duracionCicloMs = Reloj.getCycleDurationMs();
            
            // Convertimos tu ListaSimple a un array para Gson
            ProcesoData[] dataArray = new ProcesoData[procesosParaSimular.sizeLista()];
            for (int i = 0; i < procesosParaSimular.sizeLista(); i++) {
                dataArray[i] = new ProcesoData((Proceso) procesosParaSimular.get(i));
            }
            config.procesos = dataArray;

            // Guardamos el objeto completo como JSON
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(config, writer);
            consola.agregarLinea("💾 Configuracion guardada en: " + archivo.getAbsolutePath(), Color.CYAN);
        } catch (IOException e) {
            consola.agregarLinea("❌ Error al guardar: " + e.getMessage(), Color.RED);
        }
    }
}

    private void cargarConfiguracion() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Cargar Configuracion");
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File archivo = fileChooser.getSelectedFile();
            try (Reader reader = new FileReader(archivo)) {
                Gson gson = new Gson();
                ConfiguracionSimulacion config = gson.fromJson(reader, ConfiguracionSimulacion.class);

                // Cargamos la duracion del ciclo
                Reloj.setCycleDurationMs(config.duracionCicloMs);
                sliderVelocidad.setValue(config.duracionCicloMs);
                // Limpiamos la lista actual y la llenamos con los datos cargados
                this.procesosParaSimular.clear(); 
                for (ProcesoData data : config.procesos) {
                    Proceso p = new Proceso(data.nombre, data.totalInstructions, data.isCpuBound, data.ciclosExcepcionES, data.duracionES, 0);
                    this.procesosParaSimular.insertFinal(p);
                }
                consola.agregarLinea(config.procesos.length + " procesos y configuracion cargados.", Color.CYAN);
            } catch (Exception e) {
                consola.agregarLinea("Error al cargar el archivo: " + e.getMessage(), Color.RED);
            }
        }
        }
    
    private void actualizarDashboard() {
    // Nos aseguramos de que la actualizacion ocurra en el hilo de la interfaz (EDT)
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
        
        // --- Actualizar Cola de Listos-Suspendidos ---
        modeloListaListosSus.clear();
        ListaSimple procesosListosSus = motorSimulacionActual.getProcesosListosSuspendidos();
        for (int i = 0; i < procesosListosSus.sizeLista(); i++) {
            Proceso p = (Proceso) procesosListosSus.get(i);
            modeloListaListosSus.addElement(p.getId() + " - " + p.getName());
        }

        // --- Actualizar Cola de Bloqueados-Suspendidos ---
        modeloListaBloqueadosSus.clear();
        ListaSimple procesosBloqueadosSus = motorSimulacionActual.getProcesosBloqueadosSuspendidos();
        for (int i = 0; i < procesosBloqueadosSus.sizeLista(); i++) {
            Proceso p = (Proceso) procesosBloqueadosSus.get(i);
            modeloListaBloqueadosSus.addElement(p.getId() + " - " + p.getName());
        }
        
        // --- Actualizar Panel de CPU ---
        Proceso enCpu = motorSimulacionActual.getProcesoEjecutandoActual();
        if (enCpu != null) {
            labelProcesoCPU.setText(enCpu.getId() + " - " + enCpu.getName());
            labelModoOperacion.setText("Modo: Usuario");
            labelModoOperacion.setForeground(Color.CYAN);
            // --- Actualizar Panel de Info (PCB) ---
            String info = String.format(
                "ID: %s\n" +
                "Nombre: %s\n" +
                "Estado: %s\n" +
                "PC: %d / %d\n" +
                "MAR: %d\n" + // <-- LÍNEA AÑADIDA
                "--------------------\n" +
                "Tiempo Llegada: %d\n" +
                "Tiempo Inicio Ejec.: %d\n",
                enCpu.getId(),
                enCpu.getName(),
                enCpu.getState().toString(),
                enCpu.getPc(),
                enCpu.getTotalInstructions(),
                enCpu.getMar(), // <-- LÍNEA AÑADIDA
                enCpu.getTiempoLlegada(),
                enCpu.getTiempoInicioEjecucion()
            );
            areaInfoProceso.setText(info);

        } else {
            labelProcesoCPU.setText("LIBRE");
            labelModoOperacion.setText("Modo: Kernel (SO)");
            labelModoOperacion.setForeground(Color.ORANGE);
            areaInfoProceso.setText("La CPU esta libre. No hay proceso en ejecucion.");
        }
    });
    }

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

// Clases auxiliares para la serialización
class ConfiguracionSimulacion {
    int duracionCicloMs;
    ProcesoData[] procesos;
}

class ProcesoData {
    String nombre;
    int totalInstructions;
    boolean isCpuBound;
    int ciclosExcepcionES;
    int duracionES;

    public ProcesoData() {
        // Constructor vacío necesario para Gson
    }

    public ProcesoData(Proceso proceso) {
        this.nombre = proceso.getName();
        this.totalInstructions = proceso.getTotalInstructions();
        this.isCpuBound = proceso.isCpuBound();
        this.ciclosExcepcionES = proceso.getProximaExcepcionES();
        this.duracionES = proceso.getDuracionES();
    }
}