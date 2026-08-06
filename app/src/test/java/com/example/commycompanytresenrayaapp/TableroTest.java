package com.example.commycompanytresenrayaapp;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import com.example.commycompanytresenrayaapp.modelo.Ficha;
import com.example.commycompanytresenrayaapp.modelo.Tablero;

/**
 * Prueba unitaria local.
 * Tablero no usa ninguna clase de Android.
 */
public class TableroTest {

    private Tablero tablero;

    @Before
    public void crearTableroVacio() {
        tablero = new Tablero();
    }

    @Test
    public void tableroNuevoTieneLas9CasillasVacias() {
        assertEquals(9, tablero.obtenerCasillasDisponibles().size());
    }

    @Test
    public void llenarCasillaColocaLaFichaIndicada() {
        tablero.llenarCasilla(0, 0, Ficha.X);
        assertEquals(Ficha.X, tablero.getCasilla(0, 0));
    }

    @Test
    public void llenarCasillaOcupadaLanzaExcepcion() {
        tablero.llenarCasilla(0, 0, Ficha.X);

        assertThrows(IllegalStateException.class,
                () -> tablero.llenarCasilla(0, 0, Ficha.O));
    }

    @Test
    public void llenarCasillaFueraDeRangoLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> tablero.llenarCasilla(3, 0, Ficha.X));
    }

    @Test
    public void deshacerCasillaLaDejaVacia() {
        tablero.llenarCasilla(1, 1, Ficha.O);
        tablero.deshacerCasilla(1, 1);

        assertEquals(Ficha.VACIA, tablero.getCasilla(1, 1));
    }

    @Test
    public void obtenerCasillasDisponiblesExcluyeLasOcupadas() {
        tablero.llenarCasilla(0, 0, Ficha.X);
        List<int[]> disponibles = tablero.obtenerCasillasDisponibles();

        assertEquals(8, disponibles.size());
        assertFalse(disponibles.stream().anyMatch(c -> c[0] == 0 && c[1] == 0));
    }

    @Test
    public void verificarGanadorDetectaFilaCompleta() {
        tablero.llenarCasilla(0, 0, Ficha.X);
        tablero.llenarCasilla(0, 1, Ficha.X);
        tablero.llenarCasilla(0, 2, Ficha.X);

        assertTrue(tablero.verificarGanador(Ficha.X));
        assertFalse(tablero.verificarGanador(Ficha.O));
    }

    @Test
    public void verificarGanadorDetectaDiagonal() {
        tablero.llenarCasilla(0, 0, Ficha.O);
        tablero.llenarCasilla(1, 1, Ficha.O);
        tablero.llenarCasilla(2, 2, Ficha.O);

        assertTrue(tablero.verificarGanador(Ficha.O));
    }

    @Test
    public void contarLineasDisponiblesReplicaElEjemploDelEnunciado() {
        tablero.llenarCasilla(0, 0, Ficha.X);
        tablero.llenarCasilla(1, 0, Ficha.O);

        assertEquals(6, tablero.contarLineasDisponibles(Ficha.X));
        assertEquals(5, tablero.contarLineasDisponibles(Ficha.O));
    }

    @Test
    public void clonarProduceUnaCopiaIndependiente() {
        tablero.llenarCasilla(0, 0, Ficha.X);
        Tablero copia = tablero.clonar();

        assertNotSame(tablero, copia);
        assertEquals(Ficha.X, copia.getCasilla(0, 0));

        copia.llenarCasilla(1, 1, Ficha.O);
        assertEquals(
                "Modificar la copia no debería afectar al tablero original",
                Ficha.VACIA, tablero.getCasilla(1, 1));
    }
}
