package com.example.commycompanytresenrayaapp;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.example.commycompanytresenrayaapp.controlador.ModoJuego;
import com.example.commycompanytresenrayaapp.controlador.ObservadorJuego;
import com.example.commycompanytresenrayaapp.modelo.Ficha;
import com.example.commycompanytresenrayaapp.modelo.Jugador;
import com.example.commycompanytresenrayaapp.modelo.Minimax;
import com.example.commycompanytresenrayaapp.modelo.Tablero;
import com.example.commycompanytresenrayaapp.modelo.TipoJugador;
import com.example.commycompanytresenrayaapp.vista.PantallaJuego;

/**
 * Punto de entrada de la app. Siguiendo el diagrama de clases original,
 * MainActivity es solo el "ensamblador": crea el Modelo (Tablero,
 * Minimax, ControladorJuego), instancia la Vista (PantallaJuego, que
 * implementa ObservadorJuego y construye su propia UI por codigo) y la
 * incrusta en la pantalla. MainActivity tambien se registra como
 * ObservadorJuego, solo para reflejar el turno actual en pantalla.
 */
public class MainActivity extends AppCompatActivity implements ObservadorJuego {

    private static final long DEMORA_JUGADA_PC_MS = 500;

    private ControladorJuego controlador;
    private PantallaJuego pantalla;
    private TextView indicadorTurno;
    private final Handler handler = new Handler(Looper.getMainLooper());

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

        // --- GRUPO 0: MODO ---
        TextView tituloModo = new TextView(this);
        tituloModo.setText("¿Cómo quieres jugar?");
        config.addView(tituloModo);

        RadioGroup grupoModo = new RadioGroup(this);

        RadioButton opcionContraPC = new RadioButton(this);
        opcionContraPC.setId(View.generateViewId());
        opcionContraPC.setText("Contra la computadora");
        opcionContraPC.setChecked(true);

        RadioButton opcionDosHumanos = new RadioButton(this);
        opcionDosHumanos.setId(View.generateViewId());
        opcionDosHumanos.setText("Dos jugadores humanos");

        grupoModo.addView(opcionContraPC);
        grupoModo.addView(opcionDosHumanos);
        config.addView(grupoModo);

        // --- GRUPO 1: SÍMBOLO ---
        LinearLayout seccionSimbolo = new LinearLayout(this);
        seccionSimbolo.setOrientation(LinearLayout.VERTICAL);
        seccionSimbolo.setGravity(Gravity.CENTER);

        TextView tituloSimbolo = new TextView(this);
        tituloSimbolo.setText("¿Con qué símbolo quieres jugar?");
        seccionSimbolo.addView(tituloSimbolo);

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

        grupoSimbolo.addView(opcionX);
        grupoSimbolo.addView(opcionO);
        seccionSimbolo.addView(grupoSimbolo);
        config.addView(seccionSimbolo);

        // --- GRUPO 2: TURNO ---
        LinearLayout seccionTurno = new LinearLayout(this);
        seccionTurno.setOrientation(LinearLayout.VERTICAL);
        seccionTurno.setGravity(Gravity.CENTER);

        TextView tituloTurno = new TextView(this);
        tituloTurno.setText("¿Quién empieza la partida?");
        seccionTurno.addView(tituloTurno);

        // --- SUB-BLOQUE: opciones de Jugador vs Jugador ---
        LinearLayout bloqueJVJ = construirBloqueJVJ();
        bloqueJVJ.setVisibility(View.GONE);
        config.addView(bloqueJVJ);

        // --- SUB-BLOQUE: opciones de Computadora vs Computadora ---
        LinearLayout bloqueCVC = construirBloqueCVC();
        bloqueCVC.setVisibility(View.GONE);
        config.addView(bloqueCVC);

        RadioButton opcionPC = new RadioButton(this);
        opcionPC.setId(View.generateViewId());
        opcionPC.setText("La computadora");

        grupoTurno.addView(opcionHumano);
        grupoTurno.addView(opcionPC);
        seccionTurno.addView(grupoTurno);
        config.addView(seccionTurno);

        grupoModo.setOnCheckedChangeListener((group, checkedId) -> {
            boolean esDosHumanos = checkedId == opcionDosHumanos.getId();
            seccionSimbolo.setVisibility(esDosHumanos ? View.GONE : View.VISIBLE);
            seccionTurno.setVisibility(esDosHumanos ? View.GONE : View.VISIBLE);
        });

        // --- BOTÓN COMENZAR ---
        Button botonComenzar = new Button(this);
        botonComenzar.setText("Comenzar Juego");
        botonComenzar.setOnClickListener(v -> {
            if (opcionDosHumanos.isChecked()) {
                iniciarPartidaDosHumanos();
                return;
            }
            Ficha fichaHumano = opcionX.isChecked() ? Ficha.X : Ficha.O;
            boolean humanoEmpieza = opcionHumano.isChecked();
            iniciarPartidaContraPC(fichaHumano, humanoEmpieza);
        });
        config.addView(botonComenzar);

        setContentView(config);
    }

    private void iniciarPartidaContraPC(Ficha fichaHumano, boolean humanoEmpieza) {
        Ficha fichaPC = (fichaHumano == Ficha.X) ? Ficha.O : Ficha.X;

    private RadioButton jvcOpcionX;
    private RadioButton jvcOpcionHumanoEmpieza;

        controlador = new ControladorJuego(tablero, algoritmo);
        controlador.setModoJuego(ModoJuego.CONTRA_PC);
        controlador.setFichaHumano(fichaHumano);
        controlador.setFichaPC(fichaPC);
        controlador.setFichaEnTurno(humanoEmpieza ? fichaHumano : fichaPC);

        montarPantallaDeJuego();
    }

    private void iniciarPartidaDosHumanos() {
        Tablero tablero = new Tablero();
        Minimax algoritmo = new Minimax(tablero, Ficha.O, Ficha.X);

        controlador = new ControladorJuego(tablero, algoritmo);
        controlador.setModoJuego(ModoJuego.DOS_HUMANOS);
        controlador.setFichaEnTurno(Ficha.X);

        montarPantallaDeJuego();
    }

    private void montarPantallaDeJuego() {
        pantalla = new PantallaJuego(this, controlador);
        controlador.agregarObservador(this);
        setContentView(construirContenedor(pantalla));
        controlador.iniciarJuego();
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

        indicadorTurno = new TextView(this);
        indicadorTurno.setTextSize(18);
        contenedor.addView(indicadorTurno);

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
            int[] sugerencia = controlador.obtenerSugerenciaParaTurnoActual();
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

    @Override
    public void onJugadaRealizada(int fila, int columna, Ficha ficha) {
    }

    @Override
    public void onJuegoTerminado(String mensaje) {
        indicadorTurno.setText("Juego terminado.");
        pantalla.setInteractivo(false);
    }

    @Override
    public void onCambioDeTurno(Ficha fichaEnTurno) {
        if (controlador.getModoJuego() == ModoJuego.CONTRA_PC && fichaEnTurno == controlador.getFichaPC()) {
            indicadorTurno.setText("La computadora está pensando...");
            pantalla.setInteractivo(false);
            handler.postDelayed(controlador::realizarJugadaPC, DEMORA_JUGADA_PC_MS);
            return;
        }
        pantalla.setInteractivo(true);
        indicadorTurno.setText("Turno de: " + fichaEnTurno.name());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
