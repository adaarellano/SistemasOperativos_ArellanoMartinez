/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemasoperativos_arellanomartinez.Planificador;
import sistemasoperativos_arellanomartinez.Simulador.Proceso;
import sistemasoperativos_arellanomartinez.Simulador.Reloj;
import edd.ListaSimple;

/**
 * 🎲 RANDOM - Algoritmo de planificación aleatorio
 * Selecciona procesos de forma aleatoria de la cola de listos
 */
public class Random implements Planificador {
    private ListaSimple colaListos;
    private Proceso procesoEjecutando;
    private java.util.Random generador;
    
    public Random() {
        this.colaListos = new ListaSimple();
        this.procesoEjecutando = null;
        this.generador = new java.util.Random();
    }
    
    @Override
    public Proceso siguienteProceso() {
        // Si hay proceso ejecutando y no ha terminado, continúa con él
        if (procesoEjecutando != null && !procesoEjecutando.isFinished()) {
            return procesoEjecutando;
        }
        
        // Si el proceso actual terminó, limpiarlo
        if (procesoEjecutando != null && procesoEjecutando.isFinished()) {
            procesoEjecutando.setTiempoFinalizacion(Reloj.getCurrentCycle());
            procesoEjecutando = null;
        }
        
        // Si no hay procesos en cola, retornar null
        if (colaListos.isEmpty()) {
            procesoEjecutando = null;
            return null;
        }
        
        try {
            // 🎲 SELECCIÓN ALEATORIA
            int indiceAleatorio = generador.nextInt(colaListos.sizeLista());
            procesoEjecutando = obtenerProcesoEnIndice(indiceAleatorio);
            
            if (procesoEjecutando == null) {
                System.out.println("❌ Error: Proceso en índice " + indiceAleatorio + " es null");
                return null;
            }
            
            // Remover el proceso seleccionado de la cola
            boolean removido = colaListos.remove(procesoEjecutando);
            if (!removido) {
                System.out.println("❌ No se pudo remover proceso de la cola");
            }
            
            // Registrar inicio de ejecución (si es primera vez)
            if (procesoEjecutando.getTiempoInicioEjecucion() == -1) {
                procesoEjecutando.setTiempoInicioEjecucion(Reloj.getCurrentCycle());
            }
            
            procesoEjecutando.setState(Proceso.Estado.EJECUTANDO);
            System.out.println("🎲 RANDOM selecciona: " + procesoEjecutando.getName() + 
                             " (índice: " + indiceAleatorio + ", cola: " + colaListos.sizeLista() + ")");
            
            return procesoEjecutando;
            
        } catch (Exception e) {
            System.out.println("❌ Error en Random.siguienteProceso(): " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    public void agregarProceso(Proceso proceso) {
        if (proceso == null) {
            System.out.println("❌ Intento de agregar proceso null a Random");
            return;
        }
        
        proceso.setState(Proceso.Estado.LISTO);
        colaListos.insertFinal(proceso);
        System.out.println("📥 " + proceso.getName() + " agregado a Random (cola: " + colaListos.sizeLista() + ")");
    }
    
    @Override
    public void eliminarProceso(Proceso proceso) {
        if (proceso == null) return;
        
        if (procesoEjecutando == proceso) {
            procesoEjecutando = null;
        }
        
        // Eliminar de la cola de listos
        colaListos.remove(proceso);
        System.out.println("🗑️ " + proceso.getName() + " eliminado de Random");
    }
    
    @Override
    public void actualizarCiclo(int ciclo) {
        // Manejar procesos bloqueados que vuelven a listos
        if (ciclo % 5 == 0 && procesoEjecutando != null && 
            procesoEjecutando.getState() == Proceso.Estado.BLOQUEADO) {
            System.out.println("🔄 " + procesoEjecutando.getName() + " vuelve de E/S en Random");
            procesoEjecutando.setState(Proceso.Estado.LISTO);
            agregarProceso(procesoEjecutando);
            procesoEjecutando = null;
        }
    }
    
    @Override
    public boolean tieneProcesos() {
        return !colaListos.isEmpty() || procesoEjecutando != null;
    }
    
    @Override
    public String getNombre() {
        return "Random (Selección Aleatoria)";
    }
    
    // 🔧 MÉTODOS AUXILIARES
    
    /**
     * Obtiene un proceso en un índice específico de la lista
     */
    private Proceso obtenerProcesoEnIndice(int indice) {
        if (indice < 0 || indice >= colaListos.sizeLista()) {
            return null;
        }
        
        try {
            Object obj = colaListos.get(indice);
            if (obj instanceof Proceso) {
                return (Proceso) obj;
            } else {
                System.out.println("❌ Error: Elemento en índice " + indice + " no es un Proceso");
                return null;
            }
        } catch (Exception e) {
            System.out.println("❌ Error al obtener proceso en índice " + indice + ": " + e.getMessage());
            return null;
        }
    }
    
    // 🔧 MÉTODOS PARA MONITOREO
    
    public Proceso getProcesoEjecutando() {
        return procesoEjecutando;
    }
    
    public int getTamanoColaListos() {
        return colaListos.sizeLista();
    }
    
    public String getEstadoCola() {
        if (colaListos.isEmpty()) {
            return "🟢 Cola vacía";
        }
        return "📋 " + colaListos.sizeLista() + " procesos en cola";
    }
    
    public String getEstadoAleatorio() {
        if (procesoEjecutando == null) {
            return "💤 Sin proceso";
        }
        return "🎲 " + procesoEjecutando.getName() + " ejecutándose";
    }
    
    /**
     * Muestra información de debug de la cola
     */
    public void mostrarEstadoCola() {
        System.out.println("=== COLA RANDOM (" + colaListos.sizeLista() + " procesos) ===");
        for (int i = 0; i < colaListos.sizeLista(); i++) {
            Proceso p = obtenerProcesoEnIndice(i);
            if (p != null) {
                System.out.println("  " + i + ": " + p.getName() + 
                                 " (PC: " + p.getPc() + "/" + p.getTotalInstructions() + 
                                 ", Estado: " + p.getState() + ")");
            }
        }
    }
}