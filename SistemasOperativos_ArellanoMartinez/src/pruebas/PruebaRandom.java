/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pruebas;

import sistemasoperativos_arellanomartinez.Planificador.RandomPlanificador;
import sistemasoperativos_arellanomartinez.Simulador.Proceso;
import sistemasoperativos_arellanomartinez.Simulador.Reloj;
/**
 *
 * @author Day y ada
 */
public class PruebaRandom {
    public static void main(String[] args) {
        System.out.println("🎲 PRUEBA ALGORITMO RANDOM CON THREADS");
        System.out.println("🔍 Selección aleatoria de procesos");
        System.out.println("=".repeat(60));
        
        RandomPlanificador planificador = new RandomPlanificador();
        Reloj.setCycleDurationMs(500); // Velocidad media
        
        // Crear procesos con diferentes características
        Proceso p1 = new Proceso("Word", 8, false, 3, 2, 0);
        Proceso p2 = new Proceso("Excel", 6, true, 0, 0, 0);
        Proceso p3 = new Proceso("Chrome", 7, false, 4, 1, 0);
        Proceso p4 = new Proceso("Spotify", 5, false, 2, 1, 0);
        
        System.out.println("📦 Procesos creados:");
        System.out.println("✅ " + p1.getName() + " - " + p1.getTotalInstructions() + " inst");
        System.out.println("✅ " + p2.getName() + " - " + p2.getTotalInstructions() + " inst");
        System.out.println("✅ " + p3.getName() + " - " + p3.getTotalInstructions() + " inst");
        System.out.println("✅ " + p4.getName() + " - " + p4.getTotalInstructions() + " inst");
        System.out.println("🎲 Algoritmo: SELECCIÓN ALEATORIA (50% de cambiar cada ciclo)");
        
        // Agregar procesos al planificador
        planificador.agregarProceso(p1);
        planificador.agregarProceso(p2);
        planificador.agregarProceso(p3);
        planificador.agregarProceso(p4);
        
        System.out.println("\n🚀 INICIANDO SIMULACIÓN RANDOM CON THREADS");
        System.out.println("=".repeat(60));
        
        // Simular 30 ciclos
        for (int ciclo = 0; ciclo < 30 && planificador.tieneProcesos(); ciclo++) {
            Reloj.tick();
            System.out.println("\n--- Ciclo " + ciclo + " ---");
            
            planificador.actualizarCiclo(ciclo);
            Proceso actual = planificador.siguienteProceso();
            
            if (actual != null) {
                System.out.println("🎲 CPU: " + actual.getName() + 
                                 " [PC: " + actual.getPc() + "/" + actual.getTotalInstructions() + "]" +
                                 " | Thread: " + (actual.isEjecutando() ? "ACTIVO" : "PAUSADO"));
                
                // Verificar si terminó (el thread lo hace automáticamente)
                if (actual.isFinished()) {
                    actual.setTiempoFinalizacion(Reloj.getCurrentCycle());
                    System.out.println("✅ " + actual.getName() + " COMPLETADO!");
                }
            } else {
                System.out.println("💤 CPU Libre - No hay procesos listos");
            }
            
            // Mostrar estado completo con threads
            System.out.println("\n📊 ESTADO RANDOM:");
            System.out.println(planificador.getEstadoCompletoThreads());
            
            // Pequeña pausa para lectura
            try { Thread.sleep(400); } catch (InterruptedException e) {}
        }
        
        // Resultados finales
        System.out.println("\n" + "=".repeat(50));
        System.out.println("📊 RESULTADOS FINALES RANDOM");
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
        
        System.out.println("🧵 TODOS LOS THREADS RANDOM DETENIDOS");
        Reloj.reset();
        
        // 📈 ANÁLISIS RANDOM
        System.out.println("\n⚡ ANÁLISIS RANDOM:");
        System.out.println("✅ Ventaja: Simple y fácil de implementar");
        System.out.println("✅ Equitativo: Todos los procesos tienen igual oportunidad");
        System.out.println("❌ Desventaja: Comportamiento impredecible");
        System.out.println("❌ Ineficiente: Muchos cambios de contexto innecesarios");
        System.out.println("🎯 Uso: Principalmente para pruebas y comparaciones");
    }
}
