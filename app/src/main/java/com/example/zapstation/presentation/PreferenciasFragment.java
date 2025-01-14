package com.example.zapstation.presentation;

import android.os.Bundle;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.example.zapstation.R;

public class PreferenciasFragment extends PreferenceFragmentCompat {  // Cambié el nombre de la clase aquí
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Cargar las preferencias desde el archivo XML
        setPreferencesFromResource(R.xml.preferenciasadmin, rootKey);

        final EditTextPreference maximo = findPreference("max_estaciones");  // Asegúrate de que el nombre coincida

        if (maximo != null) {
            // Establecer el resumen inicial basado en el valor actual de la preferencia
            String valorActual = maximo.getText();
            if (valorActual != null && !valorActual.isEmpty()) {
                maximo.setSummary("Limita el número de estaciones que se muestran (" + valorActual + ")");
            } else {
                maximo.setSummary("Limita el número de estaciones que se muestran (0)");
            }

            // Escuchar cambios en la preferencia
            maximo.setOnPreferenceChangeListener(
                    new Preference.OnPreferenceChangeListener() {
                        @Override
                        public boolean onPreferenceChange(Preference preference, Object newValue) {
                            int valor;
                            try {
                                valor = Integer.parseInt((String) newValue);  // Parseamos el valor como entero
                            } catch (Exception e) {
                                // Si no es un número válido
                                Toast.makeText(getActivity(), "Debe ser un número", Toast.LENGTH_SHORT).show();
                                return false;
                            }

                            if (valor >= 0 && valor <= 99) {
                                // Si el valor está dentro del rango permitido, actualizamos el resumen
                                maximo.setSummary("Limita el número de estaciones que se muestran (" + valor + ")");
                                return true;
                            } else {
                                // Si el valor excede el rango
                                Toast.makeText(getActivity(), "Valor máximo permitido: 99", Toast.LENGTH_SHORT).show();
                                return false;
                            }
                        }
                    });
        }
    }
}
