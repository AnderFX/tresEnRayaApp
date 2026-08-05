/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.commycompanytresenrayaapp.controlador;

import com.example.commycompanytresenrayaapp.modelo.Ficha;

/**
 * Contrato del patrón Observer usado para que ControladorJuego pueda avisar
 * a la vista (u otros interesados) cuando ocurre una jugada o el juego
 * termina, sin conocer detalles de cómo está construida esa vista.
 */
public interface ObservadorJuego {

    void onJugadaRealizada(int fila, int columna, Ficha ficha);

    void onJuegoTerminado(String mensaje);
}
