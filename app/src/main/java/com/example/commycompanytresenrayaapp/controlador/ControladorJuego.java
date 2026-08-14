package com.example.commycompanytresenrayaapp.controlador;

import java.util.ArrayList;
import java.util.List;

import com.example.commycompanytresenrayaapp.modelo.ArbolNario;
import com.example.commycompanytresenrayaapp.modelo.Ficha;
import com.example.commycompanytresenrayaapp.modelo.Minimax;
import com.example.commycompanytresenrayaapp.modelo.Tablero;

/**
 * Orquesta una partida de Tres en Raya, en cualquiera de sus tres modos
 * ({@link ModoJuego}): humano contra la PC (el modo original), PC contra
 * sí misma, y humano contra humano.
 *
 * <p>Los campos y métodos originales ({@link #tablero}, {@link #algoritmo},
 * {@link #fichaHumano}, {@link #fichaPC}, {@link #turnoHumano},
 * {@link #juegoTerminado}, {@link #observadores}, {@link #iniciarJuego()},
 * {@link #agregarObservador(ObservadorJuego)}, {@link #jugarTurnoHumano(int, int)})
 * se mantienen con la misma firma que tenían en el modo humano vs. PC; solo
 * su lógica interna se amplió para reconocer los modos nuevos.</p>
 */
public class ControladorJuego {

    private Tablero tablero;
    private Minimax algoritmo;
    private Ficha fichaHumano;
    private Ficha fichaPC;
    private boolean turnoHumano;
    private boolean juegoTerminado;
    private List<ObservadorJuego> observadores;

    private ModoJuego modo = ModoJuego.HUMANO_VS_PC; // valor por defecto: no rompe el modo original

    /**
     * true una vez que {@link #detener()} fue invocado. Corta la cadena de
     * auto-reprogramación de {@link #avanzarTurnoPcVsPc()} (que usa
     * Handler.postDelayed) cuando el usuario abandona la partida antes de
     * que termine — por ejemplo, al presionar "Nueva Partida" en medio de
     * un PC_VS_PC. Sin esto, el callback pendiente seguiría disparándose
     * ~700 ms después e intentaría notificar a una pantalla que ya no
     * existe. No forma parte del documento original del Grupo A; es una
     * pieza mínima de robustez necesaria para integrarlo de forma segura.
     */
    private boolean detenido = false;

    // Usados SOLO en HUMANO_VS_HUMANO:
    private Ficha fichaJugador1;
    private Ficha fichaJugador2;
    private Ficha fichaEnTurno;

    // Usados SOLO en PC_VS_PC:
    private Minimax algoritmoJugador1;
    private Minimax algoritmoJugador2;

    /**
     * Instancia de {@link #algoritmoJugador1} o {@link #algoritmoJugador2}
     * que realizó la última jugada en modo PC_VS_PC. Se guarda únicamente
     * para que {@link #getUltimoArbolGeneradoPorPC()} también funcione en
     * ese modo (no forma parte del documento original del Grupo A; es la
     * pieza mínima necesaria para que la función de "ver árbol" del Grupo B
     * tenga sentido cuando quien juega es la propia PC contra sí misma).
     */
    private Minimax ultimoAlgoritmoPcVsPc;

    // Usados SOLO en HUMANO_VS_PC, para poder resaltar en el árbol el
    // camino que realmente se jugó (no solo las opciones consideradas):
    private int[] ultimaJugadaPC;              // {fila, columna} de la última jugada aplicada por la PC
    private int[] ultimaJugadaHumanoRespuesta; // {fila, columna} de la respuesta del humano a esa jugada, o null si aún no respondió

    public ControladorJuego(Tablero tablero, Minimax algoritmo) {
        this.tablero = tablero;
        this.algoritmo = algoritmo;
        this.observadores = new ArrayList<>();
        this.juegoTerminado = false;
    }

    /**
     * Arranca la partida. El comportamiento depende del modo configurado:
     * <ul>
     *   <li>{@code HUMANO_VS_PC}: si el primer turno le corresponde a la PC
     *       (turnoHumano == false), le pide de inmediato su jugada a Minimax.
     *       Si el turno inicial es del humano, no hace nada más: el
     *       controlador queda a la espera de que la vista invoque
     *       jugarTurnoHumano() cuando el usuario toque una casilla.</li>
     *   <li>{@code PC_VS_PC}: dispara la primera jugada, que se
     *       auto-encadena sola (ver {@link #avanzarTurnoPcVsPc()}).</li>
     *   <li>{@code HUMANO_VS_HUMANO}: no hace nada; se espera el primer
     *       click.</li>
     * </ul>
     *
     * <p>Según el modo, deben configurarse antes de llamar a este método:
     * fichaHumano/fichaPC/turnoHumano (vía setters, HUMANO_VS_PC),
     * {@link #configurarModoPcVsPc(Minimax, Minimax)} (PC_VS_PC), o
     * {@link #configurarModoHumanoVsHumano(Ficha, Ficha, boolean)}
     * (HUMANO_VS_HUMANO).</p>
     */
    public void iniciarJuego() {
        juegoTerminado = false;
        switch (modo) {
            case HUMANO_VS_PC:
                if (!turnoHumano) {
                    realizarJugadaPC();
                }
                break;
            case PC_VS_PC:
                avanzarTurnoPcVsPc(); // dispara la primera jugada; se auto-encadena
                break;
            case HUMANO_VS_HUMANO:
                break; // no hay nada que iniciar; se espera el primer click
        }
    }

