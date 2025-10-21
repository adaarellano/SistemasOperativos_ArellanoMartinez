/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package sistemasoperativos_arellanomartinez.Planificador;
import sistemasoperativos_arellanomartinez.Simulador.Proceso;

/**
 * Define el contrato que todos los algoritmos de planificación deben seguir. 
 * Esto permite cambiar dinámicamente entre diferentes políticas.
 * @author Indatech
 */
public interface Planificador {
    // 🔹 SELECCIÓN DEL SIGUIENTE PROCESO
    Proceso siguienteProceso();
    
    // 🔹 GESTIÓN DE COLAS
    void agregarProceso(Proceso proceso);
    void eliminarProceso(Proceso proceso);
    
    // 🔹 SINCRONIZACIÓN CON RELOJ
    void actualizarCiclo(int ciclo);
    
    // 🔹 CONTROL DE ESTADO
    boolean tieneProcesos();
    String getNombre();
    
    // 🔥 NUEVOS MÉTODOS REQUERIDOS POR SIMULATIONENGINE
    // 🔹 PARA COMPATIBILIDAD CON SIMULATIONENGINE
    default Proceso seleccionarProximoProceso() {
        return siguienteProceso();  // Método alias para compatibilidad
    }
    
    default String getNombreAlgoritmo() {
        return getNombre();  // Método alias para compatibilidad
    }
    
    // 🔹 MÉTODOS NUEVOS PARA GESTIÓN AVANZADA
    void reorganizarColas();  // Para algoritmos que necesitan reordenar
    
    // 🔹 PARA MANEJO DE E/S (opcional - algunos algoritmos pueden no implementarlo)
    default void procesoBloqueado(Proceso proceso) {
        // Implementación por defecto vacía
    }
    
    default void procesoVolvioDeES(Proceso proceso) {
        // Implementación por defecto vacía
        agregarProceso(proceso);  // Por defecto, volver a agregar a la cola
    }
    
    // 🔹 PARA OBTENER INFORMACIÓN DE DEBUGGING
    default String getEstadoColas() {
        return "Información no disponible";
    }
}