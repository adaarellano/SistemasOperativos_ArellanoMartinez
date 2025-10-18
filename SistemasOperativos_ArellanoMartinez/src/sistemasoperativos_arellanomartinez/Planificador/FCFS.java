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
 *First Come First Served = Primero en llegar, primero en ser servido
Política no apropiativa: Una vez que un proceso toma la CPU, la mantiene hasta terminar
Impacto: Simple pero puede causar el "efecto convoy" (procesos largos bloquean a cortos)
 * @author Day
 */
// first in first out de la cola
public class FCFS implements Planificador {
    private Cola colaListos;
    private Proceso procesoEjecutando;
    private final Semaphore semaforoCola; // 🔐 NUEVO: Semáforo para exclusión mútua
    
    // Métricas para threads
    private int cambiosContexto;
    private int ciclosTotales;
    private boolean soEjecutando;
    
    public FCFS() {
        this.colaListos = new Cola();
        this.procesoEjecutando = null;
        this.semaforoCola = new Semaphore(1); // 🔐 INICIALIZAR SEMÁFORO
        this.cambiosContexto = 0;
        this.ciclosTotales = 0;
        this.soEjecutando = false;
    }
    
    @Override
    public Proceso siguienteProceso() {
        try {
            semaforoCola.acquire(); // 🔐 ADQUIRIR SEMÁFORO
            
            soEjecutando = true;
            ciclosTotales++;
            
            // Si hay proceso ejecutando y no terminó, continúa con él (FCFS no apropiativo)
            if (procesoEjecutando != null && 
                !procesoEjecutando.isFinished() && 
                !procesoEjecutando.estaEnES()) {
                
                System.out.println("🔄 FCFS mantiene en CPU: " + procesoEjecutando.getName());
                soEjecutando = false;
                semaforoCola.release();
                return procesoEjecutando;
            }
            
            // Si terminó o necesita E/S, limpiar proceso actual
            if (procesoEjecutando != null) {
                if (procesoEjecutando.isFinished()) {
                    procesoEjecutando.setTiempoFinalizacion(Reloj.getCurrentCycle());
                    System.out.println("✅ " + procesoEjecutando.getName() + " TERMINADO en FCFS");
                    procesoEjecutando.detenerEjecucion(); // 🧵 DETENER THREAD
                } else if (procesoEjecutando.estaEnES()) {
                    System.out.println("🔄 " + procesoEjecutando.getName() + " BLOQUEADO por E/S");
                    procesoEjecutando.pausarEjecucion(); // 🧵 PAUSAR THREAD
                }
                procesoEjecutando = null;
                cambiosContexto++;
            }
            
            // Tomar siguiente proceso de la cola (FCFS: primero en llegar)
            if (!colaListos.estaVacia()) {
                procesoEjecutando = (Proceso) colaListos.desencolar();
                
                // Registrar inicio de ejecución (si es primera vez)
                if (procesoEjecutando.getTiempoInicioEjecucion() == -1) {
                    procesoEjecutando.setTiempoInicioEjecucion(Reloj.getCurrentCycle());
                }
                
                procesoEjecutando.setState(Proceso.Estado.EJECUTANDO);
                
                // 🧵 CONTROL DE THREADS
                if (!procesoEjecutando.isEjecutando()) {
                    procesoEjecutando.iniciarEjecucion(); // 🧵 INICIAR THREAD
                } else {
                    procesoEjecutando.reanudarEjecucion(); // 🧵 REANUDAR THREAD
                }
                
                System.out.println("🎯 FCFS selecciona NUEVO proceso: " + procesoEjecutando.getName());
                cambiosContexto++;
            }
            
            soEjecutando = false;
            semaforoCola.release(); // 🔐 LIBERAR SEMÁFORO
            return procesoEjecutando;
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }
    
    @Override
    public void agregarProceso(Proceso proceso) {
        try {
            semaforoCola.acquire(); // 🔐 ADQUIRIR SEMÁFORO
            
            proceso.setState(Proceso.Estado.LISTO);
            colaListos.encolar(proceso);
            
            // 🧵 INICIAR THREAD DEL PROCESO (pero pausado inicialmente)
            if (proceso.getState() == Proceso.Estado.NUEVO) {
                proceso.iniciarEjecucion();
                proceso.pausarEjecucion(); // Pausar hasta que FCFS lo seleccione
            }
            
            System.out.println("📥 " + proceso.getName() + " agregado a FCFS - Thread iniciado");
            
            semaforoCola.release(); // 🔐 LIBERAR SEMÁFORO
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    @Override
    public void eliminarProceso(Proceso proceso) {
        try {
            semaforoCola.acquire(); // 🔐 ADQUIRIR SEMÁFORO
            
            if (procesoEjecutando == proceso) {
                if (procesoEjecutando != null) {
                    procesoEjecutando.detenerEjecucion(); // 🧵 DETENER THREAD
                }
                procesoEjecutando = null;
                cambiosContexto++;
            }
            
            // Eliminar de cola de listos
            Cola temp = new Cola();
            while (!colaListos.estaVacia()) {
                Proceso p = (Proceso) colaListos.desencolar();
                if (p != proceso) {
                    temp.encolar(p);
                } else {
                    p.detenerEjecucion(); // 🧵 DETENER THREAD
                    System.out.println("🗑️ " + p.getName() + " removido de FCFS - Thread detenido");
                }
            }
            while (!temp.estaVacia()) {
                colaListos.encolar(temp.desencolar());
            }
            
            semaforoCola.release(); // 🔐 LIBERAR SEMÁFORO
            
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
            semaforoCola.acquire(); // 🔐 ADQUIRIR SEMÁFORO
            boolean resultado = !colaListos.estaVacia() || procesoEjecutando != null;
            semaforoCola.release(); // 🔐 LIBERAR SEMÁFORO
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
    
    // 🔄 MÉTODOS NUEVOS PARA THREADS Y VISUALIZACIÓN
    
    /**
     * 📊 Obtiene estado completo del planificador con threads
     */
    public String getEstadoCompletoThreads() {
        try {
            semaforoCola.acquire();
            
            StringBuilder sb = new StringBuilder();
            sb.append("🖥️  CPU: ").append(procesoEjecutando != null ? 
                procesoEjecutando.getName() + " [EJECUTANDO]" : "LIBRE").append("\n");
            
            sb.append("📋 Cola Listos (").append(colaListos.getTamano()).append("): ");
            if (colaListos.estaVacia()) {
                sb.append("Vacía");
            } else {
                // Mostrar procesos en cola
                Cola temp = new Cola();
                while (!colaListos.estaVacia()) {
                    Proceso p = (Proceso) colaListos.desencolar();
                    sb.append(p.getName()).append("(").append(p.getState()).append(") ");
                    temp.encolar(p);
                }
                // Restaurar cola
                while (!temp.estaVacia()) {
                    colaListos.encolar(temp.desencolar());
                }
            }
            
            sb.append("\n🔀 Cambios contexto: ").append(cambiosContexto);
            sb.append("\n⏰ Ciclos totales: ").append(ciclosTotales);
            sb.append("\n🏃 Ejecutando: ").append(soEjecutando ? "SISTEMA OPERATIVO" : "PROCESO USUARIO");
            sb.append("\n🧵 Threads activos: ").append(contarThreadsActivos());
            
            semaforoCola.release();
            return sb.toString();
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "Error al obtener estado";
        }
    }
    
    /**
     * 🔢 Cuenta threads activos para monitoreo
     */
    private int contarThreadsActivos() {
        int activos = 0;
        if (procesoEjecutando != null && procesoEjecutando.isEjecutando()) {
            activos++;
        }
        
        // Contar threads en cola de listos
        Cola temp = new Cola();
        while (!colaListos.estaVacia()) {
            Proceso p = (Proceso) colaListos.desencolar();
            if (p.isEjecutando()) {
                activos++;
            }
            temp.encolar(p);
        }
        while (!temp.estaVacia()) {
            colaListos.encolar(temp.desencolar());
        }
        
        return activos;
    }
    
    // 🔹 MÉTODOS PARA MONITOREO (mantener compatibilidad)
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
    
    public String getEstadoCola() {
        try {
            semaforoCola.acquire();
            String estado = colaListos.estaVacia() ? "🟢 Cola vacía" : 
                           "📋 " + colaListos.getTamano() + " procesos en cola";
            semaforoCola.release();
            return estado;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "Error";
        }
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
        // Manejar proceso bloqueado por E/S
        eliminarProceso(proceso);
        agregarProceso(proceso); // Volver a cola
    }
    
    @Override
    public void procesoVolvioDeES(Proceso proceso) {
        // Proceso vuelve de E/S
        agregarProceso(proceso);
    }
    
    @Override
    public String getEstadoColas() {
        return getEstadoCompletoThreads();
    }
}