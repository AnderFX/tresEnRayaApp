package com.example.commycompanytresenrayaapp.vista;

import android.app.AlertDialog;
import android.content.Context;
import android.widget.Button;
import android.widget.GridLayout;

import com.example.commycompanytresenrayaapp.controlador.ControladorJuego;
import com.example.commycompanytresenrayaapp.controlador.ObservadorJuego;
import com.example.commycompanytresenrayaapp.modelo.Ficha;

public class PantallaJuego extends GridLayout implements ObservadorJuego {

    private ControladorJuego controlador;
    private Button[][] botones;

    public PantallaJuego(Context context, ControladorJuego controlador) {
        super(context);
        this.controlador = controlador;
        this.botones = new Button[3][3];
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
                addView(boton);
            }
        }
    }

    public void onBotonClic(int fila, int columna) {
        controlador.jugarTurnoHumano(fila, columna);
    }

    @Override
    public void onJugadaRealizada(int fila, int columna, Ficha ficha) {
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