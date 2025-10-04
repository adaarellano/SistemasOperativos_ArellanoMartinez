/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pruebas;

/**
 *
 * @author Indatech
 */
import sistemasoperativos_arellanomartinez.Planificador.RR;
import sistemasoperativos_arellanomartinez.Simulador.Proceso;
import sistemasoperativos_arellanomartinez.Simulador.Reloj;

public class PruebaRR {
    public static void main(String[] args) {
        System.out.println("🎯 PRUEBA REAL ROUND ROBIN");
        
        RR rr = new RR(3); // Quantum de 3
        Reloj.reset();
        
        // Crear procesos
        Proceso p1 = new Proceso("Word", 6, false, 4, 2, 0);
        Proceso p2 = new Proceso("Excel", 4, true, 0, 0, 0);
        Proceso p3 = new Proceso("Navegador", 5, false, 3, 1, 0);
        
        // Agregar a RR
        rr.agregarProceso(p1);
        rr.agregarProceso(p2);
        rr.agregarProceso(p3);
        
        // Simular
        for (int ciclo = 0; ciclo < 25 && rr.tieneProcesos(); ciclo++) {
            Reloj.tick();
            System.out.println("\n⏰ CICLO " + ciclo);
            
            Proceso actual = rr.siguienteProceso();
            if (actual != null) {
                System.out.println("🖥️  CPU: " + actual.getId() + " - " + actual.getName());
                System.out.println("⏱️  " + rr.getEstadoQuantum());
                
                if (actual.debeGenerarES() && !actual.estaEnES()) {
                    System.out.println("🔄 GENERANDO E/S!");
                    actual.generarES();
                }
                
                if (actual.estaEnES()) {
                    System.out.println("💾 E/S: " + actual.getTiempoESRestante() + " ciclos restantes");
                    actual.procesarCicloES();
                } else {
                    actual.ejecutarInstruccion();
                    System.out.println("⚡ PC: " + actual.getPc() + "/" + actual.getTotalInstructions());
                }
            } else {
                System.out.println("💤 CPU inactiva");
            }
            
            try { Thread.sleep(500); } catch (Exception e) {}
        }
        
        System.out.println("\n🎉 PRUEBA RR COMPLETADA");
    } 
}
