package com.example.zapstation.presentation;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import com.example.zapstation.R;
import com.example.zapstation.data.PremioAdapter;
import com.example.zapstation.model.Premio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Tab1 extends Fragment {
    public static int puntosDisponibles = 1000; // Puntos iniciales
    private TextView puntosTextView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Inflar el layout
        View view = inflater.inflate(R.layout.tab1, container, false);

        // Configurar TextView de puntos
        puntosTextView = view.findViewById(R.id.puntos);
        actualizarPuntosTextView();

        //Solo una vez para subir los premios al Firestore
        //subirPremiosAFirestore();

        // Configurar RecyclerView
        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewPremios);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        List<Premio> premios = new ArrayList<>();
        PremioAdapter adapter = new PremioAdapter(premios, premio -> {
            if (puntosDisponibles >= premio.getPuntos()) {
                puntosDisponibles -= premio.getPuntos();
                actualizarPuntosTextView();
                Toast.makeText(getActivity(), "¡Canjeaste " + premio.getNombre() + " por " + premio.getPuntos() + " puntos!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), "No tienes suficientes puntos para canjear " + premio.getNombre(), Toast.LENGTH_SHORT).show();
            }
        });

        recyclerView.setAdapter(adapter);

        // Cargar premios desde Firestore
        cargarPremiosDesdeFirestore(adapter, premios);

        return view;
    }

    // Lista de premios falsa, TODO: Que las coja de Firestore
    private List<Premio> obtenerListaPremios() {
        List<Premio> premios = new ArrayList<>();
        premios.add(new Premio("Café gratis", 100, R.drawable.img));
        premios.add(new Premio("Bocadillo + Café gratis", 200, R.drawable.almuerzo));
        premios.add(new Premio("Descuento 5%", 300, R.drawable.descuento_5));
        premios.add(new Premio("Descuento 10%", 350, R.drawable.descuento_10));
        premios.add(new Premio("Lavado de coche", 400, R.drawable.lavado));
        premios.add(new Premio("Descuento 25%", 500, R.drawable.descuento_25));
        premios.add(new Premio("Descuento 50%", 650, R.drawable.descuento_50));
        premios.add(new Premio("Carga gratis 30 min", 750, R.drawable.tiempo));
        premios.add(new Premio("Carga gratis 1 hora", 1000, R.drawable.tiempo2));
        return premios;
    }

    // Método para subir la lista de premios a Firestore
    private void subirPremiosAFirestore() {
        // Instancia de Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Lista de premios
        List<Premio> premios = obtenerListaPremios();

        for (Premio premio : premios) {
            // Crear un mapa de datos para cada premio
            Map<String, Object> premioMap = new HashMap<>();
            premioMap.put("nombre", premio.getNombre());
            premioMap.put("puntos", premio.getPuntos());
            premioMap.put("recursoImagen", premio.getRecursoImagen()); // Solo la referencia del recurso, no la imagen como tal

            // Subir el premio a Firestore (colección "premios")
            db.collection("premios")
                    .add(premioMap)
                    .addOnSuccessListener(documentReference -> {
                        // Éxito
                        Log.d("Firestore", "Premio añadido con ID: " + documentReference.getId());
                    })
                    .addOnFailureListener(e -> {
                        // Error
                        Log.e("Firestore", "Error al añadir premio", e);
                    });
        }
    }

    // Método para cargar los premios desde Firestore
    private void cargarPremiosDesdeFirestore(PremioAdapter adapter, List<Premio> premios) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("premios")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    premios.clear(); // Limpiar la lista antes de agregar nuevos datos
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        String nombre = document.getString("nombre");
                        Long puntos = document.getLong("puntos");
                        Long recursoImagen = document.getLong("recursoImagen"); // Nota: Firestore guarda los recursos como Long

                        if (nombre != null && puntos != null && recursoImagen != null) {
                            premios.add(new Premio(nombre, puntos.intValue(), recursoImagen.intValue()));
                        }
                    }

                    // Ordenar los premios por puntos de menor a mayor
                    Collections.sort(premios, new Comparator<Premio>() {
                        @Override
                        public int compare(Premio p1, Premio p2) {
                            return Integer.compare(p1.getPuntos(), p2.getPuntos());
                        }
                    });

                    // Notificar al adaptador que los datos han cambiado
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error al cargar premios", e);
                    Toast.makeText(getActivity(), "Error al cargar premios", Toast.LENGTH_SHORT).show();
                });
    }

    // Método para actualizar el texto del TextView de puntos
    private void actualizarPuntosTextView() {
        puntosTextView.setText("Tus puntos: " + puntosDisponibles);
    }
}
