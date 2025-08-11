/* Edge Impulse ingestion SDK
 * Copyright (c) 2022 EdgeImpulse Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

/* Includes ---------------------------------------------------------------- */
#include <khanghy1000-project-1_inferencing.h>
#include "SparkFunLSM6DS3.h"
#include "Wire.h"
#include <BLEDevice.h>
#include <BLEUtils.h>
#include <BLEServer.h>

#define SERVICE_UUID "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
#define CHARACTERISTIC_UUID "beb5483e-36e1-4688-b7f5-ea07361b26a8"

#define FREQUENCY_HZ 10
#define INTERVAL_MS (1000 / (FREQUENCY_HZ + 1))
#define CONVERT_G_TO_MS2 9.80665f

static unsigned long last_interval_ms = 0;

// BLE variables
BLECharacteristic *pCharacteristic;
bool deviceConnected = false;

// Sensor variables
LSM6DS3 myIMU;  //Default constructor is I2C, addr 0x6B

// Data collection variables
static float features[EI_CLASSIFIER_DSP_INPUT_FRAME_SIZE];
static int feature_index = 0;
static bool data_ready = false;

class MyServerCallbacks : public BLEServerCallbacks {
  void onConnect(BLEServer *pServer) {
    deviceConnected = true;
  }

  void onDisconnect(BLEServer *pServer) {
    deviceConnected = false;
    // Restart advertising so the client can reconnect
    pServer->getAdvertising()->start();
  }
};

/**
 * @brief      Copy raw feature data in out_ptr
 *             Function called by inference library
 *
 * @param[in]  offset   The offset
 * @param[in]  length   The length
 * @param      out_ptr  The out pointer
 *
 * @return     0
 */
int raw_feature_get_data(size_t offset, size_t length, float *out_ptr) {
  memcpy(out_ptr, features + offset, length * sizeof(float));
  return 0;
}

void print_inference_result(ei_impulse_result_t result);

/**
 * @brief      Arduino setup function
 */
void setup() {
  // put your setup code here, to run once:
  Serial.begin(115200);
  delay(1000);
  // comment out the below line to cancel the wait for USB connection (needed for native USB)
  while (!Serial)
    ;
  Serial.println("Edge Impulse Inferencing Demo with BLE");

  // Initialize BLE
  BLEDevice::init("MyESP32");
  BLEServer *pServer = BLEDevice::createServer();
  pServer->setCallbacks(new MyServerCallbacks());
  BLEService *pService = pServer->createService(SERVICE_UUID);
  BLEDescriptor *pDescriptor = new BLEDescriptor((uint16_t)0x2902);
  pCharacteristic = pService->createCharacteristic(CHARACTERISTIC_UUID,
                                                   BLECharacteristic::PROPERTY_READ | BLECharacteristic::PROPERTY_WRITE | BLECharacteristic::PROPERTY_NOTIFY);

  pCharacteristic->addDescriptor(pDescriptor);
  pCharacteristic->setValue("ML Predictions Ready");
  pService->start();

  BLEAdvertising *pAdvertising = BLEDevice::getAdvertising();
  pAdvertising->addServiceUUID(SERVICE_UUID);
  pAdvertising->setScanResponse(true);
  pAdvertising->setMinPreferred(0x06);
  pAdvertising->setMinPreferred(0x12);
  pAdvertising->setAdvertisementType(ADV_TYPE_IND);
  BLEDevice::startAdvertising();
  Serial.println("BLE advertising started!");

  // Initialize IMU sensor
  myIMU.settings.gyroRange = 2000;      //Max deg/s.  Can be: 125, 245, 500, 1000, 2000
  myIMU.settings.gyroSampleRate = 833;  //Hz.  Can be: 13, 26, 52, 104, 208, 416, 833, 1666
  myIMU.settings.gyroBandWidth = 200;

  myIMU.settings.accelRange = 16;        //Max G force readable.  Can be: 2, 4, 8, 16
  myIMU.settings.accelSampleRate = 833;  //Hz.  Can be: 13, 26, 52, 104, 208, 416, 833, 1666, 3332, 6664, 13330
  myIMU.settings.accelBandWidth = 200;

  Wire.begin(9, 10);
  if (myIMU.begin() != 0) {
    Serial.println("IMU initialization failed!");
  } else {
    Serial.println("IMU initialized successfully!");
  }

  // Initialize features array
  for (int i = 0; i < EI_CLASSIFIER_DSP_INPUT_FRAME_SIZE; i++) {
    features[i] = 0.0;
  }

  Serial.println("Setup complete. Starting data collection...");
}

/**
 * @brief      Arduino main function
 */
