package com.example.commycompanytresenrayaapp;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.commycompanytresenrayaapp.controlador.ControladorJuego;
import com.example.commycompanytresenrayaapp.modelo.Ficha;
import com.example.commycompanytresenrayaapp.modelo.Jugador;
import com.example.commycompanytresenrayaapp.modelo.Minimax;
import com.example.commycompanytresenrayaapp.modelo.Tablero;
import com.example.commycompanytresenrayaapp.modelo.TipoJugador;
import com.example.commycompanytresenrayaapp.vista.PantallaJuego;

/**
 * Punto de entrada de la app. MainActivity es el "ensamblador": arma la
 * pantalla de configuración (elegir modo de juego y sus opciones), crea
 * el Modelo (Tablero, Minimax, ControladorJuego con sus dos Jugador) e
 * incrusta la Vista (PantallaJuego) en la pantalla.
 *
 * <p>Soporta tres modos, todos resueltos por el mismo ControladorJuego
 * genérico: solo cambia el TipoJugador que se le asigna a cada ficha.</p>
 * <ul>
 *   <li>Jugador vs Computadora (JVC)</li>
 *   <li>Jugador vs Jugador (JVJ)</li>
 *   <li>Computadora vs Computadora (CVC)</li>
 * </ul>
 */
public class MainActivity extends AppCompatActivity {

