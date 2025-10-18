/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemasoperativos_arellanomartinez.Planificador;
import sistemasoperativos_arellanomartinez.Simulador.Proceso;
import sistemasoperativos_arellanomartinez.Simulador.Reloj;
import sistemasoperativos_arellanomartinez.Simulador.Proceso.Estado;
import edd.Cola;
import java.util.concurrent.Semaphore;
import java.util.Random;
/**
 *
 * @author Day y Ada
 */
public class RandomPlanificador implements Planificador {
    private Cola colaListos;
    private Cola colaBloqueados;
    private Proceso procesoEjecutando;
    private final Semaphore semaforoColas;
    private final Random randomGenerator;
    
    // Métricas para threads
    private int cambiosContexto;
    private int ciclosTotales;
    private boolean soEjecutando;
    private String nombre = "Random";
    
    public RandomPlanificador() {
        this.colaListos = new Cola();
        this.colaBloqueados = new Cola();
        this.procesoEjecutando = null;
        this.semaforoColas = new Semaphore(1);
        this.randomGenerator = new Random();
        this.cambiosContexto = 0;
        this.ciclosTotales = 0;
        this.soEjecutando = false;
    }
    
    @Override
    public Proceso siguienteProceso() {
        try {
            semaforoColas.acquire();
            
            soEjecutando = true;
            ciclosTotales++;
            
            // Si hay proceso ejecutando y no ha terminado, 50% de probabilidad de cambiarlo
            if (procesoEjecutando != null && 
                procesoEjecutando.getState() == Estado.EJECUTANDO &&
                !procesoEjecutando.isFinished() && 
                !procesoEjecutando.estaEnES()) {
                
                // 🎲 50% de probabilidad de mantener el proceso actual
                if (randomGenerator.nextBoolean()) {
                    System.out.println("🎲 Random mantiene: " + procesoEjecutando.getName());
                    soEjecutando = false;
                    semaforoColas.release();
                    return procesoEjecutando;
                } else {
                    System.out.println("🎲 Random decide cambiar de proceso");
                    // Volver proceso actual a la cola
                    procesoEjecutando.pausarEjecucion();
                    procesoEjecutando.setState(Estado.LISTO);
                    colaListos.encolar(procesoEjecutando);
                    procesoEjecutando = null;
                    cambiosContexto++;
                }
            }
            
            procesoEjecutando = null;
            
            if (colaListos.estaVacia()) {
                soEjecutando = false;
                semaforoColas.release();
                return null;
            }
            
            // 🎯 RANDOM: Seleccionar proceso aleatorio de la cola
            Proceso procesoAleatorio = seleccionarProcesoAleatorio();
            
            if (procesoAleatorio != null && procesoAleatorio.getPc() < procesoAleatorio.getTotalInstructions()) {
                procesoEjecutando = procesoAleatorio;
                procesoEjecutando.setState(Estado.EJECUTANDO);
                eliminarProcesoDeCola(procesoAleatorio);
                
                // 🧵 CONTROL DE THREADS
                if (!procesoEjecutando.isEjecutando()) {
                    procesoEjecutando.iniciarEjecucion();
                } else {
                    procesoEjecutando.reanudarEjecucion();
                }
                
                System.out.println("🎲 Random selecciona: " + procesoEjecutando.getName() + 
                                 " (selección aleatoria)");
                cambiosContexto++;
            }
            
            soEjecutando = false;
            semaforoColas.release();
            return procesoEjecutando;
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }
    
    /**
     * 🎯 Selecciona un proceso aleatorio de la cola
     */
    private Proceso seleccionarProcesoAleatorio() {
        if (colaListos.estaVacia()) {
            return null;
        }
        
        int totalProcesos = colaListos.getTamano();
        int indiceAleatorio = randomGenerator.nextInt(totalProcesos);
        
        Cola temp = new Cola();
        Proceso procesoSeleccionado = null;
        int contador = 0;
        
        while (!colaListos.estaVacia()) {
            Proceso actual = (Proceso) colaListos.desencolar();
            
            if (contador == indiceAleatorio) {
                procesoSeleccionado = actual;
            } else {
                temp.encolar(actual);
            }
            contador++;
        }
        
        while (!temp.estaVacia()) {
            colaListos.encolar(temp.desencolar());
        }
        
        return procesoSeleccionado;
    }
    
    private void eliminarProcesoDeCola(Proceso proceso) {
        Cola temp = new Cola();
        while (!colaListos.estaVacia()) {
            Proceso actual = (Proceso) colaListos.desencolar();
            if (actual != proceso) {
                temp.encolar(actual);
            }
        }
        while (!temp.estaVacia()) {
            colaListos.encolar(temp.desencolar());
        }
    }
    
    @Override
    public void agregarProceso(Proceso proceso) {
        try {
            semaforoColas.acquire();
            
            if (proceso.getState() == Estado.NUEVO || proceso.getState() == Estado.LISTO) {
                proceso.setState(Estado.LISTO);
                colaListos.encolar(proceso);
                
                if (proceso.getState() == Estado.NUEVO) {
                    proceso.iniciarEjecucion();
                    proceso.pausarEjecucion();
                }
                
                System.out.println("📥 " + proceso.getName() + " agregado a Random - Thread iniciado");
            }
            
            semaforoColas.release();
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    @Override
    public void eliminarProceso(Proceso proceso) {
        try {
            semaforoColas.acquire();
            
            if (procesoEjecutando == proceso) {
                if (procesoEjecutando != null) {
                    procesoEjecutando.detenerEjecucion();
                }
                procesoEjecutando = null;
                cambiosContexto++;
            }
            
            eliminarProcesoDeCola(proceso);
            
            Cola temp = new Cola();
            while (!colaBloqueados.estaVacia()) {
                Proceso actual = (Proceso) colaBloqueados.desencolar();
                if (actual != proceso) {
                    temp.encolar(actual);
                } else {
                    actual.detenerEjecucion();
                    System.out.println("🗑️ " + actual.getName() + " removido de Random - Thread detenido");
                }
            }
            while (!temp.estaVacia()) {
                colaBloqueados.encolar(temp.desencolar());
            }
            
            semaforoColas.release();
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    @Override
    public void actualizarCiclo(int ciclo) {
        try {
            semaforoColas.acquire();
            
            if (procesoEjecutando != null && procesoEjecutando.getState() == Estado.EJECUTANDO) {
                
                if (procesoEjecutando.isFinished()) {
                    procesoEjecutando.setState(Estado.TERMINADO);
                    procesoEjecutando.detenerEjecucion();
                    System.out.println("✅ " + procesoEjecutando.getName() + " TERMINADO (Random)");
                    procesoEjecutando = null;
                    semaforoColas.release();
                    return;
                }
                
                if (procesoEjecutando.getPc() > 0 && 
                    procesoEjecutando.debeGenerarES() && 
                    !procesoEjecutando.estaEnES()) {
                    
                    procesoEjecutando.setState(Estado.BLOQUEADO);
                    procesoEjecutando.pausarEjecucion();
                    colaBloqueados.encolar(procesoEjecutando);
                    System.out.println("🔄 " + procesoEjecutando.getName() + " BLOQUEADO por E/S (Random)");
                    procesoEjecutando = null;
                    cambiosContexto++;
                    semaforoColas.release();
                    return;
                }
            }
            
            if (!colaBloqueados.estaVacia() && ciclo % 4 == 0) {
                Cola temp = new Cola();
                while (!colaBloqueados.estaVacia()) {
                    Proceso bloqueado = (Proceso) colaBloqueados.desencolar();
                    if (!bloqueado.estaEnES()) {
                        bloqueado.setState(Estado.LISTO);
                        colaListos.encolar(bloqueado);
                        System.out.println("🔄 " + bloqueado.getName() + " VUELVE de E/S a LISTO (Random)");
                        break;
                    } else {
                        temp.encolar(bloqueado);
                    }
                }
                while (!temp.estaVacia()) {
                    colaBloqueados.encolar(temp.desencolar());
                }
            }
            
            semaforoColas.release();
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    @Override
    public boolean tieneProcesos() {
        try {
            semaforoColas.acquire();
            boolean resultado = !colaListos.estaVacia() || !colaBloqueados.estaVacia() || procesoEjecutando != null;
            semaforoColas.release();
            return resultado;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
    
    @Override
    public String getNombre() {
        return this.nombre;
    }
    
    // 🔄 MÉTODOS REQUERIDOS POR LA INTERFAZ PLANIFICADOR
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
        // Random no necesita reorganizar colas explícitamente
    }
    
    @Override
    public void procesoBloqueado(Proceso proceso) {
        try {
            semaforoColas.acquire();
            
            if (proceso != null && !proceso.isFinished()) {
                proceso.pausarEjecucion();
                proceso.setState(Estado.BLOQUEADO);
                colaBloqueados.encolar(proceso);
                
                System.out.println("⏳ " + proceso.getName() + " bloqueado por E/S en Random");
                
                if (procesoEjecutando != null && procesoEjecutando.getId().equals(proceso.getId())) {
                    procesoEjecutando = null;
                    cambiosContexto++;
                }
            }
            
            semaforoColas.release();
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    @Override
    public void procesoVolvioDeES(Proceso proceso) {
        agregarProceso(proceso);
    }
    
    @Override
    public String getEstadoColas() {
        return getEstadoCompletoThreads();
    }
    
    // 🔄 MÉTODOS DE VISUALIZACIÓN
    public String getEstadoCompletoThreads() {
        try {
            semaforoColas.acquire();
            
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
            
            sb.append("\n⏳ Cola Bloqueados (").append(colaBloqueados.getTamano()).append("): ");
            if (colaBloqueados.estaVacia()) {
                sb.append("Vacía");
            } else {
                Cola temp = new Cola();
                while (!colaBloqueados.estaVacia()) {
                    Proceso p = (Proceso) colaBloqueados.desencolar();
                    sb.append(p.getName()).append("(").append(p.getTiempoESRestante()).append(") ");
                    temp.encolar(p);
                }
                while (!temp.estaVacia()) {
                    colaBloqueados.encolar(temp.desencolar());
                }
            }
            
            sb.append("\n🔀 Cambios contexto: ").append(cambiosContexto);
            sb.append("\n⏰ Ciclos totales: ").append(ciclosTotales);
            sb.append("\n🏃 Ejecutando: ").append(soEjecutando ? "SISTEMA OPERATIVO" : "PROCESO USUARIO");
            sb.append("\n🧵 Threads activos: ").append(contarThreadsActivos());
            sb.append("\n🎲 Algoritmo: SELECCIÓN ALEATORIA");
            
            semaforoColas.release();
            return sb.toString();
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "Error al obtener estado";
        }
    }
    
    private int contarThreadsActivos() {
        int activos = 0;
        if (procesoEjecutando != null && procesoEjecutando.isEjecutando()) {
            activos++;
        }
        
        activos += contarThreadsEnCola(colaListos);
        activos += contarThreadsEnCola(colaBloqueados);
        
        return activos;
    }
    
    private int contarThreadsEnCola(Cola cola) {
        int activos = 0;
        Cola temp = new Cola();
        
        while (!cola.estaVacia()) {
            Proceso p = (Proceso) cola.desencolar();
            if (p.isEjecutando()) {
                activos++;
            }
            temp.encolar(p);
        }
        
        while (!temp.estaVacia()) {
            cola.encolar(temp.desencolar());
        }
        
        return activos;
    }
    
    // 🔹 MÉTODOS PARA MONITOREO
    public Proceso getProcesoEjecutando() {
        return procesoEjecutando;
    }
    
    public int getTamanoColaListos() {
        try {
            semaforoColas.acquire();
            int tamano = colaListos.getTamano();
            semaforoColas.release();
            return tamano;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return 0;
        }
    }
    
    public int getTamanoColaBloqueados() {
        try {
            semaforoColas.acquire();
            int tamano = colaBloqueados.getTamano();
            semaforoColas.release();
            return tamano;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return 0;
        }
    }
}