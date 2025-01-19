package com.example.zapstation.presentation;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.zapstation.R;
import com.example.zapstation.model.Estacion;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class NuevaEstacionActivity extends AppCompatActivity {

    private EditText nombreEditText, direccionEditText, comentarioEditText, latitudEditText, longitudEditText;
    private ImageView fotoImageView;
    private Button guardarEstacion;
    private RatingBar ratingEstacion;
    private Uri imageUri;  // Uri para almacenar la imagen seleccionada
    private GeoPoint geoPoint;  // Coordenadas de la estación

    private FusedLocationProviderClient fusedLocationClient;  // Proveedor de ubicación
    private StorageReference storageReference;  // Referencia a Firebase Storage

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nueva_estacion);

        //No me gusta el modo noche
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        nombreEditText = findViewById(R.id.nombreEstacion);
        direccionEditText = findViewById(R.id.direccionEstacion);
        comentarioEditText = findViewById(R.id.comentarioEstacion);
        latitudEditText = findViewById(R.id.latitudEstacion);
        longitudEditText = findViewById(R.id.longitudEstacion);
        fotoImageView = findViewById(R.id.imagenEstacion);
        ratingEstacion = findViewById(R.id.ratingEstacion);
        guardarEstacion = findViewById(R.id.guardarEstacionButton);

        // Inicializar el FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Inicializar Firebase Storage
        storageReference = FirebaseStorage.getInstance().getReference();

        // Obtener la ubicación actual
        obtenerUbicacionActual();

        guardarEstacion.setOnClickListener(v -> subirImagenYGuardarEstacion());
        fotoImageView.setOnClickListener(v -> openImagePicker());
    }

    // Método para abrir un selector de imágenes
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 1);  // Código de solicitud
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            fotoImageView.setImageURI(imageUri);  // Muestra la imagen seleccionada
        }
    }

    private void obtenerUbicacionActual() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        fusedLocationClient.getLastLocation().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                Location location = task.getResult();
                geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
            } else {
                Toast.makeText(this, "No se pudo obtener la ubicación", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            obtenerUbicacionActual();
        } else {
            Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show();
        }
    }

    private void subirImagenYGuardarEstacion() {
        String nombre = nombreEditText.getText().toString().trim();
        String direccion = direccionEditText.getText().toString().trim();
        String comentario = comentarioEditText.getText().toString().trim();
        float valoracion = ratingEstacion.getRating();

        // Declarar las variables latitud y longitud como final
        final double latitud;
        final double longitud;

        // Obtener las coordenadas de latitud y longitud, si están vacías tomamos las del GeoPoint
        if (!latitudEditText.getText().toString().isEmpty() && !longitudEditText.getText().toString().isEmpty()) {
            // Si los campos no están vacíos, usaremos los valores ingresados por el usuario
            latitud = Double.parseDouble(latitudEditText.getText().toString().trim());
            longitud = Double.parseDouble(longitudEditText.getText().toString().trim());
        } else if (geoPoint != null) {
            // Si los campos están vacíos y tenemos el GeoPoint, usamos la ubicación actual
            latitud = geoPoint.getLatitude();
            longitud = geoPoint.getLongitude();
        } else {
            // Si no hay información de ubicación disponible
            Toast.makeText(this, "No se pudo obtener la ubicación. Por favor, ingrese las coordenadas manualmente.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Verificar si los campos de nombre y dirección están completos
        if (TextUtils.isEmpty(nombre) || TextUtils.isEmpty(direccion)) {
            Toast.makeText(this, "Por favor, completa todos los campos y selecciona una ubicación.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Subir la imagen si existe
        if (imageUri != null) {
            String fileName = "images/" + System.currentTimeMillis() + "_" + nombre + ".jpg";
            StorageReference fileRef = storageReference.child(fileName);

            UploadTask uploadTask = fileRef.putFile(imageUri);
            uploadTask.addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                String fotoUrl = uri.toString();
                guardarEstacionEnFirestore(nombre, direccion, comentario, valoracion, fotoUrl, latitud, longitud);
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Error al obtener la URL de la imagen.", Toast.LENGTH_SHORT).show();
            })).addOnFailureListener(e -> {
                Toast.makeText(this, "Error al subir la imagen.", Toast.LENGTH_SHORT).show();
            });
        } else {
            guardarEstacionEnFirestore(nombre, direccion, comentario, valoracion, "", latitud, longitud);
        }
    }

    private void guardarEstacionEnFirestore(String nombre, String direccion, String comentario, float valoracion, String fotoUrl, double latitud, double longitud) {
        Estacion nuevaEstacion = new Estacion(
                nombre,
                direccion,
                latitud,
                longitud,
                comentario,
                (int) valoracion,
                fotoUrl
        );

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("estaciones").add(nuevaEstacion).addOnSuccessListener(documentReference -> {
            Toast.makeText(this, "Estación añadida correctamente", Toast.LENGTH_SHORT).show();
            finish();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Error al añadir la estación.", Toast.LENGTH_SHORT).show();
        });
    }
}
