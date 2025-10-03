/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemasoperativos_arellanomartinez.Simulador;

/**
 *
 * @author raiza
 */
public class Proceso {
private static int nextId = 1;
    
    private String id;
    private String name;
    private int totalInstructions;
    private int pc; // Program Counter
    private Estado state;
    private boolean isCpuBound;
    private int ciclosExcepcionES;      // Cada cuántos ciclos genera E/S
    private int tiempoESRestante;       // Cuántos ciclos queda bloqueado
    private int proximaExcepcionES;     // Cuándo ocurrirá la próxima E/S
    
    public enum Estado {
        NUEVO, LISTO, EJECUTANDO, BLOQUEADO, 
        SUS_LISTO, SUS_BLOQUEADO, TERMINADO
    }
    
    // CONSTRUCTOR CORREGIDO
    public Proceso(String name, int totalInstructions, boolean isCpuBound, 
                  int ciclosParaExcepcionES, int duracionES) {
        this.id = "P" + nextId++;
        this.name = name;
        this.totalInstructions = totalInstructions;
        this.pc = 0;
        this.state = Estado.NUEVO;
        this.isCpuBound = isCpuBound;
        this.ciclosExcepcionES = ciclosParaExcepcionES;
        this.tiempoESRestante = 0;  // Inicialmente no está en E/S
        this.proximaExcepcionES = ciclosParaExcepcionES; // Primera E/S después de X ciclos
    }
    

    public void ejecutarInstruccion() {
        if (pc < totalInstructions) {
            pc++;
            
            // Verificar si terminó
            if (pc >= totalInstructions) {
                state = Estado.TERMINADO;
            }
        }
    }
    
    public boolean debeGenerarES() {
        // Solo los I/O-bound generan E/S, y cuando llegan al ciclo programado
        if (isCpuBound) return false;
        return pc >= proximaExcepcionES;
    }
    
    public void generarES() {
        this.tiempoESRestante = ciclosExcepcionES; // Iniciar el tiempo de E/S
        this.proximaExcepcionES = pc + ciclosExcepcionES; // Programar próxima E/S
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
    
    // GETTERS 
    public String getId() { return id; }
    public String getName() { return name; }
    public int getTotalInstructions() { return totalInstructions; }
    public int getPc() { return pc; }
    public void setPc(int pc) { this.pc = pc; }
    public Estado getState() { return state; }
    public void setState(Estado state) { this.state = state; }
    public boolean isCpuBound() { return isCpuBound; }
     public int getTiempoESRestante() { return tiempoESRestante; }
    
    // SETTERS 
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
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
    
    public boolean isFinished() {
        return pc >= totalInstructions;
    }
    
    public int getInstruccionesRestantes() {
        return totalInstructions - pc;
    }
    
    @Override
    public String toString() {
        return String.format("%s - %s (PC: %d/%d) [%s]", 
            id, name, pc, totalInstructions, state);
    }
}