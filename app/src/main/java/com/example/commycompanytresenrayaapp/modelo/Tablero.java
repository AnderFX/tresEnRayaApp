/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.commycompanytresenrayaapp.modelo;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa el tablero de 3x3 del juego de Tres en Raya.
 * Guarda el estado de las casillas y ofrece las operaciones necesarias
 * para jugar, deshacer jugadas (usado por Minimax al explorar el árbol)
 * y evaluar el estado del juego (ganador, líneas disponibles, etc.).
 */
public class Tablero {

    public static final int TAMANIO = 3;

    private Ficha[][] casillas;

    public Tablero() {
        casillas = new Ficha[TAMANIO][TAMANIO];
        for (int fila = 0; fila < TAMANIO; fila++) {
            for (int columna = 0; columna < TAMANIO; columna++) {
                casillas[fila][columna] = Ficha.VACIA;
            }
        }
    }

    public void llenarCasilla(int fila, int columna, Ficha ficha) {
        casillas[fila][columna] = ficha;
    }

    public void deshacerCasilla(int fila, int columna) {
        casillas[fila][columna] = Ficha.VACIA;
    }
    
    public List<int[]> obtenerCasillasDisponibles() {
        List<int[]> disponibles = new ArrayList<>();
        return disponibles;
    }
    
    public boolean verificarGanador(Ficha ficha) {
        return false;
    }
    
    public int contarLineasDisponibles(Ficha ficha) {
        return 0;
    }
    
    public Tablero clonar() {
        Tablero copia = new Tablero();
        return copia;
    }

    // Getters y Setters 

    public Ficha[][] getCasillas() {
        return casillas;
    }

    public void setCasillas(Ficha[][] casillas) {
        this.casillas = casillas;
    }

    public Ficha getCasilla(int fila, int columna) {
        return casillas[fila][columna];
    }

    public void setCasilla(int fila, int columna, Ficha ficha) {
        casillas[fila][columna] = ficha;
    }
}
