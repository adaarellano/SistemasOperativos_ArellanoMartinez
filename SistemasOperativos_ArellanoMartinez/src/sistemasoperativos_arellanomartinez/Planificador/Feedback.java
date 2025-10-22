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

    private int cambiosContexto;
    private int ciclosTotales;
    private int desalojosQuantum;
    private int procesosCompletados;
    private int promocionesES;

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
            ciclosTotales++;

            if (procesoEjecutando != null) {
                if (procesoEjecutando.isFinished()) {
                    manejarProcesoTerminado();
                } else if (procesoEjecutando.estaEnES()) {
                    manejarProcesoES();
                } else if (quantumRestante <= 0) {
                    manejarQuantumAgotado();
                } else {
                    quantumRestante--;
                    semaforoColas.release();
                    return procesoEjecutando;
                }
            }

            Proceso siguiente = buscarProcesoValidoEnColas();

            if (siguiente != null) {
                asignarProcesoCPU(siguiente);
            } else {
                procesoEjecutando = null;
                colaActualEjecucion = -1;
                quantumRestante = 0;
            }

            semaforoColas.release();
            return procesoEjecutando;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    private Proceso buscarProcesoValidoEnColas() {
        for (int i = 0; i < NUM_COLAS; i++) {
            for (int j = 0; j < colas[i].sizeLista(); j++) {
                Proceso p = (Proceso) colas[i].get(j);

                if (p == null || p.isFinished() || p == procesoEjecutando) {
                    colas[i].remove(j--);
                    continue;
                }

                if (!p.isFinished() && !estaEnColaCPU(p)) {
                    colas[i].remove(j);
                    colaActualEjecucion = i;
                    quantumRestante = QUANTUMS[i];
                    return p;
                }
            }
        }
        return null;
    }

    private boolean estaEnColaCPU(Proceso p) {
        return procesoEjecutando != null && procesoEjecutando == p;
    }

    private void asignarProcesoCPU(Proceso p) {
        procesoEjecutando = p;
        if (p.getTiempoInicioEjecucion() == -1)
            p.setTiempoInicioEjecucion(Reloj.getCurrentCycle());
        p.setState(Proceso.Estado.EJECUTANDO);
        cambiosContexto++;

        System.out.println("🎯 CPU asignado a " + p.getName() +
                           " (Cola " + colaActualEjecucion + ", Quantum=" + quantumRestante + ")");
    }

    private void manejarProcesoTerminado() {
        procesosCompletados++;
        procesoEjecutando.setTiempoFinalizacion(Reloj.getCurrentCycle());
        limpiarProcesoDeTodasLasColas(procesoEjecutando);
        System.out.println("✅ " + procesoEjecutando.getName() + " terminado y limpiado");
        procesoEjecutando = null;
        colaActualEjecucion = -1;
        quantumRestante = 0;
    }

    private void manejarProcesoES() {
        int nuevaCola = Math.max(0, colaActualEjecucion - 1);
        promocionesES++;

        if (!procesoEjecutando.isFinished() && !estaEnOtraCola(procesoEjecutando)) {
            procesoEjecutando.setState(Proceso.Estado.LISTO);
            colas[nuevaCola].insertFinal(procesoEjecutando);
            System.out.println("🔁 " + procesoEjecutando.getName() +
                               " a E/S -> reinsertado en cola " + nuevaCola);
        }
        procesoEjecutando = null;
        colaActualEjecucion = -1;
        quantumRestante = 0;
    }

    private void manejarQuantumAgotado() {
        int nuevaCola = Math.min(NUM_COLAS - 1, colaActualEjecucion + 1);
        desalojosQuantum++;

        if (!procesoEjecutando.isFinished() && !estaEnOtraCola(procesoEjecutando)) {
            procesoEjecutando.setState(Proceso.Estado.LISTO);
            colas[nuevaCola].insertFinal(procesoEjecutando);
            System.out.println("⏰ " + procesoEjecutando.getName() +
                               " bajó a cola " + nuevaCola);
        }
        procesoEjecutando = null;
        colaActualEjecucion = -1;
        quantumRestante = 0;
    }

    private boolean estaEnOtraCola(Proceso p) {
        for (ListaSimple cola : colas) {
            for (int i = 0; i < cola.sizeLista(); i++) {
                if (cola.get(i) == p) return true;
            }
        }
        return false;
    }

    private void limpiarProcesoDeTodasLasColas(Proceso p) {
        for (ListaSimple cola : colas) {
            for (int j = cola.sizeLista() - 1; j >= 0; j--) {
                if (cola.get(j) == p) cola.remove(j);
            }
        }
    }

    @Override
    public void agregarProceso(Proceso p) {
        try {
            semaforoColas.acquire();
            if (!p.isFinished() && !estaEnOtraCola(p)) {
                p.setState(Proceso.Estado.LISTO);
                colas[0].insertFinal(p);
                System.out.println("📥 " + p.getName() + " agregado a Cola 0");
            }
            semaforoColas.release();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override public void eliminarProceso(Proceso p) {
        try {
            semaforoColas.acquire();
            limpiarProcesoDeTodasLasColas(p);
            if (procesoEjecutando == p) procesoEjecutando = null;
            semaforoColas.release();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override public void procesoVolvioDeES(Proceso p) {
        agregarProceso(p);
    }

    @Override public void procesoBloqueado(Proceso p) { }

    @Override public String getNombreAlgoritmo() { return "Feedback Multinivel (Estable)"; }

    @Override public void actualizarCiclo(int c) { }

    @Override public void reorganizarColas() { }

    @Override
    public boolean tieneProcesos() {
        if (procesoEjecutando != null && !procesoEjecutando.isFinished()) return true;
        for (ListaSimple cola : colas) {
            for (int i = 0; i < cola.sizeLista(); i++) {
                Proceso p = (Proceso) cola.get(i);
                if (p != null && !p.isFinished()) return true;
            }
        }
        return false;
    }

    @Override
    public String getEstadoColas() {
        StringBuilder sb = new StringBuilder();
        sb.append("CPU: ").append(procesoEjecutando != null ?
            procesoEjecutando.getName() + " (cola " + colaActualEjecucion + ")" : "LIBRE").append("\n");
        for (int i = 0; i < NUM_COLAS; i++) {
            sb.append("Cola ").append(i).append(": ");
            for (int j = 0; j < colas[i].sizeLista(); j++) {
                Proceso p = (Proceso) colas[i].get(j);
                sb.append(p.getName());
                if (p.isFinished()) sb.append("[T]");
                sb.append(" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
