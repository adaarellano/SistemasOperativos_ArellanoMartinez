/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemasoperativos_arellanomartinez.Planificador;
import sistemasoperativos_arellanomartinez.Simulador.Proceso;
import edd.Cola;
import sistemasoperativos_arellanomartinez.Simulador.Reloj;
import java.util.concurrent.Semaphore;

/**
 * First Come First Served - Planificación no apropiativa
 * Política: Una vez que un proceso toma la CPU, la mantiene hasta terminar
 * @author Ada
 */
public class FCFS implements Planificador {
    private Cola colaListos;
    private Proceso procesoEjecutando;
    private final Semaphore semaforoCola;
    
    // Métricas
    private int cambiosContexto;
    private int ciclosTotales;
    
    public FCFS() {
        this.colaListos = new Cola();
        this.procesoEjecutando = null;
        this.semaforoCola = new Semaphore(1);
        this.cambiosContexto = 0;
        this.ciclosTotales = 0;
    }
    
    @Override
    public Proceso siguienteProceso() {
        try {
            semaforoCola.acquire();
            
            ciclosTotales++;
            
            // 🔄 1. VERIFICAR PROCESO ACTUAL
            if (procesoEjecutando != null) {
                // Si terminó o fue a E/S, limpiarlo
                if (procesoEjecutando.isFinished() || procesoEjecutando.estaEnES()) {
                    
                    if (procesoEjecutando.isFinished()) {
                        procesoEjecutando.setTiempoFinalizacion(Reloj.getCurrentCycle());
                        System.out.println("✅ " + procesoEjecutando.getName() + " TERMINADO en FCFS");
                    } else if (procesoEjecutando.estaEnES()) {
                        System.out.println("🔄 " + procesoEjecutando.getName() + " BLOQUEADO por E/S");
                    }
                    
                    procesoEjecutando = null;
                    cambiosContexto++;
                } 
                // Si puede continuar, mantenerlo (FCFS no apropiativa)
                else {
                    System.out.println("🔄 FCFS mantiene en CPU: " + procesoEjecutando.getName());
                    semaforoCola.release();
                    return procesoEjecutando;
                }
            }
            
            // 🎯 2. SELECCIONAR NUEVO PROCESO
            if (!colaListos.estaVacia()) {
                procesoEjecutando = (Proceso) colaListos.desencolar();
                
                // Registrar inicio de ejecución (si es primera vez)
                if (procesoEjecutando.getTiempoInicioEjecucion() == -1) {
                    procesoEjecutando.setTiempoInicioEjecucion(Reloj.getCurrentCycle());
                }
                
                procesoEjecutando.setState(Proceso.Estado.EJECUTANDO);
                
                System.out.println("🎯 FCFS selecciona NUEVO proceso: " + procesoEjecutando.getName());
                cambiosContexto++;
            }
            
            semaforoCola.release();
            return procesoEjecutando;
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }
    
    @Override
    public void agregarProceso(Proceso proceso) {
        try {
            semaforoCola.acquire();
            
            proceso.setState(Proceso.Estado.LISTO);
            colaListos.encolar(proceso);
            
            System.out.println("📥 " + proceso.getName() + " agregado a FCFS");
            
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
                System.out.println("🗑️ " + proceso.getName() + " removido de CPU");
            }
            
            // Eliminar de cola de listos
            Cola temp = new Cola();
            while (!colaListos.estaVacia()) {
                Proceso p = (Proceso) colaListos.desencolar();
                if (p != proceso) {
                    temp.encolar(p);
                } else {
                    System.out.println("🗑️ " + p.getName() + " removido de cola FCFS");
                }
            }
            while (!temp.estaVacia()) {
                colaListos.encolar(temp.desencolar());
            }
            
            semaforoCola.release();
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    @Override
    public void actualizarCiclo(int ciclo) {
        // FCFS no necesita hacer nada especial por ciclo
    }
    
    @Override
    public boolean tieneProcesos() {
        try {
            semaforoCola.acquire();
            boolean resultado = !colaListos.estaVacia() || procesoEjecutando != null;
            semaforoCola.release();
            return resultado;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
    
    @Override
    public String getNombre() {
        return "FCFS (First Come First Served)";
    }
    
    // 🔄 MÉTODOS REQUERIDOS POR LA INTERFAZ
    @Override
    public Proceso seleccionarProximoProceso() {
        return siguienteProceso();
    }
    
    @Override
    public String getNombreAlgoritmo() {
        return getNombre();
    }
    
    @Override
    public void reorganizarColas() {
        // FCFS no necesita reorganizar colas
    }
    
    @Override
    public void procesoBloqueado(Proceso proceso) {
        // Cuando un proceso va a E/S, se remueve de CPU pero queda en cola
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
    public void procesoVolvioDeES(Proceso proceso) {
        // Cuando vuelve de E/S, agregar a cola de listos
        agregarProceso(proceso);
    }
    
    @Override
    public String getEstadoColas() {
        try {
            semaforoCola.acquire();
            
            StringBuilder sb = new StringBuilder();
            sb.append("🖥️  CPU: ").append(procesoEjecutando != null ? 
                procesoEjecutando.getName() + " [EJECUTANDO]" : "LIBRE").append("\n");
            
            sb.append("📋 Cola Listos (").append(colaListos.getTamano()).append("): ");
            if (colaListos.estaVacia()) {
                sb.append("Vacía");
            } else {
                Cola temp = new Cola();
                while (!colaListos.estaVacia()) {
                    Proceso p = (Proceso) colaListos.desencolar();
                    sb.append(p.getName()).append(" ");
                    temp.encolar(p);
                }
                while (!temp.estaVacia()) {
                    colaListos.encolar(temp.desencolar());
                }
            }
            
            sb.append("\n🔀 Cambios contexto: ").append(cambiosContexto);
            sb.append("\n⏰ Ciclos totales: ").append(ciclosTotales);
            
            semaforoCola.release();
            return sb.toString();
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "Error al obtener estado";
        }
    }
    
    // 🔹 MÉTODOS PARA MONITOREO
    public Proceso getProcesoEjecutando() {
        return procesoEjecutando;
    }
    
    public int getTamanoColaListos() {
        try {
            semaforoCola.acquire();
            int tamano = colaListos.getTamano();
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