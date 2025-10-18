/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemasoperativos_arellanomartinez.Planificador;

import sistemasoperativos_arellanomartinez.Simulador.Proceso;
import sistemasoperativos_arellanomartinez.Simulador.Reloj;
import edd.Cola;
import java.util.concurrent.Semaphore;

/**
 * 🔄 ROUND ROBIN - Algoritmo de planificación por turnos
 * Asigna un quantum de tiempo a cada proceso
 */
public class RR implements Planificador {
    private Cola colaListos;
    private Proceso procesoEjecutando;
    private final int quantum;
    private int tiempoRestanteQuantum;
    private final Semaphore semaforoCola; // 🔐 SEMÁFORO NUEVO
    
    // Métricas para threads
    private int cambiosContexto;
    private int ciclosTotales;
    private boolean soEjecutando;
    private int quantumCompletados;
    
    public RR() {
        this(3); // Quantum por defecto: 3
    }
    
    public RR(int quantum) {
        this.colaListos = new Cola();
        this.procesoEjecutando = null;
        this.quantum = quantum;
        this.tiempoRestanteQuantum = 0;
        this.semaforoCola = new Semaphore(1); // 🔐 INICIALIZAR SEMÁFORO
        this.cambiosContexto = 0;
        this.ciclosTotales = 0;
        this.soEjecutando = false;
        this.quantumCompletados = 0;
    }
    