    public void agregarObservador(ObservadorJuego obs) {
        observadores.add(obs);
    }

    /**
     * Marca este controlador como "detenido": corta cualquier cadena de
     * jugadas automáticas pendiente (relevante en modo {@code PC_VS_PC}).
     * Debe llamarse antes de abandonar la partida sin que haya terminado
     * (por ejemplo, al iniciar una partida nueva), para que el callback ya
     * agendado por {@link #avanzarTurnoPcVsPc()} no intente notificar a
     * observadores de una pantalla que ya no está en uso.
     */
    public void detener() {
        detenido = true;
    }

    /**
     * Configura la partida en modo humano vs. humano. Cambia {@link #modo}
     * a {@code HUMANO_VS_HUMANO}.
     *
     * @param fichaJugador1   ficha del primer jugador
     * @param fichaJugador2   ficha del segundo jugador
     * @param empiezaJugador1 true si el primer jugador mueve primero
     */
    public void configurarModoHumanoVsHumano(Ficha fichaJugador1, Ficha fichaJugador2,
                                             boolean empiezaJugador1) {
        this.modo = ModoJuego.HUMANO_VS_HUMANO;
        this.fichaJugador1 = fichaJugador1;
        this.fichaJugador2 = fichaJugador2;
        this.fichaEnTurno = empiezaJugador1 ? fichaJugador1 : fichaJugador2;
    }

    /**
     * Configura la partida en modo PC vs. PC. Cambia {@link #modo} a
     * {@code PC_VS_PC}. Ambas instancias de Minimax deben compartir el
     * mismo {@link Tablero} que este controlador.
     *
     * @param algoritmoJugador1 Minimax del primer jugador (mueve primero)
     * @param algoritmoJugador2 Minimax del segundo jugador
     */
    public void configurarModoPcVsPc(Minimax algoritmoJugador1, Minimax algoritmoJugador2) {
        this.modo = ModoJuego.PC_VS_PC;
        this.algoritmoJugador1 = algoritmoJugador1;
        this.algoritmoJugador2 = algoritmoJugador2;
        this.fichaEnTurno = algoritmoJugador1.getFichaPC(); // el primero en mover
    }

    /**
     * Procesa un turno humano sobre la casilla (fila, columna). Funciona
     * tanto en {@code HUMANO_VS_PC} (donde solo juega {@link #fichaHumano})
     * como en {@code HUMANO_VS_HUMANO} (donde alterna entre
     * {@link #fichaJugador1} y {@link #fichaJugador2}); en {@code PC_VS_PC}
     * ignora cualquier click, porque ahí nadie humano juega.
     *
     * <p>Clics fuera de turno o sobre una casilla ya ocupada se ignoran
     * silenciosamente, en vez de lanzar una excepcion, porque son un
     * evento de UI esperado (el usuario puede tocar el tablero en
     * cualquier momento) y no un error de programacion.</p>
     */
    public void jugarTurnoHumano(int fila, int columna) {
        if (juegoTerminado || modo == ModoJuego.PC_VS_PC) {
            return; // en PC vs PC nadie puede clickear
        }

        Ficha fichaQueJuega = (modo == ModoJuego.HUMANO_VS_HUMANO) ? fichaEnTurno : fichaHumano;

        if (modo == ModoJuego.HUMANO_VS_PC && !turnoHumano) {
            return;
        }
        if (tablero.getCasilla(fila, columna) != Ficha.VACIA) {
            return;
        }

        tablero.llenarCasilla(fila, columna, fichaQueJuega);
        notificarJugada(fila, columna, fichaQueJuega);

        if (verificarYNotificarFinDeJuego(fichaQueJuega)) {
            return;
        }

        if (modo == ModoJuego.HUMANO_VS_HUMANO) {
            fichaEnTurno = (fichaEnTurno == fichaJugador1) ? fichaJugador2 : fichaJugador1;
        } else { // HUMANO_VS_PC
            ultimaJugadaHumanoRespuesta = new int[]{fila, columna};
            turnoHumano = false;
            realizarJugadaPC();
        }
    }

