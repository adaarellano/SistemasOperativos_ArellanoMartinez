/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pruebas;
import sistemasoperativos_arellanomartinez.Planificador.FCFS;
import sistemasoperativos_arellanomartinez.Simulador.Proceso;
import sistemasoperativos_arellanomartinez.Simulador.Reloj;


/**
 *
 * @author Indatech
 */
public class PruebaFCFS {
     public static void main(String[] args) {
        System.out.println("🎯 SIMULADOR DE SISTEMA OPERATIVO - FCFS");
        System.out.println("🔍 Prueba por Terminal con Reloj Integrado");
        System.out.println("⏰ Duración del ciclo: " + Reloj.getCycleDurationMs() + "ms");
        System.out.println("=" .repeat(60));
        
        // 🔹 CREAR PLANIFICADOR FCFS
        FCFS planificador = new FCFS();
        
        // 🔹 CREAR PROCESOS DE PRUEBA
        System.out.println("\n📦 CREANDO PROCESOS:");
        System.out.println("-".repeat(40));
        
        // Proceso 1: Editor de texto (I/O-bound, hace E/S cada 2 ciclos)
        Proceso p1 = new Proceso("EditorTexto", 6, false, 2, 2, 0);
        
        // Proceso 2: Calculadora (CPU-bound, sin E/S)
        Proceso p2 = new Proceso("Calculadora", 4, true, 0, 0, 1);
        
        // Proceso 3: Navegador (I/O-bound, hace E/S cada 3 ciclos)
        Proceso p3 = new Proceso("NavegadorWeb", 5, false, 3, 1, 2);
        
        System.out.println("✅ " + p1.getId() + " - " + p1.getName() + 
                         " (I/O-bound, E/S cada 2 ciclos)");
        System.out.println("✅ " + p2.getId() + " - " + p2.getName() + 
                         " (CPU-bound, sin E/S)");
        System.out.println("✅ " + p3.getId() + " - " + p3.getName() + 
                         " (I/O-bound, E/S cada 3 ciclos)");
        
        // 🔹 AGREGAR PROCESOS AL PLANIFICADOR
        planificador.agregarProceso(p1);
        planificador.agregarProceso(p2);
        planificador.agregarProceso(p3);
        
        System.out.println("\n🚀 INICIANDO SIMULACIÓN FCFS");
        System.out.println("=" .repeat(60));
        
        // 🔹 SIMULACIÓN POR 25 CICLOS MÁXIMO
        while (Reloj.getCurrentCycle() < 25 && planificador.tieneProcesos()) {
            int cicloActual = Reloj.getCurrentCycle();
            System.out.println("\n⏰ === CICLO " + cicloActual + " ===");
            
            // Obtener siguiente proceso a ejecutar
            Proceso procesoActual = planificador.siguienteProceso();
            
            if (procesoActual != null) {
                System.out.println("🖥️  CPU: " + procesoActual.getId() + " - " + 
                                 procesoActual.getName());
                System.out.println("   📍 Estado: " + procesoActual.getState() + 
                                 " | PC: " + procesoActual.getPc() + "/" + 
                                 procesoActual.getTotalInstructions());
                
                // 🔹 VERIFICAR SI GENERA E/S
                if (procesoActual.debeGenerarES() && !procesoActual.estaEnES()) {
                    System.out.println("   🔄 GENERANDO SOLICITUD E/S!");
                    procesoActual.generarES();
                    planificador.eliminarProceso(procesoActual); // Sacar de CPU
                    planificador.agregarProceso(procesoActual);  // Volver a cola
                    System.out.println("   ⏸️  " + procesoActual.getId() + " BLOQUEADO por E/S");
                }
                
                // 🔹 PROCESAR E/S SI ESTÁ BLOQUEADO
                else if (procesoActual.estaEnES()) {
                    System.out.println("   💾 PROCESANDO E/S (" + 
                                     procesoActual.getTiempoESRestante() + " ciclos restantes)");
                    procesoActual.procesarCicloES();
                    
                    // Si terminó la E/S, volver a cola de listos
                    if (!procesoActual.estaEnES() && !procesoActual.isFinished()) {
                        procesoActual.setState(Proceso.Estado.LISTO);
                        planificador.agregarProceso(procesoActual);
                        System.out.println("   ✅ E/S COMPLETADA - Volviendo a cola");
                    }
                }
                
                // 🔹 EJECUTAR INSTRUCCIÓN NORMAL
                else if (!procesoActual.isFinished()) {
                    procesoActual.ejecutarInstruccion();
                    System.out.println("   ⚡ EJECUTANDO INSTRUCCIÓN → PC: " + 
                                     procesoActual.getPc() + "/" + 
                                     procesoActual.getTotalInstructions());
                    
                    // Verificar si terminó
                    if (procesoActual.isFinished()) {
                        procesoActual.setTiempoFinalizacion(Reloj.getCurrentCycle());
                        System.out.println("   🎉 " + procesoActual.getId() + " TERMINADO!");
                        System.out.println("   ⏱️  Tiempo de retorno: " + 
                                         procesoActual.getTiempoRetorno() + " ciclos");
                    }
                }
            } else {
                System.out.println("💤 CPU INACTIVA - No hay procesos listos");
            }
            
            // 🔹 MOSTRAR ESTADO DEL SISTEMA
            mostrarEstadoSistema(planificador, cicloActual);
            
            // 🔹 AVANZAR EL RELOJ
            Reloj.tick();
            
            // Pausa para visualización (usa la duración configurada del reloj)
            try { 
                Thread.sleep(Reloj.getCycleDurationMs()); 
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        // 🔹 MOSTRAR MÉTRICAS FINALES
        System.out.println("\n" + "=" .repeat(60));
        System.out.println("📊 SIMULACIÓN COMPLETADA - MÉTRICAS FINALES");
        System.out.println("-".repeat(40));
        mostrarMetricasFinales(p1, p2, p3, Reloj.getCurrentCycle());
        
        // 🔹 RESETEAR RELOJ PARA FUTURAS PRUEBAS
        Reloj.reset();
    }
    
    private static void mostrarEstadoSistema(FCFS planificador, int ciclo) {
        System.out.println("📊 --- ESTADO DEL SISTEMA ---");
        System.out.println("   🕒 Ciclo: " + ciclo);
        System.out.println("   📋 Algoritmo: " + planificador.getNombre());
        
        Proceso enCPU = planificador.getProcesoEjecutando();
        System.out.println("   🖥️  Proceso en CPU: " + 
                         (enCPU != null ? enCPU.getId() : "Ninguno"));
        
        System.out.println("   📈 " + planificador.getEstadoCola());
        System.out.println("   🔄 Procesos activos: " + 
                         (planificador.tieneProcesos() ? "Sí" : "No"));
    }
    
    private static void mostrarMetricasFinales(Proceso p1, Proceso p2, Proceso p3, int ciclosTotales) {
        Proceso[] procesos = {p1, p2, p3};
        int completados = 0;
        int totalEspera = 0;
        int totalRetorno = 0;
        
        System.out.println("📈 MÉTRICAS POR PROCESO:");
        for (Proceso p : procesos) {
            System.out.println("\n   " + p.getId() + " - " + p.getName() + ":");
            System.out.println("     📍 Estado: " + p.getState());
            System.out.println("     🔢 Progreso: " + p.getPc() + "/" + p.getTotalInstructions());
            
            if (p.isFinished()) {
                completados++;
                totalEspera += p.getTiempoEspera();
                totalRetorno += p.getTiempoRetorno();
                
                System.out.println("     ⏱️  Tiempo espera: " + p.getTiempoEspera() + " ciclos");
                System.out.println("     ⏰ Tiempo retorno: " + p.getTiempoRetorno() + " ciclos");
            } else {
                System.out.println("     ❌ No completado");
            }
        }
        
        System.out.println("\n📊 MÉTRICAS GLOBALES:");
        System.out.println("   🔢 Ciclos totales: " + ciclosTotales);
        System.out.println("   ✅ Procesos completados: " + completados + "/3");
        
        if (completados > 0) {
            System.out.println("   📊 Tiempo espera promedio: " + (totalEspera / completados) + " ciclos");
            System.out.println("   📈 Tiempo retorno promedio: " + (totalRetorno / completados) + " ciclos");
            System.out.println("   🚀 Throughput: " + 
                             String.format("%.2f", (double) completados / ciclosTotales) + 
                             " procesos/ciclo");
        }
        
        System.out.println("\n⏰ Configuración del reloj:");
        System.out.println("   🕒 Duración del ciclo: " + Reloj.getCycleDurationMs() + "ms");
    }
}
