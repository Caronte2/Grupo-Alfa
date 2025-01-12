package com.example.zapstation;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class Mqtt extends Activity implements MqttCallback {

    private static final String topic = "dbayluj/test";
    private static final String hello = "Bon dia Gandia";
    private static final int qos = 1;
    private static final boolean retain = false;
    private static final String broker = "tcp://test.mosquitto.org:1883";
    private static final String clientId = "Test134568789";
    private MqttClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            client = new MqttClient(broker, clientId, new
                    MemoryPersistence()); // Conexión con el bróker
            MqttConnectOptions connOpts = new
                    MqttConnectOptions();
            connOpts.setCleanSession(true);
            connOpts.setKeepAliveInterval(60);
            connOpts.setWill(topic,
                    "Desconectada!".getBytes(), qos, retain);
            client.connect(connOpts);
            client.setCallback(this); // Callback
            // Suscripción al tópico
            client.subscribe(topic, qos);
            Log.i("MQTT", "Suscripción al tópico: " +
                    topic);
            MqttMessage message = new
                    MqttMessage(hello.getBytes());
            message.setQos(qos);
            client.publish(topic, message); // Envío
            Log.i("MQTT", "Mensaje enviado: " + hello);
        } catch (MqttException e) {
            Log.e("MQTT", "Error al conectar con el bróker: " + e.getMessage());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (client != null && client.isConnected()) {
                client.disconnect();
                Log.i("MQTT", "Cliente desconectado");
            }
        } catch (MqttException e) {
            Log.e("MQTT", "Error al desconectar el cliente: " + e.getMessage());
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        Log.i("MQTT", "Conexión perdida...");
    }

    @Override
    public void messageArrived(String topic, MqttMessage mens) throws Exception {
        String payload = new String(mens.getPayload());
        Log.i("MQTT", "Hemos recibido el mensaje: " +
                payload);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        Log.i("MQTT", "Entrega completa!");
    }
}
