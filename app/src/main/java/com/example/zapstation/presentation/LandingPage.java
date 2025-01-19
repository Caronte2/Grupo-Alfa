package com.example.zapstation.presentation;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.zapstation.R;
import com.google.firebase.firestore.ListenerRegistration;

public class LandingPage extends Fragment {

    private ListenerRegistration listenerRegistration;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.landing_page, container, false);

        // Inicialización de botones
        Button loginButton = view.findViewById(R.id.loginButton);
        Button registrarButton = view.findViewById(R.id.registrarButton);

        // Configura los listeners
        loginButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CustomLoginActivity.class);
            startActivity(intent);
        });

        registrarButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CustomRegisterActivity.class);
            startActivity(intent);
        });

        return view;
    }
}

