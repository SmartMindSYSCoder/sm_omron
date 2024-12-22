package com.sm.sm_omron;


import  static com.sm.sm_omron.OmronManager.device;
import  static com.sm.sm_omron.OmronManager.isScan;
import  static com.sm.sm_omron.OmronManager.mPeripheralList;
import  static com.sm.sm_omron.OmronManager.mSelectedPeripheral;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.util.Log;
import android.view.KeyEvent;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.gson.Gson;
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.DeviceConfiguration.OmronPeripheralManagerConfig;
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.Interface.OmronPeripheralManagerScanListener;
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.Interface.OmronPeripheralManagerStopScanListener;
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.LibraryManager.OmronPeripheralManager;
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.Model.OmronErrorInfo;
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.Model.OmronPeripheral;
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.OmronUtility.OmronConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import android.util.Log;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodChannel;

public class ScanningDevices  implements EventChannel.StreamHandler {

    private final   Activity activity;
    private final   Context applicationContext;

    ScanningDevices(Activity activity, Context applicationContext){

        this.activity=activity;
        this.applicationContext =applicationContext;
    };


    /// device varibales

    HashMap<String, String> personalSettings = null;
//    private Boolean isScan = false;

    private ArrayList<Integer> selectedUsers = new ArrayList<>();

//    private ArrayList<OmronPeripheral> mPeripheralList;

    private EventChannel.EventSink scanningDevicesEvent;

    StateChangesStatus stateChangesStatus;


    void initializeFun(StateChangesStatus stateChangesStatus) {
        isScan = false;
        // Selected users
        selectedUsers = null;
        if (selectedUsers == null) {
            selectedUsers = new ArrayList<>();
            selectedUsers.add(1);
        }

        this.stateChangesStatus = stateChangesStatus;

        /*getIntent().getSerializableExtra(Constants.extraKeys.KEY_SELECTED_DEVICE)*/
        ;

        //Personal settings like height, weight etc for activity devices.
        personalSettings = null/*(HashMap<String, String>) getIntent().getSerializableExtra(Constants.extraKeys.KEY_PERSONAL_SETTINGS)*/;
        startOmronPeripheralManager(false, true);
//        startScanning();
    }


    void startOrStopScanning(MethodChannel.Result result) {

        Log.d("startScanning", "on start scanning");
        // Start OmronPeripheralManager
        startOmronPeripheralManager(false, true);

        // Set State Change Listener
        stateChangesStatus.setStateChanges();

        if (isScan) {
            stopScanning(result);
        } else {

            // Start Scanning for Devices using OmronPeripheralManager
            OmronPeripheralManager.sharedManager(applicationContext).startScanPeripherals(new OmronPeripheralManagerScanListener() {

                @Override
                public void onScanCompleted(final ArrayList<OmronPeripheral> peripheralList, final OmronErrorInfo resultInfo) {
                    Log.d("onScanCompleted", "sss");
                    Log.d("onScanCompleted", "runOnUiThread");
                    Log.d("resultInfo", "resultInfo :"+resultInfo.getResultCode());




                    if (resultInfo.getResultCode() == 0) {

                        mPeripheralList = peripheralList;
                        Gson gson = new Gson();
                        List<String> jsonList = new ArrayList<String>();

                        for (OmronPeripheral element : mPeripheralList) {
                            HashMap<String, String> map = new HashMap<String, String>();
                            map.put("modelName", element.getModelName());
                            map.put("uuid", element.getUuid());
                            map.put("serialId", element.getSerialId());
                            map.put("deviceInformation", gson.toJson(element.getDeviceInformation()));
                            jsonList.add(gson.toJson(map));

//                            OmronPeripheral omronPeripheral =new OmronPeripheral(element.getLocalName(),element.getUuid());
//                            omronPeripheral.setModelName(element.getModelName());
//                            omronPeripheral.setModelSeries(element.getModelSeries());
                            mSelectedPeripheral=element;



//                            ConnectDevice connectDevice=new ConnectDevice(applicationContext,element);
//
//                            connectDevice.connectPeripheral();

//                            break;

                            Log.d("json data", "json data from scan ***********\n"+gson.toJson(map));

                            isScan = false;
                            stopScanning(result); // Stop scanning
                            result.success(gson.toJson(map));
                            return;
                        }


                        Log.d("json list", jsonList.toString());
//                                Log.d("devices list", mPeripheralList.toString());


                        if(!mPeripheralList.isEmpty()){

                            isScan=false;
                            stopScanning(result);

                        }


//                        if (scanningDevicesEvent != null) {
//                            scanningDevicesEvent.success(jsonList);
//
//                            result.success(jsonList.toString());
//
//                          //  stopScanning(result);
////                            return;
//
//                        }



                                Log.d("devices list", " ************  devices list ************* \n"+peripheralList.toString());
                    } else {
                        isScan = !isScan;
                        result.success( "Not device found");
                    }
                }
            });
        }

        isScan = !isScan;
    }

