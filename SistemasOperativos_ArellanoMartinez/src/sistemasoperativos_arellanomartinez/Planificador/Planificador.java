/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemasoperativos_arellanomartinez.Planificador;
import edd.ListaSimple;  //Importar primitiva
/**
 *
 * @author raiza
 */
public abstract class Planificador {
    protected ListaSimple readyQueue; //
    protected Process currentProcess;
    protected String algorithmName;
    
    public Planificador() {
        // Implementar Primitiva Lista
        this.readyQueue = new ListaSimple();
        this.currentProcess = null;
    }
    
    public abstract Process selectNextProcess();
    
    public abstract void addProcess(Process process);
    
    public abstract void reorganizeQueues();
    
    public String getAlgorithmName() {
        return algorithmName;
    }
    
    //metodo llamado get
    public ListaSimple getReadyQueue() {
        return readyQueue;
    }
    
    public Process getCurrentProcess() {
        return currentProcess;
    }
    
    public boolean hasProcesses() {
        return !readyQueue.isEmpty() || currentProcess != null;
    }
}
