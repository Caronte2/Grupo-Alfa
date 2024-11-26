package com.example.zapstation;

import android.app.Application;

public class Aplicacion extends Application {

    public RepositorioEstaciones estaciones = new EstacionesLista();
    public com.example.zapstation.AdaptadorEstaciones adaptador = new AdaptadorEstaciones(estaciones);
    public GeoPunto posicionActual = new GeoPunto(0.0, 0.0);

    @Override public void onCreate() {
        super.onCreate();
    }
}