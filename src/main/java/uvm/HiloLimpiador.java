package uvm;

import javax.swing.*;
import java.awt.*;

public class HiloLimpiador extends Thread {
    private final JLabel[][] matriz;
    private volatile boolean running = true;
    private boolean pausado = false;

    public HiloLimpiador(JLabel[][] matriz) {
        this.matriz = matriz;
    }

    @Override
    public void run() {
        int totalCeldas = matriz.length * matriz[0].length;

        while (running) {
            try {
                Thread.sleep(1000);

                synchronized (this) {
                    while (pausado) {
                        wait();
                    }
                }

                int celdasModificadas = 0;
                for (int i = 0; i < matriz.length; i++) {
                    for (int j = 0; j < matriz[i].length; j++) {
                        if (!matriz[i][j].getBackground().equals(Color.WHITE) || !matriz[i][j].getText().equals("-")) {
                            celdasModificadas++;
                        }
                    }
                }

                double porcentaje = (double) celdasModificadas / totalCeldas;

                if (porcentaje > 0.5) {
                    System.out.println(" [HiloLimpiador] > 50% modificado (" + (porcentaje * 100) + "%). Limpiando bloque...");

                    SwingUtilities.invokeLater(() -> {
                        limpiarBloqueMatriz();
                    });
                }

            } catch (InterruptedException e) {
                System.out.println(" [HiloLimpiador] Interrumpido.");
                Thread.currentThread().interrupt();
                break;
            }
        }
        System.out.println(" [HiloLimpiador] Finalizado.");
    }

    private void limpiarBloqueMatriz() {
        int filas = matriz.length;
        int columnas = matriz[0].length;

        for (int i = 0; i < filas / 2; i++) {
            for (int j = 0; j < columnas; j++) {
                matriz[i][j].setBackground(Color.WHITE);
                matriz[i][j].setText("-");
            }
        }
        System.out.println(" [HiloLimpiador] Bloque de la matriz limpiado y restablecido.");
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
        this.interrupt(); 
    }
    
}