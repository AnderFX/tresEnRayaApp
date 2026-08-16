/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.commycompanytresenrayaapp.modelo;

import java.util.List;
// Implementa la estrategia de decisión de la computadora

public class Minimax {

    private static final int PROFUNDIDAD_ANALISIS = 2;
    private Tablero tableroActual;

    private Ficha fichaPC;
    private Ficha fichaHumano;
    private NodoJugada raizUltimoAnalisis;

    public Minimax(Tablero tableroActual, Ficha fichaPC, Ficha fichaHumano) {
        this.tableroActual = tableroActual;
        this.fichaPC = fichaPC;
        this.fichaHumano = fichaHumano;
    }

    public void generarArbolDeJuego(ArbolNario arbol, int profundidad) {
        generarArbolDeJuegoPara(arbol, profundidad, fichaPC);
    }

    public void generarArbolDeJuegoPara(ArbolNario arbol, int profundidad, Ficha fichaQueDecide) {
        NodoJugada raiz = new NodoJugada(tableroActual.clonar(), -1, -1);
        arbol.setRaiz(raiz);
        generarHijos(raiz, profundidad, fichaQueDecide);
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

    public int minimax(NodoJugada nodoActual, int profundidad, boolean esTurnoDeQuienDecide, Ficha fichaQueDecide) {
        Tablero tableroNodo = nodoActual.getEstado();

        boolean esEstadoTerminal = nodoActual.esHoja()
                || profundidad <= 0
                || tableroNodo.verificarGanador(Ficha.X)
                || tableroNodo.verificarGanador(Ficha.O);

        if (esEstadoTerminal) {
            int utilidad = calcularUtilidad(tableroNodo, fichaQueDecide);
            nodoActual.setUtilidad(utilidad);
            return utilidad;
        }

        int mejorUtilidad = esTurnoDeQuienDecide ? Integer.MIN_VALUE : Integer.MAX_VALUE;

        for (NodoJugada hijo : nodoActual.getHijos()) {
            int utilidadHijo = minimax(hijo, profundidad - 1, !esTurnoDeQuienDecide, fichaQueDecide);
            if (esTurnoDeQuienDecide) {
                mejorUtilidad = Math.max(mejorUtilidad, utilidadHijo);
            } else {
                mejorUtilidad = Math.min(mejorUtilidad, utilidadHijo);
            }
        }

        nodoActual.setUtilidad(mejorUtilidad);
        return mejorUtilidad;
    }

    public int[] obtenerMejorJugadaPara(Ficha fichaQueDecide) {
        ArbolNario arbol = new ArbolNario();
        generarArbolDeJuegoPara(arbol, PROFUNDIDAD_ANALISIS, fichaQueDecide);

        NodoJugada raiz = arbol.getRaiz();
        minimax(raiz, PROFUNDIDAD_ANALISIS, true, fichaQueDecide);
        raizUltimoAnalisis = raiz;

        NodoJugada mejorJugada = null;
        int mejorUtilidad = Integer.MIN_VALUE;

        for (NodoJugada hijo : raiz.getHijos()) {
            if (mejorJugada == null || hijo.getUtilidad() > mejorUtilidad) {
                mejorUtilidad = hijo.getUtilidad();
                mejorJugada = hijo;
            }
        }

        if (mejorJugada == null) {
            return new int[]{-1, -1};
        }

        return new int[]{mejorJugada.getFilaJugada(), mejorJugada.getColumnaJugada()};
    }

    private int calcularUtilidad(Tablero tablero, Ficha fichaQueDecide) {
        Ficha oponente = (fichaQueDecide == fichaPC) ? fichaHumano : fichaPC;
        return tablero.contarLineasDisponibles(fichaQueDecide) - tablero.contarLineasDisponibles(oponente);
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

    public NodoJugada getRaizUltimoAnalisis() {
        return raizUltimoAnalisis;
    }
}

