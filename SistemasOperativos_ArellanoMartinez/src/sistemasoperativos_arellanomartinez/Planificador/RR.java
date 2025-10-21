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
 * Round Robin - Planificación apropiativa con quantum
 * Política: Cada proceso ejecuta un quantum fijo, luego pasa al siguiente
 * @author Day
 */
public class RR implements Planificador {
    private Cola colaListos;
    private Proceso procesoEjecutando;
    private final Semaphore semaforoCola;
    
    // Parámetros de Round Robin
    private final int quantum;
    private int contadorQuantum;
    private boolean necesitaReplanificacion;
    
    // Métricas
    private int cambiosContexto;
    private int ciclosTotales;
    private int quantumExpirados;
    
    public RR() {
        this(3); // Quantum por defecto de 3 ciclos
    }
    
    public RR(int quantum) {
        this.colaListos = new Cola();
        this.procesoEjecutando = null;
        this.semaforoCola = new Semaphore(1);
        this.quantum = quantum;
        this.contadorQuantum = 0;
        this.necesitaReplanificacion = false;
        this.cambiosContexto = 0;
        this.ciclosTotales = 0;
        this.quantumExpirados = 0;
    }
    
    @Override
    public Proceso seleccionarProximoProceso() {
        try {
            semaforoCola.acquire();
            
            ciclosTotales++;
            
            // 1. VERIFICAR SI NECESITA REPLANIFICACIÓN (quantum expirado o proceso terminó)
            if (procesoEjecutando != null) {
                if (procesoEjecutando.isFinished() || procesoEjecutando.estaEnES()) {
                    // Proceso terminó o fue a E/S
                    if (procesoEjecutando.isFinished()) {
                        procesoEjecutando.setTiempoFinalizacion(Reloj.getCurrentCycle());
                        System.out.println("✅ " + procesoEjecutando.getName() + " TERMINADO en Round Robin");
                    } else if (procesoEjecutando.estaEnES()) {
                        System.out.println("🔄 " + procesoEjecutando.getName() + " BLOQUEADO por E/S");
                    }
                    
                    procesoEjecutando = null;
                    contadorQuantum = 0;
                    cambiosContexto++;
                    necesitaReplanificacion = true;
                } 
                // Verificar si expiró el quantum
                else if (contadorQuantum >= quantum) {
                    System.out.println("⏰ Quantum expirado para " + procesoEjecutando.getName());
                    // Reinsertar proceso actual al final de la cola
                    colaListos.encolar(procesoEjecutando);
                    procesoEjecutando.setState(Proceso.Estado.LISTO);
                    procesoEjecutando = null;
                    contadorQuantum = 0;
                    quantumExpirados++;
                    cambiosContexto++;
                    necesitaReplanificacion = true;
                }
            }
            
            // 2. SELECCIONAR NUEVO PROCESO SI ES NECESARIO
            if (necesitaReplanificacion || procesoEjecutando == null) {
                if (!colaListos.estaVacia()) {
                    procesoEjecutando = (Proceso) colaListos.desencolar();
                    
                    // Registrar inicio de ejecución (si es primera vez)
                    if (procesoEjecutando.getTiempoInicioEjecucion() == -1) {
                        procesoEjecutando.setTiempoInicioEjecucion(Reloj.getCurrentCycle());
                    }
                    
                    procesoEjecutando.setState(Proceso.Estado.EJECUTANDO);
                    contadorQuantum = 1; // Reiniciar contador
                    necesitaReplanificacion = false;
                    cambiosContexto++;
                    
                    System.out.println("🎯 Round Robin selecciona: " + procesoEjecutando.getName() + 
                                     " (Quantum: " + contadorQuantum + "/" + quantum + ")");
                } else {
                    procesoEjecutando = null;
                    System.out.println("💤 Round Robin: No hay procesos listos");
                }
            } else {
                // Mismo proceso, incrementar contador de quantum
                contadorQuantum++;
                System.out.println("🔄 Round Robin mantiene: " + procesoEjecutando.getName() + 
                                 " (Quantum: " + contadorQuantum + "/" + quantum + ")");
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
            
            System.out.println("📥 " + proceso.getName() + " agregado a Round Robin (Quantum: " + quantum + ")");
            
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
                contadorQuantum = 0;
                necesitaReplanificacion = true;
                cambiosContexto++;
            }
            
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
                contadorQuantum = 0;
                necesitaReplanificacion = true;
                cambiosContexto++;
            }
            
            // Eliminar de cola de listos
            Cola temp = new Cola();
            while (!colaListos.estaVacia()) {
                Proceso p = (Proceso) colaListos.desencolar();
                if (p != proceso) {
                    temp.encolar(p);
                } else {
                    System.out.println("🗑️ " + p.getName() + " removido de Round Robin");
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
        // Round Robin no necesita hacer nada especial por ciclo
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
    public String getNombreAlgoritmo() {
        return "Round Robin (Quantum: " + quantum + ")";
    }
    
    @Override
    public void reorganizarColas() {
        // Round Robin no necesita reorganizar colas
    }
    
    @Override
    public String getEstadoColas() {
        try {
            semaforoCola.acquire();
            
            StringBuilder sb = new StringBuilder();
            sb.append("🖥️  CPU: ").append(procesoEjecutando != null ? 
                procesoEjecutando.getName() + " [EJECUTANDO]" : "LIBRE").append("\n");
            
            if (procesoEjecutando != null) {
                sb.append("   Quantum: ").append(contadorQuantum).append("/").append(quantum).append("\n");
            }
            
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
            sb.append("\n⚡ Quantum expirados: ").append(quantumExpirados);
            
            semaforoCola.release();
            return sb.toString();
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "Error al obtener estado";
        }
    }
    
    // 🔹 MÉTODOS ESPECÍFICOS DE ROUND ROBIN
    
    public int getQuantum() {
        return quantum;
    }
    
    public int getContadorQuantum() {
        return contadorQuantum;
    }
    
    public int getQuantumExpirados() {
        return quantumExpirados;
    }
    
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