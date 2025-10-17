package pruebas;

import sistemasoperativos_arellanomartinez.Planificador.SRTF;
import sistemasoperativos_arellanomartinez.Simulador.Proceso;
import sistemasoperativos_arellanomartinez.Simulador.Reloj;

/**
 * Clase para probar el algoritmo SRTF con procesos que usan hilos
 */
public class PruebaSRTF {
    private SRTF planificador;
    private Reloj reloj;
    
    public PruebaSRTF() {
        this.planificador = new SRTF();
        this.reloj = new Reloj(100); // 100ms por ciclo
    }
    
    /**
     * Prueba básica de SRTF con procesos cortos
     */
    public void pruebaBasica() {
        System.out.println("🚀 INICIANDO PRUEBA BÁSICA SRTF");
        System.out.println("=" .repeat(50));
        
        // Crear procesos de prueba
        Proceso p1 = new Proceso("P1", 5, false, 3, 2, 0);
        Proceso p2 = new Proceso("P2", 3, false, 4, 1, 0);
        Proceso p3 = new Proceso("P3", 8, false, 5, 2, 0);
        
        // Agregar procesos al planificador
        planificador.agregarProceso(p1);
        planificador.agregarProceso(p2);
        planificador.agregarProceso(p3);
        
        // Ejecutar simulación
        ejecutarSimulacion(15);
        
        mostrarMetricasFinales(p1, p2, p3);
    }
    
    /**
     * Prueba con llegada escalonada de procesos
     */
    public void pruebaLlegadaEscalonada() {
        System.out.println("\n🕒 INICIANDO PRUEBA LLEGADA ESCALONADA SRTF");
        System.out.println("=" .repeat(50));
        
        planificador = new SRTF();
        reloj = new Reloj(100);
        
        // Procesos que llegan en diferentes ciclos
        Proceso p1 = new Proceso("P1-Largo", 10, false, 6, 2, 0);
        Proceso p2 = new Proceso("P2-Corto", 4, false, 3, 1, 2);
        Proceso p3 = new Proceso("P3-Medio", 6, false, 4, 1, 4);
        
        planificador.agregarProceso(p1);
        
        // Simular 15 ciclos con llegada escalonada
        for (int ciclo = 0; ciclo < 15; ciclo++) {
            System.out.println("\n--- Ciclo " + ciclo + " ---");
            
            // Agregar procesos en ciclos específicos
            if (ciclo == 2) {
                System.out.println("📥 Llega P2 en ciclo 2");
                planificador.agregarProceso(p2);
            }
            if (ciclo == 4) {
                System.out.println("📥 Llega P3 en ciclo 4");
                planificador.agregarProceso(p3);
            }
            
            ejecutarCiclo(ciclo);
            
            if (!planificador.tieneProcesos()) {
                System.out.println("✅ Todos los procesos terminaron");
                break;
            }
        }
        
        mostrarMetricasFinales(p1, p2, p3);
    }
    
    /**
     * Prueba con procesos CPU-Bound y I/O-Bound mezclados
     */
    public void pruebaMixCPUIO() {
        System.out.println("\n🔀 INICIANDO PRUEBA MIX CPU/I-O SRTF");
        System.out.println("=" .repeat(50));
        
        planificador = new SRTF();
        reloj = new Reloj(100);
        
        // Proceso CPU-Bound (sin E/S)
        Proceso cpu1 = new Proceso("CPU1", 6, true, 0, 0, 0);
        // Procesos I/O-Bound (con E/S frecuente)
        Proceso io1 = new Proceso("IO1", 8, false, 2, 2, 0);
        Proceso io2 = new Proceso("IO2", 5, false, 3, 1, 1);
        
        planificador.agregarProceso(cpu1);
        planificador.agregarProceso(io1);
        planificador.agregarProceso(io2);
        
        ejecutarSimulacion(20);
        
        mostrarMetricasFinales(cpu1, io1, io2);
    }
    
    /**
     * Prueba de apropiación (preemption) de SRTF
     */
    public void pruebaPreemption() {
        System.out.println("\n⚡ INICIANDO PRUEBA PREEMPTION SRTF");
        System.out.println("=" .repeat(50));
        
        planificador = new SRTF();
        reloj = new Reloj(100);
        
        // Proceso largo que será interrumpido por uno más corto
        Proceso largo = new Proceso("Largo", 15, false, 8, 2, 0);
        Proceso corto = new Proceso("Corto", 3, false, 2, 1, 3);
        
        planificador.agregarProceso(largo);
        
        // Simular apropiación
        for (int ciclo = 0; ciclo < 10; ciclo++) {
            System.out.println("\n--- Ciclo " + ciclo + " ---");
            
            // El proceso corto llega en ciclo 3
            if (ciclo == 3) {
                System.out.println("🚨 Llega proceso CORTO que interrumpirá al LARGO");
                planificador.agregarProceso(corto);
            }
            
            ejecutarCiclo(ciclo);
            
            if (!planificador.tieneProcesos()) {
                break;
            }
        }
        
        mostrarMetricasFinales(largo, corto);
    }
    
