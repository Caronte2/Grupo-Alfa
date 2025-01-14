package com.example.zapstation.model;

import com.google.firebase.firestore.GeoPoint;

public class Estacion {

    private String nombre;
    private String direccion;
    private GeoPoint posicion;  // Usamos GeoPoint para las coordenadas
    private int valoracion;
    private String comentario;  // Agregar comentario
    private String foto;

    // Constructor vacío para Firestore
    public Estacion() {}

    public Estacion(String nombre, String direccion, double latitud, double longitud, String comentario, int valoracion, String foto){
        // Validar la latitud y longitud antes de cualquier inicialización
        if (latitud < -90 || latitud > 90) {
            throw new IllegalArgumentException("Latitude tiene que estar en el rango de [-90, 90]");
        }
        if (longitud < -180 || longitud > 180) {
            throw new IllegalArgumentException("Longitude tiene que estar en el rango de [-180, 180]");
        }
        this.nombre = nombre;
        this.direccion = direccion;
        this.posicion = new GeoPoint(latitud, longitud);
        this.valoracion = valoracion;
        this.comentario = comentario;
        this.foto = foto;
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

    public int getValoracion() {
        return valoracion;
    }

    public void setValoracion(int valoracion) {
        this.valoracion = valoracion;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    @Override
    public String toString() {
        return "Estacion{" +
                "nombre='" + nombre + '\'' +
                ", direccion='" + direccion + '\'' +
                ", posicion=" + posicion.getLatitude() + ", " + posicion.getLongitude() +
                ", valoracion=" + valoracion +
                ", comentario='" + comentario + '\'' +
                ", foto='" + foto + '\'' +
                '}';
    }
}
