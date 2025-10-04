/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemasoperativos_arellanomartinez.Planificador;
import sistemasoperativos_arellanomartinez.Simulador.Proceso;
import edd.Cola;
import sistemasoperativos_arellanomartinez.Simulador.Reloj;
/**
 *First Come First Served = Primero en llegar, primero en ser servido
Política no apropiativa: Una vez que un proceso toma la CPU, la mantiene hasta terminar
Impacto: Simple pero puede causar el "efecto convoy" (procesos largos bloquean a cortos)
 * @author raiza
 */
// first in first out de la cola
public class FCFS implements Planificador {
    private Cola colaListos;        // 🔹 COLA DE PROCESOS LISTOS
    private Proceso procesoEjecutando; // 🔹 PROCESO ACTUAL EN CPU
    
    
    public FCFS() {
        this.colaListos = new Cola();
        this.procesoEjecutando = null;
       
    }
    
    // 🔹 ALGORITMO FCFS: Siempre toma el primero de la cola
    @Override
    public Proceso siguienteProceso() {
        // Si hay proceso ejecutando y no terminó, continúa con él
        if (procesoEjecutando != null && !procesoEjecutando.isFinished()) {
            return procesoEjecutando;
        }
        
        // Si terminó o no hay proceso, tomar siguiente de la cola
        if (!colaListos.estaVacia()) {
            // Registrar métricas si el proceso anterior terminó
            if (procesoEjecutando != null && procesoEjecutando.isFinished()) {
                procesoEjecutando.setTiempoFinalizacion(Reloj.getCurrentCycle()); // ✅
            }
            
            // 🔹 FCFS: desencolar el primero que llegó
            procesoEjecutando = (Proceso) colaListos.desencolar();
            
            // Registrar inicio de ejecución (si es primera vez)
            if (procesoEjecutando.getTiempoInicioEjecucion() == -1) {
                procesoEjecutando.setTiempoInicioEjecucion(Reloj.getCurrentCycle()); // ✅
            }
            
            procesoEjecutando.setState(Proceso.Estado.EJECUTANDO);
            return procesoEjecutando;
        }
        
        // No hay procesos en cola
        procesoEjecutando = null;
        return null;
    }
    
    // 🔹 AGREGAR PROCESO A LA COLA
    @Override
    public void agregarProceso(Proceso proceso) {
        proceso.setState(Proceso.Estado.LISTO);
        colaListos.encolar(proceso);
        System.out.println("📥 " + proceso.getId() + " agregado a FCFS");
    }
    
    // 🔹 ELIMINAR PROCESO (para casos especiales)
    @Override
    public void eliminarProceso(Proceso proceso) {
        // En FCFS no se pueden eliminar procesos de la cola fácilmente
        // Solo se elimina si es el proceso actual
        if (procesoEjecutando == proceso) {
            procesoEjecutando = null;
        }
    }
    
    // 🔹 ACTUALIZAR CICLO GLOBAL
    @Override
    public void actualizarCiclo(int ciclo) {
    }
    
    // 🔹 VERIFICAR SI HAY PROCESOS
    @Override
    public boolean tieneProcesos() {
        return !colaListos.estaVacia() || procesoEjecutando != null;
    }
    
    @Override
    public String getNombre() {
        return "FCFS (First Come First Served)";
    }
    
    // 🔹 MÉTODOS PARA MONITOREO
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

}