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
    
    // principales
    Proceso seleccionarProximoProceso();
    void agregarProceso(Proceso proceso);
    void eliminarProceso(Proceso proceso);
    void procesoVolvioDeES(Proceso proceso);
    void procesoBloqueado(Proceso proceso);
    
    // de consulta
    String getNombreAlgoritmo();
    String getEstadoColas();
    boolean tieneProcesos();
    
    // de ciclos
    void actualizarCiclo(int ciclo);
    void reorganizarColas();
    
    default Proceso siguienteProceso() {
        return seleccionarProximoProceso();
    }
    
    default String getNombre() {
        return getNombreAlgoritmo();
    }
}