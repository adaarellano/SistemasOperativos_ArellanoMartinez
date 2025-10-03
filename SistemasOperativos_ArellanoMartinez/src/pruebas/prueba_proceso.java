/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pruebas;

import sistemasoperativos_arellanomartinez.Simulador.Proceso;

/**
 *
 * @author raiza
 */
public class prueba_proceso {
     public static void main(String[] args) {
        System.out.println("=== PRUEBA DE CLASE PROCESO ===");
        
        // 1. Probar proceso CPU-bound (nunca hace E/S)
        System.out.println("\n--- Proceso CPU-bound ---");
        Proceso cpuProcess = new Proceso("Calculo", 5, true, 0, 0);
        System.out.println("Creado: " + cpuProcess);
        
        // Ejecutar algunas instrucciones
        for (int i = 0; i < 3; i++) {
            cpuProcess.ejecutarInstruccion();
            System.out.println("Ciclo " + i + ": " + cpuProcess);
        }
        
        // 2. Probar proceso I/O-bound (hace E/S cada 3 ciclos)
        System.out.println("\n--- Proceso I/O-bound ---");
        Proceso ioProcess = new Proceso("LectorArchivo", 8, false, 3, 2);
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
            }
            
            if (ioProcess.isFinished()) {
                System.out.println("✅ PROCESO TERMINADO");
                break;
            }
        }
        
        System.out.println("\n=== PRUEBA COMPLETADA ===");
    }
}

