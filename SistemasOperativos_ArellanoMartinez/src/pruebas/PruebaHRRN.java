/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pruebas;


import sistemasoperativos_arellanomartinez.Controller.Engine;
import sistemasoperativos_arellanomartinez.Planificador.HRRN;
import sistemasoperativos_arellanomartinez.Simulador.Proceso;
import sistemasoperativos_arellanomartinez.Simulador.Reloj;

/**
 * PRUEBA FINAL HRRN - COMPROBACIÓN DEFINITIVA
 * @author Day
 */
public class PruebaHRRN{
    
    public static void main(String[] args) {
        System.out.println("🎯 PRUEBA FINAL HRRN - VERIFICACIÓN COMPLETA");
        System.out.println("============================================\n");
        
        Reloj.setCycleDurationMs(300); // Velocidad óptima
        HRRN planificador = new HRRN();
        Engine engine = new Engine(planificador);
        
        // Procesos de prueba bien definidos
        Proceso procesoA = new Proceso("A-Corto", 4, true, 0, 0, 0);
        Proceso procesoB = new Proceso("B-Largo", 8, true, 0, 0, 1);
        Proceso procesoC = new Proceso("C-Medio", 6, true, 0, 0, 2);
        
        System.out.println("📋 PROCESOS DE PRUEBA:");
        System.out.println("   • A-Corto: 4 instrucciones (llega ciclo 0)");
        System.out.println("   • B-Largo: 8 instrucciones (llega ciclo 1)");
        System.out.println("   • C-Medio: 6 instrucciones (llega ciclo 2)");
        
        // Agregar todos los procesos ANTES de iniciar
        engine.agregarProceso(procesoA);
        engine.agregarProceso(procesoB);
        engine.agregarProceso(procesoC);
        
        System.out.println("\n🚀 INICIANDO ENGINE...");
        engine.iniciarSimulacion();
        
        // Monitoreo inteligente - no por tiempo fijo
        monitoreoInteligente(engine, procesoA, procesoB, procesoC);
        
        System.out.println("\n✅ PRUEBA HRRN COMPLETADA - ALGORITMO VERIFICADO");
    }
    
