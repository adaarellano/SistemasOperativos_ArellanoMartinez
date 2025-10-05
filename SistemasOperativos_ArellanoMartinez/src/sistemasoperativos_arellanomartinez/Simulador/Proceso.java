/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemasoperativos_arellanomartinez.Simulador;

/**
 *El proceso es la unidad fundamental de ejecución en un SO. Representa 
 * un programa en ejecución con su propio estado, recursos y contexto.
 * @author raiza
 */
public class Proceso {
private static int nextId = 1;
     // 🔹 ESTADO DEL PROCESO
    private String id;
    private String name;
    private int totalInstructions;
    private int pc; // Program Counter
    private Estado state;
    private boolean isCpuBound;
     // 🔹 GESTIÓN DE E/S (I/O Management)
    private int ciclosExcepcionES;      // Cada cuántos ciclos genera E/S
    private int tiempoESRestante;       // Cuántos ciclos queda bloqueado
    private int proximaExcepcionES;     // Cuándo ocurrirá la próxima E/S
    // 🔹 MÉTRICAS DE PLANIFICACIÓN (Performance Metrics)
    private int tiempoLlegada;          // Ciclo en que llegó al sistema
    private int tiempoInicioEjecucion;  // Ciclo en que empezó a ejecutarse
    private int tiempoFinalizacion;     // Ciclo en que terminó
    
    public enum Estado {
        NUEVO, LISTO, EJECUTANDO, BLOQUEADO, 
        SUS_LISTO, SUS_BLOQUEADO, TERMINADO
    }
    
    // CONSTRUCTOR CORREGIDO
    public Proceso(String name, int totalInstructions, boolean isCpuBound, 
                  int ciclosParaExcepcionES, int duracionES, int tiempoLlegada) {
        this.id = "P" + nextId++;
        this.name = name;
        this.totalInstructions = totalInstructions;
        this.pc = 0;
        this.state = Estado.NUEVO;
        this.isCpuBound = isCpuBound;
        this.ciclosExcepcionES = ciclosParaExcepcionES;
        this.tiempoESRestante = 0;  // Inicialmente no está en E/S
        this.proximaExcepcionES = ciclosParaExcepcionES; // Primera E/S después de X ciclos
        // 🔹 NUEVOS CAMPOS PARA MÉTRICAS
        this.tiempoLlegada = tiempoLlegada;
        this.tiempoInicioEjecucion = -1; // -1 = no ha empezado
        this.tiempoFinalizacion = -1;    // -1 = no ha terminado
    }   
        
    
    // 🔹 EJECUCIÓN DE INSTRUCCIONES
    public void ejecutarInstruccion() {
        if (pc < totalInstructions) {
            pc++;
            
            // Verificar si terminó
            if (pc >= totalInstructions) {
                state = Estado.TERMINADO;
                tiempoFinalizacion = Reloj.getCurrentCycle(); // ✅ Usa reloj real // Metodo de reloj
            }
        }
    }
    
    // 🔹 GESTIÓN DE E/S (I/O Management)
    public boolean debeGenerarES() {
        // Solo los I/O-bound generan E/S, y cuando llegan al ciclo programado
        if (isCpuBound) return false;
        return pc >= proximaExcepcionES;
    }
    
    public void generarES() {
        this.tiempoESRestante = getCiclosExcepcionES(); // Iniciar el tiempo de E/S
        this.proximaExcepcionES = pc + getCiclosExcepcionES(); // Programar próxima E/S
        this.state = Estado.BLOQUEADO;
    }
    
    public void procesarCicloES() {
        if (tiempoESRestante > 0) {
            tiempoESRestante--;
            if (tiempoESRestante == 0) {
                // E/S completada, volver a listo
                state = Estado.LISTO;
            }
        }
    }
    
    public boolean estaEnES() {
        return tiempoESRestante > 0;
    }
    
    // 🔹 MÉTRICAS DE RENDIMIENTO
    public int getTiempoEspera() {
        if (tiempoInicioEjecucion == -1) return 0;
        return tiempoInicioEjecucion - tiempoLlegada;
    }
    
     // Setters para los nuevos campos
    public void setTiempoInicioEjecucion(int tiempo) {
        this.tiempoInicioEjecucion = tiempo;
    }
    
    public void setTiempoFinalizacion(int tiempo) {
        this.tiempoFinalizacion = tiempo;
    }
    
    public void setTiempoLlegada(int tiempoLlegada) {
        this.tiempoLlegada = tiempoLlegada;
    }
    
    // 🔹 MÉTODOS PARA CÁLCULO DE MÉTRICAS
    public int getTiempoRetorno() {
        if (tiempoFinalizacion == -1) return 0;
        return tiempoFinalizacion - tiempoLlegada;
    }
    
    // Getter para tiempoLlegada
    public int getTiempoLlegada() {
        return tiempoLlegada;
    }
    
    // Getter para proximaExcepcionES
    public int getProximaExcepcionES() {
        return proximaExcepcionES;
    }
    
    // GETTERS 
    public String getId() { 
        return id; }
    public String getName() { 
        return name; }
    public int getTotalInstructions() { 
        return totalInstructions; }
    public int getPc() { 
        return pc; }
    public void setPc(int pc) { 
        this.pc = pc; }
    public Estado getState() { 
        return state; }
    public void setState(Estado state) { 
        this.state = state; }
    public boolean isCpuBound() { 
        return isCpuBound; }
     public int getTiempoESRestante() { 
         return tiempoESRestante; }
     public boolean isFinished() { 
         return pc >= totalInstructions; }
    public int getInstruccionesRestantes() { 
        return totalInstructions - pc; }
    
    // SETTERS 
    public void setId(String id) { 
        this.id = id; }
    public void setName(String name) { 
        this.name = name; }
    public void setTotalInstructions(int totalInstructions) { 
        this.totalInstructions = totalInstructions; 
    }
    public void setCpuBound(boolean isCpuBound) { 
        this.isCpuBound = isCpuBound; 
    }
    public void setCiclosExcepcionES(int ciclosExcepcionES) { 
        this.ciclosExcepcionES = ciclosExcepcionES; 
    }
    public void setTiempoESRestante(int tiempoESRestante) { 
        this.tiempoESRestante = tiempoESRestante; 
    }
    public void setProximaExcepcionES(int proximaExcepcionES) { 
        this.proximaExcepcionES = proximaExcepcionES; 
    }
    
    // 🔹 AGREGAR ESTOS MÉTODOS NUEVOS:
    
    // Getters para los nuevos campos
    public int getTiempoInicioEjecucion() {
        return tiempoInicioEjecucion;
    }
    
    public int getTiempoFinalizacion() {
        return tiempoFinalizacion;
    }
    
    
    // Método auxiliar temporal (luego se conecta con Reloj)
    private int obtenerCicloActual() {
        return Reloj.getCurrentCycle(); // ✅ Ahora usa tu reloj real
    }
    
    @Override
    public String toString() {
        return String.format("%s - %s (PC: %d/%d) [%s]", 
            id, name, pc, totalInstructions, state);
    }

    /**
     * @return the ciclosExcepcionES
     */
    public int getCiclosExcepcionES() {
        return ciclosExcepcionES;
    }
    
    
}