package com.sm.sm_omron;


import static com.sm.sm_omron.OmronManager.mSelectedPeripheral;

import android.os.Handler;
import android.util.Log;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.DeviceConfiguration.OmronPeripheralManagerConfig;
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.Interface.OmronPeripheralManagerConnectListener;
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.LibraryManager.OmronPeripheralManager;
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.Model.OmronErrorInfo;
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.Model.OmronPeripheral;
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.OmronUtility.OmronConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import io.flutter.plugin.common.MethodChannel;

public class ConnectDevice  {
    final MethodChannel.Result result;
    OmronPeripheral omronPeripheral;
    private final Context applicationContext;
    private final HashMap<String, Object> deviceConfig;

    ConnectDevice(Context context, OmronPeripheral omronPeripheral, HashMap<String, Object> deviceConfig, MethodChannel.Result result) {
        this.applicationContext = context;
        this.omronPeripheral = omronPeripheral;
        this.deviceConfig = deviceConfig;
        this.result = result;
    }

    void connectPeripheral() {
        Log.d("ConnectDevice", "Starting connection/pairing to: " + omronPeripheral.getLocalName());

        // Configure the manager with the specific device details (Group IDs)
        // This is required for the SDK to know how to communicate with the device
        try {
            OmronPeripheralManagerConfig peripheralConfig = OmronPeripheralManager.sharedManager(applicationContext).getConfiguration();
            
            // Convert Object map to String map for the filter
            HashMap<String, String> filterDevice = new HashMap<>();
            
            // Use OmronConstants for the keys expected by the SDK
            if (deviceConfig.get("deviceGroupIDKey") != null) {
                filterDevice.put(OmronConstants.OMRONBLEConfigDevice.GroupID, String.valueOf(deviceConfig.get("deviceGroupIDKey")));
            }
            if (deviceConfig.get("deviceGroupIncludedGroupIDKey") != null) {
                filterDevice.put(OmronConstants.OMRONBLEConfigDevice.GroupIncludedGroupID, String.valueOf(deviceConfig.get("deviceGroupIncludedGroupIDKey")));
            }
            // Add category as it is often required
            if (deviceConfig.get("category") != null) {
                filterDevice.put(OmronConstants.OMRONBLEConfigDevice.Category, String.valueOf(deviceConfig.get("category")));
            }
            
            if (!filterDevice.isEmpty()) {
                List<HashMap<String, String>> filterDevices = new ArrayList<>();
                filterDevices.add(filterDevice);
                peripheralConfig.deviceFilters = filterDevices;
                
                ArrayList<HashMap> deviceSettings = new ArrayList<>();

                // Add category-specific settings to satisfy SDK requirements
                if (deviceConfig.get("category") != null) {
                    try {
                        int category = Integer.parseInt(String.valueOf(deviceConfig.get("category")));
                        
                        // Blood Pressure Settings
                        if (category == OmronConstants.OMRONBLEDeviceCategory.BLOODPRESSURE) {
                            HashMap<String, Object> bloodPressurePersonalSettings = new HashMap<>();
                            bloodPressurePersonalSettings.put(OmronConstants.OMRONDevicePersonalSettings.BloodPressureTruReadEnableKey, OmronConstants.OMRONDevicePersonalSettingsBloodPressureTruReadStatus.On);
                            bloodPressurePersonalSettings.put(OmronConstants.OMRONDevicePersonalSettings.BloodPressureTruReadIntervalKey, OmronConstants.OMRONDevicePersonalSettingsBloodPressureTruReadInterval.Interval30);
                            HashMap<String, Object> settings = new HashMap<>();
                            settings.put(OmronConstants.OMRONDevicePersonalSettings.BloodPressureKey, bloodPressurePersonalSettings);
                            
                            HashMap<String, HashMap> personalSettings = new HashMap<>();
                            personalSettings.put(OmronConstants.OMRONDevicePersonalSettingsKey, settings);
                            deviceSettings.add(personalSettings);
                            
                            HashMap<String, Object> transferModeSettings = new HashMap<>();
                            HashMap<String, HashMap> transferSettings = new HashMap<>();
                            transferModeSettings.put(OmronConstants.OMRONDeviceScanSettings.ModeKey, OmronConstants.OMRONDeviceScanSettingsMode.Pairing);
                            transferSettings.put(OmronConstants.OMRONDeviceScanSettingsKey, transferModeSettings);
                            deviceSettings.add(transferSettings);
                        }
                    } catch (Exception e) {
                        Log.e("ConnectDevice", "Error adding device settings: " + e.getMessage());
                    }
                }
                
                peripheralConfig.deviceSettings = deviceSettings;

                if (peripheralConfig.timeoutInterval == 0) {
                     peripheralConfig.timeoutInterval = 30;
                }

                if (peripheralConfig.sequenceNumbersForTransfer == null) {
                    peripheralConfig.sequenceNumbersForTransfer = new HashMap<>();
                }
                if (!peripheralConfig.sequenceNumbersForTransfer.containsKey(1)) {
                    peripheralConfig.sequenceNumbersForTransfer.put(1, 0);
                }
                if (!peripheralConfig.sequenceNumbersForTransfer.containsKey(2)) {
                    peripheralConfig.sequenceNumbersForTransfer.put(2, 0);
                }

                peripheralConfig.userHashId = "user@example.com"; // Mandatory field


                OmronPeripheralManager.sharedManager(applicationContext).setConfiguration(peripheralConfig);
                OmronPeripheralManager.sharedManager(applicationContext).startManager();

                performOmronHandshake();
            }
        } catch (Exception e) {
             Log.e("ConnectDevice", "Unexpected error in bonding: " + e.getMessage());
             result.error("UNEXPECTED_ERROR", e.getMessage(), null);
        }
    }


    private void performOmronHandshake() {
        Log.d("ConnectDevice", "Performing Omron SDK handshake for: " + omronPeripheral.getLocalName());
        
        OmronPeripheralManager.sharedManager(applicationContext).connectPeripheral(omronPeripheral, new OmronPeripheralManagerConnectListener() {
            @Override
            public void onConnectCompleted(final OmronPeripheral peripheral, OmronErrorInfo errorInfo) {
                if (errorInfo.isSuccess()) {
                    Log.d("ConnectDevice", "Omron SDK Handshake Successful. Device should now show OK. Disconnecting...");
                    
                    // Explicitly disconnect after handshake to release the device
                    OmronPeripheralManager.sharedManager(applicationContext).disconnectPeripheral(peripheral, new com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.Interface.OmronPeripheralManagerDisconnectListener() {
                        @Override
                        public void onDisconnectCompleted(OmronPeripheral p, OmronErrorInfo e) {
                            Log.d("ConnectDevice", "Disconnected after handshake.");
                            // Return success to Flutter only after disconnection is initiated
                            if (!result.getClass().getName().contains("ErrorResult")) {
                               try {
                                   result.success(true);
                               } catch (Exception ex) {
                                   Log.e("ConnectDevice", "Error returning result: " + ex.getMessage());
                               }
                            }
                        }
                    });
                } else {
                    Log.e("ConnectDevice", "Omron SDK Handshake Failed: " + errorInfo.getMessageInfo());
                    // Even if handshake fails, the bond might be established. 
                    // However, we report error to let Flutter know pairing didn't fully complete.
                    result.error("HANDSHAKE_FAILED", errorInfo.getMessageInfo(), errorInfo.getDetailInfo());
                }
            }
        });
    }
}
