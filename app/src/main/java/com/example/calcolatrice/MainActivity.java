package com.example.calcolatrice;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText display;

    private double  primoNumero    = 0;
    private String  operatore      = "";    // "", "+", "-", "X", "/", "^"
    private boolean aspettaSecondo = false; // true = il 2° operando non è ancora stato digitato
    private boolean dopoUguale     = false; // true = risultato appena mostrato

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        display = findViewById(R.id.display);
        if (display != null) display.setFocusable(false);

        // Cifre
        int[] idNumeri = {
                R.id.t0, R.id.t1, R.id.t2, R.id.t3, R.id.t4,
                R.id.t5, R.id.t6, R.id.t7, R.id.t8, R.id.t9
        };
        for (int id : idNumeri) {
            Button b = findViewById(id);
            if (b == null) continue;
            b.setOnClickListener(v -> premiCifra(b.getText().toString()));
        }

        // Virgola
        Button btnVirgola = findViewById(R.id.virgola);
        if (btnVirgola != null) btnVirgola.setOnClickListener(v -> premiVirgola());

        // Operatori
        int[]    idOp = { R.id.piu, R.id.meno, R.id.per, R.id.diviso, R.id.potenza };
        String[] nomi = { "+",      "-",       "X",      "/",         "^"          };
        for (int i = 0; i < idOp.length; i++) {
            Button b = findViewById(idOp[i]);
            if (b == null) continue;
            final String op = nomi[i];
            b.setOnClickListener(v -> premiOperatore(op));
        }

        // Uguale
        Button btnUguale = findViewById(R.id.uguale);
        if (btnUguale != null) btnUguale.setOnClickListener(v -> premiUguale());

        // Cancella tutto (C)
        Button btnCancella = findViewById(R.id.cancella);
        if (btnCancella != null) btnCancella.setOnClickListener(v -> reset());

        // Backspace (DEL)
        Button btnElimina = findViewById(R.id.elimina);
        if (btnElimina != null) btnElimina.setOnClickListener(v -> premiBack());

        // Cambio segno +/− (meno2)
        // Nel layout il bottone si chiama "meno2" ma mostra "+"; lo usiamo come +/−
        Button btnCambioSegno = findViewById(R.id.meno2);
        if (btnCambioSegno != null) btnCambioSegno.setOnClickListener(v -> cambiaSegnAttuale());

        Button btnLog10    = findViewById(R.id.log10);
        Button btnRadice   = findViewById(R.id.radice);
        Button btnReciproco = findViewById(R.id.reciproco);
        Button btnFatt     = findViewById(R.id.fattoriale);

        if (btnLog10    != null) btnLog10.setOnClickListener(v    -> applicaFunzione("log10"));
        if (btnRadice   != null) btnRadice.setOnClickListener(v   -> applicaFunzione("sqrt"));
        if (btnReciproco != null) btnReciproco.setOnClickListener(v -> applicaFunzione("1/x"));
        if (btnFatt     != null) btnFatt.setOnClickListener(v     -> applicaFunzione("x!"));
    }


    /** Aggiunge una cifra al numero corrente. */
    private void premiCifra(String cifra) {
        if (dopoUguale) {
            // Dopo un risultato: inizia un nuovo numero
            display.setText(cifra.equals("0") ? "0" : cifra);
            dopoUguale     = false;
            aspettaSecondo = false;
            return;
        }
        if (aspettaSecondo) {
            // Inizia il secondo operando
            display.setText(cifra.equals("0") ? "0" : cifra);
            aspettaSecondo = false;
            return;
        }

        String attuale = display.getText().toString();
        if (attuale.equals("0") && !cifra.equals("0")) { display.setText(cifra); return; }
        if (attuale.equals("0") &&  cifra.equals("0")) return;   // doppio zero inutile
        if (attuale.length() >= 14) return;                       // limite lunghezza

        display.append(cifra);
    }

    /** Aggiunge la virgola decimale se non è già presente. */
    private void premiVirgola() {
        if (dopoUguale) { display.setText("0."); dopoUguale = false; aspettaSecondo = false; return; }
        if (aspettaSecondo) { display.setText("0."); aspettaSecondo = false; return; }

        String attuale = display.getText().toString();
        if (!attuale.contains(".")) {
            display.setText(attuale.isEmpty() ? "0." : attuale + ".");
        }
    }

    /**
     * Registra un operatore binario.
     * Se esiste già un calcolo pendente con un secondo operando digitato, lo risolve prima.
     */
    private void premiOperatore(String nuovoOp) {
        double valoreCorrente = leggiDisplay();

        if (!operatore.isEmpty() && !aspettaSecondo && !dopoUguale) {
            // Calcolo intermedio
            double risultato = eseguiCalcolo(primoNumero, valoreCorrente, operatore);
            if (Double.isNaN(risultato)) { errore(); return; }
            mostra(risultato);
            primoNumero = risultato;
        } else {
            primoNumero = valoreCorrente;
        }

        operatore      = nuovoOp;
        aspettaSecondo = true;
        dopoUguale     = false;
    }

    /** Calcola e mostra il risultato finale. */
    private void premiUguale() {
        if (operatore.isEmpty()) return;

        // Se l'utente preme = senza digitare il 2° numero, usa il primo come secondo
        double secondoNumero = aspettaSecondo ? primoNumero : leggiDisplay();

        double risultato = eseguiCalcolo(primoNumero, secondoNumero, operatore);
        if (Double.isNaN(risultato)) { errore(); return; }

        mostra(risultato);
        primoNumero    = risultato;
        operatore      = "";
        aspettaSecondo = false;
        dopoUguale     = true;
    }

    /**
     * Applica una funzione unaria al numero attualmente visualizzato.
     * Gestisce: log10, sqrt (√), 1/x, x! (fattoriale)
     */
    private void applicaFunzione(String funzione) {
        double x = leggiDisplay();
        double risultato;

        switch (funzione) {
            case "log10":
                if (x <= 0) { errore(); return; }
                risultato = Math.log10(x);
                break;
            case "sqrt":
                if (x < 0) { errore(); return; }
                risultato = Math.sqrt(x);
                break;
            case "1/x":
                if (x == 0) { errore(); return; }
                risultato = 1.0 / x;
                break;
            case "x!":
                if (x < 0 || x != Math.floor(x) || x > 20) {
                    // Fattoriale solo per interi non negativi ≤ 20 (evita overflow long)
                    errore();
                    return;
                }
                risultato = (double) fattoriale((int) x);
                break;
            default:
                return;
        }

        if (Double.isNaN(risultato) || Double.isInfinite(risultato)) { errore(); return; }
        mostra(risultato);
        // Il risultato di una funzione unaria si comporta come un valore già calcolato:
        // l'utente può subito concatenare un operatore binario
        dopoUguale     = false;
        aspettaSecondo = false;
        // Aggiorniamo primoNumero solo se c'è già un operatore in attesa
        if (operatore.isEmpty()) primoNumero = risultato;
    }

    /** Inverte il segno del numero sul display. */
    private void cambiaSegnAttuale() {
        String attuale = display.getText().toString();
        if (attuale.isEmpty() || attuale.equals("0")) return;
        if (attuale.startsWith("-")) {
            display.setText(attuale.substring(1));
        } else {
            display.setText("-" + attuale);
        }
    }

    /** Cancella l'ultimo carattere digitato. */
    private void premiBack() {
        if (dopoUguale || aspettaSecondo) return; // nulla da cancellare in questi stati

        String attuale = display.getText().toString();
        if (attuale.length() <= 1 || attuale.equals("-0")) {
            display.setText("0");
        } else {
            display.setText(attuale.substring(0, attuale.length() - 1));
            // Se rimane solo "-", torna a 0
            if (display.getText().toString().equals("-")) display.setText("0");
        }
    }

    /** Azzera completamente la calcolatrice. */
    private void reset() {
        primoNumero    = 0;
        operatore      = "";
        aspettaSecondo = false;
        dopoUguale     = false;
        display.setText("0");
    }

    /** Esegue un'operazione binaria. Restituisce NaN in caso di errore. */
    private double eseguiCalcolo(double a, double b, String op) {
        switch (op) {
            case "+": return a + b;
            case "-": return a - b;
            case "X": return a * b;
            case "/": return (b == 0) ? Double.NaN : a / b;
            case "^": return Math.pow(a, b);
            default:  return b;
        }
    }

    /** Calcola n! in modo iterativo (n ≤ 20). */
    private long fattoriale(int n) {
        long r = 1;
        for (int i = 2; i <= n; i++) r *= i;
        return r;
    }

    // ════════════════════════════════════════════════════════════════════════
    // Display
    // ════════════════════════════════════════════════════════════════════════

    /** Legge il double dal display. Restituisce 0 in caso di formato non valido. */
    private double leggiDisplay() {
        try {
            return Double.parseDouble(display.getText().toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Mostra un double nel display.
     * - Interi: senza decimali (es. 5.0 → "5")
     * - Decimali: fino a 8 cifre significative, senza zeri finali inutili
     */
    private void mostra(double v) {
        if (Double.isNaN(v) || Double.isInfinite(v)) { errore(); return; }
        String testo;
        if (v == Math.floor(v) && Math.abs(v) < 1e15) {
            testo = String.valueOf((long) v);
        } else {
            testo = String.format("%.8g", v);
            // Rimuovere zeri finali (es. "3.50000000" → "3.5")
            if (testo.contains(".") && !testo.contains("e")) {
                testo = testo.replaceAll("0+$", "").replaceAll("\\.$", "");
            }
        }
        display.setText(testo);
    }

    /** Mostra "Errore" e porta la calcolatrice in uno stato sicuro. */
    private void errore() {
        display.setText("Errore");
        primoNumero    = 0;
        operatore      = "";
        aspettaSecondo = false;
        dopoUguale     = true; // il prossimo tasto cifra pulirà il display
    }
}