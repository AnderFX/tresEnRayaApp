package com.example.commycompanytresenrayaapp;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.example.commycompanytresenrayaapp.modelo.ArbolNario;
import com.example.commycompanytresenrayaapp.modelo.Ficha;
import com.example.commycompanytresenrayaapp.modelo.Minimax;
import com.example.commycompanytresenrayaapp.modelo.NodoJugada;
import com.example.commycompanytresenrayaapp.modelo.Tablero;

/**
 * Prueba unitaria local para Minimax (no usa ninguna clase de Android,
 * igual que TableroTest). Todos los tableros de partida ya tienen 2 o 3
 * casillas llenas: no se prueba desde un tablero vacío.
 */
public class MinimaxTest {

    // ---------------------------------------------------------------
    // generarArbolDeJuego(): primer nivel (jugadas de la PC)
    // ---------------------------------------------------------------

    @Test
    public void generarArbolDeJuegoCreaUnHijoPorCadaCasillaDisponible() {
        // Tablero con 2 casillas llenas: X en (0,0), O en (1,1). Quedan 7 libres.
        Tablero tablero = new Tablero();
        tablero.llenarCasilla(0, 0, Ficha.X);
        tablero.llenarCasilla(1, 1, Ficha.O);

        Minimax minimax = new Minimax(tablero, Ficha.X, Ficha.O);
        ArbolNario arbol = new ArbolNario();
        minimax.generarArbolDeJuego(arbol, 2);

        NodoJugada raiz = arbol.getRaiz();
        assertEquals(7, raiz.getHijos().size());

        // Cada hijo debe conservar las 2 fichas ya colocadas y agregar
        // una X (turno de la PC) en su propia casilla jugada.
        for (NodoJugada hijo : raiz.getHijos()) {
            Tablero tableroHijo = hijo.getEstado();
            assertEquals(Ficha.X, tableroHijo.getCasilla(0, 0));
            assertEquals(Ficha.O, tableroHijo.getCasilla(1, 1));
            assertEquals(Ficha.X, tableroHijo.getCasilla(hijo.getFilaJugada(), hijo.getColumnaJugada()));
        }
    }

    // ---------------------------------------------------------------
    // generarArbolDeJuego(): segundo nivel usa la ficha del humano
    // ---------------------------------------------------------------

    @Test
    public void generarArbolDeJuegoSegundoNivelUsaLaFichaDelHumano() {
        Tablero tablero = new Tablero();
        tablero.llenarCasilla(0, 0, Ficha.X);
        tablero.llenarCasilla(1, 1, Ficha.O);

        Minimax minimax = new Minimax(tablero, Ficha.X, Ficha.O);
        ArbolNario arbol = new ArbolNario();
        minimax.generarArbolDeJuego(arbol, 2);

        NodoJugada primerHijo = arbol.getRaiz().getHijos().get(0);
        // Al hijo le quedaban 6 casillas libres para la respuesta del humano.
        assertEquals(6, primerHijo.getHijos().size());

        for (NodoJugada nieto : primerHijo.getHijos()) {
            assertEquals(Ficha.O, nieto.getEstado().getCasilla(nieto.getFilaJugada(), nieto.getColumnaJugada()));
        }
    }

    // ---------------------------------------------------------------
    // generarArbolDeJuego(): no expande una jugada que ya ganó el juego
    // ---------------------------------------------------------------

    @Test
    public void generarArbolDeJuegoNoExpandeUnaJugadaQueYaGanaElJuego() {
        // X en (0,0) y (0,1); O en (1,1). Si la PC juega en (0,2), gana.
        Tablero tablero = new Tablero();
        tablero.llenarCasilla(0, 0, Ficha.X);
        tablero.llenarCasilla(0, 1, Ficha.X);
        tablero.llenarCasilla(1, 1, Ficha.O);

        Minimax minimax = new Minimax(tablero, Ficha.X, Ficha.O);
        ArbolNario arbol = new ArbolNario();
        minimax.generarArbolDeJuego(arbol, 2);

        NodoJugada jugadaGanadora = buscarHijo(arbol.getRaiz(), 0, 2);
        assertNotNull(jugadaGanadora);
        assertTrue(jugadaGanadora.getEstado().verificarGanador(Ficha.X));
        assertTrue("Un tablero ya ganado no debería tener más hijos", jugadaGanadora.esHoja());
    }

    // ---------------------------------------------------------------
    // minimax(): alterna correctamente entre máximo y mínimo
    // ---------------------------------------------------------------

