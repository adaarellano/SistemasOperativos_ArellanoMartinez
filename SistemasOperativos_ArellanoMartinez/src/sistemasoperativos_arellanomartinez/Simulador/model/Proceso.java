/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemasoperativos_arellanomartinez.Simulador.model;

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
    private int mar; // Memory Address Register
    private Estado state;
    private boolean isCpuBound;
    private int ioExceptionCycle;
    private int ioCompletionTime;
    private int creationTime;
    
    public enum Estado {
        NUEVO, LISTO, EJECUTANDO, BLOQUEADO, 
        SUS_LISTO, SUS_BLOQUEADO
    }
    
    public Proceso(String name, int totalInstructions, boolean isCpuBound, 
                  int ioExceptionCycle, int ioCompletionTime, int creationTime)
    {
        this.id = "P" + nextId++;
        this.name = name;
        this.totalInstructions = totalInstructions;
        this.pc = 0;
        this.mar = 0;
        this.state = Estado.NUEVO;
        this.isCpuBound = isCpuBound;
        this.ioExceptionCycle = ioExceptionCycle;
        this.ioCompletionTime = ioCompletionTime;
        this.creationTime = creationTime;
    }
    
    public String getId() { return id; }
    public String getName() { return name; }
    public int getTotalInstructions() { return totalInstructions; }
    public int getPc() { return pc; }
    public void setPc(int pc) { this.pc = pc; }
    public int getMar() { return mar; }
    public void setMar(int mar) { this.mar = mar; }
    public Estado getState() { return state; }
    public void setState(Estado state) { this.state = state; }
    public boolean isCpuBound() { return isCpuBound; }
    public int getIoExceptionCycle() { return ioExceptionCycle; }
    public int getIoCompletionTime() { return ioCompletionTime; }
    public int getCreationTime() { return creationTime; }
    
    public void executeInstruction() {
        pc++;
        mar++;
    }
    
    public boolean isFinished() {
        return pc >= totalInstructions;
    }
    
    @Override
    public String toString() {
        return String.format("%s - %s (PC: %d/%d)", id, name, pc, totalInstructions);
    }
}
