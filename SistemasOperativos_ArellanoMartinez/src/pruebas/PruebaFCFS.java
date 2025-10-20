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
 * Prueba exhaustiva del algoritmo FCFS
 */
public class PruebaFCFS {
    
    public static void main(String[] args) {
        System.out.println("🚀 INICIANDO PRUEBA FCFS COMPLETA");
        System.out.println("=================================\n");
        
        // Configurar velocidad de simulación
        Reloj.setCycleDurationMs(500); // 500ms por ciclo para mejor visualización
        
        // Crear planificador FCFS
        FCFS planificador = new FCFS();
        Engine engine = new Engine(planificador);
        
        // 🔹 PRIMERA PRUEBA: Proceso simple
        System.out.println("\n📋 PRUEBA 1: Proceso único sin E/S");
        pruebaProcesoSinES(engine);
        
        // Esperar entre pruebas
        try { Thread.sleep(2000); } catch (InterruptedException e) {}
        
        // 🔹 SEGUNDA PRUEBA: Múltiples procesos
        System.out.println("\n📋 PRUEBA 2: Múltiples procesos con E/S");
        pruebaMultiplesProcesos(engine);
        
        // 🔹 TERCERA PRUEBA: Procesos CPU-bound vs I/O-bound
        System.out.println("\n📋 PRUEBA 3: Mezcla de CPU-bound e I/O-bound");
        pruebaMezclaProcesos(engine);
        
        System.out.println("\n✅ TODAS LAS PRUEBAS FCFS COMPLETADAS");
    }
    
    /**
     * PRUEBA 1: Proceso simple sin E/S
     */
    private static void pruebaProcesoSinES(Engine engine) {
        System.out.println("🎯 Creando proceso 'Editor' (CPU-bound, 5 instrucciones)");
        
        Proceso editor = new Proceso(
            "Editor",           // nombre
            5,                  // total instrucciones
            true,               // CPU-bound (sin E/S)
            0,                  // ciclos para excepción E/S
            0,                  // duración E/S
            0                   // tiempo llegada
        );
        
        engine.agregarProceso(editor);
        engine.iniciarSimulacion();
        
        // Esperar a que termine la simulación
        esperarTerminacion(engine, 10000);
        engine.detenerSimulacion();
        
        mostrarMetricasProceso(editor, "Editor");
    }
    
