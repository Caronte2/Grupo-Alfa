package com.example.zapstation.data;

import android.util.Log;

import com.example.zapstation.model.Estacion;
import com.example.zapstation.model.GeoPunto;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;


import java.util.HashMap;
import java.util.Map;

public class EstacionesLista implements EstacionesAsinc {
    private FirebaseFirestore db;
    private CollectionReference estacionesRef;

    public EstacionesLista() {
        db = FirebaseFirestore.getInstance();  // Inicializamos Firestore
        estacionesRef = db.collection("estaciones");  // Referencia a la colección "estaciones"

    }
    @Override
    public void elemento(String id, EscuchadorElemento escuchador) {
        estacionesRef.document(id).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Estacion estacion = documentSnapshot.toObject(Estacion.class);
                        escuchador.onRespuesta(estacion);  // Notificar cuando la estación es cargada
                    } else {
                        escuchador.onError("Estación no encontrada");
                    }
                })
                .addOnFailureListener(e -> {
                    escuchador.onError("Error al cargar la estación");
                });
    }

    @Override
    public void añade(Estacion estacion) {
        // Convertir la estación a un Map<String, Object> para Firestore
        Map<String, Object> estacionData = new HashMap<>();
        estacionData.put("nombre", estacion.getNombre());
        estacionData.put("direccion", estacion.getDireccion());
        estacionData.put("posicion", new GeoPoint(estacion.getPosicion().getLatitude(), estacion.getPosicion().getLongitude()));
        estacionData.put("valoracion", estacion.getValoracion());
        estacionData.put("comentario", estacion.getComentario());
        estacionData.put("foto", estacion.getFoto());

        // Añadir la estación a Firestore
        estacionesRef.add(estacionData)
                .addOnSuccessListener(documentReference -> {
                    // Estación añadida correctamente
                })
                .addOnFailureListener(e -> {
                    // Error al añadir estación
                });
    }

    @Override
    public String nuevo(String nombre, String direccion, double valoracion, String fotoUrl, GeoPunto geoPunto) {
        // Crear una nueva instancia de Estacion con los parámetros dados
        Estacion estacion = new Estacion(nombre, direccion, geoPunto.getLatitud(), geoPunto.getLongitud(), "", (int) valoracion, fotoUrl);
        final String[] documentId = new String[1];

        // Agregar la estación a Firestore
        estacionesRef.add(estacion)
                .addOnSuccessListener(documentReference -> {
                    documentId[0] = documentReference.getId();  // Obtener el ID del documento
                })
                .addOnFailureListener(e -> {
                    // Manejar error
                });

        return documentId[0];  // Devolver el ID del documento creado
    }

    @Override
    public void borrar(String id) {
        estacionesRef.document(id).delete()
                .addOnSuccessListener(aVoid -> {
                })
                .addOnFailureListener(e -> {
                });
    }

    @Override
    public void actualiza(String id, Estacion estacion) {
        estacionesRef.document(id).update(
                "nombre", estacion.getNombre(),
                "direccion", estacion.getDireccion(),
                "posicion", estacion.getPosicion(),
                "valoracion", estacion.getValoracion(),
                "comentario", estacion.getComentario(),
                "foto", estacion.getFoto()
        ).addOnSuccessListener(aVoid -> {
            // Estación actualizada correctamente
        }).addOnFailureListener(e -> {
            // Error al actualizar
        });
    }

    @Override
    public void tamaño(EscuchadorTamaño escuchador) {
        estacionesRef.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    long tamaño = queryDocumentSnapshots.size();
                    escuchador.onRespuesta(tamaño);  // Notificar cuando el tamaño es cargado
                })
                .addOnFailureListener(e -> {
                    escuchador.onError("Error al obtener el tamaño");
                });
    }
}
