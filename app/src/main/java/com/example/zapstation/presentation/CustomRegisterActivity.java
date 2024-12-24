package com.example.zapstation.presentation;

import static android.app.PendingIntent.getActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.zapstation.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import java.util.HashMap;

public class CustomRegisterActivity extends AppCompatActivity {

    //Variables globales
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private ViewGroup contenedor;
    private EditText etNombreCompleto, etCorreo, etContraseña;
    private TextInputLayout tilNombre, tilCorreo, tilContraseña;
    private ProgressDialog dialogo;
    ImageView btnTwitter;
    Button iniciarSesion;

    //Google
    private GoogleSignInClient googleSignInClient;
    private static final int RC_GOOGLE_SIGN_IN = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_register);

        //Obtener instancia de Firebase
        auth = FirebaseAuth.getInstance();
        initUIComponents();
        setupGoogleSignIn();
    }

    //Metodo para inicializar todos los componentes
    private void initUIComponents() {
        etNombreCompleto = findViewById(R.id.etNombreCompleto); // Campo de nombre completo
        etCorreo = findViewById(R.id.correo);
        etContraseña = findViewById(R.id.contraseña);
        tilNombre = findViewById(R.id.til_nombre);
        tilCorreo = findViewById(R.id.til_correo);
        tilContraseña = findViewById(R.id.til_contraseña);
        btnTwitter = findViewById(R.id.twitter);
        contenedor = findViewById(R.id.contenedor);
        dialogo = new ProgressDialog(this);
        dialogo.setTitle("Creando cuenta");
        dialogo.setMessage("Por favor espere...");
        iniciarSesion = findViewById(R.id.inicio_sesion);

        Button registerButton = findViewById(R.id.registro);
        registerButton.setOnClickListener(this::registroCorreo);

        //Botón volver atras
        Button volver_atrasButton = findViewById(R.id.volver_atras);
        volver_atrasButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        });

        //Botón para iniciar sesión
        iniciarSesion.setOnClickListener(v -> {
            Intent login = new Intent(CustomRegisterActivity.this, CustomLoginActivity.class);
            startActivity(login);
        });

        // Botón(imageview) para registrarse con Google
        ImageView googleRegisterImageView = findViewById(R.id.googleRegisterImageView);
        googleRegisterImageView.setOnClickListener(this::autentificarGoogle);

        //Twitter
        // Botón para iniciar sesión con Twitter
        btnTwitter.setOnClickListener(v -> {
            Intent intent = new Intent(CustomRegisterActivity.this, TwitterActivity.class);
            startActivity(intent);
        });
    }

    //Metodo para autentificar con Google + lo de abajo
    public void autentificarGoogle(View v) {
        Intent i = googleSignInClient.getSignInIntent();
        startActivityForResult(i, RC_GOOGLE_SIGN_IN);
    }

    private void setupGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);
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
        auth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        currentUser = auth.getCurrentUser();
                        if (currentUser != null) {
                            guardarUsuarioEnFirestore(currentUser, currentUser.getDisplayName()); // Guardar en Firestore
                            verificarCorreo(currentUser);
                        }
                    } else {
                        mensaje(task.getException().getLocalizedMessage());
                    }
                });
    }

    //Metodo para verificar correo
    private void verificarCorreo(FirebaseUser user) {
        if (user.isEmailVerified()) {
            iniciarMainActivity();
        } else {
            enviarCorreoVerificacion(user);
            Toast.makeText(this, "Verifica tu correo para continuar.", Toast.LENGTH_LONG).show();
        }
    }

    //Para iniciar el MainActivity
    private void iniciarMainActivity() {
        Toast.makeText(this, "Iniciando sesión...", Toast.LENGTH_SHORT).show();
        Intent i = new Intent(this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

    //Verifica el correo y agrega al usuario a Firebase
    public void registroCorreo(View v) {
        if (verificaCampos()) {
            dialogo.show();
            auth.createUserWithEmailAndPassword(etCorreo.getText().toString(), etContraseña.getText().toString())
                    .addOnCompleteListener(task -> {
                        dialogo.dismiss();
                        if (task.isSuccessful()) {
                            currentUser = auth.getCurrentUser();
                            if (currentUser != null) {
                                enviarCorreoVerificacion(currentUser);
                                guardarUsuarioEnFirestore(currentUser, etNombreCompleto.getText().toString()); // Guardar en Firestore
                            }
                        } else {
                            mensaje(task.getException().getLocalizedMessage());
                        }
                    });
        }
    }

    //Este si que es el que agrega al usuario a FireStore
    private void guardarUsuarioEnFirestore(FirebaseUser user, String nombreCompleto) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        HashMap<String, Object> usuario = new HashMap<>();
        usuario.put("uid", user.getUid());
        usuario.put("nombreCompleto", nombreCompleto);
        usuario.put("correo", user.getEmail());
        usuario.put("metodoAutenticacion", user.getProviderId());
        usuario.put("rol", "usuario");

        db.collection("usuarios").document(user.getUid()).set(usuario)
                .addOnSuccessListener(aVoid -> mensaje("Usuario registrado correctamente en Firestore"))
                .addOnFailureListener(e -> mensaje("Error al guardar el usuario: " + e.getMessage()));
    }

    //Para enivar el correo de verificación
    private void enviarCorreoVerificacion(FirebaseUser user) {
        user.sendEmailVerification().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Correo de verificación enviado a: " + user.getEmail(), Toast.LENGTH_LONG).show();
                finish();
            } else {
                mensaje("Error al enviar el correo de verificación");
            }
        });
    }

    //Para verificar los campos
    private boolean verificaCampos() {
        String nombreCompleto = etNombreCompleto.getText().toString();
        String correo = etCorreo.getText().toString();
        String contraseña = etContraseña.getText().toString();

        tilNombre.setError("");
        tilCorreo.setError("");
        tilContraseña.setError("");

        if (nombreCompleto.isEmpty()) {
            tilNombre.setError("Introduce tu nombre completo");
        } else if (correo.isEmpty()) {
            tilCorreo.setError("Introduce un correo");
        } else if (!correo.matches(".+@.+[.].+")) {
            tilCorreo.setError("Correo no válido");
        } else if (contraseña.isEmpty()) {
            tilContraseña.setError("Introduce una contraseña");
        } else if (contraseña.length() < 6) {
            tilContraseña.setError("Debe contener al menos 6 caracteres");
        } else if (!contraseña.matches(".*[0-9].*")) {
            tilContraseña.setError("Debe contener un número");
        } else if (!contraseña.matches(".*[A-Z].*")) {
            tilContraseña.setError("Debe contener una letra mayúscula");
        } else {
            return true;
        }
        return false;
    }

    private void mensaje(String mensaje) {
        Snackbar.make(contenedor, mensaje, Snackbar.LENGTH_LONG).show();
    }
}
