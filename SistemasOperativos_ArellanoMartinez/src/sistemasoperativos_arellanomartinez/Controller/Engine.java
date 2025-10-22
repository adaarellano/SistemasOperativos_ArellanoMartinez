/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemasoperativos_arellanomartinez.Controller;

import edd.ListaSimple;
import sistemasoperativos_arellanomartinez.Planificador.Planificador;
import sistemasoperativos_arellanomartinez.Simulador.Proceso;
import sistemasoperativos_arellanomartinez.Simulador.Reloj;
import sistemasoperativos_arellanomartinez.view.ConsolaGamer; 
import java.awt.Color;
import javax.swing.SwingUtilities;
import java.util.concurrent.Semaphore;

/**
 * @author Ada y Day
 * Motor principal de simulacion con coordinacion mejorada
 */
public class Engine {
    // COMPONENTES QUE COORDINA
    private Planificador planificador;
    private ListaSimple todosProcesos;
    private volatile boolean simulacionActiva;
    private Thread hiloSimulacion;
    private Proceso procesoEjecutandoActual;
    private ConsolaGamer consola;
    private volatile Planificador proximoPlanificador = null; // <-- AÑADE ESTA LÍNEA
    
    // SEMAFOROS MEJORADOS
    private final Semaphore semaforoGlobal;    // Exclusion mutua global
    private final Semaphore semaforoES;        // Control de E/S simultaneas
    private final Semaphore semaforoPlanificador; // Proteccion del planificador
    
    // METRICAS GLOBALES
    private int ciclosTotales;
    private int cambiosContexto;
    private int operacionesESCompletadas;
    private int procesosSuspendidos;
    private int procesosCompletados;
    private int ciclosCpuOcupado;
    private ListaSimple procesosTerminados; // Para guardar procesos finalizados y calcular promedios
    
    public Engine(Planificador planificador, ConsolaGamer consola) {
        this.planificador = planificador;
        this.consola = consola;
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
        
        // INICIALIZAR NUEVAS MÉTRICAS
        this.procesosCompletados = 0;
        this.ciclosCpuOcupado = 0;
        this.procesosTerminados = new ListaSimple();
        
        log("Engine creado con coordinación mejorada", Color.CYAN);
    }
    
    /**
     * Inicia la simulacion en un hilo separado
     */
    public void iniciarSimulacion() {
        if (simulacionActiva) {
            log("La simulacion ya esta activa", Color.ORANGE);
            return;
        }
        
        simulacionActiva = true;
        hiloSimulacion = new Thread(this::ejecutarCicloSimulacion);
        hiloSimulacion.setName("Engine-Simulation-Thread");
        hiloSimulacion.start();
        
        log("Engine iniciado con coordinacion mejorada", Color.ORANGE);
    }
    
    /**
     * Ciclo principal de simulacion con coordinacion mejorada
     */
    private void ejecutarCicloSimulacion() {
        log("Hilo de simulación iniciado.", new Color(150, 150, 150));
        while (simulacionActiva && contarProcesosActivos() > 0) {
            try {
                semaforoGlobal.acquire();
                
                // Revisamos si la MainGUI nos dejó una "nota" para cambiar el planificador
                if (proximoPlanificador != null) {
                    realizarCambioDePlanificador(proximoPlanificador); // Ejecutamos la lógica que escribiste
                    proximoPlanificador = null; // Limpiamos la nota
                }

                // 1. Avanzar tiempo
                Reloj.tick();
                ciclosTotales++;
                log("Ciclo: " + Reloj.getCurrentCycle(), new Color(150, 150, 150));
                
                // 2. Contabilizar uso de CPU
                if (procesoEjecutandoActual != null) {
                    ciclosCpuOcupado++;
                }
                
                Proceso procesoAntesDePlanificar = procesoEjecutandoActual;

                // 3. Ejecutar planificador
                ejecutarPlanificadorConControl();
                
                // 4. Contabilizar procesos que acaban de terminar
                if (procesoAntesDePlanificar != null && procesoAntesDePlanificar.isFinished() && !procesoYaContabilizado(procesoAntesDePlanificar)) {
                    this.procesosCompletados++;
                    this.procesosTerminados.insertFinal(procesoAntesDePlanificar);

                    log("Proceso '" + procesoAntesDePlanificar.getName() + "' completado.", Color.MAGENTA);

                    log("📊 Proceso '" + procesoAntesDePlanificar.getName() + "' ha completado su ejecución.", Color.MAGENTA);

                }
                
                // 5. Manejar operaciones de E/S
                manejarOperacionesES();

                semaforoGlobal.release();
                
                // 6. Esperar para el siguiente ciclo
                Thread.sleep(Reloj.getCycleDurationMs());

            } catch (InterruptedException e) {
                log("Hilo de simulación interrumpido.", Color.ORANGE);
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log("❌ Error crítico en Engine: " + e.getMessage(), Color.RED);
                e.printStackTrace();
            }
        }
        simulacionActiva = false; // Asegura que el bucle de la GUI también termine
        log("Todos los procesos han terminado o la simulación fue detenida.", new Color(150, 150, 150));
    }
    
