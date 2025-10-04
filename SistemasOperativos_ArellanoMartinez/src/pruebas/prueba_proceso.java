/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pruebas;

import sistemasoperativos_arellanomartinez.Simulador.Proceso;
import sistemasoperativos_arellanomartinez.Simulador.Reloj;
/**
 *
 * @author raiza
 */
public class prueba_proceso {
    public static void main(String[] args) {
        System.out.println("=== PRUEBA DE CLASE PROCESO ===");
        
        // 🔹 INICIALIZAR EL RELOJ (importante para las métricas)
        sistemasoperativos_arellanomartinez.Simulador.Reloj.reset();
        
        // 1. Probar proceso CPU-bound (nunca hace E/S)
        System.out.println("\n--- Proceso CPU-bound ---");
        Proceso cpuProcess = new Proceso("Calculo", 5, true, 0, 0, 0);
        System.out.println("Creado: " + cpuProcess);
        System.out.println("Tiempo llegada: " + cpuProcess.getTiempoLlegada());
        
        // Ejecutar algunas instrucciones
        for (int i = 0; i < 3; i++) {
            cpuProcess.ejecutarInstruccion();
            System.out.println("Ciclo " + i + ": " + cpuProcess);
            System.out.println("Instrucciones restantes: " + cpuProcess.getInstruccionesRestantes());
        }
        
        // 2. Probar proceso I/O-bound (hace E/S cada 3 ciclos)
        System.out.println("\n--- Proceso I/O-bound ---");
        Proceso ioProcess = new Proceso("LectorArchivo", 8, false, 3, 2, 0);
        System.out.println("Creado: " + ioProcess);
        
        // Simular varios ciclos
        for (int i = 0; i < 10; i++) {
            System.out.println("\n--- Ciclo " + i + " ---");
            
            // Verificar si debe generar E/S
            if (ioProcess.debeGenerarES() && !ioProcess.estaEnES()) {
                System.out.println("¡GENERANDO E/S!");
                ioProcess.generarES();
            }
            
            // Procesar E/S si está en una
            if (ioProcess.estaEnES()) {
                System.out.println("Procesando E/S... (" + 
                    ioProcess.getTiempoESRestante() + " ciclos restantes)");
                ioProcess.procesarCicloES();
            } else if (!ioProcess.isFinished()) {
                // Ejecutar instrucción normal
                ioProcess.ejecutarInstruccion();
                System.out.println("Ejecutando: " + ioProcess);
                System.out.println("Próxima E/S en instrucción: " + ioProcess.getPc() + "/" + ioProcess.getProximaExcepcionES());
            }
            
            if (ioProcess.isFinished()) {
                System.out.println("✅ PROCESO TERMINADO");
                System.out.println("⏱️  Tiempo de retorno: " + ioProcess.getTiempoRetorno() + " ciclos");
                break;
            }
        }
        
        // 3. Probar las nuevas métricas
        System.out.println("\n--- Prueba de Métricas ---");
        Proceso testProcess = new Proceso("TestMetricas", 4, true, 0, 0, 0);
        testProcess.setTiempoInicioEjecucion(2);
        testProcess.setTiempoFinalizacion(6);
        
        System.out.println("Proceso: " + testProcess);
        System.out.println("Tiempo de espera: " + testProcess.getTiempoEspera() + " ciclos");
        System.out.println("Tiempo de retorno: " + testProcess.getTiempoRetorno() + " ciclos");
        
        System.out.println("\n=== PRUEBA COMPLETADA ===");
    }
}

