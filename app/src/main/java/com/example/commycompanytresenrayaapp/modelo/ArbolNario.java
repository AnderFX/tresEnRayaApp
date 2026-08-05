/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.commycompanytresenrayaapp.modelo;

/**
 * Árbol n-ario genérico usado para representar el espacio de jugadas
 * posibles del juego. Cada nodo (NodoJugada) puede tener cero o más hijos,
 * dependiendo de cuántas casillas libres tenía el tablero en ese punto.
 */
public class ArbolNario {

    private NodoJugada raiz;

    public ArbolNario() {
    }

    public ArbolNario(NodoJugada raiz) {
        this.raiz = raiz;
    }

    // Getters / Setters 

    public NodoJugada getRaiz() {
        return raiz;
    }

    public void setRaiz(NodoJugada raiz) {
        this.raiz = raiz;
    }
}
