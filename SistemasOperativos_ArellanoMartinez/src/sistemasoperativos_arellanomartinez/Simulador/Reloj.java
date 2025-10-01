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
    private static int cycleDurationMs = 1000;
    
    public static int getCurrentCycle() {
        return currentCycle;
    }
    
    public static void tick() {
        currentCycle++;
    }
    
    public static void reset() {
        currentCycle = 0;
    }
    
    public static int getCycleDurationMs() {
        return cycleDurationMs;
    }
    
    public static void setCycleDurationMs(int durationMs) {
        cycleDurationMs = durationMs;
    }
}
