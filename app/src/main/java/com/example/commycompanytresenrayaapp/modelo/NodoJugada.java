/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.commycompanytresenrayaapp.modelo;

import java.util.ArrayList;
import java.util.List;

/**
 * Nodo del árbol n-ario que representa las jugadas futuras posibles.
 * Cada NodoJugada es una "foto" del tablero en un momento dado 
 * y sus hijos son las jugadas que podrían seguir a partir de ese estado.
 */
public class NodoJugada {

    private Tablero estado;
    private List<NodoJugada> hijos;
    private int utilidad;
    private int filaJugada;
    private int columnaJugada;

    public NodoJugada(Tablero estado, int fila, int columna) {
        this.estado = estado;
        this.filaJugada = fila;
        this.columnaJugada = columna;
        this.hijos = new ArrayList<>();
        this.utilidad = 0;
    }

    public void agregarHijo(NodoJugada hijo) {
        hijos.add(hijo);
    }

    public boolean esHoja() {
        return hijos.isEmpty();
    }

    // ---------- Getters / Setters ----------

    public Tablero getEstado() {
        return estado;
    }

    public void setEstado(Tablero estado) {
        this.estado = estado;
    }

    public List<NodoJugada> getHijos() {
        return hijos;
    }

    public void setHijos(List<NodoJugada> hijos) {
        this.hijos = hijos;
    }

    public int getUtilidad() {
        return utilidad;
    }

    public void setUtilidad(int utilidad) {
        this.utilidad = utilidad;
    }

    public int getFilaJugada() {
        return filaJugada;
    }

    public void setFilaJugada(int filaJugada) {
        this.filaJugada = filaJugada;
    }

    public int getColumnaJugada() {
        return columnaJugada;
    }

    public void setColumnaJugada(int columnaJugada) {
        this.columnaJugada = columnaJugada;
    }
}
