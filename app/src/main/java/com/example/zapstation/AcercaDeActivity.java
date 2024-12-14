package com.example.zapstation;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class AcercaDeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acerca_de);

        Button volver_atrasButton = findViewById(R.id.volver_atras);
        volver_atrasButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("tab_index", 5); // Cambiar al Tab2 (índice 1)
            startActivity(intent);
        });
    }
}