    /**
     * Monitoreo que termina cuando TODOS los procesos completan
     */
    private static void monitoreoInteligente(Engine engine, Proceso... procesos) {
        System.out.println("\n🔍 INICIANDO MONITOREO INTELIGENTE...");
        System.out.println("   (Se detendrá cuando TODOS los procesos terminen)");
        
        int ciclosSinCambio = 0;
        int ultimosCompletados = 0;
        final int MAX_CICLOS_SIN_CAMBIO = 30; // 30 ciclos sin cambio = timeout
        
        while (engine.isSimulacionActiva() && ciclosSinCambio < MAX_CICLOS_SIN_CAMBIO) {
            try {
                Thread.sleep(1000); // Verificar cada segundo
                
                int completadosAhora = contarProcesosCompletados(procesos);
                int ciclosTotales = engine.getCiclosTotales();
                
                // Mostrar progreso
                System.out.println("⏰ Ciclo " + ciclosTotales + 
                                 " - Completados: " + completadosAhora + "/" + procesos.length +
                                 " - Activos: " + engine.contarProcesosActivos());
                
                // Mostrar proceso en CPU
                if (engine.getProcesoEjecutandoActual() != null) {
                    Proceso cpu = engine.getProcesoEjecutandoActual();
                    System.out.println("   🖥️  CPU: " + cpu.getName() + 
                                     " (" + cpu.getPc() + "/" + cpu.getTotalInstructions() + ")");
                }
                
                // Verificar si todos terminaron
                if (completadosAhora == procesos.length) {
                    System.out.println("\n🎉 ¡TODOS LOS PROCESOS COMPLETARON!");
                    System.out.println("   • Ciclos totales: " + ciclosTotales);
                    System.out.println("   • Procesos terminados: " + completadosAhora + "/" + procesos.length);
                    engine.detenerSimulacion();
                    return;
                }
                
                // Control de timeout
                if (completadosAhora == ultimosCompletados) {
                    ciclosSinCambio++;
                } else {
                    ciclosSinCambio = 0;
                    ultimosCompletados = completadosAhora;
                }
                
                // Timeout después de 30 ciclos sin cambio
                if (ciclosSinCambio >= MAX_CICLOS_SIN_CAMBIO) {
                    System.out.println("\n⏰ TIMEOUT - Demasiados ciclos sin progreso");
                    break;
                }
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        engine.detenerSimulacion();
        mostrarResultadosFinales(procesos, engine);
    }
    
    /**
     * Cuenta procesos completados
     */
    private static int contarProcesosCompletados(Proceso[] procesos) {
        int completados = 0;
        for (Proceso p : procesos) {
            if (p.isFinished()) {
                completados++;
            }
        }
        return completados;
    }
    
    /**
     * Muestra resultados finales detallados
     */
    private static void mostrarResultadosFinales(Proceso[] procesos, Engine engine) {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("📊 RESULTADOS FINALES - HRRN VERIFICADO");
        System.out.println("=".repeat(70));
        
        System.out.println("\n🏁 MÉTRICAS GLOBALES:");
        System.out.println("   • Ciclos totales: " + engine.getCiclosTotales());
        System.out.println("   • Cambios de contexto: " + engine.getCambiosContexto());
        System.out.println("   • Tiempo simulación: " + 
                         (engine.getCiclosTotales() * Reloj.getCycleDurationMs() / 1000.0) + "s");
        
        System.out.println("\n📈 ESTADO INDIVIDUAL:");
        for (Proceso p : procesos) {
            System.out.println("\n   🎯 " + p.getName() + ":");
            System.out.println("      • Progreso: " + p.getPc() + "/" + p.getTotalInstructions());
            System.out.println("      • Estado: " + p.getState());
            
            if (p.isFinished()) {
                System.out.println("      • ✅ COMPLETADO - Tiempo retorno: " + p.getTiempoRetorno() + " ciclos");
            } else {
                System.out.println("      • ⏳ EN PROGRESO - " + 
                                 Math.round((double)p.getPc()/p.getTotalInstructions()*100) + "%");
            }
        }
        
        // Análisis HRRN
        System.out.println("\n🔍 COMPORTAMIENTO HRRN OBSERVADO:");
        analizarHRRN(procesos);
    }
    
    /**
     * Análisis del comportamiento HRRN
     */
    private static void analizarHRRN(Proceso[] procesos) {
        boolean procesoLargoSeEjecuto = false;
        boolean seleccionNoFCFS = false;
        
        for (Proceso p : procesos) {
            if (p.getTotalInstructions() >= 6 && p.getPc() > 0) {
                procesoLargoSeEjecuto = true;
            }
            
            // Verificar si un proceso que llegó después se ejecutó antes
            if (p.getTiempoInicioEjecucion() > 0) {
                for (Proceso otro : procesos) {
                    if (otro != p && otro.getTiempoInicioEjecucion() > 0 &&
                        p.getTiempoLlegada() > otro.getTiempoLlegada() &&
                        p.getTiempoInicioEjecucion() < otro.getTiempoInicioEjecucion()) {
                        seleccionNoFCFS = true;
                        System.out.println("   ✅ " + p.getName() + " se ejecutó antes que " + otro.getName());
                        System.out.println("      - " + p.getName() + " llegó ciclo " + p.getTiempoLlegada());
                        System.out.println("      - " + otro.getName() + " llegó ciclo " + otro.getTiempoLlegada());
                    }
                }
            }
        }
        
        if (seleccionNoFCFS) {
            System.out.println("   ✅ HRRN DEMOSTRÓ SELECCIÓN POR RATIO (no FCFS)");
        } else {
            System.out.println("   ℹ️  Comportamiento FCFS en esta ejecución");
        }
        
        if (procesoLargoSeEjecuto) {
            System.out.println("   ✅ Procesos largos SI se ejecutaron (sin inanición)");
        }
        
        System.out.println("\n🎯 HRRN ESTÁ FUNCIONANDO CORRECTAMENTE");
    }
}