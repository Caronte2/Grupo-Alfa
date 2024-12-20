#include <M5Stack.h>
#include "WiFi.h"
#include "AsyncUDP.h"
#include <ArduinoJson.h>
//-----------------Configuración wifi-------------
const char* ssid = "Rusell pagame";
const char* password = "123456789";
//-----------------Sensor Ultrasonido-----------
// Declaro los pines del sensor de ultrasonidos
#define trigPin 5
#define echoPin 18

// Defino la velocidad del sonido
#define velocidadSonido 0.034

// Declarar varables para el sensor de ultrasonido
long duracion;
int distancia;

//------------------Declaro udp---------------------
AsyncUDP udp;
//Formato json
StaticJsonDocument<200> jsonBuffer;
//Array donde se guarda la información
char texto[200];

//-------------------------------------------------------------------------------------------
//                                      Setup
//-------------------------------------------------------------------------------------------
void setup() {
  //Inicio todo
  Serial.begin(115200);
  //Configurar los pines del sensor de ULTRASONIDOS
  pinMode(trigPin, OUTPUT); // Salida
  pinMode(echoPin, INPUT); //Entrada

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

  if(udp.listen(5678)){
    Serial.print("UDP escuchando en IP: ");
    Serial.println(WiFi.localIP());
    udp.onPacket([](AsyncUDPPacket packet){
      Serial.write(packet.data(), packet.length());
      Serial.println();
    });
  }else{
    Serial.println("UDP no funciona");
  }
}
//------------------------------------------------------------------------------------------
//                                      Loop
//------------------------------------------------------------------------------------------
void loop() {
  sensorUltrasonido();

  //----------formato de envio de datos----------------
  jsonBuffer["Distancia"] = distancia;

  //---------Transformación de información------------
  serializeJson(jsonBuffer, texto);
  
  udp.broadcastTo(texto, 1234); // Enviar al puerto 1234
  Serial.print("Enviado: ");
  Serial.println(texto);
  delay(1000);
}

//------------------------------------------------------------------------------------------
//                                Sensor Ultrasonido
//------------------------------------------------------------------------------------------
void sensorUltrasonido(){
  // borra el trigPin
  digitalWrite(trigPin, LOW);
  delayMicroseconds(2);
  // pone el pun trigPin en alto durante 10microsegundos
  digitalWrite(trigPin, HIGH);
  delayMicroseconds(10);
  digitalWrite(trigPin, LOW);

  // Lee el echoPin, y devuelve el tiempo que tarda el sonido en microsegundos
  duracion = pulseIn(echoPin, HIGH);

  // Calculo de la distancia
  distancia = duracion * velocidadSonido/2; 

  Serial.println(distancia);
}
