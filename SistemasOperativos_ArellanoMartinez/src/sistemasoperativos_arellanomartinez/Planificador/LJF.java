/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemasoperativos_arellanomartinez.Planificador;
import sistemasoperativos_arellanomartinez.Simulador.Proceso;
import sistemasoperativos_arellanomartinez.Simulador.Proceso.Estado;
import edd.Cola;

public class LJF implements Planificador {

    private Cola colaListos;
    private Cola colaBloqueados;
    private Proceso procesoEjecutando;
    private String nombre = "LJF";
    private int cicloActual;
    
    public LJF() {
        this.colaListos = new Cola();
        this.colaBloqueados = new Cola();
        this.procesoEjecutando = null;
        this.cicloActual = 0;
    }
    
    @Override
    public Proceso siguienteProceso() {
        // Si hay proceso ejecutando, lo mantenemos (LJF no es apropiativo)
        if (procesoEjecutando != null && procesoEjecutando.getState() == Estado.EJECUTANDO) {
            return procesoEjecutando;
        }
        
        procesoEjecutando = null;
        
        // Buscar el proceso con mayor totalInstructions en cola de listos
        if (colaListos.estaVacia()) {
            return null;
        }
        
        Cola temp = new Cola();
        Proceso procesoMasLargo = null;
        
        while (!colaListos.estaVacia()) {
            Proceso actual = (Proceso) colaListos.desencolar();
            
            if (procesoMasLargo == null || 
                actual.getTotalInstructions() > procesoMasLargo.getTotalInstructions()) {
                procesoMasLargo = actual;
            }
            temp.encolar(actual);
        }
        
        // Restaurar la cola sin el proceso seleccionado
        while (!temp.estaVacia()) {
            Proceso actual = (Proceso) temp.desencolar();
            if (actual != procesoMasLargo) {
                colaListos.encolar(actual);
            }
        }
        
        procesoEjecutando = procesoMasLargo;
        if (procesoEjecutando != null) {
            procesoEjecutando.setState(Estado.EJECUTANDO);
        }
        
        return procesoEjecutando;
    }
    
    @Override
    public void agregarProceso(Proceso proceso) {
        if (proceso.getState() == Estado.NUEVO || proceso.getState() == Estado.LISTO) {
            proceso.setState(Estado.LISTO);
            colaListos.encolar(proceso);
        }
    }
    
    @Override
    public void eliminarProceso(Proceso proceso) {
        if (proceso == procesoEjecutando) {
            procesoEjecutando = null;
        }
        
        // Eliminar de cola de listos
        Cola temp = new Cola();
        while (!colaListos.estaVacia()) {
            Proceso actual = (Proceso) colaListos.desencolar();
            if (actual != proceso) {
                temp.encolar(actual);
            }
        }
        while (!temp.estaVacia()) {
            colaListos.encolar(temp.desencolar());
        }
        
        // Eliminar de cola de bloqueados
        temp = new Cola();
        while (!colaBloqueados.estaVacia()) {
            Proceso actual = (Proceso) colaBloqueados.desencolar();
            if (actual != proceso) {
                temp.encolar(actual);
            }
        }
        while (!temp.estaVacia()) {
            colaBloqueados.encolar(temp.desencolar());
        }
    }
    
    @Override
    public void actualizarCiclo(int ciclo) {
        this.cicloActual = ciclo;
        
        // 1. Manejar proceso en ejecución
        if (procesoEjecutando != null && procesoEjecutando.getState() == Estado.EJECUTANDO) {
            // Verificar si terminó
            if (procesoEjecutando.getPc() >= procesoEjecutando.getTotalInstructions()) {
                procesoEjecutando.setState(Estado.TERMINADO);
                System.out.println("✅ " + procesoEjecutando.getName() + " TERMINADO");
                procesoEjecutando = null;
                return;
            }
            
            // Verificar si necesita E/S (usando el contador interno del proceso)
            if (procesoEjecutando.getPc() > 0 && 
                procesoEjecutando.getPc() % procesoEjecutando.getCiclosExcepcionES() == 0) {
                procesoEjecutando.setState(Estado.BLOQUEADO);
                colaBloqueados.encolar(procesoEjecutando);
                System.out.println("🔄 " + procesoEjecutando.getName() + " BLOQUEADO por E/S");
                procesoEjecutando = null;
            }
        }
        
        // 2. Manejar procesos bloqueados (simular que algunos vuelven)
        if (ciclo % 5 == 0) { // Cada 5 ciclos, un proceso vuelve de E/S
            if (!colaBloqueados.estaVacia()) {
                Proceso procesoListo = (Proceso) colaBloqueados.desencolar();
                procesoListo.setState(Estado.LISTO);
                colaListos.encolar(procesoListo);
                System.out.println("🔄 " + procesoListo.getName() + " VUELVE de E/S");
            }
        }
    }
    
    @Override
    public boolean tieneProcesos() {
        return !colaListos.estaVacia() || !colaBloqueados.estaVacia() || procesoEjecutando != null;
    }
    
    @Override
    public String getNombre() {
        return this.nombre;
    }
}