    /**
     * Le pide a Minimax la mejor jugada para la PC sobre el tablero
     * actual, la aplica y notifica el resultado. Minimax comparte la
     * misma instancia de Tablero que este controlador (ver MainActivity),
     * asi que siempre analiza el estado mas reciente sin necesidad de
     * sincronizarlo manualmente.
     *
     * <p>Solo se usa en modo {@code HUMANO_VS_PC}.</p>
     *
     * <p><b>Nota:</b> {@code turnoHumano} se actualiza <em>antes</em> de
     * notificar (a diferencia de como estaba originalmente, que lo hacía
     * después). Mientras el único observador era la vista del tablero esto
     * no importaba, pero un observador que consulte
     * {@link #isTurnoHumano()} durante {@code onJugadaRealizada(...)} (como
     * {@code PanelAyuda}, para habilitar el botón de Sugerencia) necesita
     * ver ya el valor final en ese momento, o quedaría desincronizado
     * después de la primera jugada de la PC.</p>
     */
    private void realizarJugadaPC() {
        int[] jugada = algoritmo.obtenerMejorJugada();
        if (jugada[0] == -1) {
            return; // Sin casillas disponibles; no deberia ocurrir si el juego sigue activo.
        }

        tablero.llenarCasilla(jugada[0], jugada[1], fichaPC);
        ultimaJugadaPC = jugada;
        ultimaJugadaHumanoRespuesta = null; // empieza un nuevo "camino" a partir de esta jugada
        turnoHumano = true;

        notificarJugada(jugada[0], jugada[1], fichaPC);
        verificarYNotificarFinDeJuego(fichaPC);
    }

    /**
     * Hace avanzar una partida en modo {@code PC_VS_PC}: le pide su jugada
     * al Minimax del jugador en turno, la aplica, notifica, y — si el
     * juego sigue activo — se re-programa a sí mismo con un pequeño
     * retraso para que la partida se vea jugada por jugada en vez de
     * resolverse instantáneamente.
     *
     * <p>No hace nada si el modo actual no es {@code PC_VS_PC} o el juego
     * ya terminó (esto último también corta la cadena de
     * auto-reprogramación cuando la partida concluye).</p>
     */
    public void avanzarTurnoPcVsPc() {
        if (juegoTerminado || detenido || modo != ModoJuego.PC_VS_PC) {
            return;
        }

        Minimax algoritmoActual = (fichaEnTurno == algoritmoJugador1.getFichaPC())
                ? algoritmoJugador1 : algoritmoJugador2;
        ultimoAlgoritmoPcVsPc = algoritmoActual;

        int[] jugada = algoritmoActual.obtenerMejorJugada();
        if (jugada[0] == -1) {
            return;
        }

        tablero.llenarCasilla(jugada[0], jugada[1], fichaEnTurno);
        notificarJugada(jugada[0], jugada[1], fichaEnTurno);

        if (verificarYNotificarFinDeJuego(fichaEnTurno)) {
            return;
        }

        fichaEnTurno = (fichaEnTurno == algoritmoJugador1.getFichaPC())
                ? algoritmoJugador2.getFichaPC() : algoritmoJugador1.getFichaPC();

        // Se re-programa a sí mismo tras una pausa, para que se vea jugada por jugada.
        new android.os.Handler(android.os.Looper.getMainLooper())
                .postDelayed(this::avanzarTurnoPcVsPc, 700);
    }

    /**
     * Revisa si, tras colocar {@code ultimaFicha}, el juego termino (por
     * victoria de esa ficha o por empate) y, de ser asi, notifica a los
     * observadores con el mensaje correspondiente.
     *
     * @return true si el juego termino (victoria o empate)
     */
    private boolean verificarYNotificarFinDeJuego(Ficha ultimaFicha) {
        if (tablero.verificarGanador(ultimaFicha)) {
            juegoTerminado = true;
            notificarFinDeJuego(construirMensajeVictoria(ultimaFicha));
            return true;
        }
        if (tablero.obtenerCasillasDisponibles().isEmpty()) {
            juegoTerminado = true;
            notificarFinDeJuego("Empate. Nadie logró completar una línea.");
            return true;
        }
        return false;
    }

