/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.commycompanytresenrayaapp.modelo;

import java.util.List;
// Implementa la estrategia de decisión de la computadora

public class Minimax {

    private Tablero tableroActual;

    private Ficha fichaPC;
    private Ficha fichaHumano;

    public Minimax(Tablero tableroActual, Ficha fichaPC, Ficha fichaHumano) {
        this.tableroActual = tableroActual;
        this.fichaPC = fichaPC;
        this.fichaHumano = fichaHumano;
    }

    public void generarArbolDeJuego(ArbolNario arbol, int profundidad) {
    }

    public int minimax(NodoJugada nodoActual, int profundidad, boolean esTurnoPC) {
        return 0;
    }

    public int[] obtenerMejorJugada() {
        return new int[]{-1, -1};
    }

    // Getters y Setters 
    public Tablero getTableroActual() {
        return tableroActual;
    }

    public void setTableroActual(Tablero tableroActual) {
        this.tableroActual = tableroActual;
    }

    public Ficha getFichaPC() {
        return fichaPC;
    }

    public void setFichaPC(Ficha fichaPC) {
        this.fichaPC = fichaPC;
    }

    public Ficha getFichaHumano() {
        return fichaHumano;
    }

    public void setFichaHumano(Ficha fichaHumano) {
        this.fichaHumano = fichaHumano;
    }

    public void generarArbolDeJuego(ArbolNario arbol, int profundidad) {
        NodoJugada raiz = new NodoJugada(tableroActual.clonar(), -1, -1);
        arbol.setRaiz(raiz);
        generarHijos(raiz, profundidad, fichaPC);
    }

    private void generarHijos(NodoJugada nodoPadre, int profundidad, Ficha fichaTurno) {
        if (profundidad <= 0) {
            return;
        }

        Tablero tableroPadre = nodoPadre.getEstado();

        // El juego ya terminó en este tablero: no hay más jugadas que generar.
        if (tableroPadre.verificarGanador(Ficha.X) || tableroPadre.verificarGanador(Ficha.O)) {
            return;
        }

        List<int[]> disponibles = tableroPadre.obtenerCasillasDisponibles();
        if (disponibles.isEmpty()) {
            return; // Empate: tablero lleno, tampoco hay más jugadas.
        }

        Ficha siguienteTurno = (fichaTurno == fichaPC) ? fichaHumano : fichaPC;

        for (int[] casilla : disponibles) {
            Tablero tableroHijo = tableroPadre.clonar();
            tableroHijo.llenarCasilla(casilla[0], casilla[1], fichaTurno);

            NodoJugada nodoHijo = new NodoJugada(tableroHijo, casilla[0], casilla[1]);
            nodoPadre.agregarHijo(nodoHijo);

            generarHijos(nodoHijo, profundidad - 1, siguienteTurno);
        }
    }
}
