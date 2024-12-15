package com.example.zapstation;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class Tab1 extends Fragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tab1, container, false);

        // Configurar RecyclerView
        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewPremios);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // Crear lista de premios
        List<Premio> premios = obtenerListaPremios();

        // Configurar adaptador
        PremioAdapter adapter = new PremioAdapter(premios, premio -> {
            // Acción al canjear un premio
            Toast.makeText(getActivity(),
                    "¡Canjeaste " + premio.getNombre() + " por " + premio.getPuntos() + " puntos!",
                    Toast.LENGTH_SHORT).show();
        });

        recyclerView.setAdapter(adapter);

        return view;
    }

    // Método para generar una lista de premios
    private List<Premio> obtenerListaPremios() {
        List<Premio> premios = new ArrayList<>();
        premios.add(new Premio("Café gratis", 20,R.drawable.img));
        premios.add(new Premio("Bocadillo + Café gratis", 60,R.drawable.almuerzo));
        premios.add(new Premio("Descuento 5%", 100,R.drawable.descuento1));
        premios.add(new Premio("Lavado de coche", 200,R.drawable.lavado));
        premios.add(new Premio("Descuento 10%", 300,R.drawable.descuento2));
        premios.add(new Premio("Carga gratis 30 min", 500,R.drawable.tiempo));
        premios.add(new Premio("Carga gratis 1 hora", 1000,R.drawable.tiempo2));
        return premios;
    }
}
