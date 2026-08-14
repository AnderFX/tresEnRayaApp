package com.example.commycompanytresenrayaapp;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.commycompanytresenrayaapp.controlador.ControladorJuego;
import com.example.commycompanytresenrayaapp.controlador.ModoJuego;
import com.example.commycompanytresenrayaapp.modelo.Ficha;
import com.example.commycompanytresenrayaapp.modelo.Minimax;
import com.example.commycompanytresenrayaapp.modelo.Tablero;
import com.example.commycompanytresenrayaapp.vista.PanelAyuda;
import com.example.commycompanytresenrayaapp.vista.PantallaJuego;

public class MainActivity extends AppCompatActivity {

    private ControladorJuego controlador;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Tres en Raya vs. Computadora");
        mostrarPantallaConfiguracion();
    }

    private void mostrarPantallaConfiguracion() {
        LinearLayout config = new LinearLayout(this);
        config.setOrientation(LinearLayout.VERTICAL);
        config.setGravity(Gravity.CENTER);
        int padding = dpToPx(24);
        config.setPadding(padding, padding, padding, padding);

        // --- GRUPO 0: MODO DE JUEGO ---
        TextView tituloModo = new TextView(this);
        tituloModo.setText("¿Cómo quieres jugar?");
        config.addView(tituloModo);

        RadioGroup grupoModo = new RadioGroup(this);

        RadioButton opcionHumanoVsPc = new RadioButton(this);
        opcionHumanoVsPc.setId(View.generateViewId());
        opcionHumanoVsPc.setText("Humano vs. Computadora");
        opcionHumanoVsPc.setChecked(true);

        RadioButton opcionPcVsPc = new RadioButton(this);
        opcionPcVsPc.setId(View.generateViewId());
        opcionPcVsPc.setText("Computadora vs. Computadora");

        RadioButton opcionHumanoVsHumano = new RadioButton(this);
        opcionHumanoVsHumano.setId(View.generateViewId());
        opcionHumanoVsHumano.setText("Humano vs. Humano");

        grupoModo.addView(opcionHumanoVsPc);
        grupoModo.addView(opcionPcVsPc);
        grupoModo.addView(opcionHumanoVsHumano);
        config.addView(grupoModo);

        // --- GRUPO 1: SÍMBOLO (solo aplica en Humano vs. Computadora;
        // en los otros dos modos las fichas quedan fijas: X y O) ---
        TextView tituloSimbolo = new TextView(this);
        tituloSimbolo.setText("¿Con qué símbolo quieres jugar?");
        config.addView(tituloSimbolo);

        RadioGroup grupoSimbolo = new RadioGroup(this);

        RadioButton opcionX = new RadioButton(this);
        opcionX.setId(View.generateViewId());
        opcionX.setText("X");
        opcionX.setChecked(true);

        RadioButton opcionO = new RadioButton(this);
        opcionO.setId(View.generateViewId());
        opcionO.setText("O");

        grupoSimbolo.addView(opcionX);
        grupoSimbolo.addView(opcionO);
        config.addView(grupoSimbolo);

        // --- GRUPO 2: QUIÉN EMPIEZA ---
        // El texto de las opciones cambia según el modo (ver el listener de
        // grupoModo más abajo): en Humano vs. Computadora representan
        // "Yo (humano)" / "La computadora"; en Humano vs. Humano pasan a
        // representar a cada jugador. En Computadora vs. Computadora se
        // oculta por completo (siempre empieza X).
        TextView tituloTurno = new TextView(this);
        tituloTurno.setText("¿Quién empieza la partida?");
        config.addView(tituloTurno);

        RadioGroup grupoTurno = new RadioGroup(this);

        RadioButton opcionPrimero = new RadioButton(this);
        opcionPrimero.setId(View.generateViewId());
        opcionPrimero.setText("Yo (humano)");
        opcionPrimero.setChecked(true);

        RadioButton opcionSegundo = new RadioButton(this);
        opcionSegundo.setId(View.generateViewId());
        opcionSegundo.setText("La computadora");

        grupoTurno.addView(opcionPrimero);
        grupoTurno.addView(opcionSegundo);
        config.addView(grupoTurno);

        // Ajusta qué controles se ven y cómo se etiquetan, según el modo elegido.
        grupoModo.setOnCheckedChangeListener((group, checkedId) -> {
            boolean esHumanoVsPc = checkedId == opcionHumanoVsPc.getId();
            boolean esPcVsPc = checkedId == opcionPcVsPc.getId();

            tituloSimbolo.setVisibility(esHumanoVsPc ? View.VISIBLE : View.GONE);
            grupoSimbolo.setVisibility(esHumanoVsPc ? View.VISIBLE : View.GONE);

            tituloTurno.setVisibility(esPcVsPc ? View.GONE : View.VISIBLE);
            grupoTurno.setVisibility(esPcVsPc ? View.GONE : View.VISIBLE);

            if (esHumanoVsPc) {
                opcionPrimero.setText("Yo (humano)");
                opcionSegundo.setText("La computadora");
            } else {
                opcionPrimero.setText("Jugador 1 (X)");
                opcionSegundo.setText("Jugador 2 (O)");
            }
        });

        // --- BOTÓN COMENZAR ---
        Button botonComenzar = new Button(this);
        botonComenzar.setText("Comenzar Juego");
        botonComenzar.setOnClickListener(v -> {
            ModoJuego modo;
            if (opcionPcVsPc.isChecked()) {
                modo = ModoJuego.PC_VS_PC;
            } else if (opcionHumanoVsHumano.isChecked()) {
                modo = ModoJuego.HUMANO_VS_HUMANO;
            } else {
                modo = ModoJuego.HUMANO_VS_PC;
            }

            Ficha fichaHumano = opcionX.isChecked() ? Ficha.X : Ficha.O;
            boolean primeroEmpieza = opcionPrimero.isChecked();
            iniciarPartida(modo, fichaHumano, primeroEmpieza);
        });
        config.addView(botonComenzar);

        setContentView(config);
    }

    /**
     * Arma el ControladorJuego correspondiente al modo elegido y arranca
     * la partida.
     *
     * @param modo            modo de juego elegido en la pantalla de configuración
     * @param fichaHumano     ficha del humano; solo se usa en HUMANO_VS_PC
     * @param primeroEmpieza  true si el primer jugador (humano, en
     *                        HUMANO_VS_PC/HUMANO_VS_HUMANO) mueve primero;
     *                        ignorado en PC_VS_PC (siempre empieza X)
     */
    private void iniciarPartida(ModoJuego modo, Ficha fichaHumano, boolean primeroEmpieza) {
        Tablero tablero = new Tablero();
        controlador = new ControladorJuego(tablero, null); // el algoritmo se decide abajo según el modo

        switch (modo) {
            case HUMANO_VS_PC:
                Ficha fichaPC = (fichaHumano == Ficha.X) ? Ficha.O : Ficha.X;
                Minimax algoritmo = new Minimax(tablero, fichaPC, fichaHumano);
                controlador.setAlgoritmo(algoritmo);
                controlador.setFichaHumano(fichaHumano);
                controlador.setFichaPC(fichaPC);
                controlador.setTurnoHumano(primeroEmpieza);
                break;

            case PC_VS_PC:
                Minimax algoritmoJ1 = new Minimax(tablero, Ficha.X, Ficha.O);
                Minimax algoritmoJ2 = new Minimax(tablero, Ficha.O, Ficha.X);
                controlador.configurarModoPcVsPc(algoritmoJ1, algoritmoJ2);
                break;

            case HUMANO_VS_HUMANO:
                controlador.configurarModoHumanoVsHumano(Ficha.X, Ficha.O, primeroEmpieza);
                break;
        }

        PantallaJuego pantalla = new PantallaJuego(this, controlador);
        setContentView(construirContenedor(pantalla));
        controlador.iniciarJuego();
    }

    private LinearLayout construirContenedor(PantallaJuego pantalla) {
        LinearLayout contenedor = new LinearLayout(this);
        contenedor.setOrientation(LinearLayout.VERTICAL);
        contenedor.setGravity(Gravity.CENTER);
        int padding = dpToPx(24);
        contenedor.setPadding(padding, padding, padding, padding);
        contenedor.addView(pantalla);
        contenedor.addView(new PanelAyuda(this, controlador, pantalla));
        contenedor.addView(construirBotonReiniciar());
        return contenedor;
    }

    private Button construirBotonReiniciar() {
        Button botonReiniciar = new Button(this);
        botonReiniciar.setText("Nueva Partida");

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.topMargin = dpToPx(16);
        botonReiniciar.setLayoutParams(params);

        botonReiniciar.setOnClickListener(v -> {
            // Corta cualquier cadena de jugadas automáticas pendiente
            // (relevante en modo PC vs. PC) antes de descartar esta pantalla.
            if (controlador != null) {
                controlador.detener();
            }
            recreate();
        });
        return botonReiniciar;
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
