/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pruebas;

import sistemasoperativos_arellanomartinez.Planificador.Feedback;
import sistemasoperativos_arellanomartinez.Simulador.Proceso;
import sistemasoperativos_arellanomartinez.Simulador.Reloj;

public class PruebaFeedback {

    public static void main(String[] args) {
        Feedback planificador = new Feedback();

        Proceso p1 = new Proceso("P1", 12, true, 0, 0, 0);
        Proceso p2 = new Proceso("P2", 10, false, 3, 2, 0);
        Proceso p3 = new Proceso("P3", 15, false, 5, 3, 0);

        p1.iniciarEjecucion();
        p2.iniciarEjecucion();
        p3.iniciarEjecucion();

        planificador.agregarProceso(p1);
        planificador.agregarProceso(p2);
        planificador.agregarProceso(p3);

        Reloj.inicializar(0);

        while (planificador.tieneProcesos()) {
            // Revisar procesos en E/S
            for (Proceso p : new Proceso[]{p1, p2, p3}) {
                if (p.getState() == Proceso.Estado.BLOQUEADO && !p.estaEnES() && !p.isFinished()) {
                    planificador.procesoVolvioDeES(p);
                }
            }

            // Seleccionar y ejecutar proceso en CPU
            Proceso ejecutando = planificador.seleccionarProximoProceso();

            if (ejecutando != null && ejecutando.getState() == Proceso.Estado.EJECUTANDO && !ejecutando.isPausado()) {
                ejecutando.permitirEjecutarCiclo();
            }

            // Mostrar estados
            System.out.println(planificador.getEstadoColas());

            // Avanzar reloj
            Reloj.tick();

            try {
                Thread.sleep(Reloj.getCycleDurationMs());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        System.out.println("Simulación finalizada.");
    }
}
