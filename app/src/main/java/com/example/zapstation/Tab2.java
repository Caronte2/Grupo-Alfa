package com.example.zapstation;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Tab2 extends Fragment {

    private GoogleMap mapa;
    private RepositorioEstaciones estaciones;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        estaciones = ((Aplicacion) getActivity().getApplication()).estaciones;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tab2, container, false);


        // Obtener el botón de Reservar
        Button reservarButton = view.findViewById(R.id.reservar);
        ImageView like = view.findViewById(R.id.like);
        ImageView dislike = view.findViewById(R.id.dislike);

        // Mapa de Google pequeño
        FloatingActionButton openMapButton = view.findViewById(R.id.openMapButton);

        // Configurar el botón para abrir el mapa grande
        openMapButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MapaActivity.class);
            startActivityForResult(intent, 1); // Código de solicitud 1
        });

        // Configurar el comportamiento del botón de Reservar
        reservarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();
                if (usuario != null) {
                    // Si el usuario está autenticado, mostrar un Toast
                    Toast.makeText(getActivity(), "Reserva confirmada", Toast.LENGTH_SHORT).show();
                } else {
                    // Si el usuario no está autenticado, mostrar el AlertDialog
                    mostrarDialogoRegistro();
                }
            }
        });

        // Configurar el comportamiento del like
        like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();
                if (usuario != null) {
                    // Si el usuario está autenticado, mostrar un mensaje de like
                    Toast.makeText(getActivity(), "Valoración positiva", Toast.LENGTH_SHORT).show();
                } else {
                    // Si el usuario no está autenticado, mostrar el AlertDialog
                    mostrarDialogoRegistro();
                }
            }
        });

        // Configurar el comportamiento del dislike
        dislike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();
                if (usuario != null) {
                    // Si el usuario está autenticado, mostrar un mensaje de dislike
                    Toast.makeText(getActivity(), "Valoración negativa", Toast.LENGTH_SHORT).show();
                } else {
                    // Si el usuario no está autenticado, mostrar el AlertDialog
                    mostrarDialogoRegistro();
                }
            }
        });

        return view;
    }

    // Mostrar el popup para confirmar si desea registrarse
    private void mostrarDialogoRegistro() {
        // Crear el AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Acción de sesión necesaria");
        builder.setMessage("Para continuar necesitas tener una sesión iniciada. ¿Deseas hacerlo ahora?");

        // Botón para ir a registrarse
        builder.setPositiveButton("Registrar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Lógica para ir a la actividad de registro (puedes adaptarla)
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
            }
        });

        // Botón para cancelar la acción
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Cerrar el diálogo
                dialog.dismiss();
            }
        });

        // Mostrar el diálogo
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // Manejar el resultado al regresar de MapaActivity
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == getActivity().RESULT_OK && data != null) {
            // Recuperar los datos de la estación seleccionada
            String nombreEstacion = data.getStringExtra("nombreEstacion");
            String direccionEstacion = data.getStringExtra("direccionEstacion");
            String imagenEstacion = data.getStringExtra("imagenEstacion");

            // Actualizar la interfaz del fragmento con los datos recibidos
            mostrarDatosEstacion(nombreEstacion, direccionEstacion, imagenEstacion);
        }
    }

    // Método para actualizar la interfaz
    private void mostrarDatosEstacion(String nombre, String direccion, String imagen) {
        TextView nombreTextView = getView().findViewById(R.id.nombreEstacion);
        ImageView imagenImageView = getView().findViewById(R.id.imagenEstacion);

        nombreTextView.setText(nombre);

        Bitmap bitmap = BitmapFactory.decodeFile(imagen);
        imagenImageView.setImageBitmap(bitmap);
    }
}
