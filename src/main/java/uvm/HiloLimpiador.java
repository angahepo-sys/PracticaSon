package uvm;

import javax.swing.*;
import java.awt.Color;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HiloLimpiador extends Thread {

    private static final Logger LOGGER = Logger.getLogger(HiloLimpiador.class.getName());

    private static final int TAMANO_BLOQUE = 5;

    private final JLabel[][] matriz;
    private volatile boolean running = true;
    private boolean pausado = false;

    private int bloqueActual = 0;

    public HiloLimpiador(JLabel[][] matriz) {
        this.matriz = matriz;
    }

    @Override
    public void run() {
        while (running) {
            try {
                Thread.sleep(1000);
                esperarActivo();

                if (running) {
                    SwingUtilities.invokeAndWait(this::revisarMatriz);
                }

            } catch (InterruptedException e) {
                if (running) {
                    LOGGER.log(Level.WARNING, "[HiloLimpiador] Interrumpido.", e);
                }
                Thread.currentThread().interrupt();
                running = false;

            } catch (InvocationTargetException e) {
                LOGGER.log(Level.SEVERE,
                        "[HiloLimpiador] Error al revisar la matriz.", e);
                running = false;
            }
        }

        LOGGER.info("[HiloLimpiador] Finalizado.");
    }

    private synchronized void esperarActivo() throws InterruptedException {
        while (pausado && running) {
            wait();
        }
    }

    private void revisarMatriz() {
        int totalCeldas = matriz.length * matriz[0].length;
        int celdasModificadas = contarCeldasModificadas();

        double porcentaje = (double) celdasModificadas / totalCeldas;

        if (porcentaje > 0.5) {
            LOGGER.log(Level.INFO,
                    "[HiloLimpiador] {0}% de la matriz modificada. Limpiando bloque...",
                    porcentaje * 100);

            limpiarSiguienteBloque();
        }
    }

    private int contarCeldasModificadas() {
        int celdasModificadas = 0;

        for (int i = 0; i < matriz.length; i++) {
            for (int j = 0; j < matriz[i].length; j++) {
                JLabel celda = matriz[i][j];

                boolean textoModificado = !"-".equals(celda.getText());
                boolean textoColorModificado = !Color.BLACK.equals(celda.getForeground());
                boolean fondoModificado = !Color.WHITE.equals(celda.getBackground());

                if (textoModificado || textoColorModificado || fondoModificado) {
                    celdasModificadas++;
                }
            }
        }

        return celdasModificadas;
    }

    private void limpiarSiguienteBloque() {
        int bloquesPorFila = (matriz[0].length + TAMANO_BLOQUE - 1) / TAMANO_BLOQUE;
        int totalBloques = bloquesPorFila
                * ((matriz.length + TAMANO_BLOQUE - 1) / TAMANO_BLOQUE);

        int inicioFila = (bloqueActual / bloquesPorFila) * TAMANO_BLOQUE;
        int inicioColumna = (bloqueActual % bloquesPorFila) * TAMANO_BLOQUE;

        int finFila = Math.min(inicioFila + TAMANO_BLOQUE, matriz.length);
        int finColumna = Math.min(inicioColumna + TAMANO_BLOQUE, matriz[0].length);

        for (int i = inicioFila; i < finFila; i++) {
            for (int j = inicioColumna; j < finColumna; j++) {
                matriz[i][j].setText("-");
                matriz[i][j].setForeground(Color.BLACK);
                matriz[i][j].setBackground(Color.WHITE);
            }
        }

        LOGGER.info("[HiloLimpiador] Bloque limpiado correctamente.");

        bloqueActual = (bloqueActual + 1) % totalBloques;
    }

    public synchronized void pausarHilo() {
        pausado = true;
    }

    public synchronized void reanudarHilo() {
        pausado = false;
        notifyAll();
    }

    public void terminarHilo() {
        running = false;
        interrupt();
    }
}
