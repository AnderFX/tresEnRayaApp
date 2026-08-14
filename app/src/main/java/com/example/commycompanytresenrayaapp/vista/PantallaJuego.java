package com.example.commycompanytresenrayaapp.vista;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.widget.Button;
import android.widget.GridLayout;

import com.example.commycompanytresenrayaapp.controlador.ControladorJuego;
import com.example.commycompanytresenrayaapp.controlador.ObservadorJuego;
import com.example.commycompanytresenrayaapp.modelo.Ficha;

public class PantallaJuego extends GridLayout implements ObservadorJuego {

    private ControladorJuego controlador;
    private Button[][] botones;

    /**
     * Fondo original de cada botón (el drawable por defecto de Button),
     * guardado al construir la UI. Se usa para "apagar" el resaltado de
     * sugerencia sin dejar el botón visualmente plano (setBackgroundColor
     * con TRANSPARENT elimina el estilo por defecto del Button).
     */
    private Drawable[][] fondosOriginales;

    public PantallaJuego(Context context, ControladorJuego controlador) {
        super(context);
        this.controlador = controlador;
        this.botones = new Button[3][3];
        this.fondosOriginales = new Drawable[3][3];
        this.controlador.agregarObservador(this);
        inicializarUI();
    }

    public void inicializarUI() {
        setColumnCount(3);
        setRowCount(3);
        setPadding(dpToPx(10), dpToPx(10), dpToPx(10), dpToPx(10));

        for (int fila = 0; fila < 3; fila++) {
            for (int columna = 0; columna < 3; columna++) {
                Button boton = new Button(getContext());
                boton.setText("");
                boton.setTextSize(24);

                GridLayout.LayoutParams params = new GridLayout.LayoutParams(
                        GridLayout.spec(fila),
                        GridLayout.spec(columna));
                params.width = dpToPx(90);
                params.height = dpToPx(90);
                params.setMargins(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));
                boton.setLayoutParams(params);

                final int f = fila;
                final int c = columna;
                boton.setOnClickListener(v -> onBotonClic(f, c));

                botones[fila][columna] = boton;
                fondosOriginales[fila][columna] = boton.getBackground();
                addView(boton);
            }
        }
    }

    public void onBotonClic(int fila, int columna) {
        controlador.jugarTurnoHumano(fila, columna);
    }

    /**
     * Resalta visualmente una casilla (usado por el botón "💡 Sugerencia"
     * de PanelAyuda para mostrar la jugada recomendada al humano).
     */
    public void resaltarCasilla(int fila, int columna) {
        botones[fila][columna].setBackgroundColor(Color.parseColor("#FFF59D"));
    }

    /**
     * Quita cualquier resaltado de sugerencia, devolviendo cada botón a su
     * fondo original (en vez de a un color plano).
     */
    public void quitarResaltados() {
        for (int f = 0; f < 3; f++) {
            for (int c = 0; c < 3; c++) {
                botones[f][c].setBackground(fondosOriginales[f][c]);
            }
        }
    }

    @Override
    public void onJugadaRealizada(int fila, int columna, Ficha ficha) {
        // Si había una casilla resaltada por una sugerencia previa, se
        // limpia en cada jugada (propia o de la PC) para que el resaltado
        // nunca quede mostrando una recomendación obsoleta.
        quitarResaltados();
        botones[fila][columna].setText(ficha == Ficha.VACIA ? "" : ficha.name());
    }

    @Override
    public void onJuegoTerminado(String mensaje) {
        new AlertDialog.Builder(getContext())
                .setTitle("Tres en Raya")
                .setMessage(mensaje)
                .setPositiveButton("Aceptar", null)
                .show();
    }

    private int dpToPx(int dp) {
        float density = getContext().getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
