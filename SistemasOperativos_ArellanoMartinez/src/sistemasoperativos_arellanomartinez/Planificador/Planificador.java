/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package sistemasoperativos_arellanomartinez.Planificador;
import sistemasoperativos_arellanomartinez.Simulador.Proceso;

/**
 * Define el contrato que todos los algoritmos de planificación deben seguir. 
 * Esto permite cambiar dinámicamente entre diferentes políticas.
 * @author Ada y Day
 */

public interface Planificador {
    
    // MÉTODOS PRINCIPALES
    Proceso seleccionarProximoProceso();
    void agregarProceso(Proceso proceso);
    void eliminarProceso(Proceso proceso);
    void procesoVolvioDeES(Proceso proceso);
    void procesoBloqueado(Proceso proceso);
    
    // MÉTODOS DE CONSULTA
    String getNombreAlgoritmo();
    String getEstadoColas();
    boolean tieneProcesos();
    
    // MÉTODOS DE CICLO
    void actualizarCiclo(int ciclo);
    void reorganizarColas();
    
    // MÉTODO OPCIONAL - para compatibilidad
    default Proceso siguienteProceso() {
        return seleccionarProximoProceso();
    }
    
    default String getNombre() {
        return getNombreAlgoritmo();
    }
}