    /**
     * Ejecuta un ciclo completo de simulación
     */
    private void ejecutarCiclo(int ciclo) {
        Reloj.setCurrentCycle(ciclo);
        
        // 1. Seleccionar y ejecutar proceso
        Proceso actual = planificador.seleccionarProximoProceso();
        
        if (actual != null) {
            // 2. Simular ejecución (el proceso se ejecuta en su hilo automáticamente)
            System.out.println("⚡ Ejecutando: " + actual.getName() + 
                             " - PC: " + actual.getPc() + 
                             "/" + actual.getTotalInstructions() +
                             " - Restantes: " + actual.getInstruccionesRestantes());
            
            // 3. Simular E/S aleatoria (para hacer la prueba más interesante)
            simularEventosES(actual, ciclo);
            
            // 4. Verificar si el proceso terminó durante este ciclo
            if (actual.isFinished()) {
                System.out.println("🎉 " + actual.getName() + " TERMINÓ en ciclo " + ciclo);
                planificador.procesoTerminado(actual);
            }
        } else {
            System.out.println("💤 No hay procesos para ejecutar");
        }
        
        // 5. Mostrar estado del planificador
        System.out.println("📊 Estado: " + planificador.getEstadoCompleto());
        
        // Pequeña pausa para ver la ejecución
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Simula eventos de E/S para hacer la prueba más realista
     */
    private void simularEventosES(Proceso proceso, int ciclo) {
        // Simular que un proceso genera E/S (10% de probabilidad por ciclo)
        if (!proceso.isCpuBound() && Math.random() < 0.1 && !proceso.estaEnES()) {
            System.out.println("🚨 " + proceso.getName() + " genera E/S en ciclo " + ciclo);
            proceso.generarES();
            planificador.procesoBloqueado(proceso);
        }
        
        // Simular procesos que vuelven de E/S (manejo simple)
        // En una implementación real esto lo haría un gestor de E/S
        if (proceso.estaEnES()) {
            proceso.procesarCicloES();
            if (!proceso.estaEnES() && proceso.getState() == Proceso.Estado.LISTO) {
                System.out.println("✅ " + proceso.getName() + " vuelve de E/S");
                planificador.procesoVolvioDeES(proceso);
            }
        }
    }
    
    /**
     * Ejecuta una simulación completa por N ciclos
     */
    private void ejecutarSimulacion(int ciclosTotales) {
        for (int ciclo = 0; ciclo < ciclosTotales; ciclo++) {
            System.out.println("\n--- Ciclo " + ciclo + " ---");
            ejecutarCiclo(ciclo);
            
            if (!planificador.tieneProcesos()) {
                System.out.println("✅ Todos los procesos terminaron en ciclo " + ciclo);
                break;
            }
        }
    }
    
    /**
     * Muestra métricas finales de los procesos
     */
    private void mostrarMetricasFinales(Proceso... procesos) {
        System.out.println("\n📈 MÉTRICAS FINALES");
        System.out.println("-".repeat(50));
        
        for (Proceso p : procesos) {
            System.out.printf("%s: Tiempo Espera=%d, Tiempo Retorno=%d, Tiempo Ejecución=%d, Estado=%s%n",
                p.getName(),
                p.getTiempoEspera(),
                p.getTiempoRetorno(),
                p.getTiempoEjecucionTotal(),
                p.getState());
        }
    }
    
    /**
     * Ejecuta todas las pruebas
     */
    public void ejecutarTodasLasPruebas() {
        System.out.println("🧪 INICIANDO SUITE DE PRUEBAS SRTF");
        System.out.println("⭐ Pruebas con procesos multi-hilo");
        System.out.println("=" .repeat(60));
        
        pruebaBasica();
        pruebaLlegadaEscalonada();
        pruebaMixCPUIO();
        pruebaPreemption();
        
        System.out.println("\n🎉 TODAS LAS PRUEBAS COMPLETADAS");
    }
    
    /**
     * Método main para ejecutar las pruebas
     */
    public static void main(String[] args) {
        PruebaSRTF tester = new PruebaSRTF();
        tester.ejecutarTodasLasPruebas();
        
        // Mantener el programa vivo para que los hilos terminen
        try {
            Thread.sleep(2000);
            System.out.println("\n🔚 Programa terminado");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}