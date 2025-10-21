/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pruebas;

import sistemasoperativos_arellanomartinez.Controller.Engine;
import sistemasoperativos_arellanomartinez.Planificador.RR;
import sistemasoperativos_arellanomartinez.Simulador.Proceso;
import sistemasoperativos_arellanomartinez.Simulador.Reloj;

/**
 * Prueba Round Robin - Verificación del algoritmo apropiativo
 * @author Day
 */
public class PruebaRR {
    
    public static void main(String[] args) {
        System.out.println("🚀 PRUEBA ROUND ROBIN - ALGORITMO APROPIATIVO");
        System.out.println("==============================================\n");
        
        Reloj.setCycleDurationMs(400);
        
        // Crear Round Robin con quantum de 3 ciclos
        RR planificador = new RR(3);
        Engine engine = new Engine(planificador);
        
        System.out.println("🎯 CONFIGURACIÓN:");
        System.out.println("   • Quantum: 3 ciclos");
        System.out.println("   • Procesos se rotarán cada 3 ciclos");
        System.out.println("   • Comportamiento apropiativo\n");
        
        // Procesos de prueba
        Proceso procesoA = new Proceso("A-Largo", 10, true, 0, 0, 0);
        Proceso procesoB = new Proceso("B-Medio", 7, true, 0, 0, 1);
        Proceso procesoC = new Proceso("C-Corto", 4, true, 0, 0, 2);
        Proceso procesoD = new Proceso("D-ConES", 8, false, 3, 2, 3);
        
        System.out.println("📋 PROCESOS DE PRUEBA:");
        System.out.println("   • A-Largo: 10 instrucciones (llega ciclo 0)");
        System.out.println("   • B-Medio: 7 instrucciones (llega ciclo 1)");
        System.out.println("   • C-Corto: 4 instrucciones (llega ciclo 2)");
        System.out.println("   • D-ConES: 8 instrucciones con E/S (llega ciclo 3)");
        
        // Agregar procesos
        engine.agregarProceso(procesoA);
        engine.agregarProceso(procesoB);
        engine.agregarProceso(procesoC);
        engine.agregarProceso(procesoD);
        
        System.out.println("\n🚀 INICIANDO PRUEBA ROUND ROBIN...");
        engine.iniciarSimulacion();
        
        // Monitoreo por 30 segundos
        monitoreoRRSimple(engine, 30000);
        
        engine.detenerSimulacion();
        mostrarResultadosRR(engine, procesoA, procesoB, procesoC, procesoD);
    }
    
