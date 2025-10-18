/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pruebas;

import sistemasoperativos_arellanomartinez.Planificador.SJF;
import sistemasoperativos_arellanomartinez.Simulador.Proceso;
import sistemasoperativos_arellanomartinez.Simulador.Reloj;

/**
 *
 * @author Day y Ada
 */

public class PruebaSJF {
    public static void main(String[] args) {
        System.out.println("=== PRUEBA SJF CON THREADS Y SEMÁFOROS ===");
        System.out.println("🔍 Shortest Job First - Procesos más cortos primero");
        System.out.println("=".repeat(60));
        
        SJF planificador = new SJF();
        Reloj.setCycleDurationMs(300); // Velocidad media
        
        // Crear procesos con diferentes longitudes (SJF)
        Proceso p1 = new Proceso("Editor", 18, true, 15, 5, 0);      // 18 inst - MEDIO
        Proceso p2 = new Proceso("Calculadora", 10, false, 8, 3, 1);   // 10 inst - MÁS CORTO
        Proceso p3 = new Proceso("Navegador", 12, true, 20, 8, 2);    // 12 inst - CORTO  
        Proceso p4 = new Proceso("Notas", 15, true, 10, 4, 3);         // 15 inst - MEDIO
        
        System.out.println("\n📊 Procesos creados (orden SJF esperado):");
        System.out.println("🥇 " + p2.getName() + ": " + p2.getTotalInstructions() + " inst (más corto)");
        System.out.println("🥈 " + p3.getName() + ": " + p3.getTotalInstructions() + " inst");
        System.out.println("🥉 " + p4.getName() + ": " + p4.getTotalInstructions() + " inst");
        System.out.println("4. " + p1.getName() + ": " + p1.getTotalInstructions() + " inst (más largo)");
        
        // Agregar procesos en orden diferente al de longitud para probar SJF
        System.out.println("\n--- Agregando procesos a SJF (orden aleatorio) ---");
        planificador.agregarProceso(p1);  // 18 inst - Más largo primero
        planificador.agregarProceso(p2);  // 10 inst - Más corto segundo ← Debería ejecutarse primero
        planificador.agregarProceso(p3);  // 12 inst - Corto tercero
        planificador.agregarProceso(p4);  // 15 inst - Medio último
        
        // Simular ejecución
        System.out.println("\n--- Iniciando simulación SJF CON THREADS ---");
        
        for (int ciclo = 0; ciclo < 200; ciclo++) {
            Reloj.tick();
            System.out.println("\n--- Ciclo " + ciclo + " ---");
            
            planificador.actualizarCiclo(ciclo);
            Proceso actual = planificador.siguienteProceso();
            
            if (actual != null) {
                System.out.println("🎯 SJF EJECUTANDO: " + actual.getName() + 
                                 " [PC: " + actual.getPc() + "/" + actual.getTotalInstructions() + "]" +
                                 " | Thread: " + (actual.isEjecutando() ? "ACTIVO" : "PAUSADO"));
                
                // 🚫 ELIMINADO: actual.setPc() - Lo hace el thread automáticamente
                // 🚫 ELIMINADO: Verificación manual de terminación - Lo hace el thread
                
                // Verificar si terminó (el thread lo hace automáticamente)
                if (actual.isFinished()) {
                    actual.setTiempoFinalizacion(Reloj.getCurrentCycle());
                    System.out.println("✅ " + actual.getName() + " COMPLETADO por SJF!");
                }
            } else {
                System.out.println("⏸️ CPU Libre - No hay procesos listos");
            }
            
            // Mostrar estado completo con threads
            System.out.println("\n📊 ESTADO SJF:");
            System.out.println(planificador.getEstadoCompletoThreads());
            
            // Verificar si todos terminaron
            boolean todosTerminados = p1.isFinished() && p2.isFinished() && 
                                     p3.isFinished() && p4.isFinished();
            
            if (todosTerminados) {
                System.out.println("\n🎉 TODOS LOS PROCESOS TERMINARON con SJF!");
                break;
            }
            
            if (ciclo >= 199) {
                System.out.println("\n⏰ LÍMITE DE CICLOS ALCANZADO");
            }
            
            // Pequeña pausa para lectura
            try { Thread.sleep(200); } catch (InterruptedException e) {}
        }
        
        // Resultados finales
        System.out.println("\n" + "=".repeat(50));
        System.out.println("📊 RESULTADOS FINALES SJF");
        System.out.println("-".repeat(30));
        
        System.out.println(p1.getName() + ": " + p1.getPc() + "/" + p1.getTotalInstructions() + " - " + 
                          (p1.isFinished() ? "TERMINADO" : "INCOMPLETO"));
        System.out.println(p2.getName() + ": " + p2.getPc() + "/" + p2.getTotalInstructions() + " - " + 
                          (p2.isFinished() ? "TERMINADO" : "INCOMPLETO"));
        System.out.println(p3.getName() + ": " + p3.getPc() + "/" + p3.getTotalInstructions() + " - " + 
                          (p3.isFinished() ? "TERMINADO" : "INCOMPLETO"));
        System.out.println(p4.getName() + ": " + p4.getPc() + "/" + p4.getTotalInstructions() + " - " + 
                          (p4.isFinished() ? "TERMINADO" : "INCOMPLETO"));
        
        // 🧵 DETENER THREADS
        planificador.eliminarProceso(p1);
        planificador.eliminarProceso(p2);
        planificador.eliminarProceso(p3);
        planificador.eliminarProceso(p4);
        
        System.out.println("🧵 TODOS LOS THREADS SJF DETENIDOS");
        Reloj.reset();
        
        // 📈 ANÁLISIS SJF
        System.out.println("\n⚡ ANÁLISIS SJF:");
        System.out.println("✅ Ventaja: Minimiza tiempo de espera promedio");
        System.out.println("✅ Ideal para procesos por lotes (batch)");
        System.out.println("❌ Desventaja: Puede causar 'inanición' de procesos largos");
        System.out.println("🎯 Orden esperado: Calculadora → Navegador → Notas → Editor");
    }
}