package com.example.zapstation;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    // Nombres de las pestañas
    private String[] nombres;
    // Variables para almacenar el estado de autenticación y verificación de correo del usuario
    private boolean usuarioAutentificado;
    private boolean correoVerificado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Obtener la instancia actual del usuario en Firebase
        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();

        // Obtener SharedPreferences para guardar el estado del Toast
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        boolean isToastShown = prefs.getBoolean("isToastShown", false);

        // Comprobar si el usuario está autentificado
        if (usuario != null) {
            try {
                // Si el usuario está autenticado, se comprueba si su correo está verificado
                usuarioAutentificado = true;
                correoVerificado = usuario.isEmailVerified();

                // Mostrar un aviso si el correo no está verificado
                if (!correoVerificado) {
                    Toast.makeText(this, "Por favor, verifica tu correo electrónico para acceder a todas las funciones.", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            // Si no está autentificado, se inicializan las variables a false
            usuarioAutentificado = false;
            correoVerificado = false;
            if (!isToastShown) {
                Toast.makeText(this, "Inicia sesión para acceder a todas las funciones.", Toast.LENGTH_SHORT).show();

                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("isToastShown", true);
                editor.apply();
            }
        }

        // Configurar los nombres de las pestañas según el estado del usuario (autenticado y con correo verificado o no)
        nombres = usuarioAutentificado && correoVerificado
                ? new String[]{"", "", "", "", ""}
                : new String[]{"", "", ""};

        // Configurar el ViewPager2 para deslizar entre fragmentos (pestañas)
        ViewPager2 viewPager = findViewById(R.id.viewpager);
        viewPager.setAdapter(new MiPagerAdapter(this, usuarioAutentificado, correoVerificado));

        // Configurar el TabLayout (barra de pestañas) y asociarlo con el ViewPager2
        TabLayout tabs = findViewById(R.id.tabs);
        new TabLayoutMediator(tabs, viewPager,
                (tab, position) -> {
                    // Asignar el texto y los iconos de las pestañas según la posición y el estado del usuario
                    tab.setText(nombres[position]);
                    if (correoVerificado) {
                        // Si el correo está verificado, muestra estas 5 pestañas con iconos
                        switch (position) {
                            case 0: tab.setIcon(R.drawable.star); break;
                            case 1: tab.setIcon(R.drawable.map_pin); break;
                            case 2: tab.setIcon(R.drawable.home); break;
                            case 3: tab.setIcon(R.drawable.battery_charging); break;
                            case 4: tab.setIcon(R.drawable.user); break;
                        }
                    } else {
                        // Si no está verificado, muestra solo 3 pestañas con estos iconos
                        switch (position) {
                            case 0: tab.setIcon(R.drawable.map_pin); break;
                            case 1: tab.setIcon(R.drawable.home); break;
                            case 2: tab.setIcon(R.drawable.info_help); break;
                        }
                    }
                }).attach();

        // Establecer la pestaña de Home como la activa por defecto (posición 2 si está verificado, posición 1 si no)
        viewPager.setCurrentItem(correoVerificado ? 2 : 1, false);
    }

    // Adaptador personalizado para gestionar los fragmentos asociados a las pestañas
    public static class MiPagerAdapter extends FragmentStateAdapter {
        private boolean usuarioAutentificado;
        private boolean correoVerificado;

        // Constructor que recibe la actividad y el estado del usuario
        public MiPagerAdapter(FragmentActivity activity, boolean usuarioAutenticado, boolean correoVerificado) {
            super(activity);
            this.usuarioAutentificado = usuarioAutenticado;
            this.correoVerificado = correoVerificado;
        }

        // Definir cuántas pestañas mostrar según el estado del usuario
        @Override
        public int getItemCount() {
            return (usuarioAutentificado && correoVerificado) ? 5 : 3;
        }

        // Crear los fragmentos asociados a cada pestaña según la posición y el estado del usuario
        @Override
        @NonNull
        public Fragment createFragment(int position) {
            if (usuarioAutentificado && correoVerificado) {
                // Si el usuario está autentificado y el correo verificado, muestra estos 5 fragmentos
                switch (position) {
                    case 0:
                        return new Tab1();
                    case 1:
                        return new Tab2();
                    case 2:
                        return new Tab3(); // Home / Hay dos clases tab3 y customlogin
                    case 3:
                        return new Tab4();
                    case 4:
                        return new Tab5();
                }
            } else {
                // Si no está verificado, muestra solo estos 3 fragmentos
                switch (position) {
                    case 0:
                        return new Tab2(); // Map
                    case 1:
                        return new Tab3(); // Home / Hay dos clases tab3 y customlogin
                    case 2:
                        return new TabInfo(); // Info
                }
            }
            return null;
        }
    }
}
