package com.example.zapstation.model;

import com.google.firebase.firestore.GeoPoint;

public class Estacion {

    private String nombre;
    private String direccion;
    private GeoPoint posicion;  // Usamos GeoPoint para las coordenadas
    private String tipo;
    private int valoracion;
    private String comentario;  // Agregar comentario
    private String foto;  // Puede ser URL de la imagen o el ID del recurso
    private String paginaWeb;
    private double telefono;

    // Constructor vacío para Firestore
    public Estacion() {}

    public Estacion(String nombre, String direccion, double latitud, double longitud, TipoEstacion tipo, double telefono, String paginaWeb, String comentario, int valoracion, String foto) {
        this.nombre = nombre;
        this.direccion = direccion;
        this.posicion = new GeoPoint(longitud, latitud);  // Crear GeoPoint con latitud y longitud
        this.tipo = tipo.name();  // Convertimos el tipo a String (si TipoEstacion es un enum)
        this.valoracion = valoracion;
        this.comentario = comentario;
        this.foto = foto;
        this.paginaWeb = paginaWeb;
        this.telefono = telefono;
    }

    // Getters y setters
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public GeoPoint getPosicion() {
        return posicion;
    }

    public void setPosicion(GeoPoint posicion) {
        this.posicion = posicion;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public int getValoracion() {
        return valoracion;
    }

    public void setValoracion(int valoracion) {
        this.valoracion = valoracion;
    }

    public String getComentario() {
        return comentario;  // Método para obtener comentario
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;  // Método para establecer comentario
    }

    public String getFoto() {
        return foto;  // Método para obtener la foto
    }

    public void setFoto(String foto) {
        this.foto = foto;  // Método para establecer la foto
    }



    @Override
    public String toString() {
        return "Estacion{" +
                "nombre='" + nombre + '\'' +
                ", direccion='" + direccion + '\'' +
                ", posicion=" + posicion.getLatitude() + ", " + posicion.getLongitude() +
                ", tipo='" + tipo + '\'' +
                ", valoracion=" + valoracion +
                ", comentario='" + comentario + '\'' +
                ", foto='" + foto + '\'' +
                '}';
    }
}
