package com.example.commycompanytresenrayaapp;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.commycompanytresenrayaapp.controlador.ControladorJuego;
import com.example.commycompanytresenrayaapp.controlador.ModoJuego;
import com.example.commycompanytresenrayaapp.controlador.ObservadorJuego;
import com.example.commycompanytresenrayaapp.modelo.Ficha;
import com.example.commycompanytresenrayaapp.modelo.Minimax;
import com.example.commycompanytresenrayaapp.modelo.Tablero;
import com.example.commycompanytresenrayaapp.vista.PantallaJuego;
import com.example.commycompanytresenrayaapp.vista.VistaArbol;

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
    private static final long DEMORA_PC_VS_PC_MS = 1000;
    private static final int ANCHO_BOTON_JUEGO_DP = 220;

    private ControladorJuego controlador;
    private PantallaJuego pantalla;
    private VistaArbol vistaArbol;
    private LinearLayout seccionArbol;
    private TextView indicadorTurno;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Tres en Raya vs. Computadora");
        mostrarPantallaConfiguracion();   // en vez de iniciarPartida() directo
    }

    private void mostrarPantallaConfiguracion() {
        LinearLayout config = new LinearLayout(this);
        config.setOrientation(LinearLayout.VERTICAL);
        config.setGravity(Gravity.CENTER);
        int padding = dpToPx(24);
        config.setPadding(padding, padding, padding, padding);

        TextView titulo = new TextView(this);
        titulo.setText("Tres en Raya");
        titulo.setTextSize(32);
        titulo.setGravity(Gravity.CENTER);
        titulo.setTypeface(titulo.getTypeface(), Typeface.BOLD);
        titulo.setTextColor(Color.BLACK);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            titulo.setFontVariationSettings("'wght' 900");
        }
        config.addView(titulo);

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

        RadioButton opcionPcVsPc = new RadioButton(this);
        opcionPcVsPc.setId(View.generateViewId());
        opcionPcVsPc.setText("Computadora vs. computadora");

        grupoModo.addView(opcionContraPC);
        grupoModo.addView(opcionDosHumanos);
        grupoModo.addView(opcionPcVsPc);
        config.addView(grupoModo);

        // --- GRUPO 1: SÍMBOLO ---
        LinearLayout seccionSimbolo = new LinearLayout(this);
        seccionSimbolo.setOrientation(LinearLayout.VERTICAL);
        seccionSimbolo.setGravity(Gravity.CENTER);

        TextView tituloSimbolo = new TextView(this);
        tituloSimbolo.setText("¿Con qué símbolo quieres jugar?");
        seccionSimbolo.addView(tituloSimbolo);

        RadioGroup grupoSimbolo = new RadioGroup(this);

        RadioButton opcionX = new RadioButton(this);
        opcionX.setId(View.generateViewId()); // necesario para que el RadioGroup sepa cual esta marcado
        opcionX.setText("X");
        opcionX.setChecked(true);

        RadioButton opcionO = new RadioButton(this);
        opcionO.setId(View.generateViewId());
        opcionO.setText("O");

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

        RadioGroup grupoTurno = new RadioGroup(this);

        RadioButton opcionHumano = new RadioButton(this);
        opcionHumano.setId(View.generateViewId());
        opcionHumano.setText("Yo (humano)");
        opcionHumano.setChecked(true);

        RadioButton opcionPC = new RadioButton(this);
        opcionPC.setId(View.generateViewId());
        opcionPC.setText("La computadora");

        grupoTurno.addView(opcionHumano);
        grupoTurno.addView(opcionPC);
        seccionTurno.addView(grupoTurno);
        config.addView(seccionTurno);

        grupoModo.setOnCheckedChangeListener((group, checkedId) -> {
            boolean esContraPC = checkedId == opcionContraPC.getId();
            seccionSimbolo.setVisibility(esContraPC ? View.VISIBLE : View.GONE);
            seccionTurno.setVisibility(esContraPC ? View.VISIBLE : View.GONE);
        });

        // --- BOTÓN COMENZAR ---
        Button botonComenzar = new Button(this);
        botonComenzar.setText("Comenzar Juego");
        botonComenzar.setPadding(dpToPx(24), dpToPx(12), dpToPx(24), dpToPx(12));
        botonComenzar.setOnClickListener(v -> {
            if (opcionPcVsPc.isChecked()) {
                iniciarPartidaPcVsPc();
                return;
            }
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

        Tablero tablero = new Tablero();
        Minimax algoritmo = new Minimax(tablero, fichaPC, fichaHumano);

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

    private void iniciarPartidaPcVsPc() {
        Tablero tablero = new Tablero();
        Minimax algoritmo = new Minimax(tablero, Ficha.O, Ficha.X);

        controlador = new ControladorJuego(tablero, algoritmo);
        controlador.setModoJuego(ModoJuego.PC_VS_PC);
        controlador.setFichaEnTurno(Ficha.X);

        montarPantallaDeJuego();
    }

    private void montarPantallaDeJuego() {
        pantalla = new PantallaJuego(this, controlador);
        vistaArbol = new VistaArbol(this, controlador);
        controlador.agregarObservador(this);
        controlador.agregarObservador(vistaArbol);
        setContentView(construirContenedor(pantalla));
        controlador.iniciarJuego();
    }

    private ScrollView construirContenedor(PantallaJuego pantalla) {
        LinearLayout contenedor = new LinearLayout(this);
        contenedor.setOrientation(LinearLayout.VERTICAL);
        contenedor.setGravity(Gravity.CENTER);
        int padding = dpToPx(24);
        contenedor.setPadding(padding, padding, padding, padding);

        indicadorTurno = new TextView(this);
        indicadorTurno.setTextSize(18);
        contenedor.addView(indicadorTurno, paramsCentrados());

        contenedor.addView(pantalla, paramsCentrados());

        if (controlador.getModoJuego() != ModoJuego.PC_VS_PC) {
            contenedor.addView(construirBotonSugerencia(pantalla));
        }
        contenedor.addView(construirBotonReiniciar());
        contenedor.addView(construirBotonArbol());

        seccionArbol = new LinearLayout(this);
        seccionArbol.setOrientation(LinearLayout.VERTICAL);
        seccionArbol.setGravity(Gravity.CENTER_HORIZONTAL);
        seccionArbol.addView(construirTituloArbol());
        seccionArbol.addView(construirScrollArbol());
        contenedor.addView(seccionArbol);

        ScrollView scrollExterno = new ScrollView(this);
        scrollExterno.setFillViewport(true);
        scrollExterno.addView(contenedor);
        return scrollExterno;
    }

    private LinearLayout.LayoutParams paramsCentrados() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER_HORIZONTAL;
        return params;
    }

    private Button construirBotonArbol() {
        Button botonArbol = new Button(this);
        botonArbol.setText("Ocultar árbol");
        botonArbol.setPadding(dpToPx(24), dpToPx(12), dpToPx(24), dpToPx(12));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                dpToPx(ANCHO_BOTON_JUEGO_DP), LinearLayout.LayoutParams.WRAP_CONTENT);
        params.topMargin = dpToPx(16);
        botonArbol.setLayoutParams(params);

        botonArbol.setOnClickListener(v -> {
            boolean estabaVisible = seccionArbol.getVisibility() == View.VISIBLE;
            seccionArbol.setVisibility(estabaVisible ? View.GONE : View.VISIBLE);
            botonArbol.setText(estabaVisible ? "Mostrar árbol" : "Ocultar árbol");
            if (!estabaVisible) {
                vistaArbol.actualizar(controlador.getRaizArbolPartida());
            }
        });
        return botonArbol;
    }

    private TextView construirTituloArbol() {
        TextView titulo = new TextView(this);
        titulo.setText("Árbol de la partida\n" + leyendaArbol());
        titulo.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.topMargin = dpToPx(24);
        titulo.setLayoutParams(params);
        return titulo;
    }

    private String leyendaArbol() {
        String gris = " · Gris: solo analizado";
        if (controlador.getModoJuego() == ModoJuego.DOS_HUMANOS) {
            return "Verde: Jugador X · Naranja: Jugador O" + gris;
        }
        if (controlador.getModoJuego() == ModoJuego.PC_VS_PC) {
            return "Verde: Bot 1 (X) · Naranja: Bot 2 (O)" + gris;
        }
        boolean humanoEsX = controlador.getFichaHumano() == Ficha.X;
        String colorHumano = humanoEsX ? "Verde" : "Naranja";
        String colorPC = humanoEsX ? "Naranja" : "Verde";
        return colorHumano + ": tus jugadas · " + colorPC + ": la computadora" + gris;
    }

    private HorizontalScrollView construirScrollArbol() {
        HorizontalScrollView scrollArbol = new HorizontalScrollView(this);
        scrollArbol.setFillViewport(true);
        scrollArbol.addView(vistaArbol);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.topMargin = dpToPx(8);
        scrollArbol.setLayoutParams(params);
        return scrollArbol;
    }

    private Button construirBotonSugerencia(PantallaJuego pantalla) {
        Button botonSugerencia = new Button(this);
        botonSugerencia.setText("Sugerencia");
        botonSugerencia.setPadding(dpToPx(24), dpToPx(12), dpToPx(24), dpToPx(12));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                dpToPx(ANCHO_BOTON_JUEGO_DP),
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.topMargin = dpToPx(16);
        botonSugerencia.setLayoutParams(params);

        botonSugerencia.setOnClickListener(v -> {
            int[] sugerencia = controlador.obtenerSugerenciaParaTurnoActual();
            vistaArbol.actualizar(controlador.getRaizArbolPartida());
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
        botonReiniciar.setPadding(dpToPx(24), dpToPx(12), dpToPx(24), dpToPx(12));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                dpToPx(ANCHO_BOTON_JUEGO_DP),
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.topMargin = dpToPx(16);
        botonReiniciar.setLayoutParams(params);

        botonReiniciar.setOnClickListener(v -> recreate());
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
        if (requiereJugadaAutomatica(fichaEnTurno)) {
            indicadorTurno.setText(textoPensando(fichaEnTurno));
            pantalla.setInteractivo(false);
            long demora = controlador.getModoJuego() == ModoJuego.PC_VS_PC
                    ? DEMORA_PC_VS_PC_MS
                    : DEMORA_JUGADA_PC_MS;
            handler.postDelayed(controlador::realizarJugadaAutomatica, demora);
            return;
        }
        pantalla.setInteractivo(true);
        indicadorTurno.setText("Turno de: " + fichaEnTurno.name());
    }

    private boolean requiereJugadaAutomatica(Ficha fichaEnTurno) {
        ModoJuego modo = controlador.getModoJuego();
        if (modo == ModoJuego.PC_VS_PC) {
            return true;
        }
        return modo == ModoJuego.CONTRA_PC && fichaEnTurno == controlador.getFichaPC();
    }

    private String textoPensando(Ficha fichaEnTurno) {
        if (controlador.getModoJuego() == ModoJuego.PC_VS_PC) {
            String bot = (fichaEnTurno == Ficha.X) ? "Bot 1" : "Bot 2";
            return bot + " está pensando...";
        }
        return "La computadora está pensando...";
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
