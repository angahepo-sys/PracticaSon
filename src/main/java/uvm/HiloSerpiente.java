package uvm;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedList;
import java.util.Random;

public class HiloSerpiente extends Thread {
    private final JLabel[][] matriz;
    private volatile boolean running = true;
    private boolean pausado = false;
    
    // Representa las coordenadas de la serpiente en la matriz
    private final LinkedList<Point> cuerpo = new LinkedList<>();
    private int direccionFila = 0;
    private int direccionCol = 1; // Empieza moviéndose hacia la derecha
    private final Random random = new Random();

    public HiloSerpiente(JLabel[][] matriz) {
        this.matriz = matriz;
        // Posición inicial de la serpiente
        cuerpo.add(new Point(0, 0));
        cuerpo.add(new Point(0, 1));
        cuerpo.add(new Point(0, 2));
    }

    @Override
    public void run() {
        int filas = matriz.length;
        int columnas = matriz[0].length;

        while (running) {
            try {
                // Velocidad de movimiento de la serpiente (en milisegundos)
                Thread.sleep(400);

                // Control de pausa sincronizado
                synchronized (this) {
                    while (pausado) {
                        wait();
                    }
                }

                // Actualizar la interfaz gráfica de forma segura en el EDT
                SwingUtilities.invokeLater(() -> {
                    // 1. Borrar la cola de la serpiente
                    if (!cuerpo.isEmpty()) {
                        Point cola = cuerpo.peekLast();
                        if (cola != null && cola.x >= 0 && cola.x < filas && cola.y >= 0 && cola.y < columnas) {
                            matriz[cola.x][cola.y].setBackground(Color.WHITE);
                            matriz[cola.x][cola.y].setText("-");
                        }
                    }

                    // 2. Calcular la nueva cabeza basada en la dirección actual
                    Point cabezaActual = cuerpo.peekFirst();
                    int nuevaFila = cabezaActual.x + direccionFila;
                    int nuevaCol = cabezaActual.y + direccionCol;

                    // Si choca con las paredes, cambia de dirección aleatoriamente
                    if (nuevaFila < 0 || nuevaFila >= filas || nuevaCol < 0 || nuevaCol >= columnas) {
                        cambiarDireccionAleatoria();
                        nuevaFila = cabezaActual.x + direccionFila;
                        nuevaCol = cabezaActual.y + direccionCol;
                        
                        // Asegurar límites seguros
                        nuevaFila = Math.max(0, Math.min(nuevaFila, filas - 1));
                        nuevaCol = Math.max(0, Math.min(nuevaCol, columnas - 1));
                    }

                    // Agregar nueva cabeza
                    Point nuevaCabeza = new Point(nuevaFila, nuevaCol);
                    cuerpo.addFirst(nuevaCabeza);
                    
                    // Limitar el tamaño de la serpiente
                    if (cuerpo.size() > 15) {
                        cuerpo.removeLast();
                    }

                    // 3. Pintar la serpiente en la matriz (Color Verde)
                    for (int i = 0; i < cuerpo.size(); i++) {
                        Point p = cuerpo.get(i);
                        matriz[p.x][p.y].setBackground(Color.GREEN);
                        matriz[p.x][p.y].setText(i == 0 ? "O" : "o");
                    }
                });

                // Ocasionalmente cambia de dirección para recorrer toda la matriz
                if (random.nextInt(10) == 0) {
                    cambiarDireccionAleatoria();
                }

            } catch (InterruptedException e) {
                System.out.println("⚠️ [HiloSerpiente] Interrumpido.");
                Thread.currentThread().interrupt();
                break;
            }
        }
        System.out.println("🛑 [HiloSerpiente] Finalizado.");
    }

    private void cambiarDireccionAleatoria() {
        int opcion = random.nextInt(4);
        switch (opcion) {
            case 0: // Arriba
                direccionFila = -1;
                direccionCol = 0;
                break;
            case 1: // Abajo
                direccionFila = 1;
                direccionCol = 0;
                break;
            case 2: // Izquierda
                direccionFila = 0;
                direccionCol = -1;
                break;
            case 3: // Derecha
                direccionFila = 0;
                direccionCol = 1;
                break;
        }
    }

    // Métodos para el control desde la ventana
public synchronized void continuar() {
    pausado = false;
    notifyAll();
}

public synchronized void pausar() {
    pausado = true;
}

public void terminar() {
    running = false;
    this.interrupt();
}
}