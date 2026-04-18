package com.example.calcolatrice;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText display;

    // Stato della calcolatrice
    private double primoNumero     = 0;
    private String operatore       = "";    // "", "+", "-", "X", "/", "^"
    private boolean aspettaSecondo = false; // l'utente deve ancora digitare il 2° numero
    private boolean dopoUguale     = false; // abbiamo appena mostrato un risultato

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

        // Cancella tutto
        Button btnCancella = findViewById(R.id.cancella);
        if (btnCancella != null) btnCancella.setOnClickListener(v -> reset());

        //  Backspace
        Button btnElimina = findViewById(R.id.elimina);
        if (btnElimina != null) btnElimina.setOnClickListener(v -> premiBack());
    }


    private void premiCifra(String cifra) {
        // Dopo un risultato: nuovo calcolo da zero
        if (dopoUguale) {
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

        // Sostituire "0" iniziale, ma non "0."
        if (attuale.equals("0") && !cifra.equals("0")) { display.setText(cifra); return; }
        if (attuale.equals("0") &&  cifra.equals("0")) return; // doppio zero inutile

        // Limite lunghezza display
        if (attuale.length() >= 14) return;

        display.append(cifra);
    }

    private void premiVirgola() {
        if (dopoUguale) {
            display.setText("0.");
            dopoUguale = false;
            aspettaSecondo = false;
            return;
        }
        if (aspettaSecondo) {
            display.setText("0.");
            aspettaSecondo = false;
            return;
        }
        String attuale = display.getText().toString();
        if (!attuale.contains(".")) {
            if (attuale.isEmpty()) display.setText("0.");
            else display.append(".");
        }
    }

    private void premiOperatore(String nuovoOp) {
        double valoreCorrente = leggiDisplay();

        // Calcolo intermedio se c'è già un'operazione pendente
        if (!operatore.isEmpty() && !aspettaSecondo && !dopoUguale) {
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

    private void premiUguale() {
        if (operatore.isEmpty()) return;

        double secondoNumero = aspettaSecondo ? primoNumero : leggiDisplay();

        double risultato = eseguiCalcolo(primoNumero, secondoNumero, operatore);
        if (Double.isNaN(risultato)) { errore(); return; }

        mostra(risultato);
        primoNumero    = risultato;
        operatore      = "";
        aspettaSecondo = false;
        dopoUguale     = true;
    }

    private void premiBack() {
        // Non cancellare in stati intermedi
        if (dopoUguale || aspettaSecondo) return;

        String attuale = display.getText().toString();
        if (attuale.length() <= 1 || attuale.equals("-0")) {
            display.setText("0");
        } else {
            display.setText(attuale.substring(0, attuale.length() - 1));
        }
    }

    private void reset() {
        primoNumero    = 0;
        operatore      = "";
        aspettaSecondo = false;
        dopoUguale     = false;
        display.setText("0");
    }

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

    private double leggiDisplay() {
        try {
            return Double.parseDouble(display.getText().toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void mostra(double v) {
        if (Double.isNaN(v) || Double.isInfinite(v)) { errore(); return; }
        String testo;
        if (v == Math.floor(v) && Math.abs(v) < 1e15) {
            testo = String.valueOf((long) v);
        } else {
            // Troncare a 8 cifre significative per evitare rumore floating-point
            testo = String.format("%.8g", v);
            // Rimuovere zeri finali inutili (es. "3.50000000" → "3.5")
            if (testo.contains(".")) {
                testo = testo.replaceAll("0+$", "").replaceAll("\\.$", "");
            }
        }
        display.setText(testo);
    }

    private void errore() {
        display.setText("Errore");
        primoNumero    = 0;
        operatore      = "";
        aspettaSecondo = false;
        dopoUguale     = true;
    }
}