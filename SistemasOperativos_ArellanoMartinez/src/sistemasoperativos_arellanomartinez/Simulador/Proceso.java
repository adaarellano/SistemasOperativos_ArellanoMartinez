/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemasoperativos_arellanomartinez.Simulador;

import java.util.concurrent.Semaphore;

public class Proceso {
    private static int nextId = 1;
    
    // IDENTIFICACIÓN
    private String id;
    private String name;
    
    // EJECUCIÓN
    private int totalInstructions;
    private int pc; // Program Counter
    private Estado state;
    private Thread hiloEjecucion;
    private Thread hiloES;
    private volatile boolean ejecutando;
    
    // CONTROL DE EJECUCIÓN - SIMPLIFICADO
    private final Semaphore semaforoControl; // Control principal
    private volatile boolean pausado;
    
    // CAMPOS PARA SUSPENSIÓN
    private boolean suspendido;
    private Estado estadoAntesSuspension;
    
    // TIPO DE PROCESO
    private boolean isCpuBound;
    
    // GESTIÓN DE E/S
    private int ciclosExcepcionES;
    private int duracionES;
    private int tiempoESRestante;
    private int proximaExcepcionES;
    
    // MÉTRICAS DE PLANIFICACIÓN
    private int tiempoLlegada;
    private int tiempoInicioEjecucion;
    private int tiempoFinalizacion;
    private int tiempoEjecucionTotal;
    
    public enum Estado {
        NUEVO, LISTO, EJECUTANDO, BLOQUEADO, 
        SUS_LISTO, SUS_BLOQUEADO, TERMINADO
    }
    
    public Proceso(String name, int totalInstructions, boolean isCpuBound, 
                  int ciclosParaExcepcionES, int duracionES, int tiempoLlegada) {
        this.id = "P" + nextId++;
        this.name = name;
        this.totalInstructions = totalInstructions;
        this.pc = 0;
        this.state = Estado.NUEVO;
        this.isCpuBound = isCpuBound;
        this.ciclosExcepcionES = ciclosParaExcepcionES;
        this.duracionES = duracionES;
        this.tiempoESRestante = 0;
        this.proximaExcepcionES = ciclosParaExcepcionES;
        this.tiempoLlegada = tiempoLlegada;
        this.tiempoInicioEjecucion = -1;
        this.tiempoFinalizacion = -1;
        this.tiempoEjecucionTotal = 0;
        this.ejecutando = false;
        this.pausado = true; // Iniciar pausado
        
        // SEMÁFORO SIMPLIFICADO
        this.semaforoControl = new Semaphore(0);
        
        // INICIALIZAR CAMPOS DE SUSPENSIÓN
        this.suspendido = false;
        this.estadoAntesSuspension = null;
        
        // CREAR HILOS
        crearHilos();
    }
    
