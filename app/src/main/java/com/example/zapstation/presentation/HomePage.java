package com.example.zapstation.presentation;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.zapstation.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;

public class HomePage extends Fragment {

    private ListenerRegistration listenerRegistration;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        //Inflar la vista del layout
        View view = inflater.inflate(R.layout.home_page, container, false);

        //Obtener usuario de Firebase
        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();

        TextView textBienvenido = view.findViewById(R.id.textBienvenido);
        Button abrirMapa = view.findViewById(R.id.abrirMapa);

        abrirMapa.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), MainActivity.class);
            intent.putExtra("tab_index", 1); // Cambiar al Tab2 (índice 1)
            startActivity(intent);
        });
        
        //Comprobación de que el usuario tiene sus datos en Firestore para cargarlos
        if (usuario != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference docRef = db.collection("usuarios").document(usuario.getUid());

            // Agregar SnapshotListener por si hay cambios en tiempo real
            listenerRegistration = docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                    if (e != null) {
                        Toast.makeText(getContext(), "Error al cargar el nombre", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        String nombreCompleto = documentSnapshot.getString("nombreCompleto");
                        textBienvenido.setText(nombreCompleto != null ? "Bienvenido, " + nombreCompleto : "Bienvenido, Nombre no disponible");
                    }
                }
            });
        }

        textBienvenido.setText("Bienvenido" + " " +  usuario.getDisplayName());

        return view;
    }

}