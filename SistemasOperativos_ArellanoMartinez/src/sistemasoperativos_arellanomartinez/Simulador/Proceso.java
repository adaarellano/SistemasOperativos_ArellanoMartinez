/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemasoperativos_arellanomartinez.Simulador;

/**
 * Clase que representa un proceso con capacidad de ejecución en hilos
 */
public class Proceso {
    private static int nextId = 1;
    
    // 🔹 IDENTIFICACIÓN
    private String id;
    private String name;
    
    // 🔹 EJECUCIÓN E HILOS
    private int totalInstructions;
    private int pc; // Program Counter
    private Estado state;
    private Thread hiloEjecucion;
    private volatile boolean ejecutando; // Control de ejecución del hilo
    
    // 🔹 TIPO DE PROCESO
    private boolean isCpuBound;
    
    // 🔹 GESTIÓN DE E/S (I/O Management)
    private int ciclosExcepcionES;
    private int tiempoESRestante;
    private int proximaExcepcionES;
    
    // 🔹 MÉTRICAS DE PLANIFICACIÓN
    private int tiempoLlegada;
    private int tiempoInicioEjecucion;
    private int tiempoFinalizacion;
    private int tiempoEjecucionTotal;
    
    // 🔹 SINCRONIZACIÓN
    private final Object lock = new Object();
    private boolean pausado = false;
    
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
        this.tiempoESRestante = 0;
        this.proximaExcepcionES = ciclosParaExcepcionES;
        this.tiempoLlegada = tiempoLlegada;
        this.tiempoInicioEjecucion = -1;
        this.tiempoFinalizacion = -1;
        this.tiempoEjecucionTotal = 0;
        this.ejecutando = false;
        
