package com.example.zapstation;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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
    private boolean usuarioAutenticado;
    private boolean correoVerificado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Obtener la instancia actual del usuario en Firebase
        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();

        // Comprobar si el usuario está autenticado y si el correo está verificado
        if (usuario != null) {
            usuarioAutenticado = true;
            correoVerificado = usuario.isEmailVerified();
        } else {
            usuarioAutenticado = false;
            correoVerificado = false;
            Toast.makeText(this, "Inicia sesión para acceder a todas las funciones", Toast.LENGTH_SHORT).show();
        }

        // Configurar las pestañas para usuarios autenticados y no autenticados
        nombres = usuarioAutenticado && correoVerificado
                ? new String[]{"", "", "", "", ""}
                : new String[]{"", "", ""};

        ViewPager2 viewPager = findViewById(R.id.viewpager);
        viewPager.setAdapter(new MiPagerAdapter(this, usuarioAutenticado, correoVerificado));

        TabLayout tabs = findViewById(R.id.tabs);
        new TabLayoutMediator(tabs, viewPager,
                (tab, position) -> {
                    tab.setText(nombres[position]);
                    if (correoVerificado) {
                        switch (position) {
                            case 0: tab.setIcon(R.drawable.star); break;
                            case 1: tab.setIcon(R.drawable.map_pin); break;
                            case 2: tab.setIcon(R.drawable.home); break;
                            case 3: tab.setIcon(R.drawable.battery_charging); break;
                            case 4: tab.setIcon(R.drawable.settings); break;
                        }
                    } else {
                        // Configurar solo los 3 tabs si no está verificado
                        switch (position) {
                            case 0: tab.setIcon(R.drawable.map_pin); break;
                            case 1: tab.setIcon(R.drawable.home); break;
                            case 2: tab.setIcon(R.drawable.info_help); break;
                        }
                    }
                }).attach();

        // Establecer la pestaña de Home como la activa
        viewPager.setCurrentItem(correoVerificado ? 2 : 1, false);
    }



    public static class MiPagerAdapter extends FragmentStateAdapter {
        private boolean usuarioAutenticado;
        private boolean correoVerificado;

        public MiPagerAdapter(FragmentActivity activity, boolean usuarioAutenticado, boolean correoVerificado) {
            super(activity);
            this.usuarioAutenticado = usuarioAutenticado;
            this.correoVerificado = correoVerificado;
        }

        @Override
        public int getItemCount() {
            return (usuarioAutenticado && correoVerificado) ? 5 : 3;
        }

        @Override
        @NonNull
        public Fragment createFragment(int position) {
            if (usuarioAutenticado && correoVerificado) {
                switch (position) {
                    case 0:
                        return new Tab1();
                    case 1:
                        return new Tab2();
                    case 2:
                        return new Tab3(); // Home
                    case 3:
                        return new Tab4();
                    case 4:
                        return new Tab5();
                }
            } else {
                switch (position) {
                    case 0:
                        return new Tab2(); // Map
                    case 1:
                        return new Tab3(); // Home
                    case 2:
                        return new TabInfo(); // Info
                }
            }
            return null;
        }
    }
}
