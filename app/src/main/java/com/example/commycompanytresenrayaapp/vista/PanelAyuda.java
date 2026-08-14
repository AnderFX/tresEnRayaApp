package com.example.commycompanytresenrayaapp.vista;

import android.content.Context;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.commycompanytresenrayaapp.controlador.ControladorJuego;
import com.example.commycompanytresenrayaapp.controlador.ModoJuego;
import com.example.commycompanytresenrayaapp.controlador.ObservadorJuego;
import com.example.commycompanytresenrayaapp.modelo.ArbolNario;
import com.example.commycompanytresenrayaapp.modelo.Ficha;

/**
 * Implementa ObservadorJuego únicamente para saber, en todo momento, de
 * quién es el turno y así poder habilitar/deshabilitar el botón de
 * Sugerencia — el guard "de verdad" (el que garantiza que nunca se
 * calcule una sugerencia fuera de turno o en un modo donde no aplica, la
 * toque quien la toque) vive en
 * ControladorJuego.obtenerSugerenciaParaHumano(); esto es solo la mejora
 * de UX que evita que el usuario pueda tocar el botón en primer lugar.
 *
 * <p>Funciona en los 3 modos de juego ({@link ModoJuego}): en PC_VS_PC y
 * HUMANO_VS_HUMANO el botón de Sugerencia queda deshabilitado toda la
 * partida (no hay "el humano" al que sugerirle nada), y el botón de Ver
 * árbol solo muestra algo cuando efectivamente hay una PC jugando.</p>
 */
public class PanelAyuda extends LinearLayout implements ObservadorJuego {

    private final ControladorJuego controlador;
    private final Button botonSugerencia;

    public PanelAyuda(Context context, ControladorJuego controlador, PantallaJuego pantalla) {
        super(context);
        this.controlador = controlador;
        setOrientation(HORIZONTAL);

        botonSugerencia = new Button(context);
        botonSugerencia.setText("💡 Sugerencia");
        botonSugerencia.setOnClickListener(v -> {
            int[] sugerencia = controlador.obtenerSugerenciaParaHumano();
            if (sugerencia[0] == -1) {
                Toast.makeText(context, "No hay sugerencias disponibles.", Toast.LENGTH_SHORT).show();
            } else {
                pantalla.resaltarCasilla(sugerencia[0], sugerencia[1]);
            }
        });
        addView(botonSugerencia);

        Button botonVerArbol = new Button(context);
        botonVerArbol.setText("🌳 Ver árbol");
        botonVerArbol.setOnClickListener(v -> {
            ArbolNario arbol = controlador.getUltimoArbolGeneradoPorPC();
            if (arbol == null || arbol.getRaiz() == null) {
                String mensaje = (controlador.getModo() == ModoJuego.HUMANO_VS_HUMANO)
                        ? "Este modo no usa la computadora: no hay árbol que mostrar."
                        : "La PC todavía no ha jugado.";
                Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show();
            } else {
                int[] caminoPC = controlador.getCaminoTomadoPC();
                int[] caminoHumano = controlador.getCaminoTomadoHumano();
                new VistaArbolDialog(context, arbol, caminoPC, caminoHumano).mostrar();
            }
        });
        addView(botonVerArbol);

        // Estado inicial del botón: en este punto MainActivity ya configuró
        // el modo y (según cuál sea) turnoHumano en el controlador, pero
        // todavía no llamó a iniciarJuego(), así que tanto getModo() como
        // isTurnoHumano() ya reflejan correctamente el estado inicial.
        actualizarEstadoBotonSugerencia();

        // Se registra como observador adicional (PantallaJuego ya es otro)
        // para reaccionar a cada jugada sin tocar ningún método existente
        // de ControladorJuego.
        controlador.agregarObservador(this);
    }

    private void actualizarEstadoBotonSugerencia() {
        boolean disponible = controlador.getModo() == ModoJuego.HUMANO_VS_PC
                && controlador.isTurnoHumano()
                && !controlador.isJuegoTerminado();
        botonSugerencia.setEnabled(disponible);
    }

    @Override
    public void onJugadaRealizada(int fila, int columna, Ficha ficha) {
        actualizarEstadoBotonSugerencia();
    }

    @Override
    public void onJuegoTerminado(String mensaje) {
        botonSugerencia.setEnabled(false);
    }
}