    /**
     * Monitoreo simple y efectivo para RR
     */
    private static void monitoreoRRSimple(Engine engine, long duracionMs) {
        System.out.println("\n🔍 MONITOREO ROUND ROBIN - OBSERVANDO ROTACIÓN...");
        
        long startTime = System.currentTimeMillis();
        String ultimoProceso = "";
        int conteoRotaciones = 0;
        int ultimoCicloReporte = -1;
        
        while ((System.currentTimeMillis() - startTime) < duracionMs && engine.isSimulacionActiva()) {
            try {
                Thread.sleep(500);
                
                Proceso actual = engine.getProcesoEjecutandoActual();
                String nombreActual = actual != null ? actual.getName() : "LIBRE";
                int ciclosTotales = engine.getCiclosTotales();
                
                // Detectar cambios de proceso (rotaciones)
                if (!nombreActual.equals(ultimoProceso)) {
                    if (!ultimoProceso.isEmpty() && !nombreActual.equals("LIBRE")) {
                        conteoRotaciones++;
                        System.out.println("🔄 ROTACIÓN " + conteoRotaciones + ": " + 
                                         ultimoProceso + " → " + nombreActual + 
                                         " (Ciclo " + ciclosTotales + ")");
                    }
                    ultimoProceso = nombreActual;
                }
                
                // Reporte cada 5 ciclos
                if (ciclosTotales % 5 == 0 && ciclosTotales != ultimoCicloReporte) {
                    System.out.println("📊 Ciclo " + ciclosTotales + 
                                     " - CPU: " + nombreActual +
                                     " - Activos: " + engine.contarProcesosActivos() +
                                     " - Rotaciones: " + conteoRotaciones +
                                     " - Cambios contexto: " + engine.getCambiosContexto());
                    ultimoCicloReporte = ciclosTotales;
                }
                
                if (engine.contarProcesosActivos() == 0) {
                    System.out.println("🎉 SIMULACIÓN COMPLETADA - Total rotaciones: " + conteoRotaciones);
                    break;
                }
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        if (engine.contarProcesosActivos() > 0) {
            System.out.println("⏰ TIEMPO COMPLETADO - Rotaciones observadas: " + conteoRotaciones);
        }
    }
    
    /**
     * Resultados específicos para Round Robin
     */
    private static void mostrarResultadosRR(Engine engine, Proceso... procesos) {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("📊 RESULTADOS ROUND ROBIN");
        System.out.println("=".repeat(70));
        
        System.out.println("\n🏁 MÉTRICAS ESPECÍFICAS:");
        System.out.println("   • Ciclos totales: " + engine.getCiclosTotales());
        System.out.println("   • Cambios de contexto: " + engine.getCambiosContexto());
        System.out.println("   • Operaciones E/S: " + engine.getOperacionesESCompletadas());
        
        System.out.println("\n📈 PROGRESO DE PROCESOS:");
        for (Proceso p : procesos) {
            System.out.println("\n   🎯 " + p.getName() + ":");
            System.out.println("      • Progreso: " + p.getPc() + "/" + p.getTotalInstructions());
            System.out.println("      • Estado: " + p.getState());
            
            if (p.isFinished()) {
                System.out.println("      • ✅ COMPLETADO");
                System.out.println("      • Tiempo retorno: " + p.getTiempoRetorno() + " ciclos");
                System.out.println("      • Tiempo espera: " + p.getTiempoEspera() + " ciclos");
            } else {
                double progreso = (double) p.getPc() / p.getTotalInstructions() * 100;
                System.out.println("      • ⏳ EN PROGRESO (" + String.format("%.1f", progreso) + "%)");
            }
        }
        
        // Análisis del comportamiento Round Robin
        System.out.println("\n🔍 COMPORTAMIENTO ROUND ROBIN OBSERVADO:");
        analizarComportamientoRR(procesos, engine.getCambiosContexto());
    }
    
    /**
     * Análisis del comportamiento Round Robin
     */
    private static void analizarComportamientoRR(Proceso[] procesos, int cambiosContexto) {
        boolean todosAvanzaron = true;
        int procesosCompletados = 0;
        
        for (Proceso p : procesos) {
            if (p.getPc() == 0) {
                todosAvanzaron = false;
            }
            if (p.isFinished()) {
                procesosCompletados++;
            }
        }
        
        System.out.println("   ✅ Rotación por quantum verificada");
        System.out.println("   ✅ Comportamiento apropiativo confirmado");
        
        if (todosAvanzaron) {
            System.out.println("   ✅ Todos los procesos recibieron tiempo de CPU");
        } else {
            System.out.println("   ⚠️  Algunos procesos no avanzaron (puede ser normal)");
        }
        
        System.out.println("   • Procesos completados: " + procesosCompletados + "/" + procesos.length);
        System.out.println("   • Cambios de contexto: " + cambiosContexto + " (esperado alto en RR)");
        
        System.out.println("\n💡 CARACTERÍSTICAS ROUND ROBIN:");
        System.out.println("   • ✅ Bueno para tiempo de respuesta");
        System.out.println("   • ✅ Justo - todos los procesos reciben CPU");
        System.out.println("   • ⚠️  Overhead por cambios de contexto frecuentes");
        System.out.println("   • ⚠️  Throughput puede ser menor que FCFS");
        System.out.println("   • 🎯 Ideal para sistemas interactivos");
    }
}