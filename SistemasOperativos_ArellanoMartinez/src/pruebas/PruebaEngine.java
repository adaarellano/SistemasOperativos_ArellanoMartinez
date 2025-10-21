/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pruebas;

import sistemasoperativos_arellanomartinez.Controller.Engine;
import sistemasoperativos_arellanomartinez.Planificador.FCFS;
import sistemasoperativos_arellanomartinez.Simulador.Proceso;
import sistemasoperativos_arellanomartinez.Simulador.Reloj;

/**
 * Prueba integral del Engine con procesos y threads
 */
public class PruebaEngine {
    public static void main(String[] args) {
        try {
            Reloj.setCycleDurationMs(800); // Más lento para debug
            FCFS planificador = new FCFS();
            Engine engine = new Engine(planificador);
            
            Proceso editor = new Proceso("Editor", 3, false, 2, 1, 0);
            engine.agregarProceso(editor);
            
            System.out.println("=== DEBUG EXTREMO EDITOR ===");
            engine.iniciarSimulacion();
            
            // Monitoreo MUY frecuente
            for (int i = 0; i < 20; i++) {
                Thread.sleep(100); // Solo 100ms entre lecturas
                
                System.out.println("\n--- MICRO-DEBUG " + (i + 1) + " ---");
                System.out.println("Ciclo: " + Reloj.getCurrentCycle());
                System.out.println("PC: " + editor.getPc());
                System.out.println("Estado: " + editor.getState());
                System.out.println("E/S Restante: " + editor.getTiempoESRestante());
                System.out.println("Debe generar E/S: " + editor.debeGenerarES());
                System.out.println("Permiso: " + editor.isEjecutando());
                
                // Forzar detención si no hay progreso
                if (i > 15 && editor.getPc() == 2) {
                    System.out.println("🔴 BLOQUEO CONFIRMADO en PC=2");
                    break;
                }
            }
            
            engine.detenerSimulacion();
            
        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }
}