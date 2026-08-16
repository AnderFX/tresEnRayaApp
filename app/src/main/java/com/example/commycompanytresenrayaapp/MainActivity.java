package com.example.commycompanytresenrayaapp;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.commycompanytresenrayaapp.controlador.ControladorJuego;
import com.example.commycompanytresenrayaapp.controlador.ModoJuego;
import com.example.commycompanytresenrayaapp.controlador.ObservadorJuego;
import com.example.commycompanytresenrayaapp.modelo.Ficha;
import com.example.commycompanytresenrayaapp.modelo.Minimax;
import com.example.commycompanytresenrayaapp.modelo.Tablero;
import com.example.commycompanytresenrayaapp.vista.PantallaJuego;

/**
 * Punto de entrada de la app. Siguiendo el diagrama de clases original,
 * MainActivity es solo el "ensamblador": crea el Modelo (Tablero,
 * Minimax, ControladorJuego), instancia la Vista (PantallaJuego, que
 * implementa ObservadorJuego y construye su propia UI por codigo) y la
 * incrusta en la pantalla. MainActivity tambien se registra como
 * ObservadorJuego, solo para reflejar el turno actual en pantalla.
 */
public class MainActivity extends AppCompatActivity implements ObservadorJuego {

    private static final long DEMORA_JUGADA_PC_MS = 500;

