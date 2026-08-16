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
    private Ficha fichaHumano;
    private Ficha fichaPC;
    private ModoJuego modoJuego = ModoJuego.CONTRA_PC;
    private Ficha fichaEnTurno;
    private List<ObservadorJuego> observadores;
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
    }

    /**
     * Arranca la partida notificando el turno inicial. Quien escuche
     * onCambioDeTurno() decide que hacer con ese turno: si le corresponde
     * a la PC, es responsabilidad de ese observador invocar
     * realizarJugadaPC() (posiblemente con una demora o animacion antes);
     * el controlador ya no lo hace por su cuenta.
     *
     * <p>fichaHumano, fichaPC, modoJuego y fichaEnTurno deben configurarse
     * (via sus setters) antes de llamar a este metodo.</p>
     */
    public void iniciarJuego() {
        juegoTerminado = false;
        notificarCambioDeTurno();
    }

    public void agregarObservador(ObservadorJuego obs) {
        observadores.add(obs);
    }

    /**
     * Procesa el turno de quien tenga la ficha actual (fichaEnTurno) sobre
     * la casilla (fila, columna): sirve tanto para el humano en modo
     * CONTRA_PC como para cualquiera de los dos jugadores en modo
     * DOS_HUMANOS. Si la jugada es valida y no termina el juego, pasa el
     * turno a la otra ficha y notifica ese cambio: si el nuevo turno le
     * corresponde a la PC, es responsabilidad de quien escuche
     * onCambioDeTurno() invocar realizarJugadaPC().
     *
     * <p>Clics fuera de turno o sobre una casilla ya ocupada se ignoran
     * silenciosamente, en vez de lanzar una excepcion, porque son un
     * evento de UI esperado (el usuario puede tocar el tablero en
     * cualquier momento) y no un error de programacion.</p>
     */
    public void jugarTurno(int fila, int columna) {
        if (juegoTerminado) {
            return;
        }
        if (modoJuego == ModoJuego.CONTRA_PC && fichaEnTurno != fichaHumano) {
            return;
        }
        if (tablero.getCasilla(fila, columna) != Ficha.VACIA) {
            return;
        }

        Ficha fichaQueJugo = fichaEnTurno;
        tablero.llenarCasilla(fila, columna, fichaQueJugo);
        notificarJugada(fila, columna, fichaQueJugo);

        if (verificarYNotificarFinDeJuego(fichaQueJugo)) {
            return;
        }

        fichaEnTurno = otraFicha(fichaQueJugo);
        notificarCambioDeTurno();
    }

    public int[] obtenerSugerenciaParaTurnoActual() {
        if (juegoTerminado) {
            return null;
        }
        if (modoJuego == ModoJuego.CONTRA_PC && fichaEnTurno != fichaHumano) {
            return null;
        }

        int[] sugerencia = algoritmo.obtenerMejorJugadaPara(fichaEnTurno);
        if (sugerencia[0] == -1) {
            return null;
        }
        return sugerencia;
    }

    /**
     * Le pide a Minimax la mejor jugada para la PC sobre el tablero
     * actual, la aplica y notifica el resultado. Minimax comparte la
     * misma instancia de Tablero que este controlador (ver MainActivity),
     * asi que siempre analiza el estado mas reciente sin necesidad de
     * sincronizarlo manualmente.
     *
     * <p>Publico porque ya no se autoinvoca: quien reciba
     * onCambioDeTurno() con fichaPC es quien decide cuando llamarlo (por
     * ejemplo, tras una demora para simular que la PC "piensa").</p>
     */
    public void realizarJugadaPC() {
        int[] jugada = algoritmo.obtenerMejorJugada();
        if (jugada[0] == -1) {
            return; // Sin casillas disponibles; no deberia ocurrir si el juego sigue activo.
        }

        tablero.llenarCasilla(jugada[0], jugada[1], fichaPC);
        notificarJugada(jugada[0], jugada[1], fichaPC);

        if (verificarYNotificarFinDeJuego(fichaPC)) {
            return;
        }

        fichaEnTurno = fichaHumano;
        notificarCambioDeTurno();
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
            notificarFinDeJuego(mensajeDeVictoria(ultimaFicha));
            return true;
        }
        if (tablero.obtenerCasillasDisponibles().isEmpty()) {
            juegoTerminado = true;
            notificarFinDeJuego("Empate. Nadie logró completar una línea.");
            return true;
        }
        return false;
    }

    private String mensajeDeVictoria(Ficha ganador) {
        if (modoJuego == ModoJuego.DOS_HUMANOS) {
            return "¡Gano el jugador con ficha " + ganador.name() + "!";
        }
        return (ganador == fichaHumano)
                ? "¡Ganaste! Felicidades."
                : "La computadora ha ganado esta partida.";
    }

    private Ficha otraFicha(Ficha ficha) {
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

    private void notificarCambioDeTurno() {
        for (ObservadorJuego obs : observadores) {
            obs.onCambioDeTurno(fichaEnTurno);
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

    public ModoJuego getModoJuego() {
        return modoJuego;
    }

    public void setModoJuego(ModoJuego modoJuego) {
        this.modoJuego = modoJuego;
    }

    public Ficha getFichaEnTurno() {
        return fichaEnTurno;
    }

    public void setFichaEnTurno(Ficha fichaEnTurno) {
        this.fichaEnTurno = fichaEnTurno;
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
