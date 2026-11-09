
package uvm;

import javax.swing.*;
import java.awt.*;
import java.util.logging.Logger;

public class VentanaMatrizHilos extends JFrame {

    private static final Logger LOGGER =
            Logger.getLogger(VentanaMatrizHilos.class.getName());

    private static final String TEXTO_INICIAR = "Iniciar";
    private static final String TEXTO_PAUSA = "Pausa";
    private static final String TEXTO_TERMINAR = "Terminar";

    private transient HiloCazador hcaz;
    private transient HiloLimpiador hlim;
    private transient HiloRebotador hreb;
    private transient Thread hiloRebotadorThread;
    private transient HiloSerpiente serpiente;

    // Matriz de 15x15 JLabels
    private JLabel[][] matriz = new JLabel[15][15];

    // Contador de símbolos encontrados
    private JLabel lblContador = new JLabel("Símbolos: 0");

    // Botones Hilo Serpiente
    private JButton btIniciarS;
    private JButton btPausaS;
    private JButton btTerminarS;

    // Botones Hilo Cazador
    private JButton btIniciarC;
    private JButton btPausaC;
    private JButton btTerminarC;

    // Botones Hilo Rebotador
    private JButton btIniciarR;
    private JButton btPausaR;
    private JButton btTerminarR;

    // Botones Hilo Clima
    private JButton btIniciarCl;
    private JButton btPausaCl;
    private JButton btTerminarCl;

    // Botones Hilo Limpiador
    private JButton btIniciarL;
    private JButton btPausaL;
    private JButton btTerminarL;

    public VentanaMatrizHilos() {
        super("Práctica de Concurrencia - Matriz Compartida");
        inicializarGUI();
        asociarEventos();

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
    }

    private void inicializarGUI() {
        setLayout(new BorderLayout(10, 10));

        // CENTRO: Matriz 15x15
        JPanel panelMatriz = new JPanel(new GridLayout(15, 15, 2, 2));
        panelMatriz.setBorder(
                BorderFactory.createTitledBorder("Matriz Compartida (15x15)")
        );

        for (int fila = 0; fila < 15; fila++) {
            for (int col = 0; col < 15; col++) {
                JLabel lbl = new JLabel("-", SwingConstants.CENTER);
                lbl.setOpaque(true);
                lbl.setBackground(Color.WHITE);
                lbl.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
                lbl.setFont(new Font("Monospaced", Font.BOLD, 18));

                matriz[fila][col] = lbl;
                panelMatriz.add(lbl);
            }
        }

        add(panelMatriz, BorderLayout.CENTER);

        // NORTE: Paneles Serpiente y Cazador
        JPanel panelNorte = new JPanel(new GridLayout(1, 2, 5, 5));

        // Subpanel Serpiente
        JPanel panelSerpiente = new JPanel(new FlowLayout());
        panelSerpiente.setBorder(
                BorderFactory.createTitledBorder("Hilo Serpiente")
        );

        btIniciarS = new JButton(TEXTO_INICIAR);
        btPausaS = new JButton(TEXTO_PAUSA);
        btTerminarS = new JButton(TEXTO_TERMINAR);

        panelSerpiente.add(btIniciarS);
        panelSerpiente.add(btPausaS);
        panelSerpiente.add(btTerminarS);

        // Subpanel Cazador
        JPanel panelCazador = new JPanel(new FlowLayout());
        panelCazador.setBorder(
                BorderFactory.createTitledBorder("Hilo Cazador")
        );

        btIniciarC = new JButton(TEXTO_INICIAR);
        btPausaC = new JButton(TEXTO_PAUSA);
        btTerminarC = new JButton(TEXTO_TERMINAR);

        panelCazador.add(btIniciarC);
        panelCazador.add(btPausaC);
        panelCazador.add(btTerminarC);
        panelCazador.add(lblContador);

        panelNorte.add(panelSerpiente);
        panelNorte.add(panelCazador);
        add(panelNorte, BorderLayout.NORTH);

        // SUR: Paneles Rebotador y Clima
        JPanel panelSur = new JPanel(new GridLayout(1, 2, 5, 5));

        // Subpanel Rebotador
        JPanel panelRebotador = new JPanel(new FlowLayout());
        panelRebotador.setBorder(
                BorderFactory.createTitledBorder("Hilo Rebotador")
        );

        btIniciarR = new JButton(TEXTO_INICIAR);
        btPausaR = new JButton(TEXTO_PAUSA);
        btTerminarR = new JButton(TEXTO_TERMINAR);

        panelRebotador.add(btIniciarR);
        panelRebotador.add(btPausaR);
        panelRebotador.add(btTerminarR);

        // Subpanel Clima
        JPanel panelClima = new JPanel(new FlowLayout());
        panelClima.setBorder(
                BorderFactory.createTitledBorder("Hilo Clima")
        );

        btIniciarCl = new JButton(TEXTO_INICIAR);
        btPausaCl = new JButton(TEXTO_PAUSA);
        btTerminarCl = new JButton(TEXTO_TERMINAR);

        panelClima.add(btIniciarCl);
        panelClima.add(btPausaCl);
        panelClima.add(btTerminarCl);

        panelSur.add(panelRebotador);
        panelSur.add(panelClima);
        add(panelSur, BorderLayout.SOUTH);

        // WEST: Panel Limpiador
        JPanel panelLimpiador = new JPanel(new GridLayout(3, 1, 5, 5));
        panelLimpiador.setBorder(
                BorderFactory.createTitledBorder("Limpiador")
        );

        btIniciarL = new JButton(TEXTO_INICIAR);
        btPausaL = new JButton(TEXTO_PAUSA);
        btTerminarL = new JButton(TEXTO_TERMINAR);

        panelLimpiador.add(btIniciarL);
        panelLimpiador.add(btPausaL);
        panelLimpiador.add(btTerminarL);

        JPanel panelWestContenedor =
                new JPanel(new FlowLayout(FlowLayout.CENTER));

        panelWestContenedor.add(panelLimpiador);
        add(panelWestContenedor, BorderLayout.WEST);
    }

