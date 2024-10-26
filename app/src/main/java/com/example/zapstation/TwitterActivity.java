package com.example.zapstation;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.OAuthProvider;
import com.google.firebase.auth.AuthResult;

public class TwitterActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_twitter); // Asegúrate de que tienes este layout

        firebaseAuth = FirebaseAuth.getInstance();

        // Intenta obtener un resultado pendiente de autenticación
        Task<AuthResult> pendingResultTask = firebaseAuth.getPendingAuthResult();

        // Si no hay un resultado pendiente, inicia el flujo de autenticación
        if (pendingResultTask == null) {
            startSignInWithTwitter();
        } else {
            // Procesar el resultado pendiente
            pendingResultTask
                    .addOnSuccessListener(authResult -> {
                        // El usuario está autenticado
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null) {
                            handleUser(user); // Manejar el usuario autenticado
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Manejar el error
                        Toast.makeText(TwitterActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void startSignInWithTwitter() {
        OAuthProvider.Builder provider = OAuthProvider.newBuilder("twitter.com");
        provider.addCustomParameter("lang", "fr");

        firebaseAuth.startActivityForSignInWithProvider(this, provider.build())
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    if (user != null) {
                        handleUser(user); // Manejar el usuario autenticado
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(TwitterActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void handleUser(FirebaseUser user) {
        // Verifica si el usuario tiene un correo electrónico
        if (user.getEmail() != null) {
            if (!user.isEmailVerified()) {
                // Verifica si se ha enviado un correo de verificación
                boolean emailSent = getSharedPreferences("ZapStationPrefs", MODE_PRIVATE)
                        .getBoolean("verificationEmailSent", false);
                if (!emailSent) {
                    enviarCorreoVerificacion(user); // Enviar correo de verificación
                }
            } else {
                // Si el correo está verificado, iniciar MainActivity
                iniciarMainActivity();
            }
        } else {
            // Si no hay correo electrónico, iniciar MainActivity
            iniciarMainActivity();
        }
    }

    private void iniciarMainActivity() {
        Toast.makeText(this, "Iniciando sesión...", Toast.LENGTH_SHORT).show();
        Intent i = new Intent(this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

    private void enviarCorreoVerificacion(FirebaseUser user) {
        user.sendEmailVerification()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Correo de verificación enviado a: " + user.getEmail(), Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "Error al enviar el correo: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

}
