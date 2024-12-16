package com.example.zapstation;

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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;

public class Tab3 extends Fragment {
    private ListenerRegistration listenerRegistration;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tab3, container, false);

        Button loginButton = view.findViewById(R.id.loginButton);
        Button registrarButton = view.findViewById(R.id.registrarButton);
        TextView texto1 = view.findViewById(R.id.texto1);
        TextView texto2 = view.findViewById(R.id.texto2);
        TextView texto3 = view.findViewById(R.id.texto3);
        TextView texto4 = view.findViewById(R.id.texto4);
        ImageView mobil = view.findViewById(R.id.mobil);
        ImageView imagenRegistrado = view.findViewById(R.id.imagenRegistrado);

        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();

        TextView textBienvenido = view.findViewById(R.id.textBienvenido);

        if (usuario != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference docRef = db.collection("usuarios").document(usuario.getUid());

            // Agregar SnapshotListener
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

        if (usuario == null) {
            loginButton.setVisibility(View.VISIBLE);
            registrarButton.setVisibility(View.VISIBLE);
            texto1.setVisibility(View.VISIBLE);
            texto2.setVisibility(View.VISIBLE);
            mobil.setVisibility(View.VISIBLE);

            //Registrado
            textBienvenido.setVisibility(View.GONE);
            texto3.setVisibility(View.GONE);
            texto4.setVisibility(View.GONE);
            imagenRegistrado.setVisibility(View.GONE);

            //Llamada LoginAcitivy para usar firebase en el inicio de seión
            loginButton.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), CustomLoginActivity.class); //CustomLogin o Login para cambiar la logica de inicio de sesion
                startActivity(intent);
            });

            registrarButton.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), CustomRegisterActivity.class);
                startActivity(intent);
            });
        } else {
            loginButton.setVisibility(View.GONE);
            registrarButton.setVisibility(View.GONE);
            texto1.setVisibility(View.GONE);
            texto2.setVisibility(View.GONE);
            mobil.setVisibility(View.GONE);

            //Registrado
            textBienvenido.setVisibility(View.VISIBLE);
            textBienvenido.setText("Bienvenido" + " " +  usuario.getDisplayName());
            texto3.setVisibility(View.VISIBLE);
            texto4.setVisibility(View.VISIBLE);
            imagenRegistrado.setVisibility(View.VISIBLE);
        }

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Eliminar el listener cuando la vista se destruya para evitar fugas de memoria
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
    }
}