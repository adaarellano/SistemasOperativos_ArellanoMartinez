/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pruebas;
import sistemasoperativos_arellanomartinez.Planificador.Random;
import sistemasoperativos_arellanomartinez.Simulador.Proceso;
import sistemasoperativos_arellanomartinez.Simulador.Reloj;

public class PruebaRandom {
    public static void main(String[] args) {
        System.out.println("🧪 INICIANDO PRUEBA RANDOM");
        
        Random random = new Random();
        Reloj.reset();
        
        // Crear procesos de prueba
        Proceso p1 = new Proceso("Word", 5, true, 10, 2, 0);
        Proceso p2 = new Proceso("Excel", 3, false, 5, 2, 0);
        Proceso p3 = new Proceso("Navegador", 4, true, 8, 2, 0);
        
        // Agregar procesos
        random.agregarProceso(p1);
        random.agregarProceso(p2);
        random.agregarProceso(p3);
        
        // Simular algunos ciclos
        for (int ciclo = 1; ciclo <= 10; ciclo++) {
            System.out.println("\n=== CICLO " + ciclo + " ===");
            Reloj.tick();
            
            Proceso ejecutando = random.siguienteProceso();
            if (ejecutando != null) {
                System.out.println("✅ Ejecutando: " + ejecutando.getName());
                ejecutando.ejecutarInstruccion();
                
                // Simular E/S ocasionalmente
                if (ciclo % 3 == 0 && ejecutando.debeGenerarES()) {
                    System.out.println("🔄 " + ejecutando.getName() + " genera E/S");
                    ejecutando.generarES();
                }
            } else {
                System.out.println("💤 No hay proceso para ejecutar");
            }
            
            random.actualizarCiclo(ciclo);
            random.mostrarEstadoCola();
        }
        
        System.out.println("\n🎉 PRUEBA RANDOM COMPLETADA");
    }
}