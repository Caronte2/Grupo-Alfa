package com.example.zapstation.model;

public class PrecioLuz {

    //Clase POJO del precio de la luz
    private double precioKWh;
    private String horario;
    private String diaSemana; // Nuevo campo

    public double getPrecioKWh() {
        return precioKWh;
    }

    public void setPrecioKWh(double precioKWh) {
        this.precioKWh = precioKWh;
    }

    public String getHorario() {
        return horario;
    }

    public void setHorario(String horario) {
        this.horario = horario;
    }

    public String getDiaSemana() {
        return diaSemana;
    }

    public void setDiaSemana(String diaSemana) {
        this.diaSemana = diaSemana;
    }

    public PrecioLuz(double precioKWh, String horario, String diaSemana) {
        this.precioKWh = precioKWh;
        this.horario = horario;
        this.diaSemana = diaSemana;
    }
}
