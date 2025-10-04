/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package sistemasoperativos_arellanomartinez.Planificador;
import sistemasoperativos_arellanomartinez.Simulador.Proceso;


/**
 *Define el contrato que todos los algoritmos de planificación deben seguir. 
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
    
}
