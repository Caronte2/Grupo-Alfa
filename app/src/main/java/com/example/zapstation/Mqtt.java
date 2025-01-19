package com.example.zapstation;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatDelegate;

import com.example.zapstation.presentation.AdminActivity;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class Mqtt extends Activity implements MqttCallback {

    private static final String TAG = "MqttActivity";
    private TextView proximidad, gps;
    private MqttClient mqttClient;
    private final String serverUri = "tcp://192.168.43.105:1883"; //192.168.224.105 la raspberry
    private final String topic = "proximidad/1";
    private final String topic2 = "gps/1";
    private final String clientId = "AndroidClient";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mqtt);

        //No me gusta el modo noche
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        proximidad = findViewById(R.id.proximidad);
        gps = findViewById(R.id.gps);

        try {
            mqttClient = new MqttClient(serverUri, clientId, new MemoryPersistence());
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);

            mqttClient.setCallback(this);
            mqttClient.connect(options);
            mqttClient.subscribe(topic);
            mqttClient.subscribe(topic2);

        } catch (MqttException e) {
            Log.e(TAG, "Error al conectar o suscribirse al broker MQTT", e);
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        Log.e(TAG, "Conexión perdida con el broker MQTT", cause);

        // Intentar reconectar
        try {
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            mqttClient.connect(options);
            mqttClient.subscribe(topic);
            mqttClient.subscribe(topic2);
            Log.i(TAG, "Reconexión exitosa");
        } catch (MqttException e) {
            Log.e(TAG, "Error al intentar reconectar", e);
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        final String data = new String(message.getPayload());
        Log.i(TAG, "Mensaje recibido en el tópico " + topic + ": " + data);

        // Guardar datos en SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("MqttData", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        runOnUiThread(() -> {
            switch (topic) {
                case "proximidad/1":
                    proximidad.setText("Proximidad: " + data + "m");
                    editor.putString("proximidad", data);
                    break;
                case "gps/1":
                    gps.setText("GPS: " + data + "º");
                    editor.putString("gps", data);
                    break;
            }
            editor.apply();
        });

        AdminActivity.actualizarMqtt(this);
    }


    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        Log.i(TAG, "Mensaje entregado con éxito");
    }
}
