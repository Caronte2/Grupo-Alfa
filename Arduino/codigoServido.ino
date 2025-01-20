#include <M5Stack.h>
#include "WiFi.h"
#include "AsyncUDP.h"
#include <ArduinoJson.h>
#include <TinyGPS++.h>
#include <PubSubClient.h>
// Pines de los LEDs
#define ledVerde 2
#define ledRoja 5
//-----------------Configuración wifi-------------
const char* ssid = "Rusell pagame"; // SSID del wifi
const char* password = "123456789"; //Contra del wifi
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

//latitud y longitud
double latitud;
double longitud;
//distancia
int distancia;
//----------------------------------MQTT-----------------------------------------------
const char *mqtt_server = "192.168.8.105";
const int mqtt_port = 1883; 
WiFiClient espClient;
PubSubClient client(espClient);
//char msg[50];
//char msg2[50];
String msg_distancia;
String msg_gps;

//--------------------------------------------------------------------------------------------
//                                    Configurar udp
//--------------------------------------------------------------------------------------------

void encenderLed(){
  M5.Lcd.println(distancia);
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
  M5.begin();
  Serial.begin(115200);
  //para inicializar random
  randomSeed(analogRead(0));
  //formato de texto en el m5stack
  M5.Lcd.setTextSize(3);
  M5.Lcd.setCursor(10, 10);
  // Configurar los pines de los LEDs
  pinMode(ledVerde, OUTPUT); //salida
  pinMode(ledRoja, OUTPUT); //salida
  M5.Lcd.println("Pines configurados");
  delay(5000);
  
  //Conectar al wifi
  WiFi.mode(WIFI_STA);
  WiFi.begin(ssid, password);
  if (WiFi.waitForConnectResult() != WL_CONNECTED){
    M5.Lcd.println("Error al conectar a la WiFI");
    while(1){
      delay(1000);
    }
  }else{
    M5.Lcd.println("Se a conectado correctamente");
  }
  delay(5000);

  // Iniciamos el listener UDP en el puerto 1234
  if(udp.listen(1234)){
    M5.Lcd.println("UDP escuchando en IP: ");
    M5.Lcd.println(WiFi.localIP());
    udp.onPacket([](AsyncUDPPacket packet){
      int i=200;
      while (i--) {
        *(texto+i)=*(packet.data()+i);
      }
      // recibido = 1;
    });
    M5.Lcd.clear();
  }else{
    M5.Lcd.println("UDP no funciona");
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
      M5.Lcd.println(gps.location.lat(), 6);
      latitud = gps.location.lat();
      // Longitud
      Serial.print("LONG: "); 
      M5.Lcd.println(gps.location.lng(), 6);
      longitud = gps.location.lng();
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
  //IF para falsear datos
  if(longitud == 0 && latitud == 0){
    msg_distancia = String(distancia);
    msg_gps = String(random(-90,90), 6) + "º, " + String(random(-180,180), 6);
    client.publish("proximidad/1", msg_distancia.c_str());
    client.publish("gps/1", msg_gps.c_str());
  }else{
    msg_distancia = String(distancia);
    msg_gps = String(latitud, 6) + String(longitud, 6);
    client.publish("proximidad/1", msg_distancia.c_str());
    client.publish("gps/1", msg_gps.c_str());
  }
    
  //----------------------
  M5.Lcd.setCursor(10, 100);
  delay(2000);
  M5.Lcd.clear();
}

//-----------------------------
void reconnect() {
  // Reintenta conexión al broker si está desconectado
  while (!client.connected()) {
    Serial.print("Intentando conexión MQTT...");
    if (client.connect("ESP32Client")) {
      M5.Lcd.println("Conectado");
    } else {
      Serial.print("Fallo, rc=");
      Serial.print(client.state());
      M5.Lcd.println(" Intentando de nuevo en 5 segundos");
      delay(5000);
    }
  }
}

