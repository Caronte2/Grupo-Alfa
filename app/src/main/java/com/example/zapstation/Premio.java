package com.example.zapstation;

public class Premio {
    private String nombre;
    private int puntos;
    private int recursoImagen;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getPuntos() {
        return puntos;
    }

    public void setPuntos(int puntos) {
        this.puntos = puntos;
    }

    public int getRecursoImagen() {
        return recursoImagen;
    }

    public void setRecursoImagen(int recursoImagen) {
        this.recursoImagen = recursoImagen;
    }

    public Premio(String nombre, int puntos, int recursoImagen) {
        this.nombre = nombre;
        this.puntos = puntos;
        this.recursoImagen = recursoImagen;

    }}
