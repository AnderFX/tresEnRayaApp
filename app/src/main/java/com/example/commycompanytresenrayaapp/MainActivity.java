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
import com.example.commycompanytresenrayaapp.modelo.Ficha;
import com.example.commycompanytresenrayaapp.modelo.Minimax;
import com.example.commycompanytresenrayaapp.modelo.Tablero;
import com.example.commycompanytresenrayaapp.vista.PantallaJuego;

/**
 * Punto de entrada de la app. Siguiendo el diagrama de clases original,
 * MainActivity es solo el "ensamblador": crea el Modelo (Tablero,
 * Minimax, ControladorJuego), instancia la Vista (PantallaJuego, que
 * implementa ObservadorJuego y construye su propia UI por codigo) y la
 * incrusta en la pantalla. MainActivity no conoce Fichas puestas ni
 * turnos directamente; toda esa logica vive en ControladorJuego y se
 * refleja a traves de PantallaJuego.
 */
public class MainActivity extends AppCompatActivity {

    private ControladorJuego controlador;

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

        // --- GRUPO 1: SÍMBOLO ---
        TextView tituloSimbolo = new TextView(this);
        tituloSimbolo.setText("¿Con qué símbolo quieres jugar?");
        config.addView(tituloSimbolo);

        RadioGroup grupoSimbolo = new RadioGroup(this);

        RadioButton opcionX = new RadioButton(this);
        opcionX.setId(View.generateViewId()); // 👈 ¡LÍNEA CLAVE!
        opcionX.setText("X");
        opcionX.setChecked(true);

        RadioButton opcionO = new RadioButton(this);
        opcionO.setId(View.generateViewId()); // 👈 ¡LÍNEA CLAVE!
        opcionO.setText("O");

        grupoSimbolo.addView(opcionX);
        grupoSimbolo.addView(opcionO);
        config.addView(grupoSimbolo);

        // --- GRUPO 2: TURNO ---
        TextView tituloTurno = new TextView(this);
        tituloTurno.setText("¿Quién empieza la partida?");
        config.addView(tituloTurno);

        RadioGroup grupoTurno = new RadioGroup(this);

        RadioButton opcionHumano = new RadioButton(this);
        opcionHumano.setId(View.generateViewId()); // 👈 ¡LÍNEA CLAVE!
        opcionHumano.setText("Yo (humano)");
        opcionHumano.setChecked(true);

        RadioButton opcionPC = new RadioButton(this);
        opcionPC.setId(View.generateViewId()); // 👈 ¡LÍNEA CLAVE!
        opcionPC.setText("La computadora");

        grupoTurno.addView(opcionHumano);
        grupoTurno.addView(opcionPC);
        config.addView(grupoTurno);

        // --- BOTÓN COMENZAR ---
        Button botonComenzar = new Button(this);
        botonComenzar.setText("Comenzar Juego");
        botonComenzar.setOnClickListener(v -> {
            Ficha fichaHumano = opcionX.isChecked() ? Ficha.X : Ficha.O;
            boolean humanoEmpieza = opcionHumano.isChecked();
            iniciarPartida(fichaHumano, humanoEmpieza);
        });
        config.addView(botonComenzar);

        setContentView(config);
    }

    // iniciarPartida ahora recibe parámetros en vez de usar las constantes fijas
    private void iniciarPartida(Ficha fichaHumano, boolean humanoEmpieza) {
        Ficha fichaPC = (fichaHumano == Ficha.X) ? Ficha.O : Ficha.X;

        Tablero tablero = new Tablero();
        Minimax algoritmo = new Minimax(tablero, fichaPC, fichaHumano);

        controlador = new ControladorJuego(tablero, algoritmo);
        controlador.setFichaHumano(fichaHumano);
        controlador.setFichaPC(fichaPC);
        controlador.setTurnoHumano(humanoEmpieza);

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

        botonReiniciar.setOnClickListener(v -> recreate());
        return botonReiniciar;
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}