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
 * Motor principal de simulacion con coordinacion mejorada
 */
public class Engine {
    // COMPONENTES QUE COORDINA
    private Planificador planificador;
    private ListaSimple todosProcesos;
    private volatile boolean simulacionActiva;
    private Thread hiloSimulacion;
    private Proceso procesoEjecutandoActual;
    
    // SEMAFOROS MEJORADOS
    private final Semaphore semaforoGlobal;    // Exclusion mutua global
    private final Semaphore semaforoES;        // Control de E/S simultaneas
    private final Semaphore semaforoPlanificador; // Proteccion del planificador
    
    // METRICAS GLOBALES
    private int ciclosTotales;
    private int cambiosContexto;
    private int operacionesESCompletadas;
    private int procesosSuspendidos;
    
    public Engine(Planificador planificador) {
        this.planificador = planificador;
        this.todosProcesos = new ListaSimple();
        this.simulacionActiva = false;
        this.procesoEjecutandoActual = null;
        
        // INICIALIZAR SEMAFOROS MEJORADOS
        this.semaforoGlobal = new Semaphore(1);        // Exclusion mutua
        this.semaforoES = new Semaphore(3);            // Maximo 3 E/S simultaneas
        this.semaforoPlanificador = new Semaphore(1);  // Proteccion planificador
        
        this.ciclosTotales = 0;
        this.cambiosContexto = 0;
        this.operacionesESCompletadas = 0;
        this.procesosSuspendidos = 0;
        
        System.out.println("Engine creado con coordinacion mejorada");
    }
    
    /**
     * Inicia la simulacion en un hilo separado
     */
    public void iniciarSimulacion() {
        if (simulacionActiva) {
            System.out.println("La simulacion ya esta activa");
            return;
        }
        
        simulacionActiva = true;
        hiloSimulacion = new Thread(this::ejecutarCicloSimulacion);
        hiloSimulacion.setName("Engine-Simulation-Thread");
        hiloSimulacion.start();
        
        System.out.println("Engine iniciado con coordinacion mejorada");
    }
    
    /**
     * Ciclo principal de simulacion con coordinacion mejorada
     */
    private void ejecutarCicloSimulacion() {
        System.out.println("Hilo de simulacion iniciado");
        
        while (simulacionActiva && !Thread.currentThread().isInterrupted()) {
            try {
                // ADQUIRIR SEMAFORO GLOBAL
                semaforoGlobal.acquire();
                
                // 1. AVANZAR TIEMPO
                Reloj.tick();
                ciclosTotales++;
                
                // 2. EJECUTAR PLANIFICADOR CON CONTROL DE EJECUCION
                ejecutarPlanificadorConControl();
                
                // 3. MANEJAR OPERACIONES E/S
                manejarOperacionesES();
                
                // 4. ACTUALIZAR METRICAS CADA 5 CICLOS
                if (ciclosTotales % 5 == 0) {
                    mostrarEstadoActual();
                }
                
                // LIBERAR SEMAFORO GLOBAL
                semaforoGlobal.release();
                
                // 5. ESPERAR SEGUN VELOCIDAD CONFIGURADA
                Thread.sleep(Reloj.getCycleDurationMs());
                
            } catch (InterruptedException e) {
                System.out.println("Hilo de simulacion interrumpido");
                liberarSemaforos();
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                System.err.println("Error en Engine: " + e.getMessage());
                e.printStackTrace();
                liberarSemaforos();
            }
        }
        
        System.out.println("Hilo de simulacion finalizado");
    }
    
