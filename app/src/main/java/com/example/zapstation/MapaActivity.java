package com.example.zapstation;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
    private RepositorioEstaciones estaciones;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa);
        estaciones = ((Aplicacion) getApplication()).estaciones;
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.mapa);
        mapFragment.getMapAsync(this);
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mapa = googleMap;
        mapa.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mapa.setMyLocationEnabled(true);
            mapa.getUiSettings().setZoomControlsEnabled(true);
            mapa.getUiSettings().setCompassEnabled(true);
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
                        .title(estacion.getNombre()).snippet(estacion.getDireccion())
                        .icon(BitmapDescriptorFactory.fromBitmap(icono)));
            }
        }

        // Agregar un listener para cuando se haga clic en un marcador
        mapa.setOnMarkerClickListener(marker -> {
            // Obtener la estación asociada al marcador
            String estacionNombre = marker.getTitle();
            String estacionDireccion = marker.getSnippet();

            // Crear el Fragmento para mostrar la información de la estación
            Tab2 tab2Fragment = new Tab2();

            // Pasar la información de la estación a Tab2
            Bundle args = new Bundle();
            args.putString("estacion_nombre", estacionNombre);
            args.putString("estacion_direccion", estacionDireccion);
            tab2Fragment.setArguments(args);

            // Reemplazar el fragmento actual por Tab2
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.mapa, tab2Fragment) // Asegúrate de que el contenedor sea el adecuado
                    .addToBackStack(null)
                    .commit();

            return true;
        });
    }

}
