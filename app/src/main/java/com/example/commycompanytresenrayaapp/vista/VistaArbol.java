package com.example.commycompanytresenrayaapp.vista;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;
import android.view.ViewParent;
import android.widget.HorizontalScrollView;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.example.commycompanytresenrayaapp.controlador.ControladorJuego;
import com.example.commycompanytresenrayaapp.controlador.ObservadorJuego;
import com.example.commycompanytresenrayaapp.modelo.Ficha;
import com.example.commycompanytresenrayaapp.modelo.NodoJugada;

public class VistaArbol extends View implements ObservadorJuego {

    private static final int ESPACIADO_HORIZONTAL_DP = 70;
    private static final int ESPACIADO_VERTICAL_DP = 100;
    private static final int RADIO_NODO_DP = 22;
    private static final int MARGEN_SUPERIOR_DP = 12;

    private static final int COLOR_RELLENO_X = Color.parseColor("#C8E6C9");
    private static final int COLOR_BORDE_X = Color.parseColor("#2E7D32");
    private static final int COLOR_RELLENO_O = Color.parseColor("#FFE0B2");
    private static final int COLOR_BORDE_O = Color.parseColor("#EF6C00");
    private static final int COLOR_RELLENO_INICIO = Color.parseColor("#CFD8DC");
    private static final int COLOR_BORDE_CAMINO = Color.parseColor("#455A64");
    private static final int COLOR_RELLENO_ANALISIS = Color.parseColor("#FAFAFA");
    private static final int COLOR_BORDE_ANALISIS = Color.parseColor("#BDBDBD");
    private static final int COLOR_TEXTO_ANALISIS = Color.parseColor("#9E9E9E");

    private final ControladorJuego controlador;
    private final Map<NodoJugada, Float> anchoCache = new HashMap<>();
    private final Map<NodoJugada, Float> posicionX = new HashMap<>();
    private final Map<NodoJugada, Integer> profundidadNodo = new HashMap<>();
    private final Set<NodoJugada> caminoReal = new HashSet<>();
    private final Paint lineaPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint rellenoPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bordePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textoPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private NodoJugada raiz;
    private int profundidadMaxima;
    private int desplazamientoX;

    public VistaArbol(Context context, ControladorJuego controlador) {
        super(context);
        this.controlador = controlador;
        rellenoPaint.setStyle(Paint.Style.FILL);
        bordePaint.setStyle(Paint.Style.STROKE);
        textoPaint.setTextSize(dpToPx(10));
        textoPaint.setTextAlign(Paint.Align.CENTER);
    }

    public void actualizar(NodoJugada nuevaRaiz) {
        raiz = nuevaRaiz;
        anchoCache.clear();
        posicionX.clear();
        profundidadNodo.clear();
        caminoReal.clear();
        caminoReal.addAll(controlador.getCaminoReal());
        profundidadMaxima = 0;
        if (raiz != null) {
            calcularAncho(raiz);
            asignarPosiciones(raiz, 0f, 0);
        }
        requestLayout();
        invalidate();
        post(this::centrarNodoInicial);
    }

    private void centrarNodoInicial() {
        ViewParent padre = getParent();
        Float posicionRaiz = (raiz != null) ? posicionX.get(raiz) : null;
        if (posicionRaiz == null || !(padre instanceof HorizontalScrollView)) {
            return;
        }
        HorizontalScrollView scroll = (HorizontalScrollView) padre;
        if (scroll.getWidth() == 0) {
            return;
        }
        int xRaiz = desplazamientoX + Math.round(posicionRaiz * dpToPx(ESPACIADO_HORIZONTAL_DP));
        scroll.scrollTo(Math.max(0, xRaiz - scroll.getWidth() / 2), 0);
    }

    private float calcularAncho(NodoJugada nodo) {
        float ancho;
        if (nodo.getHijos().isEmpty()) {
            ancho = 1f;
        } else {
            ancho = 0f;
            for (NodoJugada hijo : nodo.getHijos()) {
                ancho += calcularAncho(hijo);
            }
        }
        anchoCache.put(nodo, ancho);
        return ancho;
    }

