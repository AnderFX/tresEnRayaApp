package com.example.commycompanytresenrayaapp.vista;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.Button;
import android.widget.GridLayout;

import com.example.commycompanytresenrayaapp.R;
import com.example.commycompanytresenrayaapp.controlador.ControladorJuego;
import com.example.commycompanytresenrayaapp.controlador.ObservadorJuego;
import com.example.commycompanytresenrayaapp.modelo.Ficha;

public class PantallaJuego extends GridLayout implements ObservadorJuego {

    private ControladorJuego controlador;
    private Button[][] botones;
    private Drawable[][] fondosOriginales;
    private int filaResaltada = -1;
    private int columnaResaltada = -1;

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
                fondosOriginales[fila][columna] = boton.getBackground();

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
                addView(boton);
            }
        }
    }

    public void onBotonClic(int fila, int columna) {
        controlador.jugarTurno(fila, columna);
    }

    public void resaltarSugerencia(int fila, int columna) {
        limpiarResaltado();
        botones[fila][columna].setBackgroundColor(
                getContext().getColor(R.color.sugerencia_resaltado));
        filaResaltada = fila;
        columnaResaltada = columna;
    }

    private void limpiarResaltado() {
        if (filaResaltada != -1) {
            botones[filaResaltada][columnaResaltada].setBackground(
                    fondosOriginales[filaResaltada][columnaResaltada]);
        }
        filaResaltada = -1;
        columnaResaltada = -1;
    }

    @Override
    public void onJugadaRealizada(int fila, int columna, Ficha ficha) {
        limpiarResaltado();
        Button boton = botones[fila][columna];
        boton.setText(ficha == Ficha.VACIA ? "" : ficha.name());
        if (ficha != Ficha.VACIA) {
            animarAparicionFicha(boton);
        }
    }

    private void animarAparicionFicha(Button boton) {
        boton.setScaleX(0.3f);
        boton.setScaleY(0.3f);
        boton.setAlpha(0f);
        boton.animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(200).start();
    }

    public void setInteractivo(boolean interactivo) {
        for (int fila = 0; fila < 3; fila++) {
            for (int columna = 0; columna < 3; columna++) {
                botones[fila][columna].setEnabled(interactivo);
            }
        }
    }

    @Override
    public void onJuegoTerminado(String mensaje) {
        new AlertDialog.Builder(getContext())
                .setTitle("Tres en Raya")
                .setMessage(mensaje)
                .setPositiveButton("Aceptar", null)
                .show();
    }

    @Override
    public void onCambioDeTurno(Ficha fichaEnTurno) {
    }

    private int dpToPx(int dp) {
        float density = getContext().getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}