    private void asociarEventos() {

        // Eventos Hilo Serpiente
        btIniciarS.addActionListener(e -> iniciarSerpiente());
        btPausaS.addActionListener(e -> pausarSerpiente());
        btTerminarS.addActionListener(e -> terminarSerpiente());

        // Eventos Hilo Cazador
        btIniciarC.addActionListener(e -> iniciarCazador());
        btPausaC.addActionListener(e -> pausarCazador());
        btTerminarC.addActionListener(e -> terminarCazador());

        // Eventos Hilo Rebotador
        btIniciarR.addActionListener(e -> iniciarRebotador());
        btPausaR.addActionListener(e -> pausarRebotador());
        btTerminarR.addActionListener(e -> terminarRebotador());

        // Eventos Hilo Clima
        btIniciarCl.addActionListener(e -> iniciarClima());
        btPausaCl.addActionListener(e -> pausarClima());
        btTerminarCl.addActionListener(e -> terminarClima());

        // Eventos Hilo Limpiador
        btIniciarL.addActionListener(e -> iniciarLimpiador());
        btPausaL.addActionListener(e -> pausarLimpiador());
        btTerminarL.addActionListener(e -> terminarLimpiador());
    }

    // Métodos privados para manejo de eventos (Serpiente)

    private void iniciarSerpiente() {
        if (serpiente == null || !serpiente.isAlive()) {
            serpiente = new HiloSerpiente(matriz);
            serpiente.start();
        } else {
            serpiente.continuar();
        }
    }

    private void pausarSerpiente() {
        if (serpiente != null && serpiente.isAlive()) {
            serpiente.pausar();
        }
    }

    private void terminarSerpiente() {
        if (serpiente != null && serpiente.isAlive()) {
            serpiente.terminar();
            btIniciarS.setText("Continuar");
        }
    }

    // Métodos privados para manejo de eventos (Cazador)

    private void iniciarCazador() {
        LOGGER.info("Evento: Iniciar Cazador");

        if (hcaz == null || !hcaz.isAlive()) {
            lblContador.setText("Símbolos: 0");
            hcaz = new HiloCazador(matriz, lblContador);
            hcaz.start();
            btPausaC.setText(TEXTO_PAUSA);
        } else if (hcaz.estaPausado()) {
            hcaz.pausarContinuar();
            btPausaC.setText(TEXTO_PAUSA);
        }
    }

    private void pausarCazador() {
        LOGGER.info("Evento: Pausar Cazador");

        if (hcaz != null && hcaz.isAlive()) {
            hcaz.pausarContinuar();

            if (hcaz.estaPausado()) {
                btPausaC.setText("Continuar");
            } else {
                btPausaC.setText(TEXTO_PAUSA);
            }
        }
    }

    private void terminarCazador() {
        LOGGER.info("Evento: Terminar Cazador");

        if (hcaz != null && hcaz.isAlive()) {
            hcaz.terminar();
        }

        btPausaC.setText(TEXTO_PAUSA);
    }

    // Métodos privados para manejo de eventos (Rebotador)

    private void iniciarRebotador() {
        LOGGER.info("Evento: Iniciar Rebotador");

        if (hreb == null || !hiloRebotadorThread.isAlive()) {
            hreb = new HiloRebotador(matriz, 0, 0);
            hiloRebotadorThread = new Thread(hreb);
            hiloRebotadorThread.start();
        } else {
            hreb.reanudar();
        }
    }

    private void pausarRebotador() {
        LOGGER.info("Evento: Pausar Rebotador");

        if (hreb != null) {
            hreb.pausar();
        }
    }

    private void terminarRebotador() {
        LOGGER.info("Evento: Terminar Rebotador");

        if (hreb != null) {
            hreb.detener();
        }
    }

    // Métodos privados para manejo de eventos (Clima)

    private void iniciarClima() {
        LOGGER.info("Evento: Iniciar Clima");
    }

    private void pausarClima() {
        LOGGER.info("Evento: Pausar Clima");
    }

    private void terminarClima() {
        LOGGER.info("Evento: Terminar Clima");
    }

    // Métodos privados para manejo de eventos (Limpiador)

    private void iniciarLimpiador() {
        if (hlim == null || !hlim.isAlive()) {
            hlim = new HiloLimpiador(matriz);
            hlim.start();
        } else {
            hlim.reanudarHilo();
        }
    }

    private void pausarLimpiador() {
        if (hlim != null && hlim.isAlive()) {
            hlim.pausarHilo();
        }
    }

    private void terminarLimpiador() {
        if (hlim != null && hlim.isAlive()) {
            hlim.terminarHilo();
        }
    }

    // Método auxiliar para acceso a la matriz

    public JLabel[][] getMatrizLabels() {
        return matriz;
    }
}
