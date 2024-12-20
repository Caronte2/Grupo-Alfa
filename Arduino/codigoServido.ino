#include <M5Stack.h>
#include "WiFi.h"
#include "AsyncUDP.h"
#include <ArduinoJson.h>
#include <TinyGPS++.h>
#include <PubSubClient.h>
// Pines de los LEDs
#define ledVerde 23
#define ledRoja 19
//-----------------Configuración wifi-------------
const char* ssid = "Rusell pagame";
const char* password = "123456789";
//------------------Declaro udp---------------------
AsyncUDP udp;
//Formato json
StaticJsonDocument<200> jsonBufferRecv;
//--------------------GPS------------------------
//Array donde se guarda la información
char texto[200];
char respuesta[150];
char segundo_en_formato_string[10]; 
// Declaro los pines del sensor del Gps
#define RXD2 16
#define TXD2 17

#define GPS_BAUD 9600

// Declaro un objeto gps
TinyGPSPlus gps;

// Crea una instancia de la clase HardwareSerial para el puerto serie 2.
HardwareSerial gpsSerial(2);

//distancia
int distancia;
//----------------------------------MQTT-----------------------------------------------
const char *mqtt_server = "192.168.89.105";
const int mqtt_port = 1883; 
WiFiClient espClient;
PubSubClient client(espClient);
char msg[50];
char msg2[50];


//--------------------------------------------------------------------------------------------
//                                    Configurar udp
//--------------------------------------------------------------------------------------------

void encenderLed(){
  Serial.println(distancia);
  // Control de los LEDs según la distancia
  if (distancia < 10) {
    // Si la distancia es menor a 10 cm, encender ledRoja
    digitalWrite(ledVerde, LOW);
    digitalWrite(ledRoja, HIGH);
  } else {
    // Si la distancia es mayor a 10 encender la ledVerde
    digitalWrite(ledVerde, HIGH);
    digitalWrite(ledRoja, LOW);
  } 
}
//-------------------------------------------------------------------------------------------
//                                      Setup
//-------------------------------------------------------------------------------------------
void setup() {
  Serial.begin(115200);
  // Configurar los pines de los LEDs
  pinMode(ledVerde, OUTPUT); //salida
  pinMode(ledRoja, OUTPUT); //salida
  Serial.println("Pines conectados");
  delay(5000);

  WiFi.mode(WIFI_STA);
  WiFi.begin(ssid, password);
  if (WiFi.waitForConnectResult() != WL_CONNECTED){
    Serial.println("Error al conectar a la WiFI");
    while(1){
      delay(1000);
    }
  }else{
    Serial.println("Se a conectado correctamente");
  }
  delay(5000);

    // Iniciamos el listener UDP en el puerto 1234
  if(udp.listen(1234)){
    Serial.println("UDP escuchando en IP: ");
    Serial.println(WiFi.localIP());
    udp.onPacket([](AsyncUDPPacket packet){
      int i=200;
      while (i--) {
        *(texto+i)=*(packet.data()+i);
      }
      // recibido = 1;
    });
  }else{
    Serial.println("UDP no funciona");
  }
  delay(5000);

  client.setServer(mqtt_server,mqtt_port);
}

//-----------------------------------------------------------------------------------------
//                            Configurar sensor Gps
//-----------------------------------------------------------------------------------------
void sensorGps(){
  //Este programa muestra información cada vez que una nueva frase se codifica correctamente.
  unsigned long start = millis();

  while (millis() - start < 1000) {
    while (gpsSerial.available() > 0) {
      gps.encode(gpsSerial.read());
    }if (gps.location.isUpdated()) {
      // Latitud
      Serial.print("LAT: ");
      Serial.println(gps.location.lat(), 6);
      // Longitud
      Serial.print("LONG: "); 
      Serial.println(gps.location.lng(), 6);
    }
  }
}

//------------------------------------------------------------------------------------------
//                                      Loop
//------------------------------------------------------------------------------------------
void loop() {
    if (!client.connected()) {
    reconnect();
  }
  client.loop();
  // Concatenamos 2 strings
  snprintf(respuesta, sizeof(respuesta), "SERVIDOR JSON: he recibido de CLIENTE lo siguiente: %s", texto);
  // Enviamos el string concatenado a la red
  udp.broadcastTo(respuesta, 5678); 
  deserializeJson(jsonBufferRecv, texto);
  distancia = jsonBufferRecv["Distancia"];
  encenderLed();
  sensorGps();
  snprintf(msg, sizeof(msg), "{\"distancia\": %d}", distancia);
  Serial.println(msg);
  client.publish("prueba/distancia", msg);
  String localizacion = String(gps.location.lat(), 6) + ", " + String(gps.location.lng(), 6);
  snprintf (msg2, 75, "Localización: %s", localizacion.c_str);
  client.publish("prueba/localizar", msg2);
  delay(1000);
}

//-----------------------------
void reconnect() {
  // Reintenta conexión al broker si está desconectado
  while (!client.connected()) {
    Serial.print("Intentando conexión MQTT...");
    if (client.connect("ESP32Client")) {
      Serial.println("Conectado");
    } else {
      Serial.print("Fallo, rc=");
      Serial.print(client.state());
      Serial.println(" Intentando de nuevo en 5 segundos");
      delay(5000);
    }
  }
}
