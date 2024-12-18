package com.example.zapstation;

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
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapaActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mapa;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private RepositorioEstaciones estaciones;
    private Estacion estacionSeleccionada; // Variable para guardar la estación seleccionada

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa);

        estaciones = ((Aplicacion) getApplication()).estaciones;
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.mapa);
        mapFragment.getMapAsync(this);

        // Botón "Ver Estación"
        Button verEstacionButton = findViewById(R.id.verEstacion);
        verEstacionButton.setOnClickListener(v -> {
            if (estacionSeleccionada != null) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("nombreEstacion", estacionSeleccionada.getNombre());
                resultIntent.putExtra("direccionEstacion", estacionSeleccionada.getDireccion());
                resultIntent.putExtra("valoracion", estacionSeleccionada.getValoracion());
                resultIntent.putExtra("comentario", estacionSeleccionada.getComentario());

                int imagenResId = estacionSeleccionada.getFoto();
                resultIntent.putExtra("imagenEstacion", imagenResId);

                // Enviar resultado y cerrar MapaActivity
                setResult(RESULT_OK, resultIntent);
                finish();
            } else {
                // Mostrar un mensaje si no se ha seleccionado ninguna estación
                Toast.makeText(MapaActivity.this, "Por favor, selecciona una estación primero.", Toast.LENGTH_SHORT).show();
            }
        });

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

        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 10, locationListener);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, 1);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mapa = googleMap;
        mapa.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mapa.getUiSettings().setZoomControlsEnabled(true);

        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mapa.setMyLocationEnabled(true);
        }

        // Agregar los marcadores en el mapa
        for (int n = 0; n < estaciones.tamaño(); n++) {
            Estacion estacion = estaciones.elemento(n);
            GeoPunto p = estacion.getPosicion();
            if (p != null && p.getLatitud() != 0) {
                Bitmap iGrande = BitmapFactory.decodeResource(
                        getResources(), estacion.getTipo().getRecurso());
                Bitmap icono = Bitmap.createScaledBitmap(iGrande,
                        iGrande.getWidth() / 7, iGrande.getHeight() / 7, false);
                mapa.addMarker(new MarkerOptions()
                        .position(new LatLng(p.getLatitud(), p.getLongitud()))
                        .title(estacion.getNombre())
                        .snippet(estacion.getDireccion())
                        .icon(BitmapDescriptorFactory.fromBitmap(icono)));
            }
        }

        // Configurar el listener de los marcadores
        mapa.setOnMarkerClickListener(marker -> {
            String estacionNombre = marker.getTitle();

            // Buscar la estación correspondiente al marcador seleccionado
            for (int i = 0; i < estaciones.tamaño(); i++) {
                Estacion estacion = estaciones.elemento(i);
                if (estacion.getNombre().equals(estacionNombre)) {
                    estacionSeleccionada = estacion; // Guardar la estación seleccionada
                    break;
                }
            }

            // Mostrar un mensaje o realizar alguna acción
            Toast.makeText(this, "Has seleccionado: " + estacionNombre, Toast.LENGTH_SHORT).show();
            return true; // Indicar que el evento fue manejado
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (locationManager != null) {
            locationManager.removeUpdates(locationListener);
        }
    }

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
