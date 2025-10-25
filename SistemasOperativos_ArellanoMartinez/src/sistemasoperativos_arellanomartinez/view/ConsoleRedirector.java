package sistemasoperativos_arellanomartinez.view;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import javax.swing.SwingUtilities;
import java.awt.Color;

/**
 * Redirige System.out y System.err a la ConsolaGamer
 */
public class ConsoleRedirector {
    private ConsolaGamer consola;
    private PrintStream originalOut;
    private PrintStream originalErr;

    public ConsoleRedirector(ConsolaGamer consola) {
        this.consola = consola;
        redirectSystemStreams();
    }

    private void redirectSystemStreams() {
        // Guardar los streams originales
        originalOut = System.out;
        originalErr = System.err;

        // Redirigir System.out
        PrintStream printStreamOut = new PrintStream(new CustomOutputStream(consola, Color.WHITE));
        System.setOut(printStreamOut);

        // Redirigir System.err
        PrintStream printStreamErr = new PrintStream(new CustomOutputStream(consola, Color.RED));
        System.setErr(printStreamErr);
    }

    public void restoreSystemStreams() {
        if (originalOut != null) {
            System.setOut(originalOut);
        }
        if (originalErr != null) {
            System.setErr(originalErr);
        }
    }

    /**
     * Custom OutputStream que envía todo a la ConsolaGamer
     */
    private class CustomOutputStream extends OutputStream {
        private ConsolaGamer consola;
        private Color color;
        private StringBuilder buffer;

        public CustomOutputStream(ConsolaGamer consola, Color color) {
            this.consola = consola;
            this.color = color;
            this.buffer = new StringBuilder();
        }

        @Override
        public void write(int b) throws IOException {
            // Convertir el byte a char
            char c = (char) b;
            
            // Si es un salto de línea, enviar el buffer completo
            if (c == '\n') {
                final String line = buffer.toString();
                SwingUtilities.invokeLater(() -> {
                    consola.agregarLinea(line, color);
                });
                buffer.setLength(0); // Limpiar el buffer
            } else {
                buffer.append(c); // Agregar al buffer
            }
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            String text = new String(b, off, len);
            final String[] lines = text.split("\n", -1);
            
            SwingUtilities.invokeLater(() -> {
                for (int i = 0; i < lines.length; i++) {
                    if (i > 0) {
                        // Enviar línea completa
                        consola.agregarLinea(buffer.toString(), color);
                        buffer.setLength(0);
                    }
                    if (i < lines.length - 1 || text.endsWith("\n")) {
                        consola.agregarLinea(lines[i], color);
                    } else {
                        buffer.append(lines[i]);
                    }
                }
            });
        }
    }
}