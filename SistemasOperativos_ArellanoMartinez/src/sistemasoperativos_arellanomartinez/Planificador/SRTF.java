/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemasoperativos_arellanomartinez.Planificador;
import edd.ListaSimple;
import sistemasoperativos_arellanomartinez.Simulador.Proceso;
import sistemasoperativos_arellanomartinez.Simulador.Reloj;

public class SRTF {
    
    private Proceso procesoActual;
    private ListaSimple listaProcesos;
    private final String nombreAlgoritmo;
    
    public SRTF() {
        this.listaProcesos = new ListaSimple();
        this.procesoActual = null;
        this.nombreAlgoritmo = "SRTF";
    }
    
    /**
     * Selecciona el próximo proceso a ejecutar según SRTF (Shortest Remaining Time First).
     * Adaptado para trabajar con procesos que tienen hilos propios.
     */
    public Proceso seleccionarProximoProceso() {
        // Buscar el proceso con menor tiempo RESTANTE
        Proceso procesoMasCorto = encontrarProcesoMasCorto();
        
        if (procesoMasCorto == null) {
            return null;
        }
        
        // 🔄 MANEJO DE HILOS - SRTF APROPIATIVO
        if (procesoActual != null && 
            !procesoActual.getId().equals(procesoMasCorto.getId()) &&
            !procesoActual.isFinished()) {
            
            System.out.println("🔄 SRTF INTERRUMPE " + procesoActual.getName() + 
                             " por " + procesoMasCorto.getName() +
                             " (" + procesoActual.getInstruccionesRestantes() + 
                             " vs " + procesoMasCorto.getInstruccionesRestantes() + " restantes)");
            
            // ⏸️ Pausar el hilo del proceso actual
            procesoActual.pausarEjecucion();
            
            // Devolver el proceso interrumpido a la lista
            procesoActual.setState(Proceso.Estado.LISTO);
            listaProcesos.insertFinal(procesoActual);
        }
        
        // Si el más corto no es el actual, removerlo de la lista
        if (!procesoMasCorto.getId().equals((procesoActual != null ? procesoActual.getId() : null))) {
            listaProcesos.remove(procesoMasCorto);
        }
        
        // ▶️ Iniciar/Reanudar el hilo del nuevo proceso actual
        procesoActual = procesoMasCorto;
        
        if (procesoActual.getState() == Proceso.Estado.LISTO || 
            procesoActual.getState() == Proceso.Estado.SUS_LISTO) {
            procesoActual.reanudarEjecucion();
        } else {
            procesoActual.iniciarEjecucion();
        }
        
        // Registrar tiempo de inicio si es la primera vez
        if (procesoActual.getTiempoInicioEjecucion() == -1) {
            procesoActual.setTiempoInicioEjecucion(Reloj.getCurrentCycle());
        }
        
        System.out.println("🎯 SRTF ejecuta: " + procesoActual.getName() + 
                         " (" + procesoActual.getInstruccionesRestantes() + " restantes)");
        
        return procesoActual;
    }
    
    /**
     * Agrega un proceso a la lista de listos.
     * Inicia el hilo del proceso si es nuevo.
     */
    public void agregarProceso(Proceso proceso) {
        if (!proceso.isFinished()) {
            proceso.setState(Proceso.Estado.LISTO);
            listaProcesos.insertFinal(proceso);
            
            // ▶️ Iniciar el hilo del proceso si está en estado NUEVO
            if (proceso.getState() == Proceso.Estado.NUEVO) {
                proceso.iniciarEjecucion();
                proceso.pausarEjecucion(); // Lo pausamos hasta que sea seleccionado
            }
            
            System.out.println("✅ " + proceso.getName() + " agregado a SRTF" +
                             " (" + proceso.getInstruccionesRestantes() + " restantes)");
        }
    }
    
    /**
     * Encuentra el proceso con menos instrucciones restantes
     * Considera tanto el proceso actual como los en lista
     */
    private Proceso encontrarProcesoMasCorto() {
        Proceso masCorto = null;
        int minRestantes = Integer.MAX_VALUE;
        
        // Considerar el proceso actual si existe y no ha terminado
        if (procesoActual != null && !procesoActual.isFinished()) {
            masCorto = procesoActual;
            minRestantes = procesoActual.getInstruccionesRestantes();
        }
        
        // Buscar en la lista de procesos listos
        int size = listaProcesos.getSize(); 
        for (int i = 0; i < size; i++) {
            Proceso p = (Proceso) listaProcesos.get(i); 
            
            if (p != null && !p.isFinished() && p.getInstruccionesRestantes() < minRestantes) {
                minRestantes = p.getInstruccionesRestantes();
                masCorto = p;
            }
        }
        
        return masCorto;
    }
    
