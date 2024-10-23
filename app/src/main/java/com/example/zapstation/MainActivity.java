package com.example.zapstation;

import android.content.Intent;
import android.os.Bundle;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Verificar si el usuario está autenticado en Firebase
        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();
        usuarioAutenticado = (usuario != null);

        // Si está autenticado, mostrar los 5 tabs; si no, mostrar solo 3
        if (usuarioAutenticado) {
            nombres = new String[]{"", "", "", "", ""};
        } else {
            nombres = new String[]{"", "", ""}; // Solo mostrar 3 tabs si no está autenticado
        }

        ViewPager2 viewPager = findViewById(R.id.viewpager);
        viewPager.setAdapter(new MiPagerAdapter(this, usuarioAutenticado));

        TabLayout tabs = findViewById(R.id.tabs);
        new TabLayoutMediator(tabs, viewPager,
                new TabLayoutMediator.TabConfigurationStrategy() {
                    @Override
                    public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                        tab.setText(nombres[position]);

                        // Establecer un ícono para cada tab
                        if (usuarioAutenticado) {
                            switch (position) {
                                case 0:
                                    tab.setIcon(R.drawable.star);
                                    break;
                                case 1:
                                    tab.setIcon(R.drawable.map_pin);
                                    break;
                                case 2:
                                    tab.setIcon(R.drawable.home);
                                    break;
                                case 3:
                                    tab.setIcon(R.drawable.battery_charging);
                                    break;
                                case 4:
                                    tab.setIcon(R.drawable.settings);
                                    break;
                            }
                        } else {
                            // Mostrar el nuevo orden para 3 tabs: [Nuevo icono, Home, Map]
                            switch (position) {
                                case 0:
                                    tab.setIcon(R.drawable.map_pin); // Map a la izq
                                    break;
                                case 1:
                                    tab.setIcon(R.drawable.home); // Home en el centro
                                    break;
                                case 2:
                                    tab.setIcon(R.drawable.info_help); // Info a la derecha
                                    break;
                            }
                        }
                    }
                }).attach();

        // Establecer Home como la pestaña activa al inicio
        viewPager.setCurrentItem(usuarioAutenticado ? 2 : 1, false); // 2 es el índice de Home, 1 cuando no está autenticado
    }

    public static class MiPagerAdapter extends FragmentStateAdapter {
        private boolean usuarioAutenticado;

        public MiPagerAdapter(FragmentActivity activity, boolean usuarioAutenticado) {
            super(activity);
            this.usuarioAutenticado = usuarioAutenticado;
        }

        @Override
        public int getItemCount() {
            return usuarioAutenticado ? 5 : 3; // Si el usuario está autenticado, mostrar 5 tabs, de lo contrario, solo 3
        }

        @Override
        @NonNull
        public Fragment createFragment(int position) {
            if (usuarioAutenticado) {
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
                    // Aquí van los nuevos tabs
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