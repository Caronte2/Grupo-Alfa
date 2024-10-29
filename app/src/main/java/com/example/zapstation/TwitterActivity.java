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
        setContentView(R.layout.activity_twitter); // Asegúrate de tener este layout

        firebaseAuth = FirebaseAuth.getInstance();

        // Intenta obtener un resultado pendiente de autenticación
        Task<AuthResult> pendingResultTask = firebaseAuth.getPendingAuthResult();

        // Si no hay un resultado pendiente, inicia el flujo de autenticación
        if (pendingResultTask == null) {
            startSignInWithTwitter();
        } else {
            // Procesar el resultado pendiente
            pendingResultTask
                    .addOnSuccessListener(authResult -> handleUser(firebaseAuth.getCurrentUser()))
                    .addOnFailureListener(e ->
                            Toast.makeText(TwitterActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        }
    }

    private void startSignInWithTwitter() {
        OAuthProvider.Builder provider = OAuthProvider.newBuilder("twitter.com");
        provider.addCustomParameter("lang", "fr");

        firebaseAuth.startActivityForSignInWithProvider(this, provider.build())
                .addOnSuccessListener(authResult -> handleUser(firebaseAuth.getCurrentUser()))
                .addOnFailureListener(e ->
                        Toast.makeText(TwitterActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void handleUser(FirebaseUser user) {
        if (user == null) return;

        if (user.isEmailVerified() || user.getEmail() == null) {
            iniciarMainActivity();
        } else {
            enviarCorreoVerificacion(user);
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
