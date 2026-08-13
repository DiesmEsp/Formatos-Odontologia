package com.odontologia.formatos.service;

import com.odontologia.formatos.model.Operador;
import com.odontologia.formatos.repository.OperadorRepository;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class OperadorService {

    private static final List<String> TIPOS_PRE = Arrays.asList("3", "4", "5");
    private static final List<String> TIPOS_POS = Arrays.asList("R1", "R2", "R3");

    private final OperadorRepository repository = new OperadorRepository();

    public int crear(String nombres, String apellidos, String dni, String grado, String tipo, int periodo) throws SQLException {
        validarObligatorios(nombres, apellidos, grado, tipo);
        validarGradoTipo(grado, tipo);
        Operador operador = new Operador();
        operador.setNombres(nombres.trim());
        operador.setApellidos(apellidos.trim());
        operador.setDni(dni == null ? null : dni.trim());
        operador.setGrado(grado.trim().toUpperCase());
        operador.setTipo(tipo.trim().toUpperCase());
        operador.setPeriodo(periodo);
        operador.setEstado(1);
        return repository.insert(operador);
    }

    public void actualizar(Operador operador) throws SQLException {
        validarObligatorios(operador.getNombres(), operador.getApellidos(), operador.getGrado(), operador.getTipo());
        validarGradoTipo(operador.getGrado(), operador.getTipo());
        repository.update(operador);
    }

    public void eliminar(int operadorID) throws SQLException {
        repository.delete(operadorID);
    }

    public void validarGradoTipo(String grado, String tipo) {
        if (grado == null || tipo == null) {
            throw new NegocioException("Grado y tipo del operador son obligatorios.");
        }
        String g = grado.trim().toUpperCase();
        String t = tipo.trim().toUpperCase();
        if ("PRE".equals(g)) {
            if (!TIPOS_PRE.contains(t)) {
                throw new NegocioException("Un operador de grado PRE solo puede tener tipo 3, 4 o 5.");
            }
        } else if ("POS".equals(g)) {
            if (!TIPOS_POS.contains(t)) {
                throw new NegocioException("Un operador de grado POS solo puede tener tipo R1, R2 o R3.");
            }
        } else {
            throw new NegocioException("El grado debe ser PRE o POS.");
        }
    }

    private void validarObligatorios(String nombres, String apellidos, String grado, String tipo) {
        if (nombres == null || nombres.isBlank()) {
            throw new NegocioException("Los nombres del operador son obligatorios.");
        }
        if (apellidos == null || apellidos.isBlank()) {
            throw new NegocioException("Los apellidos del operador son obligatorios.");
        }
        if (grado == null || grado.isBlank()) {
            throw new NegocioException("El grado del operador es obligatorio.");
        }
        if (tipo == null || tipo.isBlank()) {
            throw new NegocioException("El tipo del operador es obligatorio.");
        }
    }
}
