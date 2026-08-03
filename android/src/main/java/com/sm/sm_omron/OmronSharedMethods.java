package com.sm.sm_omron;

import static com.sm.sm_omron.OmronManager.personalSettings;
import static com.sm.sm_omron.OmronManager.device;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.content.Context;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.DeviceConfiguration.OmronPeripheralManagerConfig;
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.LibraryManager.OmronPeripheralManager;
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.OmronUtility.OmronConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class OmronSharedMethods {
    static void startOmronPeripheralManager(boolean isHistoricDataRead, boolean isPairing,Context context) {

        OmronPeripheralManagerConfig peripheralConfig = OmronPeripheralManager.sharedManager(context).getConfiguration();
        Log.d("TAG", "Library Identifier : " + peripheralConfig.getLibraryIdentifier());

        // Filter device to scan and connect
        if (device != null) {
            String groupID = device.get(OmronConstants.OMRONBLEConfigDevice.GroupID);
            if (groupID == null) groupID = device.get("deviceGroupIDKey");
            
            String groupIncludedID = device.get(OmronConstants.OMRONBLEConfigDevice.GroupIncludedGroupID);
            if (groupIncludedID == null) groupIncludedID = device.get("deviceGroupIncludedGroupIDKey");
            
            String categoryStr = device.get(OmronConstants.OMRONBLEConfigDevice.Category);
            if (categoryStr == null) categoryStr = device.get("category");
            
            if (groupID != null && groupIncludedID != null) {
                HashMap<String, String> filterDevice = new HashMap<>();
                filterDevice.put(OmronConstants.OMRONBLEConfigDevice.GroupID, groupID);
                filterDevice.put(OmronConstants.OMRONBLEConfigDevice.GroupIncludedGroupID, groupIncludedID);
                if (categoryStr != null) {
                    filterDevice.put(OmronConstants.OMRONBLEConfigDevice.Category, categoryStr);
                }
                
                List<HashMap<String, String>> filterDevices = new ArrayList<>();
                filterDevices.add(filterDevice);
                peripheralConfig.deviceFilters = filterDevices;
                Log.d("OmronSharedMethods", "Clean deviceFilters: " + filterDevice.toString());
            }
        }

        ArrayList<HashMap> deviceSettings = new ArrayList<>();

        // Blood pressure settings (optional)
        deviceSettings = getBloodPressureSettings(deviceSettings, isPairing);

        // Activity device settings (optional)
        deviceSettings = getActivitySettings(deviceSettings);

        // BCM device settings (optional)
        deviceSettings = getBCMSettings(deviceSettings, isPairing);


        deviceSettings = (ArrayList<HashMap>) getScanSettings(deviceSettings, isPairing);

        peripheralConfig.deviceSettings = deviceSettings;

        // Set Scan timeout interval (optional)
        peripheralConfig.timeoutInterval = Constants.CONNECTION_TIMEOUT;
        // Set User Hash Id (mandatory)
        peripheralConfig.userHashId = "user@example.com"; // Set logged in user email

        // Disclaimer: Read definition before usage
        if (getDeviceCategory() != OmronConstants.OMRONBLEDeviceCategory.ACTIVITY) {
            // Reads all data from device.
            peripheralConfig.enableAllDataRead = isHistoricDataRead;
        }

        // Set sequenceNumbersForTransfer to -1 so scale streams all measurements without status 19 sequence disconnect
        HashMap<Integer, Integer> sequenceNumbersForTransfer = new HashMap<>();
        sequenceNumbersForTransfer.put(1, -1);
        sequenceNumbersForTransfer.put(2, -1);
        sequenceNumbersForTransfer.put(3, -1);
        sequenceNumbersForTransfer.put(4, -1);
        peripheralConfig.sequenceNumbersForTransfer = sequenceNumbersForTransfer;
        Log.d("OmronSharedMethods", "Configured sequenceNumbersForTransfer: {1:-1, 2:-1, 3:-1, 4:-1}");

        // Set configuration for OmronPeripheralManager
        OmronPeripheralManager.sharedManager(context).setConfiguration(peripheralConfig);

        // Stop any existing manager to clear stale BLE/GATT state from previous sessions.
        // This simulates what force-stopping the app does (destroying the SDK singleton).
        try {
            OmronPeripheralManager.sharedManager(context).stopManager();
            Log.d("startManager", "Stopped existing OmronPeripheralManager before restart");
            Thread.sleep(1000); // Let BT stack fully release GATT resources
        } catch (Exception e) {
            Log.d("startManager", "No existing manager to stop: " + e.getMessage());
        }

        Log.d("startManager", "Before Call startManager");

        //Initialize the connection process.
        OmronPeripheralManager.sharedManager(context).startManager();
        Log.d("startManager", "After Call startManager");

        // Notification Listener for BLE State Change
        LocalBroadcastManager.getInstance(context).registerReceiver(mMessageReceiver,
                new IntentFilter(OmronConstants.OMRONBLECentralManagerDidUpdateStateNotification));
    }


    private static int getDeviceCategory() {
        if (device == null) return OmronConstants.OMRONBLEDeviceCategory.BODYCOMPOSITION;
        String catStr = device.get(OmronConstants.OMRONBLEConfigDevice.Category);
        if (catStr == null) catStr = device.get("category");
        if (catStr != null) {
            try {
                return Integer.parseInt(catStr);
            } catch (Exception ignored) {}
        }
        return OmronConstants.OMRONBLEDeviceCategory.BODYCOMPOSITION;
    }

    static ArrayList<HashMap> getBloodPressureSettings(ArrayList<HashMap> deviceSettings, boolean isPairing) {

        // Blood Pressure
        if (getDeviceCategory() == OmronConstants.OMRONBLEDeviceCategory.BLOODPRESSURE) {
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

    static ArrayList<HashMap> getActivitySettings(ArrayList<HashMap> deviceSettings) {

        // Activity Tracker
        if (getDeviceCategory() == OmronConstants.OMRONBLEDeviceCategory.ACTIVITY) {

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

    static ArrayList<HashMap> getBCMSettings(ArrayList<HashMap> deviceSettings, boolean isPairing) {

        // body composition
        if (getDeviceCategory() == OmronConstants.OMRONBLEDeviceCategory.BODYCOMPOSITION) {

            //Weight settings
            HashMap<String, Object> weightPersonalSettings = new HashMap<>();
            int userWeightDCI = 100; // Default DCI

            // Use personal settings if available, otherwise fall back to defaults
            String userHeight = "17000"; // Default: 170cm
            int userGender = OmronConstants.OMRONDevicePersonalSettingsUserGenderType.Male;
            String userDOB = "19001010"; // Default DOB

            if (personalSettings != null) {
                if (personalSettings.get("personalWeight") != null) {
                    try {
                        userWeightDCI = Integer.parseInt(personalSettings.get("personalWeight"));
                        Log.d("OmronSharedMethods", "BCM using personalWeight DCI: " + userWeightDCI);
                    } catch (Exception ignored) {}
                }
                if (personalSettings.get("personalHeight") != null) {
                    userHeight = personalSettings.get("personalHeight");
                    Log.d("OmronSharedMethods", "BCM using personalHeight: " + userHeight);
                }
                if (personalSettings.get("gender") != null) {
                    String gender = personalSettings.get("gender");
                    if ("female".equalsIgnoreCase(gender)) {
                        userGender = OmronConstants.OMRONDevicePersonalSettingsUserGenderType.Female;
                    }
                    Log.d("OmronSharedMethods", "BCM using gender: " + gender + " -> " + userGender);
                }
                if (personalSettings.get("birthdayNum") != null) {
                    userDOB = personalSettings.get("birthdayNum");
                    Log.d("OmronSharedMethods", "BCM using birthdayNum: " + userDOB);
                }
            }

            weightPersonalSettings.put(OmronConstants.OMRONDevicePersonalSettings.WeightDCIKey, userWeightDCI);

            HashMap<String, Object> settings = new HashMap<>();
            settings.put(OmronConstants.OMRONDevicePersonalSettings.UserHeightKey, userHeight);
            settings.put(OmronConstants.OMRONDevicePersonalSettings.UserGenderKey, userGender);
            settings.put(OmronConstants.OMRONDevicePersonalSettings.UserDateOfBirthKey, userDOB);
            settings.put(OmronConstants.OMRONDevicePersonalSettings.WeightKey, weightPersonalSettings);

            HashMap<String, HashMap> bcmPersonalSettings = new HashMap<>();
            bcmPersonalSettings.put(OmronConstants.OMRONDevicePersonalSettingsKey, settings);

            // Weight Settings
            // Add other weight common settings if any
            HashMap<String, Object> weightCommonSettings = new HashMap<>();
            weightCommonSettings.put(OmronConstants.OMRONDeviceWeightSettings.UnitKey, OmronConstants.OMRONDeviceWeightUnit.Kg);
            HashMap<String, Object> weightSettings = new HashMap<>();
            weightSettings.put(OmronConstants.OMRONDeviceWeightSettingsKey, weightCommonSettings);

            deviceSettings.add(bcmPersonalSettings);
            deviceSettings.add(weightSettings);
        }

        return deviceSettings;
    }

    private static ArrayList<HashMap> getScanSettings(ArrayList<HashMap> deviceSettings, boolean isPairing) {
        // Check if ScanSettings has already been added
        for (HashMap map : deviceSettings) {
            if (map.containsKey(OmronConstants.OMRONDeviceScanSettingsKey)) {
                return deviceSettings;
            }
        }

        HashMap<String, Object> ScanModeSettings = new HashMap<>();
        HashMap<String, HashMap> ScanSettings = new HashMap<>();
        int mode = isPairing ? OmronConstants.OMRONDeviceScanSettingsMode.Pairing
                             : OmronConstants.OMRONDeviceScanSettingsMode.MismatchSequence;
        ScanModeSettings.put(OmronConstants.OMRONDeviceScanSettings.ModeKey, mode);
        ScanSettings.put(OmronConstants.OMRONDeviceScanSettingsKey, ScanModeSettings);

        deviceSettings.add(ScanSettings);

        return deviceSettings;
    }





    static BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            // Get extra data included in the Intent
            int status = intent.getIntExtra(OmronConstants.OMRONBLEBluetoothStateKey, 0);

            if (status == OmronConstants.OMRONBLEBluetoothState.OMRONBLEBluetoothStateUnknown) {

                Log.d("OMRONBLEBluetooth", "Bluetooth is in unknown state");

            } else if (status == OmronConstants.OMRONBLEBluetoothState.OMRONBLEBluetoothStateOff) {

                Log.d("OMRONBLEBluetooth", "Bluetooth is currently powered off");

            } else if (status == OmronConstants.OMRONBLEBluetoothState.OMRONBLEBluetoothStateOn) {

                Log.d("OMRONBLEBluetooth", "Bluetooth is currently powered on");
            }
        }
    };
}