    /**
     * Arma el mensaje de victoria según el modo de juego actual, ya que el
     * significado de "quién ganó" cambia en cada uno (el humano vs. la PC,
     * una PC vs. la otra, o un jugador vs. el otro).
     */
    private String construirMensajeVictoria(Ficha ficha) {
        switch (modo) {
            case HUMANO_VS_PC:
                return (ficha == fichaHumano) ? "¡Ganaste! Felicidades."
                        : "La computadora ha ganado esta partida.";
            case PC_VS_PC:
                return "Ganó la computadora que jugaba con " + ficha.name() + ".";
            case HUMANO_VS_HUMANO:
                return "¡Ganó el jugador con " + ficha.name() + "!";
            default:
                return "Fin del juego.";
        }
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

    public boolean isJuegoTerminado() {
        return juegoTerminado;
    }

    public List<ObservadorJuego> getObservadores() {
        return observadores;
    }

    public void setObservadores(List<ObservadorJuego> observadores) {
        this.observadores = observadores;
    }

    public ModoJuego getModo() {
        return modo;
    }

    // ---------- Métodos del Grupo B: sugerencia al humano + acceso al árbol de la PC ----------

    /**
     * Calcula cuál sería la mejor jugada para el humano si en este momento
     * fuera su turno, sin aplicarla. Usa una instancia adicional de Minimax
     * con los roles invertidos (el humano actúa como "maximizador"), por lo
     * que no requiere ningún cambio en el algoritmo de Minimax para esto.
     *
     * <p>Solo tiene sentido en modo {@code HUMANO_VS_PC}: en
     * {@code PC_VS_PC} no hay ningún humano jugando, y en
     * {@code HUMANO_VS_HUMANO} no existen los campos {@link #fichaHumano}/
     * {@link #fichaPC} con un significado válido (ambos jugadores son
     * humanos). En cualquiera de esos casos, así como si el juego ya
     * terminó o no es el turno del humano, se retorna directamente
     * {-1, -1} — el mismo valor centinela que ya usa
     * {@link Minimax#obtenerMejorJugada()} para "no hay jugada", así que
     * quien llame a este método no necesita distinguir los casos.</p>
     *
     * @return {fila, columna} de la jugada sugerida, o {-1, -1} si el modo
     *         actual no es HUMANO_VS_PC, no es el turno del humano, el
     *         juego ya terminó, o no hay casillas disponibles
     */
    public int[] obtenerSugerenciaParaHumano() {
        if (modo != ModoJuego.HUMANO_VS_PC || juegoTerminado || !turnoHumano) {
            return new int[]{-1, -1};
        }
        Minimax algoritmoSugerencia = new Minimax(tablero, fichaHumano, fichaPC);
        return algoritmoSugerencia.obtenerMejorJugada();
    }

    /**
     * Retorna el último árbol de jugadas generado por Minimax al calcular
     * una jugada de la PC.
     *
     * <p>En modo {@code HUMANO_VS_PC} viene de {@link #algoritmo}; en modo
     * {@code PC_VS_PC} viene de la instancia (jugador 1 o 2) que jugó por
     * última vez. En modo {@code HUMANO_VS_HUMANO} no hay ninguna PC
     * jugando, así que siempre retorna null.</p>
     *
     * @return el último árbol generado, o null si todavía no hay ninguno
     *         (o el modo actual no usa Minimax en absoluto)
     */
    public ArbolNario getUltimoArbolGeneradoPorPC() {
        Minimax fuente = (modo == ModoJuego.PC_VS_PC) ? ultimoAlgoritmoPcVsPc : algoritmo;
        return (fuente != null) ? fuente.getUltimoArbolGenerado() : null;
    }

    /**
     * Coordenadas {fila, columna} de la jugada que la PC realmente aplicó
     * al tablero, correspondiente al árbol devuelto por
     * {@link #getUltimoArbolGeneradoPorPC()} — es decir, cuál de los hijos
     * de la raíz del árbol es el que efectivamente se jugó.
     *
     * <p>Solo tiene un significado válido en modo {@code HUMANO_VS_PC}: en
     * {@code PC_VS_PC} el árbol mostrado puede quedar desactualizado en
     * segundos (la partida avanza sola cada 700 ms), así que resaltar un
     * "camino tomado" ahí sería engañoso; en {@code HUMANO_VS_HUMANO} no
     * hay ninguna PC jugando. En ambos casos retorna null.</p>
     *
     * @return {fila, columna} de la jugada de la PC, o null si el modo
     *         actual no es HUMANO_VS_PC o la PC todavía no ha jugado
     */
    public int[] getCaminoTomadoPC() {
        return (modo == ModoJuego.HUMANO_VS_PC) ? ultimaJugadaPC : null;
    }

    /**
     * Coordenadas {fila, columna} de la respuesta que el humano ya dio a
     * la jugada retornada por {@link #getCaminoTomadoPC()} — es decir,
     * cuál de los nietos de la raíz (bajo la rama efectivamente jugada por
     * la PC) es el que el humano efectivamente jugó después.
     *
     * @return {fila, columna} de la respuesta del humano, o null si el
     *         modo actual no es HUMANO_VS_PC, o el humano todavía no ha
     *         respondido a la última jugada de la PC (por ejemplo, porque
     *         es su turno justo ahora)
     */
    public int[] getCaminoTomadoHumano() {
        return (modo == ModoJuego.HUMANO_VS_PC) ? ultimaJugadaHumanoRespuesta : null;
    }
}
