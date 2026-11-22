// Pong — Programmazione Orientata agli Oggetti e Laboratori, Udine, a.a. 2026-27
package pong;

import geometria.Posizione;
import geometria.Rettangolo;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;
import ostacoli.MotoreDeiLivelli;
import ostacoli.Ostacolo;
import stelle.Raccolta;
import stelle.Stella;

/**
 * Mostra la partita: sessanta volte al secondo la fa avanzare di un passo e la ridisegna.
 * Prima di cominciare legge SPAZIO (via!).
 */
class Schermo extends JPanel {
    private final Partita partita;
    private final Tastiera tastiera;

    private Schermo(Partita partita, Tastiera tastiera) {
        this.partita = partita;
        this.tastiera = tastiera;
        Campo campo = partita.getCampo();
        setPreferredSize(new Dimension(campo.getLarghezza(), campo.getAltezza()));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(tastiera);
        new Timer(16, e -> passo()).start();
    }

    static void mostra(Partita partita, Tastiera tastiera) {
        JFrame finestra = new JFrame("Pong - POO 2026-27");
        finestra.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Schermo schermo = new Schermo(partita, tastiera);
        finestra.add(schermo);
        finestra.pack();
        finestra.setResizable(false);
        finestra.setLocationRelativeTo(null);
        finestra.setVisible(true);
        schermo.requestFocusInWindow();
    }

    private void passo() {
        if (!partita.isInCorso()) {
            // I tasti da 1 a 5 non servono più: si usa solo SPAZIO per iniziare o proseguire
            if (tastiera.premuto(KeyEvent.VK_SPACE)) {
                partita.avvia();
            }
        }
        partita.aggiorna();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        disegna((Graphics2D) g, partita);
    }

    /**
     * Disegna la partita su g2.
     */
    static void disegna(Graphics2D g2, Partita partita) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Campo campo = partita.getCampo();
        int larghezza = campo.getLarghezza();
        int altezza = campo.getAltezza();
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, larghezza, altezza);

        g2.setColor(Color.DARK_GRAY);
        g2.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1, new float[]{10, 10}, 0));
        g2.drawLine(larghezza / 2, 0, larghezza / 2, altezza);

        // Ostacoli
        for (Ostacolo o : partita.getOstacoli().getOstacoli()) {
            if (!o.isRotto()) {
                g2.setColor(o.getColore());
                riempi(g2, o.getArea());
            }
        }

        // Stella (se presente e visibile)
        Stella stella = partita.getStella();
        if (stella != null && stella.isVisibile()) {
            g2.setColor(Color.YELLOW);
            Rettangolo s = stella.getArea();
            g2.fillOval((int) Math.round(s.x()), (int) Math.round(s.y()),
                    (int) Math.round(s.larghezza()), (int) Math.round(s.altezza()));
        }

        // Racchette e palla
        g2.setColor(Color.WHITE);
        disegna(g2, partita.getRacchettaSinistra());
        disegna(g2, partita.getRacchettaDestra());
        Palla palla = partita.getPalla();
        int r = palla.getRaggio();
        Posizione p = palla.getPosizione();
        g2.fillOval((int) Math.round(p.x() - r), (int) Math.round(p.y() - r), 2 * r, 2 * r);

        // Punteggio in alto
        g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, 48));
        g2.drawString(String.valueOf(partita.getPunti(Lato.SINISTRA)), larghezza / 2 - 80, 60);
        g2.drawString(String.valueOf(partita.getPunti(Lato.DESTRA)), larghezza / 2 + 50, 60);

        // Barra di stato in basso: Vite, Livello e Stelle
        g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 18));
        g2.setColor(Color.LIGHT_GRAY);
        g2.drawString("Vite: " + partita.getVite(), 25, altezza - 20);
        scriviAlCentro(g2, "Livello " + partita.getLivello().numero() + " / " + MotoreDeiLivelli.NUMERO_LIVELLI, larghezza, altezza - 20);
        String testoStelle = "Stelle: " + partita.getStelle() + "/" + Raccolta.STELLE_PER_UNA_VITA;
        g2.drawString(testoStelle, larghezza - g2.getFontMetrics().stringWidth(testoStelle) - 25, altezza - 20);

        // Messaggi quando la partita è ferma
        if (!partita.isInCorso()) {
            g2.setColor(Color.WHITE);
            g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
            if (partita.isCompletato()) {
                scriviAlCentro(g2, "Complimenti! Hai completato tutti i livelli!", larghezza, altezza / 2 - 40);
                g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 20));
                scriviAlCentro(g2, "Premi SPAZIO per ricominciare da capo", larghezza, altezza / 2 + 60);
            } else if (partita.isFinito()) {
                scriviAlCentro(g2, "Game Over! Hai esaurito le vite.", larghezza, altezza / 2 - 40);
                g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 20));
                scriviAlCentro(g2, "Premi SPAZIO per ricominciare da capo", larghezza, altezza / 2 + 60);
            } else if (partita.getVincitore() == Lato.SINISTRA) {
                scriviAlCentro(g2, "Livello superato!", larghezza, altezza / 2 - 40);
                g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 20));
                scriviAlCentro(g2, "Premi SPAZIO per passare al livello successivo", larghezza, altezza / 2 + 60);
            } else if (partita.getVincitore() == Lato.DESTRA) {
                scriviAlCentro(g2, "Hai perso il livello (-1 vita).", larghezza, altezza / 2 - 40);
                g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 20));
                scriviAlCentro(g2, "Premi SPAZIO per riprovare il livello", larghezza, altezza / 2 + 60);
            } else {
                g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 20));
                scriviAlCentro(g2, "Premi SPAZIO per cominciare", larghezza, altezza / 2 + 60);
            }
        }
    }

    private static void disegna(Graphics2D g2, Racchetta racchetta) {
        g2.fillRect(racchetta.getX(), (int) Math.round(racchetta.getY()),
                racchetta.getLarghezza(), racchetta.getAltezza());
    }

    private static void riempi(Graphics2D g2, Rettangolo r) {
        g2.fillRect((int) Math.round(r.x()), (int) Math.round(r.y()),
                (int) Math.round(r.larghezza()), (int) Math.round(r.altezza()));
    }

    /** Scrive il testo centrato in orizzontale in un campo largo larghezza, all'altezza y. */
    private static void scriviAlCentro(Graphics2D g2, String testo, int larghezza, int y) {
        g2.drawString(testo, (larghezza - g2.getFontMetrics().stringWidth(testo)) / 2, y);
    }
}