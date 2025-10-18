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
        System.out.println("🎯 PRUEBA REAL ROUND ROBIN CON THREADS");
        System.out.println("🔍 Prueba con Threads y Semáforos");
        System.out.println("=".repeat(50));
        
        RR rr = new RR(3); // Quantum de 3
        Reloj.setCycleDurationMs(800); // Más lento para ver threads
        
        // Crear procesos
        Proceso p1 = new Proceso("Word", 6, false, 4, 2, 0);
        Proceso p2 = new Proceso("Excel", 4, true, 0, 0, 0);
        Proceso p3 = new Proceso("Navegador", 5, false, 3, 1, 0);
        
        System.out.println("📦 Procesos creados:");
        System.out.println("✅ " + p1.getName() + " (6 inst, E/S cada 4 ciclos)");
        System.out.println("✅ " + p2.getName() + " (4 inst, CPU-bound)");
        System.out.println("✅ " + p3.getName() + " (5 inst, E/S cada 3 ciclos)");
        System.out.println("⏱️  Quantum: " + rr.getQuantum() + " ciclos");
        
        // Agregar a RR
        rr.agregarProceso(p1);
        rr.agregarProceso(p2);
        rr.agregarProceso(p3);
        
        System.out.println("\n🚀 INICIANDO SIMULACIÓN RR CON THREADS");
        System.out.println("=".repeat(50));
        
        // Simular
        for (int ciclo = 0; ciclo < 25 && rr.tieneProcesos(); ciclo++) {
            Reloj.tick();
            System.out.println("\n⏰ CICLO " + ciclo);
            
            // 🎯 Obtener proceso (maneja threads automáticamente)
            Proceso actual = rr.siguienteProceso();
            
            if (actual != null) {
                System.out.println("🖥️  CPU: " + actual.getId() + " - " + actual.getName());
                System.out.println("⏱️  " + rr.getEstadoQuantum());
                System.out.println("📊 PC: " + actual.getPc() + "/" + actual.getTotalInstructions() + 
                                 " | Estado: " + actual.getState() + 
                                 " | Thread: " + (actual.isEjecutando() ? "ACTIVO" : "PAUSADO"));
                
                // 🔄 VERIFICAR E/S (se maneja automáticamente en threads)
                if (actual.estaEnES()) {
                    System.out.println("💾 EN E/S - Tiempo restante: " + actual.getTiempoESRestante() + " ciclos");
                    // El thread maneja la E/S automáticamente
                }
                
                // ✅ VERIFICAR SI TERMINÓ
                if (actual.isFinished()) {
                    actual.setTiempoFinalizacion(Reloj.getCurrentCycle());
                    System.out.println("🎉 " + actual.getName() + " TERMINADO!");
                }
                
                // 🚫 ELIMINADO: ejecutarInstruccion() - Lo hace el thread automáticamente
                // 🚫 ELIMINADO: procesarCicloES() - Lo hace el thread automáticamente
                // 🚫 ELIMINADO: generarES() - Lo hace el thread automáticamente
                
            } else {
                System.out.println("💤 CPU inactiva");
            }
            
            // 📊 MOSTRAR ESTADO COMPLETO CON THREADS
            System.out.println("\n📈 ESTADO COMPLETO RR:");
            System.out.println(rr.getEstadoCompletoThreads());
            
            try { 
                Thread.sleep(1000); // Más tiempo para ver los threads
            } catch (Exception e) {}
        }
        
        // 📊 MÉTRICAS FINALES
        System.out.println("\n" + "=".repeat(50));
        System.out.println("📊 SIMULACIÓN RR COMPLETADA - MÉTRICAS FINALES");
        System.out.println("-".repeat(30));
        
        mostrarMetricasRR(p1, p2, p3, Reloj.getCurrentCycle(), rr);
        
        // 🧵 DETENER THREADS
        rr.eliminarProceso(p1);
        rr.eliminarProceso(p2);
        rr.eliminarProceso(p3);
        
        System.out.println("🧵 TODOS LOS THREADS DETENIDOS");
        Reloj.reset();
    }
    
    private static void mostrarMetricasRR(Proceso p1, Proceso p2, Proceso p3, 
                                        int ciclosTotales, RR rr) {
        Proceso[] procesos = {p1, p2, p3};
        int completados = 0;
        int totalEspera = 0;
        int totalRetorno = 0;
        
        System.out.println("📈 MÉTRICAS POR PROCESO:");
        for (Proceso p : procesos) {
            System.out.println("\n   " + p.getName() + ":");
            System.out.println("     Estado: " + p.getState());
            System.out.println("     Progreso: " + p.getPc() + "/" + p.getTotalInstructions());
            System.out.println("     Thread: " + (p.isEjecutando() ? "ACTIVO" : "INACTIVO"));
            
            if (p.isFinished()) {
                completados++;
                totalEspera += p.getTiempoEspera();
                totalRetorno += p.getTiempoRetorno();
                
                System.out.println("     Tiempo espera: " + p.getTiempoEspera() + " ciclos");
                System.out.println("     Tiempo retorno: " + p.getTiempoRetorno() + " ciclos");
            } else {
                System.out.println("     No completado");
            }
        }
        
        System.out.println("\n📊 MÉTRICAS GLOBALES RR:");
        System.out.println("   Ciclos totales: " + ciclosTotales);
        System.out.println("   Procesos completados: " + completados + "/3");
        System.out.println("   Cambios contexto: " + rr.getQuantumCompletados());
        System.out.println("   Quantums completados: " + rr.getQuantumCompletados());
        
        if (completados > 0) {
            System.out.println("   Tiempo espera promedio: " + (totalEspera / completados) + " ciclos");
            System.out.println("   Tiempo retorno promedio: " + (totalRetorno / completados) + " ciclos");
            System.out.println("   Throughput: " + 
                             String.format("%.2f", (double) completados / ciclosTotales) + 
                             " procesos/ciclo");
        }
        
        System.out.println("\n⚡ CARACTERÍSTICAS RR:");
        System.out.println("   ✅ Apropiativo (cambia por quantum)");
        System.out.println("   ✅ Equitativo (todos los procesos tienen turnos)");
        System.out.println("   ✅ Buen tiempo de respuesta");
        System.out.println("   ⚠️  Más cambios de contexto que FCFS");
    }
}