    private void asignarPosiciones(NodoJugada nodo, float offsetUnidades, int profundidad) {
        posicionX.put(nodo, offsetUnidades + anchoCache.get(nodo) / 2f);
        profundidadNodo.put(nodo, profundidad);
        profundidadMaxima = Math.max(profundidadMaxima, profundidad);

        float offsetHijo = offsetUnidades;
        for (NodoJugada hijo : nodo.getHijos()) {
            asignarPosiciones(hijo, offsetHijo, profundidad + 1);
            offsetHijo += anchoCache.get(hijo);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Float anchoRaiz = (raiz != null) ? anchoCache.get(raiz) : null;
        float anchoUnidades = (anchoRaiz != null) ? anchoRaiz : 1f;
        int nivelesDeProfundidad = (raiz != null) ? profundidadMaxima + 1 : 1;
        int anchoContenido = Math.round(anchoUnidades * dpToPx(ESPACIADO_HORIZONTAL_DP)) + dpToPx(RADIO_NODO_DP) * 2;
        int altoDeseado = nivelesDeProfundidad * dpToPx(ESPACIADO_VERTICAL_DP) + dpToPx(RADIO_NODO_DP) * 2
                + dpToPx(MARGEN_SUPERIOR_DP);

        int anchoFinal = anchoContenido;
        if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.EXACTLY) {
            anchoFinal = MeasureSpec.getSize(widthMeasureSpec);
        }
        desplazamientoX = Math.max(0, (anchoFinal - anchoContenido) / 2);
        setMeasuredDimension(anchoFinal, altoDeseado);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (raiz == null) {
            return;
        }
        canvas.save();
        canvas.translate(desplazamientoX, 0);
        dibujarNodo(canvas, raiz);
        canvas.restore();
    }

    private void dibujarNodo(Canvas canvas, NodoJugada nodo) {
        float radioPx = dpToPx(RADIO_NODO_DP);
        float margenSuperiorPx = dpToPx(MARGEN_SUPERIOR_DP);
        float x = posicionX.get(nodo) * dpToPx(ESPACIADO_HORIZONTAL_DP);
        float y = profundidadNodo.get(nodo) * dpToPx(ESPACIADO_VERTICAL_DP) + radioPx + margenSuperiorPx;
        boolean nodoJugado = caminoReal.contains(nodo);

        for (NodoJugada hijo : nodo.getHijos()) {
            float xHijo = posicionX.get(hijo) * dpToPx(ESPACIADO_HORIZONTAL_DP);
            float yHijo = profundidadNodo.get(hijo) * dpToPx(ESPACIADO_VERTICAL_DP) + radioPx + margenSuperiorPx;
            boolean tramoJugado = nodoJugado && caminoReal.contains(hijo);
            lineaPaint.setColor(tramoJugado ? COLOR_BORDE_CAMINO : COLOR_BORDE_ANALISIS);
            lineaPaint.setStrokeWidth(dpToPx(tramoJugado ? 3 : 1));
            canvas.drawLine(x, y, xHijo, yHijo, lineaPaint);
        }

        aplicarColores(nodo, nodoJugado);
        canvas.drawCircle(x, y, radioPx, rellenoPaint);
        canvas.drawCircle(x, y, radioPx, bordePaint);
        canvas.drawText(textoNodo(nodo), x, y + textoPaint.getTextSize() / 3f, textoPaint);

        for (NodoJugada hijo : nodo.getHijos()) {
            dibujarNodo(canvas, hijo);
        }
    }

    private void aplicarColores(NodoJugada nodo, boolean nodoJugado) {
        bordePaint.setStrokeWidth(dpToPx(nodoJugado ? 3 : 1));

        if (!nodoJugado) {
            rellenoPaint.setColor(COLOR_RELLENO_ANALISIS);
            bordePaint.setColor(COLOR_BORDE_ANALISIS);
            textoPaint.setColor(COLOR_TEXTO_ANALISIS);
            return;
        }
        if (nodo.getFilaJugada() == -1) {
            rellenoPaint.setColor(COLOR_RELLENO_INICIO);
            bordePaint.setColor(COLOR_BORDE_CAMINO);
            textoPaint.setColor(Color.BLACK);
            return;
        }

        boolean esX = fichaDe(nodo) == Ficha.X;
        rellenoPaint.setColor(esX ? COLOR_RELLENO_X : COLOR_RELLENO_O);
        bordePaint.setColor(esX ? COLOR_BORDE_X : COLOR_BORDE_O);
        textoPaint.setColor(Color.BLACK);
    }

    private Ficha fichaDe(NodoJugada nodo) {
        return nodo.getEstado().getCasilla(nodo.getFilaJugada(), nodo.getColumnaJugada());
    }

    private String textoNodo(NodoJugada nodo) {
        if (nodo.getFilaJugada() == -1) {
            return "Inicio";
        }
        return fichaDe(nodo).name() + "(" + nodo.getFilaJugada() + "," + nodo.getColumnaJugada()
                + ") u=" + nodo.getUtilidad();
    }

    @Override
    public void onJugadaRealizada(int fila, int columna, Ficha ficha) {
        actualizar(controlador.getRaizArbolPartida());
    }

    @Override
    public void onJuegoTerminado(String mensaje) {
        actualizar(controlador.getRaizArbolPartida());
    }

    @Override
    public void onCambioDeTurno(Ficha fichaEnTurno) {
        actualizar(controlador.getRaizArbolPartida());
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
