package com.example.commycompanytresenrayaapp.modelo;

public class Jugador {

    private Ficha ficha;
    private TipoJugador tipo;
    private String nombre;

    public Jugador(Ficha ficha, TipoJugador tipo, String nombre) {
        this.ficha = ficha;
        this.tipo = tipo;
        this.nombre = nombre;
    }

    public Ficha getFicha() { return ficha; }
    public void setFicha(Ficha ficha) { this.ficha = ficha; }
    public TipoJugador getTipo() { return tipo; }
    public void setTipo(TipoJugador tipo) { this.tipo = tipo; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
}

