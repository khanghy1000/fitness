package com.example.fitness.ble;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.UUID;

public class BleServiceManager {
    
    private static final String TAG = "BleServiceManager";
    
    // ESP32 BLE Configuration (from ESP32 firmware)
    private static final String ESP32_NAME = "MyESP32";
    private static final String ESP32_MAC = "8C:D0:B2:A8:74:F4";
    private static final UUID SERVICE_UUID = UUID.fromString("4fafc201-1fb5-459e-8fcc-c5c9c331914b");
    private static final UUID CHARACTERISTIC_UUID = UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8");
    
    private static final long SCAN_TIMEOUT = 10000; // 10 seconds
    
    public interface BleConnectionListener {
        void onConnectionStatusChanged(boolean isConnected);
        void onDeviceFound(String deviceName, String deviceAddress);
        void onDataReceived(String data);
        void onError(String error);
        void onScanStarted();
        void onScanStopped();
    }
    
    private final Context context;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private BluetoothGatt bluetoothGatt;
    private BluetoothDevice targetDevice;
    private BleConnectionListener listener;
    private boolean isScanning = false;
    private boolean isConnected = false;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private int serviceDiscoveryRetries = 0;
    private static final int MAX_SERVICE_DISCOVERY_RETRIES = 3;
    
    // Data buffering for handling fragmented BLE messages
    private StringBuilder dataBuffer = new StringBuilder();
    private static final String JSON_END_MARKER = "}";
    private static final long DATA_TIMEOUT_MS = 1000; // 1 second timeout for incomplete messages
    private Runnable dataTimeoutRunnable;
    
    public BleServiceManager(Context context) {
        this.context = context;
        initializeBluetooth();
    }
    