    private ControladorJuego controlador;
    private PantallaJuego pantalla;
    private TextView indicadorTurno;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Tres en Raya vs. Computadora");
        mostrarPantallaConfiguracion();   // en vez de iniciarPartida() directo
    }

    private void mostrarPantallaConfiguracion() {
        LinearLayout config = new LinearLayout(this);
        config.setOrientation(LinearLayout.VERTICAL);
        config.setGravity(Gravity.CENTER);
        int padding = dpToPx(24);
        config.setPadding(padding, padding, padding, padding);

        // --- GRUPO 0: MODO ---
        TextView tituloModo = new TextView(this);
        tituloModo.setText("¿Cómo quieres jugar?");
        config.addView(tituloModo);

        RadioGroup grupoModo = new RadioGroup(this);

        RadioButton opcionContraPC = new RadioButton(this);
        opcionContraPC.setId(View.generateViewId());
        opcionContraPC.setText("Contra la computadora");
        opcionContraPC.setChecked(true);

        RadioButton opcionDosHumanos = new RadioButton(this);
        opcionDosHumanos.setId(View.generateViewId());
        opcionDosHumanos.setText("Dos jugadores humanos");

        grupoModo.addView(opcionContraPC);
        grupoModo.addView(opcionDosHumanos);
        config.addView(grupoModo);

        // --- GRUPO 1: SÍMBOLO ---
        LinearLayout seccionSimbolo = new LinearLayout(this);
        seccionSimbolo.setOrientation(LinearLayout.VERTICAL);
        seccionSimbolo.setGravity(Gravity.CENTER);

        TextView tituloSimbolo = new TextView(this);
        tituloSimbolo.setText("¿Con qué símbolo quieres jugar?");
        seccionSimbolo.addView(tituloSimbolo);

        RadioGroup grupoSimbolo = new RadioGroup(this);

        RadioButton opcionX = new RadioButton(this);
        opcionX.setId(View.generateViewId()); // necesario para que el RadioGroup sepa cual esta marcado
        opcionX.setText("X");
        opcionX.setChecked(true);

        RadioButton opcionO = new RadioButton(this);
        opcionO.setId(View.generateViewId());
        opcionO.setText("O");

        grupoSimbolo.addView(opcionX);
        grupoSimbolo.addView(opcionO);
        seccionSimbolo.addView(grupoSimbolo);
        config.addView(seccionSimbolo);

        // --- GRUPO 2: TURNO ---
        LinearLayout seccionTurno = new LinearLayout(this);
        seccionTurno.setOrientation(LinearLayout.VERTICAL);
        seccionTurno.setGravity(Gravity.CENTER);

        TextView tituloTurno = new TextView(this);
        tituloTurno.setText("¿Quién empieza la partida?");
        seccionTurno.addView(tituloTurno);

        RadioGroup grupoTurno = new RadioGroup(this);

        RadioButton opcionHumano = new RadioButton(this);
        opcionHumano.setId(View.generateViewId());
        opcionHumano.setText("Yo (humano)");
        opcionHumano.setChecked(true);

        RadioButton opcionPC = new RadioButton(this);
        opcionPC.setId(View.generateViewId());
        opcionPC.setText("La computadora");

        grupoTurno.addView(opcionHumano);
        grupoTurno.addView(opcionPC);
        seccionTurno.addView(grupoTurno);
        config.addView(seccionTurno);

        grupoModo.setOnCheckedChangeListener((group, checkedId) -> {
            boolean esDosHumanos = checkedId == opcionDosHumanos.getId();
            seccionSimbolo.setVisibility(esDosHumanos ? View.GONE : View.VISIBLE);
            seccionTurno.setVisibility(esDosHumanos ? View.GONE : View.VISIBLE);
        });

        // --- BOTÓN COMENZAR ---
        Button botonComenzar = new Button(this);
        botonComenzar.setText("Comenzar Juego");
        botonComenzar.setOnClickListener(v -> {
            if (opcionDosHumanos.isChecked()) {
                iniciarPartidaDosHumanos();
                return;
            }
            Ficha fichaHumano = opcionX.isChecked() ? Ficha.X : Ficha.O;
            boolean humanoEmpieza = opcionHumano.isChecked();
            iniciarPartidaContraPC(fichaHumano, humanoEmpieza);
        });
        config.addView(botonComenzar);

        setContentView(config);
    }

    private void iniciarPartidaContraPC(Ficha fichaHumano, boolean humanoEmpieza) {
        Ficha fichaPC = (fichaHumano == Ficha.X) ? Ficha.O : Ficha.X;

        Tablero tablero = new Tablero();
        Minimax algoritmo = new Minimax(tablero, fichaPC, fichaHumano);

        controlador = new ControladorJuego(tablero, algoritmo);
        controlador.setModoJuego(ModoJuego.CONTRA_PC);
        controlador.setFichaHumano(fichaHumano);
        controlador.setFichaPC(fichaPC);
        controlador.setFichaEnTurno(humanoEmpieza ? fichaHumano : fichaPC);

        montarPantallaDeJuego();
    }

    private void iniciarPartidaDosHumanos() {
        Tablero tablero = new Tablero();
        Minimax algoritmo = new Minimax(tablero, Ficha.O, Ficha.X);

        controlador = new ControladorJuego(tablero, algoritmo);
        controlador.setModoJuego(ModoJuego.DOS_HUMANOS);
        controlador.setFichaEnTurno(Ficha.X);

        montarPantallaDeJuego();
    }

    private void montarPantallaDeJuego() {
        pantalla = new PantallaJuego(this, controlador);
        controlador.agregarObservador(this);
        setContentView(construirContenedor(pantalla));
        controlador.iniciarJuego();
    }

    private LinearLayout construirContenedor(PantallaJuego pantalla) {
        LinearLayout contenedor = new LinearLayout(this);
        contenedor.setOrientation(LinearLayout.VERTICAL);
        contenedor.setGravity(Gravity.CENTER);
        int padding = dpToPx(24);
        contenedor.setPadding(padding, padding, padding, padding);

        indicadorTurno = new TextView(this);
        indicadorTurno.setTextSize(18);
        contenedor.addView(indicadorTurno);

        contenedor.addView(pantalla);
        contenedor.addView(construirBotonSugerencia(pantalla));
        contenedor.addView(construirBotonReiniciar());
        return contenedor;
    }

    private Button construirBotonSugerencia(PantallaJuego pantalla) {
        Button botonSugerencia = new Button(this);
        botonSugerencia.setText("Sugerencia");

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.topMargin = dpToPx(16);
        botonSugerencia.setLayoutParams(params);

        botonSugerencia.setOnClickListener(v -> {
            int[] sugerencia = controlador.obtenerSugerenciaParaTurnoActual();
            if (sugerencia == null) {
                Toast.makeText(this, "No hay sugerencia disponible ahora.", Toast.LENGTH_SHORT).show();
                return;
            }
            pantalla.resaltarSugerencia(sugerencia[0], sugerencia[1]);
        });
        return botonSugerencia;
    }

    private Button construirBotonReiniciar() {
        Button botonReiniciar = new Button(this);
        botonReiniciar.setText("Nueva Partida");

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.topMargin = dpToPx(16);
        botonReiniciar.setLayoutParams(params);

        botonReiniciar.setOnClickListener(v -> recreate());
        return botonReiniciar;
    }

    @Override
    public void onJugadaRealizada(int fila, int columna, Ficha ficha) {
    }

    @Override
    public void onJuegoTerminado(String mensaje) {
        indicadorTurno.setText("Juego terminado.");
        pantalla.setInteractivo(false);
    }

    @Override
    public void onCambioDeTurno(Ficha fichaEnTurno) {
        if (controlador.getModoJuego() == ModoJuego.CONTRA_PC && fichaEnTurno == controlador.getFichaPC()) {
            indicadorTurno.setText("La computadora está pensando...");
            pantalla.setInteractivo(false);
            handler.postDelayed(controlador::realizarJugadaPC, DEMORA_JUGADA_PC_MS);
            return;
        }
        pantalla.setInteractivo(true);
        indicadorTurno.setText("Turno de: " + fichaEnTurno.name());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
