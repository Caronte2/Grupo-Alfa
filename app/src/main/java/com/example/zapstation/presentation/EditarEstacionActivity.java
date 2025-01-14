package com.example.zapstation.presentation;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.zapstation.R;
import com.example.zapstation.model.Estacion;
import com.google.firebase.firestore.FirebaseFirestore;

public class EditarEstacionActivity extends AppCompatActivity {

    private EditText nombreEditText, direccionEditText, comentarioEditText;
    private EditText latitudEditText, longitudEditText; // Nuevos campos para las coordenadas
    private RatingBar ratingEstacion;
    private ImageView imagenEstacion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nueva_estacion);

        // Obtener las vistas
        nombreEditText = findViewById(R.id.nombreEstacion);
        direccionEditText = findViewById(R.id.direccionEstacion);
        comentarioEditText = findViewById(R.id.comentarioEstacion);
        latitudEditText = findViewById(R.id.latitudEstacion); // Inicializar EditText de latitud
        longitudEditText = findViewById(R.id.longitudEstacion); // Inicializar EditText de longitud
        ratingEstacion = findViewById(R.id.ratingEstacion);
        imagenEstacion = findViewById(R.id.imagenEstacion);

        Button guardarButton = findViewById(R.id.guardarEstacionButton);

        // Recuperar los datos pasados desde la actividad anterior (se asume que todos son String)
        Intent intent = getIntent();
        String nombreEstacion = intent.getStringExtra("nombreEstacion");
        String direccionEstacion = intent.getStringExtra("direccionEstacion");
        String comentarioEstacion = intent.getStringExtra("comentarioEstacion");
        float valoracionEstacion = intent.getFloatExtra("valoracionEstacion", 0f);
        String fotoEstacion = intent.getStringExtra("fotoEstacion");

        // Pre-poblar el formulario con los datos de la estación actual
        if (nombreEstacion != null) {
            nombreEditText.setText(nombreEstacion);
        }
        if (direccionEstacion != null) {
            direccionEditText.setText(direccionEstacion);
        }
        if (comentarioEstacion != null) {
            comentarioEditText.setText(comentarioEstacion);
        }
        if (valoracionEstacion != 0f) {
            ratingEstacion.setRating(valoracionEstacion);
        }
        if (fotoEstacion != null && !fotoEstacion.isEmpty()) {
            Glide.with(this).load(fotoEstacion).into(imagenEstacion);
        }

        // Guardar los cambios
        guardarButton.setOnClickListener(v -> {
            String nuevoNombre = nombreEditText.getText().toString().trim();
            String nuevaDireccion = direccionEditText.getText().toString().trim();
            String nuevoComentario = comentarioEditText.getText().toString().trim();
            float nuevaValoracion = ratingEstacion.getRating();

            // Obtener los valores de latitud y longitud
            double latitudEstacion = 0.0;
            double longitudEstacion = 0.0;

            try {
                latitudEstacion = Double.parseDouble(latitudEditText.getText().toString().trim());
                longitudEstacion = Double.parseDouble(longitudEditText.getText().toString().trim());
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Por favor, ingrese coordenadas válidas.", Toast.LENGTH_SHORT).show();
                return; // No continuar si las coordenadas no son válidas
            }

            // Validar los datos: si el nombre o dirección están vacíos, no guardar
            if (nuevoNombre.isEmpty() || nuevaDireccion.isEmpty()) {
                Toast.makeText(this, "Por favor, complete todos los campos.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Crear la estación con los datos recogidos
            Estacion nuevaEstacion = new Estacion(nuevoNombre, nuevaDireccion, latitudEstacion, longitudEstacion, nuevoComentario, (int) nuevaValoracion, fotoEstacion);

            // Actualizar la estación en Firestore
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("estaciones")
                    .document(nuevoNombre) // Usamos el nombre como documento para actualizar
                    .set(nuevaEstacion) // Establecer los nuevos datos para la estación
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(EditarEstacionActivity.this, "Estación actualizada correctamente.", Toast.LENGTH_SHORT).show();

                        // Regresar a la actividad anterior con los datos actualizados
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("nombreEstacion", nuevaEstacion.getNombre());
                        resultIntent.putExtra("direccionEstacion", nuevaEstacion.getDireccion());
                        resultIntent.putExtra("comentarioEstacion", nuevaEstacion.getComentario());
                        resultIntent.putExtra("valoracionEstacion", nuevaEstacion.getValoracion());
                        resultIntent.putExtra("fotoEstacion", nuevaEstacion.getFoto());

                        setResult(RESULT_OK, resultIntent);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(EditarEstacionActivity.this, "Error al guardar la estación.", Toast.LENGTH_SHORT).show();
                    });
        });
    }
}
