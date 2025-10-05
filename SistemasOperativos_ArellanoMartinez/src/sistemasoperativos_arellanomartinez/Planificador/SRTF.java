/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemasoperativos_arellanomartinez.Planificador;
import edd.ListaSimple;
import sistemasoperativos_arellanomartinez.Simulador.Proceso;
import sistemasoperativos_arellanomartinez.Simulador.Reloj;
/**
 *
 * @author raiza
 */


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
     * Es preemptivo: Interrumpe el actual si hay uno con menos tiempo restante en la lista.
     * @return El proceso seleccionado (o null si no hay ninguno).
     */
    public Proceso seleccionarProximoProceso() {
        // Buscar el proceso con menor tiempo RESTANTE (no total)
        Proceso procesoMasCorto = encontrarProcesoMasCorto();
        
        // Si no hay procesos disponibles, retorna null
        if (procesoMasCorto == null) {
            return null;
        }
        
        // SRTF es APROPIATIVO: SIEMPRE revisa si hay uno más corto
        if (procesoActual != null && 
            !procesoActual.getId().equals(procesoMasCorto.getId()) &&
            !procesoActual.isFinished()) {
            
            System.out.println("🔄 SRTF INTERRUMPE " + procesoActual.getName() + 
                             " por " + procesoMasCorto.getName() +
                             " (" + procesoActual.getInstruccionesRestantes() + 
                             " vs " + procesoMasCorto.getInstruccionesRestantes() + " restantes)");
            
            // Devolver el proceso interrumpido a la lista
            procesoActual.setState(Proceso.Estado.LISTO);
            listaProcesos.insertFinal(procesoActual);
        }
         // Si el más corto no es el actual (está en la lista), removerlo de la lista
        if (!procesoMasCorto.getId().equals((procesoActual != null ? procesoActual.getId() : null))) {
            listaProcesos.remove(procesoMasCorto);
        }
        
        // Establecer el nuevo proceso actual
        procesoActual = procesoMasCorto;
        procesoActual.setState(Proceso.Estado.EJECUTANDO);
        
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
     * @param proceso El proceso a agregar (no terminado).
     */
    public void agregarProceso(Proceso proceso) {
        if (!proceso.isFinished()) {
            proceso.setState(Proceso.Estado.LISTO);
            listaProcesos.insertFinal(proceso);
            System.out.println("✅ " + proceso.getName() + " agregado a SRTF" +
                             " (" + proceso.getInstruccionesRestantes() + " restantes)");
        }
    }
    
    /**
     * Reorganiza las colas si es necesario.
     * En SRTF, se reorganiza automáticamente en cada selección (no necesita acción adicional).
     */
    public void reorganizarColas() {
        // SRTF se reorganiza automáticamente en cada selección
        // No necesita reorganización adicional
    }
    
    /**
     * Verifica si hay procesos disponibles (en lista o actual no terminado).
     * @return true si hay procesos para ejecutar.
     */
    public boolean tieneProcesos() {
        return !listaProcesos.isEmpty() || 
               (procesoActual != null && !procesoActual.isFinished());
    }
    
     
    private Proceso encontrarProcesoMasCorto() {
        Proceso masCorto = procesoActual;
        int minRestantes = (procesoActual != null && !procesoActual.isFinished()) 
                            ? procesoActual.getInstruccionesRestantes() 
                            : Integer.MAX_VALUE;
        // 1. ITERAR sobre la lista de procesos listos para encontrar el más corto
        // Usa .get(i) para NO remover el proceso de la lista mientras se busca
        int size = listaProcesos.getSize(); 
        for (int i = 0; i < size; i++) {
            Proceso p = (Proceso) listaProcesos.get(i); 
            
            if (p.getInstruccionesRestantes() < minRestantes) {
                minRestantes = p.getInstruccionesRestantes();
                masCorto = p;
            }
        }
        
        return masCorto;
    }
    
     /**
     * Maneja un proceso que se bloqueó por E/S.
     * @param proceso El proceso bloqueado.
     */
    public void procesoBloqueado(Proceso proceso) {
        if (proceso != null && !proceso.isFinished()) {
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
     * @param proceso El proceso que volvió.
     */
    public void procesoVolvioDeES(Proceso proceso) {
        if (proceso != null && !proceso.isFinished()) {
            proceso.setState(Proceso.Estado.LISTO);
            listaProcesos.insertFinal(proceso);
            System.out.println("✅ " + proceso.getName() + " volvió de E/S a SRTF" +
                             " (" + proceso.getInstruccionesRestantes() + " restantes)");
        }
    }
    
      
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
        return sb.toString().trim();  // Trim para quitar espacio final
    }
    
/**
     * Getter para procesoActual (útil para tests).
     * @return El proceso actual.
     */
    public Proceso getProcesoActual() {
        return procesoActual;
    }
    
    /**
     * Obtiene el estado completo del planificador.
     * @return String con CPU y lista.
     */
    public String getEstadoCompleto() {
        String estadoActual = (procesoActual != null) ? 
            procesoActual.getName() + "(" + procesoActual.getInstruccionesRestantes() + "R)" : "Ninguno";
        
        return "CPU: " + estadoActual + " | Lista: " + getInfoLista();
    }
    /**
     * @return the nombreAlgoritmo
     */
    public String getNombreAlgoritmo() {
        return nombreAlgoritmo;
    }
}

        
