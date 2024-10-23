package com.example.zapstation;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Tab2 extends Fragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tab2, container, false);

        // Obtener el botón de Reservar
        Button reservarButton = view.findViewById(R.id.reservar);

        // Configurar el comportamiento del botón
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

        return view;
    }

    // Mostrar el popup para confirmar si desea registrarse
    private void mostrarDialogoRegistro() {
        // Crear el AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Registro necesario");
        builder.setMessage("Para reservar necesitas registrarte. ¿Deseas hacerlo ahora?");

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

