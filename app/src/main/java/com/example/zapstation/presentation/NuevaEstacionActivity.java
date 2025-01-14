package com.example.zapstation.presentation;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.zapstation.R;
import com.example.zapstation.model.Estacion;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;

public class NuevaEstacionActivity extends AppCompatActivity {

    private EditText nombreEditText, direccionEditText, comentarioEditText;
    private ImageView fotoImageView;
    private RatingBar ratingEstacion;
    private Uri imageUri;  // Uri para almacenar la imagen seleccionada

    // GeoPoint debería recibir las coordenadas de la nueva estación, por ejemplo con un mapa.
    private GeoPoint geoPoint;  // Deberías asignarlo con las coordenadas de la estación, de lo contrario lo dejarías null.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nueva_estacion);

        nombreEditText = findViewById(R.id.nombreEstacion);
        direccionEditText = findViewById(R.id.direccionEstacion);
        comentarioEditText = findViewById(R.id.comentarioEstacion);
        fotoImageView = findViewById(R.id.imagenEstacion);
        ratingEstacion = findViewById(R.id.ratingEstacion);

        // Configurar el evento para seleccionar una foto
        fotoImageView.setOnClickListener(v -> openImagePicker());
    }

    // Método para abrir un selector de imágenes
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 1);  // El 1 es un código de solicitud arbitrario
    }

    // Esto se invoca después de seleccionar una imagen
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            fotoImageView.setImageURI(imageUri);  // Muestra la imagen seleccionada
        }
    }

    public void añadirEstacion(View view) {
        String nombre = nombreEditText.getText().toString().trim();
        String direccion = direccionEditText.getText().toString().trim();
        String comentario = comentarioEditText.getText().toString().trim();
        float valoracion = ratingEstacion.getRating();

        // Verificar que los campos no estén vacíos
        if (TextUtils.isEmpty(nombre) || TextUtils.isEmpty(direccion) || geoPoint == null) {
            Toast.makeText(this, "Por favor, completa todos los campos y selecciona una ubicación.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Subir imagen y obtener URL si se seleccionó una imagen
        if (imageUri != null) {
            subirImagen((imagenUrl) -> {
                // Crear la nueva estación con la URL de la imagen cuando se haya cargado
                Estacion nuevaEstacion = new Estacion(nombre, direccion, geoPoint.getLatitude(), geoPoint.getLongitude(), comentario, (int) valoracion, imagenUrl);

                // Guardar la estación en Firestore
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("estaciones")
                        .add(nuevaEstacion)
                        .addOnSuccessListener(documentReference -> {
                            Toast.makeText(this, "Estación añadida correctamente", Toast.LENGTH_SHORT).show();
                            finish(); // Cierra la actividad después de guardar la estación
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Error al añadir la estación.", Toast.LENGTH_SHORT).show();
                        });
            });
        } else {
            // Si no hay imagen, guarda la estación solo con el resto de los datos
            Estacion nuevaEstacion = new Estacion(nombre, direccion, geoPoint.getLatitude(), geoPoint.getLongitude(), comentario, (int) valoracion, "");

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("estaciones")
                    .add(nuevaEstacion)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "Estación añadida correctamente", Toast.LENGTH_SHORT).show();
                        finish(); // Cierra la actividad después de guardar la estación
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error al añadir la estación.", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    // Método para subir la imagen al almacenamiento de Firebase y devolver la URL
    private void subirImagen(OnImageUploadedListener listener) {
        if (imageUri == null) {
            listener.onImageUploaded("");  // Si no hay imagen seleccionada, pasa una cadena vacía
            return;
        }

        // Generar una ruta única para la imagen
        String fileName = "estacion_" + UUID.randomUUID().toString();
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("images/" + fileName);

        // Subir la imagen a Firebase Storage
        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Obtener la URL de descarga después de la carga exitosa
                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        // Pasar la URL a la estación
                        listener.onImageUploaded(uri.toString());
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar la imagen.", Toast.LENGTH_SHORT).show();
                    listener.onImageUploaded("");  // En caso de error, pasa una cadena vacía
                });
    }

    // Interfaz para manejar la respuesta de la carga de la imagen
    private interface OnImageUploadedListener {
        void onImageUploaded(String imageUrl);
    }
}