    private boolean procesoYaContabilizado(Proceso p) {
        for (int i = 0; i < procesosTerminados.sizeLista(); i++) {
            if (procesosTerminados.get(i) == p) {
                return true;
            }
        }
        return false;
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
                    
                    log("CPU asignado a: " + procesoEjecutandoActual.getName(), Color.GREEN);
                } else {
                    log("CPU LIBRE - No hay procesos para ejecutar.", Color.GRAY);
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
        } finally {
            if (semaforoPlanificador.availablePermits() == 0) {
                 semaforoPlanificador.release();
            }
        }
    }
    
    /**
     * Asegura que solo el proceso actual tenga permiso de ejecucion
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
                            
                            log(proceso.getName() + " volvio de E/S", Color.GRAY);
                        }
                    }
                }
                
                // LIBERAR SEMAFORO E/S
                semaforoES.release();
                
                if (procesosEnES > 0) {
                    log("E/S: " + procesosEnES + " procesos en E/S, " + 
                                     procesosEnESCompletados + " completados", Color.GRAY);
                }
                
            } else {
                // No se pudo adquirir semaforo E/S - demasiadas E/S simultaneas
                log("Limite de E/S alcanzado (" + 
                                 semaforoES.availablePermits() + "/3 disponibles)", Color.GRAY);
            }
            
        } catch (Exception e) {
            log("Error en manejo E/S: " + e.getMessage(), Color.GRAY);
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
            todosProcesos.insertFinal(proceso);
            planificador.agregarProceso(proceso);
            proceso.iniciarEjecucion();
        } catch (InterruptedException e) {
            log("Interrupción al agregar proceso", Color.RED);
        } finally {
            semaforoGlobal.release();
            log("📥 Proceso agregado: " + proceso.getName());
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
            
            log(suspendidos + " procesos suspendidos", Color.GRAY);
            
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
        
        log("Pausando simulacion...", Color.GRAY);
        
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
        
        log("Simulacion pausada (" + procesosPausados + " procesos pausados)", Color.ORANGE);
    }
    
    /**
     * Reanuda la simulacion
     */
    public void reanudarSimulacion() {
        log("Reanudando simulacion...", Color.ORANGE);
        simulacionActiva = true;
        iniciarSimulacion();
    }
    
    /**
     * Detiene completamente la simulacion
     */
    public void detenerSimulacion() {
        log("Deteniendo simulación...", Color.ORANGE);
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
            
            log("\n=== CICLO " + ciclosTotales + " ===", Color.WHITE);
            log("Algoritmo: " + planificador.getNombreAlgoritmo(), Color.WHITE);
            log("CPU: " + (procesoEjecutandoActual != null ? procesoEjecutandoActual.getName() : "LIBRE"), Color.WHITE);
            log("Procesos activos: " + contarProcesosActivos() + "/" + todosProcesos.sizeLista(),Color.WHITE);
            log("Procesos suspendidos: " + procesosSuspendidos, Color.WHITE);
            log("Cambios de contexto: " + cambiosContexto, Color.WHITE);
            log("Operaciones E/S completadas: " + operacionesESCompletadas, Color.WHITE);
            log("Velocidad: " + Reloj.getCycleDurationMs() + "ms/ciclo", Color.WHITE);
            
            // Mostrar estado de procesos
            log("ESTADO DE PROCESOS:", Color.WHITE);
            for (int i = 0; i < todosProcesos.sizeLista(); i++) {
                Proceso p = (Proceso) todosProcesos.get(i);
                log("   - " + p.toString(), Color.WHITE);
            }
            
            semaforoGlobal.release();
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            semaforoGlobal.release();
        }
    }
    
    public int contarProcesosActivos() {
       int activos = 0;
       try {
           semaforoGlobal.acquire();

           for (int i = 0; i < todosProcesos.sizeLista(); i++) {
               Proceso p = (Proceso) todosProcesos.get(i);
               if (p != null && !p.isFinished()) {
                   // 🔥 SOLUCIÓN DEFINITIVA: Solo verificar si NO está terminado
                   // Los procesos suspendidos se consideran activos porque pueden reanudarse
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
    
        /**
     * Calcula el throughput del sistema (procesos completados por ciclo).
     */
    public double getThroughput() {
        if (ciclosTotales == 0) return 0.0;
        return (double) this.procesosCompletados / this.ciclosTotales;
    }

    /**
     * Calcula el porcentaje de utilización de la CPU.
     */
    public double getUtilizacionCPU() {
        if (ciclosTotales == 0) return 0.0;
        return ((double) this.ciclosCpuOcupado / this.ciclosTotales) * 100.0;
    }

    /**
     * Calcula el tiempo de retorno (Turnaround Time) promedio de los procesos terminados.
     * Tiempo de Retorno = Tiempo de Finalización - Tiempo de Llegada
     */
    public double getTiempoRetornoPromedio() {
        if (procesosTerminados.sizeLista() == 0) return 0.0;

        int sumaTiemposRetorno = 0;
        for (int i = 0; i < procesosTerminados.sizeLista(); i++) {
            Proceso p = (Proceso) procesosTerminados.get(i);
            sumaTiemposRetorno += p.getTiempoRetorno(); // Necesitas este método en Proceso.java
        }
        return (double) sumaTiemposRetorno / procesosTerminados.sizeLista();
    }

    /**
     * Calcula el tiempo de espera (Waiting Time) promedio de los procesos terminados.
     * Tiempo de Espera = Tiempo de Inicio de Ejecución - Tiempo de Llegada
     */
    public double getTiempoEsperaPromedio() {
        if (procesosTerminados.sizeLista() == 0) return 0.0;

        int sumaTiemposEspera = 0;
        for (int i = 0; i < procesosTerminados.sizeLista(); i++) {
            Proceso p = (Proceso) procesosTerminados.get(i);
            sumaTiemposEspera += p.getTiempoEspera(); // Necesitas este método en Proceso.java
        }
        return (double) sumaTiemposEspera / procesosTerminados.sizeLista();
    }
    
    private void log(String mensaje, Color color) {
        if (this.consola != null) {
            SwingUtilities.invokeLater(() -> consola.agregarLinea(mensaje, color));
        }
        System.out.println(mensaje); // Mantenemos la salida estándar para depuración
    }

    private void log(String mensaje) {
        // Esta versión usa un color por defecto (Blanco) cuando no se especifica uno.
        log(mensaje, Color.WHITE);
    }
    
        /**
     * Devuelve una nueva ListaSimple que contiene solo los procesos en estado LISTO.
     */
    public ListaSimple getProcesosListos() {
        ListaSimple listos = new ListaSimple();
        // Usamos semáforo para garantizar que la lista no se modifique mientras la leemos
        try {
            semaforoGlobal.acquire();
            for (int i = 0; i < todosProcesos.sizeLista(); i++) {
                Proceso p = (Proceso) todosProcesos.get(i);
                if (p.getState() == Proceso.Estado.LISTO) {
                    listos.insertFinal(p);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            semaforoGlobal.release();
        }
        return listos;
    }

    /**
     * Devuelve una nueva ListaSimple que contiene solo los procesos en estado BLOQUEADO.
     */
    public ListaSimple getProcesosBloqueados() {
        ListaSimple bloqueados = new ListaSimple();
        try {
            semaforoGlobal.acquire();
            for (int i = 0; i < todosProcesos.sizeLista(); i++) {
                Proceso p = (Proceso) todosProcesos.get(i);
                if (p.getState() == Proceso.Estado.BLOQUEADO) {
                    bloqueados.insertFinal(p);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            semaforoGlobal.release();
        }
        return bloqueados;
    }
    /**
     * Cambia el planificador actual por uno nuevo en tiempo real.
     * Transfiere todos los procesos activos al nuevo planificador.
     
    public void setPlanificador(Planificador nuevoPlanificador) {
        try {
            // Bloqueamos todo para hacer el cambio de forma segura
            semaforoGlobal.acquire();

            log("🔄 Cambiando planificador a: " + nuevoPlanificador.getNombreAlgoritmo(), Color.ORANGE);

            // 1. Crear una lista temporal con todos los procesos que no han terminado.
            ListaSimple procesosActivos = new ListaSimple();
            for (int i = 0; i < todosProcesos.sizeLista(); i++) {
                Proceso p = (Proceso) todosProcesos.get(i);
                if (!p.isFinished()) {
                    // Devolvemos los procesos a un estado neutral "LISTO"
                    p.setState(Proceso.Estado.LISTO);
                    procesosActivos.insertFinal(p);
                }
            }

            // 2. Detenemos el proceso que se estaba ejecutando (si lo había)
            if (procesoEjecutandoActual != null) {
                procesoEjecutandoActual.pausarEjecucion();
                procesoEjecutandoActual = null;
            }

            // 3. Reemplazamos el planificador
            this.planificador = nuevoPlanificador;

            // 4. Agregamos todos los procesos activos al nuevo planificador
            for (int i = 0; i < procesosActivos.sizeLista(); i++) {
                this.planificador.agregarProceso((Proceso) procesosActivos.get(i));
            }

            cambiosContexto++; // El cambio de planificador cuenta como un cambio de contexto

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            if (semaforoGlobal.availablePermits() == 0) {
                semaforoGlobal.release();
            }
        }
    }*/
    
    //Aquí cambiamos la lógica del cambio del planificador para que sus hilos no choquen al momento de cambiar de planiicador 
    private void realizarCambioDePlanificador(Planificador nuevoPlanificador) {
        // No necesita su propio try-catch-finally porque será llamado desde un lugar seguro
        log("🔄 Cambiando planificador a: " + nuevoPlanificador.getNombreAlgoritmo(), Color.ORANGE);

        // 1. Crear una lista temporal... (todo tu código actual va aquí)
        ListaSimple procesosActivos = new ListaSimple();
        for (int i = 0; i < todosProcesos.sizeLista(); i++) {
            Proceso p = (Proceso) todosProcesos.get(i);
            if (!p.isFinished()) {
                p.setState(Proceso.Estado.LISTO);
                procesosActivos.insertFinal(p);
            }
        }

        // 2. Detenemos el proceso...
        if (procesoEjecutandoActual != null) {
            procesoEjecutandoActual.pausarEjecucion();
            procesoEjecutandoActual = null;
        }

        // 3. Reemplazamos el planificador...
        this.planificador = nuevoPlanificador;

        // 4. Agregamos todos los procesos activos al nuevo planificador...
        for (int i = 0; i < procesosActivos.sizeLista(); i++) {
            this.planificador.agregarProceso((Proceso) procesosActivos.get(i));
        }

        cambiosContexto++;
    }
    
    public void setPlanificador(Planificador nuevoPlanificador) {
        this.proximoPlanificador = nuevoPlanificador;
        }

    public Planificador getPlanificador() {
    return this.planificador;
    }
    }