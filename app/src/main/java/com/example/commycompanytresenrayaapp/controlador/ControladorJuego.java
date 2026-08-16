/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.commycompanytresenrayaapp.controlador;

import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;
import java.util.List;

import com.example.commycompanytresenrayaapp.modelo.Ficha;
import com.example.commycompanytresenrayaapp.modelo.Jugador;
import com.example.commycompanytresenrayaapp.modelo.Minimax;
import com.example.commycompanytresenrayaapp.modelo.Tablero;
import com.example.commycompanytresenrayaapp.modelo.TipoJugador;

public class ControladorJuego {

    private static final int RETARDO_MAQUINA_DEFECTO_MS = 600;


    private Tablero tablero;
    private Minimax algoritmo;
    private Jugador jugadorX;
    private Jugador jugadorO;
    private Ficha fichaTurnoActual;
    private boolean juegoTerminado;
    private int retardoMaquinaMs;
    private final List<ObservadorJuego> observadores;
    private final Handler handler;

    public ControladorJuego(Tablero tablero, Minimax algoritmo, Jugador jugadorX, Jugador jugadorO) {
        this.tablero = tablero;
        this.algoritmo = algoritmo;
        this.jugadorX = jugadorX;
        this.jugadorO = jugadorO;
        this.observadores = new ArrayList<>();
        this.juegoTerminado = false;
        this.retardoMaquinaMs = RETARDO_MAQUINA_DEFECTO_MS;
        this.handler = new Handler(Looper.getMainLooper());   
    }

    public void agregarObservador(ObservadorJuego obs) {
        observadores.add(obs);
    }

    /**
     * Arranca la partida con la ficha indicada empezando. Si a quien le
     * toca es la máquina, le pide su jugada automáticamente (con
     * retardo). Si le toca a un humano, el controlador queda a la
     * espera de que la vista invoque jugarTurnoHumano().
     */

    public void iniciarJuego(Ficha fichaInicial) {
        juegoTerminado = false;
        fichaTurnoActual = fichaInicial;
        procesarTurnoSiEsMaquina();
    }

    /**
     * Cancela cualquier jugada de máquina pendiente. Debe llamarse al
     * destruir la pantalla actual (nueva partida, cierre de la
     * Activity, etc.) para que un Handler.postDelayed() no dispare
     * sobre una vista que ya no está en pantalla.
     */

    public void detenerJuego() {
        juegoTerminado = true;
        handler.removeCallbacksAndMessages(null);
    }

    /**
     * Procesa el turno de un humano sobre la casilla (fila, columna).
     * Se ignora silenciosamente si no es turno de un humano, si el
     * juego terminó, o si la casilla ya está ocupada: son eventos de
     * UI esperados, no errores de programación.
     */

    public void jugarTurnoHumano(int fila, int columna) {
        if (juegoTerminado) {
            return;
        }
        Jugador jugadorEnTurno = obtenerJugador(fichaTurnoActual);
        if (jugadorEnTurno.getTipo() != TipoJugador.HUMANO) {
            return;
        }
        if (tablero.getCasilla(fila, columna) != Ficha.VACIA) {
            return;
        }
        aplicarJugada(fila, columna, fichaTurnoActual);
    }

    /**
     * Sugerencia de jugada para el humano al que le toca el turno
     * actual. Devuelve null si no le toca a un humano o si el juego
     * terminó.
     */

    public int[] obtenerSugerenciaParaHumano() {
        if (juegoTerminado) {
            return null;
        }
        Jugador jugadorEnTurno = obtenerJugador(fichaTurnoActual);
        if (jugadorEnTurno.getTipo() != TipoJugador.HUMANO) {
            return null;
        }
        int[] sugerencia = algoritmo.obtenerMejorJugadaPara(fichaTurnoActual);
        if (sugerencia[0] == -1) {
            return null;
        }
        return sugerencia;
    }

