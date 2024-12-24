package com.example.zapstation.presentation;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.zapstation.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

public class CustomLoginActivity extends AppCompatActivity {

    //Variables globales
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private ViewGroup contenedor;
    private Button btnRegistro;
    private EditText etCorreo, etContraseña;
    private TextInputLayout tilCorreo, tilContraseña;
    private ProgressDialog dialogo;
    private GoogleSignInClient googleSignInClient;
    private static final int RC_GOOGLE_SIGN_IN = 123;
    ImageView btnTwitter;
    TextView contrasenyaOlvidada;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_login);

        //Obtener instancias de Firebase y FiresStore
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        initUIComponents();
        setupGoogleSignIn();
        verificaSiUsuarioValidado();

        //Botón para cambiar contraseña olvidada
        contrasenyaOlvidada = findViewById(R.id.contrasenyaOlvidada);
        contrasenyaOlvidada.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cambiarContrasenya();
            }
        });

    }

    //Metodo para cambiar contraseña olvidada
    public void cambiarContrasenya(){

        //Crear un dialogo a base de un xml
        EditText resetMail = new EditText(CustomLoginActivity.this);
        AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(CustomLoginActivity.this);
        passwordResetDialog.setTitle("¿Quieres cambiar la contraseña?");
        passwordResetDialog.setMessage("Escribe tu correo para recibir el link");
        passwordResetDialog.setView(resetMail);

        //Botón positivo
        passwordResetDialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String mail= resetMail.getText().toString();
                auth.sendPasswordResetEmail(mail).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(CustomLoginActivity.this, "Se ha enviado un link para cambiar la contraseña a tú correo.", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(CustomLoginActivity.this, "Error, no se ha enviado el mensaje" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        //Botón negativo para cerrarlo
        passwordResetDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        passwordResetDialog.create().show();
    }

    //Metodo para inicializar todos los componentes
    private void initUIComponents() {
        etCorreo = findViewById(R.id.correo);
        etContraseña = findViewById(R.id.contraseña);
        tilCorreo = findViewById(R.id.til_correo);
        tilContraseña = findViewById(R.id.til_contraseña);
        contenedor = findViewById(R.id.contenedor);
        btnTwitter = findViewById(R.id.twitter);
        btnRegistro = findViewById(R.id.registroLogin);
        dialogo = new ProgressDialog(this);
        dialogo.setTitle("Verificando usuario");
        dialogo.setMessage("Por favor espere...");

        Button volver_atrasButton = findViewById(R.id.volver_atras);
        volver_atrasButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        });

        // Botón para iniciar sesión con Google
        ImageView googleLoginImageView = findViewById(R.id.googleLoginImageView);
        googleLoginImageView.setOnClickListener(this::autentificarGoogle);

        // Botón para iniciar sesión con Twitter
        btnTwitter.setOnClickListener(v -> {
            Intent intent = new Intent(CustomLoginActivity.this, TwitterActivity.class);
            startActivity(intent);
        });

        btnRegistro.setOnClickListener(v -> {
            Intent intent = new Intent(CustomLoginActivity.this, CustomRegisterActivity.class);
            startActivity(intent);
        });

        // Botón para iniciar sesión con correo y contraseña
        Button loginButton = findViewById(R.id.inicio_sesion);
        loginButton.setOnClickListener(this::inicioSesionCorreo);
    }

    private void setupGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void verificaSiUsuarioValidado() {
        currentUser = auth.getCurrentUser();
        if (currentUser != null && currentUser.isEmailVerified()) {
            iniciarMainActivity();
        }
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

    //Metodo para verificar correo
    private void verificarCorreo(FirebaseUser user) {
        if (user.isEmailVerified()) {
            cargarDatosUsuario(user.getUid());
        } else {
            enviarCorreoVerificacion(user);
            Toast.makeText(this, "Verifica tu correo para continuar.", Toast.LENGTH_LONG).show();
        }
    }

    //Metodo para cargar los datos del usuario desde FireStore
    private void cargarDatosUsuario(String uid) {
        firestore.collection("usuarios").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    // Verificar el rol del usuario
                    verificarRol(uid);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar datos del usuario: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void verificarRol(String uid) {
        firestore.collection("usuarios").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Verificar el rol del usuario
                        String rol = documentSnapshot.getString("rol");
                        if ("administrador".equals(rol)) {
                            iniciarActividadAdmin();
                        } else {
                            // El usuario es un usuario normal, redirigir al MainActivity
                            iniciarMainActivity();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al verificar el rol: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    // Si hay un error en la consulta, también redirigir al MainActivity
                    iniciarMainActivity();
                });
    }

    private void iniciarActividadAdmin() {
        Toast.makeText(this, "Bienvenido, administrador.", Toast.LENGTH_SHORT).show();
        Intent adminIntent = new Intent(this, AdminActivity.class);
        adminIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(adminIntent);
        finish();
    }

    //Para enivar el correo de verificación
    private void enviarCorreoVerificacion(FirebaseUser user) {
        user.sendEmailVerification().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Correo de verificación enviado a: " + user.getEmail(), Toast.LENGTH_LONG).show();
            } else {
                mensaje("Error al enviar el correo de verificación");
            }
        });
    }

    //Metodo para autentificar con Google + lo de abajo
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

    //EEEE no lo voy a borrar por si hiciera algo porque esto es de lo más antiguo
    //pero para el proximo Sprint si que lo borraré y lo probaré
    private void sugerirRegistro() {
        Snackbar.make(contenedor, "No se encontró una cuenta de Google. ¿Deseas registrarte?", Snackbar.LENGTH_LONG)
                .setAction("Registrar", v -> {
                    Intent intent = new Intent(CustomLoginActivity.this, CustomRegisterActivity.class);
                    startActivity(intent);
                }).show();
    }

    //Metodo para autentificar con Google + lo de abajo
    private void googleAuth(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        currentUser = auth.getCurrentUser();
                        if (currentUser != null) {
                            // Comprueba si la cuenta sigue activa
                            auth.getCurrentUser().reload().addOnCompleteListener(reloadTask -> {
                                if (reloadTask.isSuccessful() && currentUser.getUid() != null) {
                                    verificarCorreo(currentUser);
                                } else {
                                    // La cuenta no existe en Firebase
                                    auth.signOut();
                                    googleSignInClient.signOut();
                                    mensaje("Esta cuenta fue eliminada. Por favor, regístrate nuevamente.");
                                }
                            });
                        }
                    } else {
                        mensaje(task.getException().getLocalizedMessage());
                    }
                });
    }

    //Para iniciar el MainActivity
    private void iniciarMainActivity() {
        Toast.makeText(this, "Iniciando sesión...", Toast.LENGTH_SHORT).show();
        Intent i = new Intent(this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

    //Para verificar los campos
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
        //else if (!contraseña.matches(".*[A-Z].*")) tilContraseña.setError("Debe contener una letra mayúscula");
        //Comentado porque luego hay problemas si cambio la contraseña
        else return true;

        return false;
    }

    private void mensaje(String mensaje) {
        Snackbar.make(contenedor, mensaje, Snackbar.LENGTH_LONG).show();
    }
}
