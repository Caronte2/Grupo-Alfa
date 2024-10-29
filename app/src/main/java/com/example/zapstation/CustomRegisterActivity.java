package com.example.zapstation;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;


public class CustomRegisterActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private ViewGroup contenedor;
    private EditText etNombreCompleto, etCorreo, etContraseña;
    private TextInputLayout tilNombre, tilCorreo, tilContraseña;
    private ProgressDialog dialogo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_register);

        auth = FirebaseAuth.getInstance();
        initUIComponents();
    }

    private void initUIComponents() {
        etNombreCompleto = findViewById(R.id.etNombreCompleto); // Campo de nombre completo
        etCorreo = findViewById(R.id.correo);
        etContraseña = findViewById(R.id.contraseña);
        tilNombre = findViewById(R.id.til_nombre);
        tilCorreo = findViewById(R.id.til_correo);
        tilContraseña = findViewById(R.id.til_contraseña);
        contenedor = findViewById(R.id.contenedor);
        dialogo = new ProgressDialog(this);
        dialogo.setTitle("Creando cuenta");
        dialogo.setMessage("Por favor espere...");

        Button registerButton = findViewById(R.id.registro);
        registerButton.setOnClickListener(this::registroCorreo);
    }

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
                                guardarNombreEnFirestore(currentUser.getUid(), etNombreCompleto.getText().toString());
                            }
                        } else {
                            mensaje(task.getException().getLocalizedMessage());
                        }
                    });
        }
    }

    private void guardarNombreEnFirestore(String uid, String nombreCompleto) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        HashMap<String, Object> usuario = new HashMap<>();
        usuario.put("nombreCompleto", nombreCompleto);

        db.collection("usuarios").document(uid).set(usuario)
                .addOnSuccessListener(aVoid -> mensaje("Nombre guardado correctamente"))
                .addOnFailureListener(e -> mensaje("Error al guardar el nombre: " + e.getMessage()));
    }

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
