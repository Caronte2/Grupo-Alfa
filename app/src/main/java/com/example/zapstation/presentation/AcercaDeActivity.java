package com.example.zapstation.presentation;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.zapstation.R;

public class AcercaDeActivity extends AppCompatActivity {

    //Para mostrar la información de Acerca de
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acerca_de);

        //No me gusta el modo noche
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        //Botón para volver atrás
        Button volver_atrasButton = findViewById(R.id.volver_atras);
        volver_atrasButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("tab_index", 5); // Cambiar al Tab2 (índice 1)
            startActivity(intent);
        });
    }
}