    @Test
    public void minimaxAlternaMaximoYMinimoEnUnArbolConocido() {
        // Árbol construido a mano (independiente de generarArbolDeJuego) para
        // verificar la recursión con valores calculados previamente a partir
        // de tableros reales (u = P_X - P_O):
        //
        //                          raiz (MAX, turno PC = X)
        //                    /                              \
        //         p1: jugada en (0,0) (MIN)         p2: jugada en (1,1) (MIN)
        //             /              \                   /              \
        //     b1: O en (0,0)   b2: O en (1,1)     b3: X en (0,0)   b4: X en (1,1)
        //        u = -3           u = -4              u = 3            u = 4
        //
        // p1 = min(-3, -4) = -4        p2 = min(3, 4) = 3
        // raiz = max(-4, 3) = 3   →  la PC debería preferir la rama p2.
        Minimax minimax = new Minimax(new Tablero(), Ficha.X, Ficha.O);

        NodoJugada raiz = new NodoJugada(new Tablero(), -1, -1);

        NodoJugada p1 = new NodoJugada(new Tablero(), 0, 0);
        raiz.agregarHijo(p1);

        Tablero tableroB1 = new Tablero();
        tableroB1.llenarCasilla(0, 0, Ficha.O);
        NodoJugada b1 = new NodoJugada(tableroB1, 0, 0);
        p1.agregarHijo(b1);

        Tablero tableroB2 = new Tablero();
        tableroB2.llenarCasilla(1, 1, Ficha.O);
        NodoJugada b2 = new NodoJugada(tableroB2, 1, 1);
        p1.agregarHijo(b2);

        NodoJugada p2 = new NodoJugada(new Tablero(), 1, 1);
        raiz.agregarHijo(p2);

        Tablero tableroB3 = new Tablero();
        tableroB3.llenarCasilla(0, 0, Ficha.X);
        NodoJugada b3 = new NodoJugada(tableroB3, 0, 0);
        p2.agregarHijo(b3);

        Tablero tableroB4 = new Tablero();
        tableroB4.llenarCasilla(1, 1, Ficha.X);
        NodoJugada b4 = new NodoJugada(tableroB4, 1, 1);
        p2.agregarHijo(b4);

        int resultado = minimax.minimax(raiz, 2, true);

        assertEquals(-3, b1.getUtilidad());
        assertEquals(-4, b2.getUtilidad());
        assertEquals(3, b3.getUtilidad());
        assertEquals(4, b4.getUtilidad());

        assertEquals(-4, p1.getUtilidad()); // mínimo de la familia de O
        assertEquals(3, p2.getUtilidad());  // mínimo de la familia de O

        assertEquals(3, raiz.getUtilidad()); // máximo entre p1 y p2
        assertEquals(3, resultado);
    }

    // ---------------------------------------------------------------
    // minimax() sobre el árbol real: utilidad de la jugada ganadora
    // ---------------------------------------------------------------

    @Test
    public void minimaxAsignaLaUtilidadCorrectaALaJugadaGanadora() {
        Tablero tablero = new Tablero();
        tablero.llenarCasilla(0, 0, Ficha.X);
        tablero.llenarCasilla(0, 1, Ficha.X);
        tablero.llenarCasilla(1, 1, Ficha.O);

        Minimax minimax = new Minimax(tablero, Ficha.X, Ficha.O);
        ArbolNario arbol = new ArbolNario();
        minimax.generarArbolDeJuego(arbol, 2);
        int resultado = minimax.minimax(arbol.getRaiz(), 2, true);

        // Tablero resultante al jugar (0,2): fila 0 completa de X, O en (1,1).
        // Líneas disponibles para X: fila0, fila2, col0, col2 = 4.
        // Líneas disponibles para O: fila1, fila2 = 2.
        // u = 4 - 2 = 2
        NodoJugada jugadaGanadora = buscarHijo(arbol.getRaiz(), 0, 2);
        assertEquals(2, jugadaGanadora.getUtilidad());
        assertEquals(2, resultado);
        assertEquals(2, arbol.getRaiz().getUtilidad());
    }

    // ---------------------------------------------------------------
    // obtenerMejorJugada(): elige la jugada que gana de inmediato
    // ---------------------------------------------------------------

    @Test
    public void obtenerMejorJugadaEligeLaJugadaGanadoraInmediata() {
        // X en (0,0) y (0,1); O en (1,1); le toca jugar a X (la PC).
        // Jugar en (0,2) completa la fila 0 y gana de inmediato.
        Tablero tablero = new Tablero();
        tablero.llenarCasilla(0, 0, Ficha.X);
        tablero.llenarCasilla(0, 1, Ficha.X);
        tablero.llenarCasilla(1, 1, Ficha.O);

        Minimax minimax = new Minimax(tablero, Ficha.X, Ficha.O);
        int[] mejorJugada = minimax.obtenerMejorJugada();

        assertEquals(0, mejorJugada[0]);
        assertEquals(2, mejorJugada[1]);
    }

    // ---------------------------------------------------------------
    // obtenerMejorJugada(): no hay jugadas si el tablero ya tiene ganador
    // ---------------------------------------------------------------

    @Test
    public void obtenerMejorJugadaRetornaCentinelaSiYaHayGanador() {
        Tablero tablero = new Tablero();
        tablero.llenarCasilla(0, 0, Ficha.X);
        tablero.llenarCasilla(0, 1, Ficha.X);
        tablero.llenarCasilla(0, 2, Ficha.X);
        tablero.llenarCasilla(1, 0, Ficha.O);
        tablero.llenarCasilla(1, 1, Ficha.O);

        Minimax minimax = new Minimax(tablero, Ficha.X, Ficha.O);
        int[] mejorJugada = minimax.obtenerMejorJugada();

        assertEquals(-1, mejorJugada[0]);
        assertEquals(-1, mejorJugada[1]);
    }

    // ---------------------------------------------------------------
    // Utilidad interna para las pruebas
    // ---------------------------------------------------------------

    private NodoJugada buscarHijo(NodoJugada padre, int fila, int columna) {
        for (NodoJugada hijo : padre.getHijos()) {
            if (hijo.getFilaJugada() == fila && hijo.getColumnaJugada() == columna) {
                return hijo;
            }
        }
        return null;
    }
}