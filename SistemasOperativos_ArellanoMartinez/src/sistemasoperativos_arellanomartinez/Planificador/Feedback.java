/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemasoperativos_arellanomartinez.Planificador;

import sistemasoperativos_arellanomartinez.Simulador.Proceso;
import sistemasoperativos_arellanomartinez.Simulador.Reloj;
import edd.ListaSimple;
import java.util.concurrent.Semaphore;

/**
 * Feedback
 * @author Day
 */

public class Feedback implements Planificador {
    private static final int NUM_COLAS = 4;
    private static final int[] QUANTUMS = {2, 4, 8, Integer.MAX_VALUE};

    private final ListaSimple[] colas;
    private Proceso procesoEjecutando;
    private int colaActualEjecucion;
    private int quantumRestante;
    private final Semaphore semaforoColas;
    
    private int cambiosContexto = 0;

    public Feedback() {
        this.colas = new ListaSimple[NUM_COLAS];
        for (int i = 0; i < NUM_COLAS; i++) {
            colas[i] = new ListaSimple();
        }
        this.procesoEjecutando = null;
        this.colaActualEjecucion = -1;
        this.quantumRestante = 0;
        this.semaforoColas = new Semaphore(1);
    }

    @Override
    public Proceso seleccionarProximoProceso() {
        try {
            semaforoColas.acquire();

            // 1. Evaluar el proceso actual
            if (procesoEjecutando != null) {
                if (procesoEjecutando.isFinished()) {
                    System.out.println(procesoEjecutando.getName() + " terminado.");
                    procesoEjecutando = null; // Liberar CPU
                } else if (procesoEjecutando.estaEnES()) {
                    System.out.println(procesoEjecutando.getName() + " bloqueado por E/S. Promociona a cola superior.");
                    int colaPromocion = Math.max(0, colaActualEjecucion - 1);
                    procesoEjecutando.setState(Proceso.Estado.LISTO);
                    colas[colaPromocion].insertFinal(procesoEjecutando);
                    procesoEjecutando = null; // Liberar CPU
                } else if (quantumRestante <= 0) {
                    System.out.println("Quantum expirado para " + procesoEjecutando.getName() + ". Degrada a cola inferior.");
                    int colaDegradacion = Math.min(NUM_COLAS - 1, colaActualEjecucion + 1);
                    procesoEjecutando.setState(Proceso.Estado.LISTO);
                    colas[colaDegradacion].insertFinal(procesoEjecutando);
                    procesoEjecutando = null; // Liberar CPU
                }
            }

            // 2. Si la CPU está libre, buscar un nuevo proceso
            if (procesoEjecutando == null) {
                for (int i = 0; i < NUM_COLAS; i++) {
                    if (!colas[i].isEmpty()) {
                        procesoEjecutando = (Proceso) colas[i].get(0);
                        colas[i].deleteBegin(); // Sacarlo de la cola

                        colaActualEjecucion = i;
                        quantumRestante = QUANTUMS[i];
                        
                        procesoEjecutando.setState(Proceso.Estado.EJECUTANDO);
                        cambiosContexto++;
                        System.out.println("CPU asignado a " + procesoEjecutando.getName() + " (Cola " + i + ", Q=" + quantumRestante + ")");
                        break; 
                    }
                }
            }

            // 3. Si un proceso está en CPU, simplemente decrementar su quantum
            if (procesoEjecutando != null) {
                quantumRestante--;
            } else {
                 System.out.println("Feedback: No hay procesos listos en ninguna cola.");
            }

            semaforoColas.release();
            return procesoEjecutando;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }
    
    private void removerProcesoDeColas(Proceso p) {
        for (ListaSimple cola : colas) {
            cola.remove(p);
        }
    }

    @Override
    public void agregarProceso(Proceso p) {
        try {
            semaforoColas.acquire();
            removerProcesoDeColas(p); // Evitar duplicados
            p.setState(Proceso.Estado.LISTO);
            colas[0].insertFinal(p);
            System.out.println(p.getName() + " agregado a Cola 0 de Feedback.");
            semaforoColas.release();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override public void eliminarProceso(Proceso p) {
        try {
            semaforoColas.acquire();
            if (procesoEjecutando == p) {
                procesoEjecutando = null;
            }
            removerProcesoDeColas(p);
            semaforoColas.release();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    @Override public void procesoVolvioDeES(Proceso p) {
        // La lógica de promoción ya lo maneja, pero si vuelve de una suspensión, se agrega a la cola 0
        agregarProceso(p);
    }
    
    @Override public void procesoBloqueado(Proceso p) {
        // La lógica principal en seleccionarProximoProceso se encarga de esto
    }
    
    @Override public boolean tieneProcesos() {
        if (procesoEjecutando != null) return true;
        for(ListaSimple cola : colas) {
            if(!cola.isEmpty()) return true;
        }
        return false;
    }
    
    @Override public String getNombreAlgoritmo() { return "Feedback Multinivel (Estable)"; }
    @Override public void actualizarCiclo(int c) { }
    @Override public void reorganizarColas() { }
    @Override public String getEstadoColas() { return "Estado de colas de Feedback no implementado en detalle."; }
}