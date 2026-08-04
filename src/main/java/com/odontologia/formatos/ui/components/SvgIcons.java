package com.odontologia.formatos.ui.components;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;

import java.util.ArrayList;
import java.util.List;

public final class SvgIcons {

    private SvgIcons() {}

    private static final double STROKE_W = 1.8;

    private static Group make(double size, PathElement... elements) {
        SVGPath p = new SVGPath();
        StringBuilder sb = new StringBuilder();
        for (PathElement e : elements) {
            sb.append(e.cmd).append(' ');
            for (double v : e.args) sb.append(v).append(' ');
        }
        p.setContent(sb.toString().trim());
        p.setStrokeWidth(STROKE_W);
        p.setStrokeLineCap(StrokeLineCap.ROUND);
        p.setStrokeLineJoin(StrokeLineJoin.ROUND);
        Group g = new Group(p);
        if (size > 0) {
            double scale = size / 24.0;
            g.setScaleX(scale);
            g.setScaleY(scale);
        }
        return g;
    }

    public static Group tooth(double size) {
        return make(size,
            new PathElement("M", 12, 3.2),
            new PathElement("C", 10.1, 3.2, 8.7, 3.9, 7.6, 5.2),
            new PathElement("C", 6.1, 6.9, 5.9, 9.3, 6.3, 12),
            new PathElement("C", 6.8, 14.5, 7.3, 16.9, 7.7, 19.1),
            new PathElement("C", 7.9, 20.1, 8.3, 21, 9.2, 21),
            new PathElement("C", 10, 21, 10.3, 20.1, 10.5, 19.1),
            new PathElement("C", 10.8, 17.8, 11.2, 16.6, 12, 15.7),
            new PathElement("C", 12.8, 14.8, 13.3, 14.8, 14, 15.7),
            new PathElement("C", 14.8, 16.6, 15.2, 17.8, 15.5, 19.1),
            new PathElement("C", 15.7, 20.1, 16, 21, 16.8, 21),
            new PathElement("C", 17.7, 21, 18.1, 20.1, 18.3, 19.1),
            new PathElement("C", 18.7, 16.9, 19.2, 14.5, 19.7, 12),
            new PathElement("C", 20.1, 9.3, 19.9, 6.9, 18.4, 5.2),
            new PathElement("C", 17.3, 3.9, 15.9, 3.2, 14, 3.2),
            new PathElement("Z")
        );
    }

    public static Group dashboard(double size) {
        return make(size,
            new PathElement("M", 22, 12), new PathElement("L", 18, 12), new PathElement("L", 15, 21),
            new PathElement("L", 9, 3), new PathElement("L", 6, 12), new PathElement("L", 2, 12)
        );
    }

    public static Group catalogo(double size) {
        return make(size,
            new PathElement("M", 2, 3), new PathElement("H", 8), new PathElement("A", 4, 4, 0, 0, 1, 12, 7),
            new PathElement("V", 21), new PathElement("A", 3, 3, 0, 0, 0, 9, 18), new PathElement("H", 2), new PathElement("Z"),
            new PathElement("M", 22, 3), new PathElement("H", 16), new PathElement("A", 4, 4, 0, 0, 0, 12, 7),
            new PathElement("V", 21), new PathElement("A", 3, 3, 0, 0, 1, 15, 18), new PathElement("H", 22), new PathElement("Z")
        );
    }

    public static Group modulos(double size) {
        return make(size,
            new PathElement("M", 3, 3), new PathElement("H", 10), new PathElement("V", 10), new PathElement("H", 3), new PathElement("Z"),
            new PathElement("M", 14, 3), new PathElement("H", 21), new PathElement("V", 10), new PathElement("H", 14), new PathElement("Z"),
            new PathElement("M", 14, 14), new PathElement("H", 21), new PathElement("V", 21), new PathElement("H", 14), new PathElement("Z"),
            new PathElement("M", 3, 14), new PathElement("H", 10), new PathElement("V", 21), new PathElement("H", 3), new PathElement("Z")
        );
    }

