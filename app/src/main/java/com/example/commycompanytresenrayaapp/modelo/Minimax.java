/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.commycompanytresenrayaapp.modelo;

import java.util.List;

// Implementa la estrategia de decisión de la computadora

public class Minimax {

    /**
     * Profundidad (en turnos) que se analiza a partir del tablero actual:
     * el turno propio de la PC y, a continuación, la respuesta del humano.
     * Coincide exactamente con el algoritmo descrito en el enunciado del
     * proyecto ("se deben generar los posibles estados... considerando
     * dos turnos").
     */
    private static final int PROFUNDIDAD_ANALISIS = 2;

    private Tablero tableroActual;

    private Ficha fichaPC;
    private Ficha fichaHumano;

    /**
     * Último árbol n-ario generado por {@link #obtenerMejorJugada()}. Se
     * guarda únicamente para poder mostrarlo después (funcionalidad
     * opcional de visualización del árbol); no participa en el cálculo
     * de la jugada en sí. Puede ser null si obtenerMejorJugada() todavía
     * no se ha llamado sobre esta instancia.
     */
    private ArbolNario ultimoArbolGenerado;

    public Minimax(Tablero tableroActual, Ficha fichaPC, Ficha fichaHumano) {
        this.tableroActual = tableroActual;
        this.fichaPC = fichaPC;
        this.fichaHumano = fichaHumano;
    }

    /**
     * Genera el árbol n-ario de jugadas posibles a partir de {@link #tableroActual}.
     * La raíz representa el tablero actual (sin jugada asociada, fila = columna = -1).
     * El primer nivel contiene las jugadas que la PC podría realizar en su turno y,
     * por cada una de ellas, el segundo nivel contiene las jugadas con las que el
     * humano podría responder.
     *
     * <p>La expansión de una rama se detiene antes de llegar a {@code profundidad}
     * niveles si el tablero de ese nodo ya tiene un ganador o no tiene casillas
     * libres, porque en esos casos no existen más jugadas posibles a partir de
     * dicho estado (el nodo queda como hoja de forma natural).</p>
     *
     * @param arbol       árbol en el que se guardará la raíz generada
     * @param profundidad número de turnos a generar (2 en el uso normal del juego)
     */
    public void generarArbolDeJuego(ArbolNario arbol, int profundidad) {
        NodoJugada raiz = new NodoJugada(tableroActual.clonar(), -1, -1);
        arbol.setRaiz(raiz);
        generarHijos(raiz, profundidad, fichaPC);
    }

    /**
     * Genera recursivamente los hijos de {@code nodoPadre}: uno por cada casilla
     * disponible en su tablero, colocando en cada uno la ficha del jugador que
     * tiene el turno ({@code fichaTurno}). En cada nivel se alterna entre
     * {@link #fichaPC} y {@link #fichaHumano}.
     */
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

    /**
     * Aplica el algoritmo minimax sobre el árbol generado por
     * {@link #generarArbolDeJuego(ArbolNario, int)}, guardando en cada nodo
     * (vía {@link NodoJugada#setUtilidad(int)}) la utilidad calculada.
     *
     * <p>En un nodo hoja (o cuando se agota la profundidad, o el tablero del
     * nodo ya tiene un ganador) la utilidad se calcula directamente con la
     * fórmula del enunciado: u(t) = P_PC(t) − P_Humano(t). En los demás nodos,
     * la utilidad es el máximo de las utilidades de sus hijos si le
     * corresponde el turno a la PC ({@code esTurnoPC == true}, nodo MAX), o el
     * mínimo si le corresponde al humano (nodo MIN) — "la PC elige el mejor
     * movimiento para sí misma, asumiendo que el humano escogerá el peor
     * para ella".</p>
     *
     * @param nodoActual  nodo del árbol que se está evaluando
     * @param profundidad turnos restantes por analizar desde este nodo
     * @param esTurnoPC   true si, desde este nodo, le toca jugar a la PC
     *                    (nodo MAX); false si le toca al humano (nodo MIN)
     * @return la utilidad calculada para {@code nodoActual}
     */
    public int minimax(NodoJugada nodoActual, int profundidad, boolean esTurnoPC) {
        Tablero tableroNodo = nodoActual.getEstado();

        boolean esEstadoTerminal = nodoActual.esHoja()
                || profundidad <= 0
                || tableroNodo.verificarGanador(Ficha.X)
                || tableroNodo.verificarGanador(Ficha.O);

        if (esEstadoTerminal) {
            int utilidad = calcularUtilidad(tableroNodo);
            nodoActual.setUtilidad(utilidad);
            return utilidad;
        }

        int mejorUtilidad = esTurnoPC ? Integer.MIN_VALUE : Integer.MAX_VALUE;

        for (NodoJugada hijo : nodoActual.getHijos()) {
            int utilidadHijo = minimax(hijo, profundidad - 1, !esTurnoPC);
            if (esTurnoPC) {
                mejorUtilidad = Math.max(mejorUtilidad, utilidadHijo);
            } else {
                mejorUtilidad = Math.min(mejorUtilidad, utilidadHijo);
            }
        }

        nodoActual.setUtilidad(mejorUtilidad);
        return mejorUtilidad;
    }

    /**
     * Genera el árbol de jugadas a partir de {@link #tableroActual}, aplica
     * minimax sobre él y retorna la coordenada {fila, columna} de la jugada
     * de la PC con mayor utilidad garantizada.
     *
     * <p>Como efecto secundario, guarda el árbol generado en
     * {@link #ultimoArbolGenerado} para que pueda consultarse después
     * mediante {@link #getUltimoArbolGenerado()} (usado, por ejemplo, para
     * mostrar el árbol de decisión en pantalla).</p>
     *
     * @return {fila, columna} de la mejor jugada, o {-1, -1} si no hay
     *         casillas disponibles (tablero lleno o juego ya terminado)
     */
    public int[] obtenerMejorJugada() {
        ArbolNario arbol = new ArbolNario();
        generarArbolDeJuego(arbol, PROFUNDIDAD_ANALISIS);
        this.ultimoArbolGenerado = arbol; // ← única línea nueva dentro de este método

        NodoJugada raiz = arbol.getRaiz();
        minimax(raiz, PROFUNDIDAD_ANALISIS, true);

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

    /**
     * Calcula u(t) = P_PC(t) − P_Humano(t), donde P_j(t) es el número de
     * filas, columnas y diagonales del tablero {@code t} aún disponibles
     * para el jugador j (ver sección "Utilidad de un Tablero" del enunciado).
     */
    private int calcularUtilidad(Tablero tablero) {
        return tablero.contarLineasDisponibles(fichaPC) - tablero.contarLineasDisponibles(fichaHumano);
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

    public ArbolNario getUltimoArbolGenerado() {
        return ultimoArbolGenerado;
    }
}