    private void stopScanning(MethodChannel.Result result) {
        Log.d("scan", "stopScanning");
        //scanBtn.setText("SCAN");
        // Stop Scanning for Devices using OmronPeripheralManager
        OmronPeripheralManager.sharedManager(applicationContext).stopScanPeripherals(new OmronPeripheralManagerStopScanListener() {
            @Override
            public void onStopScanCompleted(final OmronErrorInfo resultInfo) {
                Log.d("onStopScan", "inside on scan completed");

                Log.d("onStopScand", "inside on scan completed");
//                mPeripheralList = new ArrayList<OmronPeripheral>();
//
//                if (resultInfo.getResultCode() == 0) {
//                    if (scanningDevicesEvent != null) {
//                        scanningDevicesEvent.success(
//                                new ArrayList<String>());
//                    }
//                            result.success(true);
//                } else {
////                            result.error("88", "Error on stop scanning", null);
//
//
////                         Toast.makeText(MainActivity.this, "Error Code : " + resultInfo.getResultCode() + "\nError Detail Code : " + resultInfo.getDetailInfo(), Toast.LENGTH_LONG).show();
//                }

            }
        });
    }


    private void startOmronPeripheralManager(boolean isHistoricDataRead, boolean isPairing) {

        OmronPeripheralManagerConfig peripheralConfig = OmronPeripheralManager.sharedManager(applicationContext).getConfiguration();
//        Log.d(TAG, "Library Identifier : " + peripheralConfig.getLibraryIdentifier());

        // Filter device to scan and connect (optional)
        if (device != null && device.get(OmronConstants.OMRONBLEConfigDevice.GroupID) != null && device.get(OmronConstants.OMRONBLEConfigDevice.GroupIncludedGroupID) != null) {

            // Add item
            List<HashMap<String, String>> filterDevices = new ArrayList<>();
            filterDevices.add(device);
            peripheralConfig.deviceFilters = filterDevices;
        }

        ArrayList<HashMap> deviceSettings = new ArrayList<>();

        // Blood pressure settings (optional)
        deviceSettings = getBloodPressureSettings(deviceSettings, isPairing);

        // Activity device settings (optional)
        deviceSettings = getActivitySettings(deviceSettings);

        // BCM device settings (optional)
        deviceSettings = getBCMSettings(deviceSettings);

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
        HashMap<Integer, Integer> sequenceNumbersForTransfer = new HashMap<>();
        sequenceNumbersForTransfer.put(1, 42);
        sequenceNumbersForTransfer.put(2, 8);
        peripheralConfig.sequenceNumbersForTransfer = sequenceNumbersForTransfer;

        // Set configuration for OmronPeripheralManager
        OmronPeripheralManager.sharedManager(applicationContext).setConfiguration(peripheralConfig);

        //Initialize the connection process.
        OmronPeripheralManager.sharedManager(applicationContext).startManager();

        // Notification Listener for BLE State Change
        LocalBroadcastManager.getInstance(applicationContext).registerReceiver(mMessageReceiver,
                new IntentFilter(OmronConstants.OMRONBLECentralManagerDidUpdateStateNotification));
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {


            // Get extra data included in the Intent
            int status = intent.getIntExtra(OmronConstants.OMRONBLEBluetoothStateKey, 0);
            Log.d("onReceive","status "+status);

            if (status == OmronConstants.OMRONBLEBluetoothState.OMRONBLEBluetoothStateUnknown) {

                Log.d("OMRONBLEBluetooth", "Bluetooth is in unknown state");

            } else if (status == OmronConstants.OMRONBLEBluetoothState.OMRONBLEBluetoothStateOff) {

                Log.d("OMRONBLEBluetooth", "Bluetooth is currently powered off");

            } else if (status == OmronConstants.OMRONBLEBluetoothState.OMRONBLEBluetoothStateOn) {

                Log.d("OMRONBLEBluetooth", "Bluetooth is currently powered on");
            }
        }
    };

