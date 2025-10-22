/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemasoperativos_arellanomartinez.Simulador;

/**
 *
 * @author Ada y Day
 */
public class Reloj {
    private static int currentCycle = 0;
    private static int cycleDurationMs = 100;
    private static int initialCycleDurationMs = 100;
    
    public Reloj(int cycleDurationMs) {
        Reloj.cycleDurationMs = cycleDurationMs;
    }
    
    public static int getCurrentCycle() {
        return currentCycle;
    }
    
     public static void inicializar(int cicloInicial) {
        currentCycle = cicloInicial;
    }
    
    public static void setCurrentCycle(int cycle) {
        currentCycle = cycle;
    }
    
    public static int getCycleDurationMs() {
        return cycleDurationMs;
    }
    
    public static void incrementCycle() {
        currentCycle++;
    }
    
    // incrementa proceso
    public static void tick() {
        currentCycle++;
        System.out.println("Reloj avanzó a ciclo: " + currentCycle);
    }
    
    public static void reset() {
        currentCycle = 0;
        cycleDurationMs = initialCycleDurationMs;
        System.out.println("Reloj reiniciado a ciclo 0");
    }
    
    public static void setCycleDurationMs(int duration) {
        cycleDurationMs = Math.max(10, duration);
        System.out.println("Duración del ciclo cambiada a: " + duration + "ms");
    }
}