package com.odontologia.formatos.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Limitador de frecuencia simple de ventana fija (auditoría 2.8).
 * Protege endpoints costosos (reportes/dashboard) de abuso sin
 * dependencias externas.
 */
public final class RateLimiter {

    private final int maxRequests;
    private final long windowMillis;
    private final Map<String, Window> ventanas = new ConcurrentHashMap<>();

    public RateLimiter(int maxRequests, long windowMillis) {
        this.maxRequests = maxRequests;
        this.windowMillis = windowMillis;
    }

    public boolean permitir(String clave) {
        long ahora = System.currentTimeMillis();
        Window w = ventanas.compute(clave, (k, prev) -> {
            if (prev == null || ahora - prev.inicio >= windowMillis) {
                return new Window(ahora, 1);
            }
            prev.contador++;
            return prev;
        });
        return w.contador <= maxRequests;
    }

    private static final class Window {
        final long inicio;
        int contador;

        Window(long inicio, int contador) {
            this.inicio = inicio;
            this.contador = contador;
        }
    }
}