    /**
     * Ejecutar planificador con control de ejecucion mejorado
     */
    private void ejecutarPlanificadorConControl() {
        try {
            semaforoPlanificador.acquire();
            
            // 1. DETENER EJECUCION DE TODOS LOS PROCESOS
            detenerEjecucionTodosProcesos();
            
            // 2. SELECCIONAR NUEVO PROCESO
            Proceso procesoSeleccionado = planificador.seleccionarProximoProceso();
            
            // 3. VERIFICAR SI HAY CAMBIO DE PROCESO
            if (procesoSeleccionado != procesoEjecutandoActual) {
                cambiosContexto++;
                
                // Actualizar proceso actual
                procesoEjecutandoActual = procesoSeleccionado;
                
                if (procesoEjecutandoActual != null) {
                    // Registrar inicio de ejecucion (si es primera vez)
                    if (procesoEjecutandoActual.getTiempoInicioEjecucion() == -1) {
                        procesoEjecutandoActual.setTiempoInicioEjecucion(Reloj.getCurrentCycle());
                    }
                    
                    // 🔥 CAMBIO: Configurar estado y PERMITIR EJECUCION usando el nuevo método
                    procesoEjecutandoActual.setState(Proceso.Estado.EJECUTANDO);
                    procesoEjecutandoActual.reanudarEjecucion(); // <- NUEVO MÉTODO
                    
                    System.out.println("NUEVO proceso en CPU: " + procesoEjecutandoActual.getName());
                } else {
                    System.out.println("CPU LIBRE - No hay procesos para ejecutar");
                }
            } else if (procesoEjecutandoActual != null) {
                // Mismo proceso, mantener ejecucion SOLO a este
                procesoEjecutandoActual.reanudarEjecucion(); // <- NUEVO MÉTODO
            }
            
            // 🔥 NUEVO: Asegurar que SOLO el proceso actual tiene permiso
            asegurarUnSoloProcesoConPermiso();
            
            // 🔥 NUEVO: Ejecutar ciclo del proceso actual
            if (procesoEjecutandoActual != null && 
                procesoEjecutandoActual.getState() == Proceso.Estado.EJECUTANDO &&
                !procesoEjecutandoActual.estaEnES() &&
                !procesoEjecutandoActual.isSuspendido()) {
                
                procesoEjecutandoActual.permitirEjecutarCiclo(); // <- NUEVO MÉTODO
            }
            
            semaforoPlanificador.release();
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            semaforoPlanificador.release();
        }
    }
    
    /**
     * 🔥 NUEVO: Asegura que solo el proceso actual tenga permiso de ejecucion
     */
    private void asegurarUnSoloProcesoConPermiso() {
        for (int i = 0; i < todosProcesos.sizeLista(); i++) {
            Proceso proceso = (Proceso) todosProcesos.get(i);
            
            // Si no es el proceso actual y NO está pausado, pausarlo
            if (proceso != procesoEjecutandoActual && !proceso.isPausado()) {
                proceso.pausarEjecucion();
            }
        }
    }
    
    /**
     * Detener ejecucion de todos los procesos (excepto el seleccionado)
     */
    private void detenerEjecucionTodosProcesos() {
        for (int i = 0; i < todosProcesos.sizeLista(); i++) {
            Proceso proceso = (Proceso) todosProcesos.get(i);
            
            // No detener el proceso que va a ser ejecutado
            if (proceso != procesoEjecutandoActual) {
                proceso.pausarEjecucion(); // <- NUEVO MÉTODO
            }
        }
    }
    
    /**
     * Maneja operaciones de E/S con semaforo especifico
     */
    private void manejarOperacionesES() {
        try {
            // INTENTAR ADQUIRIR SEMAFORO E/S (no bloqueante)
            if (semaforoES.tryAcquire()) {
                int procesosEnES = 0;
                int procesosVolvieronES = 0;
                int procesosEnESCompletados = 0;
                
                for (int i = 0; i < todosProcesos.sizeLista(); i++) {
                    Proceso proceso = (Proceso) todosProcesos.get(i);
                    
                    if (proceso.estaEnES()) {
                        procesosEnES++;
                        
                        // Procesar ciclo de E/S
                        proceso.procesarCicloES();
                        
                        // Verificar si termino E/S
                        if (!proceso.estaEnES() && !proceso.isFinished() && 
                            proceso.getState() == Proceso.Estado.LISTO) {
                            
                            // PROCESO VOLVIO DE E/S - NOTIFICAR AL PLANIFICADOR
                            planificador.procesoVolvioDeES(proceso);
                            procesosVolvieronES++;
                            operacionesESCompletadas++;
                            procesosEnESCompletados++;
                            
                            System.out.println(proceso.getName() + " volvio de E/S");
                        }
                    }
                }
                
                // LIBERAR SEMAFORO E/S
                semaforoES.release();
                
                if (procesosEnES > 0) {
                    System.out.println("E/S: " + procesosEnES + " procesos en E/S, " + 
                                     procesosEnESCompletados + " completados");
                }
                
            } else {
                // No se pudo adquirir semaforo E/S - demasiadas E/S simultaneas
                System.out.println("Limite de E/S alcanzado (" + 
                                 semaforoES.availablePermits() + "/3 disponibles)");
            }
            
        } catch (Exception e) {
            System.err.println("Error en manejo E/S: " + e.getMessage());
            e.printStackTrace();
            liberarSemaforos();
        }
    }
    
