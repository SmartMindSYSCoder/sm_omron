package com.sm.sm_omron;


import static com.sm.sm_omron.OmronManager.mSelectedPeripheral;

import android.os.Handler;
import android.util.Log;
import android.content.Context;
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
            // Add other necessary keys if needed, assuming these match OmronConstants
            
            if (!filterDevice.isEmpty()) {
                List<HashMap<String, String>> filterDevices = new ArrayList<>();
                filterDevices.add(filterDevice);
                peripheralConfig.deviceFilters = filterDevices;
                
                // Create a FRESH deviceSettings list to avoid stale state
                ArrayList<HashMap> deviceSettings = new ArrayList<>();

                if (peripheralConfig.deviceSettings == null) {
                    // peripheralConfig.deviceSettings = new ArrayList<>(); // This line is no longer needed as deviceSettings is declared above
                }
                
                // Add category-specific settings to satisfy SDK requirements
                if (deviceConfig.get("category") != null) {
                    try {
                        int category = Integer.parseInt(String.valueOf(deviceConfig.get("category")));
                        
                        // Blood Pressure Settings
                        if (category == OmronConstants.OMRONBLEDeviceCategory.BLOODPRESSURE) {
                            // Add default BP settings
                             HashMap<String, Object> bloodPressurePersonalSettings = new HashMap<>();
                            bloodPressurePersonalSettings.put(OmronConstants.OMRONDevicePersonalSettings.BloodPressureTruReadEnableKey, OmronConstants.OMRONDevicePersonalSettingsBloodPressureTruReadStatus.On);
                            bloodPressurePersonalSettings.put(OmronConstants.OMRONDevicePersonalSettings.BloodPressureTruReadIntervalKey, OmronConstants.OMRONDevicePersonalSettingsBloodPressureTruReadInterval.Interval30);
                            HashMap<String, Object> settings = new HashMap<>();
                            settings.put(OmronConstants.OMRONDevicePersonalSettings.BloodPressureKey, bloodPressurePersonalSettings);
                            
                            HashMap<String, HashMap> personalSettings = new HashMap<>();
                            personalSettings.put(OmronConstants.OMRONDevicePersonalSettingsKey, settings);
                            deviceSettings.add(personalSettings);
                            
                            // Add Transfer Scan Settings (Pairing Mode)
                            HashMap<String, Object> transferModeSettings = new HashMap<>();
                            HashMap<String, HashMap> transferSettings = new HashMap<>();
                            // Explicit pairing mode
                            transferModeSettings.put(OmronConstants.OMRONDeviceScanSettings.ModeKey, OmronConstants.OMRONDeviceScanSettingsMode.Pairing);
                            transferSettings.put(OmronConstants.OMRONDeviceScanSettingsKey, transferModeSettings);
                            deviceSettings.add(transferSettings);
                        }
                    } catch (Exception e) {
                        Log.e("ConnectDevice", "Error adding device settings: " + e.getMessage());
                    }
                }
                
                // Assign the fresh settings list
                peripheralConfig.deviceSettings = deviceSettings;

                // Set other mandatory fields
                if (peripheralConfig.timeoutInterval == 0) {
                     peripheralConfig.timeoutInterval = 30; // Default timeout
                }

                // Sequence Numbers (Mandatory for some flows)
                if (peripheralConfig.sequenceNumbersForTransfer == null) {
                    peripheralConfig.sequenceNumbersForTransfer = new HashMap<>();
                }
                // Ensure at least basic keys exist
                if (!peripheralConfig.sequenceNumbersForTransfer.containsKey(1)) {
                    peripheralConfig.sequenceNumbersForTransfer.put(1, 0);
                }
                if (!peripheralConfig.sequenceNumbersForTransfer.containsKey(2)) {
                    peripheralConfig.sequenceNumbersForTransfer.put(2, 0);
                }

                // Apply configuration and start manager
                OmronPeripheralManager.sharedManager(applicationContext).setConfiguration(peripheralConfig);
                OmronPeripheralManager.sharedManager(applicationContext).startManager();

                // Explicitly force pairing using native Android Bluetooth API
                // This ensures the system pairing dialog is shown
                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (bluetoothAdapter != null) {
                    BluetoothDevice device = bluetoothAdapter.getRemoteDevice(omronPeripheral.getUuid());
                    if (device != null) {
                        if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                            Log.d("ConnectDevice", "Device already bonded: " + omronPeripheral.getUuid());
                            result.success(true);
                        } else {
                            Log.d("ConnectDevice", "Creating bond for: " + omronPeripheral.getUuid());
                            boolean success = device.createBond();
                             if (success) {
                                // Result will be handled by SDK broadcast listener eventually, 
                                // but for now we return true as the process initiated successfully.
                                result.success(true);
                            } else {
                                Log.e("ConnectDevice", "Failed to create bond (createBond returned false)");
                                result.error("PAIRING_FAILED", "Failed to initiate pairing", null);
                            }
                        }
                    } else {
                         result.error("DEVICE_NOT_FOUND", "BluetoothDevice is null", null);
                    }
                } else {
                    result.error("BLUETOOTH_UNAVAILABLE", "BluetoothAdapter is null", null);
                }
            }
        } catch (Exception e) {
             Log.e("ConnectDevice", "Unexpected error in bonding: " + e.getMessage());
             result.success(false); // Or error
        }
    }
}
