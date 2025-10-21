/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemasoperativos_arellanomartinez.Controller;


import edd.ListaSimple;
import sistemasoperativos_arellanomartinez.Planificador.Planificador;
import sistemasoperativos_arellanomartinez.Simulador.Proceso;
import sistemasoperativos_arellanomartinez.Simulador.Reloj;
import java.util.concurrent.Semaphore;

/**
 * Motor principal de simulación - Usa ListaSimple personalizada
 */
public class Engine {
    // 🔹 COMPONENTES QUE COORDINA
    private Planificador planificador;
    private ListaSimple todosProcesos;  // ✅ TU ListaSimple
    private volatile boolean simulacionActiva;
    private Thread hiloSimulacion;
    private Semaphore semaforo;  // ✅ Se crea en el constructor
    
    // 🔹 MÉTRICAS GLOBALES
    private int ciclosTotales;
    private int cambiosContexto;
    
    public Engine(Planificador planificador) {
        this.planificador = planificador;
        this.todosProcesos = new ListaSimple();  // ✅ TU estructura
        this.simulacionActiva = false;
        this.semaforo = new Semaphore(1);  // 🚦 SEMÁFORO CREADO AQUÍ
        this.ciclosTotales = 0;
        this.cambiosContexto = 0;
        
        System.out.println("🔧 SimulationEngine creado con semáforo");
    }
    
    /**
     * 🎬 Inicia la simulación en un hilo separado
     */
    public void iniciarSimulacion() {
        if (simulacionActiva) {
            System.out.println("⚠️  La simulación ya está activa");
            return;
        }
        
        simulacionActiva = true;
        hiloSimulacion = new Thread(this::ejecutarCicloSimulacion);
        hiloSimulacion.setName("SimulationEngine-Thread");
        hiloSimulacion.start();
        
        System.out.println("🚀 SimulationEngine iniciado");
    }
    
    /**
     * 🔄 Ciclo principal de simulación
     */
    private void ejecutarCicloSimulacion() {
        System.out.println("🔄 Hilo de simulación iniciado");
        
        while (simulacionActiva && !Thread.currentThread().isInterrupted()) {
            try {
                // 🚦 ADQUIRIR SEMÁFORO (sincronización)
                semaforo.acquire();
                
                // 1. AVANZAR TIEMPO
                Reloj.tick();
                ciclosTotales++;
                
                // 2. EJECUTAR PLANIFICADOR
                Proceso procesoSeleccionado = planificador.seleccionarProximoProceso();
                
                // 3. CONTROLAR HILOS DE PROCESOS
                controlarEjecucionProcesos(procesoSeleccionado);
                
                // 4. MANEJAR OPERACIONES E/S
                manejarOperacionesES();
                
                // 5. ACTUALIZAR MÉTRICAS CADA 5 CICLOS
                if (ciclosTotales % 5 == 0) {
                    mostrarEstadoActual();
                }
                
                // 🚦 LIBERAR SEMÁFORO
                semaforo.release();
                
                // 6. ESPERAR SEGÚN VELOCIDAD CONFIGURADA
                Thread.sleep(Reloj.getCycleDurationMs());
                
            } catch (InterruptedException e) {
                System.out.println("⏹️  Hilo de simulación interrumpido");
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                System.err.println("❌ Error en SimulationEngine: " + e.getMessage());
                semaforo.release();  // 🚦 IMPORTANTE: Liberar en caso de error
            }
        }
        
        System.out.println("🛑 Hilo de simulación finalizado");
    }
    
    /**
     * 🎮 Controla qué procesos ejecutan en sus hilos internos
     */
    private void controlarEjecucionProcesos(Proceso procesoSeleccionado) {
        int procesosPausados = 0;
        
        // Recorrer todos los procesos usando TU ListaSimple
        for (int i = 0; i < todosProcesos.sizeLista(); i++) {
            Proceso proceso = (Proceso) todosProcesos.get(i);
            
            if (proceso != procesoSeleccionado && proceso.isEjecutando()) {
                proceso.pausarEjecucion();
                procesosPausados++;
                cambiosContexto++;
            }
        }
        
        // Iniciar el proceso seleccionado
        if (procesoSeleccionado != null && !procesoSeleccionado.isFinished()) {
            if (!procesoSeleccionado.isEjecutando()) {
                procesoSeleccionado.iniciarEjecucion();
                System.out.println("🎯 CPU asignada a: " + procesoSeleccionado.getName());
            }
        }
        
        if (procesosPausados > 0) {
            System.out.println("⏸️  " + procesosPausados + " procesos pausados");
        }
    }
    
