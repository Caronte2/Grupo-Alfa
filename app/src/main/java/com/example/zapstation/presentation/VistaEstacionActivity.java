package com.example.zapstation.presentation;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.zapstation.R;
import com.example.zapstation.data.EstacionesLista;
import com.example.zapstation.model.Estacion;

public class VistaEstacionActivity extends AppCompatActivity {

    private Estacion estacion;
    private int pos;
    private ImageView foto;
    private EstacionesLista estacionesLista;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.info_estacion_completa);
        Toolbar toolbar = findViewById(R.id.toolbarEstacion);
        setSupportActionBar(toolbar);

        // Inicializar EstacionesLista para consultar datos en Firestore
        estacionesLista = new EstacionesLista();
        pos = getIntent().getIntExtra("pos", 0); // Obtener posición o ID de la estación
        cargarEstacion(pos); // Cargar los datos de la estación desde Firestore
        foto = findViewById(R.id.fotoEstacion); // ImageView donde se mostrará la foto de la estación
    }

    // Cargar datos de la estación desde Firestore
    private void cargarEstacion(int pos) {
        estacionesLista.elemento(String.valueOf(pos), new EstacionesLista.EscuchadorElemento() {
            @Override
            public void onRespuesta(Estacion estacionData) {
                estacion = estacionData; // Obtener la estación desde Firestore
                actualizaVistas(); // Actualizar las vistas con los datos
            }

            @Override
            public void onError(String error) {
                // Mostrar mensaje de error en caso de falla
                Toast.makeText(VistaEstacionActivity.this, error, Toast.LENGTH_LONG).show();
            }
        });
    }

    // Actualiza las vistas con la información de la estación
    private void actualizaVistas() {
        TextView nombre = findViewById(R.id.nombre);
        TextView direccion = findViewById(R.id.direccion);
        TextView comentario = findViewById(R.id.comentarioEstacion);
        RatingBar valoracion = findViewById(R.id.valoracion);

        // Llenar los campos con los datos obtenidos
        nombre.setText(estacion.getNombre());
        direccion.setText(estacion.getDireccion());
        comentario.setText(estacion.getComentario());
        valoracion.setRating(estacion.getValoracion()); // Configurar el rating de la estación

        // Cargar la foto desde la URL utilizando Glide
        ponerFoto(foto, estacion.getFoto());
    }

    // Método para cargar la imagen en el ImageView utilizando Glide
    private void ponerFoto(ImageView imageView, String uri) {
        if (uri != null && !uri.isEmpty()) {
            // Usando Glide para cargar la imagen desde la URL en el ImageView
            Glide.with(this)
                    .load(uri)  // URI de la imagen
                    .into(imageView);  // Establecerla en el ImageView
        } else {
            // Si la URL es vacía o nula, limpiar la imagen del ImageView
            imageView.setImageBitmap(null);
        }
    }

    // Método para eliminar la estación
    public void eliminarEstacion(View view) {
        estacionesLista.borrar(String.valueOf(estacion.getNombre()));
        Toast.makeText(this, "Estación eliminada", Toast.LENGTH_SHORT).show();
        finish(); // Finalizar la actividad después de la eliminación
    }

    // Método para actualizar la estación
    public void actualizarEstacion(View view) {
        // Actualizar la estación en Firestore
        estacionesLista.actualiza(String.valueOf(estacion.getNombre()), estacion);
        Toast.makeText(this, "Estación actualizada", Toast.LENGTH_SHORT).show();
    }

    // Método para compartir la estación
    public void compartirEstacion(View view) {
        try {
            // Crear un intento para compartir la información de la estación
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_TEXT, estacion.getNombre() + " - " + estacion.getComentario()); // Compartir nombre y comentario
            startActivity(i); // Iniciar la acción de compartir
        } catch (Exception e) {
            Toast.makeText(this, "Error al compartir: ", Toast.LENGTH_SHORT).show();
        }
    }
}
