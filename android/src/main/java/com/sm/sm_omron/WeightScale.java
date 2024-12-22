package com.sm.sm_omron;
import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.google.gson.Gson;

import android.util.Log;
import android.content.Context;
import com.google.gson.Gson;
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.DeviceConfiguration.OmronPeripheralManagerConfig;

import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.Interface.OmronPeripheralManagerDataTransferListener;
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.Interface.*;
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.Interface.OmronPeripheralManagerDisconnectListener;
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.LibraryManager.OmronPeripheralManager;
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.Model.OmronErrorInfo;
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.Model.OmronPeripheral;
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.OmronUtility.OmronConstants;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import io.flutter.plugin.common.MethodChannel;

public class WeightScale {

    private String TAG = "WeightScale";

    private final   Activity activity;
    private final   Context applicationContext;
    private ArrayList<OmronPeripheral> mPeripheralList;
    private OmronPeripheral mSelectedPeripheral;
    private ArrayList<Integer> selectedUsers = new ArrayList<>();
    private Bundle weightBundle;
    MethodChannel.Result result;

 private    HashMap<String, String> device = null;

    WeightScale(  Context applicationContext,Activity activity){

        this.activity=activity;
        this.applicationContext =applicationContext;
    };



    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            // Get extra data included in the Intent
            int status = intent.getIntExtra(OmronConstants.OMRONBLEBluetoothStateKey, 0);

