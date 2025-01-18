package com.example.zapstation.presentation;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.bumptech.glide.Glide;
import com.example.zapstation.R;
import com.example.zapstation.model.Estacion;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import android.location.Location;

public class EditarEstacionActivity extends AppCompatActivity {

    private EditText direccionEditText, comentarioEditText, latitudEditText, longitudEditText;
    private TextView nombreText;
    private RatingBar ratingEstacion;
    private ImageView imagenEstacion;

    private Uri imageUri;
    private StorageReference storageReference;

    private GeoPoint geoPoint;

    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_estacion);

        //No me gusta el modo noche
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        // Inicializar la ubicación
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        storageReference = FirebaseStorage.getInstance().getReference();

        // Obtener las vistas
        nombreText = findViewById(R.id.nombreEstacion);
        direccionEditText = findViewById(R.id.direccionEstacion);
        comentarioEditText = findViewById(R.id.comentarioEstacion);
        latitudEditText = findViewById(R.id.latitudEstacion);
        longitudEditText = findViewById(R.id.longitudEstacion);
        ratingEstacion = findViewById(R.id.ratingEstacion);
        imagenEstacion = findViewById(R.id.imagenEstacion);

        Button actualizarButton = findViewById(R.id.actualizarEstacionButton);

        // Recuperar los datos pasados desde la actividad anterior
        Intent intent = getIntent();
        String nombreEstacion = intent.getStringExtra("nombreEstacion");
        String direccionEstacion = intent.getStringExtra("direccionEstacion");
        String comentarioEstacion = intent.getStringExtra("comentarioEstacion");
        float valoracionEstacion = intent.getFloatExtra("valoracionEstacion", 0f);
        String fotoEstacion = intent.getStringExtra("fotoEstacion");
        final double latitudRecibida = intent.getDoubleExtra("latitudEstacion", 0.0);
        final double longitudRecibida = intent.getDoubleExtra("longitudEstacion", 0.0);

        // Pre-poblar el formulario con los datos de la estación actual
        nombreText.setText(nombreEstacion);
        direccionEditText.setText(direccionEstacion);
        comentarioEditText.setText(comentarioEstacion);
        ratingEstacion.setRating(valoracionEstacion);
        latitudEditText.setText(String.valueOf(latitudRecibida));
        longitudEditText.setText(String.valueOf(longitudRecibida));
        Glide.with(this).load(fotoEstacion).into(imagenEstacion);
        geoPoint = new GeoPoint(latitudRecibida, longitudRecibida);

        // Abrir la galería al hacer clic en la imagen
        imagenEstacion.setOnClickListener(v -> openImagePicker());

        // Actualizar los cambios
        actualizarButton.setOnClickListener(v -> {
            String nuevoNombre = nombreText.getText().toString().trim();
            String nuevaDireccion = direccionEditText.getText().toString().trim();
            String nuevoComentario = comentarioEditText.getText().toString().trim();
            float nuevaValoracion = ratingEstacion.getRating();

            // Eliminar el 'final' de estas líneas
            double latitudEstacion = 0.0;  // Ahora puedes modificar estas variables
            double longitudEstacion = 0.0;  // Ahora puedes modificar estas variables

            // Verificar si las coordenadas fueron modificadas
            if (latitudEditText.getText().toString().isEmpty() || longitudEditText.getText().toString().isEmpty()) {
                // Si las coordenadas están vacías, obtener la ubicación actual
                obtenerUbicacionActual(nuevoNombre, nuevaDireccion, nuevoComentario, nuevaValoracion, fotoEstacion);
            } else {
                // Si el usuario ingresó nuevas coordenadas
                try {
                    latitudEstacion = Double.parseDouble(latitudEditText.getText().toString().trim());
                    longitudEstacion = Double.parseDouble(longitudEditText.getText().toString().trim());
                    actualizarEstacion(nuevoNombre, nuevaDireccion, nuevoComentario, nuevaValoracion, fotoEstacion, latitudEstacion, longitudEstacion);
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Por favor, ingrese coordenadas válidas.", Toast.LENGTH_SHORT).show();
                }
            }

            // Subir la imagen si es necesario
            if (imageUri != null) {
                String fileName = "images/" + System.currentTimeMillis() + "_" + nuevoNombre + ".jpg";
                StorageReference fileRef = storageReference.child(fileName);

                UploadTask uploadTask = fileRef.putFile(imageUri);
                double finalLatitudEstacion = latitudEstacion;
                double finalLongitudEstacion = longitudEstacion;
                uploadTask.addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String fotoUrl = uri.toString();
                    // Aquí guardamos la estación con la nueva imagen
                    actualizarEstacion(nuevoNombre, nuevaDireccion, nuevoComentario, nuevaValoracion, fotoUrl, finalLatitudEstacion, finalLongitudEstacion);
                }).addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al obtener la URL de la imagen.", Toast.LENGTH_SHORT).show();
                })).addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al subir la imagen.", Toast.LENGTH_SHORT).show();
                });
            } else {
                actualizarEstacion(nuevoNombre, nuevaDireccion, nuevoComentario, nuevaValoracion, fotoEstacion, latitudEstacion, longitudEstacion);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            Glide.with(this)
                    .load(imageUri)
                    .into(imagenEstacion);
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 1);  // 1 es el código de solicitud
    }

    // Método para obtener la ubicación actual
    private void obtenerUbicacionActual(String nuevoNombre, String nuevaDireccion, String nuevoComentario, float nuevaValoracion, String fotoEstacion) {
        fusedLocationClient.getLastLocation()
                .addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(Task<Location> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            Location location = task.getResult();
                            double latitudActual = location.getLatitude();
                            double longitudActual = location.getLongitude();

                            // Llamar a la función para actualizar la estación con la ubicación actual
                            actualizarEstacion(nuevoNombre, nuevaDireccion, nuevoComentario, nuevaValoracion, fotoEstacion, latitudActual, longitudActual);
                        } else {
                            // Si no se puede obtener la ubicación
                            Toast.makeText(EditarEstacionActivity.this, "No se pudo obtener la ubicación actual.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // Método para actualizar la estación en Firestore
    private void actualizarEstacion(String nuevoNombre, String nuevaDireccion, String nuevoComentario, float nuevaValoracion, String fotoEstacion, double latitudEstacion, double longitudEstacion) {
        GeoPoint nuevaPosicion = new GeoPoint(latitudEstacion, longitudEstacion);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("estaciones")
                .whereEqualTo("nombre", nuevoNombre)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        QuerySnapshot result = task.getResult();
                        String documentId = result.getDocuments().get(0).getId(); // Obtener el ID del documento

                        db.collection("estaciones")
                                .document(documentId)
                                .update(
                                        "direccion", nuevaDireccion,
                                        "comentario", nuevoComentario,
                                        "valoracion", (int) nuevaValoracion,
                                        "foto", fotoEstacion,
                                        "posicion", nuevaPosicion
                                )
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(EditarEstacionActivity.this, "Estación actualizada correctamente.", Toast.LENGTH_SHORT).show();
                                    Intent resultIntent = new Intent();
                                    resultIntent.putExtra("nombreEstacion", nuevoNombre);
                                    resultIntent.putExtra("direccionEstacion", nuevaDireccion);
                                    resultIntent.putExtra("comentarioEstacion", nuevoComentario);
                                    resultIntent.putExtra("valoracionEstacion", nuevaValoracion);
                                    resultIntent.putExtra("fotoEstacion", fotoEstacion);
                                    setResult(RESULT_OK, resultIntent);
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(EditarEstacionActivity.this, "Error al actualizar la estación.", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(EditarEstacionActivity.this, "No se encontró la estación con ese nombre.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
