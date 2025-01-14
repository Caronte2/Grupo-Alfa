package com.example.zapstation.presentation;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.zapstation.R;
import com.example.zapstation.model.Estacion;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

public class NuevaEstacionActivity extends AppCompatActivity {

    private EditText nombreEditText, direccionEditText, comentarioEditText;
    private ImageView fotoImageView;
    private Button guardarEstacion;
    private RatingBar ratingEstacion;
    private Uri imageUri;  // Uri para almacenar la imagen seleccionada
    private GeoPoint geoPoint;  // Deberías asignarlo con las coordenadas de la estación

    private FusedLocationProviderClient fusedLocationClient;  // Proveedor de ubicación

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nueva_estacion);

        nombreEditText = findViewById(R.id.nombreEstacion);
        direccionEditText = findViewById(R.id.direccionEstacion);
        comentarioEditText = findViewById(R.id.comentarioEstacion);
        fotoImageView = findViewById(R.id.imagenEstacion);
        ratingEstacion = findViewById(R.id.ratingEstacion);
        guardarEstacion = findViewById(R.id.guardarEstacionButton);

        // Inicializar el FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Llamar para obtener la ubicación
        obtenerUbicacionActual();

        guardarEstacion.setOnClickListener(v -> añadirEstacion());

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

    // Método para obtener la ubicación actual
    private void obtenerUbicacionActual() {
        // Verificar si el permiso de ubicación ha sido concedido
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Solicitar permisos de ubicación
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        // Obtener la ubicación actual
        fusedLocationClient.getLastLocation()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        Location location = task.getResult();
                        // Crear un GeoPoint con la latitud y longitud obtenida
                        geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                    } else {
                        Toast.makeText(NuevaEstacionActivity.this, "No se pudo obtener la ubicación", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Método para manejar el resultado de la solicitud de permisos
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido, obtener la ubicación
                obtenerUbicacionActual();
            } else {
                // Permiso denegado
                Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Método para añadir la estación a Firebase
    public void añadirEstacion() {
        String nombre = nombreEditText.getText().toString().trim();
        String direccion = direccionEditText.getText().toString().trim();
        String comentario = comentarioEditText.getText().toString().trim();
        float valoracion = ratingEstacion.getRating();

        // Verificar que los campos no estén vacíos
        if (TextUtils.isEmpty(nombre) || TextUtils.isEmpty(direccion)) {
            Toast.makeText(this, "Por favor, completa todos los campos y selecciona una ubicación.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Si no hay imagen seleccionada, asigna una cadena vacía
        String fotoUrl = imageUri != null ? imageUri.toString() : "";

        // Crear la nueva estación con los datos ingresados
        Estacion nuevaEstacion = new Estacion(
                nombre,
                direccion,
                geoPoint.getLatitude(),
                geoPoint.getLongitude(),
                comentario,
                (int) valoracion,
                fotoUrl  // Guardamos la foto como cadena vacía si no se selecciona ninguna
        );

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
    }
}