    /**
     * Agrega un proceso a la simulacion
     */
    public void agregarProceso(Proceso proceso) {
        try {
            semaforoGlobal.acquire();
            semaforoPlanificador.acquire();
            
            todosProcesos.insertFinal(proceso);
            planificador.agregarProceso(proceso);
            
            // 🔥 CAMBIO: INICIAR THREAD DEL PROCESO (pero pausado inicialmente)
            proceso.iniciarEjecucion(); // Ya inicia pausado por defecto
            
            semaforoPlanificador.release();
            semaforoGlobal.release();
            
            System.out.println("Proceso agregado: " + proceso.getName());
            
        } catch (InterruptedException e) {
            System.out.println("Interrupcion al agregar proceso");
            liberarSemaforos();
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Suspende un proceso especifico
     */
    public void suspenderProceso(Proceso proceso) {
        try {
            semaforoGlobal.acquire();
            
            if (!proceso.isSuspendido() && !proceso.isFinished()) {
                proceso.suspender();
                procesosSuspendidos++;
                
                // Si estaba ejecutando, seleccionar nuevo proceso
                if (proceso == procesoEjecutandoActual) {
                    procesoEjecutandoActual = null;
                    cambiosContexto++;
                }
            }
            
            semaforoGlobal.release();
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            semaforoGlobal.release();
        }
    }
    
    /**
     * Reanuda un proceso suspendido
     */
    public void reanudarProceso(Proceso proceso) {
        try {
            semaforoGlobal.acquire();
            
            if (proceso.isSuspendido()) {
                proceso.reanudar();
                procesosSuspendidos--;
            }
            
            semaforoGlobal.release();
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            semaforoGlobal.release();
        }
    }
    
    /**
     * Suspende todos los procesos
     */
    public void suspenderTodosProcesos() {
        try {
            semaforoGlobal.acquire();
            
            int suspendidos = 0;
            for (int i = 0; i < todosProcesos.sizeLista(); i++) {
                Proceso proceso = (Proceso) todosProcesos.get(i);
                if (!proceso.isSuspendido() && !proceso.isFinished()) {
                    proceso.suspender();
                    suspendidos++;
                }
            }
            
            procesosSuspendidos = suspendidos;
            procesoEjecutandoActual = null;
            
            semaforoGlobal.release();
            
            System.out.println(suspendidos + " procesos suspendidos");
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            semaforoGlobal.release();
        }
    }
    
    /**
     * Reanuda todos los procesos suspendidos
     */
    public void reanudarTodosProcesos() {
        try {
            semaforoGlobal.acquire();
            
            int reanudados = 0;
            for (int i = 0; i < todosProcesos.sizeLista(); i++) {
                Proceso proceso = (Proceso) todosProcesos.get(i);
                if (proceso.isSuspendido()) {
                    proceso.reanudar();
                    reanudados++;
                }
            }
            
            procesosSuspendidos = 0;
            
            semaforoGlobal.release();
            
            System.out.println(reanudados + " procesos reanudados");
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            semaforoGlobal.release();
        }
    }
    
    /**
     * Pausa la simulacion completa
     */
    public void pausarSimulacion() {
        simulacionActiva = false;
        int procesosPausados = 0;
        
        System.out.println("Pausando simulacion...");
        
        try {
            semaforoGlobal.acquire();
            
            for (int i = 0; i < todosProcesos.sizeLista(); i++) {
                Proceso proceso = (Proceso) todosProcesos.get(i);
                if (!proceso.isPausado()) {
                    proceso.pausarEjecucion();
                    procesosPausados++;
                }
            }
            
            procesoEjecutandoActual = null;
            
            semaforoGlobal.release();
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            semaforoGlobal.release();
        }
        
        System.out.println("Simulacion pausada (" + procesosPausados + " procesos pausados)");
    }
    
    /**
     * Reanuda la simulacion
     */
    public void reanudarSimulacion() {
        System.out.println("Reanudando simulacion...");
        simulacionActiva = true;
        iniciarSimulacion();
    }
    
    /**
     * Detiene completamente la simulacion
     */
    public void detenerSimulacion() {
        System.out.println("Deteniendo simulacion...");
        simulacionActiva = false;
        
        if (hiloSimulacion != null && hiloSimulacion.isAlive()) {
            hiloSimulacion.interrupt();
        }
        
        int procesosDetenidos = 0;
        try {
            semaforoGlobal.acquire();
            
            for (int i = 0; i < todosProcesos.sizeLista(); i++) {
                Proceso proceso = (Proceso) todosProcesos.get(i);
                proceso.detenerCompletamente();
                procesosDetenidos++;
            }
            
            procesoEjecutandoActual = null;
            
            semaforoGlobal.release();
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            semaforoGlobal.release();
        }
        
        System.out.println("Simulacion detenida (" + procesosDetenidos + " procesos detenidos)");
    }
    
    /**
     * Libera todos los semaforos (seguridad)
     */
    private void liberarSemaforos() {
        semaforoGlobal.release();
        semaforoES.release();
        semaforoPlanificador.release();
    }
    
    /**
     * Muestra estado actual de la simulacion
     */
    private void mostrarEstadoActual() {
        try {
            semaforoGlobal.acquire();
            
            System.out.println("\n=== CICLO " + ciclosTotales + " ===");
            System.out.println("Algoritmo: " + planificador.getNombreAlgoritmo());
            System.out.println("CPU: " + (procesoEjecutandoActual != null ? 
                procesoEjecutandoActual.getName() : "LIBRE"));
            System.out.println("Procesos activos: " + contarProcesosActivos() + "/" + todosProcesos.sizeLista());
            System.out.println("Procesos suspendidos: " + procesosSuspendidos);
            System.out.println("Cambios de contexto: " + cambiosContexto);
            System.out.println("Operaciones E/S completadas: " + operacionesESCompletadas);
            System.out.println("Velocidad: " + Reloj.getCycleDurationMs() + "ms/ciclo");
            
            // Mostrar estado de procesos
            System.out.println("ESTADO DE PROCESOS:");
            for (int i = 0; i < todosProcesos.sizeLista(); i++) {
                Proceso p = (Proceso) todosProcesos.get(i);
                System.out.println("   - " + p.toString());
            }
            
            semaforoGlobal.release();
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            semaforoGlobal.release();
        }
    }
    
    /**
     * Cuenta procesos activos (no terminados)
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
     * Obtiene copia de los procesos (para la GUI)
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
    
    // METODOS DE CONSULTA
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
    
    public int getProcesosSuspendidos() {
        return procesosSuspendidos;
    }
    
    public String getEstadoSimulacion() {
        return simulacionActiva ? "Ejecutandose" : "Detenida";
    }
    
    public Proceso getProcesoEjecutandoActual() {
        return procesoEjecutandoActual;
    }
    
    /**
     * Obtiene informacion de semaforos
     */
    public String getInfoSemaforos() {
        return String.format("Global: %d/1, E/S: %d/3, Planificador: %d/1",
            semaforoGlobal.availablePermits(),
            semaforoES.availablePermits(),
            semaforoPlanificador.availablePermits());
    }
    
    /**
     * Obtiene estadisticas completas de la simulacion
     */
    public String getEstadisticasCompletas() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== ESTADISTICAS SIMULACION ===\n");
        sb.append("Ciclos totales: ").append(ciclosTotales).append("\n");
        sb.append("Cambios de contexto: ").append(cambiosContexto).append("\n");
        sb.append("Operaciones E/S: ").append(operacionesESCompletadas).append("\n");
        sb.append("Procesos suspendidos: ").append(procesosSuspendidos).append("\n");
        sb.append("Procesos activos: ").append(contarProcesosActivos()).append("/").append(todosProcesos.sizeLista()).append("\n");
        sb.append("Algoritmo: ").append(planificador.getNombreAlgoritmo()).append("\n");
        
        return sb.toString();
    }
}