/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.commycompanytresenrayaapp.controlador;

import java.util.ArrayList;
import java.util.List;

import com.example.commycompanytresenrayaapp.modelo.Ficha;
import com.example.commycompanytresenrayaapp.modelo.Minimax;
import com.example.commycompanytresenrayaapp.modelo.Tablero;

public class ControladorJuego {

    private Tablero tablero;
    private Minimax algoritmo;
    private Ficha fichaHumano;
    private Ficha fichaPC;
    private boolean turnoHumano;
    private List<ObservadorJuego> observadores;
    private boolean juegoTerminado;

    public ControladorJuego(Tablero tablero, Minimax algoritmo) {
        this.tablero = tablero;
        this.algoritmo = algoritmo;
        this.observadores = new ArrayList<>();
        this.juegoTerminado = false;
    }

    /**
     * Arranca la partida. Si el primer turno le corresponde a la PC
     * (turnoHumano == false), le pide de inmediato su jugada a Minimax.
     * Si el turno inicial es del humano, no hace nada más: el controlador
     * queda a la espera de que la vista invoque jugarTurnoHumano() cuando
     * el usuario toque una casilla.
     *
     * <p>fichaHumano, fichaPC y turnoHumano deben configurarse (via sus
     * setters) antes de llamar a este metodo.</p>
     */
    public void iniciarJuego() {
        juegoTerminado = false;
        if (!turnoHumano) {
            realizarJugadaPC();
        }
    }

    public void agregarObservador(ObservadorJuego obs) {
        observadores.add(obs);
    }

    /**
     * Procesa el turno del humano sobre la casilla (fila, columna). Si la
     * jugada es valida y no termina el juego, cede el turno a la PC y le
     * pide su jugada a Minimax de inmediato: al retornar de este metodo,
     * el tablero ya refleja tanto la jugada del humano como, si
     * corresponde, la respuesta de la computadora.
     *
     * <p>Clics fuera de turno o sobre una casilla ya ocupada se ignoran
     * silenciosamente, en vez de lanzar una excepcion, porque son un
     * evento de UI esperado (el usuario puede tocar el tablero en
     * cualquier momento) y no un error de programacion.</p>
     */
    public void jugarTurnoHumano(int fila, int columna) {
        if (juegoTerminado || !turnoHumano) {
            return;
        }
        if (tablero.getCasilla(fila, columna) != Ficha.VACIA) {
            return;
        }

        tablero.llenarCasilla(fila, columna, fichaHumano);
        notificarJugada(fila, columna, fichaHumano);

        if (verificarYNotificarFinDeJuego(fichaHumano)) {
            return;
        }

        turnoHumano = false;
        realizarJugadaPC();
    }

    public int[] obtenerSugerenciaParaHumano() {
        if (juegoTerminado || !turnoHumano) {
            return null;
        }

        int[] sugerencia = algoritmo.obtenerMejorJugadaPara(fichaHumano);
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
     */
    private void realizarJugadaPC() {
        int[] jugada = algoritmo.obtenerMejorJugada();
        if (jugada[0] == -1) {
            return; // Sin casillas disponibles; no deberia ocurrir si el juego sigue activo.
        }

        tablero.llenarCasilla(jugada[0], jugada[1], fichaPC);
        notificarJugada(jugada[0], jugada[1], fichaPC);

        if (verificarYNotificarFinDeJuego(fichaPC)) {
            return;
        }

        turnoHumano = true;
    }

    /**
     * Revisa si, tras colocar {@code ultimaFicha}, el juego termino (por
     * victoria de esa ficha o por empate) y, de ser asi, notifica a los
     * observadores con el mensaje correspondiente.
     *
     * @return true si el juego termino (victoria o empate)
     */
    private boolean verificarYNotificarFinDeJuego(Ficha ultimaFicha) {
        if (tablero.verificarGanador(ultimaFicha)) {
            juegoTerminado = true;
            String mensaje = (ultimaFicha == fichaHumano)
                    ? "¡Ganaste! Felicidades."
                    : "La computadora ha ganado esta partida.";
            notificarFinDeJuego(mensaje);
            return true;
        }
        if (tablero.obtenerCasillasDisponibles().isEmpty()) {
            juegoTerminado = true;
            notificarFinDeJuego("Empate. Nadie logro completar una linea.");
            return true;
        }
        return false;
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

    public Ficha getFichaHumano() {
        return fichaHumano;
    }

    public void setFichaHumano(Ficha fichaHumano) {
        this.fichaHumano = fichaHumano;
    }

    public Ficha getFichaPC() {
        return fichaPC;
    }

    public void setFichaPC(Ficha fichaPC) {
        this.fichaPC = fichaPC;
    }

    public boolean isTurnoHumano() {
        return turnoHumano;
    }

    public void setTurnoHumano(boolean turnoHumano) {
        this.turnoHumano = turnoHumano;
    }

    public boolean isJuegoTerminado() {
        return juegoTerminado;
    }

    public List<ObservadorJuego> getObservadores() {
        return observadores;
    }

    public void setObservadores(List<ObservadorJuego> observadores) {
        this.observadores = observadores;
    }
}
