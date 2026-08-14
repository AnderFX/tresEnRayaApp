package com.example.commycompanytresenrayaapp.vista;

import android.app.AlertDialog;
import android.content.Context;
import android.view.ViewGroup;
import android.webkit.WebView;

import java.util.ArrayList;
import java.util.List;

import com.example.commycompanytresenrayaapp.modelo.ArbolNario;
import com.example.commycompanytresenrayaapp.modelo.Ficha;
import com.example.commycompanytresenrayaapp.modelo.NodoJugada;
import com.example.commycompanytresenrayaapp.modelo.Tablero;

public class VistaArbolDialog {

    private final Context context;
    private final ArbolNario arbol;

    /**
     * {fila, columna} de la jugada que la PC realmente aplicó (el hijo de
     * la raíz que corresponde al camino tomado), o null si no se conoce
     * (por ejemplo, en modo PC_VS_PC, donde no se resalta ningún camino).
     */
    private final int[] caminoPC;

    /**
     * {fila, columna} de la respuesta que el humano ya dio a esa jugada,
     * o null si todavía no respondió (es su turno justo ahora) o no se
     * conoce.
     */
    private final int[] caminoHumano;

    public VistaArbolDialog(Context context, ArbolNario arbol, int[] caminoPC, int[] caminoHumano) {
        this.context = context;
        this.arbol = arbol;
        this.caminoPC = caminoPC;
        this.caminoHumano = caminoHumano;
    }

