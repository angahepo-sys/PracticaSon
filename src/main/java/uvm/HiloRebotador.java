
package uvm;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;

public class HiloRebotador implements Runnable {
    private final JLabel[][] matriz;
    private int fila;
    private int col;
    private int dirFila = 1;
    private int dirCol = 1;
    private final int filasMax;
    private final int colsMax;

    private volatile boolean activo = true;
    private boolean pausado = false;

    public HiloRebotador(JLabel[][] matriz, int filaInicial, int colInicial) {
        this.matriz = matriz;
        this.fila = filaInicial;
        this.col = colInicial;
        this.filasMax = matriz.length;
        this.colsMax = matriz[0].length;
    }

    @Override
    public void run() {
        try {
            while (activo) {
                synchronized (this) {
                    while (pausado && activo) {
                        wait();
                    }
                }

                if (!activo) {
                    break;
                }

                final int nuevaFila;
                final int nuevaCol;

                synchronized (matriz) {
                    matriz[fila][col].setText("");

                    int siguienteFila = fila + dirFila;
                    int siguienteCol = col + dirCol;

                    if (siguienteFila < 0 || siguienteFila >= filasMax) {
                        dirFila = -dirFila;
                        siguienteFila = fila + dirFila;
                    }

                    if (siguienteCol < 0 || siguienteCol >= colsMax) {
                        dirCol = -dirCol;
                        siguienteCol = col + dirCol;
                    }

                    fila = siguienteFila;
                    col = siguienteCol;

                    nuevaFila = fila;
                    nuevaCol = col;
                }

                SwingUtilities.invokeLater(() -> {
                    matriz[nuevaFila][nuevaCol].setText("@");
                });

                Thread.sleep(200);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
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
    }
}
