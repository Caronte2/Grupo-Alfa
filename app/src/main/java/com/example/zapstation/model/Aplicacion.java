package com.example.zapstation.model;

import android.app.Application;

import com.example.zapstation.data.AdaptadorEstaciones;
import com.example.zapstation.data.EstacionesLista;
import com.example.zapstation.data.RepositorioEstaciones;

public class Aplicacion extends Application {

    //Para lo que tiene que ver con las estaciones
    public RepositorioEstaciones estaciones = new EstacionesLista();
    public AdaptadorEstaciones adaptador = new AdaptadorEstaciones(estaciones);
    public GeoPunto posicionActual = new GeoPunto(0.0, 0.0);

    @Override public void onCreate() {
        super.onCreate();
    }
}