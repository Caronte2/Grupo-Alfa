package com.example.zapstation.data;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.mislugaresfirebase.model.Lugar;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class EstacionesFirestore implements EstacionesAsinc {
    private CollectionReference estaciones;
    public EstacionesFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        estaciones = db.collection("estaciones");
    }
    public void elemento(String id, final EscuchadorElemento escuchador) {
        lugares.document(id).get().addOnCompleteListener(
                new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            Lugar lugar = task.getResult().toObject(Lugar.class);
                            escuchador.onRespuesta(lugar);
                        } else {
                            Log.e("Firebase", "Error al leer", task.getException());
                            escuchador.onRespuesta(null);
                        }
                    }
                });
    }
    public void añade(Lugar lugar) {
        lugares.document().set(lugar); //o lugares.add(lugar);
    }
    public String nuevo() {
        return lugares.document().getId();
    }
    public void borrar(String id) {
        lugares.document(id).delete();
    }
    public void actualiza(String id, Lugar lugar) {
        lugares.document(id).set(lugar);
    }
    public void tamaño(final EscuchadorTamaño escuchador) {
        lugares.get().addOnCompleteListener(
                new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            escuchador.onRespuesta(task.getResult().size());
                        } else {
                            Log.e("Firebase","Error en tamaño",task.getException());
                            escuchador.onRespuesta(-1);
                        }
                    }
                });
    }
}
