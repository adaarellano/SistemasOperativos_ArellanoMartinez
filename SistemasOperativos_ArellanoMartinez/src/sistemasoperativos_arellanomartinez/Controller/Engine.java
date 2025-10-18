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
 * Motor principal de simulación con semáforos mejorados
 */
public class Engine {
    // 🔹 COMPONENTES QUE COORDINA
    private Planificador planificador;
    private ListaSimple todosProcesos;
    private volatile boolean simulacionActiva;
    private Thread hiloSimulacion;
    
    // 🔐 SEMÁFOROS MEJORADOS
    private final Semaphore semaforoGlobal;    // Exclusión mútua global
    private final Semaphore semaforoES;        // Control de E/S simultáneas
    private final Semaphore semaforoPlanificador; // Protección del planificador
    
    // 🔹 MÉTRICAS GLOBALES
    private int ciclosTotales;
    private int cambiosContexto;
    private int operacionesESCompletadas;
    
    public Engine(Planificador planificador) {
        this.planificador = planificador;
        this.todosProcesos = new ListaSimple();
        this.simulacionActiva = false;
        
        // 🔐 INICIALIZAR SEMÁFOROS MEJORADOS
        this.semaforoGlobal = new Semaphore(1);        // Exclusión mútua
        this.semaforoES = new Semaphore(3);            // Máximo 3 E/S simultáneas
        this.semaforoPlanificador = new Semaphore(1);  // Protección planificador
        
        this.ciclosTotales = 0;
        this.cambiosContexto = 0;
        this.operacionesESCompletadas = 0;
        
        System.out.println("🔧 SimulationEngine creado con 3 semáforos");
        System.out.println("   - SemaforoGlobal: Exclusión mútua");
        System.out.println("   - SemaforoES: 3 E/S simultáneas máximo");
        System.out.println("   - SemaforoPlanificador: Protección planificador");
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
        hiloSimulacion.setName("Engine-Simulation-Thread");
        hiloSimulacion.start();
        
        System.out.println("🚀 SimulationEngine iniciado con semáforos");
    }
    
    /**
     * 🔄 Ciclo principal de simulación con semáforos
     */
    private void ejecutarCicloSimulacion() {
        System.out.println("🔄 Hilo de simulación iniciado");
        
        while (simulacionActiva && !Thread.currentThread().isInterrupted()) {
            try {
                // 🔐 ADQUIRIR SEMÁFORO GLOBAL
                semaforoGlobal.acquire();
                
                // 1. AVANZAR TIEMPO
                Reloj.tick();
                ciclosTotales++;
                
                // 2. EJECUTAR PLANIFICADOR CON SEMÁFORO ESPECÍFICO
                ejecutarPlanificadorConSemaforo();
                
                // 3. CONTROLAR HILOS DE PROCESOS
                controlarEjecucionProcesos();
                
                // 4. MANEJAR OPERACIONES E/S CON SEMÁFORO ESPECÍFICO
                manejarOperacionesESConSemaforo();
                
                // 5. ACTUALIZAR MÉTRICAS CADA 5 CICLOS
                if (ciclosTotales % 5 == 0) {
                    mostrarEstadoActual();
                }
                
                // 🔐 LIBERAR SEMÁFORO GLOBAL
                semaforoGlobal.release();
                
                // 6. ESPERAR SEGÚN VELOCIDAD CONFIGURADA
                Thread.sleep(Reloj.getCycleDurationMs());
                
            } catch (InterruptedException e) {
                System.out.println("⏹️  Hilo de simulación interrumpido");
                liberarSemaforos(); // 🔐 LIBERAR TODOS LOS SEMÁFOROS
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                System.err.println("❌ Error en SimulationEngine: " + e.getMessage());
                liberarSemaforos(); // 🔐 LIBERAR TODOS LOS SEMÁFOROS
            }
        }
        
        System.out.println("🛑 Hilo de simulación finalizado");
    }
    
    /**
     * 🔐 Ejecutar planificador con semáforo específico
     */
    private void ejecutarPlanificadorConSemaforo() {
        try {
            semaforoPlanificador.acquire();
            
            Proceso procesoSeleccionado = planificador.seleccionarProximoProceso();
            
            if (procesoSeleccionado != null) {
                // Verificar si hubo cambio de contexto
                if (!procesoSeleccionado.isEjecutando() || 
                    procesoSeleccionado.getState() != Proceso.Estado.EJECUTANDO) {
                    cambiosContexto++;
                }
            }
            
            semaforoPlanificador.release();
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            semaforoPlanificador.release();
        }
    }
    
