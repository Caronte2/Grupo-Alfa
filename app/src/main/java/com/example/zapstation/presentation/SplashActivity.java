package com.example.zapstation.presentation;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.zapstation.R;

public class SplashActivity extends AppCompatActivity {

    //Esto solo sirve para mostrar el splash screen
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageView splashLogo = findViewById(R.id.splashLogo);

        //Animación que se parece a la del Unity
        Animation scaleUp = AnimationUtils.loadAnimation(this, R.anim.intro_logo);

        splashLogo.startAnimation(scaleUp);

        //Tiempo de espera antes de iniciar el MainActivity
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }, 2000);
    }
}
