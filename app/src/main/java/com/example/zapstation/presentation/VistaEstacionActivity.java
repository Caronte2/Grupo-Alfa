package com.example.zapstation.presentation;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class VistaEstacionActivity extends AppCompatActivity {

    private Estacion estacion;
    private ImageView foto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.info_estacion_completa);

        Toolbar toolbar = findViewById(R.id.toolbarEstacion);
        setSupportActionBar(toolbar);

        // Obtener el nombre de la estación desde el Intent
        String nombreEstacion = getIntent().getStringExtra("nombreEstacion");
        if (nombreEstacion == null || nombreEstacion.isEmpty()) {
            Toast.makeText(this, "Nombre de la estación no válido.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Referencia a las vistas
        TextView tvNombre = findViewById(R.id.nombreEstacion);
        TextView tvDireccion = findViewById(R.id.direccionEstacion);
        TextView tvComentario = findViewById(R.id.comentarioEstacion);
        TextView tvValoracion = findViewById(R.id.valoracionEstacion);
        TextView tvLatitud = findViewById(R.id.latitudEstacion);
        TextView tvLongitud = findViewById(R.id.longitudEstacion);
        ImageView ivFoto = findViewById(R.id.fotoEstacion);
        foto = ivFoto;

        // Consultar los datos de la estación desde Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("estaciones")
                .whereEqualTo("nombre", nombreEstacion)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Obtener el primer documento
                        estacion = queryDocumentSnapshots.getDocuments().get(0).toObject(Estacion.class);
                        if (estacion != null) {
                            // Actualizar la interfaz con los datos obtenidos
                            tvNombre.setText(estacion.getNombre());
                            tvDireccion.setText(estacion.getDireccion());
                            tvComentario.setText(estacion.getComentario());
                            tvValoracion.setText("Valoración: " + estacion.getValoracion() + "/5");

                            // Mostrar las coordenadas (Latitud y Longitud)
                            tvLatitud.setText("Latitud: " + estacion.getPosicion().getLatitude());
                            tvLongitud.setText("Longitud: " + estacion.getPosicion().getLongitude());

                            if (estacion.getFoto() != null && !estacion.getFoto().isEmpty()) {
                                Glide.with(this).load(estacion.getFoto()).into(ivFoto);
                            } else {
                                ivFoto.setImageResource(R.drawable.punto_carga); // Imagen por defecto
                            }
                        }
                    } else {
                        Toast.makeText(this, "No se encontró la estación.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar datos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("FirestoreError", "Error al cargar datos de la estación", e);
                    finish();
                });

        // Configurar el botón de eliminar
        Button btnEliminar = findViewById(R.id.btnEliminarEstacion);
        btnEliminar.setOnClickListener(v -> eliminarEstacion());

        // Configurar el botón de compartir
        Button btnCompartir = findViewById(R.id.btnCompartir);
        btnCompartir.setOnClickListener(this::compartirEstacion);

        // Configurar el botón de editar
        Button btnEditar = findViewById(R.id.btnEditarEstacion);
        btnEditar.setOnClickListener(this::editarEstacion);
    }


    public void editarEstacion(View view) {
        if (estacion == null) {
            Toast.makeText(this, "No se puede editar la estación, no hay datos disponibles.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Preparar el Intent para abrir el formulario de edición
        Intent intent = new Intent(this, EditarEstacionActivity.class);
        intent.putExtra("nombreEstacion", estacion.getNombre());
        intent.putExtra("direccionEstacion", estacion.getDireccion());
        intent.putExtra("comentarioEstacion", estacion.getComentario());
        intent.putExtra("valoracionEstacion", estacion.getValoracion());
        intent.putExtra("fotoEstacion", estacion.getFoto());

        // Iniciar la actividad para que el usuario pueda editar los datos
        startActivityForResult(intent, 1001); // Código de solicitud
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1001 && resultCode == RESULT_OK) {
            // Recuperamos los nuevos datos desde el formulario
            String nuevoNombre = data.getStringExtra("nombreEstacion");
            String nuevaDireccion = data.getStringExtra("direccionEstacion");
            String nuevoComentario = data.getStringExtra("comentarioEstacion");
            float nuevaValoracion = data.getFloatExtra("valoracionEstacion", 0f);
            String nuevaFoto = data.getStringExtra("fotoEstacion");

            // Actualizamos la estación localmente
            estacion.setNombre(nuevoNombre);
            estacion.setDireccion(nuevaDireccion);
            estacion.setComentario(nuevoComentario);
            estacion.setValoracion((int) nuevaValoracion);
            estacion.setFoto(nuevaFoto);

            //Actualizamos la vista
            actualizarVista();
        }
    }

    // Método que actualiza la vista con la nueva información de la estación
    private void actualizarVista() {
        if (estacion != null) {
            TextView nombre = findViewById(R.id.nombreEstacion);
            TextView direccion = findViewById(R.id.direccionEstacion);
            TextView comentario = findViewById(R.id.comentarioEstacion);
            TextView valoracion = findViewById(R.id.valoracionEstacion);
            TextView latitud = findViewById(R.id.latitudEstacion);
            TextView longitud = findViewById(R.id.longitudEstacion);
            ImageView ivFoto = findViewById(R.id.fotoEstacion);

            nombre.setText(estacion.getNombre());
            direccion.setText(estacion.getDireccion());
            comentario.setText(estacion.getComentario());
            valoracion.setText("Valoración: " + estacion.getValoracion() + "/5");

            // Actualizar las coordenadas (latitud, longitud)
            latitud.setText("Latitud: " + estacion.getPosicion().getLatitude());
            longitud.setText("Longitud: " + estacion.getPosicion().getLongitude());

            if (estacion.getFoto() != null && !estacion.getFoto().isEmpty()) {
                Glide.with(this).load(estacion.getFoto()).into(ivFoto);
            } else {
                ivFoto.setImageResource(R.drawable.punto_carga); // Imagen por defecto
            }
        }
    }

    public void compartirEstacion(View view) {
        if (estacion == null) {
            Toast.makeText(this, "No hay información de estación para compartir.", Toast.LENGTH_SHORT).show();
            return;
        }

        String mensaje = "Estación de carga:\n" +
                "Nombre: " + estacion.getNombre() + "\n" +
                "Dirección: " + estacion.getDireccion() + "\n" +
                "Comentario: " + estacion.getComentario() + "\n" +
                "Valoración: " + estacion.getValoracion() + "/5";

        Intent compartirIntent = new Intent(Intent.ACTION_SEND);
        compartirIntent.setType("text/plain");
        compartirIntent.putExtra(Intent.EXTRA_TEXT, mensaje);

        startActivity(Intent.createChooser(compartirIntent, "Compartir estación"));
    }

    public void eliminarEstacion() {
        if (estacion == null) {
            Toast.makeText(this, "No se puede eliminar la estación, no hay datos disponibles.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Filtrar por el nombre de la estación en Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("estaciones")
                .whereEqualTo("nombre", estacion.getNombre()) // Filtramos por nombre
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        // Obtener el ID del primer documento que coincida
                        QuerySnapshot result = task.getResult();
                        String documentId = result.getDocuments().get(0).getId();

                        // Obtener la URL de la foto desde el documento
                        String fotoUrl = result.getDocuments().get(0).getString("foto");

                        // Eliminar la foto de Firebase Storage si existe
                        if (fotoUrl != null && !fotoUrl.isEmpty()) {
                            // Obtener la referencia de la foto en Firebase Storage
                            FirebaseStorage storage = FirebaseStorage.getInstance();
                            StorageReference photoRef = storage.getReferenceFromUrl(fotoUrl);

                            // Eliminar la foto
                            photoRef.delete().addOnSuccessListener(aVoid -> {
                                Log.d("EliminarFoto", "Foto eliminada correctamente.");
                            }).addOnFailureListener(e -> {
                                Log.e("EliminarFoto", "Error al eliminar la foto", e);
                            });
                        }

                        // Eliminar el documento de Firestore
                        db.collection("estaciones")
                                .document(documentId)
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Estación y foto eliminada correctamente.", Toast.LENGTH_SHORT).show();
                                    finish(); // Regresar a la actividad anterior
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Error al eliminar la estación.", Toast.LENGTH_SHORT).show();
                                    Log.e("EliminarError", "Error al eliminar la estación", e);
                                });
                    } else {
                        Toast.makeText(this, "No se encontró la estación con ese nombre.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