    /**
     * Si a la ficha en turno le corresponde una máquina, programa su
     * jugada con el retardo configurado. Se llama tanto al iniciar la
     * partida como después de cada jugada aplicada, así que en modo
     * Computadora vs Computadora esta cadena de llamadas hace que la
     * partida se juegue sola de principio a fin.
     */
    private void procesarTurnoSiEsMaquina() {
        if (juegoTerminado) {
            return;
        }
        Jugador jugadorEnTurno = obtenerJugador(fichaTurnoActual);
        if (jugadorEnTurno.getTipo() == TipoJugador.MAQUINA) {
            handler.postDelayed(this::realizarJugadaMaquina, retardoMaquinaMs);
        }
    }
    
    private void realizarJugadaMaquina() {
        if (juegoTerminado) {
            return;
        }
        int[] jugada = algoritmo.obtenerMejorJugadaPara(fichaTurnoActual);
        if (jugada[0] == -1) {
            return; // Sin casillas disponibles; no debería ocurrir si el juego sigue activo.
        }
        aplicarJugada(jugada[0], jugada[1], fichaTurnoActual);
    }

    /**
     * Coloca la ficha, notifica a la vista, revisa fin de juego y, si
     * la partida sigue, cede el turno y dispara la jugada de la
     * máquina si corresponde.
     */
    private void aplicarJugada(int fila, int columna, Ficha ficha) {
        tablero.llenarCasilla(fila, columna, ficha);
        notificarJugada(fila, columna, ficha);

        if (verificarYNotificarFinDeJuego(ficha)) {
            return;
        }

        fichaTurnoActual = obtenerFichaContraria(fichaTurnoActual);
        procesarTurnoSiEsMaquina();
    }
    

    private boolean verificarYNotificarFinDeJuego(Ficha ultimaFicha) {
        if (tablero.verificarGanador(ultimaFicha)) {
            juegoTerminado = true;
            Jugador ganador = obtenerJugador(ultimaFicha);
            notificarFinDeJuego(ganador.getNombre() + " ha ganado la partida.");
            return true;
        }
        if (tablero.obtenerCasillasDisponibles().isEmpty()) {
            juegoTerminado = true;
            notificarFinDeJuego("Empate. Nadie logró completar una línea.");
            return true;
        }
        return false;
    }

    private Jugador obtenerJugador(Ficha ficha) {
        return (ficha == jugadorX.getFicha()) ? jugadorX : jugadorO;
    }

    private Ficha obtenerFichaContraria(Ficha ficha) {
        return (ficha == Ficha.X) ? Ficha.O : Ficha.X;
    }

    private void notificarJugada(int fila, int columna, Ficha ficha) {
        for (ObservadorJuego obs : observadores) {
            obs.onJugadaRealizada(fila, columna, ficha);
        }
    }

    private void notificarFinDeJuego(String mensaje) {
        for (ObservadorJuego obs : observadores) {
            obs.onJuegoTerminado(mensaje);
        }
    }

    // Getters y Setters 

    public Tablero getTablero() {
        return tablero;
    }

    public void setTablero(Tablero tablero) {
        this.tablero = tablero;
    }

    public Minimax getAlgoritmo() {
        return algoritmo;
    }

    public void setAlgoritmo(Minimax algoritmo) {
        this.algoritmo = algoritmo;
    }

    public Jugador getJugadorX() {
        return jugadorX;
    }

    public void setJugadorX(Jugador jugadorX) {
        this.jugadorX = jugadorX;
    }

    public Jugador getJugadorO() {
        return jugadorO;
    }

    public void setJugadorO(Jugador jugadorO) {
        this.jugadorO = jugadorO;
    }

    public Ficha getFichaTurnoActual() {
        return fichaTurnoActual;
    }

    public boolean isJuegoTerminado() {
        return juegoTerminado;
    }

    public int getRetardoMaquinaMs() {
        return retardoMaquinaMs;
    }

    public void setRetardoMaquinaMs(int retardoMaquinaMs) {
        this.retardoMaquinaMs = retardoMaquinaMs;
    }

    public List<ObservadorJuego> getObservadores() {
        return observadores;
    }
}