    public static Group docente(double size) {
        return make(size,
            new PathElement("M", 16, 4), new PathElement("H", 18), new PathElement("A", 2, 2, 0, 0, 1, 20, 6),
            new PathElement("V", 20), new PathElement("A", 2, 2, 0, 0, 1, 18, 22), new PathElement("H", 6),
            new PathElement("A", 2, 2, 0, 0, 1, 4, 20), new PathElement("V", 6), new PathElement("A", 2, 2, 0, 0, 1, 6, 4),
            new PathElement("H", 8),
            new PathElement("M", 8, 2), new PathElement("H", 16), new PathElement("V", 6), new PathElement("H", 8), new PathElement("Z"),
            new PathElement("M", 9, 14), new PathElement("L", 11, 16), new PathElement("L", 15, 12)
        );
    }

    public static Group reportes(double size) {
        return make(size,
            new PathElement("M", 14, 2), new PathElement("H", 6), new PathElement("A", 2, 2, 0, 0, 0, 4, 4),
            new PathElement("V", 20), new PathElement("A", 2, 2, 0, 0, 0, 6, 22), new PathElement("H", 18),
            new PathElement("A", 2, 2, 0, 0, 0, 20, 20), new PathElement("V", 8), new PathElement("Z"),
            new PathElement("M", 14, 2), new PathElement("V", 8), new PathElement("H", 20),
            new PathElement("M", 16, 13), new PathElement("H", 8),
            new PathElement("M", 16, 17), new PathElement("H", 8),
            new PathElement("M", 10, 9), new PathElement("H", 8)
        );
    }

    public static Group asistencia(double size) {
        return make(size,
            new PathElement("M", 13.5, 20), new PathElement("V", 18), new PathElement("A", 3.5, 3.5, 0, 0, 0, 10, 14.5),
            new PathElement("H", 7), new PathElement("A", 3.5, 3.5, 0, 0, 0, 3.5, 18), new PathElement("V", 20),
            new PathElement("M", 9, 6.5), new PathElement("A", 3.5, 3.5, 0, 1, 0, 9, 13.5), new PathElement("A", 3.5, 3.5, 0, 1, 0, 9, 6.5),
            new PathElement("M", 16, 10.5), new PathElement("L", 18.5, 13), new PathElement("L", 22.5, 8)
        );
    }

    public static Group plus(double size) {
        return make(size, new PathElement("M", 12, 5), new PathElement("V", 19), new PathElement("M", 5, 12), new PathElement("H", 19));
    }

    public static Group check(double size) {
        return make(size, new PathElement("M", 20, 6), new PathElement("L", 9, 17), new PathElement("L", 4, 12));
    }

    public static Group close(double size) {
        return make(size, new PathElement("M", 18, 6), new PathElement("L", 6, 18), new PathElement("M", 6, 6), new PathElement("L", 18, 18));
    }

    public static Group search(double size) {
        return make(size, new PathElement("M", 11, 11), new PathElement("A", 8, 8, 0, 1, 1, 21.66, 21.66), new PathElement("L", 21, 21), new PathElement("L", 16.65, 16.65));
    }

    public static Group info(double size) {
        return make(size, new PathElement("M", 12, 22), new PathElement("A", 10, 10, 0, 1, 1, 22, 12),
            new PathElement("A", 10, 10, 0, 1, 1, 12, 22), new PathElement("M", 12, 16), new PathElement("V", 12),
            new PathElement("M", 12, 8), new PathElement("H", 12.01));
    }