    private void initializeBluetooth() {
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager != null) {
            bluetoothAdapter = bluetoothManager.getAdapter();
            if (bluetoothAdapter != null) {
                bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
            }
        }
    }
    
    public void setBleConnectionListener(BleConnectionListener listener) {
        this.listener = listener;
    }
    
    public boolean isBluetoothEnabled() {
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
    }
    
    public boolean hasRequiredPermissions() {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED &&
               ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
               ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
    
    public void startScanning() {
        if (!isBluetoothEnabled()) {
            if (listener != null) listener.onError("Bluetooth is not enabled");
            return;
        }
        
        if (!hasRequiredPermissions()) {
            if (listener != null) listener.onError("Missing required permissions");
            return;
        }
        
        if (isScanning) {
            return;
        }
        
        isScanning = true;
        if (listener != null) listener.onScanStarted();
        
        try {
            bluetoothLeScanner.startScan(scanCallback);
            
            // Stop scanning after timeout
            handler.postDelayed(() -> {
                if (isScanning) {
                    stopScanning();
                    if (targetDevice == null && listener != null) {
                        listener.onError("ESP32 device not found within timeout");
                    }
                }
            }, SCAN_TIMEOUT);
            
        } catch (SecurityException e) {
            isScanning = false;
            if (listener != null) listener.onError("Permission denied: " + e.getMessage());
        }
    }
    
    public void stopScanning() {
        if (!isScanning) return;
        
        isScanning = false;
        if (listener != null) listener.onScanStopped();
        
        try {
            if (bluetoothLeScanner != null) {
                bluetoothLeScanner.stopScan(scanCallback);
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Permission denied when stopping scan", e);
        }
    }
    
    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice device = result.getDevice();
            
            try {
                String deviceName = device.getName();
                String deviceAddress = device.getAddress();
                
                if (listener != null) {
                    listener.onDeviceFound(deviceName != null ? deviceName : "Unknown", deviceAddress);
                }
                
                // Check if this is our target device
                if ((ESP32_NAME.equals(deviceName) || ESP32_MAC.equals(deviceAddress)) && targetDevice == null) {
                    targetDevice = device;
                    stopScanning();
                    connectToDevice();
                }
            } catch (SecurityException e) {
                Log.e(TAG, "Permission denied when accessing device info", e);
            }
        }
        
        @Override
        public void onScanFailed(int errorCode) {
            isScanning = false;
            if (listener != null) {
                listener.onScanStopped();
                listener.onError("Scan failed with error code: " + errorCode);
            }
        }
    };
    
    private void connectToDevice() {
        if (targetDevice == null) {
            if (listener != null) listener.onError("No target device found");
            return;
        }
        
        try {
            // Connect to the device using TRANSPORT_LE for BLE
            bluetoothGatt = targetDevice.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE);
            
            // Clear BLE cache to ensure fresh service discovery
            refreshDeviceCache(bluetoothGatt);
        } catch (SecurityException e) {
            if (listener != null) listener.onError("Permission denied when connecting: " + e.getMessage());
        }
    }
    
    /**
     * Clear the internal cache and force a refresh of services from the remote device.
     * This uses reflection to call the hidden refresh() method in BluetoothGatt.
     * Based on solutions from StackOverflow and Android BLE best practices.
     */
    private boolean refreshDeviceCache(BluetoothGatt gatt) {
        try {
            Method refreshMethod = gatt.getClass().getMethod("refresh");
            if (refreshMethod != null) {
                boolean result = (Boolean) refreshMethod.invoke(gatt);
                Log.d(TAG, "🔄 BLE cache refresh result: " + result);
                return result;
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Failed to refresh BLE cache: " + e.getMessage());
        }
        return false;
    }
    
    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.d(TAG, "🔗 Connection state change - Status: " + status + ", New State: " + newState);
            
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    isConnected = true;
                    if (listener != null) {
                        handler.post(() -> listener.onConnectionStatusChanged(true));
                    }
                    
                    Log.d(TAG, "✅ Successfully connected to device");
                    
                    // Add a delay before discovering services to ensure connection is stable
                    // Based on Android BLE best practices from research
                    handler.postDelayed(() -> {
                        try {
                            Log.d(TAG, "🔍 Starting service discovery...");
                            boolean discoveryStarted = gatt.discoverServices();
                            Log.d(TAG, "📋 Service discovery started: " + discoveryStarted);
                        } catch (SecurityException e) {
                            Log.e(TAG, "Permission denied when discovering services", e);
                        }
                    }, 1000); // Increased delay to 1000ms based on research findings
                    
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    isConnected = false;
                    if (listener != null) {
                        handler.post(() -> listener.onConnectionStatusChanged(false));
                    }
                    Log.d(TAG, "🔌 Disconnected from device");
                    
                    // Clean up resources as recommended by Android BLE best practices
                    if (gatt != null) {
                        gatt.close();
                    }
                }
            } else {
                // Handle connection errors
                Log.e(TAG, "❌ Connection failed with status: " + status);
                isConnected = false;
                
                if (status == 133) { // GATT_ERROR - very common in Android BLE
                    Log.e(TAG, "   Status 133 (GATT_ERROR) - Common Android BLE issue, may need retry");
                    if (listener != null) {
                        handler.post(() -> listener.onError("Connection failed (Status 133). Try again."));
                    }
                } else {
                    if (listener != null) {
                        handler.post(() -> listener.onError("Connection failed with status: " + status));
                    }
                }
                
                // Clean up on error
                if (gatt != null) {
                    gatt.close();
                }
            }
        }
        
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "🔍 Services discovered (attempt " + (serviceDiscoveryRetries + 1) + "), looking for service: " + SERVICE_UUID);
                Log.d(TAG, "� Total services found: " + gatt.getServices().size());
                
                // List all services found
                for (BluetoothGattService service : gatt.getServices()) {
                    Log.d(TAG, "� Found service: " + service.getUuid());
                    for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                        Log.d(TAG, "  📄 Found characteristic: " + characteristic.getUuid());
                    }
                }
                
                // Try to find the specific service (case-insensitive UUID matching)
                BluetoothGattService targetService = findServiceByUuid(gatt, SERVICE_UUID);
                BluetoothGattCharacteristic targetCharacteristic = null;
                
                if (targetService != null) {
                    Log.d(TAG, "✅ Found target service: " + SERVICE_UUID);
                    targetCharacteristic = findCharacteristicByUuid(targetService, CHARACTERISTIC_UUID);
                    if (targetCharacteristic != null) {
                        Log.d(TAG, "✅ Found target characteristic: " + CHARACTERISTIC_UUID);
                    } else {
                        Log.e(TAG, "❌ Target characteristic not found in service: " + CHARACTERISTIC_UUID);
                    }
                } else {
                    Log.e(TAG, "❌ Target service not found: " + SERVICE_UUID);
                    
                    // Retry service discovery if we haven't reached max retries
                    if (serviceDiscoveryRetries < MAX_SERVICE_DISCOVERY_RETRIES) {
                        serviceDiscoveryRetries++;
                        Log.d(TAG, "🔄 Retrying service discovery in 2 seconds... (attempt " + serviceDiscoveryRetries + "/" + MAX_SERVICE_DISCOVERY_RETRIES + ")");
                        
                        // Clear cache again before retry
                        refreshDeviceCache(gatt);
                        
                        handler.postDelayed(() -> {
                            try {
                                gatt.discoverServices();
                            } catch (SecurityException e) {
                                Log.e(TAG, "Permission denied when retrying service discovery", e);
                            }
                        }, 2000); // Longer delay for retries
                        return;
                    } else {
                        Log.e(TAG, "❌ Max retries reached, service discovery failed");
                    }
                }
                
                // Setup notifications if we found the characteristic
                if (targetCharacteristic != null) {
                    serviceDiscoveryRetries = 0; // Reset retry counter on success
                    setupNotifications(gatt, targetCharacteristic);
                } else {
                    Log.e(TAG, "❌ No suitable characteristic found for notifications");
                    if (listener != null) {
                        handler.post(() -> listener.onError("Could not find the required BLE characteristic"));
                    }
                }
            } else {
                Log.e(TAG, "❌ Service discovery failed with status: " + status);
                if (listener != null) {
                    handler.post(() -> listener.onError("Service discovery failed with status: " + status));
                }
            }
        }
        
        /**
         * Find service by UUID with case-insensitive matching
         */
        private BluetoothGattService findServiceByUuid(BluetoothGatt gatt, UUID targetUuid) {
            for (BluetoothGattService service : gatt.getServices()) {
                if (service.getUuid().toString().equalsIgnoreCase(targetUuid.toString())) {
                    return service;
                }
            }
            return null;
        }
        
        /**
         * Find characteristic by UUID with case-insensitive matching
         */
        private BluetoothGattCharacteristic findCharacteristicByUuid(BluetoothGattService service, UUID targetUuid) {
            for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                if (characteristic.getUuid().toString().equalsIgnoreCase(targetUuid.toString())) {
                    return characteristic;
                }
            }
            return null;
        }
        
        private void setupNotifications(BluetoothGatt gatt, BluetoothGattCharacteristic targetCharacteristic) {
            try {
                // Enable local notifications first
                boolean notificationSet = gatt.setCharacteristicNotification(targetCharacteristic, true);
                Log.d(TAG, "📡 Notification enabled: " + notificationSet);
                
                // Check characteristic properties
                int properties = targetCharacteristic.getProperties();
                Log.d(TAG, "📋 Characteristic properties: " + properties);
                Log.d(TAG, "📋 Supports notify: " + ((properties & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0));
                Log.d(TAG, "📋 Supports indicate: " + ((properties & BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0));
                
                // Try to find and write to CCCD
                UUID CCCD_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
                BluetoothGattDescriptor descriptor = targetCharacteristic.getDescriptor(CCCD_UUID);
                if (descriptor != null) {
                    // Choose the right descriptor value based on what the characteristic supports
                    byte[] value;
                    if ((properties & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0) {
                        value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
                        Log.d(TAG, "📝 Using NOTIFICATION value for CCCD");
                    } else if ((properties & BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0) {
                        value = BluetoothGattDescriptor.ENABLE_INDICATION_VALUE;
                        Log.d(TAG, "📝 Using INDICATION value for CCCD");
                    } else {
                        Log.w(TAG, "⚠️ Characteristic doesn't support notify or indicate");
                        if (listener != null) {
                            handler.post(() -> listener.onError("Characteristic doesn't support notifications"));
                        }
                        return;
                    }
                    
                    descriptor.setValue(value);
                    
                    // Add a small delay before writing descriptor (some devices need this)
                    handler.postDelayed(() -> {
                        try {
                            boolean descriptorWritten = gatt.writeDescriptor(descriptor);
                            Log.d(TAG, "📝 CCCD descriptor write requested: " + descriptorWritten);
                            if (!descriptorWritten && listener != null) {
                                handler.post(() -> listener.onError("Failed to write CCCD descriptor"));
                            }
                        } catch (SecurityException e) {
                            Log.e(TAG, "Permission denied when writing descriptor", e);
                            if (listener != null) {
                                handler.post(() -> listener.onError("Permission denied when writing descriptor"));
                            }
                        }
                    }, 100);
                } else {
                    Log.w(TAG, "⚠️ CCCD descriptor not found - notifications may not work properly");
                    if (listener != null) {
                        handler.post(() -> listener.onError("CCCD descriptor not found"));
                    }
                }
            } catch (SecurityException e) {
                Log.e(TAG, "Permission denied when setting notification", e);
                if (listener != null) {
                    handler.post(() -> listener.onError("Permission denied when setting notification"));
                }
            }
        }
        
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            byte[] data = characteristic.getValue();
            if (data != null) {
                String receivedChunk = new String(data).trim();
                Log.d(TAG, "📡 Raw BLE chunk received (" + data.length + " bytes): " + receivedChunk);
                
                // Handle data reassembly for fragmented messages
                handleDataChunk(receivedChunk);
                
            } else {
                Log.d(TAG, "⚠️ Received null data from characteristic");
            }
        }
        
        /**
         * Handle fragmented BLE data by buffering chunks until we have a complete JSON message
         */
        private void handleDataChunk(String chunk) {
            // Cancel any existing timeout
            if (dataTimeoutRunnable != null) {
                handler.removeCallbacks(dataTimeoutRunnable);
            }
            
            // Add chunk to buffer
            dataBuffer.append(chunk);
            String currentData = dataBuffer.toString();
            
            Log.d(TAG, "📊 Current buffer (" + currentData.length() + " chars): " + currentData);
            
            // Check if we have a complete JSON message
            if (isCompleteJsonMessage(currentData)) {
                Log.d(TAG, "✅ Complete JSON message received: " + currentData);
                
                // Send complete message to listener
                if (listener != null) {
                    handler.post(() -> listener.onDataReceived(currentData));
                }
                
                // Clear buffer for next message
                dataBuffer.setLength(0);
            } else {
                // Set timeout to prevent buffer from growing indefinitely
                dataTimeoutRunnable = () -> {
                    Log.w(TAG, "⏰ Data timeout - clearing incomplete buffer: " + dataBuffer.toString());
                    dataBuffer.setLength(0);
                };
                handler.postDelayed(dataTimeoutRunnable, DATA_TIMEOUT_MS);
            }
        }
        
        /**
         * Check if the buffered data contains a complete JSON message
         */
        private boolean isCompleteJsonMessage(String data) {
            if (data == null || data.isEmpty()) {
                return false;
            }
            
            // Basic JSON validation - count braces
            int openBraces = 0;
            int closeBraces = 0;
            
            for (char c : data.toCharArray()) {
                if (c == '{') {
                    openBraces++;
                } else if (c == '}') {
                    closeBraces++;
                }
            }
            
            // We have a complete JSON object when braces are balanced and we have at least one pair
            boolean isComplete = openBraces > 0 && openBraces == closeBraces;
            
            if (isComplete) {
                Log.d(TAG, "🎯 JSON validation: " + openBraces + " open braces, " + closeBraces + " close braces - COMPLETE");
            } else {
                Log.d(TAG, "🔄 JSON validation: " + openBraces + " open braces, " + closeBraces + " close braces - INCOMPLETE");
            }
            
            return isComplete;
        }
        
        /**
         * Request a larger MTU size for better data throughput
         */
        private void requestMtu(BluetoothGatt gatt) {
            try {
                // Request maximum MTU size (517 bytes is the BLE maximum)
                boolean mtuRequested = gatt.requestMtu(517);
                Log.d(TAG, "📡 MTU size increase requested (517 bytes): " + mtuRequested);
            } catch (SecurityException e) {
                Log.e(TAG, "Permission denied when requesting MTU", e);
            }
        }
        
        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            Log.d(TAG, "📡 MTU changed - New size: " + mtu + " bytes, Status: " + status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "✅ MTU successfully changed to " + mtu + " bytes (data payload: " + (mtu - 3) + " bytes)");
            } else {
                Log.w(TAG, "⚠️ MTU change failed with status: " + status + ", using default MTU");
            }
        }
        
        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            Log.d(TAG, "📝 Descriptor write callback - UUID: " + descriptor.getUuid() + ", Status: " + status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                                    Log.d(TAG, "✅ CCCD descriptor write successful - notifications should be active now");
                    
                    // Request larger MTU for better data throughput
                    requestMtu(gatt);
                } else {
                Log.e(TAG, "❌ CCCD descriptor write failed with status: " + status);
                // Common status codes:
                // 1 = GATT_INVALID_HANDLE
                // 3 = GATT_WRITE_NOT_PERMIT
                // 8 = GATT_INVALID_OFFSET
                switch (status) {
                    case 1:
                        Log.e(TAG, "   Status 1: Invalid handle - descriptor might not exist");
                        break;
                    case 3:
                        Log.e(TAG, "   Status 3: Write not permitted - insufficient authentication/encryption");
                        break;
                    case 8:
                        Log.e(TAG, "   Status 8: Invalid offset");
                        break;
                    default:
                        Log.e(TAG, "   Unknown status code: " + status);
                        break;
                }
            }
        }
    };
    
    public void disconnect() {
        if (bluetoothGatt != null) {
            try {
                bluetoothGatt.disconnect();
                bluetoothGatt.close();
            } catch (SecurityException e) {
                Log.e(TAG, "Permission denied when disconnecting", e);
            }
            bluetoothGatt = null;
        }
        targetDevice = null;
        isConnected = false;
        serviceDiscoveryRetries = 0; // Reset retry counter
        
        // Clear data buffer on disconnect
        dataBuffer.setLength(0);
        if (dataTimeoutRunnable != null) {
            handler.removeCallbacks(dataTimeoutRunnable);
            dataTimeoutRunnable = null;
        }
    }
    
    public boolean isConnected() {
        return isConnected;
    }
    
    public boolean isScanning() {
        return isScanning;
    }
    
    public void cleanup() {
        stopScanning();
        disconnect();
        
        // Additional cleanup for data buffer
        dataBuffer.setLength(0);
        if (dataTimeoutRunnable != null) {
            handler.removeCallbacks(dataTimeoutRunnable);
            dataTimeoutRunnable = null;
        }
    }
}
