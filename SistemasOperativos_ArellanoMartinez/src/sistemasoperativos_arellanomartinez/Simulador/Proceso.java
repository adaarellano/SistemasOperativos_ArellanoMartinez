/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemasoperativos_arellanomartinez.Simulador;
import java.util.concurrent.Semaphore;

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
    private Thread hiloES; // 🧵 HILO SEPARADO PARA E/S
    private volatile boolean ejecutando;
    private final Semaphore semaforo; // 🔐 SEMÁFORO PARA EXCLUSIÓN MÚTUA
    
    // 🔹 TIPO DE PROCESO
    private boolean isCpuBound;
    
    // 🔹 GESTIÓN DE E/S (I/O Management)
    private int ciclosExcepcionES;
    private int duracionES; // 🆕 DURACIÓN DE E/S
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
        this.duracionES = duracionES; // 🆕 GUARDAR DURACIÓN
        this.tiempoESRestante = 0;
        this.proximaExcepcionES = ciclosParaExcepcionES;
        this.tiempoLlegada = tiempoLlegada;
        this.tiempoInicioEjecucion = -1;
        this.tiempoFinalizacion = -1;
        this.tiempoEjecucionTotal = 0;
        this.ejecutando = false;
        this.semaforo = new Semaphore(1); // 🔐 INICIALIZAR SEMÁFORO
        
        // 🧵 CREAR AMBOS HILOS
        crearHilos();
    }
    
    /**
     * 🧵 Crea ambos hilos: ejecución y E/S separados
     */
    private void crearHilos() {
        // Hilo principal de ejecución
        hiloEjecucion = new Thread(() -> {
            System.out.println("🧵 Hilo ejecución creado para: " + name);
            
            while (!Thread.currentThread().isInterrupted() && !isFinished()) {
                try {
                    semaforo.acquire(); // 🔐 ADQUIRIR SEMÁFORO
                    
                    synchronized (lock) {
                        while (pausado && !Thread.currentThread().isInterrupted()) {
                            lock.wait();
                        }
                    }
                    
                    if (ejecutando && state == Estado.EJECUTANDO && !estaEnES()) {
                        ejecutarCiclo();
                    }
                    
                    semaforo.release(); // 🔐 LIBERAR SEMÁFORO
                    
                    Thread.sleep(50);
                    
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    semaforo.release(); // 🔐 LIBERAR EN INTERRUPCIÓN
                    break;
                }
            }
            System.out.println("🧵 Hilo ejecución terminado: " + name);
        });
        
        hiloEjecucion.setName("Hilo-Ejecucion-" + name);
        
        // 🧵 HILO SEPARADO PARA E/S
        hiloES = new Thread(() -> {
            System.out.println("🧵 Hilo E/S creado para: " + name);
            
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    synchronized (lock) {
                        // Esperar hasta que haya E/S para procesar
                        while (!estaEnES() && !Thread.currentThread().isInterrupted()) {
                            lock.wait();
                        }
                    }
                    
                    // 🎯 PROCESAR E/S EN HILO SEPARADO
                    if (estaEnES() && !Thread.currentThread().isInterrupted()) {
                        procesarESEnHiloSeparado();
                    }
                    
                    Thread.sleep(50);
                    
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            System.out.println("🧵 Hilo E/S terminado: " + name);
        });
        
        hiloES.setName("Hilo-ES-" + name);
    }
    
    /**
     * 🔄 Ejecuta un ciclo completo del proceso
     */
    private void ejecutarCiclo() {
        if (pc < totalInstructions && !estaEnES()) {
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
                System.out.println("🎉 " + name + " terminó en hilo ejecución");
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
     * 🎯 Procesa E/S en hilo separado
     */
    private void procesarESEnHiloSeparado() {
        try {
            semaforo.acquire(); // 🔐 ADQUIRIR SEMÁFORO
            
            if (tiempoESRestante > 0) {
                System.out.println("🔄 " + name + " procesando E/S en hilo separado (" + 
                                 tiempoESRestante + " ciclos restantes)");
                
                // Simular tiempo de E/S
                Thread.sleep(Reloj.getCycleDurationMs());
                
                tiempoESRestante--;
                
                if (tiempoESRestante == 0) {
                    // ✅ E/S COMPLETADA - VOLVER AL PROCESADOR ORIGINAL
                    state = Estado.LISTO;
                    System.out.println("✅ " + name + " E/S completada - Volviendo al CPU original");
                    
                    // Notificar que está listo para ejecutar nuevamente
                    synchronized (lock) {
                        lock.notifyAll();
                    }
                }
            }
            
            semaforo.release(); // 🔐 LIBERAR SEMÁFORO
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            semaforo.release(); // 🔐 LIBERAR EN INTERRUPCIÓN
        }
    }
    
    /**
     * ▶️ Inicia la ejecución de ambos hilos
     */
    public void iniciarEjecucion() {
        try {
            semaforo.acquire(); // 🔐 ADQUIRIR SEMÁFORO
            
            if (hiloEjecucion.getState() == Thread.State.NEW) {
                hiloEjecucion.start();
            }
            if (hiloES.getState() == Thread.State.NEW) {
                hiloES.start(); // 🧵 INICIAR HILO E/S
            }
            
            ejecutando = true;
            pausado = false;
            state = Estado.EJECUTANDO;
            
            synchronized (lock) {
                lock.notifyAll();
            }
            
            if (tiempoInicioEjecucion == -1) {
                tiempoInicioEjecucion = Reloj.getCurrentCycle();
            }
            
            semaforo.release(); // 🔐 LIBERAR SEMÁFORO
            
            System.out.println("▶️  Iniciando ejecución de " + name + " (ambos hilos)");
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * ⏸️ Pausa la ejecución del proceso
     */
    public void pausarEjecucion() {
        try {
            semaforo.acquire(); // 🔐 ADQUIRIR SEMÁFORO
            
            ejecutando = false;
            pausado = true;
            if (state == Estado.EJECUTANDO) {
                state = Estado.LISTO;
            }
            
            semaforo.release(); // 🔐 LIBERAR SEMÁFORO
            
            System.out.println("⏸️  Pausando ejecución de " + name);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * 🛑 Detiene completamente ambos hilos
     */
    public void detenerEjecucion() {
        try {
            semaforo.acquire(); // 🔐 ADQUIRIR SEMÁFORO
            
            ejecutando = false;
            pausado = false;
            if (state != Estado.TERMINADO) {
                state = Estado.LISTO;
            }
            
            semaforo.release(); // 🔐 LIBERAR SEMÁFORO
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Detener ambos hilos
        if (hiloEjecucion != null && hiloEjecucion.isAlive()) {
            hiloEjecucion.interrupt();
        }
        if (hiloES != null && hiloES.isAlive()) {
            hiloES.interrupt(); // 🧵 DETENER HILO E/S
        }
        
        System.out.println("🛑 Ambos hilos detenidos para: " + name);
    }
    
    /**
     * 🔁 Reanuda la ejecución del proceso
     */
    public void reanudarEjecucion() {
        try {
            semaforo.acquire(); // 🔐 ADQUIRIR SEMÁFORO
            
            ejecutando = true;
            pausado = false;
            state = Estado.EJECUTANDO;
            
            synchronized (lock) {
                lock.notifyAll();
            }
            
            semaforo.release(); // 🔐 LIBERAR SEMÁFORO
            
            System.out.println("🔁 Reanudando ejecución de " + name);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    // 🔹 MÉTODOS DE E/S MEJORADOS
    
    public boolean debeGenerarES() {
        if (isCpuBound) return false;
        return pc >= proximaExcepcionES;
    }
    
    /**
     * 🚨 Genera E/S y activa el hilo E/S separado
     */
    public void generarES() {
        try {
            semaforo.acquire(); // 🔐 ADQUIRIR SEMÁFORO
            
            this.tiempoESRestante = duracionES; // 🆕 USAR DURACIÓN ESPECÍFICA
            this.proximaExcepcionES = pc + ciclosExcepcionES;
            this.state = Estado.BLOQUEADO;
            this.ejecutando = false;
            
            // 🧵 NOTIFICAR AL HILO E/S PARA QUE PROCESE
            synchronized (lock) {
                lock.notifyAll();
            }
            
            semaforo.release(); // 🔐 LIBERAR SEMÁFORO
            
            System.out.println("🚨 " + name + " genera E/S - Hilo E/S activado (" + 
                             tiempoESRestante + " ciclos)");
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * ⏳ Procesa un ciclo de E/S (para compatibilidad)
     */
    public void procesarCicloES() {
        // 🎯 AHORA LO HACE EL HILO E/S SEPARADO AUTOMÁTICAMENTE
        // Este método se mantiene para compatibilidad
        if (tiempoESRestante > 0) {
            tiempoESRestante--;
            if (tiempoESRestante == 0) {
                try {
                    semaforo.acquire();
                    state = Estado.LISTO;
                    semaforo.release();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                System.out.println("✅ " + name + " E/S completada");
            }
        }
    }
    
    public boolean estaEnES() {
        return tiempoESRestante > 0;
    }
    
    // 🔹 MÉTODOS DE MÉTRICAS
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
    
    // 🔹 GETTERS Y SETTERS
    public String getId() { return id; }
    public String getName() { return name; }
    public int getTotalInstructions() { return totalInstructions; }
    public int getPc() { return pc; }
    public void setPc(int pc) { 
        try {
            semaforo.acquire();
            this.pc = pc; 
            semaforo.release();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    public Estado getState() { return state; }
    public void setState(Estado state) { 
        try {
            semaforo.acquire();
            this.state = state; 
            semaforo.release();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    public boolean isCpuBound() { return isCpuBound; }
    public int getTiempoESRestante() { return tiempoESRestante; }
    public boolean isFinished() { return pc >= totalInstructions; }
    public int getInstruccionesRestantes() { return totalInstructions - pc; }
    
    // Setters para los campos
    public void setTiempoInicioEjecucion(int tiempo) { this.tiempoInicioEjecucion = tiempo; }
    public void setTiempoFinalizacion(int tiempo) { this.tiempoFinalizacion = tiempo; }
    public void setTiempoLlegada(int tiempoLlegada) { this.tiempoLlegada = tiempoLlegada; }
    
    // Getters para los campos
    public int getTiempoInicioEjecucion() { return tiempoInicioEjecucion; }
    public int getTiempoFinalizacion() { return tiempoFinalizacion; }
    public int getTiempoLlegada() { return tiempoLlegada; }
    public int getProximaExcepcionES() { return proximaExcepcionES; }
    public int getDuracionES() { return duracionES; } // 🆕 GETTER PARA DURACIÓN
    
    // Métodos para control del hilo
    public boolean isEjecutando() { return ejecutando; }
    public Thread getHiloEjecucion() { return hiloEjecucion; }
    public Thread getHiloES() { return hiloES; } // 🆕 GETTER HILO E/S
    public boolean isPausado() { return pausado; }
    
    /**
     * 📊 Obtiene información del estado de threads
     */
    public String getInfoThreads() {
        return String.format("%s - Ejecución: %s, E/S: %s", 
            name,
            (hiloEjecucion != null ? hiloEjecucion.getState() : "No creado"),
            (hiloES != null ? hiloES.getState() : "No creado"));
    }
    
    @Override
    public String toString() {
        return String.format("%s - %s (PC: %d/%d) [%s] [E/S: %s]", 
            id, name, pc, totalInstructions, state, 
            estaEnES() ? tiempoESRestante + " ciclos" : "No");
    }
}

