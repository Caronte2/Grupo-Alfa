package com.example.zapstation.data;

import com.example.zapstation.model.Estacion;
import com.example.zapstation.model.GeoPunto;

public interface EstacionesAsinc {
    interface EscuchadorElemento {
        void onRespuesta(Estacion estacion);
        void onError(String error);
    }

    interface EscuchadorTamaño {
        void onRespuesta(long tamaño);
        void onError(String error);
    }

    void elemento(String id, EscuchadorElemento escuchador);
    void añade(Estacion estacion);
    String nuevo(String nombre, String direccion, double valoracion, String fotoUrl, GeoPunto geoPunto);
    void borrar(String id);
    void actualiza(String id, Estacion estacion);
    void tamaño(EscuchadorTamaño escuchador);
}

