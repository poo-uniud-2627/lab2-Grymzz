// Pong — Programmazione Orientata agli Oggetti e Laboratori, Udine, a.a. 2026-27
package pong;

import geometria.Rettangolo;
import giocatori.GiocatoreAutomatico;
import java.util.Random;
import ostacoli.Livello;
import ostacoli.MotoreDeiLivelli;
import ostacoli.Ostacoli;
import ostacoli.Ostacolo;
import stelle.Raccolta;
import stelle.Stella;

/**
 * Una partita a livelli: voi a sinistra, il giocatore automatico a destra, due racchette,
 * una palla, gli ostacoli del livello, le stelle e i conti di vite e livelli.
 */
class Partita {
    /** Si gioca a 5, con almeno due punti di scarto (5 a 4 non basta, 6 a 4 sì). */
    static final int PUNTI_PER_VINCERE = 5;
    static final int SCARTO_MINIMO = 2;
    static final int VITE_INIZIALI = 3;

    private final Campo campo;
    private final Giocatore giocatoreSinistra;
    private Giocatore giocatoreDestra;
    private final Racchetta racchettaSinistra;
    private final Racchetta racchettaDestra;
    private final Palla palla;
    private final Punteggio punteggio = new Punteggio(PUNTI_PER_VINCERE, SCARTO_MINIMO);
    private final Livelli livelli = new Livelli(MotoreDeiLivelli.NUMERO_LIVELLI, VITE_INIZIALI, Lato.SINISTRA);
    private final Raccolta raccolta = new Raccolta(Raccolta.STELLE_PER_UNA_VITA);
    private final Random random = new Random();

    private Livello livello;
    private Ostacoli ostacoli;
    private boolean iniziata;
    private Stella stella;
    private boolean esitoRegistrato;

    /** Una partita fra umano, a sinistra, e il giocatore automatico, a destra; si comincia dal livello 1. */
    Partita(Campo campo, Giocatore umano) {
        this.campo = campo;
        this.giocatoreSinistra = umano;
        double centroY = campo.getAltezza() / 2.0 - Racchetta.ALTEZZA / 2.0;
        this.racchettaSinistra = new Racchetta(30, centroY);
        this.racchettaDestra = new Racchetta(campo.getLarghezza() - 30 - Racchetta.LARGHEZZA, centroY);
        this.palla = new Palla(campo.getLarghezza() / 2.0, campo.getAltezza() / 2.0);
        preparaLivello(livelli.getLivello());
    }

    /**
     * Prepara il livello numero: chiede al motore dei livelli ostacoli nuovi, velocità della palla ed
     * errore del giocatore automatico; rimette i punti a zero e la palla ferma al centro.
     */
    void preparaLivello(int numero) {
        livello = MotoreDeiLivelli.livello(numero, campo.getLarghezza(), campo.getAltezza());
        ostacoli = livello.ostacoli();
        giocatoreDestra = new GiocatoreAutomatico(livello.erroreMassimo());
        punteggio.azzera();
        iniziata = false;
        esitoRegistrato = false;
        stella = null;
        palla.fermaIn(campo.getLarghezza() / 2.0, campo.getAltezza() / 2.0);
    }

    // ---- Metodi di lettura per lo Schermo

    int getPunti(Lato chi) { return punteggio.getPunti(chi); }
    Lato getVincitore() { return punteggio.getVincitore(); }
    boolean isInCorso() { return iniziata && !punteggio.isFinita() && !livelli.isFinito(); }
    boolean isIniziata() { return iniziata; }
    Livello getLivello() { return livello; }
    Ostacoli getOstacoli() { return ostacoli; }
    int getVite() { return livelli.getVite(); }
    int getStelle() { return raccolta.getStelle(); }
    boolean isFinito() { return livelli.isFinito(); }
    boolean isCompletato() { return livelli.isCompletato(); }
    Stella getStella() { return stella; }

    /**
     * Fa cominciare la partita e lancia la palla.
     * Se il gioco intero era finito (vinto o perso), ricomincia dal livello 1 con 3 vite.
     * Altrimenti prepara il livello corrente indicato da Livelli (quello successivo se si è vinto,
     * oppure lo stesso se si è perso ma restano vite).
     */
    void avvia() {
        if (livelli.isFinito()) {
            livelli.ricomincia();
            // Svuota eventuali stelle residue dell'ultima partita
            while (raccolta.getStelle() > 0) {
                raccolta.aggiungi();
            }
            preparaLivello(livelli.getLivello());
        } else if (iniziata) {
            preparaLivello(livelli.getLivello());
        }
        iniziata = true;
        lanciaPalla(random.nextBoolean());
    }