    private ArrayList<HashMap> getBloodPressureSettings(ArrayList<HashMap> deviceSettings, boolean isPairing) {


        Log.d("getBloodPressureSettings","getBloodPressureSettings");
        // Blood Pressure
        if (Integer.parseInt(device.get(OmronConstants.OMRONBLEConfigDevice.Category)) == OmronConstants.OMRONBLEDeviceCategory.BLOODPRESSURE) {
            HashMap<String, Object> bloodPressurePersonalSettings = new HashMap<>();
            bloodPressurePersonalSettings.put(OmronConstants.OMRONDevicePersonalSettings.BloodPressureTruReadEnableKey, OmronConstants.OMRONDevicePersonalSettingsBloodPressureTruReadStatus.On);
            bloodPressurePersonalSettings.put(OmronConstants.OMRONDevicePersonalSettings.BloodPressureTruReadIntervalKey, OmronConstants.OMRONDevicePersonalSettingsBloodPressureTruReadInterval.Interval30);
            HashMap<String, Object> settings = new HashMap<>();
            settings.put(OmronConstants.OMRONDevicePersonalSettings.BloodPressureKey, bloodPressurePersonalSettings);
            HashMap<String, HashMap> personalSettings = new HashMap<>();
            personalSettings.put(OmronConstants.OMRONDevicePersonalSettingsKey, settings);

            HashMap<String, Object> transferModeSettings = new HashMap<>();
            HashMap<String, HashMap> transferSettings = new HashMap<>();
            if (isPairing) {
                transferModeSettings.put(OmronConstants.OMRONDeviceScanSettings.ModeKey, OmronConstants.OMRONDeviceScanSettingsMode.Pairing);
            } else {
                transferModeSettings.put(OmronConstants.OMRONDeviceScanSettings.ModeKey, OmronConstants.OMRONDeviceScanSettingsMode.MismatchSequence);
            }
            transferSettings.put(OmronConstants.OMRONDeviceScanSettingsKey, transferModeSettings);

            // Personal settings for device
            deviceSettings.add(personalSettings);

            deviceSettings.add(transferSettings);
        }

        return deviceSettings;
    }

