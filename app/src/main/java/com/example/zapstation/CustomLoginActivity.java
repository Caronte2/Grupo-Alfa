package com.example.zapstation;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class CustomLoginActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private ViewGroup contenedor;
    private EditText etCorreo, etContraseña;
    private TextInputLayout tilCorreo, tilContraseña;
    private ProgressDialog dialogo;
    //Google
    private GoogleSignInClient googleSignInClient;
    private static final int RC_GOOGLE_SIGN_IN = 123;

    //Twitter
    ImageView btnTwitter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_login);

        auth = FirebaseAuth.getInstance();
        etCorreo = findViewById(R.id.correo);
        etContraseña = findViewById(R.id.contraseña);
        tilCorreo = findViewById(R.id.til_correo);
        tilContraseña = findViewById(R.id.til_contraseña);
        contenedor = findViewById(R.id.contenedor);
        btnTwitter = findViewById(R.id.twitter);
        dialogo = new ProgressDialog(this);
        dialogo.setTitle("Verificando usuario");
        dialogo.setMessage("Por favor espere...");

        verificaSiUsuarioValidado();  // Verifica al iniciar si ya está logeado

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        Button googleLoginButton = findViewById(R.id.googleLogin);

        // Asigna el listener al botón
        googleLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                autentificarGoogle(v);
            }
        });

        btnTwitter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CustomLoginActivity.this, TwitterActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        });
    }

    private void verificarCorreo(FirebaseUser user) {
        if (user.isEmailVerified()) {
            iniciarMainActivity();
        } else {
            boolean emailSent = getSharedPreferences("ZapStationPrefs", MODE_PRIVATE)
                    .getBoolean("verificationEmailSent", false);
            if (!emailSent) {
                enviarCorreoVerificacion(user); // Solo envía si no se envió anteriormente
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(this, "Verifica tu correo para continuar.", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Ya se ha enviado un correo de verificación. Revisa tu bandeja de entrada.", Toast.LENGTH_LONG).show();
            }
        }
    }



    private void enviarCorreoVerificacion(FirebaseUser user) {
        user.sendEmailVerification()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Correo de verificación enviado a: " + user.getEmail(), Toast.LENGTH_LONG).show();
                        // Almacena en SharedPreferences que se ha enviado el correo
                        getSharedPreferences("ZapStationPrefs", MODE_PRIVATE)
                                .edit()
                                .putBoolean("verificationEmailSent", true)
                                .apply();
                    } else {
                        Toast.makeText(this, "Error al enviar el correo: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }


    public void inicioSesionCorreo(View v) {
        if (verificaCampos()) {
            dialogo.show();
            auth.signInWithEmailAndPassword(etCorreo.getText().toString(), etContraseña.getText().toString())
                    .addOnCompleteListener(task -> {
                        dialogo.dismiss();
                        if (task.isSuccessful()) {
                            currentUser = auth.getCurrentUser();
                            if (currentUser != null) verificarCorreo(currentUser);
                        } else {
                            mensaje(task.getException().getLocalizedMessage());
                        }
                    });
        }
    }

    public void registroCorreo(View v) {
        if (verificaCampos()) {
            dialogo.show();
            auth.createUserWithEmailAndPassword(etCorreo.getText().toString(), etContraseña.getText().toString())
                    .addOnCompleteListener(task -> {
                        dialogo.dismiss();
                        if (task.isSuccessful()) {
                            currentUser = auth.getCurrentUser();
                            if (currentUser != null) enviarCorreoVerificacion(currentUser);
                        } else {
                            mensaje(task.getException().getLocalizedMessage());
                        }
                    });
        }
    }

    public void autentificarGoogle(View v) {
        Intent i = googleSignInClient.getSignInIntent();
        startActivityForResult(i, RC_GOOGLE_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_GOOGLE_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) googleAuth(account.getIdToken());
            } catch (ApiException e) {
                mensaje("Error de autentificación con Google");
            }
        }
    }

    private void googleAuth(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        // Usamos currentUser definido en la clase
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            // Si ya hay un usuario autenticado, intenta vincular la cuenta
            currentUser.linkWithCredential(credential)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Actualizamos currentUser con el nuevo estado
                            this.currentUser = FirebaseAuth.getInstance().getCurrentUser();
                            verificarCorreo(this.currentUser);
                        } else {
                            mensaje(task.getException().getLocalizedMessage());
                        }
                    });
        } else {
            // Si no hay usuario autenticado, inicia sesión normalmente
            auth.signInWithCredential(credential)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Actualizamos currentUser con el nuevo estado
                            this.currentUser = auth.getCurrentUser();
                            if (this.currentUser != null) verificarCorreo(this.currentUser);
                        } else {
                            mensaje(task.getException().getLocalizedMessage());
                        }
                    });
        }
    }


    private void verificaSiUsuarioValidado() {
        currentUser = auth.getCurrentUser();
        if (currentUser != null && currentUser.isEmailVerified()) {
            iniciarMainActivity();
        } else if (currentUser != null) {
            enviarCorreoVerificacion(currentUser);
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(this, "Verifica tu correo para continuar.", Toast.LENGTH_LONG).show();
        }
    }

    private void iniciarMainActivity() {
        Toast.makeText(this, "Iniciando sesión...", Toast.LENGTH_SHORT).show();
        // Reiniciar el estado de envío de correo al iniciar la actividad principal
        getSharedPreferences("ZapStationPrefs", MODE_PRIVATE)
                .edit()
                .putBoolean("verificationEmailSent", false)
                .apply();
        Intent i = new Intent(this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }


    private boolean verificaCampos() {
        String correo = etCorreo.getText().toString();
        String contraseña = etContraseña.getText().toString();
        tilCorreo.setError("");
        tilContraseña.setError("");

        if (correo.isEmpty()) tilCorreo.setError("Introduce un correo");
        else if (!correo.matches(".+@.+[.].+")) tilCorreo.setError("Correo no válido");
        else if (contraseña.isEmpty()) tilContraseña.setError("Introduce una contraseña");
        else if (contraseña.length() < 6) tilContraseña.setError("Debe contener al menos 6 caracteres");
        else if (!contraseña.matches(".*[0-9].*")) tilContraseña.setError("Debe contener un número");
        else if (!contraseña.matches(".*[A-Z].*")) tilContraseña.setError("Debe contener una letra mayúscula");
        else return true;

        return false;
    }

    private void mensaje(String mensaje) {
        Snackbar.make(contenedor, mensaje, Snackbar.LENGTH_LONG).show();
    }
}

