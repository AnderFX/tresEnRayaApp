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

    /**
     * Las 8 líneas ganadoras del tablero (3 filas, 3 columnas, 2 diagonales),
     * expresadas como coordenadas {fila, columna}. Se centraliza aquí para
     * que verificarGanador() y contarLineasDisponibles() no dupliquen la
     * misma lista de líneas cada uno por su lado.
     */
    private static final int[][][] LINEAS = {
            {{0, 0}, {0, 1}, {0, 2}}, // fila 0
            {{1, 0}, {1, 1}, {1, 2}}, // fila 1
            {{2, 0}, {2, 1}, {2, 2}}, // fila 2
            {{0, 0}, {1, 0}, {2, 0}}, // columna 0
            {{0, 1}, {1, 1}, {2, 1}}, // columna 1
            {{0, 2}, {1, 2}, {2, 2}}, // columna 2
            {{0, 0}, {1, 1}, {2, 2}}, // diagonal principal
            {{0, 2}, {1, 1}, {2, 0}}  // diagonal secundaria
    };

    private Ficha[][] casillas;

    public Tablero() {
        casillas = new Ficha[TAMANIO][TAMANIO];
        for (int fila = 0; fila < TAMANIO; fila++) {
            for (int columna = 0; columna < TAMANIO; columna++) {
                casillas[fila][columna] = Ficha.VACIA;
            }
        }
    }

    /**
     * Coloca una ficha en la casilla indicada.
     */
    public void llenarCasilla(int fila, int columna, Ficha ficha) {
        validarPosicion(fila, columna);
        if (casillas[fila][columna] != Ficha.VACIA) {
            throw new IllegalStateException(
                    "La casilla (" + fila + ", " + columna + ") ya está ocupada.");
        }
        casillas[fila][columna] = ficha;
    }

    /**
     * Vacía la casilla indicada. Se usa junto con llenarCasilla() para que
     * Minimax pueda probar jugadas futuras sobre un mismo tablero de trabajo
     * sin tener que clonar en cada nivel del árbol.
     */
    public void deshacerCasilla(int fila, int columna) {
        validarPosicion(fila, columna);
        casillas[fila][columna] = Ficha.VACIA;
    }

    /**
     * Retorna la lista de coordenadas {fila, columna} de las casillas libres.
     */
    public List<int[]> obtenerCasillasDisponibles() {
        List<int[]> disponibles = new ArrayList<>();
        for (int fila = 0; fila < TAMANIO; fila++) {
            for (int columna = 0; columna < TAMANIO; columna++) {
                if (casillas[fila][columna] == Ficha.VACIA) {
                    disponibles.add(new int[]{fila, columna});
                }
            }
        }
        return disponibles;
    }

    /**
     * Indica si la ficha dada completó una línea (fila, columna o diagonal).
     */
    public boolean verificarGanador(Ficha ficha) {
        for (int[][] linea : LINEAS) {
            boolean completaLinea = true;
            for (int[] celda : linea) {
                if (casillas[celda[0]][celda[1]] != ficha) {
                    completaLinea = false;
                    break;
                }
            }
            if (completaLinea) {
                return true;
            }
        }
        return false;
    }
    
    public int contarLineasDisponibles(Ficha ficha) {
        return 0;
    }
    
    public Tablero clonar() {
        Tablero copia = new Tablero();
        return copia;
    }

    /**
     * Valida que una coordenada esté dentro de los límites del tablero.
     */
    private void validarPosicion(int fila, int columna) {
        if (fila < 0 || fila >= TAMANIO || columna < 0 || columna >= TAMANIO) {
            throw new IllegalArgumentException(
                    "Posición fuera del tablero: (" + fila + ", " + columna + ")");
        }
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
