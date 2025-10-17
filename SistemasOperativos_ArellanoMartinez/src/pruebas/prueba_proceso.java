/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pruebas;

import sistemasoperativos_arellanomartinez.Simulador.Proceso;
import sistemasoperativos_arellanomartinez.Simulador.Reloj;

/**
 * Prueba completa de procesos con hilos internos
 * @author raiza
 */
public class prueba_proceso {
    public static void main(String[] args) {
        System.out.println("🎯 === PRUEBA DE PROCESOS CON HILOS INTERNOS ===\n");
        
        // Configurar velocidad de simulación (más lento para ver mejor)
        Reloj.setCycleDurationMs(800);
        
        // 🔹 CREAR PROCESOS DE DIFERENTES TIPOS
        System.out.println("📦 CREANDO PROCESOS...");
        
        // Proceso CPU-bound (sin E/S)
        Proceso procesoCPU = new Proceso("CALCULO_PESADO", 6, true, 0, 0, 0);
        
        // Proceso I/O-bound (con E/S frecuente)
        Proceso procesoIO = new Proceso("LECTOR_ARCHIVOS", 8, false, 2, 3, 0);
        
        // Proceso mixto
        Proceso procesoMixto = new Proceso("NAVEGADOR_WEB", 10, false, 3, 2, 0);
        
        System.out.println("✅ Procesos creados exitosamente");
        
        // 🔹 MOSTRAR INFORMACIÓN INICIAL
        System.out.println("\n📊 INFORMACIÓN INICIAL DE PROCESOS:");
        mostrarInfoProceso(procesoCPU, "CPU-bound");
        mostrarInfoProceso(procesoIO, "I/O-bound");  
        mostrarInfoProceso(procesoMixto, "Mixto");
        
        // 🔹 PRUEBA 1: EJECUCIÓN SECUENCIAL
        System.out.println("\n" + "=" .repeat(50));
        System.out.println("🔹 PRUEBA 1: EJECUCIÓN SECUENCIAL");
        System.out.println("=" .repeat(50));
        
        pruebaEjecucionSecuencial(procesoCPU, procesoIO, procesoMixto);
        
        // 🔹 PRUEBA 2: EJECUCIÓN CONCURRENTE  
        System.out.println("\n" + "=" .repeat(50));
        System.out.println("🔹 PRUEBA 2: EJECUCIÓN CONCURRENTE");
        System.out.println("=" .repeat(50));
        
        pruebaEjecucionConcurrente(procesoCPU, procesoIO, procesoMixto);
        
        // 🔹 PRUEBA 3: CONTROL AVANZADO (PAUSA/REANUDACIÓN)
        System.out.println("\n" + "=" .repeat(50));
        System.out.println("🔹 PRUEBA 3: CONTROL AVANZADO");
        System.out.println("=" .repeat(50));
        
        pruebaControlAvanzado();
        
        System.out.println("\n🎉 === PRUEBA COMPLETADA EXITOSAMENTE ===");
    }
    
    /**
     * 🔹 PRUEBA 1: Ejecución secuencial de procesos
     */
    private static void pruebaEjecucionSecuencial(Proceso p1, Proceso p2, Proceso p3) {
        System.out.println("🚀 Iniciando ejecución SECUENCIAL...");
        
        try {
            // Ejecutar proceso 1
            System.out.println("\n--- Ejecutando " + p1.getName() + " ---");
            p1.iniciarEjecucion();
            Thread.sleep(3000); // Esperar 3 segundos
            p1.pausarEjecucion();
            mostrarProgreso(p1);
            
            // Ejecutar proceso 2  
            System.out.println("\n--- Ejecutando " + p2.getName() + " ---");
            p2.iniciarEjecucion();
            Thread.sleep(3000);
            p2.pausarEjecucion();
            mostrarProgreso(p2);
            
            // Ejecutar proceso 3
            System.out.println("\n--- Ejecutando " + p3.getName() + " ---");
            p3.iniciarEjecucion();
            Thread.sleep(3000);
            p3.pausarEjecucion();
            mostrarProgreso(p3);
            
        } catch (InterruptedException e) {
            System.out.println("❌ Prueba interrumpida");
        }
    }
    
    /**
     * 🔹 PRUEBA 2: Ejecución concurrente de procesos
     */
    private static void pruebaEjecucionConcurrente(Proceso p1, Proceso p2, Proceso p3) {
        System.out.println("🚀 Iniciando ejecución CONCURRENTE...");
        
        try {
            // Reiniciar todos los procesos
            p1.setPc(0);
            p2.setPc(0); 
            p3.setPc(0);
            p1.setState(Proceso.Estado.LISTO);
            p2.setState(Proceso.Estado.LISTO);
            p3.setState(Proceso.Estado.LISTO);
            
            // Iniciar los 3 procesos AL MISMO TIEMPO
            System.out.println("🎬 Iniciando los 3 procesos concurrentemente...");
            p1.iniciarEjecucion();
            p2.iniciarEjecucion();
            p3.iniciarEjecucion();
            
            // Esperar y monitorear progreso
            for (int i = 1; i <= 5; i++) {
                Thread.sleep(2000); // Esperar 2 segundos
                System.out.println("\n--- MONITOREO " + i + " (después de " + (i * 2) + " segundos) ---");
                mostrarProgresoConcurrente(p1, p2, p3);
            }
            
            // Detener todos los procesos
            System.out.println("\n🛑 Deteniendo ejecución concurrente...");
            p1.detenerEjecucion();
            p2.detenerEjecucion();
            p3.detenerEjecucion();
            
        } catch (InterruptedException e) {
            System.out.println("❌ Prueba interrumpida");
        }
    }
    
