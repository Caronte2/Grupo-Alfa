package com.example.zapstation;

public class Estacion {
    private String nombre;
    private String direccion;
    private com.example.zapstation.GeoPunto posicion;
    private com.example.zapstation.TipoEstacion tipo;
    private int foto;
    private int telefono;
    private String url;
    private String comentario;
    private long fecha;
    private float valoracion;

    public Estacion(String nombre, String direccion, double longitud,
                 double latitud, com.example.zapstation.TipoEstacion tipo, int telefono, String url, String comentario,
                 int valoracion, int foto) {
        fecha = System.currentTimeMillis();
        posicion = new com.example.zapstation.GeoPunto(longitud, latitud);
        this.nombre = nombre;
        this.direccion = direccion;
        this.tipo = tipo;
        this.telefono = telefono;
        this.url = url;
        this.comentario = comentario;
        this.valoracion = valoracion;
        this.foto = foto;
    }

    public Estacion() {
        fecha = System.currentTimeMillis();
        posicion = new com.example.zapstation.GeoPunto(0.0,0.0);
        tipo = com.example.zapstation.TipoEstacion.OTROS;
        this.foto = R.drawable.ejemplo2;
    }

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

    public com.example.zapstation.GeoPunto getPosicion() {
        return posicion;
    }

    public void setPosicion(GeoPunto posicion) {
        this.posicion = posicion;
    }

    public int getFoto() {return foto;}

    public void setFoto(int foto) {this.foto = foto;}

    public int getTelefono() {
        return telefono;
    }

    public void setTelefono(int telefono) {
        this.telefono = telefono;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public long getFecha() {
        return fecha;
    }

    public void setFecha(long fecha) {
        this.fecha = fecha;
    }

    public float getValoracion() {
        return valoracion;
    }

    public void setValoracion(float valoracion) {
        this.valoracion = valoracion;
    }

    public com.example.zapstation.TipoEstacion getTipo() {
        return tipo;
    }

    public void setTipo(TipoEstacion tipo) {
        this.tipo = tipo;
    }

    @Override
    public String toString() {
        return "Estacion{" +
                "nombre='" + nombre + '\'' +
                ", direccion='" + direccion + '\'' +
                ", posicion=" + posicion +
                ", tipo=" + tipo +
                ", foto='" + foto + '\'' +
                ", telefono=" + telefono +
                ", url='" + url + '\'' +
                ", comentario='" + comentario + '\'' +
                ", fecha=" + fecha +
                ", valoracion=" + valoracion +
                '}';
    }

}
