package com.example.zapstation;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class Tab1 extends Fragment {
    private int puntosDisponibles = 1000; // Puntos iniciales
    private TextView puntosTextView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tab1, container, false);

        // Configurar TextView de puntos
        puntosTextView = view.findViewById(R.id.puntos);
        actualizarPuntosTextView();

        // Configurar RecyclerView
        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewPremios);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // Crear lista de premios
        List<Premio> premios = obtenerListaPremios();

        // Configurar adaptador
        PremioAdapter adapter = new PremioAdapter(premios, premio -> {
            if (puntosDisponibles >= premio.getPuntos()) {
                // Restar los puntos y actualizar el TextView
                puntosDisponibles -= premio.getPuntos();
                actualizarPuntosTextView();

                // Mostrar mensaje de éxito
                Toast.makeText(getActivity(),
                        "¡Canjeaste " + premio.getNombre() + " por " + premio.getPuntos() + " puntos!",
                        Toast.LENGTH_SHORT).show();
            } else {
                // Mostrar mensaje de error
                Toast.makeText(getActivity(),
                        "No tienes suficientes puntos para canjear " + premio.getNombre(),
                        Toast.LENGTH_SHORT).show();
            }
        });

        recyclerView.setAdapter(adapter);

        return view;
    }

    // Método para generar una lista de premios
    private List<Premio> obtenerListaPremios() {
        List<Premio> premios = new ArrayList<>();
        premios.add(new Premio("Café gratis", 20, R.drawable.img));
        premios.add(new Premio("Bocadillo + Café gratis", 40, R.drawable.almuerzo));
        premios.add(new Premio("Descuento 5%", 50, R.drawable.descuento_5));
        premios.add(new Premio("Descuento 10%", 70, R.drawable.descuento_10));
        premios.add(new Premio("Lavado de coche", 200, R.drawable.lavado));
        premios.add(new Premio("Descuento 25%", 250, R.drawable.descuento_25));
        premios.add(new Premio("Descuento 50%", 500, R.drawable.descuento_50));
        premios.add(new Premio("Carga gratis 30 min", 500, R.drawable.tiempo));
        premios.add(new Premio("Carga gratis 1 hora", 1000, R.drawable.tiempo2));
        return premios;
    }

    // Método para actualizar el texto del TextView de puntos
    private void actualizarPuntosTextView() {
        puntosTextView.setText("Tus puntos: " + puntosDisponibles);
    }
}