    /** Lancia la palla dal centro, alla velocità del livello. */
    private void lanciaPalla(boolean versoDestra) {
        palla.lancia(campo.getLarghezza() / 2.0, campo.getAltezza() / 2.0, versoDestra, livello.velocitaPalla());
    }

    /** Un passo del gioco: viene chiamato 60 volte al secondo. */
    void aggiorna() {
        if (!isInCorso()) return;
        ostacoli.avanzaTutti();
        muoviRacchette();
        aggiornaStella();
        palla.muovi();
        controllaRimbalzi();
        controllaOstacoli();
        controllaPunto();
    }

    private void muoviRacchette() {
        Mossa mossaSinistra = giocatoreSinistra.decidiMossa(palla, racchettaSinistra);
        Mossa mossaDestra = giocatoreDestra.decidiMossa(palla, racchettaDestra);
        racchettaSinistra.muovi(mossaSinistra, campo);
        racchettaDestra.muovi(mossaDestra, campo);
    }

    private void aggiornaStella() {
        // Se non c'è una stella visibile, ogni tanto ne crea una nella corsia della racchetta sinistra.
        if (stella == null || !stella.isVisibile()) {
            if (random.nextInt(400) == 0) {
                int dimensione = racchettaSinistra.getLarghezza();
                int x = racchettaSinistra.getX();
                int y = random.nextInt(campo.getAltezza() - dimensione);
                stella = new Stella(
                        new Rettangolo(x, y, dimensione, dimensione),
                        livello.durataStella()
                );
            }
            return;
        }

        // Costruisce l'area attuale della racchetta sinistra.
        Rettangolo areaRacchetta = new Rettangolo(
                racchettaSinistra.getX(),
                racchettaSinistra.getY(),
                racchettaSinistra.getLarghezza(),
                racchettaSinistra.getAltezza()
        );

        // Controlla se la racchetta tocca la stella.
        if (areaRacchetta.tocca(stella.getArea())) {
            stella.raccogli();
            // Ogni 5 stelle si guadagna una vita.
            if (raccolta.aggiungi()) {
                livelli.guadagnaVita();
            }
        } else {
            // La stella perde un fotogramma di durata.
            stella.avanza();
        }
    }

    private void registraRisultato() {
        if (punteggio.isFinita() && !esitoRegistrato) {
            livelli.partitaFinita(punteggio.getVincitore());
            esitoRegistrato = true;
        }
    }

    private void controllaRimbalzi() {
        double y = palla.getPosizione().y();
        boolean toccaAlto = y - palla.getRaggio() <= 0 && palla.getVelocitaY() < 0;
        boolean toccaBasso = y + palla.getRaggio() >= campo.getAltezza() && palla.getVelocitaY() > 0;
        if (toccaAlto || toccaBasso) palla.rimbalzaVerticale();

        if (palla.tocca(racchettaSinistra) && palla.getVelocitaX() < 0) palla.rimbalzaSu(racchettaSinistra);
        if (palla.tocca(racchettaDestra) && palla.getVelocitaX() > 0) palla.rimbalzaSu(racchettaDestra);
    }

    private void controllaOstacoli() {
        Ostacolo colpito = ostacoli.ostacoloCheTocca(palla.getArea());
        if (colpito != null) {
            palla.rimbalzaSu(colpito.getArea());
            colpito.colpisci(ostacoli);
        }
    }

    /** Se la palla è uscita da un lato, il punto va all'altro lato. */
    private void controllaPunto() {
        double x = palla.getPosizione().x();
        if (x < 0) {
            punteggio.segnaPunto(Lato.DESTRA);
            registraRisultato();
            dopoIlPunto(true);
        } else if (x > campo.getLarghezza()) {
            punteggio.segnaPunto(Lato.SINISTRA);
            registraRisultato();
            dopoIlPunto(false);
        }
    }

    private void dopoIlPunto(boolean versoSinistra) {
        if (punteggio.isFinita()) {
            palla.fermaIn(campo.getLarghezza() / 2.0, campo.getAltezza() / 2.0);
        } else {
            lanciaPalla(!versoSinistra);
        }
    }

    Campo getCampo() { return campo; }
    Palla getPalla() { return palla; }
    Racchetta getRacchettaSinistra() { return racchettaSinistra; }
    Racchetta getRacchettaDestra() { return racchettaDestra; }
}