    /**
     * Hilo principal de ejecución - VERSIÓN SIMPLIFICADA
     */
    private void crearHilos() {
        // Hilo principal de ejecución
        hiloEjecucion = new Thread(() -> {
            System.out.println("Hilo ejecución creado para: " + name);
            
            while (!Thread.currentThread().isInterrupted() && !isFinished()) {
                try {
                    // Esperar permiso del Engine
                    semaforoControl.acquire();
                    
                    // Verificar si realmente puede ejecutar
                    if (state == Estado.EJECUTANDO && !estaEnES() && !suspendido && !pausado) {
                        ejecutarCiclo();
                    }
                    
                } catch (InterruptedException e) {
                    System.out.println("Hilo ejecución interrumpido: " + name);
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    System.err.println("Error en hilo ejecución " + name + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
            System.out.println("Hilo ejecución FINALIZADO: " + name);
        });
        
        hiloEjecucion.setName("Hilo-Ejecucion-" + name);
        
        // Hilo E/S
        hiloES = new Thread(() -> {
            System.out.println("Hilo E/S creado para: " + name);
            
            while (!Thread.currentThread().isInterrupted() && !isFinished()) {
                try {
                    Thread.sleep(Reloj.getCycleDurationMs());
                    
                    if (estaEnES() && !suspendido) {
                        procesarCicloES();
                    }
                    
                } catch (InterruptedException e) {
                    System.out.println("Hilo E/S interrumpido: " + name);
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    System.err.println("Error en hilo E/S " + name + ": " + e.getMessage());
                }
            }
            System.out.println("Hilo E/S FINALIZADO: " + name);
        });
        
        hiloES.setName("Hilo-ES-" + name);
    }
    
    /**
     * Ejecuta un ciclo de CPU
     */
    private void ejecutarCiclo() {
        if (pc < totalInstructions && !estaEnES() && !suspendido && !pausado) {
            // Ejecutar instrucción
            pc++;
            tiempoEjecucionTotal++;
            
            System.out.println(name + " ejecutó instrucción " + pc + "/" + totalInstructions);
            
            // Verificar si debe generar E/S
            if (debeGenerarES() && !estaEnES()) {
                generarES();
            }
            
            // Verificar si terminó
            if (pc >= totalInstructions) {
                state = Estado.TERMINADO;
                tiempoFinalizacion = Reloj.getCurrentCycle();
                System.out.println(name + " TERMINADO - PC: " + pc + "/" + totalInstructions);
                detenerCompletamente();
            }
            
            try {
                Thread.sleep(Reloj.getCycleDurationMs() / 2);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    // MÉTODOS DE CONTROL DEL ENGINE
    
    /**
     * El Engine da permiso para ejecutar UN ciclo
     */
    public void permitirEjecutarCiclo() {
        if (!pausado && !suspendido && state == Estado.EJECUTANDO && !estaEnES()) {
            semaforoControl.release(); // Permitir UN ciclo
        }
    }
    
    /**
     * Inicia los hilos (pero pausados)
     */
    public void iniciarEjecucion() {
        if (hiloEjecucion.getState() == Thread.State.NEW) {
            hiloEjecucion.start();
        }
        if (hiloES.getState() == Thread.State.NEW) {
            hiloES.start();
        }
        
        ejecutando = true;
        state = Estado.EJECUTANDO;
        pausado = false;
        
        System.out.println("Proceso INICIADO: " + name);
    }
    
    /**
     * Pausa la ejecución
     */
    public void pausarEjecucion() {
        pausado = true;
        if (state == Estado.EJECUTANDO) {
            state = Estado.LISTO;
        }
        System.out.println("Proceso PAUSADO: " + name);
    }
    
    /**
     * Reanuda la ejecución
     */
    public void reanudarEjecucion() {
        pausado = false;
        state = Estado.EJECUTANDO;
        System.out.println("Proceso REANUDADO: " + name);
    }
    
    /**
     * Detiene completamente el proceso
     */
    public void detenerCompletamente() {
        ejecutando = false;
        pausado = true;
        
        if (hiloEjecucion != null && hiloEjecucion.isAlive()) {
            hiloEjecucion.interrupt();
        }
        if (hiloES != null && hiloES.isAlive()) {
            hiloES.interrupt();
        }
        
        if (state != Estado.TERMINADO) {
            state = Estado.LISTO;
        }
        
        System.out.println("Proceso DETENIDO: " + name);
    }
    
    // MÉTODOS DE SUSPENSIÓN (mantener igual)
    public void suspender() {
        if (!suspendido && state != Estado.TERMINADO) {
            estadoAntesSuspension = state;
            state = (state == Estado.BLOQUEADO) ? Estado.SUS_BLOQUEADO : Estado.SUS_LISTO;
            suspendido = true;
            pausado = true;
            System.out.println(name + " SUSPENDIDO");
        }
    }
    
    public void reanudar() {
        if (suspendido && estadoAntesSuspension != null) {
            state = estadoAntesSuspension;
            suspendido = false;
            estadoAntesSuspension = null;
            pausado = false;
            System.out.println(name + " REANUDADO");
        }
    }
    
    public boolean isSuspendido() {
        return suspendido;
    }
    
    // MÉTODOS DE E/S (mantener igual)
    public boolean debeGenerarES() {
        if (isCpuBound) return false;
        return pc >= proximaExcepcionES;
    }
    
    public void generarES() {
        this.tiempoESRestante = duracionES;
        this.proximaExcepcionES = pc + ciclosExcepcionES;
        this.state = Estado.BLOQUEADO;
        this.pausado = true;
        System.out.println(name + " GENERA E/S - Bloqueado por " + tiempoESRestante + " ciclos");
    }
    
    public void procesarCicloES() {
        if (tiempoESRestante > 0) {
            tiempoESRestante--;
            if (tiempoESRestante == 0) {
                state = Estado.LISTO;
                pausado = false;
                System.out.println(name + " COMPLETÓ E/S - Estado: LISTO");
            }
        }
    }
    
    public boolean estaEnES() {
        return tiempoESRestante > 0;
    }
    
    // GETTERS Y SETTERS (simplificados - sin sincronización excesiva)
    public String getId() { return id; }
    public String getName() { return name; }
    public int getTotalInstructions() { return totalInstructions; }
    public int getPc() { return pc; }
    public Estado getState() { return state; }
    public void setState(Estado state) { this.state = state; }
    public boolean isCpuBound() { return isCpuBound; }
    public int getTiempoESRestante() { return tiempoESRestante; }
    public boolean isFinished() { return pc >= totalInstructions; }
    public int getInstruccionesRestantes() { return totalInstructions - pc; }
    public boolean isEjecutando() { return ejecutando && !pausado && !suspendido; }
    public boolean isPausado() { return pausado; }
    
    // Setters y Getters para métricas
    public void setTiempoInicioEjecucion(int tiempo) { this.tiempoInicioEjecucion = tiempo; }
    public void setTiempoFinalizacion(int tiempo) { this.tiempoFinalizacion = tiempo; }
    public void setTiempoLlegada(int tiempoLlegada) { this.tiempoLlegada = tiempoLlegada; }
    
    public int getTiempoInicioEjecucion() { return tiempoInicioEjecucion; }
    public int getTiempoFinalizacion() { return tiempoFinalizacion; }
    public int getTiempoLlegada() { return tiempoLlegada; }
    public int getProximaExcepcionES() { return proximaExcepcionES; }
    public int getDuracionES() { return duracionES; }
    
    // Métricas
    public int getTiempoEspera() {
        if (tiempoInicioEjecucion == -1) return 0;
        return tiempoInicioEjecucion - tiempoLlegada;
    }
    
    public int getTiempoRetorno() {
        if (tiempoFinalizacion == -1) return 0;
        return tiempoFinalizacion - tiempoLlegada;
    }
    
    public int getTiempoEjecucionTotal() {
        return tiempoEjecucionTotal;
    }
    
    @Override
    public String toString() {
        String estadoCompleto = state.toString();
        if (suspendido) estadoCompleto += " [SUSPENDIDO]";
        if (pausado) estadoCompleto += " [PAUSADO]";
        
        return String.format("%s - %s (PC: %d/%d) [%s] [E/S: %s]", 
            id, name, pc, totalInstructions, estadoCompleto, 
            estaEnES() ? tiempoESRestante + " ciclos" : "No");
    }
}