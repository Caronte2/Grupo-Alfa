package com.example.zapstation;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;

public class LoginActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 123;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener authStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        setupAuthStateListener(); // Configura el listener al iniciar la actividad
        login();
    }

    private void login() {
        FirebaseUser usuario = mAuth.getCurrentUser();
        if (usuario != null) {
            verificarCorreo(usuario);
        } else {
            List<AuthUI.IdpConfig> providers = Arrays.asList(
                    new AuthUI.IdpConfig.EmailBuilder().build(),
                    new AuthUI.IdpConfig.GoogleBuilder().build(),
                    new AuthUI.IdpConfig.TwitterBuilder().build() // Agregar Twitter como proveedor
            );
            startActivityForResult(
                    AuthUI.getInstance().createSignInIntentBuilder()
                            .setLogo(R.mipmap.logo_zap)
                            .setTheme(R.style.FirebaseUITema)
                            .setAvailableProviders(providers)
                            .setIsSmartLockEnabled(false)
                            .build(),
                    RC_SIGN_IN);
        }
    }

    private void verificarCorreo(FirebaseUser usuario) {
        if (usuario.isEmailVerified()) {
            iniciarMainActivity(); // El correo ha sido verificado, continuar con la app
        } else {
            enviarCorreoVerificacion(usuario); // El correo no ha sido verificado, enviar verificación
            Toast.makeText(LoginActivity.this,
                    "Verifica tu correo electrónico para continuar.",
                    Toast.LENGTH_LONG).show();

            // Volver a la pantalla de login hasta que el correo sea verificado
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(LoginActivity.this, LoginActivity.class));
            finish(); // Finaliza la actividad actual
        }
    }

    private void enviarCorreoVerificacion(FirebaseUser usuario) {
        usuario.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this,
                                    "Correo de verificación enviado a: " + usuario.getEmail(),
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(LoginActivity.this,
                                    "Error al enviar el correo de verificación: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void setupAuthStateListener() {
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                if (currentUser != null) {
                    verificarCorreo(currentUser);
                }
            }
        };
        mAuth.addAuthStateListener(authStateListener);
    }

    private void iniciarMainActivity() {
        Toast.makeText(this, "Iniciando sesión...", Toast.LENGTH_SHORT).show();
        Intent i = new Intent(this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (authStateListener != null) {
            mAuth.removeAuthStateListener(authStateListener);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                verificarCorreo(user);
            } else {
                // Manejar el error si el inicio de sesión falla
                if (response != null) {
                    Toast.makeText(this, "Error de autenticación: " + response.getError(), Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}
