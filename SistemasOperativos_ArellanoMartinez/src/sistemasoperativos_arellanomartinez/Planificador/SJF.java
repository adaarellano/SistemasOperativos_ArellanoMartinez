/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemasoperativos_arellanomartinez.Planificador;
import sistemasoperativos_arellanomartinez.Simulador.Proceso;
import sistemasoperativos_arellanomartinez.Simulador.Proceso.Estado;
import edd.Cola;
/**
 *
 * @author raiza
 */
public class SJF implements Planificador {
    private Cola colaListos;
    private Cola colaBloqueados;
    private Proceso procesoEjecutando;
    private String nombre = "SJF";
    private int cicloActual;
    
    public SJF() {
        this.colaListos = new Cola();
        this.colaBloqueados = new Cola();
        this.procesoEjecutando = null;
        this.cicloActual = 0;
    }
    
    @Override
    public Proceso siguienteProceso() {
        // Si hay proceso ejecutando y no ha terminado, lo mantenemos (SJF no apropiativo)
        if (procesoEjecutando != null && 
            procesoEjecutando.getState() == Estado.EJECUTANDO &&
            procesoEjecutando.getPc() < procesoEjecutando.getTotalInstructions()) {
            return procesoEjecutando;
        }
        
        procesoEjecutando = null;
        
        if (colaListos.estaVacia()) {
            return null;
        }
        
        // SJF: Buscar el proceso con MENOR totalInstructions
        Cola temp = new Cola();
        Proceso procesoMasCorto = null;
        
        while (!colaListos.estaVacia()) {
            Proceso actual = (Proceso) colaListos.desencolar();
            
            // Solo considerar procesos que no hayan terminado
            if (actual.getPc() < actual.getTotalInstructions()) {
                if (procesoMasCorto == null || 
                    actual.getTotalInstructions() < procesoMasCorto.getTotalInstructions()) {
                    procesoMasCorto = actual;
                }
            }
            temp.encolar(actual);
        }
        
        // Restaurar la cola
        while (!temp.estaVacia()) {
            colaListos.encolar(temp.desencolar());
        }
        
        // Si encontramos un proceso válido, lo seleccionamos
        if (procesoMasCorto != null && procesoMasCorto.getPc() < procesoMasCorto.getTotalInstructions()) {
            procesoEjecutando = procesoMasCorto;
            procesoEjecutando.setState(Estado.EJECUTANDO);
            eliminarProcesoDeCola(procesoMasCorto);
        }
        
        return procesoEjecutando;
    }
    
    // Método auxiliar para eliminar proceso específico de colaListos
    private void eliminarProcesoDeCola(Proceso proceso) {
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
    }
    
    @Override
    public void agregarProceso(Proceso proceso) {
        if (proceso.getState() == Estado.NUEVO || proceso.getState() == Estado.LISTO) {
            proceso.setState(Estado.LISTO);
            colaListos.encolar(proceso);
            System.out.println("➕ " + proceso.getName() + " agregado a SJF - Instrucciones: " + 
                             proceso.getTotalInstructions());
        }
    }
    
    @Override
    public void eliminarProceso(Proceso proceso) {
        if (proceso == procesoEjecutando) {
            procesoEjecutando = null;
        }
        
        // Eliminar de cola de listos
        eliminarProcesoDeCola(proceso);
        
        // Eliminar de cola de bloqueados
        Cola temp = new Cola();
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
                System.out.println("✅ " + procesoEjecutando.getName() + " TERMINADO (SJF)");
                procesoEjecutando = null;
                return;
            }
            
            // Verificar si necesita E/S (basado en ciclos de excepción E/S)
            if (procesoEjecutando.getPc() > 0 && 
                procesoEjecutando.getPc() % procesoEjecutando.getCiclosExcepcionES() == 0) {
                procesoEjecutando.setState(Estado.BLOQUEADO);
                colaBloqueados.encolar(procesoEjecutando);
                System.out.println("🔄 " + procesoEjecutando.getName() + " BLOQUEADO por E/S (SJF)");
                procesoEjecutando = null;
            }
        }
        
        // 2. Procesos bloqueados vuelven a lista de listos
        if (!colaBloqueados.estaVacia()) {
            Cola temp = new Cola();
            while (!colaBloqueados.estaVacia()) {
                Proceso bloqueado = (Proceso) colaBloqueados.desencolar();
                // Simular que procesos vuelven de E/S después de algunos ciclos
                if (ciclo % 4 == 0) { // Cada 4 ciclos vuelve uno
                    bloqueado.setState(Estado.LISTO);
                    colaListos.encolar(bloqueado);
                    System.out.println("🔄 " + bloqueado.getName() + " VUELVE de E/S a LISTO (SJF)");
                } else {
                    temp.encolar(bloqueado);
                }
            }
            // Restaurar los que no volvieron
            while (!temp.estaVacia()) {
                colaBloqueados.encolar(temp.desencolar());
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
    
    // Método para debug: mostrar estado actual de colas
    public void mostrarEstado() {
        System.out.println("=== ESTADO SJF ===");
        System.out.println("Proceso ejecutando: " + 
                          (procesoEjecutando != null ? procesoEjecutando.getName() : "Ninguno"));
        System.out.println("Procesos en cola listos: " + colaListos.getTamano());
        System.out.println("Procesos bloqueados: " + colaBloqueados.getTamano());
    }
}
