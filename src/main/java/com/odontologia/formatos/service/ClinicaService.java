package com.odontologia.formatos.service;

import com.odontologia.formatos.db.ConnectionManager;
import com.odontologia.formatos.model.Clinica;
import com.odontologia.formatos.repository.ClinicaRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class ClinicaService {

    private final ClinicaRepository repository = new ClinicaRepository();

    public int crear(String nombre, String grupo) throws SQLException {
        validarNombre(nombre);
        String nombreLimpio = nombre.trim();
        if (repository.findByNombre(nombreLimpio) != null) {
            throw new EntidadDuplicadaException("Ya existe una clínica con el nombre '" + nombreLimpio + "'.");
        }
        Clinica clinica = new Clinica();
        clinica.setNombre(nombreLimpio);
        clinica.setGrupo(grupo == null ? null : grupo.trim());
        clinica.setEstado(1);
        return repository.insert(clinica);
    }

    public void actualizar(Clinica clinica) throws SQLException {
        validarNombre(clinica.getNombre());
        String nombreLimpio = clinica.getNombre().trim();
        Clinica existente = repository.findByNombre(nombreLimpio);
        if (existente != null && existente.getClinicaID() != clinica.getClinicaID()) {
            throw new EntidadDuplicadaException("Ya existe una clínica con el nombre '" + nombreLimpio + "'.");
        }
        clinica.setNombre(nombreLimpio);
        if (clinica.getGrupo() != null) {
            clinica.setGrupo(clinica.getGrupo().trim());
        }
        repository.update(clinica);
    }

    public void eliminar(int clinicaID) throws SQLException {
        if (tieneRegistros(clinicaID)) {
            throw new NegocioException("La clínica tiene registros asociados (catálogos o transacciones) y no puede eliminarse.");
        }
        repository.delete(clinicaID);
    }

    public List<Clinica> listar() throws SQLException {
        return repository.findAll();
    }

    public Clinica buscarPorId(int clinicaID) throws SQLException {
        return repository.findById(clinicaID);
    }

    private boolean tieneRegistros(int clinicaID) throws SQLException {
        String sql = "SELECT " +
                "(SELECT COUNT(*) FROM Operadores WHERE ClinicaID = ?) + " +
                "(SELECT COUNT(*) FROM Docentes WHERE ClinicaID = ?) + " +
                "(SELECT COUNT(*) FROM Unidad WHERE ClinicaID = ?) + " +
                "(SELECT COUNT(*) FROM Pacientes WHERE ClinicaID = ?) + " +
                "(SELECT COUNT(*) FROM Tratamiento WHERE ClinicaID = ?) + " +
                "(SELECT COUNT(*) FROM Asistencia WHERE ClinicaID = ?) + " +
                "(SELECT COUNT(*) FROM RegistroAnulacion WHERE ClinicaID = ?)";
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 1; i <= 7; i++) {
                ps.setInt(i, clinicaID);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private void validarNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new NegocioException("El nombre de la clínica es obligatorio.");
        }
    }
}