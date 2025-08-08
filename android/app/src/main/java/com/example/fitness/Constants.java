package com.example.fitness;

public class Constants {
    public static String getBaseUrl() {
        if (isEmulator()) {
            return "http://10.0.2.2:3000/";
        } else {
            return "http://192.168.10.121:3000/";
        }
    }

    private static boolean isEmulator() {
        String fingerprint = android.os.Build.FINGERPRINT;
        String model = android.os.Build.MODEL;
        String product = android.os.Build.PRODUCT;
        String brand = android.os.Build.BRAND;
        return fingerprint != null && (fingerprint.contains("generic") || fingerprint.contains("emulator"))
                || model != null && (model.contains("Emulator") || model.contains("Android SDK built for x86"))
                || product != null && product.contains("sdk")
                || brand != null && brand.contains("generic");
    }
}
