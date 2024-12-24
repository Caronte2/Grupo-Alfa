package com.example.zapstation.data;

import com.example.zapstation.model.Estacion;
import com.example.zapstation.model.TipoEstacion;
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

    public Estacion elemento(int id) {
        // Este método no se utiliza, ya que es asincrónico
        return null;
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
        estacionData.put("posicion", estacion.getPosicion());  // Firestore ya maneja GeoPoint
        estacionData.put("tipo", estacion.getTipo());
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
    public String nuevo() {
        // Crear una nueva estación vacía (en Firestore)
        Estacion estacion = new Estacion();  // Estación vacía
        final String[] documentId = new String[1];

        estacionesRef.add(estacion)
                .addOnSuccessListener(documentReference -> {
                    // Estación creada
                    documentId[0] = documentReference.getId();  // Obtener el ID del documento
                })
                .addOnFailureListener(e -> {
                    // Error
                });

        return documentId[0];  // Retorna el ID del documento creado
    }

    @Override
    public void borrar(String id) {
        estacionesRef.document(id).delete()
                .addOnSuccessListener(aVoid -> {
                    // Estación eliminada correctamente
                })
                .addOnFailureListener(e -> {
                    // Error al eliminar
                });
    }

    @Override
    public void actualiza(String id, Estacion estacion) {
        estacionesRef.document(id).update(
                "nombre", estacion.getNombre(),
                "direccion", estacion.getDireccion(),
                "posicion", estacion.getPosicion(),
                "tipo", estacion.getTipo(),
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

    public void borrar(int id) {
        estacionesRef.document(String.valueOf(id)).delete()
                .addOnSuccessListener(aVoid -> {
                    // Estación eliminada correctamente
                })
                .addOnFailureListener(e -> {
                    // Error al eliminar
                });
    }

    public int tamaño() {
        // Obtener el número de estaciones en Firestore (no se usa)
        estacionesRef.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    long tamaño = queryDocumentSnapshots.size();
                    // Aquí puedes manejar el tamaño de la colección de estaciones
                });
        return 0;  // Aquí debes devolver el tamaño, pero necesita ser asincrónico
    }

    public void actualiza(int id, Estacion estacion) {
        estacionesRef.document(String.valueOf(id)).update(
                "nombre", estacion.getNombre(),
                "direccion", estacion.getDireccion(),
                "posicion", estacion.getPosicion(),
                "tipo", estacion.getTipo(),
                "valoracion", estacion.getValoracion(),
                "comentario", estacion.getComentario(),
                "foto", estacion.getFoto()
        ).addOnSuccessListener(aVoid -> {
            // Estación actualizada correctamente
        }).addOnFailureListener(e -> {
            // Error al actualizar
        });
    }

    // Añadir ejemplos de estaciones
    public void añadeEjemplos() {
        Estacion estacion1 = new Estacion("Ajuntament de Sueca",
                "Plaça de l'Ajuntament, 10, 46410 Sueca, València",
                -0.310510, 39.202553, TipoEstacion.HOTEL, 644307085, "https://www.sueca.es/",
                "Mal lloc pa carregar el coche.", 1, "cochecargando");
        añade(estacion1);

        Estacion estacion2 = new Estacion("Plaça de l'estació",
                "Parque de la estación, 46410 Sueca, Valencia",
                -0.308471, 39.205706, TipoEstacion.HOTEL, 644306095, "https://www.sueca.es/",
                "Ñenfe Cercanias.", 4, "punto_carga");
        añade(estacion2);

        Estacion estacion3 = new Estacion("Escuela Politécnica Superior de Gandía",
                "C/ Paranimf, 1 46730 Gandia (SPAIN)", -0.166093, 38.995656, TipoEstacion.HOTEL, 962849300,
                "http://www.epsg.upv.es", "Uno de los mejores lugares para formarse.", 5, "foto_epsg");
        añade(estacion3);
    }
}
