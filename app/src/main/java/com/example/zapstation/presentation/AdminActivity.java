package com.example.zapstation.presentation;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zapstation.R;
import com.example.zapstation.data.EstacionAdapter;
import com.example.zapstation.model.Estacion;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class AdminActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EstacionAdapter adaptador;
    private FirebaseUser usuario;
    private EditText editTextBuscarEstacion;
    private Button buttonBuscarEstacion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        //No me gusta el modo noche
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        usuario = FirebaseAuth.getInstance().getCurrentUser();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        editTextBuscarEstacion = findViewById(R.id.editTextBuscarEstacion);
        buttonBuscarEstacion = findViewById(R.id.buttonBuscarEstacion);

        // Configurar el botón de búsqueda
        buttonBuscarEstacion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buscarEstacion();
            }
        });

        // Inicializar adaptador sin el listener
        adaptador = new EstacionAdapter(new FirestoreRecyclerOptions.Builder<Estacion>()
                .setQuery(FirebaseFirestore.getInstance().collection("estaciones"), Estacion.class)
                .setLifecycleOwner(this)
                .build());
        recyclerView.setAdapter(adaptador);

        // Configurar el OnItemClickListener después de la inicialización
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
    }

    private void buscarEstacion() {
        String nombreBuscado = editTextBuscarEstacion.getText().toString().trim();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        int maxEstaciones = Integer.parseInt(preferences.getString("max_estaciones", "20"));

        if (nombreBuscado.isEmpty()) {
            obtenerEstacionesDesdeFirebase(maxEstaciones);
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Ajustar consulta para búsqueda parcial
        Query query = db.collection("estaciones")
                .orderBy("nombre")
                .startAt(nombreBuscado)
                .endAt(nombreBuscado + "\uf8ff")

                .limit(maxEstaciones);

        FirestoreRecyclerOptions<Estacion> options = new FirestoreRecyclerOptions.Builder<Estacion>()
                .setQuery(query, Estacion.class)
                .build();

        adaptador.updateOptions(options);
        adaptador.notifyDataSetChanged();
    }

    private void obtenerEstacionesDesdeFirebase(int limite) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Crear consulta con límite
        Query query = db.collection("estaciones").limit(limite);

        // Crear opciones de FirestoreRecycler
        FirestoreRecyclerOptions<Estacion> options = new FirestoreRecyclerOptions.Builder<Estacion>()
                .setQuery(query, Estacion.class)
                .build();

        // Establecer adaptador con las opciones creadas
        adaptador.updateOptions(options);
        adaptador.notifyDataSetChanged();
    }

    private void actualizarEstacionesConLimite(int limite) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Query query = db.collection("estaciones").limit(limite);

        // Crear opciones con la nueva consulta
        FirestoreRecyclerOptions<Estacion> options = new FirestoreRecyclerOptions.Builder<Estacion>()
                .setQuery(query, Estacion.class)
                .build();

        // Actualizar las opciones del adaptador y notificar los cambios
        adaptador.updateOptions(options);

        // Notificar que los datos han cambiado, especialmente si el número de elementos ha cambiado
        adaptador.notifyDataSetChanged();
    }

    private void mostrarEstacion(int position) {
        Estacion estacion = adaptador.getItem(position);

        if (estacion != null) {
            Intent intent = new Intent(this, VistaEstacionActivity.class);
            // Pasar el nombre de la estación al Intent
            intent.putExtra("nombreEstacion", estacion.getNombre());
            startActivity(intent);
        } else {
            Toast.makeText(this, "No se pudo cargar la estación seleccionada.", Toast.LENGTH_SHORT).show();
        }
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

    @Override
    protected void onStart() {
        super.onStart();
        adaptador.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adaptador.stopListening();
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        int maxEstaciones = Integer.parseInt(preferences.getString("max_estaciones", "20"));

        actualizarEstacionesConLimite(maxEstaciones);
    }
}
