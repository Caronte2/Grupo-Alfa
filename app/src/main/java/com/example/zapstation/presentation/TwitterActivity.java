package com.example.zapstation.presentation;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.zapstation.R;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.OAuthProvider;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class TwitterActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_twitter);

        firebaseAuth = FirebaseAuth.getInstance();

        Task<AuthResult> pendingResultTask = firebaseAuth.getPendingAuthResult();

        if (pendingResultTask == null) {
            startSignInWithTwitter();
        } else {
            pendingResultTask
                    .addOnSuccessListener(authResult -> handleUser(firebaseAuth.getCurrentUser()))
                    .addOnFailureListener(e ->
                            Toast.makeText(TwitterActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        }
    }

    private void startSignInWithTwitter() {
        OAuthProvider.Builder provider = OAuthProvider.newBuilder("twitter.com");
        provider.addCustomParameter("lang", "es");

        firebaseAuth.startActivityForSignInWithProvider(this, provider.build())
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    if (user != null) {
                        handleUser(user);
                        guardarUsuarioEnFirestore(user, user.getDisplayName()); // Guardar en Firestore
                    }
                })
                .addOnFailureListener(e -> {
                    if (e instanceof FirebaseAuthUserCollisionException) {
                        FirebaseAuthUserCollisionException collisionException = (FirebaseAuthUserCollisionException) e;
                        String existingEmail = collisionException.getEmail();
                        Log.e("TwitterActivity", "Colisión de cuentas: " + existingEmail, e);
                        Toast.makeText(this, "Ya existe una cuenta con este correo. Inicie sesión con su otra cuenta.", Toast.LENGTH_LONG).show();
                        Intent abrirLogin = new Intent(TwitterActivity.this, CustomLoginActivity.class);
                        startActivity(abrirLogin);
                    } else {
                        Toast.makeText(TwitterActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("TwitterActivity", "Error de autenticación", e);
                    }
                });
    }

    //Guardar usuario en firestore
    private void guardarUsuarioEnFirestore(FirebaseUser user, String nombreCompleto) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        HashMap<String, Object> usuario = new HashMap<>();
        usuario.put("uid", user.getUid());
        usuario.put("nombreCompleto", nombreCompleto != null ? nombreCompleto : "Nombre no disponible"); // Nombre o algo por defecto
        usuario.put("correo", user.getEmail()); // Guardar correo del usuario
        usuario.put("metodoAutenticacion", user.getProviderId()); // Método de autenticación (email, google, twitter, etc.)

        db.collection("usuarios").document(user.getUid()).set(usuario)
                .addOnSuccessListener(aVoid -> Toast.makeText(TwitterActivity.this, "Usuario registrado correctamente en Firestore", Toast.LENGTH_LONG).show())
                .addOnFailureListener(e -> Toast.makeText(TwitterActivity.this, "Error al guardar el usuario: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    // Comprobación tipo usuario
    private void handleUser(FirebaseUser user) {
        if (user == null) return;

        Log.d("TwitterActivity", "Usuario autenticado: " + user.getUid() + ", Proveedores: " + user.getProviderData());

        if (user.getEmail() == null || user.isEmailVerified()) {
            iniciarMainActivity();
        } else {
            enviarCorreoVerificacion(user);
        }
    }

    //Iniciar el mainActivity
    private void iniciarMainActivity() {
        Toast.makeText(this, "Iniciando sesión...", Toast.LENGTH_SHORT).show();
        Intent i = new Intent(this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

    //Enviar correo para verificar a un usuario
    private void enviarCorreoVerificacion(FirebaseUser user) {
        user.sendEmailVerification()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Correo de verificación enviado a: " + user.getEmail(), Toast.LENGTH_LONG).show();
                        // Y lo enviamos al login
                        Intent abrirLogin = new Intent(TwitterActivity.this, CustomLoginActivity.class);
                        startActivity(abrirLogin);
                    } else {
                        Toast.makeText(this, "Error al enviar el correo: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
