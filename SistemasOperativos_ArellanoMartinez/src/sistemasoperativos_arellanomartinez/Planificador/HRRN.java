/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemasoperativos_arellanomartinez.Planificador;

import sistemasoperativos_arellanomartinez.Simulador.Proceso;
import edd.ListaSimple;
import sistemasoperativos_arellanomartinez.Simulador.Reloj;
import java.util.concurrent.Semaphore;

/**
 * Highest Response Ratio Next - Planificación no apropiativa
 * @author Ada
 */
public class HRRN implements Planificador {
    private ListaSimple procesosListos;
    private Proceso procesoEjecutando;
    private final Semaphore semaforoCola;
    
    // Métricas
    private int cambiosContexto;
    private int ciclosTotales;
    
    public HRRN() {
        this.procesosListos = new ListaSimple();
        this.procesoEjecutando = null;
        this.semaforoCola = new Semaphore(1);
        this.cambiosContexto = 0;
        this.ciclosTotales = 0;
    }
    
    @Override
    public Proceso seleccionarProximoProceso() {
        try {
            semaforoCola.acquire();
            
            ciclosTotales++;
            
            // 1. verifica proceso actual
            if (procesoEjecutando != null) {
                if (procesoEjecutando.isFinished() || procesoEjecutando.estaEnES()) {
                    
                    if (procesoEjecutando.isFinished()) {
                        procesoEjecutando.setTiempoFinalizacion(Reloj.getCurrentCycle());
                        System.out.println(procesoEjecutando.getName() + " terminado en HRRN");
                    } else if (procesoEjecutando.estaEnES()) {
                        System.out.println(procesoEjecutando.getName() + " bloqueado por E/S");
                    }
                    
                    procesoEjecutando = null;
                    cambiosContexto++;
                } 
                // Si puede continuar se mantiene
                else {
                    System.out.println("HRRN mantiene en CPU: " + procesoEjecutando.getName());
                    semaforoCola.release();
                    return procesoEjecutando;
                }
            }
            
            // 2. Calcular ls ratios de los procesos
            Proceso mejorProceso = calcularProcesoConMayorRatio();
            
            if (mejorProceso != null) {
                procesoEjecutando = mejorProceso;
                removerDeLista(mejorProceso);
                
                // Registrar inicio de ejecución (si es primera vez)
                if (procesoEjecutando.getTiempoInicioEjecucion() == -1) {
                    procesoEjecutando.setTiempoInicioEjecucion(Reloj.getCurrentCycle());
                }
                
                procesoEjecutando.setState(Proceso.Estado.EJECUTANDO);
                cambiosContexto++;
                
                // Calcular ratio para mostrar en debug
                double ratio = calcularRatioRespuesta(mejorProceso);
                System.out.println("🎯 HRRN selecciona: " + mejorProceso.getName() + 
                                 " (Ratio: " + String.format("%.2f", ratio) + ")");
            } else {
                System.out.println("💤 HRRN: No hay procesos listos");
            }
            
            semaforoCola.release();
            return procesoEjecutando;
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }
    
    /**
     * Calcula el ratio de respuesta para un proceso
     */
    private double calcularRatioRespuesta(Proceso proceso) {
        int cicloActual = Reloj.getCurrentCycle();
        int tiempoLlegada = proceso.getTiempoLlegada();
        int tiempoEjecutado = proceso.getTiempoEjecucionTotal();
        int tiempoServicio = proceso.getTotalInstructions();
        
        int tiempoEspera = (cicloActual - tiempoLlegada) - tiempoEjecutado;
        tiempoEspera = Math.max(0, tiempoEspera);
        
        if (tiempoServicio == 0) return 0.0;
        
        double ratio = (double) (tiempoEspera + tiempoServicio) / tiempoServicio;
        return Math.max(0.0, ratio);
    }
    
    /**
     * Encuentra el proceso con mayor ratio de respuesta
     */
    private Proceso calcularProcesoConMayorRatio() {
        if (procesosListos.sizeLista() == 0) return null;
        
        Proceso mejorProceso = null;
        double mejorRatio = -1.0;
        
        for (int i = 0; i < procesosListos.sizeLista(); i++) {
            Proceso proceso = (Proceso) procesosListos.get(i);
            double ratio = calcularRatioRespuesta(proceso);
            
            System.out.println(proceso.getName() + 
                             " Espera: " + (Reloj.getCurrentCycle() - proceso.getTiempoLlegada() - proceso.getTiempoEjecucionTotal()) +
                             ", Servicio: " + proceso.getTotalInstructions() +
                             ", Ratio: " + String.format("%.2f", ratio));
            
            if (ratio > mejorRatio) {
                mejorRatio = ratio;
                mejorProceso = proceso;
            }
        }
        
        return mejorProceso;
    }
    
    /**
     * Remueve un proceso de la lista de listos
     */
    private void removerDeLista(Proceso proceso) {
        for (int i = 0; i < procesosListos.sizeLista(); i++) {
            if (procesosListos.get(i) == proceso) {
                procesosListos.remove(i);
                break;
            }
        }
    }
    
    
    @Override
    public void agregarProceso(Proceso proceso) {
        try {
            semaforoCola.acquire();
            
            proceso.setState(Proceso.Estado.LISTO);
            procesosListos.insertFinal(proceso);
            
            double ratio = calcularRatioRespuesta(proceso);
            System.out.println(proceso.getName() + " agregado a HRRN" +
                             " (Ratio: " + String.format("%.2f", ratio) + ")");
            
            semaforoCola.release();
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    @Override
    public void eliminarProceso(Proceso proceso) {
        try {
            semaforoCola.acquire();
            
            if (procesoEjecutando == proceso) {
                procesoEjecutando = null;
                cambiosContexto++;
            }
            removerDeLista(proceso);
            
            semaforoCola.release();
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    @Override
    public void procesoVolvioDeES(Proceso proceso) {
        agregarProceso(proceso);
    }
    
    @Override
    public void procesoBloqueado(Proceso proceso) {
        try {
            semaforoCola.acquire();
            
            if (procesoEjecutando == proceso) {
                procesoEjecutando = null;
                cambiosContexto++;
            }
            
            semaforoCola.release();
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    @Override
    public void actualizarCiclo(int ciclo) {
        // HRRN no necesita hacer nada especial por ciclo
    }
    
    @Override
    public boolean tieneProcesos() {
        try {
            semaforoCola.acquire();
            boolean resultado = procesosListos.sizeLista() > 0 || procesoEjecutando != null;
            semaforoCola.release();
            return resultado;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
    
    @Override
    public String getNombreAlgoritmo() {
        return "HRRN (Highest Response Ratio Next)";
    }
    
    @Override
    public void reorganizarColas() {
        // HRRN no necesita reorganizar colas
    }
    
    @Override
    public String getEstadoColas() {
        try {
            semaforoCola.acquire();
            
            StringBuilder sb = new StringBuilder();
            sb.append("CPU: ").append(procesoEjecutando != null ? 
                procesoEjecutando.getName() + " [EJECUTANDO]" : "LIBRE").append("\n");
            
            sb.append("Cola Listos (").append(procesosListos.sizeLista()).append("):\n");
            if (procesosListos.sizeLista() == 0) {
                sb.append("   Vacía");
            } else {
                for (int i = 0; i < procesosListos.sizeLista(); i++) {
                    Proceso p = (Proceso) procesosListos.get(i);
                    double ratio = calcularRatioRespuesta(p);
                    sb.append("   • ").append(p.getName())
                      .append(" - Ratio: ").append(String.format("%.2f", ratio))
                      .append(" (Espera: ").append(Reloj.getCurrentCycle() - p.getTiempoLlegada() - p.getTiempoEjecucionTotal())
                      .append(", Servicio: ").append(p.getTotalInstructions()).append(")\n");
                }
            }
            
            sb.append("Cambios contexto: ").append(cambiosContexto);
            sb.append("\nCiclos totales: ").append(ciclosTotales);
            
            semaforoCola.release();
            return sb.toString();
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "Error al obtener estado";
        }
    }

    public Proceso getProcesoEjecutando() {
        return procesoEjecutando;
    }
    
    public int getTamanoColaListos() {
        try {
            semaforoCola.acquire();
            int tamano = procesosListos.sizeLista();
            semaforoCola.release();
            return tamano;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return 0;
        }
    }
    
    public int getCambiosContexto() {
        return cambiosContexto;
    }
}