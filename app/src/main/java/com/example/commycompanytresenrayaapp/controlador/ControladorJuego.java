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

    public ControladorJuego(Tablero tablero, Minimax algoritmo) {
        this.tablero = tablero;
        this.algoritmo = algoritmo;
        this.observadores = new ArrayList<>();
    }

    public void iniciarJuego() {
    }

    public void agregarObservador(ObservadorJuego obs) {
        observadores.add(obs);
    }

    public void jugarTurnoHumano(int fila, int columna) {
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

    public List<ObservadorJuego> getObservadores() {
        return observadores;
    }

    public void setObservadores(List<ObservadorJuego> observadores) {
        this.observadores = observadores;
    }
}
