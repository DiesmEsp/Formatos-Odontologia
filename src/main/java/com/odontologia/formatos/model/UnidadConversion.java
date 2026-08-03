package com.odontologia.formatos.model;

public class UnidadConversion {

    private int conversionID;
    private int materialID;
    private String unidadBase;
    private String unidadEmpaque;
    private double factor;

    public UnidadConversion() {
    }

    public UnidadConversion(int conversionID, int materialID, String unidadBase, String unidadEmpaque, double factor) {
        this.conversionID = conversionID;
        this.materialID = materialID;
        this.unidadBase = unidadBase;
        this.unidadEmpaque = unidadEmpaque;
        this.factor = factor;
    }

    public int getConversionID() {
        return conversionID;
    }

    public void setConversionID(int conversionID) {
        this.conversionID = conversionID;
    }

    public int getMaterialID() {
        return materialID;
    }

    public void setMaterialID(int materialID) {
        this.materialID = materialID;
    }

    public String getUnidadBase() {
        return unidadBase;
    }

    public void setUnidadBase(String unidadBase) {
        this.unidadBase = unidadBase;
    }

    public String getUnidadEmpaque() {
        return unidadEmpaque;
    }

    public void setUnidadEmpaque(String unidadEmpaque) {
        this.unidadEmpaque = unidadEmpaque;
    }

    public double getFactor() {
        return factor;
    }

    public void setFactor(double factor) {
        this.factor = factor;
    }
}
