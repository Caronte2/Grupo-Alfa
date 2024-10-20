package com.example.zapstation;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MainActivity extends AppCompatActivity {
    // Nombres de las pestañas
    private String[] nombres = new String[]{"", "", "", "", ""};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ViewPager2 viewPager = findViewById(R.id.viewpager);
        viewPager.setAdapter(new MiPagerAdapter(this));

        TabLayout tabs = findViewById(R.id.tabs);
        new TabLayoutMediator(tabs, viewPager,
                new TabLayoutMediator.TabConfigurationStrategy() {
                    @Override
                    public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                        // Dejar el texto vacío
                        tab.setText(nombres[position]);

                        // Establecer un ícono para cada tab
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
                    }
                }).attach();
        // Establecer Home como la pestaña activa al inicio
        viewPager.setCurrentItem(2, false); // 2 es el índice de la pestaña Home
    }

    public static class MiPagerAdapter extends FragmentStateAdapter {
        public MiPagerAdapter(FragmentActivity activity) {
            super(activity);
        }

        @Override
        public int getItemCount() {
            return 5; // Cambia a la cantidad de pestañas que tienes
        }

        @Override
        @NonNull
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new Tab1();
                case 1:
                    return new Tab2();
                case 2:
                    return new Tab3();
                case 3:
                    return new Tab4();
                case 4:
                    return new Tab5();
            }
            return null;
        }
    }
}