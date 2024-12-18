package com.example.zapstation;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import android.content.SharedPreferences;

public class Tab2 extends Fragment {

    private MediaPlayer mediaPlayer;
    private float currentSongPosition = 0.0f;

    private GoogleMap mapa;
    private RepositorioEstaciones estaciones;

    String comentarioEstacion;

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

        // Mapa de Google pequeño
        FloatingActionButton openMapButton = view.findViewById(R.id.openMapButton);

        mediaPlayer = MediaPlayer.create(getActivity(), R.raw.bossa_velha);
        mediaPlayer.setVolume(0.2f, 0.2f);
        mediaPlayer.start();

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

        // Compartir estación
        Button compartirEstacion = view.findViewById(R.id.compartirEstacion);
        compartirEstacion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView nombreTextView = view.findViewById(R.id.nombreEstacion);
                TextView direccionTextView = view.findViewById(R.id.direccionEstacion);

                String nombreEstacion = nombreTextView.getText().toString();
                String direccionEstacion = direccionTextView != null ? direccionTextView.getText().toString() : "";

                Intent compartir = new Intent(Intent.ACTION_SEND);
                compartir.setType("text/plain");

                String mensaje = "Estación de carga:\n" +
                        "Nombre: " + nombreEstacion + "\n" +
                        "Dirección: " + direccionEstacion + "\n" +
                        "Comentario: " + comentarioEstacion;
                compartir.putExtra(Intent.EXTRA_TEXT, mensaje);

                startActivity(Intent.createChooser(compartir, "Compartir estación"));
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
            float valoracion = data.getFloatExtra("valoracion", 0.0f);
            comentarioEstacion = data.getStringExtra("comentario");

            int fotoEstacion = data.getIntExtra("imagenEstacion", R.drawable.educacion); // Usa un valor por defecto

            // Actualizar la interfaz del fragmento con los datos recibidos
            mostrarDatosEstacion(nombreEstacion, direccionEstacion, fotoEstacion, valoracion);
        }
    }

    // Método para actualizar la interfaz
    private void mostrarDatosEstacion(String nombre, String direccion, int imagenResId, float estrellas) {
        TextView nombreEstacion = getView().findViewById(R.id.nombreEstacion);
        TextView direccionEstacion = getView().findViewById(R.id.direccionEstacion);
        ImageView imagenImageView = getView().findViewById(R.id.imagenEstacion);
        RatingBar valoracion = getView().findViewById(R.id.valoracion);

        nombreEstacion.setText(nombre);
        direccionEstacion.setText(direccion);
        valoracion.setRating(estrellas);
        imagenImageView.setImageResource(imagenResId);

    }

    @Override
    public void onPause() {
        super.onPause();

        // Guardar la posición de la canción en SharedPreferences
        currentSongPosition = mediaPlayer.getCurrentPosition() / 1000f; // Convertir a segundos
        SharedPreferences preferences = getActivity().getSharedPreferences("MiAppPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat("song_position", currentSongPosition);
        editor.apply();

        // Pausar la canción cuando el fragmento está en pausa
        mediaPlayer.pause();
    }

    @Override
    public void onResume() {
        super.onResume();

        // Recuperar la posición de la canción guardada en SharedPreferences
        SharedPreferences preferences = getActivity().getSharedPreferences("MiAppPrefs", Context.MODE_PRIVATE);
        currentSongPosition = preferences.getFloat("song_position", 0.0f); // Valor por defecto es 0

        // Reanudar la canción desde la posición guardada (convertir a milisegundos)
        mediaPlayer.seekTo((int) (currentSongPosition * 1000)); // Convertir a milisegundos
        mediaPlayer.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Liberar el MediaPlayer cuando el fragmento se destruya
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

}
