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
 * SPN (Shortest Process Next) - Planificación no apropiativa CORREGIDA
 */
public class SPN implements Planificador {
    private ListaSimple procesosListos;
    private Proceso procesoEjecutando;
    private final Semaphore semaforoCola;
    
    // Métricas
    private int cambiosContexto;
    private int ciclosTotales;
    private int procesosCompletados;
    
    public SPN() {
        this.procesosListos = new ListaSimple();
        this.procesoEjecutando = null;
        this.semaforoCola = new Semaphore(1);
        this.cambiosContexto = 0;
        this.ciclosTotales = 0;
        this.procesosCompletados = 0;
    }
    
    @Override
    public Proceso seleccionarProximoProceso() {
        try {
            semaforoCola.acquire();
            
            ciclosTotales++;
            
            // 1. VERIFICAR SI EL PROCESO ACTUAL SIGUE EJECUTÁNDOSE
            if (procesoEjecutando != null) {
                if (procesoEjecutando.isFinished()) {
                    // 🔥 CORRECCIÓN: Proceso terminado - liberar CPU
                    System.out.println("✅ " + procesoEjecutando.getName() + " TERMINADO en SPN");
                    procesoEjecutando.setTiempoFinalizacion(Reloj.getCurrentCycle());
                    procesosCompletados++;
                    procesoEjecutando = null;
                    cambiosContexto++;
                } else if (procesoEjecutando.estaEnES()) {
                    // Proceso fue a E/S
                    System.out.println("🔄 " + procesoEjecutando.getName() + " BLOQUEADO por E/S");
                    procesoEjecutando = null;
                    cambiosContexto++;
                } else {
                    // 🔥 CORRECCIÓN: SPN es NO APROPITATIVO - continuar con el mismo proceso
                    System.out.println("🔄 SPN continúa: " + procesoEjecutando.getName() + 
                                     " (restantes: " + procesoEjecutando.getInstruccionesRestantes() + ")");
                    semaforoCola.release();
                    return procesoEjecutando;
                }
            }
            
            // 2. SELECCIONAR NUEVO PROCESO (el más corto de los DISPONIBLES)
            Proceso mejorProceso = encontrarProcesoMasCortoDisponible();
            
            if (mejorProceso != null) {
                procesoEjecutando = mejorProceso;
                removerDeLista(mejorProceso);
                
                // Registrar inicio de ejecución (si es primera vez)
                if (procesoEjecutando.getTiempoInicioEjecucion() == -1) {
                    procesoEjecutando.setTiempoInicioEjecucion(Reloj.getCurrentCycle());
                }
                
                procesoEjecutando.setState(Proceso.Estado.EJECUTANDO);
                cambiosContexto++;
                
                System.out.println("🎯 SPN selecciona NUEVO: " + procesoEjecutando.getName() + 
                                 " (total: " + procesoEjecutando.getTotalInstructions() + " instrucciones)");
                
            } else {
                System.out.println("💤 SPN: No hay procesos listos disponibles");
                procesoEjecutando = null;
            }
            
            semaforoCola.release();
            return procesoEjecutando;
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }
    
    /**
     * 🔥 CORRECCIÓN: Encuentra el proceso más corto que NO esté terminado
     */
    private Proceso encontrarProcesoMasCortoDisponible() {
        Proceso mejor = null;
        int menorTotal = Integer.MAX_VALUE;
        
        // Buscar en procesos listos que no estén terminados
        for (int i = 0; i < procesosListos.sizeLista(); i++) {
            Proceso p = (Proceso) procesosListos.get(i);
            if (p != null && !p.isFinished() && !p.estaEnES()) {
                int total = p.getTotalInstructions();
                if (total < menorTotal) {
                    menorTotal = total;
                    mejor = p;
                }
            }
        }
        
        return mejor;
    }
    
    /**
     * 🔥 CORRECCIÓN: Remueve un proceso específico de la lista
     */
    private void removerDeLista(Proceso proceso) {
        for (int i = 0; i < procesosListos.sizeLista(); i++) {
            Proceso p = (Proceso) procesosListos.get(i);
            if (p == proceso) {
                procesosListos.remove(i);
                break;
            }
        }
    }
    
    @Override
    public void agregarProceso(Proceso proceso) {
        try {
            semaforoCola.acquire();
            
            // 🔥 CORRECCIÓN: Solo agregar si no está terminado
            if (!proceso.isFinished()) {
                proceso.setState(Proceso.Estado.LISTO);
                procesosListos.insertFinal(proceso);
                
                System.out.println("📥 " + proceso.getName() + " agregado a SPN" +
                                 " (total: " + proceso.getTotalInstructions() + " instrucciones)");
            } else {
                System.out.println("⚠️  " + proceso.getName() + " ya terminado - no se agrega a SPN");
            }
            
            semaforoCola.release();
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    @Override
    public void procesoVolvioDeES(Proceso proceso) {
        // Cuando un proceso vuelve de E/S, se agrega a la cola de listos
        if (!proceso.isFinished()) {
            agregarProceso(proceso);
        }
    }
    
    @Override
    public void procesoBloqueado(Proceso proceso) {
        try {
            semaforoCola.acquire();
            
            if (procesoEjecutando == proceso) {
                // SPN es no apropiativo, pero E/S causa bloqueo
                procesoEjecutando = null;
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
                cambiosContexto++;
            }
            removerDeLista(proceso);
            
            semaforoCola.release();
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    @Override
    public boolean tieneProcesos() {
        try {
            semaforoCola.acquire();
            
            // 🔥 CORRECCIÓN: Verificar procesos que no estén terminados
            boolean hayProcesosListos = false;
            for (int i = 0; i < procesosListos.sizeLista(); i++) {
                Proceso p = (Proceso) procesosListos.get(i);
                if (p != null && !p.isFinished()) {
                    hayProcesosListos = true;
                    break;
                }
            }
            
            boolean resultado = hayProcesosListos || (procesoEjecutando != null && !procesoEjecutando.isFinished());
            semaforoCola.release();
            return resultado;
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
    
    @Override
    public String getEstadoColas() {
        try {
            semaforoCola.acquire();
            
            StringBuilder sb = new StringBuilder();
            sb.append("🖥️  CPU: ").append(procesoEjecutando != null ? 
                procesoEjecutando.getName() + " [EJECUTANDO]" : "LIBRE").append("\n");
            
            // 🔥 CORRECCIÓN: Mostrar solo procesos no terminados
            sb.append("📋 Cola Listos (").append(contarProcesosListosValidos()).append("): ");
            if (contarProcesosListosValidos() == 0) {
                sb.append("Vacía");
            } else {
                for (int i = 0; i < procesosListos.sizeLista(); i++) {
                    Proceso p = (Proceso) procesosListos.get(i);
                    if (p != null && !p.isFinished()) {
                        sb.append(p.getName()).append("(").append(p.getTotalInstructions()).append(") ");
                    }
                }
            }
            
            sb.append("\n🔀 Cambios contexto: ").append(cambiosContexto);
            sb.append("\n⏰ Ciclos totales: ").append(ciclosTotales);
            sb.append("\n✅ Procesos completados: ").append(procesosCompletados);
            
            semaforoCola.release();
            return sb.toString();
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "Error al obtener estado";
        }
    }
    
    /**
     * 🔥 CORRECCIÓN: Contar solo procesos válidos en la cola
     */
    private int contarProcesosListosValidos() {
        int count = 0;
        for (int i = 0; i < procesosListos.sizeLista(); i++) {
            Proceso p = (Proceso) procesosListos.get(i);
            if (p != null && !p.isFinished()) {
                count++;
            }
        }
        return count;
    }
    
    // Resto de métodos se mantienen igual...
    public Proceso getProcesoEjecutando() { return procesoEjecutando; }
    public int getCambiosContexto() { return cambiosContexto; }
    public int getProcesosCompletados() { return procesosCompletados; }
    
    // Métodos de la interfaz que no requieren cambios
    @Override public void actualizarCiclo(int ciclo) {}
    @Override public String getNombreAlgoritmo() { return "SPN (Shortest Process Next)"; }
    @Override public void reorganizarColas() {}
}