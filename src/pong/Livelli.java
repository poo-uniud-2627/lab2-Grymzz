// Pong — Programmazione Orientata agli Oggetti e Laboratori, Udine, a.a. 2026-27
// Consegna 5, variante 2: la classe Livelli della consegna 3, con guadagnaVita in più. I contratti ci sono;
// i campi e i corpi dei metodi li scrivete voi (potete ripartire dalla vostra consegna 3).
package pong;

/**
 * Il gioco a livelli contro il giocatore automatico: a che livello si è e quante vite restano.
 * Si vince un livello vincendo una partita; perdendo una partita si perde una vita.
 */
public class Livelli {

    private final int numeroLivelli;
    private final int vite;
    private final Lato umano;
    private int livelloCorrente;
    private int viteRimaste;
    private boolean completato;
    private boolean perso;
    /**
     * Un gioco nuovo.
     * Pretende: niente.
     * Promette: livello 1, con tutte le vite; il gioco non è completato né perso.
     * Lancia IllegalArgumentException se numeroLivelli o vite è minore di 1, o se umano è null.
     */
    public Livelli(int numeroLivelli, int vite, Lato umano) {
         if (numeroLivelli < 1) {
            throw new IllegalArgumentException("numeroLivelli < 1");
        }  else if (vite < 1) {
            throw new IllegalArgumentException("vite < 1");
        } else if (umano == null) {
            throw new IllegalArgumentException("umano non può essere null");
        }

        this.numeroLivelli = numeroLivelli;
        this.vite = vite;
        this.umano = umano;
        this.livelloCorrente = 1;
        this.viteRimaste = vite;
    }

    /** Il livello a cui si gioca, fra 1 e getNumeroLivelli(). */
    public int getLivello() {
        return this.livelloCorrente;
    }

    public int getNumeroLivelli() {
        return this.numeroLivelli;
    }

    /** Le vite che restano, fra 0 e quelle iniziali. */
    public int getVite() {
        return this.viteRimaste;
    }

    /** Il lato su cui gioca la persona (l'altro è il giocatore automatico). */
    public Lato getUmano() {
        return this.umano;
    }

    /** Vero se è stato vinto l'ultimo livello. */
    public boolean isCompletato() {
        return this.completato;
    }

    /** Vero se le vite sono finite. */
    public boolean isPerso() {
        return this.perso;
    }

    /** Vero se il gioco è completato o perso. */
    public boolean isFinito() {
        return isCompletato() || isPerso();
    }

    /**
     * Registra com'è finita una partita.
     * Pretende: niente.
     * Promette: se il gioco è finito (completato o perso), non cambia niente. Altrimenti:
     *           se ha vinto umano, si passa al livello dopo; se era l'ultimo, il gioco è completato
     *           e il livello resta l'ultimo; le vite non cambiano.
     *           Se ha vinto l'altro lato, una vita in meno e il livello non cambia;
     *           con l'ultima vita il gioco è perso.
     * Lancia IllegalArgumentException se vincitore è null, e non cambia niente.
     */
    public void partitaFinita(Lato vincitore) {
        if (vincitore == null) {
            throw new  IllegalArgumentException("vincitore non può essere null");
        }
        if (isFinito()) {
            return;
        }   
    if (vincitore == this.umano) {
        if (this.livelloCorrente == this.numeroLivelli) {
            this.completato = true;
        } else {
            this.livelloCorrente++;
        }
    } 
    else {
        this.viteRimaste--;
        if (this.viteRimaste == 0) {
            this.perso = true;
        }
    }
}

    /**
     * Si ricomincia da capo.
     * Pretende: niente.
     * Promette: livello 1, con tutte le vite; il gioco non è completato né perso.
     */
    public void ricomincia() {
        this.livelloCorrente = 1;
        this.viteRimaste = this.vite;
        this.completato = false;
        this.perso = false;
    }

    /**
     * Si riguadagna una vita (variante 2: con le stelle).
     * Pretende: niente.
     * Promette: se il gioco è finito (completato o perso), non cambia niente. Altrimenti una vita in più,
     *           ma mai più delle vite iniziali: con tutte le vite, non cambia niente. Il livello non cambia.
     */
    public void guadagnaVita() {
        if (isFinito()) {
            return ;
        } else {
            if (this.viteRimaste == this.vite) {
                return;
            } else{
                this.viteRimaste++;
            }
        }
    }
}
