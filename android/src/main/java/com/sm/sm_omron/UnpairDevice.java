package com.sm.sm_omron;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.HashMap;

import io.flutter.plugin.common.MethodChannel;

public class UnpairDevice {
    private final Context context;
    private final String deviceAddress; // UUID in Omron terms is often the MAC address for BLE
    private final MethodChannel.Result result;

    public UnpairDevice(Context context, HashMap<String, Object> args, MethodChannel.Result result) {
        this.context = context;
        this.result = result;
        this.deviceAddress = (String) args.get("uuid");
    }

    public void unpair() {
        if (deviceAddress == null) {
            result.error("INVALID_ARGUMENTS", "Device UUID (MAC address) is required", null);
            return;
        }

        try {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter == null) {
                result.error("BLUETOOTH_UNAVAILABLE", "Bluetooth is not available", null);
                return;
            }

            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
            if (device == null) {
                 result.error("DEVICE_NOT_FOUND", "Device with address " + deviceAddress + " not found", null);
                 return;
            }

            // Using reflection to call hide method removeBond
            Method removeBondMethod = device.getClass().getMethod("removeBond");
            boolean success = (boolean) removeBondMethod.invoke(device);

            if (success) {
                Log.d("UnpairDevice", "Successfully initiated unpairing for: " + deviceAddress);
                result.success(true);
            } else {
                Log.e("UnpairDevice", "Failed to initiate unpairing for: " + deviceAddress);
                result.error("UNPAIR_FAILED", "Could not unpair device", null);
            }

        } catch (Exception e) {
            Log.e("UnpairDevice", "Error unpairing device: " + e.getMessage());
            result.error("UNPAIR_ERROR", "Error unpairing device: " + e.getMessage(), null);
        }
    }
}
