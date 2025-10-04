/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemasoperativos_arellanomartinez.Planificador;

import sistemasoperativos_arellanomartinez.Simulador.Proceso;
import sistemasoperativos_arellanomartinez.Simulador.Reloj;
import edd.Cola;
import edd.ListaCircular;

/**
 * 🔄 ROUND ROBIN - Algoritmo de planificación por turnos
 * Asigna un quantum de tiempo a cada proceso
 */
public class RR implements Planificador {
    private Cola colaListos;
    private Proceso procesoEjecutando;
    private int quantum;
    private int tiempoRestanteQuantum;
    
    // Quantum por defecto (puede configurarse)
    private static final int QUANTUM_DEFAULT = 3;
    
    public RR() {
        this(QUANTUM_DEFAULT);
    }
    
    public RR(int quantum) {
        this.colaListos = new Cola();
        this.procesoEjecutando = null;
        this.quantum = quantum;
        this.tiempoRestanteQuantum = 0;
    }
    
    @Override
    public Proceso siguienteProceso() {
        // Si hay proceso ejecutando y aún tiene quantum, continúa con él
        if (procesoEjecutando != null && tiempoRestanteQuantum > 0 && !procesoEjecutando.isFinished()) {
            tiempoRestanteQuantum--;
            return procesoEjecutando;
        }
        
        // Si el proceso actual terminó o se acabó el quantum
        if (procesoEjecutando != null) {
            // Si terminó, registrar tiempo final
            if (procesoEjecutando.isFinished()) {
                procesoEjecutando.setTiempoFinalizacion(Reloj.getCurrentCycle());
            } 
            // Si no terminó pero se acabó el quantum, volver a la cola
            else if (tiempoRestanteQuantum == 0) {
                procesoEjecutando.setState(Proceso.Estado.LISTO);
                colaListos.encolar(procesoEjecutando);
            }
        }
        
        // Tomar siguiente proceso de la cola
        if (!colaListos.estaVacia()) {
            procesoEjecutando = (Proceso) colaListos.desencolar();
            
            // Registrar inicio de ejecución (si es primera vez)
            if (procesoEjecutando.getTiempoInicioEjecucion() == -1) {
                procesoEjecutando.setTiempoInicioEjecucion(Reloj.getCurrentCycle());
            }
            
            procesoEjecutando.setState(Proceso.Estado.EJECUTANDO);
            tiempoRestanteQuantum = quantum - 1; // -1 porque ya usamos 1 ciclo
            
            return procesoEjecutando;
        }
        
        // No hay procesos en cola
        procesoEjecutando = null;
        tiempoRestanteQuantum = 0;
        return null;
    }
    
    @Override
    public void agregarProceso(Proceso proceso) {
        proceso.setState(Proceso.Estado.LISTO);
        colaListos.encolar(proceso);
        System.out.println("📥 " + proceso.getId() + " agregado a Round Robin");
    }
    
    @Override
    public void eliminarProceso(Proceso proceso) {
        if (procesoEjecutando == proceso) {
            procesoEjecutando = null;
            tiempoRestanteQuantum = 0;
        }
    }
    
    @Override
    public void actualizarCiclo(int ciclo) {
        // No necesita hacer nada especial por ciclo
    }
    
    @Override
    public boolean tieneProcesos() {
        return !colaListos.estaVacia() || procesoEjecutando != null;
    }
    
    @Override
    public String getNombre() {
        return "Round Robin (Quantum: " + quantum + ")";
    }
    
    // 🔧 MÉTODOS ESPECÍFICOS DE RR
    
    public int getQuantum() {
        return quantum;
    }
    
    public void setQuantum(int quantum) {
        this.quantum = quantum;
    }
    
    public int getTiempoRestanteQuantum() {
        return tiempoRestanteQuantum;
    }
    
    public Proceso getProcesoEjecutando() {
        return procesoEjecutando;
    }
    
    public int getTamanoColaListos() {
        return colaListos.getTamano();
    }
    
    public String getEstadoCola() {
        if (colaListos.estaVacia()) {
            return "🟢 Cola vacía";
        }
        return "📋 " + colaListos.getTamano() + " procesos en cola";
    }
    
    public String getEstadoQuantum() {
        if (procesoEjecutando == null) {
            return "💤 Sin proceso";
        }
        return "⏱️  Quantum: " + (tiempoRestanteQuantum + 1) + "/" + quantum;
    }
}
