/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pruebas;
import sistemasoperativos_arellanomartinez.Planificador.SPN;
import sistemasoperativos_arellanomartinez.Simulador.Proceso;
import sistemasoperativos_arellanomartinez.Simulador.Reloj;

   
/**
 * Prueba del algoritmo SPN (Shortest Process Next)
 */
public class PruebaSPN {

    
    public static void main(String[] args) {
        System.out.println("🚀 INICIANDO PRUEBAS DEL ALGORITMO SPN");
        System.out.println("======================================\n");
        
        // Configurar reloj
        Reloj.inicializar(0);
        
        // Prueba 1: SPN básico - selecciona el proceso más corto
        pruebaSPNBasico();
        
        // Prueba 2: SPN no apropiativo - no interrumpe procesos
        pruebaSPNNoApropiativo();
        
        // Prueba 3: SPN con E/S
        pruebaSPNConES();
        
        System.out.println("\n✅ TODAS LAS PRUEBAS SPN COMPLETADAS");
    }
    
    /**
     * Prueba 1: SPN básico - debe seleccionar siempre el proceso más corto
     */
    private static void pruebaSPNBasico() {
        System.out.println("📋 PRUEBA 1: SPN BÁSICO (Selección del más corto)");
        System.out.println("-------------------------------------------------");
        
        SPN planificador = new SPN();
        
        // Crear procesos con diferentes longitudes TOTALES
        Proceso p1 = new Proceso("P1-Largo", 10, true, 0, 0, 1);
        Proceso p2 = new Proceso("P2-Medio", 6, true, 0, 0, 1);
        Proceso p3 = new Proceso("P3-Corto", 3, true, 0, 0, 1);
        
        // Agregar en orden inverso para verificar que SPN los reordena
        planificador.agregarProceso(p1); // Largo primero
        planificador.agregarProceso(p2); // Luego medio
        planificador.agregarProceso(p3); // Finalmente corto
        
        System.out.println("\n🔄 Simulando 12 ciclos...");
        
        // Simular ejecución
        for (int ciclo = 1; ciclo <= 12; ciclo++) {
            Reloj.tick();
            System.out.println("\n--- Ciclo " + ciclo + " ---");
            
            Proceso seleccionado = planificador.seleccionarProximoProceso();
            
            if (seleccionado != null) {
                // Simular ejecución manual
                ejecutarInstruccionSPN(seleccionado);
                
                System.out.println("Ejecutando: " + seleccionado.getName() + 
                                 " | PC: " + seleccionado.getPc() + "/" + seleccionado.getTotalInstructions());
            }
            
            // Mostrar estado cada 4 ciclos
            if (ciclo % 4 == 0) {
                System.out.println("\n" + planificador.getEstadoColas());
            }
        }
        
        System.out.println("\n📊 RESULTADO PRUEBA 1:");
        System.out.println("- P3-Corto (más corto) debería ejecutarse PRIMERO y COMPLETARSE");
        System.out.println("- SPN debe ser NO APROPITATIVO");
        System.out.println("- Cambios de contexto: " + planificador.getCambiosContexto());
        System.out.println("- Procesos completados: " + planificador.getProcesosCompletados());
        System.out.println("✅ PRUEBA 1 COMPLETADA\n");
    }
    
    /**
     * Prueba 2: Verificar que SPN es NO apropiativo
     */
    private static void pruebaSPNNoApropiativo() {
        System.out.println("🚫 PRUEBA 2: SPN NO APROPITATIVO");
        System.out.println("--------------------------------");
        
        SPN planificador = new SPN();
        
        // Crear proceso largo primero
        Proceso pLargo = new Proceso("P-Largo", 8, true, 0, 0, 1);
        planificador.agregarProceso(pLargo);
        
        System.out.println("\n🔶 FASE 1: Ejecutando proceso largo...");
        
        // Ejecutar proceso largo por 3 ciclos
        for (int ciclo = 1; ciclo <= 3; ciclo++) {
            Reloj.tick();
            System.out.println("\n--- Ciclo " + ciclo + " ---");
            
            Proceso seleccionado = planificador.seleccionarProximoProceso();
            if (seleccionado != null) {
                ejecutarInstruccionSPN(seleccionado);
                System.out.println("Ejecutando: " + seleccionado.getName());
            }
        }
        
        // Agregar proceso más corto DURANTE la ejecución
        System.out.println("\n⚠️  AGREGANDO PROCESO MÁS CORTO durante ejecución...");
        Proceso pCorto = new Proceso("P-Corto", 3, true, 0, 0, 4);
        planificador.agregarProceso(pCorto);
        
        System.out.println("\n🔶 FASE 2: Verificando NO apropiatividad...");
        
        // Continuar ejecución - SPN NO debe desalojar al proceso actual
        for (int ciclo = 4; ciclo <= 12; ciclo++) {
            Reloj.tick();
            System.out.println("\n--- Ciclo " + ciclo + " ---");
            
            Proceso seleccionado = planificador.seleccionarProximoProceso();
            if (seleccionado != null) {
                ejecutarInstruccionSPN(seleccionado);
                System.out.println("Ejecutando: " + seleccionado.getName() + 
                                 " | PC: " + seleccionado.getPc() + "/" + seleccionado.getTotalInstructions());
                
                if (seleccionado.isFinished()) {
                    System.out.println("🎉 " + seleccionado.getName() + " TERMINADO!");
                }
            }
            
            if (ciclo == 6 || ciclo == 9) {
                System.out.println("\n" + planificador.getEstadoColas());
            }
        }
        
        System.out.println("\n📊 RESULTADO PRUEBA 2:");
        System.out.println("- SPN NO debe desalojar P-Largo cuando llega P-Corto");
        System.out.println("- P-Largo debe completarse primero (no apropiativo)");
        System.out.println("- Luego P-Corto debe ejecutarse");
        System.out.println("✅ PRUEBA 2 COMPLETADA\n");
    }
    