    private ControladorJuego controlador;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Tres en Raya");
        mostrarPantallaConfiguracion();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (controlador != null) {
            controlador.detenerJuego();
        }
    }

    // ---------------------------------------------------------------
    // Pantalla de configuración
    // ---------------------------------------------------------------

    private void mostrarPantallaConfiguracion() {
        LinearLayout config = new LinearLayout(this);
        config.setOrientation(LinearLayout.VERTICAL);
        config.setGravity(Gravity.CENTER);
        int padding = dpToPx(24);
        config.setPadding(padding, padding, padding, padding);

        // --- GRUPO MODO ---
        TextView tituloModo = new TextView(this);
        tituloModo.setText("¿A qué quieres jugar?");
        config.addView(tituloModo);

        RadioGroup grupoModo = new RadioGroup(this);
        RadioButton opcionJVC = new RadioButton(this);
        opcionJVC.setId(View.generateViewId());
        opcionJVC.setText("Jugador vs Computadora");
        opcionJVC.setChecked(true);

        RadioButton opcionJVJ = new RadioButton(this);
        opcionJVJ.setId(View.generateViewId());
        opcionJVJ.setText("Jugador vs Jugador");

        RadioButton opcionCVC = new RadioButton(this);
        opcionCVC.setId(View.generateViewId());
        opcionCVC.setText("Computadora vs Computadora");

        grupoModo.addView(opcionJVC);
        grupoModo.addView(opcionJVJ);
        grupoModo.addView(opcionCVC);
        config.addView(grupoModo);

        // --- SUB-BLOQUE: opciones de Jugador vs Computadora ---
        LinearLayout bloqueJVC = construirBloqueJVC();
        config.addView(bloqueJVC);

        // --- SUB-BLOQUE: opciones de Jugador vs Jugador ---
        LinearLayout bloqueJVJ = construirBloqueJVJ();
        bloqueJVJ.setVisibility(View.GONE);
        config.addView(bloqueJVJ);

        // --- SUB-BLOQUE: opciones de Computadora vs Computadora ---
        LinearLayout bloqueCVC = construirBloqueCVC();
        bloqueCVC.setVisibility(View.GONE);
        config.addView(bloqueCVC);

        // Mostrar solo el sub-bloque del modo elegido
        grupoModo.setOnCheckedChangeListener((group, checkedId) -> {
            bloqueJVC.setVisibility(checkedId == opcionJVC.getId() ? View.VISIBLE : View.GONE);
            bloqueJVJ.setVisibility(checkedId == opcionJVJ.getId() ? View.VISIBLE : View.GONE);
            bloqueCVC.setVisibility(checkedId == opcionCVC.getId() ? View.VISIBLE : View.GONE);
        });

        // --- BOTÓN COMENZAR ---
        Button botonComenzar = new Button(this);
        botonComenzar.setText("Comenzar Juego");
        botonComenzar.setOnClickListener(v -> {
            int idModo = grupoModo.getCheckedRadioButtonId();
            if (idModo == opcionJVC.getId()) {
                comenzarJVC(bloqueJVC);
            } else if (idModo == opcionJVJ.getId()) {
                comenzarJVJ(bloqueJVJ);
            } else {
                comenzarCVC(bloqueCVC);
            }
        });
        config.addView(botonComenzar);

        setContentView(config);
    }

    // ---------------------------------------------------------------
    // Bloque: Jugador vs Computadora
    // ---------------------------------------------------------------

    private RadioButton jvcOpcionX;
    private RadioButton jvcOpcionHumanoEmpieza;

    private LinearLayout construirBloqueJVC() {
        LinearLayout bloque = new LinearLayout(this);
        bloque.setOrientation(LinearLayout.VERTICAL);
        bloque.setGravity(Gravity.CENTER);

        TextView tituloSimbolo = new TextView(this);
        tituloSimbolo.setText("¿Con qué símbolo quieres jugar?");
        bloque.addView(tituloSimbolo);

        RadioGroup grupoSimbolo = new RadioGroup(this);
        jvcOpcionX = new RadioButton(this);
        jvcOpcionX.setId(View.generateViewId());
        jvcOpcionX.setText("X");
        jvcOpcionX.setChecked(true);

        RadioButton opcionO = new RadioButton(this);
        opcionO.setId(View.generateViewId());
        opcionO.setText("O");

        grupoSimbolo.addView(jvcOpcionX);
        grupoSimbolo.addView(opcionO);
        bloque.addView(grupoSimbolo);

        TextView tituloTurno = new TextView(this);
        tituloTurno.setText("¿Quién empieza la partida?");
        bloque.addView(tituloTurno);

        RadioGroup grupoTurno = new RadioGroup(this);
        jvcOpcionHumanoEmpieza = new RadioButton(this);
        jvcOpcionHumanoEmpieza.setId(View.generateViewId());
        jvcOpcionHumanoEmpieza.setText("Yo (humano)");
        jvcOpcionHumanoEmpieza.setChecked(true);

        RadioButton opcionPC = new RadioButton(this);
        opcionPC.setId(View.generateViewId());
        opcionPC.setText("La computadora");

        grupoTurno.addView(jvcOpcionHumanoEmpieza);
        grupoTurno.addView(opcionPC);
        bloque.addView(grupoTurno);

        return bloque;
    }

    private void comenzarJVC(LinearLayout bloqueJVC) {
        Ficha fichaHumano = jvcOpcionX.isChecked() ? Ficha.X : Ficha.O;
        Ficha fichaPC = obtenerFichaContraria(fichaHumano);
        boolean humanoEmpieza = jvcOpcionHumanoEmpieza.isChecked();

        Jugador jugadorHumano = new Jugador(fichaHumano, TipoJugador.HUMANO, "Jugador");
        Jugador jugadorPC = new Jugador(fichaPC, TipoJugador.MAQUINA, "Computadora");

        Jugador jugadorX = (fichaHumano == Ficha.X) ? jugadorHumano : jugadorPC;
        Jugador jugadorO = (fichaHumano == Ficha.X) ? jugadorPC : jugadorHumano;

        Ficha fichaInicial = humanoEmpieza ? fichaHumano : fichaPC;
        iniciarPartida(jugadorX, jugadorO, fichaInicial, /*retardoMs=*/ 600, /*mostrarSugerencia=*/ true);
    }

    // ---------------------------------------------------------------
    // Bloque: Jugador vs Jugador
    // ---------------------------------------------------------------

    private RadioButton jvjOpcionJugador1Empieza;

    private LinearLayout construirBloqueJVJ() {
        LinearLayout bloque = new LinearLayout(this);
        bloque.setOrientation(LinearLayout.VERTICAL);
        bloque.setGravity(Gravity.CENTER);

        TextView nota = new TextView(this);
        nota.setText("Jugador 1 = X   ·   Jugador 2 = O");
        bloque.addView(nota);

        TextView tituloTurno = new TextView(this);
        tituloTurno.setText("¿Quién empieza la partida?");
        bloque.addView(tituloTurno);

        RadioGroup grupoTurno = new RadioGroup(this);
        jvjOpcionJugador1Empieza = new RadioButton(this);
        jvjOpcionJugador1Empieza.setId(View.generateViewId());
        jvjOpcionJugador1Empieza.setText("Jugador 1 (X)");
        jvjOpcionJugador1Empieza.setChecked(true);

        RadioButton opcionJugador2 = new RadioButton(this);
        opcionJugador2.setId(View.generateViewId());
        opcionJugador2.setText("Jugador 2 (O)");

        grupoTurno.addView(jvjOpcionJugador1Empieza);
        grupoTurno.addView(opcionJugador2);
        bloque.addView(grupoTurno);

        return bloque;
    }

    private void comenzarJVJ(LinearLayout bloqueJVJ) {
        Jugador jugadorX = new Jugador(Ficha.X, TipoJugador.HUMANO, "Jugador 1");
        Jugador jugadorO = new Jugador(Ficha.O, TipoJugador.HUMANO, "Jugador 2");

        Ficha fichaInicial = jvjOpcionJugador1Empieza.isChecked() ? Ficha.X : Ficha.O;
        iniciarPartida(jugadorX, jugadorO, fichaInicial, /*retardoMs=*/ 0, /*mostrarSugerencia=*/ true);
    }

    // ---------------------------------------------------------------
    // Bloque: Computadora vs Computadora
    // ---------------------------------------------------------------

    private RadioButton cvcOpcionVelocidadNormal;
    private RadioButton cvcOpcionVelocidadLenta;

    private LinearLayout construirBloqueCVC() {
        LinearLayout bloque = new LinearLayout(this);
        bloque.setOrientation(LinearLayout.VERTICAL);
        bloque.setGravity(Gravity.CENTER);

        TextView nota = new TextView(this);
        nota.setText("Dos computadoras jugarán entre sí automáticamente.");
        bloque.addView(nota);

        TextView tituloVelocidad = new TextView(this);
        tituloVelocidad.setText("Velocidad de la partida");
        bloque.addView(tituloVelocidad);

        RadioGroup grupoVelocidad = new RadioGroup(this);
        RadioButton opcionRapida = new RadioButton(this);
        opcionRapida.setId(View.generateViewId());
        opcionRapida.setText("Rápida");

        cvcOpcionVelocidadNormal = new RadioButton(this);
        cvcOpcionVelocidadNormal.setId(View.generateViewId());
        cvcOpcionVelocidadNormal.setText("Normal");
        cvcOpcionVelocidadNormal.setChecked(true);

        cvcOpcionVelocidadLenta = new RadioButton(this);
        cvcOpcionVelocidadLenta.setId(View.generateViewId());
        cvcOpcionVelocidadLenta.setText("Lenta");

        grupoVelocidad.addView(opcionRapida);
        grupoVelocidad.addView(cvcOpcionVelocidadNormal);
        grupoVelocidad.addView(cvcOpcionVelocidadLenta);
        bloque.addView(grupoVelocidad);

        return bloque;
    }

    private void comenzarCVC(LinearLayout bloqueCVC) {
        Jugador jugadorX = new Jugador(Ficha.X, TipoJugador.MAQUINA, "Computadora 1 (X)");
        Jugador jugadorO = new Jugador(Ficha.O, TipoJugador.MAQUINA, "Computadora 2 (O)");

        int retardoMs;
        if (cvcOpcionVelocidadLenta.isChecked()) {
            retardoMs = 1500;
        } else if (cvcOpcionVelocidadNormal.isChecked()) {
            retardoMs = 700;
        } else {
            retardoMs = 250;
        }

        iniciarPartida(jugadorX, jugadorO, Ficha.X, retardoMs, /*mostrarSugerencia=*/ false);
    }

    // ---------------------------------------------------------------
    // Armado común de la partida
    // ---------------------------------------------------------------

    private void iniciarPartida(Jugador jugadorX, Jugador jugadorO, Ficha fichaInicial,
                                 int retardoMs, boolean mostrarSugerencia) {
        Tablero tablero = new Tablero();
        // fichaPC/fichaHumano de Minimax son solo etiquetas de las dos fichas
        // opuestas (X y O); el algoritmo calcula la mejor jugada para
        // cualquiera de las dos vía obtenerMejorJugadaPara(ficha), así que
        // esta misma instancia sirve para los tres modos.
        Minimax algoritmo = new Minimax(tablero, Ficha.X, Ficha.O);

        controlador = new ControladorJuego(tablero, algoritmo, jugadorX, jugadorO);
        controlador.setRetardoMaquinaMs(retardoMs);

        PantallaJuego pantalla = new PantallaJuego(this, controlador);
        setContentView(construirContenedor(pantalla, mostrarSugerencia));
        controlador.iniciarJuego(fichaInicial);
    }

    private Ficha obtenerFichaContraria(Ficha ficha) {
        return (ficha == Ficha.X) ? Ficha.O : Ficha.X;
    }

    private LinearLayout construirContenedor(PantallaJuego pantalla, boolean mostrarSugerencia) {
        LinearLayout contenedor = new LinearLayout(this);
        contenedor.setOrientation(LinearLayout.VERTICAL);
        contenedor.setGravity(Gravity.CENTER);
        int padding = dpToPx(24);
        contenedor.setPadding(padding, padding, padding, padding);
        contenedor.addView(pantalla);
        if (mostrarSugerencia) {
            contenedor.addView(construirBotonSugerencia(pantalla));
        }
        contenedor.addView(construirBotonReiniciar());
        return contenedor;
    }

    private Button construirBotonSugerencia(PantallaJuego pantalla) {
        Button botonSugerencia = new Button(this);
        botonSugerencia.setText("Sugerencia");

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.topMargin = dpToPx(16);
        botonSugerencia.setLayoutParams(params);

        botonSugerencia.setOnClickListener(v -> {
            int[] sugerencia = controlador.obtenerSugerenciaParaHumano();
            if (sugerencia == null) {
                Toast.makeText(this, "No hay sugerencia disponible ahora.", Toast.LENGTH_SHORT).show();
                return;
            }
            pantalla.resaltarSugerencia(sugerencia[0], sugerencia[1]);
        });
        return botonSugerencia;
    }

    private Button construirBotonReiniciar() {
        Button botonReiniciar = new Button(this);
        botonReiniciar.setText("Nueva Partida");

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.topMargin = dpToPx(16);
        botonReiniciar.setLayoutParams(params);

        botonReiniciar.setOnClickListener(v -> {
            if (controlador != null) {
                controlador.detenerJuego();
            }
            recreate();
        });
        return botonReiniciar;
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}