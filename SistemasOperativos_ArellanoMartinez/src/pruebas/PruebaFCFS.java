/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pruebas;

import sistemasoperativos_arellanomartinez.Controller.Engine;
import sistemasoperativos_arellanomartinez.Planificador.FCFS;
import sistemasoperativos_arellanomartinez.Simulador.Proceso;
import sistemasoperativos_arellanomartinez.Simulador.Reloj;

/**
 * Prueba DEBUG de FCFS - Versión simplificada
 */
public class PruebaFCFS {
    
    public static void main(String[] args) {
        System.out.println("🐛 INICIANDO PRUEBA DEBUG FCFS");
        System.out.println("==============================\n");
        
        try {
            // 1. CONFIGURACIÓN MÁS RÁPIDA
            Reloj.setCycleDurationMs(100); // 100ms por ciclo (más rápido)
            System.out.println("⏰ Reloj: " + Reloj.getCycleDurationMs() + "ms/ciclo");
            
            // 2. CREAR COMPONENTES
            FCFS planificador = new FCFS();
            Engine engine = new Engine(planificador);
            
            // 3. SOLO 1 PROCESO SIMPLE PARA DEBUG
            Proceso procesoSimple = new Proceso("Test", 3, true, 0, 0, 0);
            System.out.println("📦 Proceso creado: " + procesoSimple.getName() + " (3 instrucciones)");
            
            // 4. AGREGAR Y INICIAR
            engine.agregarProceso(procesoSimple);
            engine.iniciarSimulacion();
            
            System.out.println("🚀 Simulación iniciada - Esperando 3 segundos...");
            
            // 5. ESPERAR MÁS TIEMPO Y VER QUÉ PASA
            for (int i = 0; i < 30; i++) { // 30 ciclos * 100ms = 3 segundos
                Thread.sleep(100);
                System.out.println("⏰ Ciclo " + (i + 1) + " - Proceso: " + 
                    (engine.getProcesoEjecutandoActual() != null ? 
                     engine.getProcesoEjecutandoActual().getName() : "Ninguno") +
                    " - Estado: " + engine.getEstadoSimulacion());
                
                if (procesoSimple.isFinished()) {
                    System.out.println("✅ PROCESO TERMINADO EN CICLO " + (i + 1));
                    break;
                }
            }
            
            // 6. DETENER Y MOSTRAR RESULTADOS
            engine.detenerSimulacion();
            System.out.println("\n📊 RESULTADOS DEBUG:");
            System.out.println("Ciclos totales: " + engine.getCiclosTotales());
            System.out.println("Proceso terminado: " + procesoSimple.isFinished());
            System.out.println("PC final: " + procesoSimple.getPc() + "/" + procesoSimple.getTotalInstructions());
            System.out.println("Estado: " + procesoSimple.getState());
            
        } catch (Exception e) {
            System.err.println("❌ ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }
}