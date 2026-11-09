package uvm;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HiloRebotador implements Runnable {

    private static final Logger LOGGER =
            Logger.getLogger(HiloRebotador.class.getName());
    private static final long INTERVALO_MOVIMIENTO = 200L;

    private final JLabel[][] matriz;
    private final int filasMax;
    private final int columnasMax;

    private int fila;
    private int columna;
    private int direccionFila = 1;
    private int direccionColumna = 1;

    private volatile boolean activo = true;
    private boolean pausado;
    private volatile Thread ejecutor;

    public HiloRebotador(JLabel[][] matriz, int filaInicial,
            int columnaInicial) {
        validarMatriz(matriz);

        if (filaInicial < 0 || filaInicial >= matriz.length
                || columnaInicial < 0
                || columnaInicial >= matriz[0].length) {
            throw new IllegalArgumentException(
                    "La posición inicial está fuera de la matriz.");
        }

        this.matriz = matriz;
        this.fila = filaInicial;
        this.columna = columnaInicial;
        this.filasMax = matriz.length;
        this.columnasMax = matriz[0].length;
    }

    private static void validarMatriz(JLabel[][] matriz) {
        if (matriz == null || matriz.length < 2
                || matriz[0] == null || matriz[0].length < 2) {
            throw new IllegalArgumentException(
                    "La matriz debe tener al menos dos filas y dos columnas.");
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
        ejecutor = Thread.currentThread();

        try {
            while (activo) {
                esperarActivo();

                if (!activo) {
                    break;
                }

                mover();
                Thread.sleep(INTERVALO_MOVIMIENTO);
            }
        } catch (InterruptedException e) {
            if (activo) {
                LOGGER.log(Level.WARNING,
                        "[HiloRebotador] Hilo interrumpido.", e);
            }
            Thread.currentThread().interrupt();
        } finally {
            ejecutor = null;
            LOGGER.info("[HiloRebotador] Finalizado.");
        }
    }

    private synchronized void esperarActivo() throws InterruptedException {
        while (pausado && activo) {
            wait();
        }
    }

    private void mover() throws InterruptedException {
        int filaAnterior = fila;
        int columnaAnterior = columna;

        calcularSiguientePosicion();

        int nuevaFila = fila;
        int nuevaColumna = columna;

        SwingUtilities.invokeAndWait(() -> {
            matriz[filaAnterior][columnaAnterior].setText("");
            matriz[nuevaFila][nuevaColumna].setText("@");
        });
    }

    private void calcularSiguientePosicion() {
        int siguienteFila = fila + direccionFila;
        int siguienteColumna = columna + direccionColumna;

        if (siguienteFila < 0 || siguienteFila >= filasMax) {
            direccionFila = -direccionFila;
            siguienteFila = fila + direccionFila;
        }

        if (siguienteColumna < 0
                || siguienteColumna >= columnasMax) {
            direccionColumna = -direccionColumna;
            siguienteColumna = columna + direccionColumna;
        }

        fila = siguienteFila;
        columna = siguienteColumna;
    }

    public synchronized void pausar() {
        pausado = true;
    }

    public synchronized void reanudar() {
        pausado = false;
        notifyAll();
    }

    public synchronized void detener() {
        activo = false;
        pausado = false;
        notifyAll();

        Thread hilo = ejecutor;
        if (hilo != null) {
            hilo.interrupt();
        }
    }
}
