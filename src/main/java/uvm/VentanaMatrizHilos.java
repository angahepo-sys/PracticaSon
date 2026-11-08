package uvm;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class VentanaMatrizHilos extends JFrame {
    HiloCazador hcaz;
    HiloClima hcli;
    HiloLimpiador hlim;
    HiloRebotador hreb;
    HiloSerpiente hser;

    // Matriz de 15x15 JLabels (Recurso Compartido)
    private JLabel[][] matriz = new JLabel[15][15];

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
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null); // Centrar en pantalla
    }

    private void inicializarGUI() {
        setLayout(new BorderLayout(10, 10));

        // -----------------------------------------------------------------
        // CENTRO: Matriz 15x15 JLabels
        // -----------------------------------------------------------------
        JPanel panelMatriz = new JPanel(new GridLayout(15, 15, 2, 2));
        panelMatriz.setBorder(BorderFactory.createTitledBorder("Matriz Compartida (15x15)"));
        
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

        // -----------------------------------------------------------------
        // NORTE: Paneles Serpiente y Cazador
        // -----------------------------------------------------------------
        JPanel panelNorte = new JPanel(new GridLayout(1, 2, 5, 5));

        // Subpanel Serpiente
        JPanel panelSerpiente = new JPanel(new FlowLayout());
        panelSerpiente.setBorder(BorderFactory.createTitledBorder("Hilo Serpiente"));
        btIniciarS = new JButton("Iniciar");
        btPausaS = new JButton("Pausa");
        btTerminarS = new JButton("Terminar");
        panelSerpiente.add(btIniciarS);
        panelSerpiente.add(btPausaS);
        panelSerpiente.add(btTerminarS);

        // Subpanel Cazador
        JPanel panelCazador = new JPanel(new FlowLayout());
        panelCazador.setBorder(BorderFactory.createTitledBorder("Hilo Cazador"));
        btIniciarC = new JButton("Iniciar");
        btPausaC = new JButton("Pausa");
        btTerminarC = new JButton("Terminar");
        panelCazador.add(btIniciarC);
        panelCazador.add(btPausaC);
        panelCazador.add(btTerminarC);

        panelNorte.add(panelSerpiente);
        panelNorte.add(panelCazador);
        add(panelNorte, BorderLayout.NORTH);

        // -----------------------------------------------------------------
        // SUR: Paneles Rebotador y Clima
        // -----------------------------------------------------------------
        JPanel panelSur = new JPanel(new GridLayout(1, 2, 5, 5));

        // Subpanel Rebotador
        JPanel panelRebotador = new JPanel(new FlowLayout());
        panelRebotador.setBorder(BorderFactory.createTitledBorder("Hilo Rebotador"));
        btIniciarR = new JButton("Iniciar");
        btPausaR = new JButton("Pausa");
        btTerminarR = new JButton("Terminar");
        panelRebotador.add(btIniciarR);
        panelRebotador.add(btPausaR);
        panelRebotador.add(btTerminarR);

        // Subpanel Clima
        JPanel panelClima = new JPanel(new FlowLayout());
        panelClima.setBorder(BorderFactory.createTitledBorder("Hilo Clima"));
        btIniciarCl = new JButton("Iniciar");
        btPausaCl = new JButton("Pausa");
        btTerminarCl = new JButton("Terminar");
        panelClima.add(btIniciarCl);
        panelClima.add(btPausaCl);
        panelClima.add(btTerminarCl);

        panelSur.add(panelRebotador);
        panelSur.add(panelClima);
        add(panelSur, BorderLayout.SOUTH);

        // -----------------------------------------------------------------
        // WEST: Panel Limpiador (Botones en Vertical)
        // -----------------------------------------------------------------
        JPanel panelLimpiador = new JPanel(new GridLayout(3, 1, 5, 5));
        panelLimpiador.setBorder(BorderFactory.createTitledBorder("Limpiador"));
        btIniciarL = new JButton("Iniciar");
        btPausaL = new JButton("Pausa");
        btTerminarL = new JButton("Terminar");
        
        panelLimpiador.add(btIniciarL);
        panelLimpiador.add(btPausaL);
        panelLimpiador.add(btTerminarL);

        JPanel panelWestContenedor = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelWestContenedor.add(panelLimpiador);
        add(panelWestContenedor, BorderLayout.WEST);
    }

    private void asociarEventos() {
        // Eventos Hilo Serpiente
        btIniciarS.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                iniciarSerpiente();
            }
        });
        btPausaS.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pausarSerpiente();
            }
        });
        btTerminarS.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                terminarSerpiente();
            }
        });

        // Eventos Hilo Cazador
        btIniciarC.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                iniciarCazador();
            }
        });
        btPausaC.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pausarCazador();
            }
        });
        btTerminarC.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                terminarCazador();
            }
        });

        // Eventos Hilo Rebotador
        btIniciarR.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                iniciarRebotador();
            }
        });
        btPausaR.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pausarRebotador();
            }
        });
        btTerminarR.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                terminarRebotador();
            }
        });

        // Eventos Hilo Clima
        btIniciarCl.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                iniciarClima();
            }
        });
        btPausaCl.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pausarClima();
            }
        });
        btTerminarCl.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                terminarClima();
            }
        });

        // Eventos Hilo Limpiador
        btIniciarL.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                iniciarLimpiador();
            }
        });
        btPausaL.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pausarLimpiador();
            }
        });
        btTerminarL.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                terminarLimpiador();
            }
        });
    }

    // -----------------------------------------------------------------
    // Métodos privados para manejo de eventos (Serpiente)
    // -----------------------------------------------------------------
    private HiloSerpiente serpiente;
    
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

    // -----------------------------------------------------------------
    // Métodos privados para manejo de eventos (Cazador)
    // -----------------------------------------------------------------
    private void iniciarCazador() {
        System.out.println("Evento: Iniciar Cazador");
    }

    private void pausarCazador() {
        System.out.println("Evento: Pausar Cazador");
    }

    private void terminarCazador() {
        System.out.println("Evento: Terminar Cazador");
    }

    // -----------------------------------------------------------------
    // Métodos privados para manejo de eventos (Rebotador)
    // -----------------------------------------------------------------
    private void iniciarRebotador() {
        System.out.println("Evento: Iniciar Rebotador");
    }

    private void pausarRebotador() {
        System.out.println("Evento: Pausar Rebotador");
    }

    private void terminarRebotador() {
        System.out.println("Evento: Terminar Rebotador");
    }

    // -----------------------------------------------------------------
    // Métodos privados para manejo de eventos (Clima)
    // -----------------------------------------------------------------
    private void iniciarClima() {
        System.out.println("Evento: Iniciar Clima");
    }

    private void pausarClima() {
        System.out.println("Evento: Pausar Clima");
    }

    private void terminarClima() {
        System.out.println("Evento: Terminar Clima");
    }

    // -----------------------------------------------------------------
    // Métodos privados para manejo de eventos (Limpiador)
    // -----------------------------------------------------------------
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

}
