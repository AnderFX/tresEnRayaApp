package com.example.commycompanytresenrayaapp;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.example.commycompanytresenrayaapp.controlador.ControladorJuego;
import com.example.commycompanytresenrayaapp.modelo.Ficha;
import com.example.commycompanytresenrayaapp.modelo.Minimax;
import com.example.commycompanytresenrayaapp.modelo.Tablero;
import com.example.commycompanytresenrayaapp.vista.PantallaJuego;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Tablero tablero = new Tablero();
        Minimax algoritmo = new Minimax(tablero, Ficha.O, Ficha.X);
        ControladorJuego controlador = new ControladorJuego(tablero, algoritmo);

        PantallaJuego pantalla = new PantallaJuego(this, controlador);
        controlador.iniciarJuego();

        setTitle("Tres en Raya vs. Computadora");
        setContentView(pantalla);
    }
}