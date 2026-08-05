/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.commycompanytresenrayaapp.modelo;

// Implementa la estrategia de decisión de la computadora

public class Minimax {

    private Tablero tableroActual;

    private Ficha fichaPC;
    private Ficha fichaHumano;

    public Minimax(Tablero tableroActual, Ficha fichaPC, Ficha fichaHumano) {
        this.tableroActual = tableroActual;
        this.fichaPC = fichaPC;
        this.fichaHumano = fichaHumano;
    }
    
    public void generarArbolDeJuego(ArbolNario arbol, int profundidad) {
    }

    public int minimax(NodoJugada nodoActual, int profundidad, boolean esTurnoPC) {
        return 0;
    }
    
    public int[] obtenerMejorJugada() {
        return new int[]{-1, -1};
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
}
