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

// --- INICIO: IMPORTACIONES AÑADIDAS PARA LA GRÁFICA ---
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
// --- FIN: IMPORTACIONES AÑADIDAS PARA LA GRÁFICA ---


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
    
    // Modelos y Listas para la GUI
    private DefaultListModel<String> modeloListaListos;
    private DefaultListModel<String> modeloListaBloqueados;
    private DefaultListModel<String> modeloListaListosSus;
    private DefaultListModel<String> modeloListaBloqueadosSus;
    private DefaultListModel<String> modeloListaTerminados;

    // Componentes de la GUI
    private JTextArea areaInfoProceso;
    private JLabel labelProcesoCPU;
    private JLabel labelModoOperacion;
    private JLabel labelCiclos;
    
    public MainGUI() {
        this.procesosParaSimular = new ListaSimple(); 
        configurarVentana();
        crearComponentes();
        agregarEventos();
        mostrarBienvenida();
        
    }
    
    private void configurarVentana() {
        setTitle("SIMULADOR DE SISTEMAS OPERATIVOS"); // Quitado emoji
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800); 
        setLocationRelativeTo(null); 
        getContentPane().setBackground(COLOR_FONDO);
        setLayout(new BorderLayout());
    }
    
    private void crearComponentes() {
        JPanel panelTitulo = crearPanelTitulo();
        
        JPanel panelDashboard = new JPanel(new GridBagLayout()); 
        GridBagConstraints gbc = new GridBagConstraints();
        panelDashboard.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panelDashboard.setBackground(COLOR_FONDO);

        JPanel panelIzquierda = new JPanel(new GridLayout(2, 1, 10, 10));
        panelIzquierda.setOpaque(false);
        panelIzquierda.add(crearPanelCola("Procesos Listos"));
        panelIzquierda.add(crearPanelCola("Listos-Suspendidos"));
        
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0.33; gbc.weighty = 1.0; gbc.gridx = 0; gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 0, 10);
        panelDashboard.add(panelIzquierda, gbc);

        JPanel panelCentro = new JPanel(new BorderLayout(10, 10));
        panelCentro.setOpaque(false);
        panelCentro.add(crearPanelCPU(), BorderLayout.NORTH);
        JPanel panelColasBloqueados = new JPanel(new GridLayout(2, 1, 10, 10));
        panelColasBloqueados.setOpaque(false);
        panelColasBloqueados.add(crearPanelCola("Procesos Bloqueados"));
        panelColasBloqueados.add(crearPanelCola("Bloqueados-Suspendidos"));
        panelCentro.add(panelColasBloqueados, BorderLayout.CENTER);

        gbc.gridx = 1;
        panelDashboard.add(panelCentro, gbc);

        JPanel panelDerecha = new JPanel(new GridLayout(2, 1, 10, 10));
        panelDerecha.setOpaque(false);
        panelDerecha.add(crearPanelInfoPCB());
        panelDerecha.add(crearPanelCola("Procesos Culminados"));
        
        gbc.gridx = 2;
        panelDashboard.add(panelDerecha, gbc);
    
        consola = new ConsolaGamer();
        JScrollPane scrollConsola = new JScrollPane(consola);
        scrollConsola.setBorder(BorderFactory.createLineBorder(COLOR_BOTON, 2));
        
        // **INICIO DE LA CORRECCIÓN CLAVE**
        // 1. Creamos un panel contenedor para la consola.
        JPanel panelContenedorConsola = new JPanel(new BorderLayout());
        // 2. Añadimos la consola (con su scroll) al CENTRO de este contenedor.
        //    Esto fuerza a la consola a ocupar solo el espacio disponible.
        panelContenedorConsola.add(scrollConsola, BorderLayout.CENTER);
        // **FIN DE LA CORRECCIÓN CLAVE**

        JPanel panelBotones = crearPanelBotones();
        
        JSplitPane splitPaneCentral = new JSplitPane(JSplitPane.VERTICAL_SPLIT, panelDashboard, panelContenedorConsola);
        splitPaneCentral.setResizeWeight(0.5);
        splitPaneCentral.setBorder(null);
        splitPaneCentral.setContinuousLayout(true);

        add(panelTitulo, BorderLayout.NORTH);
        add(splitPaneCentral, BorderLayout.CENTER);
        add(panelBotones, BorderLayout.SOUTH);
    }
    
    private JPanel crearPanelCola(String titulo) {
        DefaultListModel<String> modelo = new DefaultListModel<>();
        JList<String> lista = new JList<>(modelo);

        lista.setBackground(new Color(20, 20, 40));
        lista.setForeground(Color.WHITE);
        lista.setFont(new Font("Consolas", Font.PLAIN, 14));

        if (titulo.contains("Listos-Suspendidos")) {
            this.modeloListaListosSus = modelo;
        } else if (titulo.contains("Listos")) {
            this.modeloListaListos = modelo;
        } else if (titulo.contains("Bloqueados-Suspendidos")) {
            this.modeloListaBloqueadosSus = modelo;
        } else if (titulo.contains("Bloqueados")) {
            this.modeloListaBloqueados = modelo;
        } else if (titulo.contains("Culminados")) { 
            this.modeloListaTerminados = modelo;
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
        panel.add(labelCiclos, BorderLayout.NORTH);
        
        labelProcesoCPU = new JLabel("LIBRE", SwingConstants.CENTER);
        labelProcesoCPU.setFont(new Font("Consolas", Font.BOLD, 20));
        labelProcesoCPU.setForeground(Color.WHITE);
        
        labelModoOperacion = new JLabel("Modo: Kernel (SO)", SwingConstants.CENTER);
        labelModoOperacion.setFont(new Font("Consolas", Font.PLAIN, 12));
        labelModoOperacion.setForeground(Color.ORANGE);
        
        panel.add(labelModoOperacion, BorderLayout.SOUTH);
        panel.add(labelProcesoCPU, BorderLayout.CENTER);
        
        panel.setPreferredSize(new Dimension(200, 150)); 

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

        return panel;
    }
    
    private JPanel crearPanelTitulo() {
        JPanel panel = new JPanel();
        panel.setBackground(COLOR_FONDO);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
        
        JLabel titulo = new JLabel("SIMULADOR DE SISTEMAS OPERATIVOS"); // Quitado emoji
        titulo.setFont(new Font("Consolas", Font.BOLD, 24));
        titulo.setForeground(COLOR_TEXTO);
        
        panel.add(titulo);
        return panel;
    }
    
    private JPanel crearPanelBotones() {
        JPanel panelContenedor = new JPanel(new BorderLayout(10, 5));
        panelContenedor.setBackground(COLOR_FONDO);
        panelContenedor.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel panelAcciones = new JPanel(new GridBagLayout());
        panelAcciones.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(2, 5, 2, 5);
        gbc.weightx = 1.0;

        btnSalir = crearBotonGamer("SALIR"); // Quitado emoji
        btnAgregarProceso = crearBotonGamer("Añadir Proceso");
        guardar = crearBotonGamer("Guardar");
        cargar = crearBotonGamer("Cargar archivo");
        btnIniciar = crearBotonGamer("Iniciar Simulacion"); // Quitado emoji
        
        selectorAlgoritmo = new JComboBox<>(new String[]{"FCFS", "Round Robin", "SPN", "SRT", "HRRN", "Feedback"});
        selectorAlgoritmo.setFont(new Font("Consolas", Font.BOLD, 12));

        gbc.gridx = 0; gbc.gridy = 0; panelAcciones.add(btnSalir, gbc);
        gbc.gridx = 1; gbc.gridy = 0; panelAcciones.add(btnAgregarProceso, gbc);
        gbc.gridx = 2; gbc.gridy = 0; panelAcciones.add(guardar, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1; panelAcciones.add(cargar, gbc);
        gbc.gridx = 1; gbc.gridy = 1; panelAcciones.add(selectorAlgoritmo, gbc);
        gbc.gridx = 2; gbc.gridy = 1; panelAcciones.add(btnIniciar, gbc);

        JPanel panelVelocidad = new JPanel(new BorderLayout(10, 0));
        panelVelocidad.setOpaque(false);
        JLabel etiquetaVelocidad = new JLabel("Velocidad:", SwingConstants.CENTER);
        etiquetaVelocidad.setForeground(Color.WHITE);
        
        sliderVelocidad = new JSlider(JSlider.HORIZONTAL, 10, 1000, 100);
        sliderVelocidad.setMajorTickSpacing(200);
        sliderVelocidad.setPaintTicks(true);
        sliderVelocidad.setOpaque(false);

        panelVelocidad.add(etiquetaVelocidad, BorderLayout.WEST);
        panelVelocidad.add(sliderVelocidad, BorderLayout.CENTER);
        
        panelContenedor.add(panelAcciones, BorderLayout.CENTER);
        panelContenedor.add(panelVelocidad, BorderLayout.SOUTH);

        return panelContenedor;
    }
    
    private JButton crearBotonGamer(String texto) {
        JButton boton = new JButton(texto);
        boton.setFont(new Font("Consolas", Font.BOLD, 12));
        boton.setBackground(COLOR_BOTON);
        boton.setForeground(Color.WHITE);
        boton.setFocusPainted(false);
        boton.setBorder(BorderFactory.createRaisedBevelBorder());
        boton.setPreferredSize(new Dimension(140, 35));
        
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
            if (motorSimulacionActual == null || !motorSimulacionActual.isSimulacionActiva()) {
                ejecutarSimulacion();
                btnIniciar.setText("Detener Simulación"); // Quitado emoji
                btnIniciar.setBackground(Color.RED);
            } else {
                motorSimulacionActual.detenerSimulacion();
                btnIniciar.setText("Iniciar Simulación"); // Quitado emoji
                btnIniciar.setBackground(COLOR_BOTON);
            }
        });
        
        selectorAlgoritmo.addActionListener(e -> {
            if (motorSimulacionActual != null && motorSimulacionActual.isSimulacionActiva()) {
                Planificador nuevoPlanificador = crearPlanificadorDesdeSelector();
                motorSimulacionActual.setPlanificador(nuevoPlanificador);
            }
        });
        
        btnAgregarProceso.addActionListener(e -> mostrarFormularioProceso());
        guardar.addActionListener(e -> guardarConfiguracion());
        cargar.addActionListener(e -> cargarConfiguracion());
        btnSalir.addActionListener(e -> {
            consola.agregarLinea("¡Hasta la proxima!", Color.ORANGE); // Quitado emoji
            try { Thread.sleep(1000); } catch (Exception ex) {}
            System.exit(0);
        });
        sliderVelocidad.addChangeListener(e -> {
            JSlider source = (JSlider) e.getSource();
            if (!source.getValueIsAdjusting()) {
                int nuevaDuracion = source.getValue();
                Reloj.setCycleDurationMs(nuevaDuracion);
                consola.agregarLinea("Velocidad del ciclo ajustada a " + nuevaDuracion + " ms.", Color.GRAY); // Quitado emoji
            }
        });
        
    }
    
    private Planificador crearPlanificadorDesdeSelector() {
        String seleccion = (String) selectorAlgoritmo.getSelectedItem();
        switch (seleccion) {
            case "Round Robin": return new RR(3); 
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
        consola.agregarLinea("BIENVENIDO AL SIMULADOR DE SISTEMAS OPERATIVOS", Color.CYAN); // Quitado emoji
        consola.agregarLinea("STYLE GAMER EDITION", Color.CYAN); // Quitado emoji
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
        
        Planificador planificadorInicial = crearPlanificadorDesdeSelector();
        ListaSimple jobPool = new ListaSimple();
        for (int i = 0; i < this.procesosParaSimular.sizeLista(); i++) {
            Proceso p = (Proceso) this.procesosParaSimular.get(i);
            jobPool.insertFinal(new Proceso(p.getName(), p.getTotalInstructions(), p.isCpuBound(), p.getProximaExcepcionES(), p.getDuracionES(), 0));
        }
        
        new Thread(() -> {
            try {
                motorSimulacionActual = new Engine(planificadorInicial, this.consola, jobPool);
                SwingUtilities.invokeLater(() -> {
                    consola.limpiar();
                    consola.agregarLinea("INICIANDO SIMULACIÓN: " + planificadorInicial.getNombreAlgoritmo(), Color.GREEN); // Quitado emoji
                    consola.agregarLinea(jobPool.sizeLista() + " procesos enviados al pool de trabajos.", Color.WHITE); // Quitado emoji
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

                    // --- INICIO: Captura de métricas (ya existía) ---
                    final String nombreAlgoritmo = motorSimulacionActual.getPlanificador().getNombreAlgoritmo();
                    final double tRetornoProm = motorSimulacionActual.getTiempoRetornoPromedio();
                    final double tEsperaProm = motorSimulacionActual.getTiempoEsperaPromedio();
                    
                    final String resultados = String.format(
                        "METRICAS FINALES (%s):\n" + // Quitado emoji
                        "    • Throughput: %.4f procesos/ciclo\n" +
                        "    • Utilizacion de CPU: %.2f%%\n" +
                        "    • Tiempo de Retorno Promedio: %.2f ciclos\n" +
                        "    • Tiempo de Espera Promedio: %.2f ciclos\n" +
                        "    • Precio Total Computacional: %.2f",
                        nombreAlgoritmo,
                        motorSimulacionActual.getThroughput(),
                        motorSimulacionActual.getUtilizacionCPU(),
                        tRetornoProm,
                        tEsperaProm,
                        motorSimulacionActual.getPrecioTotal()
                    );
                    // --- FIN: Captura de métricas ---

                    SwingUtilities.invokeLater(() -> {
                        consola.agregarSeparador();
                        consola.agregarLinea("SIMULACION COMPLETADA", Color.GREEN);
                        consola.agregarLinea(resultados, Color.YELLOW);
                        
                        // --- INICIO: CÓDIGO AÑADIDO ---
                        // Llamar al nuevo método para mostrar la gráfica emergente
                        mostrarGraficaResultados(nombreAlgoritmo, tRetornoProm, tEsperaProm);
                        // --- FIN: CÓDIGO AÑADIDO ---
                        
                        consola.agregarLinea("Puedes cambiar el algoritmo y volver a iniciar.", Color.ORANGE);
                        btnIniciar.setText("Iniciar Simulación"); // Quitado emoji
                        btnIniciar.setBackground(COLOR_BOTON);
                    });
                }
            }
        }).start();
    }

    // --- INICIO: MÉTODO NUEVO AÑADIDO PARA LA GRÁFICA ---
    /**
     * Crea y muestra una ventana emergente (JFrame) con una gráfica de barras
     * de JFreeChart que muestra los resultados de la simulación.
     * @param nombreAlgoritmo El nombre del algoritmo ejecutado.
     * @param tRetornoProm El tiempo de retorno promedio.
     * @param tEsperaProm El tiempo de espera promedio.
     */
    private void mostrarGraficaResultados(String nombreAlgoritmo, double tRetornoProm, double tEsperaProm) {
        
        // 1. Crear el Dataset (los datos de la gráfica)
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        // Añadimos dos series de datos. Cada una tendrá una barra.
        // valor, nombreDeLaSerie (leyenda), nombreDeLaCategoria (eje X)
        dataset.addValue(tRetornoProm, "Tiempo de Retorno Prom.", nombreAlgoritmo);
        dataset.addValue(tEsperaProm, "Tiempo de Espera Prom.", nombreAlgoritmo);

        // 2. Crear el Gráfico de Barras
        JFreeChart barChart = ChartFactory.createBarChart(
            "Reporte de Rendimiento: " + nombreAlgoritmo, // Título principal de la gráfica
            "Métricas",          // Etiqueta Eje X
            "Ciclos Promedio",   // Etiqueta Eje Y
            dataset,
            PlotOrientation.VERTICAL,
            true,  // Mostrar leyenda
            true,  // Usar tooltips (mostrar valor al pasar el mouse)
            false  // No usar URLs
        );

        // 3. Aplicar Estilo (simple y legible, basado en tu tema)
        barChart.setBackgroundPaint(COLOR_FONDO); // Fondo de la ventana
        barChart.getTitle().setPaint(COLOR_TEXTO); // Color del título
        barChart.getLegend().setBackgroundPaint(COLOR_FONDO); // Fondo de la leyenda
        barChart.getLegend().setItemPaint(Color.WHITE); // Texto de la leyenda

        CategoryPlot plot = barChart.getCategoryPlot();
        plot.setBackgroundPaint(new Color(20, 20, 40)); // Fondo del área de la gráfica
        plot.setRangeGridlinePaint(COLOR_BOTON); // Líneas de la cuadrícula
        plot.setDomainGridlinesVisible(false); // Ocultar líneas verticales (más limpio)

        // Estilo de los ejes
        plot.getDomainAxis().setLabelPaint(Color.WHITE);
        plot.getDomainAxis().setTickLabelPaint(Color.WHITE);
        plot.getRangeAxis().setLabelPaint(Color.WHITE);
        plot.getRangeAxis().setTickLabelPaint(Color.WHITE);
        
        // Estilo de las barras
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, COLOR_TEXTO); // Color para la barra "Tiempo de Retorno"
        renderer.setSeriesPaint(1, Color.ORANGE);  // Color para la barra "Tiempo de Espera"
        renderer.setDrawBarOutline(false); // Sin borde
        
        // 4. Crear el Panel y la Ventana Emergente
        ChartPanel chartPanel = new ChartPanel(barChart);
        chartPanel.setPreferredSize(new Dimension(550, 400)); // Tamaño por defecto

        JFrame popupFrame = new JFrame("Reporte: " + nombreAlgoritmo);
        popupFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Se cierra solo esta ventana
        popupFrame.setContentPane(chartPanel);
        popupFrame.pack(); // Ajusta la ventana al tamaño del chartPanel
        popupFrame.setLocationRelativeTo(this); // Centra la ventana sobre la MainGUI
        popupFrame.setVisible(true); // Muestra la ventana
    }
    // --- FIN: MÉTODO NUEVO AÑADIDO PARA LA GRÁFICA ---

    
    private void mostrarFormularioProceso() {
        JTextField nombreField = new JTextField();
        JTextField instruccionesField = new JTextField();
        JCheckBox esCpuBoundCheck = new JCheckBox("CPU Bound (ignora E/S)");
        JTextField ciclosExcepcionField = new JTextField("0");
        JTextField duracionESField = new JTextField("0");

        esCpuBoundCheck.addActionListener(e -> {
            boolean esCpuBound = esCpuBoundCheck.isSelected();
            ciclosExcepcionField.setEnabled(!esCpuBound);
            duracionESField.setEnabled(!esCpuBound);
        });
        esCpuBoundCheck.setSelected(true);
        ciclosExcepcionField.setEnabled(false);
        duracionESField.setEnabled(false);

        JPanel panelForm = new JPanel(new GridLayout(0, 2, 5, 5));
        panelForm.add(new JLabel("Nombre del Proceso:"));
        panelForm.add(nombreField);
        panelForm.add(new JLabel("Total de Instrucciones:"));
        panelForm.add(instruccionesField);
        panelForm.add(esCpuBoundCheck);
        panelForm.add(new JLabel()); 
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
                
                consola.agregarLinea("Proceso '" + nombre + "' añadido a la lista. Total: " + procesosParaSimular.sizeLista(), Color.CYAN); // Quitado emoji

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
                ConfiguracionSimulacion config = new ConfiguracionSimulacion();
                config.duracionCicloMs = Reloj.getCycleDurationMs();
                
                java.util.List<ProcesoData> procesosList = new java.util.ArrayList<>();
                for (int i = 0; i < procesosParaSimular.sizeLista(); i++) {
                    procesosList.add(new ProcesoData((Proceso) procesosParaSimular.get(i)));
                }
                config.procesos = procesosList;

                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                gson.toJson(config, writer);
                consola.agregarLinea("Configuracion guardada en: " + archivo.getAbsolutePath(), Color.CYAN); // Quitado emoji
            } catch (IOException e) {
                consola.agregarLinea("Error al guardar: " + e.getMessage(), Color.RED); // Quitado emoji
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

                Reloj.setCycleDurationMs(config.duracionCicloMs);

                this.procesosParaSimular.clear(); 
                for (ProcesoData data : config.procesos) {
                    Proceso p = new Proceso(data.nombre, data.totalInstructions, data.isCpuBound, data.ciclosExcepcionES, data.duracionES, 0);
                    this.procesosParaSimular.insertFinal(p);
                }
                consola.agregarLinea(config.procesos.size() + " procesos y configuracion cargados.", Color.CYAN);
            } catch (Exception e) {
                consola.agregarLinea("Error al cargar el archivo: " + e.getMessage(), Color.RED);
            }
        }
    }
    
    private void actualizarDashboard() {
        SwingUtilities.invokeLater(() -> {
            if (motorSimulacionActual == null) return;

            modeloListaListos.clear();
            ListaSimple procesosListos = motorSimulacionActual.getProcesosListos();
            for (int i = 0; i < procesosListos.sizeLista(); i++) {
                Proceso p = (Proceso) procesosListos.get(i);
                modeloListaListos.addElement(p.getId() + " - " + p.getName());
            }

            modeloListaBloqueados.clear();
            ListaSimple procesosBloqueados = motorSimulacionActual.getProcesosBloqueados();
            for (int i = 0; i < procesosBloqueados.sizeLista(); i++) {
                Proceso p = (Proceso) procesosBloqueados.get(i);
                modeloListaBloqueados.addElement(p.getId() + " - " + p.getName());
            }
            
            modeloListaListosSus.clear();
            ListaSimple procesosListosSus = motorSimulacionActual.getProcesosListosSuspendidos();
            for (int i = 0; i < procesosListosSus.sizeLista(); i++) {
                Proceso p = (Proceso) procesosListosSus.get(i);
                modeloListaListosSus.addElement(p.getId() + " - " + p.getName());
            }

            modeloListaBloqueadosSus.clear();
            ListaSimple procesosBloqueadosSus = motorSimulacionActual.getProcesosBloqueadosSuspendidos();
            for (int i = 0; i < procesosBloqueadosSus.sizeLista(); i++) {
                Proceso p = (Proceso) procesosBloqueadosSus.get(i);
                modeloListaBloqueadosSus.addElement(p.getId() + " - " + p.getName());
            }

            modeloListaTerminados.clear();
            ListaSimple procesosTerminados = motorSimulacionActual.getProcesosTerminados();
            for (int i = 0; i < procesosTerminados.sizeLista(); i++) {
                Proceso p = (Proceso) procesosTerminados.get(i);
                modeloListaTerminados.addElement(p.getId() + " - " + p.getName());
            }
            
            Proceso enCpu = motorSimulacionActual.getProcesoEjecutandoActual();
            labelCiclos.setText("Ciclo: " + Reloj.getCurrentCycle()); 

            if (enCpu != null) {
                labelProcesoCPU.setText(enCpu.getId() + " - " + enCpu.getName());
                labelModoOperacion.setText("Modo: Usuario");
                labelModoOperacion.setForeground(Color.CYAN);
                String info = String.format(
                    "ID: %s\n" +
                    "Nombre: %s\n" +
                    "Estado: %s\n" +
                    "PC: %d / %d\n" +
                    "MAR: %d\n" + 
                    "--------------------\n" +
                    "Tiempo Llegada: %d\n" +
                    "Tiempo Inicio Ejec.: %d\n",
                    enCpu.getId(),
                    enCpu.getName(),
                    enCpu.getState().toString(),
                    enCpu.getPc(),
                    enCpu.getTotalInstructions(),
                    enCpu.getMar(),
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
        
        SwingUtilities.invokeLater(() -> new MainGUI().setVisible(true));
    }
}