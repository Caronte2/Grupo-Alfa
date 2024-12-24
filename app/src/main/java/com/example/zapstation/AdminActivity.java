package com.example.zapstation;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class AdminActivity extends AppCompatActivity {

    private FirebaseUser usuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        usuario = FirebaseAuth.getInstance().getCurrentUser();

        ImageView btnCerrarSesion = findViewById(R.id.btn_cerrar_sesion);
        // Establecer OnClickListener para la ImageView de cerrar sesión
        btnCerrarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarDialogoCerrarSesion();
            }
        });
    }

    // Método para mostrar el popup para confirmar si desea cerrar sesión
    private void mostrarDialogoCerrarSesion() {
        // Crear el AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(AdminActivity.this);
        builder.setTitle("Cerrar sesión");
        builder.setMessage("¿Deseas cerrar sesión?");

        // Botón "Sí" para confirmar cerrar sesión
        builder.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Lógica para ir a la actividad de inicio de sesión o registro
                FirebaseAuth.getInstance().signOut(); // Cierra sesión en Firebase
                Intent intent = new Intent(AdminActivity.this, MainActivity.class);
                startActivity(intent);
                finish(); // Finaliza la actividad actual
            }
        });

        // Botón "No" para cancelar la acción
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Cerrar el diálogo sin hacer nada
                dialog.dismiss();
            }
        });

        // Mostrar el diálogo
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}