    public static Group warning(double size) {
        return make(size, new PathElement("M", 10.29, 3.86), new PathElement("L", 1.82, 18),
            new PathElement("A", 2, 2, 0, 0, 0, 3.53, 21), new PathElement("H", 20.47),
            new PathElement("A", 2, 2, 0, 0, 0, 22.18, 18), new PathElement("L", 13.71, 3.86),
            new PathElement("A", 2, 2, 0, 0, 0, 10.29, 3.86), new PathElement("M", 12, 9), new PathElement("V", 13),
            new PathElement("M", 12, 17), new PathElement("H", 12.01));
    }

    public static Group trash(double size) {
        return make(size, new PathElement("M", 3, 6), new PathElement("H", 5), new PathElement("H", 21),
            new PathElement("M", 19, 6), new PathElement("V", 20), new PathElement("A", 2, 2, 0, 0, 1, 17, 22),
            new PathElement("H", 7), new PathElement("A", 2, 2, 0, 0, 1, 5, 20), new PathElement("V", 6),
            new PathElement("M", 8, 6), new PathElement("V", 4), new PathElement("A", 2, 2, 0, 0, 1, 10, 2),
            new PathElement("H", 14), new PathElement("A", 2, 2, 0, 0, 1, 16, 4), new PathElement("V", 6),
            new PathElement("M", 10, 11), new PathElement("V", 17), new PathElement("M", 14, 11), new PathElement("V", 17));
    }

    public static Group user(double size) {
        return make(size, new PathElement("M", 20, 21), new PathElement("V", 19),
            new PathElement("A", 4, 4, 0, 0, 0, 16, 15), new PathElement("H", 8),
            new PathElement("A", 4, 4, 0, 0, 0, 4, 19), new PathElement("V", 21),
            new PathElement("M", 12, 11), new PathElement("A", 4, 4, 0, 1, 0, 12, 3), new PathElement("A", 4, 4, 0, 1, 0, 12, 11));
    }

    public static Group download(double size) {
        return make(size, new PathElement("M", 21, 15), new PathElement("V", 19),
            new PathElement("A", 2, 2, 0, 0, 1, 19, 21), new PathElement("H", 5),
            new PathElement("A", 2, 2, 0, 0, 1, 3, 19), new PathElement("V", 15),
            new PathElement("M", 7, 10), new PathElement("L", 12, 15), new PathElement("L", 17, 10),
            new PathElement("M", 12, 15), new PathElement("V", 3));
    }

    public static Group folder(double size) {
        return make(size, new PathElement("M", 22, 19), new PathElement("A", 2, 2, 0, 0, 1, 20, 21),
            new PathElement("H", 4), new PathElement("A", 2, 2, 0, 0, 1, 2, 19), new PathElement("V", 5),
            new PathElement("A", 2, 2, 0, 0, 1, 4, 3), new PathElement("H", 9), new PathElement("L", 11, 6),
            new PathElement("H", 20), new PathElement("A", 2, 2, 0, 0, 1, 22, 8), new PathElement("Z"));
    }

    public static Group clock(double size) {
        return make(size, new PathElement("M", 12, 22), new PathElement("A", 10, 10, 0, 1, 1, 22, 12),
            new PathElement("A", 10, 10, 0, 1, 1, 12, 22), new PathElement("M", 12, 6), new PathElement("V", 12),
            new PathElement("L", 16, 14));
    }

    public static Group chevronDown(double size) {
        return make(size, new PathElement("M", 6, 9), new PathElement("L", 12, 15), new PathElement("L", 18, 9));
    }

    public static Group pencil(double size) {
        return make(size, new PathElement("M", 17, 3), new PathElement("A", 2.828, 2.828, 0, 1, 1, 21, 7),
            new PathElement("L", 7.5, 20.5), new PathElement("L", 2, 22), new PathElement("L", 3.5, 16.5), new PathElement("L", 17, 3), new PathElement("Z"));
    }

