# Tres en Raya vs. Computadora (Android App)

Proyecto del curso de Estructuras de Datos. La computadora decide su jugada usando el algoritmo minimax sobre un árbol que construimos nosotros mismos (árbol n-ario), sin usar ninguna colección no lineal de las librerías estándar de Java.

## Qué implementamos

- **Tablero**: guarda el estado de las 9 casillas y detecta si alguien ganó o si hay empate.
- **Árbol n-ario** (`NodoJugada` + `ArbolNario`): representa las jugadas futuras posibles a partir de un tablero.
- **Minimax**: recorre ese árbol y elige la mejor jugada para la computadora.
- **Ficha** (enum `X`, `O`, `VACIA`): para no usar números sueltos en las casillas que puedan confundirse.

## Arquitectura y Diagrama de Clases

Organizamos el código bajo el patrón **Modelo-Vista-Controlador (MVC)** apoyado en el **Patrón Observer**. El `MainActivity` actúa como el ensamblador (punto de entrada) que conecta la lógica con la vista de Android.

```mermaid
classDiagram
    class Ficha {
        <<enumeration>>
        X
        O
        VACIA
    }
    class Tablero {
        - Ficha[][] casillas
        + Tablero()
        + llenarCasilla(int fila, int columna, Ficha ficha) void
        + deshacerCasilla(int fila, int columna) void
        + obtenerCasillasDisponibles() List~int[]~
        + verificarGanador(Ficha ficha) boolean
        + contarLineasDisponibles(Ficha ficha) int
        + clonar() Tablero
    }
    class NodoJugada {
        - Tablero estado
        - List~NodoJugada~ hijos
        - int utilidad
        - int filaJugada
        - int columnaJugada
        + NodoJugada(Tablero estado, int fila, int columna)
        + agregarHijo(NodoJugada hijo) void
        + esHoja() boolean
    }
    class ArbolNario {
        - NodoJugada raiz
        + ArbolNario()
        + getRaiz() NodoJugada
        + setRaiz(NodoJugada raiz) void
    }
    class Minimax {
        - Tablero tableroActual
        + generarArbolDeJuego(ArbolNario arbol, int profundidad) void
        + generarArbolDeJuegoPara(ArbolNario arbol, int profundidad, Ficha fichaQueDecide) void
        + minimax(NodoJugada nodoActual, int profundidad, boolean esTurnoDeQuienDecide, Ficha fichaQueDecide) int
        + obtenerMejorJugada() int[]
        + obtenerMejorJugadaPara(Ficha fichaQueDecide) int[]
    }
    class ControladorJuego {
        - Tablero tablero
        - Minimax algoritmo
        - Ficha fichaHumano
        - Ficha fichaPC
        - ModoJuego modoJuego
        - Ficha fichaEnTurno
        - List~ObservadorJuego~ observadores
        + ControladorJuego(Tablero tablero, Minimax algoritmo)
        + iniciarJuego() void
        + agregarObservador(ObservadorJuego obs) void
        + jugarTurno(int fila, int columna) void
        + obtenerSugerenciaParaTurnoActual() int[]
        + realizarJugadaPC() void
        - notificarJugada(int fila, int columna, Ficha ficha) void
        - notificarFinDeJuego(String mensaje) void
        - notificarCambioDeTurno() void
    }
    class ModoJuego {
        <<enumeration>>
        CONTRA_PC
        DOS_HUMANOS
    }
    class ObservadorJuego {
        <<interface>>
        + onJugadaRealizada(int fila, int columna, Ficha ficha) void
        + onJuegoTerminado(String mensaje) void
        + onCambioDeTurno(Ficha fichaEnTurno) void
    }
    class PantallaJuego {
        - ControladorJuego controlador
        - Button[][] botones
        + inicializarUI() void
        + onBotonClic(int fila, int columna) void
        + setInteractivo(boolean interactivo) void
        + onJugadaRealizada(int fila, int columna, Ficha ficha) void
        + onJuegoTerminado(String mensaje) void
    }
    class MainActivity {
        <<Activity>>
        # onCreate(Bundle) void
    }

    PantallaJuego ..|> ObservadorJuego : Implementa
    MainActivity ..|> ObservadorJuego : Implementa
    PantallaJuego --> ControladorJuego : Delega eventos
    ControladorJuego o-- ObservadorJuego : Notifica
    ControladorJuego --> ModoJuego : Usa
    ControladorJuego --> Tablero : Manipula
    ControladorJuego --> Minimax : Delega IA
    ArbolNario o-- NodoJugada : Contiene
    NodoJugada --> Tablero : Mantiene estado
    Minimax --> Tablero : Evalúa
    Tablero ..> Ficha : Depende
    ControladorJuego ..> Ficha : Depende
    ObservadorJuego ..> Ficha : Depende
    
    %% Ensamblaje en Android
    MainActivity --> Tablero : Instancia
    MainActivity --> Minimax : Instancia
    MainActivity --> ControladorJuego : Instancia
    MainActivity --> PantallaJuego : Instancia e incrusta en UI