            if (status == OmronConstants.OMRONBLEBluetoothState.OMRONBLEBluetoothStateUnknown) {

                Log.d(TAG, "Bluetooth is in unknown state");

            } else if (status == OmronConstants.OMRONBLEBluetoothState.OMRONBLEBluetoothStateOff) {

                Log.d(TAG, "Bluetooth is currently powered off");

            } else if (status == OmronConstants.OMRONBLEBluetoothState.OMRONBLEBluetoothStateOn) {

                Log.d(TAG, "Bluetooth is currently powered on");
            }
        }
    };



    public void init(MethodChannel.Result result){

        this.result=result;
        this.device=OmronManager.device;

        selectedUsers = new ArrayList<>();
        selectedUsers.add(1);

        weightBundle = new Bundle();
        weightBundle.putString(Constants.bundleKeys.KEY_BUNDLE_WEIGHT_UNIT, "Kg");
//        if (noOfUsers > 1) {
//          //  float height_cm = Float.valueOf(etHeight.getText().toString());
//        String height = String.valueOf((int) (Utilities.round(100, 2) * 100));
        weightBundle.putString(Constants.bundleKeys.KEY_BUNDLE_DOB, "202411");
        weightBundle.putString(Constants.bundleKeys.KEY_BUNDLE_GENDER, "Male");
        weightBundle.putString(Constants.bundleKeys.KEY_BUNDLE_HEIGHT_CM, "165");
//        }


//        String localName="BLESmart_00010212C1C15F482CB0";
//        String uuid="C1:C1:5F:48:2C:B0";
        String uuid=(String) device.get("uuid");
        String localName=(String) device.get("localName");


        Log.d("device","  **********  \n "+device.toString());

        mSelectedPeripheral=new OmronPeripheral(localName,uuid);



        startOmronPeripheralManager(false,true);

        transferUsersDataWithPeripheral(mSelectedPeripheral);



    }




    private void startOmronPeripheralManager(boolean isHistoricDataRead, boolean isPairing) {

        OmronPeripheralManagerConfig peripheralConfig = OmronPeripheralManager.sharedManager(applicationContext).getConfiguration();
        // Log.d(TAG, "Library Identifier : " + peripheralConfig.getLibraryIdentifier());

        // Filter device to scan and connect (optional)
        if (device != null && device.get(OmronConstants.OMRONBLEConfigDevice.GroupID) != null && device.get(OmronConstants.OMRONBLEConfigDevice.GroupIncludedGroupID) != null) {

            // Add item
            List<HashMap<String, String>> filterDevices = new ArrayList<>();
            filterDevices.add(device);
            peripheralConfig.deviceFilters = filterDevices;
        }

        ArrayList<HashMap> deviceSettings = new ArrayList<>();

        // BCM device settings (optional)
        deviceSettings = getBCMSettings(deviceSettings);

        // Scan settings (optional)
        deviceSettings = getScanSettings(deviceSettings, isPairing);

        peripheralConfig.deviceSettings = deviceSettings;
        // Set Scan timeout interval (optional)
        peripheralConfig.timeoutInterval = Constants.CONNECTION_TIMEOUT;
        // Set User Hash Id (mandatory)
        peripheralConfig.userHashId = "<email_address_of_user>"; // Set logged in user email

        // Disclaimer: Read definition before usage
        if (Integer.parseInt(device.get(OmronConstants.OMRONBLEConfigDevice.Category)) != OmronConstants.OMRONBLEDeviceCategory.ACTIVITY) {
            // Reads all data from device.
            peripheralConfig.enableAllDataRead = isHistoricDataRead;
        }

        // Pass the last sequence number of reading  tracked by app - "SequenceKey" for each vital data
//        HashMap<Integer, Integer> sequenceNumbersForTransfer = new HashMap<>();
//        sequenceNumbersForTransfer.put(1, 20);
//        sequenceNumbersForTransfer.put(2, 0);
//        peripheralConfig.sequenceNumbersForTransfer = sequenceNumbersForTransfer;

        // Set configuration for OmronPeripheralManager
        OmronPeripheralManager.sharedManager(applicationContext).setConfiguration(peripheralConfig);


        //Initialize the connection process.
        OmronPeripheralManager.sharedManager(applicationContext).startManager();

        // Notification Listener for BLE State Change
        LocalBroadcastManager.getInstance(applicationContext).registerReceiver(mMessageReceiver,
                new IntentFilter(OmronConstants.OMRONBLECentralManagerDidUpdateStateNotification));
    }

    private void transferUsersDataWithPeripheral(OmronPeripheral peripheral) {

        OmronPeripheralManager.sharedManager(applicationContext).startDataTransferFromPeripheral(peripheral, selectedUsers, true, new OmronPeripheralManagerDataTransferListener() {
            @Override
            public void onDataTransferCompleted(OmronPeripheral peripheral, final OmronErrorInfo resultInfo) {

                if ( peripheral != null) {

                    HashMap<String, String> deviceInformation = peripheral.getDeviceInformation();
                    Log.d(TAG, "Device Information : " + deviceInformation);

                    ArrayList<HashMap> allSettings = (ArrayList<HashMap>) peripheral.getDeviceSettings();
                    Log.i(TAG, "Device settings : " + allSettings.toString());

                    mSelectedPeripheral = peripheral; // Saving for Transfer Function


                    // Save Device to List
                    // To change based on data available
                   // preferencesManager.addDataStoredDeviceList(peripheral.getLocalName(), Integer.parseInt(device.get(OmronConstants.OMRONBLEConfigDevice.Category)), peripheral.getModelName(),device,peripheral.getDeviceInformation());

                    // Get vital data for previously selected user using OmronPeripheral
                    Object output = peripheral.getVitalData();

                    if (output instanceof OmronErrorInfo) {

                        final OmronErrorInfo errorInfo = (OmronErrorInfo) output;


                        disconnectDevice();

                    } else {

                        HashMap<String, Object> vitalData = (HashMap<String, Object>) output;

                        if (vitalData != null) {
                            uploadData(vitalData, peripheral, true);
                        }
                    }

                } else {

                    Log.d("device","No Device found");

                }
            }

        });
    }


    private void uploadData(HashMap<String, Object> vitalData, OmronPeripheral peripheral, boolean isWait) {

        HashMap<String, String> deviceInfo = peripheral.getDeviceInformation();

        // Weight Data
//        ArrayList<HashMap<String, Object>> weightData = (ArrayList<HashMap<String, Object>>) vitalData.get(OmronConstants.OMRONVitalDataWeightKey);



//        if (weightData != null) {
//
//            for (HashMap<String, Object> weightItem : weightData) {
//
//                Log.d("Weight - ", weightItem.toString());
//
//            }
//        }
        ArrayList<HashMap<String, Object>> weightData = (ArrayList<HashMap<String, Object>>) vitalData.get(OmronConstants.OMRONVitalDataWeightKey);
        if (weightData != null  && !weightData.isEmpty()) {

            Gson gson = new Gson();

            List<String> jsonList = new ArrayList<String>();

            for (HashMap<String, Object> element : weightData) {
                jsonList.add(gson.toJson(element));
            }
            //   Log.d("weightItemList","  this is from weightItemList check\n json list *********************************  "+ jsonList.toString());

            result.success(jsonList);
            disconnectDevice();
            return;

        }






//        if (isWait) {
//
//            mHandler = new Handler();
//            mRunnable = new Runnable() {
//                @Override
//                public void run() {
//                    continueDataTransfer();
//                }
//            };
//
//            mHandler.postDelayed(mRunnable, TIME_INTERVAL);
//
//        } else {
//
//            if (mHandler != null)
//                mHandler.removeCallbacks(mRunnable);
//
//            continueDataTransfer();
//        }
    }
    private void disconnectDevice() {

        // Disconnect device using OmronPeripheralManager
        OmronPeripheralManager.sharedManager(applicationContext).disconnectPeripheral(mSelectedPeripheral, new OmronPeripheralManagerDisconnectListener() {
            @Override
            public void onDisconnectCompleted(OmronPeripheral peripheral, OmronErrorInfo resultInfo) {

            }
        });
    }


    private ArrayList<HashMap> getBCMSettings(ArrayList<HashMap> deviceSettings) {

        // body composition
        if (Integer.parseInt(device.get(OmronConstants.OMRONBLEConfigDevice.Category)) == OmronConstants.OMRONBLEDeviceCategory.BODYCOMPOSITION) {

            HashMap<String, Object> settings = new HashMap<>();
            if (Integer.parseInt(device.get(OmronConstants.OMRONBLEConfigDevice.Users)) > 1) {
                // BCM configuration

                String gender = weightBundle.getString(Constants.bundleKeys.KEY_BUNDLE_GENDER);
                int genderValue = OmronConstants.OMRONDevicePersonalSettingsUserGenderType.Male;
                if (gender.equals("Female")) {
                    genderValue = OmronConstants.OMRONDevicePersonalSettingsUserGenderType.Female;
                }

                settings.put(OmronConstants.OMRONDevicePersonalSettings.UserHeightKey, weightBundle.getString(Constants.bundleKeys.KEY_BUNDLE_HEIGHT_CM));
                settings.put(OmronConstants.OMRONDevicePersonalSettings.UserGenderKey, genderValue);
                settings.put(OmronConstants.OMRONDevicePersonalSettings.UserDateOfBirthKey, weightBundle.getString(Constants.bundleKeys.KEY_BUNDLE_DOB, "19911221"));
            }

            HashMap<String, HashMap> personalSettings = new HashMap<>();
            personalSettings.put(OmronConstants.OMRONDevicePersonalSettingsKey, settings);

            // Weight Settings
            // Add other weight common settings if any
            String unit = weightBundle.getString(Constants.bundleKeys.KEY_BUNDLE_WEIGHT_UNIT);
            int unitValue;
            if (unit.equals("Kg")) {
                unitValue = OmronConstants.OMRONDeviceWeightUnit.Kg;
            } else if (unit.equals("Lbs")) {
                unitValue = OmronConstants.OMRONDeviceWeightUnit.Lbs;
            } else {
                unitValue = OmronConstants.OMRONDeviceWeightUnit.St;

            }
            HashMap<String, Object> weightCommonSettings = new HashMap<>();
            weightCommonSettings.put(OmronConstants.OMRONDeviceWeightSettings.UnitKey, unitValue);
            HashMap<String, Object> weightSettings = new HashMap<>();
            weightSettings.put(OmronConstants.OMRONDeviceWeightSettingsKey, weightCommonSettings);

            deviceSettings.add(personalSettings);
            deviceSettings.add(weightSettings);
        }

        return deviceSettings;
    }

    private ArrayList<HashMap> getScanSettings(ArrayList<HashMap> deviceSettings, boolean isPairing) {

        // Scan Settings
        HashMap<String, Object> ScanModeSettings = new HashMap<>();
        HashMap<String, HashMap> ScanSettings = new HashMap<>();
        if(isPairing) {
            ScanModeSettings.put(OmronConstants.OMRONDeviceScanSettings.ModeKey, OmronConstants.OMRONDeviceScanSettingsMode.Pairing);
        }else {
            ScanModeSettings.put(OmronConstants.OMRONDeviceScanSettings.ModeKey, OmronConstants.OMRONDeviceScanSettingsMode.MismatchSequence);
        }
        ScanSettings.put(OmronConstants.OMRONDeviceScanSettingsKey, ScanModeSettings);

        deviceSettings.add(ScanSettings);

        return deviceSettings;
    }


}
