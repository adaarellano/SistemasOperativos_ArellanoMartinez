package pruebas;

import sistemasoperativos_arellanomartinez.Planificador.SRTF;
import sistemasoperativos_arellanomartinez.Simulador.Proceso;
import sistemasoperativos_arellanomartinez.Simulador.Reloj;

public class PruebaSRTF {
    public static void main(String[] args) {
        System.out.println("🎯 === PRUEBA SRTF CON ENTRADA/SALIDA (E/S) ===");
        
        SRTF srtf = new SRTF();
        
        // Crear procesos I/O-bound que generan E/S
        Proceso p1 = new Proceso("NAVEGADOR", 8, false, 3, 2, 0);  // E/S cada 3 ciclos
        Proceso p2 = new Proceso("EDITOR", 6, false, 2, 1, 0);     // E/S cada 2 ciclos
        Proceso p3 = new Proceso("CALCULO", 10, true, 0, 0, 0);    // CPU-bound (sin E/S)
        
        System.out.println("📦 Procesos creados (con E/S):");
        System.out.println("- " + p1.getName() + " (E/S cada " + p1.getCiclosExcepcionES() + " ciclos)");
        System.out.println("- " + p2.getName() + " (E/S cada " + p2.getCiclosExcepcionES() + " ciclos)");
        System.out.println("- " + p3.getName() + " (CPU-bound, sin E/S)");
        
        // Agregar todos los procesos al inicio
        srtf.agregarProceso(p1);
        srtf.agregarProceso(p2);
        srtf.agregarProceso(p3);
        
        System.out.println("\n--- INICIANDO SIMULACIÓN (20 ciclos) ---");
        
        for (int ciclo = 0; ciclo < 20; ciclo++) {
            System.out.println("\n--- CICLO " + ciclo + " ---");
            Reloj.tick();
            
            // 1. Seleccionar y ejecutar proceso
            Proceso actual = srtf.seleccionarProximoProceso();
            
            if (actual != null) {
                System.out.println("⚡ CPU: " + actual.getName());
                
                // Ejecutar instrucción
                actual.ejecutarInstruccion();
                System.out.println("   → PC: " + actual.getPc() + "/" + actual.getTotalInstructions() + 
                                 " | Restantes: " + actual.getInstruccionesRestantes());
                
                // 2. Verificar si debe generar E/S
                if (actual.debeGenerarES() && !actual.estaEnES()) {
                    System.out.println("🚨 " + actual.getName() + " GENERA E/S!");
                    actual.generarES();
                    srtf.procesoBloqueado(actual);
                }
                
                // 3. Verificar si terminó
                if (actual.isFinished()) {
                    System.out.println("✅ " + actual.getName() + " TERMINÓ!");
                    actual.setTiempoFinalizacion(Reloj.getCurrentCycle());
                }
            } else {
                System.out.println("💤 CPU: No hay procesos listos");
            }
            
            // 4. Procesar E/S de todos los procesos bloqueados
            procesarES(srtf, p1, p2, p3);
            
            // 5. Mostrar estado
            System.out.println("📊 " + srtf.getEstadoCompleto());
            
            if (!srtf.tieneProcesos()) {
                System.out.println("🏁 TODOS LOS PROCESOS TERMINARON");
                break;
            }
        }
        
        System.out.println("\n=== MÉTRICAS FINALES CON E/S ===");
        mostrarMetricasDetalladas(p1);
        mostrarMetricasDetalladas(p2);
        mostrarMetricasDetalladas(p3);
    }
    
    /**
     * Procesa E/S de todos los procesos
     */
    private static void procesarES(SRTF srtf, Proceso... procesos) {
        for (Proceso p : procesos) {
            if (p.estaEnES()) {
                System.out.println("⏳ " + p.getName() + " en E/S (" + 
                    p.getTiempoESRestante() + " ciclos restantes)");
                p.procesarCicloES();
                
                // Si terminó la E/S, volver a lista de listos
                if (!p.estaEnES() && !p.isFinished()) {
                    srtf.procesoVolvioDeES(p);
                }
            }
        }
    }
    
    private static void mostrarMetricasDetalladas(Proceso p) {
        System.out.println("\n" + p.getName() + ":");
        System.out.println("  - Instrucciones: " + p.getPc() + "/" + p.getTotalInstructions());
        System.out.println("  - Tiempo de espera: " + p.getTiempoEspera() + " ciclos");
        System.out.println("  - Tiempo de retorno: " + p.getTiempoRetorno() + " ciclos");
        System.out.println("  - Estado: " + p.getState());
        System.out.println("  - Tipo: " + (p.isCpuBound() ? "CPU-bound" : "I/O-bound"));
    }
}