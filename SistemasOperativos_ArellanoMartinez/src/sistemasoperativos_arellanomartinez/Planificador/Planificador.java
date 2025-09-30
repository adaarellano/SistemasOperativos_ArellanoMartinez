/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemasoperativos_arellanomartinez.Planificador;
import java.util.List;
/**
 *
 * @author raiza
 */
public abstract class Planificador {
    protected List<Process> readyQueue;
    protected Process currentProcess;
    protected String algorithmName;
    
    public Planificador() {
        // Debes implementar tu propia List aquí
        this.readyQueue = new Lista<>();//primitiva // Implementar esta clase
    }
    
    public abstract Process selectNextProcess();
    
    public abstract void addProcess(Process process);
    
    public abstract void reorganizeQueues();
    
    public String getAlgorithmName() {
        return algorithmName;
    }
    
    public List<Process> getReadyQueue() {
        return readyQueue;
    }
    
    public Process getCurrentProcess() {
        return currentProcess;
    }
    
    public boolean hasProcesses() {
        return !readyQueue.isEmpty() || currentProcess != null;
    }
}
