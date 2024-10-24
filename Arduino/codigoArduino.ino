#include <TinyGPS++.h>

// Declaro los pines del sensor del Gps
#define RXD2 16
#define TXD2 17

#define GPS_BAUD 9600

// Declaro un objeto gps
TinyGPSPlus gps;

// Crea una instancia de la clase HardwareSerial para el puerto serie 2.
HardwareSerial gpsSerial(2);

// Declaro los pines del sensor de ultrasonidos
#define trigPin 5
#define echoPin 18

// Defino la velocidad del sonido
#define velocidadSonido 0.034

// Pines de los LEDs
#define ledVerde 23
#define ledRoja 19

// Declarar varables para el sensor de ultrasonido
long duracion;
int distancia;

void setup() {
    // inicio todo
    Serial.begin(115200);

    gpsSerial.begin(GPS_BAUD, SERIAL_8N1, RXD2, TXD2);

    //Configurar los pines del sensor de luminosidad
    pinMode(trigPin, OUTPUT); // Salida
    pinMode(echoPin, INPUT); //Entrada

    // Configurar los pines de los LEDs
    pinMode(ledVerde, OUTPUT); //salida
    pinMode(ledRoja, OUTPUT); //salida
}

void loop() {
    sensorUltrasonido();
    sensorGps();
    delay(1000);
}

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

    // Imprime en el serial monitor los datos
    Serial.print("Distance (cm): ");
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
            // Numero de satelites que tiene conectado
            Serial.print("Satelites = "); 
            Serial.println(gps.satellites.value());
            // Tiempo en el que detecta los datos
            Serial.print("Time in UTC: ");
            Serial.println(String(gps.date.year()) + "/" + String(gps.date.month()) + "/" + String(gps.date.day()) + "," + String(gps.time.hour()) + ":" + String(gps.time.minute()) + ":" + String(gps.time.second()));
            Serial.println("");
        }
    }
}





