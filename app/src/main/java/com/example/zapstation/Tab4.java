package com.example.zapstation;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class Tab4 extends Fragment {

    private boolean isCargaDetenida = false; // Variable para rastrear si se pulsó "Detener carga"
    private boolean isCargaIniciada = false; // Variable para rastrear si se pulsó "Iniciar carga"
    private TextView tiempoCargaActual;
    private TextView tiempoCargaEstimado;
    private TextView porcentajeBateria; // Nuevo TextView para mostrar el porcentaje de batería

    private int tiempoActual; // Variable para tiempo de carga actual
    private int tiempoEstimado; // Variable para tiempo de carga estimado
    private int porcentajeCarga; // Variable para rastrear el porcentaje de carga

    private Button preciosLuzButton;
    private RecyclerView recyclerViewPreciosLuz;
    private PrecioLuzAdapter adapter;
    private Button botonPreciosLuz;
    private boolean isRecyclerViewVisible = false; // Para alternar la visibilidad
    private List<PrecioLuz> preciosLuzList;


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
                    handler.postDelayed(this, 5000); // Actualización cada minuto
                }
            }
        }
    };

    //Permisos
    private static final int PERMISSION_REQUEST_CODE = 1;

    // Método para verificar y solicitar permiso de notificaciones
    private void verificarPermisoNotificaciones() {
        // Verificamos si el permiso de notificaciones está concedido
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            // Si no tiene el permiso, lo solicitamos
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    PERMISSION_REQUEST_CODE);
        } else {
            // Si ya tiene el permiso, podemos proceder a iniciar el servicio
            iniciarServicioCargaCoche();
        }
    }

    //Metodo para iniciar la lógica de carga
    private void iniciarServicioCargaCoche() {
        // Iniciar el servicio de carga del coche en primer plano
        Intent servicioCargaIntent = new Intent(getActivity(), ServicioCargaCoche.class);
        ContextCompat.startForegroundService(getActivity(), servicioCargaIntent);
        Toast.makeText(getActivity(), "Servicio de carga iniciado", Toast.LENGTH_SHORT).show();

        // Se inicia la actualización periódica
        handler.postDelayed(actualizarTiemposRunnable, 500); // Comienza la actualización cada 500ms
    }

    // Manejo del resultado de la solicitud de permisos
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Si el permiso fue concedido, podemos iniciar el servicio
                iniciarServicioCargaCoche();
            } else {
                // Si el permiso no fue concedido, muestra un mensaje
                Toast.makeText(getActivity(), "Permiso para notificaciones denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflar el layout del fragmento
        View rootView = inflater.inflate(R.layout.tab4, container, false);
        // Obtener referencias a los botones y TextView
        Button botonIniciarCarga = rootView.findViewById(R.id.iniciarCarga); // Referencia al botón "Iniciar carga"
        Button botonDetenerCarga = rootView.findViewById(R.id.detenerCarga);
        Button botonPagar = rootView.findViewById(R.id.botonPagar);
        botonPreciosLuz = rootView.findViewById(R.id.preciosLuz); // Referencia al botón "Precios Luz"
        tiempoCargaActual = rootView.findViewById(R.id.tiempoCarga);
        tiempoCargaEstimado = rootView.findViewById(R.id.tiempoCargaEstimado);
        porcentajeBateria = rootView.findViewById(R.id.porcentajebateria);
        recyclerViewPreciosLuz = rootView.findViewById(R.id.recyclerViewPreciosLuz);

        // Inicializar tiempos aleatorios y porcentaje al cargar la vista
        inicializarTiempos();

        // Inicializar RecyclerView
        recyclerViewPreciosLuz.setLayoutManager(new LinearLayoutManager(getContext()));
        preciosLuzList = generarListaPreciosLuz(); // Genera datos de ejemplo
        adapter = new PrecioLuzAdapter(preciosLuzList);
        recyclerViewPreciosLuz.setAdapter(adapter);

        recyclerViewPreciosLuz.setVisibility(View.GONE); // Inicialmente oculto
        inicializarTiempos();

        // Configurar listener para el botón "Iniciar carga"
        botonIniciarCarga.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verificarPermisoNotificaciones();
                isCargaIniciada = true;
            }
        });

        // Configurar listener para el botón "Detener carga"
        botonDetenerCarga.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isCargaDetenida = true; // Marcar que se ha detenido la carga
                Toast.makeText(getActivity(), "Carga detenida correctamente", Toast.LENGTH_SHORT).show();

                Intent servicioCargaIntent = new Intent(getActivity(), ServicioCargaCoche.class);
                getActivity().stopService(servicioCargaIntent);
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
        // Configurar listener para el botón "Precios Luz"
        botonPreciosLuz.setOnClickListener(v -> {
            // Verificar si el RecyclerView está visible y alternar su visibilidad
            if (recyclerViewPreciosLuz.getVisibility() == View.VISIBLE) {
                recyclerViewPreciosLuz.setVisibility(View.GONE);
                Toast.makeText(getActivity(), "Ocultando precios de la luz", Toast.LENGTH_SHORT).show();
            } else {
                recyclerViewPreciosLuz.setVisibility(View.VISIBLE);
                Toast.makeText(getActivity(), "Mostrando precios de la luz", Toast.LENGTH_SHORT).show();
                // Notificar cambios en el adaptador (esto ahora debería funcionar)
                adapter.notifyDataSetChanged();
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

    }

    //Lista mock para los precios de la luz, estaria bien usar la API de Iberdrola
    private List<PrecioLuz> generarListaPreciosLuz() {
        List<PrecioLuz> lista = new ArrayList<>();
        lista.add(new PrecioLuz(0.1507, "00:00 - 12:00", "Lunes"));
        lista.add(new PrecioLuz(0.2203, "12:00 - 00:00",  "Lunes"));
        lista.add(new PrecioLuz(0.1423, "00:00 - 12:00", "Martes"));
        lista.add(new PrecioLuz(0.2009, "12:00 - 00:00",  "Martes"));
        lista.add(new PrecioLuz(0.1507, "00:00 - 12:00", "Miércoles"));
        lista.add(new PrecioLuz(0.1485, "12:00 - 00:00",  "Miércoles"));
        lista.add(new PrecioLuz(0.1423, "00:00 - 12:00", "Jueves"));
        lista.add(new PrecioLuz(0.1489, "12:00 - 00:00",  "Jueves"));
        lista.add(new PrecioLuz(0.0505, "00:00 - 12:00", "Viernes"));
        lista.add(new PrecioLuz(0.2480, "12:00 - 00:00", "Viernes"));
        lista.add(new PrecioLuz(0.1897, "00:00 - 12:00", "Sábado"));
        lista.add(new PrecioLuz(0.2470, "12:00 - 00:00", "Sábado"));
        lista.add(new PrecioLuz(0.1395, "00:00 - 12:00", "Domingo"));
        lista.add(new PrecioLuz(0.2490, "12:00 - 00:00", "Domingo"));
        return lista;
    }

    //Destruir para no dejar fugas de memoria
   @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacks(actualizarTiemposRunnable);
    }
}





