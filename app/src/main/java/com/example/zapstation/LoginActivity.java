package com.example.zapstation;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
        setupAuthStateListener();
        login();
    }

    /*
     Inicia el proceso de autentificación.
     Si el usuario ya está autentificado, se verifica su correo.
     Si no, se abre la pantalla de selección de métodos de login.
    */

    private void login() {
        FirebaseUser usuario = mAuth.getCurrentUser();
        if (usuario != null) {
            // Si el usuario ya está autentificado, verifica si su correo está verificado.
            verificarCorreo(usuario);
        } else {
            // Configura los proveedores de inicio de sesión disponibles (Email, Google, Twitter)
            List<AuthUI.IdpConfig> providers = Arrays.asList(
                    new AuthUI.IdpConfig.EmailBuilder().build(),
                    new AuthUI.IdpConfig.GoogleBuilder().build(),
                    new AuthUI.IdpConfig.TwitterBuilder().build()
            );
            // Inicia la pantalla de login utilizando FirebaseUI
            startActivityForResult(
                    AuthUI.getInstance().createSignInIntentBuilder()
                            .setLogo(R.mipmap.ic_zap_logo_launcher)
                            .setTheme(R.style.FirebaseUITema)
                            .setAvailableProviders(providers)
                            .setIsSmartLockEnabled(false)
                            .build(),
                    RC_SIGN_IN);
        }
    }

    /*
     Verifica si el correo electrónico del usuario ha sido verificado.
     Si no está verificado, envía un correo de verificación y cierra la sesión hasta que lo verifique.
    */
    private void verificarCorreo(FirebaseUser usuario) {
        if (usuario.isEmailVerified()) {
            // Si el correo ya fue verificado, inicia la actividad principal
            iniciarMainActivity();
        } else {
            // Si el correo no ha sido verificado, se envía el correo de verificación
            enviarCorreoVerificacion(usuario);
            Toast.makeText(LoginActivity.this,
                    "Verifica tu correo electrónico para continuar.",
                    Toast.LENGTH_LONG).show();

            // Cierra la sesión y vuelve a la pantalla de login hasta que el correo esté verificado
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(LoginActivity.this, LoginActivity.class));
            finish();
        }
    }

    /*
     Envía un correo de verificación al usuario.
    */
    private void enviarCorreoVerificacion(FirebaseUser usuario) {
        usuario.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Notifica al usuario que se ha enviado un correo de verificación
                            Toast.makeText(LoginActivity.this,
                                    "Correo de verificación enviado a: " + usuario.getEmail(),
                                    Toast.LENGTH_LONG).show();
                        } else {
                            // Notifica si hubo un error al enviar el correo de verificación
                            Toast.makeText(LoginActivity.this,
                                    "Error al enviar el correo de verificación: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    /*
     Configura un listener que verifica el estado de autentificación del usuario.
     Si el usuario cambia su estado (inicia o cierra sesión), se ejecuta este listener.
    */
    private void setupAuthStateListener() {
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                if (currentUser != null) {
                    // Si el usuario está autentificado, verificar su correo
                    verificarCorreo(currentUser);
                }
            }
        };
        // Agrega el listener al objeto FirebaseAuth
        mAuth.addAuthStateListener(authStateListener);
    }

    /*
     Inicia la MainActivity cuando el usuario ha iniciado sesión y verificado su correo.
    */
    private void iniciarMainActivity() {
        Toast.makeText(this, "Iniciando sesión...", Toast.LENGTH_SHORT).show();
        Intent i = new Intent(this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

    // Remueve el listener de autentificación al destruir la actividad
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (authStateListener != null) {
            mAuth.removeAuthStateListener(authStateListener);
        }
    }

    /*
     Maneja el resultado de la actividad de autentificación.
    */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // El usuario se ha autentificado correctamente
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                verificarCorreo(user);
            } else {
                // Maneja el error si el inicio de sesión falla
                if (response != null) {
                    Toast.makeText(this, "Error de autentificación: " + response.getError(), Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}
