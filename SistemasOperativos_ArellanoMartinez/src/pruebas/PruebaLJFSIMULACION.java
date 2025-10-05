/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pruebas;
import sistemasoperativos_arellanomartinez.Planificador.LJF;
import sistemasoperativos_arellanomartinez.Simulador.Proceso;
import sistemasoperativos_arellanomartinez.Simulador.Proceso.Estado;

public class PruebaLJFSIMULACION {
      public static void main(String[] args) {
        System.out.println("=== SIMULACIÓN COMPLETA LJF CORREGIDA ===");
        
        LJF planificador = new LJF();
        
        // Crear procesos con diferentes características
        Proceso p1 = new Proceso("Word", 20, true, 10, 5, 0);      // E/S cada 10 instrucciones
        Proceso p2 = new Proceso("Calculadora", 30, false, 8, 3, 1); // E/S cada 8 instrucciones  
        Proceso p3 = new Proceso("Navegador", 50, true, 15, 8, 2);  // E/S cada 15 instrucciones
        
        // Agregar procesos
        planificador.agregarProceso(p1);
        planificador.agregarProceso(p2);
        planificador.agregarProceso(p3);
        
        // Simular más ciclos
        for (int ciclo = 0; ciclo < 200; ciclo++) {
            System.out.println("\n--- Ciclo " + ciclo + " ---");
            
            planificador.actualizarCiclo(ciclo);
            Proceso actual = planificador.siguienteProceso();
            
            if (actual != null) {
                System.out.println("Ejecutando: " + actual.getName() + 
                                 " - PC: " + actual.getPc() + 
                                 "/" + actual.getTotalInstructions());
                
                // Ejecutar instrucción
                if (actual.getState() == Estado.EJECUTANDO) {
                    actual.setPc(actual.getPc() + 1);
                }
            } else {
                System.out.println("CPU Libre - Esperando procesos...");
            }
            
            // Terminar simulación si todos los procesos terminaron
            if (!planificador.tieneProcesos()) {
                System.out.println("🎉 TODOS LOS PROCESOS TERMINARON en ciclo " + ciclo);
                break;
            }
            
            // Pausa para ver la ejecución
            try { Thread.sleep(100); } catch (InterruptedException e) {}
        }
        
        // Mostrar resultados finales
        System.out.println("\n=== RESULTADOS FINALES ===");
        System.out.println("Word - PC: " + p1.getPc() + "/" + p1.getTotalInstructions() + " - " + p1.getState());
        System.out.println("Calculadora - PC: " + p2.getPc() + "/" + p2.getTotalInstructions() + " - " + p2.getState());
        System.out.println("Navegador - PC: " + p3.getPc() + "/" + p3.getTotalInstructions() + " - " + p3.getState());
    }
}