    private ArrayList<HashMap> getActivitySettings(ArrayList<HashMap> deviceSettings) {

        // Activity Tracker
        if (Integer.parseInt(device.get(OmronConstants.OMRONBLEConfigDevice.Category)) == OmronConstants.OMRONBLEDeviceCategory.ACTIVITY) {

            // Set Personal Settings in Configuration (mandatory for Activity devices)
            if (personalSettings != null) {

                HashMap<String, String> settingsModel = new HashMap<String, String>();
                settingsModel.put(OmronConstants.OMRONDevicePersonalSettings.UserHeightKey, personalSettings.get("personalHeight"));
                settingsModel.put(OmronConstants.OMRONDevicePersonalSettings.UserWeightKey, personalSettings.get("personalWeight"));
                settingsModel.put(OmronConstants.OMRONDevicePersonalSettings.UserStrideKey, personalSettings.get("personalStride"));
                settingsModel.put(OmronConstants.OMRONDevicePersonalSettings.TargetSleepKey, "120");
                settingsModel.put(OmronConstants.OMRONDevicePersonalSettings.TargetStepsKey, "2000");

                HashMap<String, HashMap> userSettings = new HashMap<>();
                userSettings.put(OmronConstants.OMRONDevicePersonalSettingsKey, settingsModel);

                // Notification settings
                ArrayList<String> notificationsAvailable = new ArrayList<>();
                notificationsAvailable.add("android.intent.action.PHONE_STATE");
                notificationsAvailable.add("com.google.android.gm");
                notificationsAvailable.add("android.provider.Telephony.SMS_RECEIVED");
                notificationsAvailable.add("com.omronhealthcare.OmronConnectivitySample");
                HashMap<String, Object> notificationSettings = new HashMap<String, Object>();
                notificationSettings.put(OmronConstants.OMRONDeviceNotificationSettingsKey, notificationsAvailable);

                // Time Format
                HashMap<String, Object> timeFormatSettings = new HashMap<String, Object>();
                timeFormatSettings.put(OmronConstants.OMRONDeviceTimeSettings.FormatKey, OmronConstants.OMRONDeviceTimeFormat.Time12Hour);
                HashMap<String, HashMap> timeSettings = new HashMap<>();
                timeSettings.put(OmronConstants.OMRONDeviceTimeSettingsKey, timeFormatSettings);


                // Sleep Settings
                HashMap<String, Object> sleepTimeSettings = new HashMap<String, Object>();
                sleepTimeSettings.put(OmronConstants.OMRONDeviceSleepSettings.AutomaticKey, OmronConstants.OMRONDeviceSleepAutomatic.Off);
                sleepTimeSettings.put(OmronConstants.OMRONDeviceSleepSettings.StartTimeKey, "19");
                sleepTimeSettings.put(OmronConstants.OMRONDeviceSleepSettings.StopTimeKey, "20");
                HashMap<String, HashMap> sleepSettings = new HashMap<>();
                sleepSettings.put(OmronConstants.OMRONDeviceSleepSettingsKey, sleepTimeSettings);


                // Alarm Settings
                // Alarm 1 Time
                HashMap<String, Object> alarmTime1 = new HashMap<String, Object>();
                alarmTime1.put(OmronConstants.OMRONDeviceAlarmSettings.HourKey, "15");
                alarmTime1.put(OmronConstants.OMRONDeviceAlarmSettings.MinuteKey, "33");
                // Alarm 1 Day (SUN-SAT)
                HashMap<String, Object> alarmDays1 = new HashMap<String, Object>();
                alarmDays1.put(OmronConstants.OMRONDeviceAlarmSettings.SundayKey, OmronConstants.OMRONDeviceAlarmStatus.Off);
                alarmDays1.put(OmronConstants.OMRONDeviceAlarmSettings.MondayKey, OmronConstants.OMRONDeviceAlarmStatus.Off);
                alarmDays1.put(OmronConstants.OMRONDeviceAlarmSettings.TuesdayKey, OmronConstants.OMRONDeviceAlarmStatus.Off);
                alarmDays1.put(OmronConstants.OMRONDeviceAlarmSettings.WednesdayKey, OmronConstants.OMRONDeviceAlarmStatus.Off);
                alarmDays1.put(OmronConstants.OMRONDeviceAlarmSettings.ThursdayKey, OmronConstants.OMRONDeviceAlarmStatus.On);
                alarmDays1.put(OmronConstants.OMRONDeviceAlarmSettings.FridayKey, OmronConstants.OMRONDeviceAlarmStatus.Off);
                alarmDays1.put(OmronConstants.OMRONDeviceAlarmSettings.SaturdayKey, OmronConstants.OMRONDeviceAlarmStatus.Off);
                HashMap<String, Object> alarm1 = new HashMap<>();
                alarm1.put(OmronConstants.OMRONDeviceAlarmSettings.DaysKey, alarmDays1);
                alarm1.put(OmronConstants.OMRONDeviceAlarmSettings.TimeKey, alarmTime1);
                alarm1.put(OmronConstants.OMRONDeviceAlarmSettings.TypeKey, OmronConstants.OMRONDeviceAlarmType.Measure);


                // Alarm 2 Time
                HashMap<String, Object> alarmTime2 = new HashMap<String, Object>();
                alarmTime2.put(OmronConstants.OMRONDeviceAlarmSettings.HourKey, "15");
                alarmTime2.put(OmronConstants.OMRONDeviceAlarmSettings.MinuteKey, "34");
                // Alarm 2 Day (SUN-SAT)
                HashMap<String, Object> alarmDays2 = new HashMap<String, Object>();
                alarmDays2.put(OmronConstants.OMRONDeviceAlarmSettings.SundayKey, OmronConstants.OMRONDeviceAlarmStatus.Off);
                alarmDays2.put(OmronConstants.OMRONDeviceAlarmSettings.MondayKey, OmronConstants.OMRONDeviceAlarmStatus.Off);
                alarmDays2.put(OmronConstants.OMRONDeviceAlarmSettings.TuesdayKey, OmronConstants.OMRONDeviceAlarmStatus.Off);
                alarmDays2.put(OmronConstants.OMRONDeviceAlarmSettings.WednesdayKey, OmronConstants.OMRONDeviceAlarmStatus.Off);
                alarmDays2.put(OmronConstants.OMRONDeviceAlarmSettings.ThursdayKey, OmronConstants.OMRONDeviceAlarmStatus.On);
                alarmDays2.put(OmronConstants.OMRONDeviceAlarmSettings.FridayKey, OmronConstants.OMRONDeviceAlarmStatus.Off);
                alarmDays2.put(OmronConstants.OMRONDeviceAlarmSettings.SaturdayKey, OmronConstants.OMRONDeviceAlarmStatus.Off);
                HashMap<String, Object> alarm2 = new HashMap<>();
                alarm2.put(OmronConstants.OMRONDeviceAlarmSettings.DaysKey, alarmDays2);
                alarm2.put(OmronConstants.OMRONDeviceAlarmSettings.TimeKey, alarmTime2);
                alarm2.put(OmronConstants.OMRONDeviceAlarmSettings.TypeKey, OmronConstants.OMRONDeviceAlarmType.Medication);

                // Add Alarm1, Alarm2, Alarm3 to List
                ArrayList<HashMap> alarms = new ArrayList<>();
                alarms.add(alarm1);
                alarms.add(alarm2);
                HashMap<String, Object> alarmSettings = new HashMap<>();
                alarmSettings.put(OmronConstants.OMRONDeviceAlarmSettingsKey, alarms);


                // Notification enable settings
                HashMap<String, Object> notificationEnableSettings = new HashMap<String, Object>();
                notificationEnableSettings.put(OmronConstants.OMRONDeviceNotificationStatusKey, OmronConstants.OMRONDeviceNotificationStatus.On);
                HashMap<String, HashMap> notificationStatusSettings = new HashMap<>();
                notificationStatusSettings.put(OmronConstants.OMRONDeviceNotificationEnableSettingsKey, notificationEnableSettings);


                deviceSettings.add(userSettings);
                deviceSettings.add(notificationSettings);
                deviceSettings.add(alarmSettings);
                deviceSettings.add(timeSettings);
                deviceSettings.add(sleepSettings);
                deviceSettings.add(notificationStatusSettings);
            }
        }

        return deviceSettings;
    }

