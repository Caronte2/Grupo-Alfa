package com.example.zapstation;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

public class Tab4 extends Fragment {
    private boolean isCargaDetenida = false; // Variable para rastrear si se pulsó "Detener carga"
    private boolean isCargaIniciada = false; // Variable para rastrear si se pulsó "Iniciar carga"
    private TextView tiempoCargaActual;
    private TextView tiempoCargaEstimado;
    private TextView porcentajeBateria; // Nuevo TextView para mostrar el porcentaje de batería

    private int tiempoActual; // Variable para tiempo de carga actual
    private int tiempoEstimado; // Variable para tiempo de carga estimado
    private int porcentajeCarga; // Variable para rastrear el porcentaje de carga

    private final Handler handler = new Handler(); // Handler para actualizar periódicamente los tiempos
    private final Runnable actualizarTiemposRunnable = new Runnable() {
        @Override
        public void run() {
            if (isCargaIniciada) {
                if (tiempoActual < tiempoEstimado) {
                    tiempoActual++; // Incrementar tiempo de carga actual
                }

                // Aumentar el porcentaje de carga basado en el tiempo actual
                if (!isCargaDetenida && porcentajeCarga < 100) {
                    porcentajeCarga = Math.min(100, porcentajeCarga + 1); // Incrementa y asegura que no pase de 100%
                    porcentajeBateria.setText("Batería al " + porcentajeCarga + "%");

                    // Actualizar el tiempo estimado según el porcentaje restante
                    tiempoEstimado = 100 - porcentajeCarga; // 1 minuto por cada 1% de batería
                }

                // Actualizar los TextView con los nuevos valores
                tiempoCargaActual.setText("Tiempo de carga actual: " + tiempoActual + " minutos");
                tiempoCargaEstimado.setText("Tiempo de carga estimado: " + tiempoEstimado + " minutos");

                // Continuar la actualización periódica hasta que la carga esté completa
                if (tiempoActual < 100 && porcentajeCarga < 100) {
                    handler.postDelayed(this, 60000); // Actualización cada minuto
                }
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflar el layout del fragmento
        View rootView = inflater.inflate(R.layout.tab4, container, false);

        // Obtener referencias a los botones y TextView
        Button botonIniciarCarga = rootView.findViewById(R.id.iniciarCarga); // Referencia al botón "Iniciar carga"
        Button botonDetenerCarga = rootView.findViewById(R.id.detenerCarga);
        Button botonPagar = rootView.findViewById(R.id.botonPagar);
        tiempoCargaActual = rootView.findViewById(R.id.tiempoCarga);
        tiempoCargaEstimado = rootView.findViewById(R.id.tiempoCargaEstimado);
        porcentajeBateria = rootView.findViewById(R.id.porcentajebateria);

        // Inicializar tiempos aleatorios y porcentaje al cargar la vista
        inicializarTiempos();

        // Configurar listener para el botón "Iniciar carga"
        botonIniciarCarga.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isCargaIniciada = true; // Iniciar la carga
                Toast.makeText(getActivity(), "Carga iniciada", Toast.LENGTH_SHORT).show();
                // Se inicia la actualización periódica
                handler.postDelayed(actualizarTiemposRunnable, 500); // Comienza la actualización cada 500ms
            }
        });

        // Configurar listener para el botón "Detener carga"
        botonDetenerCarga.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isCargaDetenida = true; // Marcar que se ha detenido la carga
                Toast.makeText(getActivity(), "Carga detenida correctamente", Toast.LENGTH_SHORT).show();
            }
        });

        // Configurar listener para el botón "Pagar"
        botonPagar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isCargaDetenida) {
                    // Si la carga fue detenida, proceder con la acción de pago
                    Toast.makeText(getActivity(), "Procediendo con el pago...", Toast.LENGTH_SHORT).show();
                } else {
                    // Mostrar mensaje si no se detuvo la carga antes de intentar pagar
                    Toast.makeText(getActivity(), "Debe detener la carga antes de pagar.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return rootView;
    }

    private void inicializarTiempos() {
        // Generar tiempos aleatorios iniciales
        tiempoActual = 0; // Comienza en 0 minutos
        // Generar un porcentaje aleatorio entre 20% y 80% para simular el estado de la batería
        porcentajeCarga = (int) (Math.random() * (80 - 20 + 1)) + 20; // Entre 20% y 80%

        // Establecer el tiempo estimado según el porcentaje de batería
        tiempoEstimado = 100 - porcentajeCarga; // 1 minuto por cada 1% de batería

        // Mostrar los valores iniciales
        tiempoCargaActual.setText("Tiempo de carga actual: " + tiempoActual + " minutos");
        tiempoCargaEstimado.setText("Tiempo de carga estimado: " + tiempoEstimado + " minutos");
        porcentajeBateria.setText("Batería al " + porcentajeCarga + "%");

        // No se inicia la carga automáticamente
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Detener actualizaciones al destruir la vista
        handler.removeCallbacks(actualizarTiemposRunnable);
    }
}