    @Override
    public Proceso siguienteProceso() {
        try {
            semaforoCola.acquire(); // 🔐 ADQUIRIR SEMÁFORO
            
            soEjecutando = true;
            ciclosTotales++;
            
            // 🔄 VERIFICAR SI HAY QUE CAMBIAR DE PROCESO (RR APROPIATIVO)
            boolean cambiarProceso = debeCambiarProceso();
            
            if (cambiarProceso) {
                manejarCambioProceso();
            }
            
            // 🎯 SELECCIONAR NUEVO PROCESO SI ES NECESARIO
            if (procesoEjecutando == null && !colaListos.estaVacia()) {
                seleccionarNuevoProceso();
            }
            
            // ⏱️ ACTUALIZAR QUANTUM SI HAY PROCESO EJECUTANDO
            if (procesoEjecutando != null && procesoEjecutando.getState() == Proceso.Estado.EJECUTANDO) {
                tiempoRestanteQuantum--;
                System.out.println("⏱️  RR Quantum: " + (tiempoRestanteQuantum + 1) + "/" + quantum + 
                                 " para " + procesoEjecutando.getName());
            }
            
            soEjecutando = false;
            semaforoCola.release(); // 🔐 LIBERAR SEMÁFORO
            return procesoEjecutando;
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }
    
    /**
     * 🔄 Determina si debe cambiar de proceso (quantum agotado o proceso bloqueado)
     */
    private boolean debeCambiarProceso() {
        if (procesoEjecutando == null) return true;
        
        // 1. Quantum agotado
        if (tiempoRestanteQuantum <= 0) {
            System.out.println("🔄 RR: Quantum agotado para " + procesoEjecutando.getName());
            return true;
        }
        
        // 2. Proceso terminado
        if (procesoEjecutando.isFinished()) {
            System.out.println("🔄 RR: " + procesoEjecutando.getName() + " terminó");
            return true;
        }
        
        // 3. Proceso bloqueado por E/S
        if (procesoEjecutando.estaEnES()) {
            System.out.println("🔄 RR: " + procesoEjecutando.getName() + " bloqueado por E/S");
            return true;
        }
        
        return false;
    }
    
    /**
     * 🔄 Maneja el cambio de proceso (RR apropiativo)
     */
    private void manejarCambioProceso() {
        if (procesoEjecutando != null) {
            // 🧵 PAUSAR THREAD DEL PROCESO ACTUAL
            procesoEjecutando.pausarEjecucion();
            
            if (procesoEjecutando.isFinished()) {
                // Proceso terminó
                procesoEjecutando.setTiempoFinalizacion(Reloj.getCurrentCycle());
                procesoEjecutando.detenerEjecucion();
                System.out.println("✅ " + procesoEjecutando.getName() + " TERMINADO en RR");
            } else if (procesoEjecutando.estaEnES()) {
                // Proceso bloqueado por E/S
                procesoEjecutando.setState(Proceso.Estado.BLOQUEADO);
                System.out.println("⏸️  " + procesoEjecutando.getName() + " BLOQUEADO por E/S en RR");
            } else {
                // Quantum agotado - volver a cola
                procesoEjecutando.setState(Proceso.Estado.LISTO);
                colaListos.encolar(procesoEjecutando);
                System.out.println("🔁 " + procesoEjecutando.getName() + " vuelve a cola (quantum agotado)");
                quantumCompletados++;
            }
            
            procesoEjecutando = null;
            cambiosContexto++;
        }
    }
    
    /**
     * 🎯 Selecciona nuevo proceso de la cola
     */
    private void seleccionarNuevoProceso() {
        procesoEjecutando = (Proceso) colaListos.desencolar();
        
        // Registrar inicio de ejecución (si es primera vez)
        if (procesoEjecutando.getTiempoInicioEjecucion() == -1) {
            procesoEjecutando.setTiempoInicioEjecucion(Reloj.getCurrentCycle());
        }
        
        procesoEjecutando.setState(Proceso.Estado.EJECUTANDO);
        tiempoRestanteQuantum = quantum - 1; // -1 porque ya usamos 1 ciclo
        
        // 🧵 CONTROL DE THREADS
        if (!procesoEjecutando.isEjecutando()) {
            procesoEjecutando.iniciarEjecucion(); // 🧵 INICIAR THREAD
        } else {
            procesoEjecutando.reanudarEjecucion(); // 🧵 REANUDAR THREAD
        }
        
        System.out.println("🎯 RR selecciona: " + procesoEjecutando.getName() + 
                         " (Quantum: " + quantum + " ciclos)");
        cambiosContexto++;
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
                proceso.pausarEjecucion(); // Pausar hasta que RR lo seleccione
            }
            
            System.out.println("📥 " + proceso.getName() + " agregado a RR - Thread iniciado");
            
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
                tiempoRestanteQuantum = 0;
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
                    System.out.println("🗑️ " + p.getName() + " removido de RR - Thread detenido");
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
    
    // 🔄 MÉTODOS NUEVOS PARA VISUALIZACIÓN
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
            
            sb.append("\n⏱️  Quantum actual: ").append(tiempoRestanteQuantum + 1).append("/").append(quantum);
            sb.append("\n🔀 Cambios contexto: ").append(cambiosContexto);
            sb.append("\n🔄 Quantums completados: ").append(quantumCompletados);
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
    
    // 🔹 MÉTODOS RESTANTES DE LA INTERFAZ
    @Override
    public void actualizarCiclo(int ciclo) {
        // RR no necesita hacer nada especial por ciclo
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
        return "Round Robin (Quantum: " + quantum + ")";
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
    
    public String getEstadoQuantum() {
        if (procesoEjecutando == null) {
            return "💤 Sin proceso";
        }
        return "⏱️  Quantum: " + (tiempoRestanteQuantum + 1) + "/" + quantum;
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
        // RR no necesita reorganizar colas
    }
    
    @Override
    public void procesoBloqueado(Proceso proceso) {
        // Manejar proceso bloqueado por E/S
        eliminarProceso(proceso);
        agregarProceso(proceso); // Volver a cola cuando termine E/S
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
    
    // 🔧 GETTERS ESPECÍFICOS DE RR
    public int getQuantum() {
        return quantum;
    }
    
    public int getTiempoRestanteQuantum() {
        return tiempoRestanteQuantum;
    }
    
    public int getQuantumCompletados() {
        return quantumCompletados;
    }
}
