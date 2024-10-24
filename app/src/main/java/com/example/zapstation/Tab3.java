package com.example.zapstation;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Tab3 extends Fragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tab3, container, false);

        Button loginButton = view.findViewById(R.id.loginButton);
        //TODO Que registre se diferencie de inicar sesión
        Button registrarButton = view.findViewById(R.id.registrarButton);
        TextView texto1 = view.findViewById(R.id.texto1);
        TextView texto2 = view.findViewById(R.id.texto2);
        TextView texto3 = view.findViewById(R.id.texto3);
        TextView texto4 = view.findViewById(R.id.texto4);

        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();

        TextView textBienvenido = view.findViewById(R.id.textBienvenido);


        if (usuario == null) {
            loginButton.setVisibility(View.VISIBLE);
            registrarButton.setVisibility(View.VISIBLE);
            texto1.setVisibility(View.VISIBLE);
            texto2.setVisibility(View.VISIBLE);

            //Registrado
            textBienvenido.setVisibility(View.GONE);
            texto3.setVisibility(View.GONE);
            texto4.setVisibility(View.GONE);


            //Llamada LoginAcitivy para usar firebase en el inicio de seión
            loginButton.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
            });

            registrarButton.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
            });
        } else {
            loginButton.setVisibility(View.GONE);
            registrarButton.setVisibility(View.GONE);
            texto1.setVisibility(View.GONE);
            texto2.setVisibility(View.GONE);

            //Registrado
            textBienvenido.setVisibility(View.VISIBLE);
            textBienvenido.setText("Bienvenido" + " " +  usuario.getDisplayName());
            texto3.setVisibility(View.VISIBLE);
            texto4.setVisibility(View.VISIBLE);
        }

        return view;
    }
}
