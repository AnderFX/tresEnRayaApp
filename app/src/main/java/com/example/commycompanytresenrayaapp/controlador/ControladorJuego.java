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
    private ModoJuego modoJuego = ModoJuego.CONTRA_PC;
    private Ficha fichaEnTurno;
    private List<ObservadorJuego> observadores;
    private boolean juegoTerminado;

    public ControladorJuego(Tablero tablero, Minimax algoritmo) {
        this.tablero = tablero;
        this.algoritmo = algoritmo;
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
     * onCambioDeTurno() invocar realizarJugadaAutomatica().
     *
     * <p>Clics fuera de turno o sobre una casilla ya ocupada se ignoran
     * silenciosamente, en vez de lanzar una excepcion, porque son un
     * evento de UI esperado (el usuario puede tocar el tablero en
     * cualquier momento) y no un error de programacion.</p>
     */
    public void jugarTurno(int fila, int columna) {
        if (juegoTerminado || !esTurnoDeUnHumano()) {
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
     * Le pide a Minimax la mejor jugada para quien tenga el turno actual
     * (fichaEnTurno), la aplica y notifica el resultado. Minimax comparte
     * la misma instancia de Tablero que este controlador (ver
     * MainActivity), asi que siempre analiza el estado mas reciente sin
     * necesidad de sincronizarlo manualmente.
     *
     * <p>Publico porque no se autoinvoca: quien reciba onCambioDeTurno()
     * y decida que ese turno es automatico (la PC contra un humano, o
     * cualquiera de los dos turnos en PC_VS_PC) es quien decide cuando
     * llamarlo (por ejemplo, tras una demora para simular que la PC
     * "piensa").</p>
     */
    public void realizarJugadaAutomatica() {
        Ficha fichaQueJuega = fichaEnTurno;
        int[] jugada = algoritmo.obtenerMejorJugadaPara(fichaQueJuega);
        if (jugada[0] == -1) {
            return; // Sin casillas disponibles; no deberia ocurrir si el juego sigue activo.
        }

        tablero.llenarCasilla(jugada[0], jugada[1], fichaQueJuega);
        notificarJugada(jugada[0], jugada[1], fichaQueJuega);

        if (verificarYNotificarFinDeJuego(fichaQueJuega)) {
            return;
        }

        fichaEnTurno = otraFicha(fichaQueJuega);
        notificarCambioDeTurno();
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
            notificarFinDeJuego(mensajeDeVictoria(ultimaFicha));
            return true;
        }
        if (tablero.obtenerCasillasDisponibles().isEmpty()) {
            juegoTerminado = true;
            notificarFinDeJuego("Empate. Nadie logro completar una linea.");
            return true;
        }
        return false;
    }

    private String mensajeDeVictoria(Ficha ganador) {
        if (modoJuego == ModoJuego.PC_VS_PC) {
            return "La computadora que jugaba con " + ganador.name() + " ha ganado esta partida.";
        }
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

    private boolean esTurnoDeUnHumano() {
        if (modoJuego == ModoJuego.PC_VS_PC) {
            return false;
        }
        if (modoJuego == ModoJuego.CONTRA_PC) {
            return fichaEnTurno == fichaHumano;
        }
        return true;
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

    public List<ObservadorJuego> getObservadores() {
        return observadores;
    }

    public void setObservadores(List<ObservadorJuego> observadores) {
        this.observadores = observadores;
    }
}