    /**
     * Prueba 3: SPN con procesos E/S
     */
    private static void pruebaSPNConES() {
        System.out.println("⏱️  PRUEBA 3: SPN CON OPERACIONES E/S");
        System.out.println("-------------------------------------");
        
        SPN planificador = new SPN();
        
        // Crear mix de procesos
        Proceso pCPU = new Proceso("P-CPU", 6, true, 0, 0, 1);
        Proceso pIO = new Proceso("P-IO", 8, false, 3, 2, 1); // E/S cada 3 ciclos, dura 2
        
        planificador.agregarProceso(pCPU);
        planificador.agregarProceso(pIO);
        
        System.out.println("Procesos creados:");
        System.out.println("- " + pCPU.getName() + " (CPU Bound)");
        System.out.println("- " + pIO.getName() + " (I/O Bound - E/S cada 3 ciclos)");
        
        System.out.println("\n🔄 Simulando 15 ciclos con E/S...");
        
        for (int ciclo = 1; ciclo <= 15; ciclo++) {
            Reloj.tick();
            System.out.println("\n--- Ciclo " + ciclo + " ---");
            
            // Verificar E/S para P-IO
            if (pIO.getPc() > 0 && pIO.getPc() % 3 == 0 && !pIO.estaEnES()) {
                System.out.println("🔄 " + pIO.getName() + " solicita E/S");
                pIO.generarES();
                if (planificador.getProcesoEjecutando() == pIO) {
                    planificador.procesoBloqueado(pIO);
                }
            }
            
            // Procesar E/S si está en curso
            if (pIO.estaEnES()) {
                pIO.procesarCicloES();
                System.out.println(pIO.getName() + " en E/S - tiempo restante: " + pIO.getTiempoESRestante());
                
                if (!pIO.estaEnES() && pIO.getState() == Proceso.Estado.LISTO) {
                    System.out.println("🔄 " + pIO.getName() + " vuelve de E/S");
                    planificador.procesoVolvioDeES(pIO);
                }
            }
            
            Proceso seleccionado = planificador.seleccionarProximoProceso();
            
            if (seleccionado != null && !seleccionado.estaEnES()) {
                ejecutarInstruccionSPN(seleccionado);
                
                System.out.println("Ejecutando: " + seleccionado.getName() + 
                                 " | PC: " + seleccionado.getPc() + "/" + seleccionado.getTotalInstructions() +
                                 " | E/S: " + (seleccionado.estaEnES() ? "SÍ" : "NO"));
            }
            
            if (ciclo % 5 == 0) {
                System.out.println("\n" + planificador.getEstadoColas());
            }
        }
        
        System.out.println("\n📊 RESULTADO PRUEBA 3:");
        System.out.println("- SPN debe manejar correctamente los bloqueos por E/S");
        System.out.println("- Cuando un proceso va a E/S, SPN selecciona otro proceso");
        System.out.println("✅ PRUEBA 3 COMPLETADA\n");
    }
    
    /**
     * Simula la ejecución de una instrucción en SPN
     */
    private static void ejecutarInstruccionSPN(Proceso proceso) {
        if (proceso.getPc() < proceso.getTotalInstructions()) {
            // Avanzar manualmente el PC
            try {
                java.lang.reflect.Field field = Proceso.class.getDeclaredField("pc");
                field.setAccessible(true);
                int pcActual = (int) field.get(proceso);
                field.set(proceso, pcActual + 1);
                
                // Verificar si terminó
                if (proceso.getPc() >= proceso.getTotalInstructions()) {
                    proceso.setState(Proceso.Estado.TERMINADO);
                    proceso.setTiempoFinalizacion(Reloj.getCurrentCycle());
                }
            } catch (Exception e) {
                System.out.println("⚠️  No se pudo avanzar PC para " + proceso.getName());
            }
        }
    }
}
    

