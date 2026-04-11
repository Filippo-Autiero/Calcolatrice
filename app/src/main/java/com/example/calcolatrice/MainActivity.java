package com.example.calcolatrice;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText display;
    private String primoNumero = "";
    private String operatore = "";
    private boolean nuovoNumero = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        display = findViewById(R.id.display);

        if (display != null) {
            display.setFocusable(false);
        }

        // NUMERI
        int[] numeri = {
                R.id.t0, R.id.t1, R.id.t2, R.id.t3, R.id.t4,
                R.id.t5, R.id.t6, R.id.t7, R.id.t8, R.id.t9
        };

        for (int id : numeri) {
            Button b = findViewById(id);
            if (b != null) {
                b.setOnClickListener(v -> {
                    if (nuovoNumero) {
                        display.setText("");
                        nuovoNumero = false;
                    }
                    display.append(b.getText().toString());
                });
            }
        }

        // VIRGOLA
        Button virgola = findViewById(R.id.virgola);
        if (virgola != null) {
            virgola.setOnClickListener(v -> {
                if (nuovoNumero) {
                    display.setText("0.");
                    nuovoNumero = false;
                    return;
                }
                String testo = display.getText().toString();
                if (!testo.contains(".")) {
                    if (testo.isEmpty()) {
                        display.setText("0.");
                    } else {
                        display.append(".");
                    }
                }
            });
        }

        // OPERATORI
        setOperatore(R.id.piu, "+");
        setOperatore(R.id.meno, "-");
        setOperatore(R.id.per, "X");
        setOperatore(R.id.diviso, "/");
        setOperatore(R.id.potenza, "^");

        // UGUALE
        Button uguale = findViewById(R.id.uguale);
        if (uguale != null) {
            uguale.setOnClickListener(v -> calcola());
        }

        // RADICE
        Button radice = findViewById(R.id.radice);
        if (radice != null) {
            radice.setOnClickListener(v -> {
                Double n = getNumero();
                if (n == null || n < 0) {
                    errore();
                    return;
                }
                display.setText("√(" + n + ") = " + formatta(Math.sqrt(n)));
                nuovoNumero = true;
            });
        }

        // RECIPROCO
        Button reciproco = findViewById(R.id.reciproco);
        if (reciproco != null) {
            reciproco.setOnClickListener(v -> {
                Double n = getNumero();
                if (n == null || n == 0) {
                    errore();
                    return;
                }
                display.setText("1/(" + n + ") = " + formatta(1.0 / n));
                nuovoNumero = true;
            });
        }

        // FATTORIALE
        Button fattoriale = findViewById(R.id.fattoriale);
        if (fattoriale != null) {
            fattoriale.setOnClickListener(v -> {
                Double n = getNumero();
                if (n == null || n < 0 || n != Math.floor(n) || n > 20) {
                    errore();
                    return;
                }
                display.setText(n.intValue() + "! = " + fattoriale(n.intValue()));
                nuovoNumero = true;
            });
        }

        // LOG10
        Button log10 = findViewById(R.id.log10);
        if (log10 != null) {
            log10.setOnClickListener(v -> {
                Double n = getNumero();
                if (n == null || n <= 0) {
                    errore();
                    return;
                }
                display.setText("log(" + n + ") = " + formatta(Math.log10(n)));
                nuovoNumero = true;
            });
        }

        // CANCELLA
        Button cancella = findViewById(R.id.cancella);
        if (cancella != null) {
            cancella.setOnClickListener(v -> {
                display.setText("");
                primoNumero = "";
                operatore = "";
                nuovoNumero = false;
            });
        }

        // DEL
        Button elimina = findViewById(R.id.elimina);
        if (elimina != null) {
            elimina.setOnClickListener(v -> {
                String testo = display.getText().toString();
                if (!testo.isEmpty()) {
                    display.setText(testo.substring(0, testo.length() - 1));
                }
            });
        }
    }

    // OPERATORE
    private void setOperatore(int id, String op) {
        Button b = findViewById(id);
        if (b != null) {
            b.setOnClickListener(v -> {
                String testo = display.getText().toString();
                if (!testo.isEmpty()) {
                    primoNumero = testo;
                    operatore = op;

                    display.setText(primoNumero + " " + operatore + " ");
                    nuovoNumero = false;
                }
            });
        }
    }

    // CALCOLO
    private void calcola() {
        Double a = parse(primoNumero);
        Double b = getNumero();

        if (a == null || b == null) return;

        double risultato;

        switch (operatore) {
            case "+": risultato = a + b; break;
            case "-": risultato = a - b; break;
            case "X": risultato = a * b; break;
            case "/":
                if (b == 0) { errore(); return; }
                risultato = a / b;
                break;
            case "^":
                risultato = Math.pow(a, b);
                break;
            default: return;
        }

        display.setText(primoNumero + " " + operatore + " " + formatta(b) + " = " + formatta(risultato));

        primoNumero = "";
        operatore = "";
        nuovoNumero = true;
    }

    // PRENDE ULTIMO NUMERO
    private Double getNumero() {
        String testo = display.getText().toString();

        if (testo.contains(" ")) {
            String[] parti = testo.split(" ");
            try {
                return Double.parseDouble(parti[parti.length - 1]);
            } catch (Exception e) {
                return null;
            }
        }

        return parse(testo);
    }

    private Double parse(String s) {
        try {
            return Double.parseDouble(s);
        } catch (Exception e) {
            return null;
        }
    }

    private void errore() {
        display.setText("Errore");
        nuovoNumero = true;
    }

    private String formatta(double v) {
        if (Double.isNaN(v) || Double.isInfinite(v)) return "Errore";
        if (v == Math.floor(v)) return String.valueOf((long) v);
        return String.valueOf(v);
    }

    private long fattoriale(int n) {
        long r = 1;
        for (int i = 2; i <= n; i++) r *= i;
        return r;
    }
}