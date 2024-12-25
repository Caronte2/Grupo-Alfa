package com.example.zapstation.presentation;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.zapstation.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.material.navigation.NavigationView;

public class AdminActivity extends AppCompatActivity {

    private FirebaseUser usuario;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        usuario = FirebaseAuth.getInstance().getCurrentUser();

        // Configuración del DrawerLayout y NavigationView
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mNavigationView = findViewById(R.id.nav_view);

        // Configurar el Toolbar y el ícono de hamburguesa
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.menu);  // Asegúrate de tener este ícono

        // Configurar el listener para los ítems del menú
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_log_out) {
                    mostrarDialogoCerrarSesion();
                }

                // Cierra el drawer
                mDrawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });

    }

    // Método para mostrar el popup de confirmar cerrar sesión
    private void mostrarDialogoCerrarSesion() {
        AlertDialog.Builder builder = new AlertDialog.Builder(AdminActivity.this);
        builder.setTitle("Cerrar sesión");
        builder.setMessage("¿Deseas cerrar sesión?");

        builder.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                FirebaseAuth.getInstance().signOut(); // Cierra sesión en Firebase
                Intent intent = new Intent(AdminActivity.this, MainActivity.class);
                startActivity(intent);
                finish(); // Finaliza la actividad actual
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss(); // Cierra el diálogo sin hacer nada
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Manejar la selección del ícono de hamburguesa
        if (item.getItemId() == android.R.id.home) {
            mDrawerLayout.openDrawer(GravityCompat.START); // Abrir el menú hamburguesa
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