    /**
     * 🖨️ Maneja operaciones de Entrada/Salida
     */
    private void manejarOperacionesES() {
        int procesosEnES = 0;
        
        for (int i = 0; i < todosProcesos.sizeLista(); i++) {
            Proceso proceso = (Proceso) todosProcesos.get(i);
            
            if (proceso.estaEnES()) {
                procesosEnES++;
                proceso.procesarCicloES();
                
                // Si terminó la E/S, volver a competir
                if (!proceso.estaEnES() && !proceso.isFinished()) {
                    proceso.reanudarEjecucion();
                    System.out.println("✅ " + proceso.getName() + " volvió de E/S");
                }
            }
        }
    }
    
    /**
     * 📊 Agrega un proceso a la simulación
     */
    public void agregarProceso(Proceso proceso) {
        try {
            semaforo.acquire();  // 🚦 Sincronizar acceso
            
            todosProcesos.insertFinal(proceso);  // ✅ TU método
            planificador.agregarProceso(proceso);
            
            semaforo.release();  // 🚦 Liberar
            
            System.out.println("📥 Proceso agregado: " + proceso.getName());
            
        } catch (InterruptedException e) {
            System.out.println("❌ Interrupción al agregar proceso");
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * ⏸️ Pausa la simulación completa
     */
    public void pausarSimulacion() {
        simulacionActiva = false;
        int procesosPausados = 0;
        
        System.out.println("⏸️  Pausando simulación...");
        
        for (int i = 0; i < todosProcesos.sizeLista(); i++) {
            Proceso proceso = (Proceso) todosProcesos.get(i);
            if (proceso.isEjecutando()) {
                proceso.pausarEjecucion();
                procesosPausados++;
            }
        }
        
        System.out.println("⏸️  SimulationEngine pausado (" + procesosPausados + " procesos pausados)");
    }
    
    /**
     * ▶️ Reanuda la simulación
     */
    public void reanudarSimulacion() {
        System.out.println("▶️  Reanudando simulación...");
        simulacionActiva = true;
        iniciarSimulacion();
    }
    
    /**
     * ⏹️ Detiene completamente la simulación
     */
    public void detenerSimulacion() {
        System.out.println("🛑 Deteniendo simulación...");
        simulacionActiva = false;
        
        if (hiloSimulacion != null && hiloSimulacion.isAlive()) {
            hiloSimulacion.interrupt();
        }
        
        int procesosDetenidos = 0;
        for (int i = 0; i < todosProcesos.sizeLista(); i++) {
            Proceso proceso = (Proceso) todosProcesos.get(i);
            proceso.detenerEjecucion();
            procesosDetenidos++;
        }
        
        System.out.println("🛑 SimulationEngine detenido (" + procesosDetenidos + " procesos detenidos)");
    }
    
    /**
     * 📈 Muestra estado actual de la simulación
     */
    private void mostrarEstadoActual() {
        System.out.println("\n📊 === CICLO " + ciclosTotales + " ===");
        System.out.println("Algoritmo: " + planificador.getNombreAlgoritmo());
        System.out.println("Procesos activos: " + contarProcesosActivos() + "/" + todosProcesos.sizeLista());
        System.out.println("Cambios de contexto: " + cambiosContexto);
        System.out.println("Velocidad: " + Reloj.getCycleDurationMs() + "ms/ciclo");
        
        for (int i = 0; i < todosProcesos.sizeLista(); i++) {
            Proceso p = (Proceso) todosProcesos.get(i);
            System.out.println(" - " + p.getName() + ": " + p.getPc() + "/" + 
                             p.getTotalInstructions() + " [" + p.getState() + 
                             "] Hilo: " + p.isEjecutando());
        }
    }
    
    /**
     * 🔍 Cuenta procesos activos (no terminados)
     */
    public int contarProcesosActivos() {
        int activos = 0;
        for (int i = 0; i < todosProcesos.sizeLista(); i++) {
            Proceso p = (Proceso) todosProcesos.get(i);
            if (!p.isFinished()) {
                activos++;
            }
        }
        return activos;
    }
    
    /**
     * 📋 Obtiene copia de los procesos (para la GUI)
     */
    public ListaSimple getProcesos() {
        // Crear una nueva lista para no compartir la referencia interna
        ListaSimple copia = new ListaSimple();
        try {
            semaforo.acquire();
            
            for (int i = 0; i < todosProcesos.sizeLista(); i++) {
                copia.insertFinal(todosProcesos.get(i));
            }
            
            semaforo.release();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return copia;
    }
    
    // 🔹 MÉTODOS DE CONSULTA
    public boolean isSimulacionActiva() {
        return simulacionActiva;
    }
    
    public int getCiclosTotales() {
        return ciclosTotales;
    }
    
    public int getCambiosContexto() {
        return cambiosContexto;
    }
    
    public String getEstadoSimulacion() {
        return simulacionActiva ? "Ejecutándose" : "Detenida";
    }
}