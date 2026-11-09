
package uvm;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HiloCazador extends Thread {

    private static final Logger LOGGER =
            Logger.getLogger(HiloCazador.class.getName());

    private final JLabel[][] matriz;
    private final JLabel lblContador;

    private volatile boolean pausado = false;
    private volatile boolean terminado = false;
    private int encontrados = 0;

    public HiloCazador(JLabel[][] matriz, JLabel lblContador) {
        this.matriz = matriz;
        this.lblContador = lblContador;
    }

    @Override
    public void run() {
        try {
            recorrerMatriz();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (InvocationTargetException e) {
            LOGGER.log(Level.SEVERE,
                    "Error al actualizar la interfaz gráfica.", e);
        }
    }

    private void recorrerMatriz()
            throws InterruptedException, InvocationTargetException {

        for (int fila = 0; fila < matriz.length && !terminado; fila++) {
            for (int col = 0; col < matriz[fila].length && !terminado; col++) {
                if (!procesarCelda(matriz[fila][col])) {
                    return;
                }
            }
        }
    }

    private boolean procesarCelda(JLabel celda)
            throws InterruptedException, InvocationTargetException {

        if (!esperarActivo()) {
            return false;
        }

        String texto = obtenerTexto(celda);

        if ("@".equals(texto)) {
            buscarSimbolo(celda);
        }

        return !terminado;
    }

    private String obtenerTexto(JLabel celda)
            throws InterruptedException, InvocationTargetException {

        final String[] texto = new String[1];

        SwingUtilities.invokeAndWait(() ->
                texto[0] = celda.getText()
        );

        return texto[0];
    }

    private void buscarSimbolo(JLabel celda)
            throws InterruptedException, InvocationTargetException {

        Color colorOriginal = obtenerColor(celda);

        if (!parpadearCelda(celda, colorOriginal)) {
            return;
        }

        if (!esperarActivo()) {
            return;
        }

        SwingUtilities.invokeAndWait(() -> {
            if ("@".equals(celda.getText())) {
                celda.setText("X");
                celda.setForeground(Color.BLACK);
            }
        });

        encontrados++;
        actualizarContador();
    }

    private boolean parpadearCelda(JLabel celda, Color colorOriginal)
            throws InterruptedException, InvocationTargetException {

        for (int i = 0; i < 4; i++) {
            if (!esperarActivo()) {
                return false;
            }

            final Color color = (i % 2 == 0)
                    ? Color.RED : colorOriginal;

            SwingUtilities.invokeAndWait(() ->
                    celda.setForeground(color)
            );

            if (!esperarIntervalo(250)) {
                return false;
            }
        }

        return true;
    }

    private void actualizarContador()
            throws InterruptedException, InvocationTargetException {

        final int total = encontrados;

        SwingUtilities.invokeAndWait(() ->
                lblContador.setText("Símbolos: " + total)
        );
    }

    private Color obtenerColor(JLabel celda)
            throws InterruptedException, InvocationTargetException {

        final Color[] color = new Color[1];

        SwingUtilities.invokeAndWait(() ->
                color[0] = celda.getForeground()
        );

        return color[0];
    }

    private boolean esperarActivo() throws InterruptedException {
        synchronized (this) {
            while (pausado && !terminado) {
                wait();
            }
        }

        return !terminado;
    }

    private boolean esperarIntervalo(long milisegundos)
            throws InterruptedException {

        long inicio = System.currentTimeMillis();

        while (System.currentTimeMillis() - inicio < milisegundos) {
            if (!esperarActivo()) {
                return false;
            }

            long transcurrido = System.currentTimeMillis() - inicio;
            long restante = milisegundos - transcurrido;

            Thread.sleep(Math.min(50, Math.max(1, restante)));
        }

        return !terminado;
    }

    public synchronized void pausarContinuar() {
        pausado = !pausado;
        notifyAll();
    }

    public synchronized void terminar() {
        terminado = true;
        pausado = false;
        notifyAll();
        interrupt();
    }

    public boolean estaPausado() {
        return pausado;
    }
}