    /**
     * 🔹 PRUEBA 3: Control avanzado (pausa/reanudación)
     */
    private static void pruebaControlAvanzado() {
        System.out.println("🚀 Probando control avanzado (pausa/reanudación)...");
        
        Proceso procesoTest = new Proceso("PROCESO_TEST", 12, false, 2, 2, 0);
        
        try {
            // Iniciar proceso
            System.out.println("▶️  Iniciando proceso...");
            procesoTest.iniciarEjecucion();
            Thread.sleep(2500);
            
            // Pausar proceso
            System.out.println("⏸️  Pausando proceso...");
            procesoTest.pausarEjecucion();
            Thread.sleep(2000);
            mostrarProgreso(procesoTest);
            
            // Reanudar proceso
            System.out.println("🔁 Reanudando proceso...");
            procesoTest.reanudarEjecucion();
            Thread.sleep(2500);
            
            // Pausar nuevamente
            System.out.println("⏸️  Pausando nuevamente...");
            procesoTest.pausarEjecucion();
            Thread.sleep(1500);
            mostrarProgreso(procesoTest);
            
            // Reanudar y dejar terminar
            System.out.println("🔁 Reanudando hasta completar...");
            procesoTest.reanudarEjecucion();
            
            // Esperar a que termine o timeout
            int espera = 0;
            while (!procesoTest.isFinished() && espera < 10) {
                Thread.sleep(1000);
                espera++;
                System.out.println("⏳ Esperando finalización... " + procesoTest.getPc() + "/" + 
                                 procesoTest.getTotalInstructions());
            }
            
            if (procesoTest.isFinished()) {
                System.out.println("✅ " + procesoTest.getName() + " COMPLETADO EXITOSAMENTE");
            } else {
                System.out.println("⏰ Timeout - deteniendo proceso");
                procesoTest.detenerEjecucion();
            }
            
            mostrarProgreso(procesoTest);
            
        } catch (InterruptedException e) {
            System.out.println("❌ Prueba interrumpida");
            procesoTest.detenerEjecucion();
        }
    }
    
    /**
     * 🔹 MUESTRA información detallada de un proceso
     */
    private static void mostrarInfoProceso(Proceso proceso, String tipo) {
        System.out.println("\n📋 " + proceso.getName() + " (" + tipo + "):");
        System.out.println("   - ID: " + proceso.getId());
        System.out.println("   - Instrucciones totales: " + proceso.getTotalInstructions());
        System.out.println("   - CPU-bound: " + proceso.isCpuBound());
        if (!proceso.isCpuBound()) {
            System.out.println("   - E/S cada: " + proceso.getInstruccionesRestantes() + " ciclos");
            System.out.println("   - Duración E/S: " + proceso.getTiempoESRestante() + " ciclos");
        }
        System.out.println("   - Estado inicial: " + proceso.getState());
        System.out.println("   - PC inicial: " + proceso.getPc());
    }
    
    /**
     * 🔹 MUESTRA progreso de un proceso individual
     */
    private static void mostrarProgreso(Proceso proceso) {
        System.out.println("📊 PROGRESO " + proceso.getName() + ":");
        System.out.println("   - PC: " + proceso.getPc() + "/" + proceso.getTotalInstructions());
        System.out.println("   - Instrucciones restantes: " + proceso.getInstruccionesRestantes());
        System.out.println("   - Estado: " + proceso.getState());
        System.out.println("   - Ejecutando: " + proceso.isEjecutando());
        System.out.println("   - En E/S: " + proceso.estaEnES());
        if (proceso.estaEnES()) {
            System.out.println("   - Tiempo E/S restante: " + proceso.getTiempoESRestante());
        }
        System.out.println("   - Terminado: " + proceso.isFinished());
    }
    
    /**
     * 🔹 MUESTRA progreso de múltiples procesos concurrentes
     */
    private static void mostrarProgresoConcurrente(Proceso p1, Proceso p2, Proceso p3) {
        System.out.println("📊 PROGRESO CONCURRENTE:");
        
        System.out.printf("   %-15s: %2d/%-2d [%s] E/S:%s Ejec:%s\n",
            p1.getName(), p1.getPc(), p1.getTotalInstructions(), 
            p1.getState(), p1.estaEnES(), p1.isEjecutando());
            
        System.out.printf("   %-15s: %2d/%-2d [%s] E/S:%s Ejec:%s\n", 
            p2.getName(), p2.getPc(), p2.getTotalInstructions(),
            p2.getState(), p2.estaEnES(), p2.isEjecutando());
            
        System.out.printf("   %-15s: %2d/%-2d [%s] E/S:%s Ejec:%s\n",
            p3.getName(), p3.getPc(), p3.getTotalInstructions(),
            p3.getState(), p3.estaEnES(), p3.isEjecutando());
        
        // Mostrar eventos de E/S si ocurren
        if (p1.estaEnES()) {
            System.out.println("   ⚠️  " + p1.getName() + " en E/S (" + p1.getTiempoESRestante() + " ciclos restantes)");
        }
        if (p2.estaEnES()) {
            System.out.println("   ⚠️  " + p2.getName() + " en E/S (" + p2.getTiempoESRestante() + " ciclos restantes)");
        }
        if (p3.estaEnES()) {
            System.out.println("   ⚠️  " + p3.getName() + " en E/S (" + p3.getTiempoESRestante() + " ciclos restantes)");
        }
    }
}