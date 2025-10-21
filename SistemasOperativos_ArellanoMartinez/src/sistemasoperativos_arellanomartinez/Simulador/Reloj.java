/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemasoperativos_arellanomartinez.Simulador;

/**
 *
 * @author raiza
 */
public class Reloj {
    private static int currentCycle = 0;
    private static int cycleDurationMs = 100;
    
    public Reloj(int cycleDurationMs) {
        Reloj.cycleDurationMs = cycleDurationMs;
    }
    
    public static int getCurrentCycle() {
        return currentCycle;
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
}