    private ArrayList<HashMap> getBCMSettings(ArrayList<HashMap> deviceSettings) {

        // body composition
        if (Integer.parseInt(device.get(OmronConstants.OMRONBLEConfigDevice.Category)) == OmronConstants.OMRONBLEDeviceCategory.BODYCOMPOSITION) {

            //Weight settings
            HashMap<String, Object> weightPersonalSettings = new HashMap<>();
            weightPersonalSettings.put(OmronConstants.OMRONDevicePersonalSettings.WeightDCIKey, 100);

            HashMap<String, Object> settings = new HashMap<>();
            settings.put(OmronConstants.OMRONDevicePersonalSettings.UserHeightKey, "17000");
            settings.put(OmronConstants.OMRONDevicePersonalSettings.UserGenderKey, OmronConstants.OMRONDevicePersonalSettingsUserGenderType.Male);
            settings.put(OmronConstants.OMRONDevicePersonalSettings.UserDateOfBirthKey, "19001010");
            settings.put(OmronConstants.OMRONDevicePersonalSettings.WeightKey, weightPersonalSettings);

            HashMap<String, HashMap> personalSettings = new HashMap<>();
            personalSettings.put(OmronConstants.OMRONDevicePersonalSettingsKey, settings);

            // Weight Settings
            // Add other weight common settings if any
            HashMap<String, Object> weightCommonSettings = new HashMap<>();
            weightCommonSettings.put(OmronConstants.OMRONDeviceWeightSettings.UnitKey, OmronConstants.OMRONDeviceWeightUnit.Lbs);
            HashMap<String, Object> weightSettings = new HashMap<>();
            weightSettings.put(OmronConstants.OMRONDeviceWeightSettingsKey, weightCommonSettings);

            deviceSettings.add(personalSettings);
            deviceSettings.add(weightSettings);
        }

        return deviceSettings;
    }

    @Override
    public void onListen(Object arguments, EventChannel.EventSink events) {
        scanningDevicesEvent = events;
    }

    @Override
    public void onCancel(Object arguments) {
        scanningDevicesEvent = null;
    }
}
