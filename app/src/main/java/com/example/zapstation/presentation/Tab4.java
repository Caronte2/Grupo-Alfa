package com.example.zapstation.presentation;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.ui.PlayerView;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zapstation.R;
import com.example.zapstation.data.PrecioLuzAdapter;
import com.example.zapstation.model.PrecioLuz;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Tab4 extends Fragment {

    private ExoPlayer player;
    private PlayerView playerView;

    private boolean isCargaDetenida = false; // Variable para rastrear si se pulsó "Detener carga"
    private boolean isCargaIniciada = false; // Variable para rastrear si se pulsó "Iniciar carga"
    private boolean estacionSelecionada;
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
            if (isCargaIniciada && !isCargaDetenida) { // Asegurarse de no actualizar si se detuvo la carga
                if (tiempoActual < tiempoEstimado) {
                    tiempoActual++; // Incrementar tiempo de carga actual
                }

                // Aumentar el porcentaje de carga basado en el tiempo actual
                if (porcentajeCarga < 100) {
                    porcentajeCarga = Math.min(100, porcentajeCarga + 1); // Incrementa y asegura que no pase de 100%
                    porcentajeBateria.setText("Batería al " + porcentajeCarga + "%");

                    // Actualizar el tiempo estimado según el porcentaje restante
                    tiempoEstimado = 100 - porcentajeCarga; // 1 minuto por cada 1% de batería
                }

                // Actualizar los TextView con los nuevos valores
                tiempoCargaActual.setText("Tiempo de carga actual: " + tiempoActual + " minutos");
                tiempoCargaEstimado.setText("Tiempo de carga estimado: " + tiempoEstimado + " minutos");

                // Enviar una notificación cuando la carga llegue al 100%
                if (porcentajeCarga == 100) {
                    enviarNotificacion();
                }

                // Continuar la actualización periódica hasta que la carga esté completa
                if (tiempoActual < 100 && porcentajeCarga < 100) {
                    handler.postDelayed(this, 1000); // Actualización cada minuto
                }
            }
        }
    };

    private void enviarNotificacion() {
        // Crear el canal de notificaciones (solo necesario para Android 8.0 o superior)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            String channelId = "carga_completa_channel";
            CharSequence name = "Carga Completa";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(channelId, name, importance);
            NotificationManager notificationManager = getActivity().getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        // Crear la notificación
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getActivity(), "carga_completa_channel")
                .setSmallIcon(R.drawable.icono_estacion) // Aquí puedes colocar el icono de tu notificación
                .setContentTitle("¡Carga Completa!")
                .setContentText("La carga de tu coche ha llegado al 100%.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        // Obtener el NotificationManager y mostrar la notificación
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(getActivity());
        notificationManagerCompat.notify(1, builder.build());
    }

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

        // Codigo camara
        playerView = rootView.findViewById(R.id.player_view);
        player = new ExoPlayer.Builder(getActivity()).build();
        playerView.setPlayer(player);
        String streamUrl = "rtsp://192.168.43.105:8554/live.stream";
        Uri uri = Uri.parse(streamUrl);
        MediaItem mediaItem = MediaItem.fromUri(uri);
        player.setMediaItem(mediaItem);
        player.prepare();
        player.play();

        // Inicializar tiempos aleatorios y porcentaje al cargar la vista
        inicializarTiempos();

        // Subir precios de luz al Firestore solo una vez
        //subirPreciosLuzAFirestore();

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
                Intent intent = getActivity().getIntent();
                if (intent != null) { // Verifica que el Intent no sea null
                    Bundle extras = intent.getExtras();
                    if (extras != null && extras.containsKey("estacionSeleccionada")) { // Verifica que extras no sean null y contenga la clave
                        estacionSelecionada = extras.getBoolean("estacionSeleccionada");
                    } else {
                        estacionSelecionada = false; // Si no hay extras, establece un valor predeterminado
                    }
                } else {
                    estacionSelecionada = false; // Si el Intent es null, establece un valor predeterminado
                }

                if (!estacionSelecionada) {
                    Toast.makeText(getActivity(), "Reserva una estación primero.", Toast.LENGTH_SHORT).show();
                } else {
                    verificarPermisoNotificaciones();
                    isCargaIniciada = true;
                    isCargaDetenida = false; // Reiniciar el estado de detención al iniciar
                }
            }
        });

        // Configurar listener para el botón "Detener carga"
        botonDetenerCarga.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isCargaDetenida = true; // Marcar que se ha detenido la carga
                isCargaIniciada = false; // Detener cualquier actualización de carga
                Toast.makeText(getActivity(), "Carga detenida correctamente", Toast.LENGTH_SHORT).show();

                Intent servicioCargaIntent = new Intent(getActivity(), ServicioCargaCoche.class);
                getActivity().stopService(servicioCargaIntent);

                // Calcular puntos proporcionalmente: 100 puntos por hora (60 minutos)
                int puntosObtenidos = (tiempoActual * 100) / 60;
                Tab1.puntosDisponibles += puntosObtenidos; // Sumar los puntos al total

                Toast.makeText(getActivity(), "Has ganado " + puntosObtenidos + " puntos.", Toast.LENGTH_SHORT).show();

                tiempoActual = 0;
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
            if (recyclerViewPreciosLuz.getVisibility() == View.VISIBLE) {recyclerViewPreciosLuz.setVisibility(View.GONE);
                Toast.makeText(getActivity(), "Ocultando precios de la luz", Toast.LENGTH_SHORT).show();
            } else {
                recyclerViewPreciosLuz.setVisibility(View.VISIBLE);
                cargarPreciosLuzDesdeFirestore();
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
        if (player != null){
            player.release();
        }
    }

    private void subirPreciosLuzAFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        List<PrecioLuz> listaPreciosLuz = generarListaPreciosLuz(); // Esta es la lista que quieres subir

        for (PrecioLuz precioLuz : listaPreciosLuz) {
            // Crear un mapa con los datos del precio de luz
            Map<String, Object> precioLuzMap = new HashMap<>();
            precioLuzMap.put("precio", precioLuz.getPrecioKWh());
            precioLuzMap.put("hora", precioLuz.getHorario());
            precioLuzMap.put("dia", precioLuz.getDiaSemana());

            // Subir a Firestore en la colección "precios_luz"
            db.collection("precios_luz")
                    .add(precioLuzMap)
                    .addOnSuccessListener(documentReference -> {
                        Log.d("Firestore", "Precio de luz añadido con ID: " + documentReference.getId());
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firestore", "Error al añadir precio de luz", e);
                    });
        }
    }

    //Para cargar toda la lista de precios desde el Firestore
    // Método para cargar precios desde Firestore
    private void cargarPreciosLuzDesdeFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("precios_luz")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<PrecioLuz> listaPreciosLuz = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Double precio = document.getDouble("precio");
                        String hora = document.getString("hora");
                        String dia = document.getString("dia");

                        if (precio != null && hora != null && dia != null) {
                            listaPreciosLuz.add(new PrecioLuz(precio, hora, dia));
                        }
                    }

                    // Ordenar la lista por día y luego por hora
                    Collections.sort(listaPreciosLuz, new Comparator<PrecioLuz>() {
                        @Override
                        public int compare(PrecioLuz p1, PrecioLuz p2) {
                            // Comparar por índice del día de la semana
                            int diaComparison = Integer.compare(obtenerIndiceDiaSemana(p1.getDiaSemana()), obtenerIndiceDiaSemana(p2.getDiaSemana()));
                            if (diaComparison != 0) {
                                return diaComparison;
                            }
                            // Si los días son iguales, comparar por horario (hora)
                            return p1.getHorario().compareTo(p2.getHorario());
                        }
                    });

                    // Actualizar el adaptador con la lista ordenada
                    adapter = new PrecioLuzAdapter(listaPreciosLuz);
                    recyclerViewPreciosLuz.setAdapter(adapter);
                    recyclerViewPreciosLuz.setVisibility(View.VISIBLE);

                    // Notificar al adaptador que los datos han cambiado
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error al cargar precios de luz", e);
                    Toast.makeText(getActivity(), "Error al cargar los precios de luz", Toast.LENGTH_SHORT).show();
                });
    }

    // Método para obtener el índice numérico del día de la semana
    private int obtenerIndiceDiaSemana(String diaSemana) {
        switch (diaSemana) {
            case "Lunes":
                return 0;
            case "Martes":
                return 1;
            case "Miércoles":
                return 2;
            case "Jueves":
                return 3;
            case "Viernes":
                return 4;
            case "Sábado":
                return 5;
            case "Domingo":
                return 6;
            default:
                return -1;
        }
    }

}