void loop() {
  if (millis() > last_interval_ms + INTERVAL_MS) {
    last_interval_ms = millis();
    // Collect sensor data
    float ax = myIMU.readFloatAccelX();
    float ay = myIMU.readFloatAccelY();
    float az = myIMU.readFloatAccelZ();
    float gx = myIMU.readFloatGyroX();
    float gy = myIMU.readFloatGyroY();
    float gz = myIMU.readFloatGyroZ();

    // Store sensor readings in features array (assuming 6 values per sample: ax, ay, az, gx, gy, gz)
    if (feature_index < EI_CLASSIFIER_DSP_INPUT_FRAME_SIZE - 5) {
      features[feature_index++] = ax;
      features[feature_index++] = ay;
      features[feature_index++] = az;
      features[feature_index++] = gx;
      features[feature_index++] = gy;
      features[feature_index++] = gz;
    }

    // If we have enough data for inference
    if (feature_index >= EI_CLASSIFIER_DSP_INPUT_FRAME_SIZE) {
      data_ready = true;
      feature_index = 0;  // Reset for next collection cycle
    }

    // Perform inference when data is ready
    if (data_ready) {
      ei_printf("Running inference...\n");

      ei_impulse_result_t result = { 0 };

      // Setup signal structure
      signal_t features_signal;
      features_signal.total_length = EI_CLASSIFIER_DSP_INPUT_FRAME_SIZE;
      features_signal.get_data = &raw_feature_get_data;

      // Run classifier
      EI_IMPULSE_ERROR res = run_classifier(&features_signal, &result, false /* debug */);

      if (res == EI_IMPULSE_OK) {
        ei_printf("Inference completed successfully\n");

        // Create JSON with sensor data and predictions
        String jsonData = createPredictionJSON(ax, ay, az, gx, gy, gz, result);

        // Send via BLE if connected
        if (deviceConnected) {
          pCharacteristic->setValue(jsonData.c_str());
          pCharacteristic->notify();
          Serial.println("Data sent via BLE: " + jsonData);
        }

        // Print results to serial
        print_inference_result(result);
      } else {
        ei_printf("ERR: Failed to run classifier (%d)\n", res);
      }

      data_ready = false;
    }
  }
}

String createPredictionJSON(float ax, float ay, float az, float gx, float gy, float gz, ei_impulse_result_t result) {
  String json = "{";

  // Add sensor data
  json += "\"sensor\":{";
  json += "\"ax\":" + String(ax, 3) + ",";
  json += "\"ay\":" + String(ay, 3) + ",";
  json += "\"az\":" + String(az, 3) + ",";
  json += "\"gx\":" + String(gx, 3) + ",";
  json += "\"gy\":" + String(gy, 3) + ",";
  json += "\"gz\":" + String(gz, 3);
  json += "},";

  // Add timing information
  json += "\"timing\":{";
  json += "\"dsp\":" + String(result.timing.dsp) + ",";
  json += "\"classification\":" + String(result.timing.classification) + ",";
  json += "\"anomaly\":" + String(result.timing.anomaly);
  json += "},";

  // Add predictions
  json += "\"predictions\":{";
  for (uint16_t i = 0; i < EI_CLASSIFIER_LABEL_COUNT; i++) {
    if (i > 0) json += ",";
    json += "\"" + String(ei_classifier_inferencing_categories[i]) + "\":" + String(result.classification[i].value, 5);
  }
  json += "}";

  // Add anomaly detection if available
#if EI_CLASSIFIER_HAS_ANOMALY
  json += ",\"anomaly\":" + String(result.anomaly, 3);
#endif

  json += "}";
  return json;
}

void print_inference_result(ei_impulse_result_t result) {

  // Print how long it took to perform inference
  ei_printf("Timing: DSP %d ms, inference %d ms, anomaly %d ms\r\n",
            result.timing.dsp,
            result.timing.classification,
            result.timing.anomaly);

  // Print the prediction results (object detection)
#if EI_CLASSIFIER_OBJECT_DETECTION == 1
  ei_printf("Object detection bounding boxes:\r\n");
  for (uint32_t i = 0; i < result.bounding_boxes_count; i++) {
    ei_impulse_result_bounding_box_t bb = result.bounding_boxes[i];
    if (bb.value == 0) {
      continue;
    }
    ei_printf("  %s (%f) [ x: %u, y: %u, width: %u, height: %u ]\r\n",
              bb.label,
              bb.value,
              bb.x,
              bb.y,
              bb.width,
              bb.height);
  }

  // Print the prediction results (classification)
#else
  ei_printf("Predictions:\r\n");
  for (uint16_t i = 0; i < EI_CLASSIFIER_LABEL_COUNT; i++) {
    ei_printf("  %s: ", ei_classifier_inferencing_categories[i]);
    ei_printf("%.5f\r\n", result.classification[i].value);
  }
#endif

  // Print anomaly result (if it exists)
#if EI_CLASSIFIER_HAS_ANOMALY
  ei_printf("Anomaly prediction: %.3f\r\n", result.anomaly);
#endif

#if EI_CLASSIFIER_HAS_VISUAL_ANOMALY
  ei_printf("Visual anomalies:\r\n");
  for (uint32_t i = 0; i < result.visual_ad_count; i++) {
    ei_impulse_result_bounding_box_t bb = result.visual_ad_grid_cells[i];
    if (bb.value == 0) {
      continue;
    }
    ei_printf("  %s (%f) [ x: %u, y: %u, width: %u, height: %u ]\r\n",
              bb.label,
              bb.value,
              bb.x,
              bb.y,
              bb.width,
              bb.height);
  }
#endif
}