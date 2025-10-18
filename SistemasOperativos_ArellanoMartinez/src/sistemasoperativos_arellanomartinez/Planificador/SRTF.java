/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemasoperativos_arellanomartinez.Planificador;
import edd.ListaSimple;
import sistemasoperativos_arellanomartinez.Simulador.Proceso;
import sistemasoperativos_arellanomartinez.Simulador.Reloj;
import sistemasoperativos_arellanomartinez.Simulador.Proceso.Estado;
import java.util.concurrent.Semaphore;

public class SRTF implements Planificador {
    private Proceso procesoActual;
    private ListaSimple listaProcesos;
    private ListaSimple colaBloqueados;
    private final Semaphore semaforoColas; // 🔐 SEMÁFORO NUEVO
    
    // Métricas para threads
    private int cambiosContexto;
    private int ciclosTotales;
    private boolean soEjecutando;
    private final String nombreAlgoritmo;
    
    public SRTF() {
        this.listaProcesos = new ListaSimple();
        this.colaBloqueados = new ListaSimple();
        this.procesoActual = null;
        this.semaforoColas = new Semaphore(1); // 🔐 INICIALIZAR SEMÁFORO
        this.cambiosContexto = 0;
        this.ciclosTotales = 0;
        this.soEjecutando = false;
        this.nombreAlgoritmo = "SRTF";
    }
    