    /**
     * Maneja un proceso que se bloqueó por E/S.
     * Pausa su hilo de ejecución.
     */
    public void procesoBloqueado(Proceso proceso) {
        if (proceso != null && !proceso.isFinished()) {
            // ⏸️ Pausar el hilo del proceso bloqueado
            proceso.pausarEjecucion();
            proceso.setState(Proceso.Estado.BLOQUEADO);
            
            System.out.println("⏳ " + proceso.getName() + " bloqueado por E/S en SRTF");
            
            // Si el proceso bloqueado es el actual, limpiarlo
            if (procesoActual != null && procesoActual.getId().equals(proceso.getId())) {
                procesoActual = null;
            }
        }
    }
    
    /**
     * Maneja un proceso que volvió de E/S (lo agrega a listos).
     * El hilo permanece pausado hasta que sea seleccionado.
     */
    public void procesoVolvioDeES(Proceso proceso) {
        if (proceso != null && !proceso.isFinished()) {
            proceso.setState(Proceso.Estado.LISTO);
            listaProcesos.insertFinal(proceso);
            
            // El hilo sigue pausado hasta que SRTF lo seleccione
            System.out.println("✅ " + proceso.getName() + " volvió de E/S a SRTF" +
                             " (" + proceso.getInstruccionesRestantes() + " restantes)");
        }
    }
    
    /**
     * Ejecuta un ciclo del proceso actual
     * En esta implementación con hilos, el proceso se ejecuta automáticamente
     * cuando está en estado EJECUTANDO
     */
    public void ejecutarCiclo() {
        if (procesoActual != null && procesoActual.getState() == Proceso.Estado.EJECUTANDO) {
            // El proceso se ejecuta automáticamente en su hilo
            // Solo verificamos si terminó
            if (procesoActual.isFinished()) {
                System.out.println("🎉 " + procesoActual.getName() + " terminó en SRTF");
                procesoActual = null;
            }
        }
    }
    
    /**
     * Limpia el proceso actual cuando termina
     */
    public void procesoTerminado(Proceso proceso) {
        if (proceso != null && proceso.isFinished()) {
            // ⏹️ Detener el hilo del proceso terminado
            proceso.detenerEjecucion();
            
            if (procesoActual != null && procesoActual.getId().equals(proceso.getId())) {
                procesoActual = null;
            }
            
            // Remover de la lista si está allí
            listaProcesos.remove(proceso);
            
            System.out.println("🏁 " + proceso.getName() + " removido de SRTF (terminado)");
        }
    }
    
    /**
     * Obtiene información de la lista de procesos
     */
    public String getInfoLista() {
        if (listaProcesos.isEmpty()) {
            return "Lista vacía";
        }
        
        StringBuilder sb = new StringBuilder();
        int size = listaProcesos.getSize();
        for (int i = 0; i < size; i++) {
            Proceso p = (Proceso) listaProcesos.get(i);
            sb.append(p.getName())
              .append("(").append(p.getInstruccionesRestantes()).append("R)")
              .append("[").append(p.getState()).append("] ");
        }
        return sb.toString().trim();
    }
    
    /**
     * Obtiene el estado completo del planificador
     */
    public String getEstadoCompleto() {
        String estadoActual = (procesoActual != null) ? 
            procesoActual.getName() + "(" + procesoActual.getInstruccionesRestantes() + "R)" : "Ninguno";
        
        return "CPU: " + estadoActual + " | Lista: " + getInfoLista();
    }
    
    // 🔹 GETTERS
    public Proceso getProcesoActual() {
        return procesoActual;
    }
    
    public String getNombreAlgoritmo() {
        return nombreAlgoritmo;
    }
    
    public boolean tieneProcesos() {
        return !listaProcesos.isEmpty() || 
               (procesoActual != null && !procesoActual.isFinished());
    }
    
    /**
     * Reorganiza las colas si es necesario
     */
    public void reorganizarColas() {
        // SRTF se reorganiza automáticamente en cada selección
    }
    
    /**
     * Limpia todos los procesos (para reinicio del sistema)
     */
    public void limpiar() {
        // Detener todos los hilos
        if (procesoActual != null) {
            procesoActual.detenerEjecucion();
            procesoActual = null;
        }
        
        int size = listaProcesos.getSize();
        for (int i = 0; i < size; i++) {
            Proceso p = (Proceso) listaProcesos.get(i);
            if (p != null) {
                p.detenerEjecucion();
            }
        }
        
        listaProcesos = new ListaSimple();
    }
}