    public static Group gear(double size) {
        return make(size, new PathElement("M", 12, 15), new PathElement("A", 3, 3, 0, 1, 0, 12, 9), new PathElement("A", 3, 3, 0, 1, 0, 12, 15),
            new PathElement("M", 19.4, 15), new PathElement("A", 1.65, 1.65, 0, 0, 0, 19.73, 16.82),
            new PathElement("L", 19.79, 16.88), new PathElement("A", 2, 2, 0, 0, 1, 19.79, 19.71),
            new PathElement("A", 2, 2, 0, 0, 1, 16.96, 19.71), new PathElement("L", 16.9, 19.65),
            new PathElement("A", 1.65, 1.65, 0, 0, 0, 15.08, 19.32), new PathElement("A", 1.65, 1.65, 0, 0, 0, 14.08, 20.83),
            new PathElement("V", 21), new PathElement("A", 2, 2, 0, 0, 1, 12.08, 23), new PathElement("A", 2, 2, 0, 0, 1, 10.08, 23),
            new PathElement("V", 22.91), new PathElement("A", 1.65, 1.65, 0, 0, 0, 9, 21.4),
            new PathElement("A", 1.65, 1.65, 0, 0, 0, 7.18, 21.73), new PathElement("L", 7.12, 21.79),
            new PathElement("A", 2, 2, 0, 0, 1, 4.29, 21.79), new PathElement("A", 2, 2, 0, 0, 1, 4.29, 18.96),
            new PathElement("L", 4.35, 18.9), new PathElement("A", 1.65, 1.65, 0, 0, 0, 4.68, 17.08),
            new PathElement("A", 1.65, 1.65, 0, 0, 0, 3.17, 16.08), new PathElement("H", 3),
            new PathElement("A", 2, 2, 0, 0, 1, 1, 14.08), new PathElement("A", 2, 2, 0, 0, 1, 3, 12.08),
            new PathElement("H", 3.09), new PathElement("A", 1.65, 1.65, 0, 0, 0, 4.6, 11),
            new PathElement("A", 1.65, 1.65, 0, 0, 0, 4.27, 9.18), new PathElement("L", 4.21, 9.12),
            new PathElement("A", 2, 2, 0, 0, 1, 4.21, 6.29), new PathElement("A", 2, 2, 0, 0, 1, 7.04, 6.29),
            new PathElement("L", 7.1, 6.35), new PathElement("A", 1.65, 1.65, 0, 0, 0, 8.92, 6.68),
            new PathElement("H", 9), new PathElement("A", 1.65, 1.65, 0, 0, 0, 10, 5.17), new PathElement("V", 3),
            new PathElement("A", 2, 2, 0, 0, 1, 12, 1), new PathElement("A", 2, 2, 0, 0, 1, 14, 3),
            new PathElement("V", 3.09), new PathElement("A", 1.65, 1.65, 0, 0, 0, 15, 4.51),
            new PathElement("A", 1.65, 1.65, 0, 0, 0, 16.82, 4.18), new PathElement("L", 16.88, 4.12),
            new PathElement("A", 2, 2, 0, 0, 1, 19.71, 4.12), new PathElement("A", 2, 2, 0, 0, 1, 19.71, 6.95),
            new PathElement("L", 19.65, 7.01), new PathElement("A", 1.65, 1.65, 0, 0, 0, 19.32, 8.83),
            new PathElement("V", 9), new PathElement("A", 1.65, 1.65, 0, 0, 0, 20.83, 10), new PathElement("H", 21),
            new PathElement("A", 2, 2, 0, 0, 1, 23, 12), new PathElement("A", 2, 2, 0, 0, 1, 21, 14),
            new PathElement("H", 20.91), new PathElement("A", 1.65, 1.65, 0, 0, 0, 19.4, 15));
    }

    public static Group arrowLeft(double size) {
        return make(size, new PathElement("M", 19, 12), new PathElement("H", 5),
            new PathElement("M", 12, 19), new PathElement("L", 5, 12), new PathElement("L", 12, 5));
    }

    private record PathElement(String cmd, double... args) {}
}