        // Crear el hilo del proceso
        crearHilo();
    }
    
    /**
     * Crea el hilo de ejecución para este proceso
     */
    private void crearHilo() {
        hiloEjecucion = new Thread(() -> {
            System.out.println("🧵 Hilo creado para proceso: " + name);
            
            while (!Thread.currentThread().isInterrupted() && !isFinished()) {
                synchronized (lock) {
                    while (pausado && !Thread.currentThread().isInterrupted()) {
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
                
                if (ejecutando && state == Estado.EJECUTANDO) {
                    ejecutarCiclo();
                }
                
                try {
                    Thread.sleep(50); // Pequeña pausa para no saturar la CPU
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            
            System.out.println("🧵 Hilo terminado para proceso: " + name);
        });
        
        hiloEjecucion.setName("Hilo-" + name);
    }
    
    /**
     * Ejecuta un ciclo completo del proceso
     */
    private void ejecutarCiclo() {
        if (pc < totalInstructions && state == Estado.EJECUTANDO) {
            // Ejecutar instrucción
            pc++;
            tiempoEjecucionTotal++;
            
            // Verificar si debe generar E/S
            if (debeGenerarES() && !estaEnES()) {
                generarES();
            }
            
            // Verificar si terminó
            if (pc >= totalInstructions) {
                state = Estado.TERMINADO;
                tiempoFinalizacion = Reloj.getCurrentCycle();
                detenerEjecucion();
                System.out.println("🎉 Proceso " + name + " terminó en hilo");
            }
            
            // Pequeña pausa para simular tiempo de ejecución
            try {
                Thread.sleep(Reloj.getCycleDurationMs());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    /**
     * Inicia la ejecución del proceso en su hilo
     */
    public void iniciarEjecucion() {
        if (hiloEjecucion.getState() == Thread.State.NEW) {
            hiloEjecucion.start();
        }
        
        synchronized (lock) {
            ejecutando = true;
            pausado = false;
            state = Estado.EJECUTANDO;
            lock.notifyAll();
        }
        
        if (tiempoInicioEjecucion == -1) {
            tiempoInicioEjecucion = Reloj.getCurrentCycle();
        }
        
        System.out.println("▶️  Iniciando ejecución de " + name + " en hilo");
    }
    
    /**
     * Pausa la ejecución del proceso
     */
    public void pausarEjecucion() {
        synchronized (lock) {
            ejecutando = false;
            pausado = true;
            if (state == Estado.EJECUTANDO) {
                state = Estado.LISTO;
            }
        }
        System.out.println("⏸️  Pausando ejecución de " + name);
    }
    
    /**
     * Detiene completamente la ejecución del proceso
     */
    public void detenerEjecucion() {
        synchronized (lock) {
            ejecutando = false;
            pausado = false;
            if (state != Estado.TERMINADO) {
                state = Estado.LISTO;
            }
        }
        
        if (hiloEjecucion != null && hiloEjecucion.isAlive()) {
            hiloEjecucion.interrupt();
        }
        System.out.println("⏹️  Deteniendo ejecución de " + name);
    }
    
    /**
     * Reanuda la ejecución del proceso
     */
    public void reanudarEjecucion() {
        synchronized (lock) {
            ejecutando = true;
            pausado = false;
            state = Estado.EJECUTANDO;
            lock.notifyAll();
        }
        System.out.println("🔁 Reanudando ejecución de " + name);
    }
    
    // 🔹 MÉTODOS DE E/S (se mantienen iguales)
    public boolean debeGenerarES() {
        if (isCpuBound) return false;
        return pc >= proximaExcepcionES;
    }
    
    public void generarES() {
        synchronized (lock) {
            this.tiempoESRestante = ciclosExcepcionES;
            this.proximaExcepcionES = pc + ciclosExcepcionES;
            this.state = Estado.BLOQUEADO;
            this.ejecutando = false;
        }
        System.out.println("🚨 " + name + " genera E/S - Bloqueado por " + tiempoESRestante + " ciclos");
    }
    
    public void procesarCicloES() {
        if (tiempoESRestante > 0) {
            tiempoESRestante--;
            if (tiempoESRestante == 0) {
                synchronized (lock) {
                    state = Estado.LISTO;
                }
                System.out.println("✅ " + name + "E/S completada");
            }
        }
    }
    
    public boolean estaEnES() {
        return tiempoESRestante > 0;
    }
    
    // 🔹 MÉTODOS DE MÉTRICAS (se mantienen iguales)
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
    
    // 🔹 GETTERS Y SETTERS (se mantienen iguales)
    public String getId() { return id; }
    public String getName() { return name; }
    public int getTotalInstructions() { return totalInstructions; }
    public int getPc() { return pc; }
    public void setPc(int pc) { this.pc = pc; }
    public Estado getState() { return state; }
    public void setState(Estado state) { 
        synchronized (lock) {
            this.state = state; 
        }
    }
    public boolean isCpuBound() { return isCpuBound; }
    public int getTiempoESRestante() { return tiempoESRestante; }
    public boolean isFinished() { return pc >= totalInstructions; }
    public int getInstruccionesRestantes() { return totalInstructions - pc; }
    
    // Setters para los nuevos campos
    public void setTiempoInicioEjecucion(int tiempo) { this.tiempoInicioEjecucion = tiempo; }
    public void setTiempoFinalizacion(int tiempo) { this.tiempoFinalizacion = tiempo; }
    public void setTiempoLlegada(int tiempoLlegada) { this.tiempoLlegada = tiempoLlegada; }
    
    // Getters para los nuevos campos
    public int getTiempoInicioEjecucion() { return tiempoInicioEjecucion; }
    public int getTiempoFinalizacion() { return tiempoFinalizacion; }
    public int getTiempoLlegada() { return tiempoLlegada; }
    public int getProximaExcepcionES() { return proximaExcepcionES; }
    
    // Métodos para control del hilo
    public boolean isEjecutando() { return ejecutando; }
    public Thread getHiloEjecucion() { return hiloEjecucion; }
    public boolean isPausado() { return pausado; }
    
    @Override
    public String toString() {
        return String.format("%s - %s (PC: %d/%d) [%s] [Hilo: %s]", 
            id, name, pc, totalInstructions, state, 
            (hiloEjecucion != null ? hiloEjecucion.getState() : "No creado"));
    }
}