package com.example.zapstation;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa);

        estaciones = ((Aplicacion) getApplication()).estaciones;
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.mapa);
        mapFragment.getMapAsync(this);

        // Inicializar el LocationManager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Crear un LocationListener para obtener la ubicación
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (mapa != null) {
                    // Obtenemos la latitud y longitud
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();

                    // Mover la cámara a la ubicación actual del usuario
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

        // Verificar permisos y solicitar ubicación
        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 10, locationListener);
        } else {
            // Solicitar permiso al usuario si no se tiene
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
            mapa.getUiSettings().setZoomControlsEnabled(true);
            mapa.getUiSettings().setCompassEnabled(true);
        }

        // Agregar los marcadores en el mapa (Estaciones)
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
                        .title(estacion.getNombre()).snippet(estacion.getDireccion())
                        .icon(BitmapDescriptorFactory.fromBitmap(icono)));
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Dejar de recibir actualizaciones de ubicación cuando la actividad esté en pausa
        if (locationManager != null) {
            locationManager.removeUpdates(locationListener);
        }
    }

    // Método para manejar la respuesta de los permisos
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Permiso concedido, iniciar el proceso de ubicación
            if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 10, locationListener);
            }
        }
    }
}
