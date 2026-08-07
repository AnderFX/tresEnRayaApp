package com.example.commycompanytresenrayaapp;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.commycompanytresenrayaapp.controlador.ControladorJuego;
import com.example.commycompanytresenrayaapp.controlador.ObservadorJuego;
import com.example.commycompanytresenrayaapp.modelo.Ficha;
import com.example.commycompanytresenrayaapp.modelo.Minimax;
import com.example.commycompanytresenrayaapp.modelo.Tablero;

/**
 * Punto de entrada de la app y, a la vez, la Vista del patron MVC para
 * este primer prototipo: infla el layout XML del tablero, conecta cada
 * boton con ControladorJuego.jugarTurnoHumano() y, como ObservadorJuego,
 * refleja en la UI cada jugada (propia o de la PC) y el fin de la partida.
 */
public class MainActivity extends AppCompatActivity implements ObservadorJuego {

    // Configuracion fija de este primer prototipo. La seleccion de fichas
    // y de quien empieza (requerimiento obligatorio del enunciado) queda
    // pendiente para una pantalla de configuracion inicial.
    private static final Ficha FICHA_HUMANO = Ficha.X;
    private static final Ficha FICHA_PC = Ficha.O;
    private static final boolean HUMANO_EMPIEZA = true;

    private ControladorJuego controlador;
    private final Button[][] botones = new Button[3][3];
    private TextView textoEstado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Tres en Raya vs. Computadora");

        enlazarVistas();
        iniciarPartida();
    }

    private void enlazarVistas() {
        textoEstado = findViewById(R.id.textoEstado);

        botones[0][0] = findViewById(R.id.boton00);
        botones[0][1] = findViewById(R.id.boton01);
        botones[0][2] = findViewById(R.id.boton02);
        botones[1][0] = findViewById(R.id.boton10);
        botones[1][1] = findViewById(R.id.boton11);
        botones[1][2] = findViewById(R.id.boton12);
        botones[2][0] = findViewById(R.id.boton20);
        botones[2][1] = findViewById(R.id.boton21);
        botones[2][2] = findViewById(R.id.boton22);

        for (int fila = 0; fila < 3; fila++) {
            for (int columna = 0; columna < 3; columna++) {
                final int f = fila;
                final int c = columna;
                botones[fila][columna].setOnClickListener(v -> controlador.jugarTurnoHumano(f, c));
            }
        }

        Button botonReiniciar = findViewById(R.id.botonReiniciar);
        botonReiniciar.setOnClickListener(v -> recreate());
    }

    private void iniciarPartida() {
        Tablero tablero = new Tablero();
        Minimax algoritmo = new Minimax(tablero, FICHA_PC, FICHA_HUMANO);

        controlador = new ControladorJuego(tablero, algoritmo);
        controlador.setFichaHumano(FICHA_HUMANO);
        controlador.setFichaPC(FICHA_PC);
        controlador.setTurnoHumano(HUMANO_EMPIEZA);
        controlador.agregarObservador(this);

        textoEstado.setText(turnoTexto());
        controlador.iniciarJuego();
    }

    private String turnoTexto() {
        return controlador.isTurnoHumano()
                ? "Tu turno (" + FICHA_HUMANO.name() + ")"
                : "Pensando...";
    }

    @Override
    public void onJugadaRealizada(int fila, int columna, Ficha ficha) {
        Button boton = botones[fila][columna];
        boton.setText(ficha == Ficha.VACIA ? "" : ficha.name());
        boton.setEnabled(ficha == Ficha.VACIA);
        textoEstado.setText(turnoTexto());
    }

    @Override
    public void onJuegoTerminado(String mensaje) {
        textoEstado.setText(mensaje);
        for (Button[] fila : botones) {
            for (Button boton : fila) {
                boton.setEnabled(false);
            }
        }
        new AlertDialog.Builder(this)
                .setTitle("Tres en Raya")
                .setMessage(mensaje)
                .setPositiveButton("Aceptar", null)
                .show();
    }
}