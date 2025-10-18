/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemasoperativos_arellanomartinez.Planificador;
import sistemasoperativos_arellanomartinez.Simulador.Proceso;
import sistemasoperativos_arellanomartinez.Simulador.Proceso.Estado;
import edd.Cola;
import java.util.concurrent.Semaphore;

public class LJF implements Planificador {
    private Cola colaListos;
    private Cola colaBloqueados;
    private Proceso procesoEjecutando;
    private final Semaphore semaforoColas; // 🔐 SEMÁFORO NUEVO
    
    // Métricas para threads
    private int cambiosContexto;
    private int ciclosTotales;
    private boolean soEjecutando;
    private String nombre = "LJF";
    
    public LJF() {
        this.colaListos = new Cola();
        this.colaBloqueados = new Cola();
        this.procesoEjecutando = null;
        this.semaforoColas = new Semaphore(1); // 🔐 INICIALIZAR SEMÁFORO
        this.cambiosContexto = 0;
        this.ciclosTotales = 0;
        this.soEjecutando = false;
    }
    
    @Override
    public Proceso siguienteProceso() {
        try {
            semaforoColas.acquire(); // 🔐 ADQUIRIR SEMÁFORO
            
            soEjecutando = true;
            ciclosTotales++;
            
            // Si hay proceso ejecutando y no ha terminado, lo mantenemos (LJF no apropiativo)
            if (procesoEjecutando != null && 
                procesoEjecutando.getState() == Estado.EJECUTANDO &&
                !procesoEjecutando.isFinished() && 
                !procesoEjecutando.estaEnES()) {
                
                System.out.println("🔄 LJF mantiene en CPU: " + procesoEjecutando.getName() + 
                                 " (" + procesoEjecutando.getTotalInstructions() + " inst)");
                soEjecutando = false;
                semaforoColas.release();
                return procesoEjecutando;
            }
            
            procesoEjecutando = null;
            
            if (colaListos.estaVacia()) {
                soEjecutando = false;
                semaforoColas.release();
                return null;
            }
            
            // 🎯 LJF: Buscar el proceso con MAYOR totalInstructions
            Proceso procesoMasLargo = encontrarProcesoMasLargo();
            
            if (procesoMasLargo != null && procesoMasLargo.getPc() < procesoMasLargo.getTotalInstructions()) {
                procesoEjecutando = procesoMasLargo;
                procesoEjecutando.setState(Estado.EJECUTANDO);
                eliminarProcesoDeCola(procesoMasLargo);
                
                // 🧵 CONTROL DE THREADS
                if (!procesoEjecutando.isEjecutando()) {
                    procesoEjecutando.iniciarEjecucion(); // 🧵 INICIAR THREAD
                } else {
                    procesoEjecutando.reanudarEjecucion(); // 🧵 REANUDAR THREAD
                }
                
                System.out.println("🎯 LJF selecciona (más largo): " + procesoEjecutando.getName() + 
                                 " (" + procesoEjecutando.getTotalInstructions() + " inst)");
                cambiosContexto++;
            }
            
            soEjecutando = false;
            semaforoColas.release(); // 🔐 LIBERAR SEMÁFORO
            return procesoEjecutando;
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }
    
    /**
     * 🎯 Encuentra el proceso más largo (LJF)
     */
    private Proceso encontrarProcesoMasLargo() {
        Cola temp = new Cola();
        Proceso procesoMasLargo = null;
        int maxInstrucciones = -1;
        
        while (!colaListos.estaVacia()) {
            Proceso actual = (Proceso) colaListos.desencolar();
            
            // Solo considerar procesos que no hayan terminado
            if (actual.getPc() < actual.getTotalInstructions()) {
                if (procesoMasLargo == null || 
                    actual.getTotalInstructions() > maxInstrucciones) {
                    procesoMasLargo = actual;
                    maxInstrucciones = actual.getTotalInstructions();
                }
            }
            temp.encolar(actual);
        }
        
        // Restaurar la cola
        while (!temp.estaVacia()) {
            colaListos.encolar(temp.desencolar());
        }
        
        return procesoMasLargo;
    }
    
    // Método auxiliar para eliminar proceso específico de colaListos
    private void eliminarProcesoDeCola(Proceso proceso) {
        Cola temp = new Cola();
        while (!colaListos.estaVacia()) {
            Proceso actual = (Proceso) colaListos.desencolar();
            if (actual != proceso) {
                temp.encolar(actual);
            }
        }
        while (!temp.estaVacia()) {
            colaListos.encolar(temp.desencolar());
        }
    }
    
    @Override
    public void agregarProceso(Proceso proceso) {
        try {
            semaforoColas.acquire(); // 🔐 ADQUIRIR SEMÁFORO
            
            if (proceso.getState() == Estado.NUEVO || proceso.getState() == Estado.LISTO) {
                proceso.setState(Estado.LISTO);
                colaListos.encolar(proceso);
                
                // 🧵 INICIAR THREAD DEL PROCESO (pero pausado inicialmente)
                if (proceso.getState() == Estado.NUEVO) {
                    proceso.iniciarEjecucion();
                    proceso.pausarEjecucion(); // Pausar hasta que LJF lo seleccione
                }
                
                System.out.println("📥 " + proceso.getName() + " agregado a LJF - " + 
                                 proceso.getTotalInstructions() + " inst - Thread iniciado");
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
            
            if (procesoEjecutando == proceso) {
                if (procesoEjecutando != null) {
                    procesoEjecutando.detenerEjecucion(); // 🧵 DETENER THREAD
                }
                procesoEjecutando = null;
                cambiosContexto++;
            }
            
            // Eliminar de cola de listos
            eliminarProcesoDeCola(proceso);
            
            // Eliminar de cola de bloqueados
            Cola temp = new Cola();
            while (!colaBloqueados.estaVacia()) {
                Proceso actual = (Proceso) colaBloqueados.desencolar();
                if (actual != proceso) {
                    temp.encolar(actual);
                } else {
                    actual.detenerEjecucion(); // 🧵 DETENER THREAD
                    System.out.println("🗑️ " + actual.getName() + " removido de LJF - Thread detenido");
                }
            }
            while (!temp.estaVacia()) {
                colaBloqueados.encolar(temp.desencolar());
            }
            
            semaforoColas.release(); // 🔐 LIBERAR SEMÁFORO
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    @Override
    public void actualizarCiclo(int ciclo) {
        try {
            semaforoColas.acquire(); // 🔐 ADQUIRIR SEMÁFORO
            
            // 1. Manejar proceso en ejecución
            if (procesoEjecutando != null && procesoEjecutando.getState() == Estado.EJECUTANDO) {
                
                // Verificar si terminó
                if (procesoEjecutando.isFinished()) {
                    procesoEjecutando.setState(Estado.TERMINADO);
                    procesoEjecutando.detenerEjecucion(); // 🧵 DETENER THREAD
                    System.out.println("✅ " + procesoEjecutando.getName() + " TERMINADO (LJF)");
                    procesoEjecutando = null;
                    semaforoColas.release();
                    return;
                }
                
                // Verificar si necesita E/S (basado en ciclos de excepción E/S)
                if (procesoEjecutando.getPc() > 0 && 
                    procesoEjecutando.debeGenerarES() && 
                    !procesoEjecutando.estaEnES()) {
                    
                    procesoEjecutando.setState(Estado.BLOQUEADO);
                    procesoEjecutando.pausarEjecucion(); // 🧵 PAUSAR THREAD
                    colaBloqueados.encolar(procesoEjecutando);
                    System.out.println("🔄 " + procesoEjecutando.getName() + " BLOQUEADO por E/S (LJF)");
                    procesoEjecutando = null;
                    cambiosContexto++;
                    semaforoColas.release();
                    return;
                }
            }
            
            // 2. Procesos bloqueados vuelven a lista de listos (simulación simple)
            if (!colaBloqueados.estaVacia() && ciclo % 5 == 0) { // Cada 5 ciclos vuelve uno
                Cola temp = new Cola();
                while (!colaBloqueados.estaVacia()) {
                    Proceso bloqueado = (Proceso) colaBloqueados.desencolar();
                    if (!bloqueado.estaEnES()) {
                        bloqueado.setState(Estado.LISTO);
                        colaListos.encolar(bloqueado);
                        System.out.println("🔄 " + bloqueado.getName() + " VUELVE de E/S a LISTO (LJF)");
                        break; // Solo uno por ciclo
                    } else {
                        temp.encolar(bloqueado);
                    }
                }
                // Restaurar los que no volvieron
                while (!temp.estaVacia()) {
                    colaBloqueados.encolar(temp.desencolar());
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
            boolean resultado = !colaListos.estaVacia() || !colaBloqueados.estaVacia() || procesoEjecutando != null;
            semaforoColas.release(); // 🔐 LIBERAR SEMÁFORO
            return resultado;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
    
    @Override
    public String getNombre() {
        return this.nombre;
    }
    
    // 🔄 MÉTODOS NUEVOS PARA THREADS Y VISUALIZACIÓN
    
    /**
     * 📊 Obtiene estado completo del planificador con threads
     */
    public String getEstadoCompletoThreads() {
        try {
            semaforoColas.acquire();
            
            StringBuilder sb = new StringBuilder();
            sb.append("🖥️  CPU: ").append(procesoEjecutando != null ? 
                procesoEjecutando.getName() + " (" + procesoEjecutando.getTotalInstructions() + " inst) [EJECUTANDO]" : "LIBRE").append("\n");
            
            sb.append("📋 Cola Listos (").append(colaListos.getTamano()).append("): ");
            if (colaListos.estaVacia()) {
                sb.append("Vacía");
            } else {
                // Mostrar procesos en cola ordenados por longitud
                mostrarProcesosOrdenadosLJF(sb);
            }
            
            sb.append("\n⏳ Cola Bloqueados (").append(colaBloqueados.getTamano()).append("): ");
            if (colaBloqueados.estaVacia()) {
                sb.append("Vacía");
            } else {
                Cola temp = new Cola();
                while (!colaBloqueados.estaVacia()) {
                    Proceso p = (Proceso) colaBloqueados.desencolar();
                    sb.append(p.getName()).append("(").append(p.getTiempoESRestante()).append(") ");
                    temp.encolar(p);
                }
                while (!temp.estaVacia()) {
                    colaBloqueados.encolar(temp.desencolar());
                }
            }
            
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
     * 📈 Muestra procesos ordenados por longitud (LJF)
     */
    private void mostrarProcesosOrdenadosLJF(StringBuilder sb) {
        // Crear lista temporal para ordenar
        Cola temp = new Cola();
        java.util.ArrayList<Proceso> procesos = new java.util.ArrayList<>();
        
        while (!colaListos.estaVacia()) {
            Proceso p = (Proceso) colaListos.desencolar();
            procesos.add(p);
            temp.encolar(p);
        }
        
        // Ordenar por longitud descendente (LJF)
        procesos.sort((p1, p2) -> Integer.compare(p2.getTotalInstructions(), p1.getTotalInstructions()));
        
        // Mostrar ordenados
        for (Proceso p : procesos) {
            sb.append(p.getName()).append("(").append(p.getTotalInstructions()).append(") ");
        }
        
        // Restaurar cola
        while (!temp.estaVacia()) {
            colaListos.encolar(temp.desencolar());
        }
    }
    
    /**
     * 🔢 Cuenta threads activos para monitoreo
     */
    private int contarThreadsActivos() {
        int activos = 0;
        if (procesoEjecutando != null && procesoEjecutando.isEjecutando()) {
            activos++;
        }
        
        // Contar threads en cola de listos
        activos += contarThreadsEnCola(colaListos);
        // Contar threads en cola de bloqueados  
        activos += contarThreadsEnCola(colaBloqueados);
        
        return activos;
    }
    
    private int contarThreadsEnCola(Cola cola) {
        int activos = 0;
        Cola temp = new Cola();
        
        while (!cola.estaVacia()) {
            Proceso p = (Proceso) cola.desencolar();
            if (p.isEjecutando()) {
                activos++;
            }
            temp.encolar(p);
        }
        
        // Restaurar cola
        while (!temp.estaVacia()) {
            cola.encolar(temp.desencolar());
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
        // LJF no necesita reorganizar colas explícitamente
    }
    
    @Override
    public void procesoBloqueado(Proceso proceso) {
        // Ya se maneja en actualizarCiclo()
    }
    
    @Override
    public void procesoVolvioDeES(Proceso proceso) {
        // Ya se maneja en actualizarCiclo()
    }
    
    @Override
    public String getEstadoColas() {
        return getEstadoCompletoThreads();
    }
    
    // 🔹 MÉTODOS PARA MONITOREO
    public Proceso getProcesoEjecutando() {
        return procesoEjecutando;
    }
    
    public int getTamanoColaListos() {
        try {
            semaforoColas.acquire();
            int tamano = colaListos.getTamano();
            semaforoColas.release();
            return tamano;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return 0;
        }
    }
    
    public int getTamanoColaBloqueados() {
        try {
            semaforoColas.acquire();
            int tamano = colaBloqueados.getTamano();
            semaforoColas.release();
            return tamano;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return 0;
        }
    }
}