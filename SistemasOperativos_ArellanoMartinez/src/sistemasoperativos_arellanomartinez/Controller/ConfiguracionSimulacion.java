/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemasoperativos_arellanomartinez.Controller;
import sistemasoperativos_arellanomartinez.Simulador.ProcesoData;
import java.util.List;
/**
 *
 * @author Day y Ada
 */
public class ConfiguracionSimulacion {
    public int duracionCicloMs;
    public List<ProcesoData> procesos;

    public ConfiguracionSimulacion() {} // Requerido por Gson
}