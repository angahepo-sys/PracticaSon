package uvm;

import javax.swing.*;
import java.awt.Color;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HiloSerpiente extends Thread {

    private static final Logger LOGGER = Logger.getLogger(HiloSerpiente.class.getName());

    private final JLabel[][] matriz;
    private volatile boolean running = true;
    private boolean pausado = false;

    private int fila = 0;
    private int columna = 0;
    private int numero = 1;

    public HiloSerpiente(JLabel[][] matriz) {
        this.matriz = matriz;
    }

    @Override
    public void run() {
        while (running && numero <= matriz.length * matriz[0].length) {
            try {
                esperarActivo();

                if (running) {
                    escribirNumero();
                    avanzarPosicion();
                    Thread.sleep(400);
                }

            } catch (InterruptedException e) {
                if (running) {
                    LOGGER.log(Level.WARNING, "[HiloSerpiente] Interrumpido.", e);
                }
                Thread.currentThread().interrupt();
                running = false;
            }
        }

        LOGGER.info("[HiloSerpiente] Recorrido finalizado.");
    }

    private synchronized void esperarActivo() throws InterruptedException {
        while (pausado && running) {
            wait();
        }
    }

    private void escribirNumero() throws InterruptedException {
        final int filaActual = fila;
        final int columnaActual = columna;
        final int numeroActual = numero;

        try {
            SwingUtilities.invokeAndWait(() -> {
                matriz[filaActual][columnaActual].setText(String.valueOf(numeroActual));
                matriz[filaActual][columnaActual].setForeground(Color.BLUE);
            });

            numero++;

        } catch (InvocationTargetException e) {
            LOGGER.log(Level.SEVERE,
                    "[HiloSerpiente] Error al escribir en la matriz.", e);
            running = false;
        }
    }

    private void avanzarPosicion() {
        int columnas = matriz[0].length;

        if (fila % 2 == 0) {
            avanzarDerecha(columnas);
        } else {
            avanzarIzquierda();
        }
    }

    private void avanzarDerecha(int columnas) {
        if (columna < columnas - 1) {
            columna++;
        } else {
            fila++;
        }
    }

    private void avanzarIzquierda() {
        if (columna > 0) {
            columna--;
        } else {
            fila++;
        }
    }

    public synchronized void continuar() {
        pausado = false;
        notifyAll();
    }

    public synchronized void pausar() {
        pausado = true;
    }

    public void terminar() {
        running = false;
        interrupt();
    }
}

