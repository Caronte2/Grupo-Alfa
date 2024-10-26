package com.example.zapstation;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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
        ImageView btnCerrarSesion = view.findViewById(R.id.btn_cerrar_sesion);

        // Establecer OnClickListener para la ImageView de cerrar sesión
        btnCerrarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();
                if (usuario != null) {
                    mostrarDialogoCerrarSesion();
                } else {
                    // Muestra el diálogo si el usuario no está autenticado
                    mostrarDialogoCerrarSesion();
                }
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

    // Método para mostrar el popup para confirmar si desea cerrar sesión
    private void mostrarDialogoCerrarSesion() {
        // Crear el AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Cerrar sesión");
        builder.setMessage("¿Deseas cerrar sesión?");

        // Botón "Sí" para confirmar cerrar sesión
        builder.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Lógica para ir a la actividad de inicio de sesión o registro
                FirebaseAuth.getInstance().signOut(); // Cierra sesión en Firebase
                Intent intent = new Intent(getActivity(), MainActivity.class);
                startActivity(intent);
                getActivity().finish(); // Finaliza la actividad actual
            }
        });

        // Botón "No" para cancelar la acción
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Cerrar el diálogo sin hacer nada
                dialog.dismiss();
            }
        });

        // Mostrar el diálogo
        AlertDialog dialog = builder.create();
        dialog.show();
    }

}
