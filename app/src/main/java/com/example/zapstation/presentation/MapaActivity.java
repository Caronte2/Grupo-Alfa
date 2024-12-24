package com.example.zapstation.presentation;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.zapstation.R;
import com.example.zapstation.data.EstacionesLista;
import com.example.zapstation.model.Estacion;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MapaActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mapa;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Estacion estacionSeleccionada;

    private FirebaseFirestore db;  // Firebase Firestore
    private List<Estacion> estacionesList;  // Lista de estaciones cargadas

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa);

        //Solo se usa una vez pa subir a Firestore
        //EstacionesLista estacionesLista = new EstacionesLista();
        //estacionesLista.añadeEjemplos();

        // Inicializar Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // Inicializar la lista de estaciones
        estacionesList = new ArrayList<>();

        // Inicializar el mapa
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapa);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);  // Esto llamará a onMapReady cuando el mapa esté listo
        }

        // Botón "Ver Estación"
        Button verEstacionButton = findViewById(R.id.verEstacion);
        verEstacionButton.setOnClickListener(v -> {
            if (estacionSeleccionada != null) {
                // Cargar la información de la estación desde Firestore
                cargarInformacionEstacion(estacionSeleccionada.getNombre());  // Buscar por el nombre de la estación
            } else {
                // Mostrar un mensaje si no se ha seleccionado ninguna estación
                Toast.makeText(MapaActivity.this, "Por favor, selecciona una estación primero.", Toast.LENGTH_SHORT).show();
            }
        });

        // Botón para volver al tab2
        Button volver_atrasButton = findViewById(R.id.volver_atras2);
        volver_atrasButton.setOnClickListener(v -> {
            Intent intent = new Intent(MapaActivity.this, MainActivity.class);
            intent.putExtra("tab_index", 1); // Cambiar al Tab2 (índice 1)
            startActivity(intent);
        });

        // Inicializar el LocationManager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (mapa != null) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    LatLng currentLocation = new LatLng(latitude, longitude);
                    mapa.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}

            @Override
            public void onProviderEnabled(String provider) {}

            @Override
            public void onProviderDisabled(String provider) {}
        };

        // Para pedir permisos
        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 10, locationListener);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, 1);
        }
    }

    // Método que devuelve el mapa de Google una vez cargado
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mapa = googleMap;
        mapa.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mapa.getUiSettings().setZoomControlsEnabled(true);

        // Ahora puedes agregar los marcadores, ya que el mapa está listo
        cargarEstaciones();

        // Listener para cuando se hace clic en un marcador
        mapa.setOnMarkerClickListener(marker -> {
            // Solo se selecciona la estación correspondiente
            for (Estacion estacion : estacionesList) {
                if (marker.getTitle().equals(estacion.getNombre())) {
                    estacionSeleccionada = estacion;
                    break;
                }
            }
            // No abrir nada al hacer clic en el marcador
            return false;  // Indica que el evento sigue siendo procesado
        });
    }

    // Método para cargar estaciones desde Firestore
    private void cargarEstaciones() {
        db.collection("estaciones")  // Colección 'estaciones'
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Estacion estacion = document.toObject(Estacion.class);

                            // Verificar si la estación tiene coordenadas válidas
                            if (estacion != null && estacion.getPosicion() != null) {
                                GeoPoint posicion = estacion.getPosicion();
                                LatLng ubicacion = new LatLng(posicion.getLatitude(), posicion.getLongitude());

                                // Agregar marcador al mapa
                                agregarMarcador(estacion, ubicacion);

                                // Agregar la estación a la lista
                                estacionesList.add(estacion);
                            }
                        }
                    } else {
                        Toast.makeText(MapaActivity.this, "Error al cargar estaciones.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Método para agregar marcador en el mapa
    private void agregarMarcador(Estacion estacion, LatLng ubicacion) {
        // Obtener el icono para el marcador
        Bitmap iGrande = BitmapFactory.decodeResource(getResources(), R.drawable.hotel);
        Bitmap icono = Bitmap.createScaledBitmap(iGrande, iGrande.getWidth() / 7, iGrande.getHeight() / 7, false);

        // Agregar marcador al mapa
        mapa.addMarker(new MarkerOptions()
                .position(ubicacion)
                .title(estacion.getNombre())
                .snippet(estacion.getDireccion())
                .icon(BitmapDescriptorFactory.fromBitmap(icono)));
    }

    // Método para cargar la información de la estación seleccionada desde Firestore
    private void cargarInformacionEstacion(String estacionNombre) {
        db.collection("estaciones")
                .whereEqualTo("nombre", estacionNombre)  // Filtramos por nombre
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Estacion estacion = document.toObject(Estacion.class);

                            // Crear el Intent para pasar la información
                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("nombreEstacion", estacion.getNombre());
                            resultIntent.putExtra("direccionEstacion", estacion.getDireccion());
                            resultIntent.putExtra("valoracion", (float) estacion.getValoracion());
                            resultIntent.putExtra("comentario", estacion.getComentario());

                            // Obtener la imagen (en este caso es un URL de imagen)
                            resultIntent.putExtra("imagenEstacion", estacion.getFoto());
                            //Log.d("IMAGEN-ESTACION", estacion.getFoto());

                            // Enviar el resultado
                            setResult(RESULT_OK, resultIntent);
                            finish();
                        }
                    } else {
                        Toast.makeText(MapaActivity.this, "No se encontró la información de la estación.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Quitar los listeners
    @Override
    protected void onPause() {
        super.onPause();
        if (locationManager != null) {
            locationManager.removeUpdates(locationListener);
        }
    }

    // Para los permisos
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 10, locationListener);
            }
        }
    }
}
