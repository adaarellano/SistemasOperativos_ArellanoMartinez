/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemasoperativos_arellanomartinez.Simulador;

/**
 *
 * @author Day y Ada
 */
public class ProcesoData {
    public String nombre;
    public int totalInstructions;
    public boolean isCpuBound;
    public int ciclosExcepcionES;
    public int duracionES;

    // Constructor vacío necesario para Gson
    public ProcesoData() {}

    public ProcesoData(Proceso p) {
        this.nombre = p.getName();
        this.totalInstructions = p.getTotalInstructions();
        this.isCpuBound = p.isCpuBound();
        this.ciclosExcepcionES = p.getProximaExcepcionES();
        this.duracionES = p.getDuracionES();
    }
   }
      
