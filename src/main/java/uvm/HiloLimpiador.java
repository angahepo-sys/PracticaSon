package uvm;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HiloLimpiador extends Thread {

    private static final Logger LOGGER =
            Logger.getLogger(HiloLimpiador.class.getName());

    private static final int TAMANO_BLOQUE = 5;
    private static final double LIMITE_MODIFICACION = 0.5;
    private static final long INTERVALO_REVISION = 1000L;

    private final JLabel[][] matriz;
    private volatile boolean running = true;
    private boolean pausado;
    private int bloqueActual;

    public HiloLimpiador(JLabel[][] matriz) {
        validarMatriz(matriz);
        this.matriz = matriz;
    }

    private static void validarMatriz(JLabel[][] matriz) {
        if (matriz == null || matriz.length == 0 || matriz[0] == null
                || matriz[0].length == 0) {
            throw new IllegalArgumentException(
                    "La matriz debe contener al menos una celda.");
        }

        int columnas = matriz[0].length;

        for (JLabel[] fila : matriz) {
            if (fila == null || fila.length != columnas) {
                throw new IllegalArgumentException(
                        "La matriz debe ser rectangular.");
            }

            for (JLabel celda : fila) {
                if (celda == null) {
                    throw new IllegalArgumentException(
                            "La matriz no puede contener celdas nulas.");
                }
            }
        }
    }

    @Override
    public void run() {
        while (running) {
            try {
                Thread.sleep(INTERVALO_REVISION);
                esperarActivo();

                if (running) {
                    SwingUtilities.invokeAndWait(this::revisarMatriz);
                }
            } catch (InterruptedException e) {
                if (running) {
                    LOGGER.log(Level.WARNING,
                            "[HiloLimpiador] Hilo interrumpido.", e);
                }
                running = false;
                Thread.currentThread().interrupt();
            } catch (InvocationTargetException e) {
                LOGGER.log(Level.SEVERE,
                        "[HiloLimpiador] No fue posible revisar la matriz.",
                        e.getCause());
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

        if (porcentaje > LIMITE_MODIFICACION) {
            LOGGER.log(Level.INFO,
                    "[HiloLimpiador] {0}% de la matriz modificada. Limpiando bloque...",
                    porcentaje * 100);
            limpiarSiguienteBloque();
        }
    }

    private int contarCeldasModificadas() {
        int modificadas = 0;

        for (JLabel[] fila : matriz) {
            for (JLabel celda : fila) {
                boolean textoModificado = !"-".equals(celda.getText());
                boolean colorModificado =
                        !Color.BLACK.equals(celda.getForeground());
                boolean fondoModificado =
                        !Color.WHITE.equals(celda.getBackground());

                if (textoModificado || colorModificado || fondoModificado) {
                    modificadas++;
                }
            }
        }

        return modificadas;
    }

    private void limpiarSiguienteBloque() {
        int bloquesPorFila =
                (matriz[0].length + TAMANO_BLOQUE - 1) / TAMANO_BLOQUE;
        int bloquesPorColumna =
                (matriz.length + TAMANO_BLOQUE - 1) / TAMANO_BLOQUE;
        int totalBloques = bloquesPorFila * bloquesPorColumna;

        int inicioFila =
                (bloqueActual / bloquesPorFila) * TAMANO_BLOQUE;
        int inicioColumna =
                (bloqueActual % bloquesPorFila) * TAMANO_BLOQUE;
        int finFila = Math.min(inicioFila + TAMANO_BLOQUE, matriz.length);
        int finColumna =
                Math.min(inicioColumna + TAMANO_BLOQUE, matriz[0].length);

        for (int fila = inicioFila; fila < finFila; fila++) {
            for (int columna = inicioColumna;
                    columna < finColumna; columna++) {
                JLabel celda = matriz[fila][columna];
                celda.setText("-");
                celda.setForeground(Color.BLACK);
                celda.setBackground(Color.WHITE);
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

    public synchronized void terminarHilo() {
        running = false;
        pausado = false;
        notifyAll();
        interrupt();
    }
}
