package com.example.fitness.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.fitness.R;
import com.example.fitness.ble.BleServiceManager;

/**
 * Dialog for BLE device connection with status updates
 */
public class BleConnectionDialog extends DialogFragment implements BleServiceManager.BleConnectionListener {
    
    public interface BleConnectionDialogListener {
        void onBleConnected();
        void onBleConnectionFailed(String error);
        void onBleConnectionCancelled();
    }
    
    private BleServiceManager bleServiceManager;
    private BleConnectionDialogListener listener;
    private TextView statusText;
    private TextView deviceText;
    private ProgressBar progressBar;
    private Button retryButton;
    private Button cancelButton;
    
    public static BleConnectionDialog newInstance() {
        return new BleConnectionDialog();
    }
    
    public void setBleServiceManager(BleServiceManager bleServiceManager) {
        this.bleServiceManager = bleServiceManager;
    }
    
    public void setBleConnectionDialogListener(BleConnectionDialogListener listener) {
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        
        View view = inflater.inflate(R.layout.dialog_ble_connection, null);
        
        statusText = view.findViewById(R.id.textViewStatus);
        deviceText = view.findViewById(R.id.textViewDevice);
        progressBar = view.findViewById(R.id.progressBar);
        retryButton = view.findViewById(R.id.buttonRetry);
        cancelButton = view.findViewById(R.id.buttonCancel);
        
        retryButton.setOnClickListener(v -> startConnection());
        cancelButton.setOnClickListener(v -> {
            if (listener != null) listener.onBleConnectionCancelled();
            dismiss();
        });
        
        builder.setView(view);
        builder.setTitle("Connecting to Exercise Tracker");
        builder.setCancelable(false);
        
        return builder.create();
    }
    
    @Override
    public void onStart() {
        super.onStart();
        if (bleServiceManager != null) {
            bleServiceManager.setBleConnectionListener(this);
            startConnection();
        }
    }
    
    private void startConnection() {
        statusText.setText("Searching for exercise tracker device...");
        deviceText.setText("");
        progressBar.setVisibility(View.VISIBLE);
        retryButton.setVisibility(View.GONE);
        
        if (bleServiceManager != null) {
            if (!bleServiceManager.isBluetoothEnabled()) {
                onError("Bluetooth is not enabled. Please enable Bluetooth and try again.");
                return;
            }
            
            if (!bleServiceManager.hasRequiredPermissions()) {
                onError("Missing required permissions. Please grant Bluetooth permissions.");
                return;
            }
            
            bleServiceManager.startScanning();
        }
    }
    
    @Override
    public void onConnectionStatusChanged(boolean isConnected) {
        if (isConnected) {
            statusText.setText("Connected successfully!");
            deviceText.setText("Exercise tracker is ready");
            progressBar.setVisibility(View.GONE);
            
            // Delay to show success message, then dismiss
            statusText.postDelayed(() -> {
                if (listener != null) listener.onBleConnected();
                dismiss();
            }, 1500);
        } else {
            statusText.setText("Connection lost");
            progressBar.setVisibility(View.GONE);
            retryButton.setVisibility(View.VISIBLE);
        }
    }
    
    @Override
    public void onDeviceFound(String deviceName, String deviceAddress) {
        deviceText.setText("Found: " + deviceName + " (" + deviceAddress + ")");
        if ("MyESP32".equals(deviceName) || "8C:D0:B2:A8:74:F4".equals(deviceAddress)) {
            statusText.setText("Connecting to exercise tracker...");
        }
    }
    
    @Override
    public void onDataReceived(String data) {
        // Not needed for connection dialog
    }
    
    @Override
    public void onError(String error) {
        statusText.setText("Connection failed: " + error);
        progressBar.setVisibility(View.GONE);
        retryButton.setVisibility(View.VISIBLE);
    }
    
    @Override
    public void onScanStarted() {
        statusText.setText("Scanning for devices...");
    }
    
    @Override
    public void onScanStopped() {
        // Handle in other callbacks
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (bleServiceManager != null) {
            bleServiceManager.cleanup();
        }
    }
}
