package pruebas;

import sistemasoperativos_arellanomartinez.Planificador.SRT;
import sistemasoperativos_arellanomartinez.Simulador.Proceso;
import sistemasoperativos_arellanomartinez.Simulador.Reloj;

/**
 * Prueba SRT SUPER SIMPLIFICADA - Sin hilos complejos
 */
public class PruebaSRT {
    
    // Clase interna para procesos simplificados
    static class ProcesoSimple {
        String nombre;
        int totalInstrucciones;
        int pc;
        int tiempoLlegada;
        int tiempoInicio;
        int tiempoFin;
        
        public ProcesoSimple(String nombre, int instrucciones, int llegada) {
            this.nombre = nombre;
            this.totalInstrucciones = instrucciones;
            this.pc = 0;
            this.tiempoLlegada = llegada;
            this.tiempoInicio = -1;
            this.tiempoFin = -1;
        }
        
        public void ejecutarInstruccion() {
            if (pc < totalInstrucciones) {
                pc++;
            }
        }
        
        public boolean isFinished() {
            return pc >= totalInstrucciones;
        }
        
        public int getInstruccionesRestantes() {
            return totalInstrucciones - pc;
        }
        
        public String getInfo() {
            return String.format("%s: PC=%d/%d, Restantes=%d", 
                nombre, pc, totalInstrucciones, getInstruccionesRestantes());
        }
    }
    
    public static void main(String[] args) {
        System.out.println("🚀 PRUEBA SRT SUPER SIMPLIFICADA");
        System.out.println("===============================\n");
        
        Reloj.inicializar(0);
        
        // Crear procesos de prueba
        ProcesoSimple p1 = new ProcesoSimple("P1-Corto", 3, 1);
        ProcesoSimple p2 = new ProcesoSimple("P2-Medio", 5, 1);
        ProcesoSimple p3 = new ProcesoSimple("P3-Largo", 8, 1);
        
        ProcesoSimple[] procesos = {p1, p2, p3};
        ProcesoSimple procesoActual = null;
        
        System.out.println("Procesos de prueba:");
        for (ProcesoSimple p : procesos) {
            System.out.println("- " + p.getInfo());
        }
        
        System.out.println("\n🔄 INICIANDO SIMULACIÓN SRT");
        
        int ciclo = 1;
        int procesosTerminados = 0;
        
        while (procesosTerminados < procesos.length && ciclo <= 25) {
            System.out.println("\n--- Ciclo " + ciclo + " ---");
            
            // 1. Seleccionar proceso con menor tiempo restante
            ProcesoSimple siguiente = null;
            int menorRestante = Integer.MAX_VALUE;
            
            for (ProcesoSimple p : procesos) {
                if (!p.isFinished() && p.getInstruccionesRestantes() < menorRestante) {
                    menorRestante = p.getInstruccionesRestantes();
                    siguiente = p;
                }
            }
            
            // 2. Verificar cambio de proceso (apropiativo)
            if (siguiente != procesoActual) {
                if (procesoActual != null) {
                    System.out.println("🔄 Desalojando: " + procesoActual.nombre);
                }
                procesoActual = siguiente;
                if (procesoActual != null) {
                    System.out.println("🎯 Seleccionado: " + procesoActual.nombre);
                    if (procesoActual.tiempoInicio == -1) {
                        procesoActual.tiempoInicio = ciclo;
                    }
                }
            }
            
            // 3. Ejecutar proceso actual
            if (procesoActual != null) {
                procesoActual.ejecutarInstruccion();
                System.out.println("✓ Ejecutando: " + procesoActual.getInfo());
                
                if (procesoActual.isFinished()) {
                    procesoActual.tiempoFin = ciclo;
                    procesosTerminados++;
                    System.out.println("🎉 " + procesoActual.nombre + " TERMINADO!");
                    procesoActual = null;
                }
            } else {
                System.out.println("💤 CPU libre - no hay procesos listos");
            }
            
            ciclo++;
        }
        
        // Resultados
        System.out.println("\n📊 RESULTADOS FINALES:");
        System.out.println("- Ciclos totales: " + (ciclo - 1));
        System.out.println("- Procesos terminados: " + procesosTerminados + "/" + procesos.length);
        
        System.out.println("\n⏰ TIEMPOS:");
        for (ProcesoSimple p : procesos) {
            int retorno = p.tiempoFin != -1 ? p.tiempoFin - p.tiempoLlegada : 0;
            int espera = p.tiempoInicio != -1 ? p.tiempoInicio - p.tiempoLlegada : 0;
            System.out.printf("- %s: Llegada=%d, Inicio=%d, Fin=%d, Retorno=%d, Espera=%d%n",
                p.nombre, p.tiempoLlegada, p.tiempoInicio, p.tiempoFin, retorno, espera);
        }
    }
}