    /**
     * 🎮 Controla qué procesos ejecutan en sus hilos internos
     */
    private void controlarEjecucionProcesos() {
        int procesosPausados = 0;
        int procesosEjecutando = 0;
        
        try {
            semaforoGlobal.acquire();
            
            // Recorrer todos los procesos
            for (int i = 0; i < todosProcesos.sizeLista(); i++) {
                Proceso proceso = (Proceso) todosProcesos.get(i);
                
                // Solo procesos que no estén en E/S pueden ser pausados/ejecutados
                if (!proceso.estaEnES()) {
                    if (proceso.isEjecutando() && proceso.getState() == Proceso.Estado.EJECUTANDO) {
                        procesosEjecutando++;
                    } else if (proceso.isEjecutando() && proceso.getState() != Proceso.Estado.EJECUTANDO) {
                        proceso.pausarEjecucion();
                        procesosPausados++;
                    }
                }
            }
            
            semaforoGlobal.release();
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            semaforoGlobal.release();
        }
        
        if (procesosPausados > 0) {
            System.out.println("⏸️  " + procesosPausados + " procesos pausados");
        }
        if (procesosEjecutando > 0) {
            System.out.println("⚡ " + procesosEjecutando + " procesos ejecutando");
        }
    }
    
    /**
     * 🖨️ Maneja operaciones de E/S con semáforo específico
     */
    private void manejarOperacionesESConSemaforo() {
        try {
            // 🔐 INTENTAR ADQUIRIR SEMÁFORO E/S (no bloqueante)
            if (semaforoES.tryAcquire()) {
                int procesosEnES = 0;
                int procesosVolvieronES = 0;
                
                semaforoGlobal.acquire();
                
                for (int i = 0; i < todosProcesos.sizeLista(); i++) {
                    Proceso proceso = (Proceso) todosProcesos.get(i);
                    
                    if (proceso.estaEnES()) {
                        procesosEnES++;
                        
                        // 🧵 EL HILO E/S DEL PROCESO MANEJA LA E/S AUTOMÁTICAMENTE
                        // Solo verificamos si terminó para notificar al planificador
                        if (!proceso.estaEnES() && !proceso.isFinished() && 
                            proceso.getState() == Proceso.Estado.LISTO) {
                            
                            // ✅ PROCESO VOLVIÓ DE E/S - NOTIFICAR AL PLANIFICADOR
                            planificador.procesoVolvioDeES(proceso);
                            procesosVolvieronES++;
                            operacionesESCompletadas++;
                            
                            System.out.println("✅ " + proceso.getName() + " volvió de E/S al CPU original");
                        }
                    }
                }
                
                semaforoGlobal.release();
                
                // 🔐 LIBERAR SEMÁFORO E/S
                semaforoES.release();
                
                if (procesosEnES > 0) {
                    System.out.println("💾 " + procesosEnES + " procesos en E/S " +
                                     "(Semáforo ES: " + semaforoES.availablePermits() + "/3 disponibles)");
                }
                if (procesosVolvieronES > 0) {
                    System.out.println("🔄 " + procesosVolvieronES + " procesos volvieron de E/S");
                }
                
            } else {
                // No se pudo adquirir semáforo E/S - demasiadas E/S simultáneas
                System.out.println("⚠️  Límite de E/S alcanzado (" + 
                                 semaforoES.availablePermits() + "/3 disponibles)");
            }
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            liberarSemaforos();
        } catch (Exception e) {
            System.err.println("❌ Error en manejo E/S: " + e.getMessage());
            liberarSemaforos();
        }
    }
    
