package com.example.zapstation;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MqttService extends Service implements MqttCallback {

    private static final String TAG = "MqttService";
    private MqttClient mqttClient;
    private final String serverUri = "tcp://192.168.43.105:1883"; // Dirección del broker
    private final String[] topics = {"proximidad/1", "gps/1"};
    private final String clientId = "ZapStation2025";

    @Override
    public void onCreate() {
        super.onCreate();

        try {
            mqttClient = new MqttClient(serverUri, clientId, new MemoryPersistence());
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);

            mqttClient.setCallback(this);
            mqttClient.connect(options);

            for (String topic : topics) {
                mqttClient.subscribe(topic);
            }

            Log.i(TAG, "Servicio MQTT iniciado y suscrito a los tópicos.");

        } catch (MqttException e) {
            Log.e(TAG, "Error al iniciar el servicio MQTT", e);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Mantener el servicio ejecutándose hasta que se detenga explícitamente
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            if (mqttClient != null && mqttClient.isConnected()) {
                mqttClient.disconnect();
            }
        } catch (MqttException e) {
            Log.e(TAG, "Error al desconectar el cliente MQTT", e);
        }
        Log.i(TAG, "Servicio MQTT detenido.");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null; // No se usa para servicios no vinculados
    }

    @Override
    public void connectionLost(Throwable cause) {
        Log.e(TAG, "Conexión perdida con el broker MQTT", cause);
        try {
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            mqttClient.connect(options);

            for (String topic : topics) {
                mqttClient.subscribe(topic);
            }
            Log.i(TAG, "Reconexión exitosa.");
        } catch (MqttException e) {
            Log.e(TAG, "Error al intentar reconectar", e);
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        String data = new String(message.getPayload());
        Log.i(TAG, "Mensaje recibido en el tópico " + topic + ": " + data);

        // Guardar en SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("MqttData", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        switch (topic) {
            case "proximidad/1":
                editor.putString("proximidad", data);
                break;
            case "gps/1":
                editor.putString("gps", data);
                break;
        }
        editor.apply();

        // Notificar la actualización
        Intent broadcastIntent = new Intent("com.example.zapstation.MQTT_UPDATE");
        sendBroadcast(broadcastIntent);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        Log.i(TAG, "Mensaje entregado con éxito.");
    }
}