    /**
     * PRUEBA 2: Múltiples procesos con E/S
     */
    private static void pruebaMultiplesProcesos(Engine engine) {
        System.out.println("🎯 Creando 3 procesos con diferentes características:");
        
        // Proceso 1: Navegador (I/O-bound)
        Proceso navegador = new Proceso(
            "Navegador",        // nombre
            8,                  // total instrucciones
            false,              // I/O-bound
            3,                  // cada 3 ciclos genera E/S
            2,                  // E/S dura 2 ciclos
            0                   // tiempo llegada
        );
        
        // Proceso 2: Compilador (CPU-bound)
        Proceso compilador = new Proceso(
            "Compilador",       // nombre
            6,                  // total instrucciones
            true,               // CPU-bound
            0,                  // sin E/S
            0,                  // sin E/S
            1                   // llega en ciclo 1
        );
        
        // Proceso 3: Reproductor (I/O-bound)
        Proceso reproductor = new Proceso(
            "Reproductor",      // nombre
            10,                 // total instrucciones
            false,              // I/O-bound
            4,                  // cada 4 ciclos genera E/S
            1,                  // E/S dura 1 ciclo
            2                   // llega en ciclo 2
        );
        
        engine.agregarProceso(navegador);
        
        // Iniciar simulación y agregar procesos en diferentes momentos
        engine.iniciarSimulacion();
        
        // Agregar compilador después de 3 ciclos
        new Thread(() -> {
            try {
                Thread.sleep(1500); // 3 ciclos * 500ms
                System.out.println("\n⏰ Agregando Compilador en ciclo 3...");
                engine.agregarProceso(compilador);
                
                Thread.sleep(1000); // 2 ciclos más
                System.out.println("⏰ Agregando Reproductor en ciclo 5...");
                engine.agregarProceso(reproductor);
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
        
        // Esperar a que terminen todos los procesos
        esperarTerminacionMultiples(engine, 20000);
        engine.detenerSimulacion();
        
        // Mostrar métricas de todos los procesos
        System.out.println("\n📊 MÉTRICAS FINALES - PRUEBA 2:");
        mostrarMetricasProceso(navegador, "Navegador");
        mostrarMetricasProceso(compilador, "Compilador");
        mostrarMetricasProceso(reproductor, "Reproductor");
        
        mostrarMetricasEngine(engine);
    }
    
    /**
     * PRUEBA 3: Mezcla de procesos CPU-bound e I/O-bound
     */
    private static void pruebaMezclaProcesos(Engine engine) {
        System.out.println("🎯 Creando mezcla de procesos:");
        
        // Proceso CPU-bound largo
        Proceso render = new Proceso(
            "Render3D",         // nombre
            12,                 // total instrucciones
            true,               // CPU-bound
            0,                  // sin E/S
            0,                  // sin E/S
            0                   // tiempo llegada
        );
        
        // Proceso I/O-bound corto
        Proceso chat = new Proceso(
            "Chat",             // nombre
            6,                  // total instrucciones
            false,              // I/O-bound
            2,                  // cada 2 ciclos genera E/S
            1,                  // E/S dura 1 ciclo
            1                   // llega en ciclo 1
        );
        
        // Proceso mixto
        Proceso juego = new Proceso(
            "Juego",            // nombre
            15,                 // total instrucciones
            false,              // I/O-bound
            5,                  // cada 5 ciclos genera E/S
            2,                  // E/S dura 2 ciclos
            2                   // llega en ciclo 2
        );
        
        engine.agregarProceso(render);
        engine.iniciarSimulacion();
        
        // Agregar procesos en diferentes momentos
        new Thread(() -> {
            try {
                Thread.sleep(1000); // 2 ciclos
                System.out.println("\n⏰ Agregando Chat en ciclo 2...");
                engine.agregarProceso(chat);
                
                Thread.sleep(1500); // 3 ciclos más
                System.out.println("⏰ Agregando Juego en ciclo 5...");
                engine.agregarProceso(juego);
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
        
        // Esperar a que terminen
        esperarTerminacionMultiples(engine, 25000);
        engine.detenerSimulacion();
        
        System.out.println("\n📊 MÉTRICAS FINALES - PRUEBA 3:");
        mostrarMetricasProceso(render, "Render3D");
        mostrarMetricasProceso(chat, "Chat");
        mostrarMetricasProceso(juego, "Juego");
        
        mostrarMetricasEngine(engine);
        
        // Análisis del comportamiento FCFS
        System.out.println("\n📈 ANÁLISIS COMPORTAMIENTO FCFS:");
        System.out.println("• FCFS es NO apropiativo: una vez que un proceso toma la CPU,");
        System.out.println("  la mantiene hasta terminar o ir a E/S");
        System.out.println("• Los procesos se ejecutan en orden de llegada");
        System.out.println("• Puede causar alto tiempo de espera para procesos cortos");
        System.out.println("• Simple pero puede no ser óptimo para tiempos de respuesta");
    }
    
    /**
     * Espera hasta que un proceso termine o timeout
     */
    private static void esperarTerminacion(Engine engine, long timeoutMs) {
        long startTime = System.currentTimeMillis();
        while (engine.isSimulacionActiva() && 
               (System.currentTimeMillis() - startTime) < timeoutMs) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
    
    /**
     * Espera hasta que todos los procesos terminen o timeout
     */
    private static void esperarTerminacionMultiples(Engine engine, long timeoutMs) {
        long startTime = System.currentTimeMillis();
        while (engine.isSimulacionActiva() && 
               engine.contarProcesosActivos() > 0 &&
               (System.currentTimeMillis() - startTime) < timeoutMs) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
    
    /**
     * Muestra métricas de un proceso individual
     */
    private static void mostrarMetricasProceso(Proceso proceso, String nombre) {
        System.out.println("\n📈 " + nombre + ":");
        System.out.println("   • Instrucciones: " + proceso.getPc() + "/" + proceso.getTotalInstructions());
        System.out.println("   • Tiempo llegada: " + proceso.getTiempoLlegada());
        System.out.println("   • Tiempo inicio: " + proceso.getTiempoInicioEjecucion());
        System.out.println("   • Tiempo finalización: " + proceso.getTiempoFinalizacion());
        System.out.println("   • Tiempo de retorno: " + proceso.getTiempoRetorno());
        System.out.println("   • Tiempo de espera: " + proceso.getTiempoEspera());
        System.out.println("   • Tiempo ejecución total: " + proceso.getTiempoEjecucionTotal());
        System.out.println("   • Estado final: " + proceso.getState());
    }
    
    /**
     * Muestra métricas del engine
     */
    private static void mostrarMetricasEngine(Engine engine) {
        System.out.println("\n🏁 MÉTRICAS DEL ENGINE:");
        System.out.println("   • Ciclos totales: " + engine.getCiclosTotales());
        System.out.println("   • Cambios de contexto: " + engine.getCambiosContexto());
        System.out.println("   • Operaciones E/S completadas: " + engine.getOperacionesESCompletadas());
        System.out.println("   • Procesos suspendidos: " + engine.getProcesosSuspendidos());
        System.out.println("   • Estado simulación: " + engine.getEstadoSimulacion());
    }
}