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
 * SPN (Shortest Process Next) - Planificacion no apropiativa CORREGIDA
 * @author Day
 */
public class SPN implements Planificador {
    private ListaSimple procesosListos;
    private Proceso procesoEjecutando;
    private final Semaphore semaforoCola;
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
            
            // 1. verifica si el proceso se sigue ejecutando (el actual)
            if (procesoEjecutando != null) {
                if (procesoEjecutando.isFinished()) {
                    // liberar CPU
                    System.out.println(procesoEjecutando.getName() + " TERMINADO en SPN");
                    procesoEjecutando.setTiempoFinalizacion(Reloj.getCurrentCycle());
                    procesosCompletados++;
                    procesoEjecutando = null;
                    cambiosContexto++;
                } else if (procesoEjecutando.estaEnES()) {
                    // Proceso fue a E/S
                    System.out.println(procesoEjecutando.getName() + " BLOQUEADO por E/S");
                    procesoEjecutando = null;
                    cambiosContexto++;
                } else {
                    System.out.println("SPN continua: " + procesoEjecutando.getName() + 
                                     " (restantes: " + procesoEjecutando.getInstruccionesRestantes() + ")");
                    semaforoCola.release();
                    return procesoEjecutando;
                }
            }
            
            // 2. seleccionar nuevo p (el mas corto de los dispo)
            Proceso mejorProceso = encontrarProcesoMasCortoDisponible();
            
            if (mejorProceso != null) {
                procesoEjecutando = mejorProceso;
                removerDeLista(mejorProceso);
                
                // registrar inicio de ejecucion (si es primera vez)
                if (procesoEjecutando.getTiempoInicioEjecucion() == -1) {
                    procesoEjecutando.setTiempoInicioEjecucion(Reloj.getCurrentCycle());
                }
                
                procesoEjecutando.setState(Proceso.Estado.EJECUTANDO);
                cambiosContexto++;
                
                System.out.println("SPN selecciona NUEVO: " + procesoEjecutando.getName() + 
                                 " (total: " + procesoEjecutando.getTotalInstructions() + " instrucciones)");
                
            } else {
                System.out.println("SPN: No hay procesos listos disponibles");
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
     * encuentra el proceso mas corto que NO este terminado
     */
    private Proceso encontrarProcesoMasCortoDisponible() {
        Proceso mejor = null;
        int menorTotal = Integer.MAX_VALUE;
        
        // Buscar en procesos listos que no esten terminados
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
     * Remueve un proceso especifico de la lista
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
            
            // Solo agregar si no esta terminado
            if (!proceso.isFinished()) {
                proceso.setState(Proceso.Estado.LISTO);
                procesosListos.insertFinal(proceso);
                
                System.out.println(proceso.getName() + " agregado a SPN" +
                                 " (total: " + proceso.getTotalInstructions() + " instrucciones)");
            } else {
                System.out.println(proceso.getName() + " ya terminado - no se agrega a SPN");
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
            
            // verificar procesos que no esten terminados
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
            sb.append("CPU: ").append(procesoEjecutando != null ? 
                procesoEjecutando.getName() + " [EJECUTANDO]" : "LIBRE").append("\n");
            
            // Mostrar solo procesos no terminados
            sb.append("Cola Listos (").append(contarProcesosListosValidos()).append("): ");
            if (contarProcesosListosValidos() == 0) {
                sb.append("Vacia");
            } else {
                for (int i = 0; i < procesosListos.sizeLista(); i++) {
                    Proceso p = (Proceso) procesosListos.get(i);
                    if (p != null && !p.isFinished()) {
                        sb.append(p.getName()).append("(").append(p.getTotalInstructions()).append(") ");
                    }
                }
            }
            
            sb.append("\nCambios contexto: ").append(cambiosContexto);
            sb.append("\nCiclos totales: ").append(ciclosTotales);
            sb.append("\nProcesos completados: ").append(procesosCompletados);
            
            semaforoCola.release();
            return sb.toString();
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "Error al obtener estado";
        }
    }
    
    /**
     *Contar solo procesos validos en la cola
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
    
    // Resto de metodos se mantienen igual...
    public Proceso getProcesoEjecutando() { return procesoEjecutando; }
    public int getCambiosContexto() { return cambiosContexto; }
    public int getProcesosCompletados() { return procesosCompletados; }
    
    // Metodos de la interfaz que no requieren cambios
    @Override public void actualizarCiclo(int ciclo) {}
    @Override public String getNombreAlgoritmo() { return "SPN (Shortest Process Next)"; }
    @Override public void reorganizarColas() {}
}