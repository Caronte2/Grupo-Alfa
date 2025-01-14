package com.example.zapstation.presentation;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zapstation.R;
import com.example.zapstation.data.EstacionAdapter;
import com.example.zapstation.model.Estacion;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EstacionAdapter adaptador;
    private List<Estacion> listaEstaciones = new ArrayList<>();
    private FirebaseUser usuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        usuario = FirebaseAuth.getInstance().getCurrentUser();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Inicialización del adaptador antes de configurarlo en el RecyclerView
        adaptador = new EstacionAdapter(new ArrayList<>());
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adaptador);

        // Pasar el OnItemClickListener al adaptador
        adaptador.setOnItemClickListener(position -> mostrarEstacion(position));

        // Obtener las preferencias de configuración del número máximo de estaciones
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        int maxEstaciones = Integer.parseInt(preferences.getString("max_estaciones", "20"));

        // Llamar a obtenerEstacionesDesdeFirebase para cargar las estaciones desde Firebase
        obtenerEstacionesDesdeFirebase(maxEstaciones);

        // Registrar cambios en las preferencias
        preferences.registerOnSharedPreferenceChangeListener((sharedPreferences, key) -> {
            if ("max_estaciones".equals(key)) {
                int nuevoMax = Integer.parseInt(sharedPreferences.getString("max_estaciones", "20"));
                actualizarEstacionesConLimite(nuevoMax);
            }
        });
        // La función cargarDatosFalsos() ya no es necesaria aquí
    }

    private void obtenerEstacionesDesdeFirebase(final int limite) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("estaciones")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            Log.d("AdminActivity", "Número de estaciones obtenidas: " + querySnapshot.size());
                            listaEstaciones.clear(); // Limpiar la lista antes de añadir los datos nuevos

                            for (DocumentSnapshot document : querySnapshot) {
                                if (document.contains("nombre") && document.contains("direccion")) {
                                    String nombre = document.getString("nombre");
                                    String direccion = document.getString("direccion");
                                    double valoracion = document.getDouble("valoracion") != null ? document.getDouble("valoracion") : 0.0;
                                    String fotoUrl = document.getString("foto");
                                    GeoPoint geoPoint = document.getGeoPoint("coordenadas");

                                    if (geoPoint != null) {
                                        Estacion estacion = new Estacion(
                                                nombre,
                                                direccion,
                                                geoPoint.getLatitude(),
                                                geoPoint.getLongitude(),
                                                fotoUrl,
                                                (int) valoracion,
                                                "" // Comentario vacío por defecto
                                        );
                                        listaEstaciones.add(estacion);
                                    } else {
                                        Log.w("AdminActivity", "Estación " + nombre + " no tiene coordenadas");
                                    }
                                }
                            }

                            // Actualizar el RecyclerView con los datos obtenidos
                            actualizarEstacionesConLimite(limite);
                        } else {
                            Log.e("AdminActivity", "No se encontraron estaciones o consulta vacía");
                        }
                    } else {
                        Log.e("AdminActivity", "Error al obtener las estaciones de Firebase", task.getException());
                    }
                });
    }

    private void actualizarEstacionesConLimite(int limite) {
        // Verificar si la lista de estaciones excede el límite
        if (listaEstaciones.size() > limite) {
            adaptador.setEstaciones(listaEstaciones.subList(0, limite));
        } else {
            adaptador.setEstaciones(listaEstaciones);
        }
        adaptador.notifyDataSetChanged();
    }


    private void cargarDatosFalsos() {
        List<Estacion> estacionesPrueba = new ArrayList<>();
        estacionesPrueba.add(new Estacion("Estación A", "Dirección A", -34.5, 138.6, "https://url_imagen", 5, "Comentario 1"));
        estacionesPrueba.add(new Estacion("Estación B", "Dirección B", -35.5, 137.6, "https://url_imagen", 3, "Comentario 2"));
        estacionesPrueba.add(new Estacion("Estación C", "Dirección C", -36.5, 139.6, "https://url_imagen", 4, "Comentario 3"));

        adaptador.setEstaciones(estacionesPrueba); // Establece las estaciones falsos en el adaptador
        adaptador.notifyDataSetChanged(); // Notifica al adaptador que actualice la vista
    }

    private void mostrarEstacion(int position) {
        Estacion estacion = listaEstaciones.get(position);
        // Implementar la lógica para mostrar la información de la estación
    }

    private void mostrarDialogoCerrarSesion() {
        new AlertDialog.Builder(this)
                .setTitle("Cerrar sesión")
                .setMessage("¿Deseas cerrar sesión?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_admin, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            abrirPreferencias();
            return true;
        }

        if (id == R.id.menu_nuevo_lugar) {
            añadirNuevaEstacion();
            return true;
        }

        if (id == R.id.action_log_out) {
            mostrarDialogoCerrarSesion();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void abrirPreferencias() {
        Intent intent = new Intent(this, PreferenciasActivity.class);
        startActivity(intent);
    }

    private void añadirNuevaEstacion() {
        Intent intent = new Intent(this, NuevaEstacionActivity.class);
        startActivity(intent);
    }
}




