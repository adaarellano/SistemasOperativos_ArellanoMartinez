/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemasoperativos_arellanomartinez.view;
import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;

/**
 * CONSOLA PERSONALIZADA CON ESTILO GAMER
 * Muestra texto con colores y formato estilo terminal gaming
 * @author Ada
 */
public class ConsolaGamer extends JTextPane {
    private StyledDocument doc;
    private SimpleAttributeSet estiloNormal;
    
    public ConsolaGamer() {
        configurarConsola();
    }
    
    private void configurarConsola() {
        doc = getStyledDocument();
        estiloNormal = new SimpleAttributeSet();
        
        // Configuración estilo gamer
        setEditable(false);
        setBackground(new Color(0, 0, 20)); // Fondo azul oscuro
        setForeground(Color.WHITE);
        setFont(new Font("Consolas", Font.PLAIN, 14));
        setCaretColor(Color.CYAN);
        
        // Borde estilo gaming
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 100, 200), 3),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
    }
    
    /**
     * Agrega una línea de texto con color específico
     */
    public void agregarLinea(String texto, Color color) {
        try {
            // Crear estilo con el color especificado
            SimpleAttributeSet estilo = new SimpleAttributeSet();
            StyleConstants.setForeground(estilo, color);
            StyleConstants.setFontFamily(estilo, "Consolas");
            StyleConstants.setFontSize(estilo, 14);
            
            // Agregar texto
            doc.insertString(doc.getLength(), texto + "\n", estilo);
            
            // Auto-scroll al final
            setCaretPosition(doc.getLength());
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Agrega una línea de texto con color por defecto (blanco)
     */
    public void agregarLinea(String texto) {
        agregarLinea(texto, Color.WHITE);
    }
    
    /**
     * Limpia la consola
     */
    public void limpiar() {
        try {
            doc.remove(0, doc.getLength());
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Agrega una línea de separación
     */
    public void agregarSeparador() {
        agregarLinea("=" .repeat(60), Color.YELLOW);
    }
}