package com.example.zapstation.presentation;

import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
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

import com.example.zapstation.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Tab2 extends Fragment {

    private MediaPlayer mediaPlayer;
    private float currentSongPosition = 0.0f;

    private GoogleMap mapa;

    String comentarioEstacion;

    private Button reservarButton;
    private Button compartirEstacion;

    private boolean estacionSeleccionada = false;
    private boolean esVisibleElTab = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tab2, container, false);

        // Obtener el botón de Reservar
        reservarButton = view.findViewById(R.id.reservar);

        // Mapa de Google pequeño
        FloatingActionButton openMapButton = view.findViewById(R.id.openMapButton);

        //Canción y volumen bajo para no molestar
        mediaPlayer = MediaPlayer.create(getActivity(), R.raw.bossa_velha);
        mediaPlayer.setVolume(0.2f, 0.2f);

        // Configurar el botón para abrir el mapa grande
        openMapButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MapaActivity.class);
            startActivityForResult(intent, 1); // Código de solicitud 1
        });

        // Configurar el comportamiento del botón de Reservar
        reservarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!estacionSeleccionada) { // Verificar si no hay estación seleccionada
                    Toast.makeText(getActivity(), "Por favor, selecciona una estación primero.", Toast.LENGTH_SHORT).show();
                    return; // Salir de la acción
                }

                FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();
                if (usuario != null) {
                    // Si el usuario está autenticado, mostrar un Toast
                    Toast.makeText(getActivity(), "Reserva confirmada", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    intent.putExtra("tab_index", 3); // Cambiar al Tab4 (índice 3)
                    intent.putExtra("estacionSeleccionada", true);
                    startActivity(intent);
                } else {
                    // Si el usuario no está autenticado, mostrar el AlertDialog
                    mostrarDialogoRegistro();
                }
            }
        });

        // Compartir estación
        compartirEstacion = view.findViewById(R.id.compartirEstacion);
        compartirEstacion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!estacionSeleccionada) { // Verificar si no hay estación seleccionada
                    Toast.makeText(getActivity(), "Por favor, selecciona una estación primero.", Toast.LENGTH_SHORT).show();
                    return;
                }

                //Obtener las referencias
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

                //Compartirla
                startActivity(Intent.createChooser(compartir, "Compartir estación"));
            }
        });

        return view;
    }

    //Para arreglar el bug de que al pasar la pestaña por ecncima suena la música
    @Override
    public void setUserVisibleHint(boolean esVisibleElTab) {
        super.setUserVisibleHint(esVisibleElTab);
        this.esVisibleElTab = esVisibleElTab;

        if (mediaPlayer != null) {
            if (esVisibleElTab) {
                // Reanudar la música cuando el tab sea visible
                mediaPlayer.start();
            } else {
                // Pausar la música si el tab no está visible
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                }
            }
        }
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
                Intent intent = new Intent(getActivity(), CustomLoginActivity.class);
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

            String fotoEstacion = data.getStringExtra("imagenEstacion");

            // Actualizar la interfaz del fragmento con los datos recibidos
            mostrarDatosEstacion(nombreEstacion, direccionEstacion, fotoEstacion, valoracion);

            estacionSeleccionada = true;
        }
    }

    private void mostrarDatosEstacion(String nombre, String direccion, String imagenNombre, float estrellas) {
        TextView nombreEstacion = getView().findViewById(R.id.nombreEstacion);
        TextView direccionEstacion = getView().findViewById(R.id.direccionEstacion);
        ImageView imagenImageView = getView().findViewById(R.id.imagenEstacion);
        RatingBar valoracion = getView().findViewById(R.id.valoracion);

        nombreEstacion.setText(nombre);
        direccionEstacion.setText(direccion);
        valoracion.setRating(estrellas);

        // Cargar la imagen desde los recursos usando el nombre de la imagen
        int imagenResId = getResources().getIdentifier(imagenNombre, "drawable", getActivity().getPackageName());

        if (imagenResId != 0) {
            // Si la imagen se encuentra en los recursos, cargarla
            imagenImageView.setImageResource(imagenResId);
        } else {
            // Si no se encuentra la imagen, puedes establecer una imagen por defecto
            imagenImageView.setImageResource(R.drawable.ejemplo2);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        // Guardar la posición de la canción en la variable estática
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            currentSongPosition = mediaPlayer.getCurrentPosition() / 1000f;
            mediaPlayer.pause();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mediaPlayer != null) {
            // Reanudar la canción desde la posición guardada
            mediaPlayer.seekTo((int) (currentSongPosition * 1000));
            mediaPlayer.start();
        }
    }

    // Liberar el MediaPlayer cuando el fragmento se destruya para evitar fugas de memorias o bugs
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