    @Override
    public Proceso siguienteProceso() {
        try {
            semaforoColas.acquire(); // 🔐 ADQUIRIR SEMÁFORO
            
            soEjecutando = true;
            ciclosTotales++;
            
            // 🎯 SRTF: Buscar el proceso con menor tiempo RESTANTE (apropiativo)
            Proceso procesoMasCorto = encontrarProcesoMasCorto();
            
            if (procesoMasCorto == null) {
                soEjecutando = false;
                semaforoColas.release();
                return null;
            }
            
            // 🔄 MANEJO DE APROPIACIÓN - SRTF PUEDE INTERRUMPIR
            if (procesoActual != null && 
                !procesoActual.getId().equals(procesoMasCorto.getId()) &&
                !procesoActual.isFinished()) {
                
                // ⏸️ Interrumpir proceso actual si hay uno más corto
                procesoActual.pausarEjecucion(); // 🧵 PAUSAR THREAD
                procesoActual.setState(Estado.LISTO);
                listaProcesos.insertFinal(procesoActual);
                
                System.out.println("🔄 SRTF INTERRUMPE " + procesoActual.getName() + 
                                 " (" + procesoActual.getInstruccionesRestantes() + " restantes)" +
                                 " por " + procesoMasCorto.getName() + 
                                 " (" + procesoMasCorto.getInstruccionesRestantes() + " restantes)");
                cambiosContexto++;
            }
            
            // Remover el proceso seleccionado de la lista
            if (!procesoMasCorto.getId().equals((procesoActual != null ? procesoActual.getId() : null))) {
                listaProcesos.remove(procesoMasCorto);
            }
            
            // 🎯 Establecer nuevo proceso actual
            procesoActual = procesoMasCorto;
            
            // 🧵 CONTROL DE THREADS
            if (procesoActual.getState() == Estado.LISTO || procesoActual.getState() == Estado.SUS_LISTO) {
                procesoActual.reanudarEjecucion(); // 🧵 REANUDAR THREAD
            } else {
                procesoActual.iniciarEjecucion(); // 🧵 INICIAR THREAD
            }
            
            procesoActual.setState(Estado.EJECUTANDO);
            
            // Registrar tiempo de inicio si es la primera vez
            if (procesoActual.getTiempoInicioEjecucion() == -1) {
                procesoActual.setTiempoInicioEjecucion(Reloj.getCurrentCycle());
            }
            
            System.out.println("🎯 SRTF ejecuta: " + procesoActual.getName() + 
                             " (" + procesoActual.getInstruccionesRestantes() + " restantes)");
            cambiosContexto++;
            
            soEjecutando = false;
            semaforoColas.release(); // 🔐 LIBERAR SEMÁFORO
            return procesoActual;
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }
    
    /**
     * 🎯 Encuentra el proceso con menos instrucciones restantes
     */
    private Proceso encontrarProcesoMasCorto() {
        Proceso masCorto = null;
        int minRestantes = Integer.MAX_VALUE;
        
        // Considerar el proceso actual si existe y no ha terminado
        if (procesoActual != null && !procesoActual.isFinished() && !procesoActual.estaEnES()) {
            masCorto = procesoActual;
            minRestantes = procesoActual.getInstruccionesRestantes();
        }
        
        // Buscar en la lista de procesos listos
        for (int i = 0; i < listaProcesos.sizeLista(); i++) {
            Proceso p = (Proceso) listaProcesos.get(i);
            
            if (p != null && !p.isFinished() && !p.estaEnES() && 
                p.getInstruccionesRestantes() < minRestantes) {
                minRestantes = p.getInstruccionesRestantes();
                masCorto = p;
            }
        }
        
        return masCorto;
    }
    
    @Override
    public void agregarProceso(Proceso proceso) {
        try {
            semaforoColas.acquire(); // 🔐 ADQUIRIR SEMÁFORO
            
            if (!proceso.isFinished()) {
                proceso.setState(Estado.LISTO);
                listaProcesos.insertFinal(proceso);
                
                // 🧵 INICIAR THREAD DEL PROCESO (pero pausado inicialmente)
                if (proceso.getState() == Estado.NUEVO) {
                    proceso.iniciarEjecucion();
                    proceso.pausarEjecucion(); // Pausar hasta que SRTF lo seleccione
                }
                
                System.out.println("✅ " + proceso.getName() + " agregado a SRTF" +
                                 " (" + proceso.getInstruccionesRestantes() + " restantes)");
            }
            
            semaforoColas.release(); // 🔐 LIBERAR SEMÁFORO
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    @Override
    public void eliminarProceso(Proceso proceso) {
        try {
            semaforoColas.acquire(); // 🔐 ADQUIRIR SEMÁFORO
            
            if (procesoActual != null && procesoActual.getId().equals(proceso.getId())) {
                procesoActual.detenerEjecucion(); // 🧵 DETENER THREAD
                procesoActual = null;
                cambiosContexto++;
            }
            
            // Remover de lista de procesos
            listaProcesos.remove(proceso);
            
            // Remover de cola de bloqueados
            colaBloqueados.remove(proceso);
            
            proceso.detenerEjecucion(); // 🧵 DETENER THREAD
            
            System.out.println("🗑️ " + proceso.getName() + " removido de SRTF");
            
            semaforoColas.release(); // 🔐 LIBERAR SEMÁFORO
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    @Override
    public void actualizarCiclo(int ciclo) {
        try {
            semaforoColas.acquire(); // 🔐 ADQUIRIR SEMÁFORO
            
            // 1. Manejar proceso actual si terminó
            if (procesoActual != null && procesoActual.isFinished()) {
                procesoActual.detenerEjecucion(); // 🧵 DETENER THREAD
                System.out.println("🎉 " + procesoActual.getName() + " terminó en SRTF");
                procesoActual = null;
            }
            
            // 2. Procesos bloqueados vuelven a lista de listos
            if (!colaBloqueados.isEmpty() && ciclo % 3 == 0) { // Cada 3 ciclos
                for (int i = 0; i < colaBloqueados.sizeLista(); i++) {
                    Proceso p = (Proceso) colaBloqueados.get(i);
                    if (!p.estaEnES()) {
                        colaBloqueados.remove(p);
                        p.setState(Estado.LISTO);
                        listaProcesos.insertFinal(p);
                        System.out.println("✅ " + p.getName() + " volvió de E/S a SRTF");
                        break; // Solo uno por ciclo
                    }
                }
            }
            
            semaforoColas.release(); // 🔐 LIBERAR SEMÁFORO
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    @Override
    public boolean tieneProcesos() {
        try {
            semaforoColas.acquire(); // 🔐 ADQUIRIR SEMÁFORO
            boolean resultado = !listaProcesos.isEmpty() || !colaBloqueados.isEmpty() || 
                              (procesoActual != null && !procesoActual.isFinished());
            semaforoColas.release(); // 🔐 LIBERAR SEMÁFORO
            return resultado;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
    
    @Override
    public String getNombre() {
        return this.nombreAlgoritmo;
    }
    
    // 🔄 MÉTODOS ESPECÍFICOS DE SRTF (compatibilidad)
    
    /**
     * Maneja un proceso que se bloqueó por E/S
     */
    public void procesoBloqueado(Proceso proceso) {
        try {
            semaforoColas.acquire();
            
            if (proceso != null && !proceso.isFinished()) {
                proceso.pausarEjecucion(); // 🧵 PAUSAR THREAD
                proceso.setState(Estado.BLOQUEADO);
                colaBloqueados.insertFinal(proceso);
                
                System.out.println("⏳ " + proceso.getName() + " bloqueado por E/S en SRTF");
                
                // Si el proceso bloqueado es el actual, limpiarlo
                if (procesoActual != null && procesoActual.getId().equals(proceso.getId())) {
                    procesoActual = null;
                    cambiosContexto++;
                }
            }
            
            semaforoColas.release();
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Maneja un proceso que volvió de E/S
     */
    public void procesoVolvioDeES(Proceso proceso) {
        agregarProceso(proceso); // Reutilizar el método existente
    }
    
    /**
     * Limpia el proceso actual cuando termina
     */
    public void procesoTerminado(Proceso proceso) {
        eliminarProceso(proceso); // Reutilizar el método existente
    }
    
    // 🔄 MÉTODOS NUEVOS PARA VISUALIZACIÓN
    
    /**
     * 📊 Obtiene estado completo del planificador con threads
     */
    public String getEstadoCompleto() {
        try {
            semaforoColas.acquire();
            
            StringBuilder sb = new StringBuilder();
            sb.append("🖥️  CPU: ").append(procesoActual != null ? 
                procesoActual.getName() + " (" + procesoActual.getInstruccionesRestantes() + "R) [EJECUTANDO]" : "LIBRE").append("\n");
            
            sb.append("📋 Lista Listos (").append(listaProcesos.sizeLista()).append("): ");
            sb.append(getInfoLista(listaProcesos));
            
            sb.append("\n⏳ Cola Bloqueados (").append(colaBloqueados.sizeLista()).append("): ");
            sb.append(getInfoLista(colaBloqueados));
            
            sb.append("\n🔀 Cambios contexto: ").append(cambiosContexto);
            sb.append("\n⏰ Ciclos totales: ").append(ciclosTotales);
            sb.append("\n🏃 Ejecutando: ").append(soEjecutando ? "SISTEMA OPERATIVO" : "PROCESO USUARIO");
            sb.append("\n🧵 Threads activos: ").append(contarThreadsActivos());
            
            semaforoColas.release();
            return sb.toString();
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "Error al obtener estado";
        }
    }
    
    /**
     * Obtiene información de una lista de procesos
     */
    private String getInfoLista(ListaSimple lista) {
        if (lista.isEmpty()) {
            return "Vacía";
        }
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lista.sizeLista(); i++) {
            Proceso p = (Proceso) lista.get(i);
            sb.append(p.getName())
              .append("(").append(p.getInstruccionesRestantes()).append("R)")
              .append("[").append(p.getState()).append("] ");
        }
        return sb.toString().trim();
    }
    
    /**
     * 🔢 Cuenta threads activos para monitoreo
     */
    private int contarThreadsActivos() {
        int activos = 0;
        
        if (procesoActual != null && procesoActual.isEjecutando()) {
            activos++;
        }
        
        // Contar en lista de procesos
        activos += contarThreadsEnLista(listaProcesos);
        // Contar en cola de bloqueados
        activos += contarThreadsEnLista(colaBloqueados);
        
        return activos;
    }
    
    private int contarThreadsEnLista(ListaSimple lista) {
        int activos = 0;
        for (int i = 0; i < lista.sizeLista(); i++) {
            Proceso p = (Proceso) lista.get(i);
            if (p.isEjecutando()) {
                activos++;
            }
        }
        return activos;
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
        // SRTF se reorganiza automáticamente en cada selección
    }
    
    @Override
    public String getEstadoColas() {
        return getEstadoCompleto();
    }
    
    // 🔹 GETTERS ESPECÍFICOS
    public Proceso getProcesoActual() {
        return procesoActual;
    }
    
    public String getInfoLista() {
        return getInfoLista(listaProcesos);
    }
    
    /**
     * Limpia todos los procesos (para reinicio del sistema)
     */
    public void limpiar() {
        try {
            semaforoColas.acquire();
            
            // Detener todos los threads
            if (procesoActual != null) {
                procesoActual.detenerEjecucion();
                procesoActual = null;
            }
            
            // Detener threads en lista de procesos
            for (int i = 0; i < listaProcesos.sizeLista(); i++) {
                Proceso p = (Proceso) listaProcesos.get(i);
                if (p != null) {
                    p.detenerEjecucion();
                }
            }
            
            // Detener threads en cola de bloqueados
            for (int i = 0; i < colaBloqueados.sizeLista(); i++) {
                Proceso p = (Proceso) colaBloqueados.get(i);
                if (p != null) {
                    p.detenerEjecucion();
                }
            }
            
            listaProcesos = new ListaSimple();
            colaBloqueados = new ListaSimple();
            cambiosContexto = 0;
            ciclosTotales = 0;
            
            semaforoColas.release();
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}