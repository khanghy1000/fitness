#include "SparkFunLSM6DS3.h"
#include "Wire.h"
//#include "C:/Users/Ahri/Documents/Arduino/thuctap/fsUtils.h"

#include <BLEDevice.h>
#include <BLEUtils.h>
#include <BLEServer.h>

#define SERVICE_UUID "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
#define CHARACTERISTIC_UUID "beb5483e-36e1-4688-b7f5-ea07361b26a8"

#define FREQUENCY_HZ 10
#define INTERVAL_MS (1000 / (FREQUENCY_HZ + 1))
#define CONVERT_G_TO_MS2 9.80665f

static unsigned long last_interval_ms = 0;

BLECharacteristic *pCharacteristic;
bool deviceConnected = false;

class MyServerCallbacks : public BLEServerCallbacks {
  void onConnect(BLEServer *pServer) {
    deviceConnected = true;
    //Serial.println("Client connected");
  }

  void onDisconnect(BLEServer *pServer) {
    deviceConnected = false;
    //Serial.println("Client disconnected, restarting advertising...");
    // Restart advertising so the client can reconnect
    pServer->getAdvertising()->start();
  }
};

LSM6DS3 myIMU;  //Default constructor is I2C, addr 0x6B
unsigned long getDataTime = millis();
unsigned long writeDataTime = millis();

void setup() {
  // put your setup code here, to run once:
  Serial.begin(115200);
  delay(1000);  //relax...
  Serial.println("Processor came out of reset.\n");

  BLEDevice::init("MyESP32");
  BLEServer *pServer = BLEDevice::createServer();
  pServer->setCallbacks(new MyServerCallbacks());
  BLEService *pService = pServer->createService(SERVICE_UUID);
  BLEDescriptor *pDescriptor = new BLEDescriptor((uint16_t)0x2902);
  pCharacteristic = pService->createCharacteristic(CHARACTERISTIC_UUID, BLECharacteristic::PROPERTY_READ | BLECharacteristic::PROPERTY_WRITE | BLECharacteristic::PROPERTY_NOTIFY);

  pCharacteristic->addDescriptor(pDescriptor);
  pCharacteristic->setValue("Hello World says Neil");
  pService->start();
  // BLEAdvertising *pAdvertising = pServer->getAdvertising();  // this still is working for backward compatibility
  BLEAdvertising *pAdvertising = BLEDevice::getAdvertising();
  pAdvertising->addServiceUUID(SERVICE_UUID);
  pAdvertising->setScanResponse(true);
  pAdvertising->setMinPreferred(0x06);  // functions that help with iPhone connections issue
  pAdvertising->setMinPreferred(0x12);
  pAdvertising->setAdvertisementType(ADV_TYPE_IND);
  BLEDevice::startAdvertising();
  Serial.println("Characteristic defined! Now you can read it in your phone!");

  pinMode(0, OUTPUT);
  digitalWrite(0, HIGH);

  myIMU.settings.gyroRange = 2000;      //Max deg/s.  Can be: 125, 245, 500, 1000, 2000
  myIMU.settings.gyroSampleRate = 833;  //Hz.  Can be: 13, 26, 52, 104, 208, 416, 833, 1666
  myIMU.settings.gyroBandWidth = 200;

  myIMU.settings.accelRange = 16;        //Max G force readable.  Can be: 2, 4, 8, 16
  myIMU.settings.accelSampleRate = 833;  //Hz.  Can be: 13, 26, 52, 104, 208, 416, 833, 1666, 3332, 6664, 13330
  myIMU.settings.accelBandWidth = 200;

  Wire.begin(9, 10);
  myIMU.begin();
}


void loop() {
  if (deviceConnected) {
    if (millis() > last_interval_ms + INTERVAL_MS) {
      last_interval_ms = millis();

      float ax = myIMU.readFloatAccelX();
      float ay = myIMU.readFloatAccelY();
      float az = myIMU.readFloatAccelZ();

      float gx = myIMU.readFloatGyroX();
      float gy = myIMU.readFloatGyroY();
      float gz = myIMU.readFloatGyroZ();

      char buf[128];
      int len = snprintf(buf, sizeof(buf),
                         "%.2f,%.2f,%.2f,%.2f,%.2f,%.2f",
                         ax, ay, az, gx, gy, gz);

      pCharacteristic->setValue((uint8_t *)buf, len);
      pCharacteristic->notify();
    }
  }
}