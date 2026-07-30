package main.java.com.odontologia.formatos.model;

/* Imports */
import java.util.Date;
import java.util.List;
/**
 * Tratamiento
 */
public class Tratamiento {

    private String codigo;
    private String codigoPaciente;
    private String codigoOperador;
    private String nombreTratamiento;
    private Date fechaTratamiento;
    private List<Materiales> materiales;
    private float monto_total;
}