    /**
     * 📥 Agrega un proceso a la simulación con semáforos
     */
    public void agregarProceso(Proceso proceso) {
        try {
            semaforoGlobal.acquire(); // 🔐 ADQUIRIR SEMÁFORO GLOBAL
            semaforoPlanificador.acquire(); // 🔐 ADQUIRIR SEMÁFORO PLANIFICADOR
            
            todosProcesos.insertFinal(proceso);
            planificador.agregarProceso(proceso);
            
            semaforoPlanificador.release(); // 🔐 LIBERAR SEMÁFORO PLANIFICADOR
            semaforoGlobal.release(); // 🔐 LIBERAR SEMÁFORO GLOBAL
            
            System.out.println("📥 Proceso agregado con semáforos: " + proceso.getName());
            System.out.println("   🧵 Threads: " + proceso.getInfoThreads());
            
        } catch (InterruptedException e) {
            System.out.println("❌ Interrupción al agregar proceso");
            liberarSemaforos();
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
        
        try {
            semaforoGlobal.acquire();
            
            for (int i = 0; i < todosProcesos.sizeLista(); i++) {
                Proceso proceso = (Proceso) todosProcesos.get(i);
                if (proceso.isEjecutando()) {
                    proceso.pausarEjecucion();
                    procesosPausados++;
                }
            }
            
            semaforoGlobal.release();
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            semaforoGlobal.release();
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
        try {
            semaforoGlobal.acquire();
            
            for (int i = 0; i < todosProcesos.sizeLista(); i++) {
                Proceso proceso = (Proceso) todosProcesos.get(i);
                proceso.detenerEjecucion();
                procesosDetenidos++;
            }
            
            semaforoGlobal.release();
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            semaforoGlobal.release();
        }
        
        System.out.println("🛑 SimulationEngine detenido (" + procesosDetenidos + " procesos detenidos)");
    }
    
    /**
     * 🔐 Libera todos los semáforos (seguridad)
     */
    private void liberarSemaforos() {
        semaforoGlobal.release();
        semaforoES.release();
        semaforoPlanificador.release();
        System.out.println("🔐 Todos los semáforos liberados");
    }
    
    /**
     * 📊 Muestra estado actual de la simulación
     */
    private void mostrarEstadoActual() {
        try {
            semaforoGlobal.acquire();
            
            System.out.println("\n📊 === CICLO " + ciclosTotales + " ===");
            System.out.println("Algoritmo: " + planificador.getNombreAlgoritmo());
            System.out.println("Procesos activos: " + contarProcesosActivos() + "/" + todosProcesos.sizeLista());
            System.out.println("Cambios de contexto: " + cambiosContexto);
            System.out.println("Operaciones E/S completadas: " + operacionesESCompletadas);
            System.out.println("Velocidad: " + Reloj.getCycleDurationMs() + "ms/ciclo");
            System.out.println("Semáforos - Global: " + semaforoGlobal.availablePermits() + 
                             ", E/S: " + semaforoES.availablePermits() + 
                             ", Planificador: " + semaforoPlanificador.availablePermits());
            
            // Mostrar estado de threads
            System.out.println("🧵 ESTADO DE THREADS:");
            for (int i = 0; i < todosProcesos.sizeLista(); i++) {
                Proceso p = (Proceso) todosProcesos.get(i);
                System.out.println("   - " + p.getInfoThreads());
            }
            
            semaforoGlobal.release();
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            semaforoGlobal.release();
        }
    }
    
    /**
     * 🔍 Cuenta procesos activos (no terminados)
     */
    public int contarProcesosActivos() {
        int activos = 0;
        try {
            semaforoGlobal.acquire();
            
            for (int i = 0; i < todosProcesos.sizeLista(); i++) {
                Proceso p = (Proceso) todosProcesos.get(i);
                if (!p.isFinished()) {
                    activos++;
                }
            }
            
            semaforoGlobal.release();
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            semaforoGlobal.release();
        }
        return activos;
    }
    
    /**
     * 📋 Obtiene copia de los procesos (para la GUI)
     */
    public ListaSimple getProcesos() {
        ListaSimple copia = new ListaSimple();
        try {
            semaforoGlobal.acquire();
            
            for (int i = 0; i < todosProcesos.sizeLista(); i++) {
                copia.insertFinal(todosProcesos.get(i));
            }
            
            semaforoGlobal.release();
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            semaforoGlobal.release();
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
    
    public int getOperacionesESCompletadas() {
        return operacionesESCompletadas;
    }
    
    public String getEstadoSimulacion() {
        return simulacionActiva ? "Ejecutándose" : "Detenida";
    }
    
    /**
     * 📈 Obtiene información de semáforos
     */
    public String getInfoSemaforos() {
        return String.format("Global: %d/1, E/S: %d/3, Planificador: %d/1",
            semaforoGlobal.availablePermits(),
            semaforoES.availablePermits(),
            semaforoPlanificador.availablePermits());
    }
}