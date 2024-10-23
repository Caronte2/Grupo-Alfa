package com.example.zapstation;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.android.material.button.MaterialButton;

public class Tab5 extends Fragment {
    private FirebaseUser usuario;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Obtener el usuario autenticado de Firebase
        usuario = FirebaseAuth.getInstance().getCurrentUser();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflar el layout para este fragmento
        View view = inflater.inflate(R.layout.tab5, container, false);

        // Inicializar los TextViews
        TextView nombre = view.findViewById(R.id.nombre);
        TextView correo = view.findViewById(R.id.correo);
        TextView uid = view.findViewById(R.id.uid);
        NetworkImageView foto = view.findViewById(R.id.imagen);
        MaterialButton btnCerrarSesion = view.findViewById(R.id.btn_cerrar_sesion); // Asegúrate de tener este ID en tu layout

        // Establecer OnClickListener para el botón de cerrar sesión
        btnCerrarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cerrarSesion(v);
            }
        });

        // Mostrar la información del usuario en los TextViews
        if (usuario != null) {
            nombre.setText(usuario.getDisplayName());
            correo.setText(usuario.getEmail());
            uid.setText(usuario.getUid());

            // Mostrar foto de usuario si está disponible
            Uri urlImagen = usuario.getPhotoUrl();
            if (urlImagen != null) {
                // Inicializar Volley para la carga de imágenes
                RequestQueue colaPeticiones = Volley.newRequestQueue(getContext());
                ImageLoader lectorImagenes = new ImageLoader(colaPeticiones,
                        new ImageLoader.ImageCache() {
                            private final LruCache<String, Bitmap> cache = new LruCache<>(10);
                            public void putBitmap(String url, Bitmap bitmap) {
                                cache.put(url, bitmap);
                            }
                            public Bitmap getBitmap(String url) {
                                return cache.get(url);
                            }
                        });
                // Cargar imagen de usuario
                foto.setImageUrl(urlImagen.toString(), lectorImagenes);
            }
        }

        return view;
    }

    public void cerrarSesion(View view) {
        AuthUI.getInstance().signOut(getContext()) // Cambia getApplicationContext() por getContext()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Intent i = new Intent(getContext(), LoginActivity.class); // Cambia getApplicationContext() por getContext()
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                                | Intent.FLAG_ACTIVITY_NEW_TASK
                                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(i);
                        requireActivity().finish(); // Usa requireActivity() para terminar la actividad
                    }
                });
    }
}