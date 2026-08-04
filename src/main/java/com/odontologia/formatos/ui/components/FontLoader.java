package com.odontologia.formatos.ui.components;

import javafx.scene.text.Font;

import java.io.InputStream;

public final class FontLoader {

    private static final String FONT_DIR = "/fonts/";
    private static volatile boolean loaded = false;

    private FontLoader() {}

    public static void load() {
        if (loaded) return;
        synchronized (FontLoader.class) {
            if (loaded) return;
            loadFont("ibm-plex-sans-400.woff2", 14);
            loadFont("ibm-plex-sans-500.woff2", 14);
            loadFont("ibm-plex-sans-600.woff2", 14);
            loadFont("ibm-plex-mono-400.woff2", 14);
            loadFont("ibm-plex-mono-500.woff2", 14);
            loadFont("ibm-plex-mono-600.woff2", 14);
            loaded = true;
        }
    }

    private static void loadFont(String name, double size) {
        InputStream is = FontLoader.class.getResourceAsStream(FONT_DIR + name);
        if (is != null) {
            Font.loadFont(is, size);
            try { is.close(); } catch (Exception ignored) {}
        }
    }
}
