/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pruebas;
import sistemasoperativos_arellanomartinez.Planificador.LJF;
import sistemasoperativos_arellanomartinez.Simulador.Proceso;
import sistemasoperativos_arellanomartinez.Simulador.Reloj;

public class PruebaLJFSIMULACION {
    public static void main(String[] args) {
        System.out.println("=== SIMULACIÓN LJF CON THREADS Y SEMÁFOROS ===");
        System.out.println("🔍 Longest Job First - Procesos más largos primero");
        System.out.println("=".repeat(60));
        
        LJF planificador = new LJF();
        Reloj.setCycleDurationMs(500); // Más rápido para simulación larga
        
        // Crear procesos con diferentes longitudes (LJF)
        Proceso p1 = new Proceso("Word", 20, true, 10, 5, 0);      // 20 inst - MEDIO
        Proceso p2 = new Proceso("Calculadora", 30, false, 8, 3, 1); // 30 inst - LARGO
        Proceso p3 = new Proceso("Navegador", 50, true, 15, 8, 2);  // 50 inst - MÁS LARGO
        
        System.out.println("📦 Procesos creados (orden LJF):");
        System.out.println("🥇 " + p3.getName() + " - " + p3.getTotalInstructions() + " inst (más largo)");
        System.out.println("🥈 " + p2.getName() + " - " + p2.getTotalInstructions() + " inst");
        System.out.println("🥉 " + p1.getName() + " - " + p1.getTotalInstructions() + " inst (más corto)");
        
        // Agregar procesos en orden inverso para probar LJF
        planificador.agregarProceso(p1); // Más corto primero
        planificador.agregarProceso(p2); // Medio  
        planificador.agregarProceso(p3); // Más largo último
        
        System.out.println("\n🚀 INICIANDO SIMULACIÓN LJF CON THREADS");
        System.out.println("=".repeat(60));
        
        // Simular más ciclos
        for (int ciclo = 0; ciclo < 200; ciclo++) {
            Reloj.tick();
            System.out.println("\n--- Ciclo " + ciclo + " ---");
            
            planificador.actualizarCiclo(ciclo);
            Proceso actual = planificador.siguienteProceso();
            
            if (actual != null) {
                System.out.println("🎯 EJECUTANDO: " + actual.getName() + 
                                 " - PC: " + actual.getPc() + "/" + actual.getTotalInstructions() +
                                 " | Thread: " + (actual.isEjecutando() ? "ACTIVO" : "PAUSADO"));
                
                // 🚫 ELIMINADO: actual.setPc() - Lo hace el thread automáticamente
                // El thread maneja la ejecución automáticamente
                
            } else {
                System.out.println("💤 CPU Libre - Esperando procesos...");
            }
            
            // Mostrar estado completo
            System.out.println("\n📊 ESTADO LJF:");
            System.out.println(planificador.getEstadoCompletoThreads());
            
            // Terminar simulación si todos los procesos terminaron
            if (!planificador.tieneProcesos()) {
                System.out.println("🎉 TODOS LOS PROCESOS TERMINARON en ciclo " + ciclo);
                break;
            }
            
            // Pausa más corta para simulación larga
            try { Thread.sleep(300); } catch (InterruptedException e) {}
        }
        
        // Mostrar resultados finales
        System.out.println("\n" + "=".repeat(50));
        System.out.println("📊 RESULTADOS FINALES LJF");
        System.out.println("-".repeat(30));
        
        System.out.println("Word - PC: " + p1.getPc() + "/" + p1.getTotalInstructions() + " - " + p1.getState());
        System.out.println("Calculadora - PC: " + p2.getPc() + "/" + p2.getTotalInstructions() + " - " + p2.getState());
        System.out.println("Navegador - PC: " + p3.getPc() + "/" + p3.getTotalInstructions() + " - " + p3.getState());
        
        // 🧵 DETENER THREADS
        planificador.eliminarProceso(p1);
        planificador.eliminarProceso(p2);
        planificador.eliminarProceso(p3);
        
        System.out.println("🧵 TODOS LOS THREADS LJF DETENIDOS");
        Reloj.reset();
        
        // 📈 ANÁLISIS LJF
        System.out.println("\n⚡ ANÁLISIS LJF:");
        System.out.println("✅ Ventaja: Minimiza el número de cambios de contexto");
        System.out.println("❌ Desventaja: Puede causar 'inanición' de procesos cortos");
        System.out.println("🎯 Uso ideal: Sistemas por lotes (batch)");
    }
}