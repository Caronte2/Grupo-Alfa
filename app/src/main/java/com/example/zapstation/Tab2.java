package com.example.zapstation;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Tab2 extends Fragment {

    private GoogleMap mapa;
    private RepositorioEstaciones estaciones;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        estaciones = ((Aplicacion) getActivity().getApplication()).estaciones;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tab2, container, false);


        // Obtener el botón de Reservar
        Button reservarButton = view.findViewById(R.id.reservar);
        ImageView like = view.findViewById(R.id.like);
        ImageView dislike = view.findViewById(R.id.dislike);

        // Mapa de Google pequeño
        FloatingActionButton openMapButton = view.findViewById(R.id.openMapButton);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapaPequeño);

        // Configurar el mapa pequeño
        if (mapFragment != null) {
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    mapa = googleMap;
                    mapa.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    if (ActivityCompat.checkSelfPermission(getActivity(), ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        mapa.setMyLocationEnabled(true);
                        mapa.getUiSettings().setZoomControlsEnabled(true);
                        mapa.getUiSettings().setCompassEnabled(true);
                    }
                    if (estaciones.tamaño() > 0) {
                        GeoPunto p = estaciones.elemento(0).getPosicion();
                        mapa.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                new LatLng(p.getLatitud(), p.getLongitud()), 10));
                    }
                    for (int n=0; n<estaciones.tamaño(); n++) {
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
            });
        }
        // Configurar el botón para abrir el mapa grande
        openMapButton.setOnClickListener(v -> {
            // Iniciar la actividad de mapa grande
            Intent intent = new Intent(getActivity(), MapaActivity.class);
            startActivity(intent);
        });

        // Configurar el comportamiento del botón de Reservar
        reservarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();
                if (usuario != null) {
                    // Si el usuario está autenticado, mostrar un Toast
                    Toast.makeText(getActivity(), "Reserva confirmada", Toast.LENGTH_SHORT).show();
                } else {
                    // Si el usuario no está autenticado, mostrar el AlertDialog
                    mostrarDialogoRegistro();
                }
            }
        });

        // Configurar el comportamiento del like
        like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();
                if (usuario != null) {
                    // Si el usuario está autenticado, mostrar un mensaje de like
                    Toast.makeText(getActivity(), "Valoración positiva", Toast.LENGTH_SHORT).show();
                } else {
                    // Si el usuario no está autenticado, mostrar el AlertDialog
                    mostrarDialogoRegistro();
                }
            }
        });

        // Configurar el comportamiento del dislike
        dislike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();
                if (usuario != null) {
                    // Si el usuario está autenticado, mostrar un mensaje de dislike
                    Toast.makeText(getActivity(), "Valoración negativa", Toast.LENGTH_SHORT).show();
                } else {
                    // Si el usuario no está autenticado, mostrar el AlertDialog
                    mostrarDialogoRegistro();
                }
            }
        });

        return view;
    }

    // Mostrar el popup para confirmar si desea registrarse
    private void mostrarDialogoRegistro() {
        // Crear el AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Acción de sesión necesaria");
        builder.setMessage("Para continuar necesitas tener una sesión iniciada. ¿Deseas hacerlo ahora?");

        // Botón para ir a registrarse
        builder.setPositiveButton("Registrar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Lógica para ir a la actividad de registro (puedes adaptarla)
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
            }
        });

        // Botón para cancelar la acción
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Cerrar el diálogo
                dialog.dismiss();
            }
        });

        // Mostrar el diálogo
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
