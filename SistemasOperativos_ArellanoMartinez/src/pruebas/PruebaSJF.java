/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pruebas;

import sistemasoperativos_arellanomartinez.Planificador.SJF;
import sistemasoperativos_arellanomartinez.Simulador.Proceso;

/**
 *
 * @author raiza
 */

public class PruebaSJF {
    public static void main(String[] args) {
        System.out.println("=== PRUEBA ALGORITMO SJF (Shortest Job First) ===");
        
        SJF planificador = new SJF();
        
        // Crear procesos con diferentes longitudes
        Proceso p1 = new Proceso("Editor", 18, true, 15, 5, 0);      // Largo
        Proceso p2 = new Proceso("Calculadora", 10, false, 8, 3, 1);   // Corto
        Proceso p3 = new Proceso("Navegador", 12, true, 20, 8, 2);    // Muy largo
        Proceso p4 = new Proceso("Notas", 15, true, 10, 4, 3);         // Medio
        
        System.out.println("\n📊 Procesos creados (ordenados por longitud):");
        System.out.println("1. " + p2.getName() + ": " + p2.getTotalInstructions() + " instrucciones");
        System.out.println("2. " + p4.getName() + ": " + p4.getTotalInstructions() + " instrucciones");
        System.out.println("3. " + p1.getName() + ": " + p1.getTotalInstructions() + " instrucciones");
        System.out.println("4. " + p3.getName() + ": " + p3.getTotalInstructions() + " instrucciones");
        
        // Agregar procesos en orden diferente al de longitud
        System.out.println("\n--- Agregando procesos a SJF ---");
        planificador.agregarProceso(p1);  // 100
        planificador.agregarProceso(p2);  // 30  ← Debería ejecutarse primero
        planificador.agregarProceso(p3);  // 150
        planificador.agregarProceso(p4);  // 50
        
        // Simular ejecución
        System.out.println("\n--- Iniciando simulación SJF ---");
        
        for (int ciclo = 0; ciclo < 200; ciclo++) {
            System.out.println("\n--- Ciclo " + ciclo + " ---");
            
            planificador.actualizarCiclo(ciclo);
            Proceso actual = planificador.siguienteProceso();
            
            if (actual != null) {
                System.out.println("▶️ SJF Ejecutando: " + actual.getName() + 
                                 " [PC: " + actual.getPc() + "/" + actual.getTotalInstructions() + "]");
                
                // Ejecutar instrucción
                if (actual.getState() == Proceso.Estado.EJECUTANDO) {
                    actual.setPc(actual.getPc() + 1);
                    
                    // Verificar si terminó
                    if (actual.getPc() >= actual.getTotalInstructions()) {
                        System.out.println("✅ " + actual.getName() + " COMPLETADO por SJF!");
                    }
                }
            } else {
                System.out.println("⏸️ CPU Libre - No hay procesos listos");
            }
            
            // Verificar si todos terminaron
            boolean todosTerminados = p1.getPc() >= p1.getTotalInstructions() &&
                                     p2.getPc() >= p2.getTotalInstructions() &&
                                     p3.getPc() >= p3.getTotalInstructions() &&
                                     p4.getPc() >= p4.getTotalInstructions();
            
            if (todosTerminados) {
                System.out.println("\n🎉 TODOS LOS PROCESOS TERMINARON con SJF!");
                break;
            }
            
            if (ciclo >= 199) {
                System.out.println("\n⏰ LÍMITE DE CICLOS ALCANZADO");
            }
            
            // Pequeña pausa para lectura
            try { Thread.sleep(50); } catch (InterruptedException e) {}
        }
        
        // Resultados finales
        System.out.println("\n=== RESULTADOS FINALES SJF ===");
        System.out.println(p1.getName() + ": " + p1.getPc() + "/" + p1.getTotalInstructions() + " - " + 
                          (p1.getPc() >= p1.getTotalInstructions() ? "TERMINADO" : "INCOMPLETO"));
        System.out.println(p2.getName() + ": " + p2.getPc() + "/" + p2.getTotalInstructions() + " - " + 
                          (p2.getPc() >= p2.getTotalInstructions() ? "TERMINADO" : "INCOMPLETO"));
        System.out.println(p3.getName() + ": " + p3.getPc() + "/" + p3.getTotalInstructions() + " - " + 
                          (p3.getPc() >= p3.getTotalInstructions() ? "TERMINADO" : "INCOMPLETO"));
        System.out.println(p4.getName() + ": " + p4.getPc() + "/" + p4.getTotalInstructions() + " - " + 
                          (p4.getPc() >= p4.getTotalInstructions() ? "TERMINADO" : "INCOMPLETO"));
        
        // Verificar orden de ejecución esperado
    }
}