    public void mostrar() {
        WebView webView = new WebView(context);
        webView.getSettings().setJavaScriptEnabled(true);       // Mermaid necesita JS
        webView.getSettings().setBuiltInZoomControls(true);     // el árbol puede ser grande
        webView.getSettings().setDisplayZoomControls(false);    // zoom con gestos, sin botones +/-
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLoadWithOverviewMode(true);

        int altoPx = (int) (500 * context.getResources().getDisplayMetrics().density);
        webView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, altoPx));

        String html = generarHtmlMermaid();
        // "file:///android_asset/" como baseURL permite que el HTML encuentre
        // mermaid.min.js dentro de app/src/main/assets/ por su nombre simple.
        webView.loadDataWithBaseURL("file:///android_asset/", html, "text/html", "utf-8", null);

        new AlertDialog.Builder(context)
                .setTitle("Árbol de decisión (minimax)")
                .setView(webView)
                .setPositiveButton("Cerrar", null)
                .show();
    }

    /** Arma el documento HTML completo que Mermaid necesita para dibujar. */
    private String generarHtmlMermaid() {
        StringBuilder definicion = new StringBuilder("graph TD\n");
        List<String> idsEnCamino = new ArrayList<>();

        // La raíz siempre está "en el camino" (representa el tablero tal
        // como estaba antes de la jugada de la PC); el objetivo para sus
        // hijos es encontrar cuál de ellos coincide con caminoPC.
        construirMermaid(arbol.getRaiz(), definicion, "N0", new int[]{0},
                true, idsEnCamino, caminoPC);

        if (!idsEnCamino.isEmpty()) {
            definicion.append("classDef caminoTomado fill:#FFD54F,stroke:#F57F17,stroke-width:3px;\n");
            definicion.append("class ").append(String.join(",", idsEnCamino)).append(" caminoTomado;\n");
        }

        // Leyenda: explica una sola vez la notación de cada nodo, en vez de
        // esperar que se entienda "(1,0) u=1" sin contexto.
        StringBuilder leyenda = new StringBuilder(
                "<p style=\"font-size:12px;color:#555;margin-top:6px;line-height:1.5;\">");
        if (!idsEnCamino.isEmpty()) {
            leyenda.append("🟨 Resaltado: camino realmente jugado hasta ahora.<br/>");
        }
        leyenda.append("Cada casilla muestra el tablero de ese nodo; ")
                .append("[X] o [O] entre corchetes es la jugada que se acaba de colocar en ese paso.<br/>")
                .append("u = líneas disponibles para la PC − líneas disponibles para el humano ")
                .append("(fórmula del enunciado); mientras más alto, mejor para la PC.</p>");

        return "<html><head>"
                + "<script src=\"mermaid.min.js\"></script>"
                + "<style>body{margin:0;padding:8px;font-family:sans-serif;}"
                + ".mermaid{font-family:monospace;}</style>"
                + "</head><body>"
                + "<pre class=\"mermaid\">\n" + definicion + "\n</pre>"
                + leyenda
                + "<script>mermaid.initialize({startOnLoad:true});</script>"
                + "</body></html>";
    }

    /**
     * Recorre el árbol y genera la sintaxis de Mermaid ("graph TD"): una
     * línea por nodo (con su etiqueta) y una línea "idPadre --> idHijo"
     * por cada conexión. contador[] asegura que cada nodo tenga un id
     * único (N0, N1, N2, ...) sin importar la forma del árbol.
     *
     * <p>Además, va identificando qué nodos forman parte del "camino
     * tomado" (la secuencia de jugadas que realmente ocurrieron), para
     * poder resaltarlos después. La lógica es: un hijo está en el camino
     * si su padre lo está Y sus coordenadas (fila, columna) coinciden con
     * {@code objetivoHijos} — el objetivo esperado en este nivel. Ese
     * objetivo es {@code caminoPC} para los hijos de la raíz (nivel 1,
     * jugadas de la PC) y {@code caminoHumano} para los hijos del nodo de
     * la PC que sí coincidió (nivel 2, respuesta del humano). Con la
     * profundidad actual del árbol (2 niveles) no hace falta ir más allá;
     * si el árbol creciera, este esquema necesitaría un objetivo adicional
     * por cada nivel nuevo.</p>
     *
     * @param enCamino     true si {@code nodo} mismo ya forma parte del camino tomado
     * @param idsEnCamino  lista donde se acumulan los ids de los nodos en el camino
     * @param objetivoHijos coordenadas {fila, columna} que debe tener un hijo de
     *                      {@code nodo} para seguir en el camino, o null si no hay
     *                      ninguna jugada real conocida en este nivel (el camino
     *                      "se corta" aquí: nada más abajo se resalta)
     */
    private void construirMermaid(NodoJugada nodo, StringBuilder sb, String idNodo, int[] contador,
                                  boolean enCamino, List<String> idsEnCamino, int[] objetivoHijos) {
        if (enCamino) {
            idsEnCamino.add(idNodo);
        }

        sb.append(idNodo).append("[\"").append(construirEtiquetaTablero(nodo)).append("\"]\n");

        for (NodoJugada hijo : nodo.getHijos()) {
            contador[0]++;
            String idHijo = "N" + contador[0];
            sb.append(idNodo).append(" --> ").append(idHijo).append("\n");

            boolean hijoEnCamino = enCamino && objetivoHijos != null
                    && hijo.getFilaJugada() == objetivoHijos[0]
                    && hijo.getColumnaJugada() == objetivoHijos[1];

            // El objetivo para los NIETOS de este nodo (si hijo sí está en
            // el camino) es caminoHumano: la única jugada real que se
            // conoce para el nivel siguiente al de la PC.
            int[] objetivoNietos = hijoEnCamino ? caminoHumano : null;

            construirMermaid(hijo, sb, idHijo, contador, hijoEnCamino, idsEnCamino, objetivoNietos);
        }
    }

    /**
     * Arma la etiqueta de un nodo como una miniatura del tablero real en
     * ese punto del árbol (3 líneas de 3 casillas, separadas con
     * {@code <br/>} para que Mermaid las dibuje en filas dentro de la
     * misma caja), en vez de mostrar solo las coordenadas crudas de la
     * jugada — mucho más fácil de leer de un vistazo.
     *
     * <p>La casilla que corresponde a la jugada de este nodo específico
     * (la que lo diferencia de su padre) se marca entre corchetes, por
     * ejemplo {@code [X]}, para distinguirla de las fichas que ya estaban
     * puestas desde niveles anteriores. La raíz no tiene jugada propia
     * (representa el tablero tal como está ahora mismo), así que ninguna
     * casilla se marca ahí.</p>
     */
    private String construirEtiquetaTablero(NodoJugada nodo) {
        Tablero estado = nodo.getEstado();
        boolean esRaiz = nodo.getFilaJugada() == -1;

        StringBuilder etiqueta = new StringBuilder();
        if (esRaiz) {
            etiqueta.append("Tablero actual<br/>");
        }

        for (int fila = 0; fila < 3; fila++) {
            if (fila > 0) {
                etiqueta.append("<br/>");
            }
            for (int columna = 0; columna < 3; columna++) {
                if (columna > 0) {
                    etiqueta.append(" ");
                }
                String simbolo = simboloCasilla(estado.getCasilla(fila, columna));
                boolean esLaJugadaDeEsteNodo = !esRaiz
                        && fila == nodo.getFilaJugada()
                        && columna == nodo.getColumnaJugada();
                etiqueta.append(esLaJugadaDeEsteNodo ? "[" + simbolo + "]" : simbolo);
            }
        }

        etiqueta.append("<br/>u = ").append(nodo.getUtilidad());
        return etiqueta.toString();
    }

    private String simboloCasilla(Ficha ficha) {
        switch (ficha) {
            case X:
                return "X";
            case O:
                return "O";
            default:
                return "·";
        }
    }
}
