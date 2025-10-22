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
 * SRT (Shortest Remaining Time) - Planificación apropiativa
 * Política: Siempre ejecuta el proceso con menos instrucciones restantes
 * @author Ada
 */
public class SRT implements Planificador {
    private ListaSimple procesosListos;
    private Proceso procesoEjecutando;
    private final Semaphore semaforoCola;
    
    // Métricas
    private int cambiosContexto;
    private int ciclosTotales;
    private int desalojosApropiativos;
    
    public SRT() {
        this.procesosListos = new ListaSimple();
        this.procesoEjecutando = null;
        this.semaforoCola = new Semaphore(1);
        this.cambiosContexto = 0;
        this.ciclosTotales = 0;
        this.desalojosApropiativos = 0;
    }
    
    @Override
    public Proceso seleccionarProximoProceso() {
        try {
            semaforoCola.acquire();
            
            ciclosTotales++;
            
            // 1. BUSCAR EL PROCESO CON MENOR TIEMPO RESTANTE (incluyendo el actual)
            Proceso mejorProceso = encontrarProcesoMasCorto();
            
            // 2. VERIFICAR SI HAY CAMBIO DE PROCESO (apropiativo)
            if (mejorProceso != null && mejorProceso != procesoEjecutando) {
                // 🔥 CAMBIO APROPITIVO: Desalojar proceso actual si hay uno mejor
                if (procesoEjecutando != null) {
                    // Reinsertar el proceso actual a la cola (no terminó, solo fue desalojado)
                    procesosListos.insertFinal(procesoEjecutando);
                    procesoEjecutando.setState(Proceso.Estado.LISTO);
                    System.out.println("🔄 SRT desaloja: " + procesoEjecutando.getName() + 
                                     " (restantes: " + procesoEjecutando.getInstruccionesRestantes() + ")");
                    desalojosApropiativos++;
                }
                
                procesoEjecutando = mejorProceso;
                removerDeLista(mejorProceso);
                
                // Registrar inicio de ejecución (si es primera vez)
                if (procesoEjecutando.getTiempoInicioEjecucion() == -1) {
                    procesoEjecutando.setTiempoInicioEjecucion(Reloj.getCurrentCycle());
                }
                
                procesoEjecutando.setState(Proceso.Estado.EJECUTANDO);
                cambiosContexto++;
                
                System.out.println("🎯 SRT selecciona: " + procesoEjecutando.getName() + 
                                 " (restantes: " + procesoEjecutando.getInstruccionesRestantes() + ")");
                
            } else if (procesoEjecutando != null) {
                // Mismo proceso, continuar ejecución
                System.out.println("🔄 SRT mantiene: " + procesoEjecutando.getName() + 
                                 " (restantes: " + procesoEjecutando.getInstruccionesRestantes() + ")");
            } else if (mejorProceso != null) {
                // Primer proceso a ejecutar
                procesoEjecutando = mejorProceso;
                removerDeLista(mejorProceso);
                
                if (procesoEjecutando.getTiempoInicioEjecucion() == -1) {
                    procesoEjecutando.setTiempoInicioEjecucion(Reloj.getCurrentCycle());
                }
                
                procesoEjecutando.setState(Proceso.Estado.EJECUTANDO);
                cambiosContexto++;
                
                System.out.println("🎯 SRT selecciona: " + procesoEjecutando.getName() + 
                                 " (restantes: " + procesoEjecutando.getInstruccionesRestantes() + ")");
            } else {
                System.out.println("💤 SRT: No hay procesos listos");
            }
            
            // 3. VERIFICAR SI EL PROCESO ACTUAL TERMINÓ O FUE A E/S
            if (procesoEjecutando != null && 
                (procesoEjecutando.isFinished() || procesoEjecutando.estaEnES())) {
                
                if (procesoEjecutando.isFinished()) {
                    procesoEjecutando.setTiempoFinalizacion(Reloj.getCurrentCycle());
                    System.out.println("✅ " + procesoEjecutando.getName() + " TERMINADO en SRT");
                } else if (procesoEjecutando.estaEnES()) {
                    System.out.println("🔄 " + procesoEjecutando.getName() + " BLOQUEADO por E/S");
                }
                
                procesoEjecutando = null;
                cambiosContexto++;
            }
            
            semaforoCola.release();
            return procesoEjecutando;
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }
    
    /**
     * Encuentra el proceso con menos instrucciones restantes
     * Considera tanto procesos en cola como el proceso actualmente ejecutando
     */
    private Proceso encontrarProcesoMasCorto() {
        Proceso mejor = null;
        int menorRestante = Integer.MAX_VALUE;
        
        // Buscar en procesos listos
        for (int i = 0; i < procesosListos.sizeLista(); i++) {
            Proceso p = (Proceso) procesosListos.get(i);
            int restantes = p.getInstruccionesRestantes();
            if (restantes < menorRestante) {
                menorRestante = restantes;
                mejor = p;
            }
        }
        
        // Comparar con proceso actual (si existe)
        if (procesoEjecutando != null) {
            int actualRestantes = procesoEjecutando.getInstruccionesRestantes();
            if (mejor == null || actualRestantes <= menorRestante) {
                // El proceso actual es el mejor o igual al mejor
                return procesoEjecutando;
            }
        }
        
        return mejor;
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
            
            System.out.println("📥 " + proceso.getName() + " agregado a SRT" +
                             " (restantes: " + proceso.getInstruccionesRestantes() + ")");
            
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
    public void actualizarCiclo(int ciclo) {
        // SRT no necesita hacer nada especial por ciclo
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
        return "SRT (Shortest Remaining Time)";
    }
    
    @Override
    public void reorganizarColas() {
        // SRT no necesita reorganizar colas
    }
    
    @Override
    public String getEstadoColas() {
        try {
            semaforoCola.acquire();
            
            StringBuilder sb = new StringBuilder();
            sb.append("🖥️  CPU: ").append(procesoEjecutando != null ? 
                procesoEjecutando.getName() + " [EJECUTANDO]" : "LIBRE").append("\n");
            
            sb.append("📋 Cola Listos (").append(procesosListos.sizeLista()).append("): ");
            if (procesosListos.sizeLista() == 0) {
                sb.append("Vacía");
            } else {
                for (int i = 0; i < procesosListos.sizeLista(); i++) {
                    Proceso p = (Proceso) procesosListos.get(i);
                    sb.append(p.getName()).append("(").append(p.getInstruccionesRestantes()).append(") ");
                }
            }
            
            sb.append("\n🔀 Cambios contexto: ").append(cambiosContexto);
            sb.append("\n⏰ Ciclos totales: ").append(ciclosTotales);
            sb.append("\n⚡ Desalojos apropiativos: ").append(desalojosApropiativos);
            
            semaforoCola.release();
            return sb.toString();
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "Error al obtener estado";
        }
    }
    
    // 🔹 MÉTODOS ESPECÍFICOS DE SRT
    
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
    
    public int getDesalojosApropiativos() {
        return desalojosApropiativos;
    }
}