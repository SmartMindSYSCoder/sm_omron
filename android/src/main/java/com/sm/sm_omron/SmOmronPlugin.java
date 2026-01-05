package com.sm.sm_omron;

import static com.sm.sm_omron.OmronManager.mSelectedPeripheral;

import androidx.annotation.NonNull;
import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.Interface.OmronPeripheralManagerDataTransferListener;
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.Interface.OmronPeripheralManagerRecordListener;
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.Interface.OmronPeripheralManagerRecordSignalListener;
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.LibraryManager.OmronPeripheralManager;
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.Model.OmronErrorInfo;
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.Model.OmronPeripheral;
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.OmronUtility.OmronConstants;
import com.sm.sm_omron.core.OmronDeviceManager;
import com.sm.sm_omron.core.ParserFactory;
import com.sm.sm_omron.core.TransferResult;
import com.sm.sm_omron.core.VitalDataParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

/**
 * SmOmronPlugin - Flutter plugin for Omron health devices.
 */
public class SmOmronPlugin implements FlutterPlugin, MethodCallHandler, ActivityAware {
    
    private static final String TAG = "SmOmronPlugin";
    
    private MethodChannel channel;
    private EventChannel scanEventChannel;
    private EventChannel statusEventChannel;
    private EventChannel.EventSink statusEventSink;

    private Context applicationContext;
    private Activity activity;
    
    // Helpers
    private DevicesList devicesList;
    private PermissionHelper permissionHelper;
    private StateChangesStatus stateChangesStatus;
    private ScanningDevices scanningDevices;
    private WeightScale weightScale;
    
    // Core manager
    private OmronDeviceManager deviceManager;
    
    private final Gson gson = new Gson();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "sm_omron");
        statusEventChannel = new EventChannel(flutterPluginBinding.getBinaryMessenger(), "sm_omron_status");
        scanEventChannel = new EventChannel(flutterPluginBinding.getBinaryMessenger(), "sm_omron_scan");

        channel.setMethodCallHandler(this);
        this.applicationContext = flutterPluginBinding.getApplicationContext();
        
        // Setup status event channel
        statusEventChannel.setStreamHandler(new EventChannel.StreamHandler() {
            @Override
            public void onListen(Object arguments, EventChannel.EventSink events) {
                statusEventSink = events;
            }

            @Override
            public void onCancel(Object arguments) {
                statusEventSink = null;
            }
        });
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        switch (call.method) {
            case "getPlatformVersion":
                result.success("Android " + android.os.Build.VERSION.RELEASE);
                break;

            case "checkBluetoothPermissions":
                handleCheckBluetoothPermissions(result);
                break;

            case "checkRecordPermissions":
                handleCheckRecordPermissions(result);
                break;

            case "isPermissionsGranted":
                result.success(permissionHelper.isPermissionsGranted());
                break;

            case "getDevicesList":
                handleGetDevicesList(result);
                break;

            case "scan":
                handleScan(call, result);
                break;

            case "connectToDevice":
                handleConnect(call, result);
                break;
                
            case "unpairDevice":
                handleUnpairDevice(call, result);
                break;

            case "autoTransferData":
                handleAutoTransferData(call, result);
                break;

            case "weight":
                handleWeight(call, result);
                break;

            default:
                result.notImplemented();
        }
    }

    // ============================================================
    // METHOD HANDLERS
    // ============================================================

    private void handleCheckBluetoothPermissions(Result result) {
        permissionHelper.checkPermissions();
        result.success(true);
    }

    private void handleCheckRecordPermissions(Result result) {
        permissionHelper.checkRecordAudioPermission();
        result.success(true);
    }

    private void handleGetDevicesList(Result result) {
        if (permissionHelper.isPermissionsGranted()) {
            devicesList.initializeFun(result);
        } else {
            permissionHelper.checkPermissions();
            result.error("PERMISSION_DENIED", "Bluetooth permission not granted", null);
        }
    }

    private void handleScan(MethodCall call, Result result) {
        if (!permissionHelper.isPermissionsGranted()) {
            permissionHelper.checkPermissions();
            result.error("PERMISSION_DENIED", "Bluetooth permission not granted", null);
            return;
        }

        OmronManager.device = (HashMap<String, String>) call.arguments();
        scanningDevices.initializeFun(stateChangesStatus);
        scanningDevices.startOrStopScanning(result);
    }

    private void handleConnect(MethodCall call, Result result) {
        if (!permissionHelper.isPermissionsGranted()) {
            permissionHelper.checkPermissions();
            result.error("PERMISSION_DENIED", "Bluetooth permission not granted", null);
            return;
        }

        HashMap<String, Object> args = (HashMap<String, Object>) call.arguments();
        String localName = (String) args.get("localName");
        String uuid = (String) args.get("uuid");

        if (localName == null || uuid == null) {
            result.error("INVALID_ARGUMENTS", "localName and uuid are required", null);
            return;
        }

        OmronPeripheral peripheral = new OmronPeripheral(localName, uuid);
        // Remove invalid setters. We now handle configuration in ConnectDevice.
        
        // Pass the full arguments map        
        // Use updated ConnectDevice which handles the result
        ConnectDevice connectDevice = new ConnectDevice(applicationContext, peripheral, args, result);
        connectDevice.connectPeripheral();
    }

    private void handleUnpairDevice(MethodCall call, Result result) {
        HashMap<String, Object> args = (HashMap<String, Object>) call.arguments();
        UnpairDevice unpairDevice = new UnpairDevice(applicationContext, args, result);
        unpairDevice.unpair();
    }

    private void handleAutoTransferData(MethodCall call, Result result) {
        HashMap<String, Object> map = (HashMap<String, Object>) call.arguments();

        String uuid = (String) map.get("uuid");
        String localName = (String) map.get("localName");
        int category = (int) map.get("category");
        OmronManager.device = (HashMap<String, String>) map.get("model");

        // Get transfer options
        Map<String, Object> options = (Map<String, Object>) map.get("options");
        boolean readHistoricalData = false;
        List<Integer> userIds = new ArrayList<>();
        userIds.add(1);
        int timeoutSeconds = 30;

        if (options != null) {
            if (options.containsKey("readHistoricalData")) {
                readHistoricalData = (boolean) options.get("readHistoricalData");
            }
            if (options.containsKey("userIds")) {
                userIds = (List<Integer>) options.get("userIds");
            }
            if (options.containsKey("timeoutSeconds")) {
                timeoutSeconds = (int) options.get("timeoutSeconds");
            }
        }

        // Check for temperature device (audio-based)
        if ("MC-280B-E".equals(uuid) || category == OmronConstants.OMRONBLEDeviceCategory.TEMPERATURE) {
            handleTemperatureRecording(result);
            return;
        }

        // BLE device transfer
        if (!permissionHelper.isPermissionsGranted()) {
            result.error("PERMISSION_DENIED", "Bluetooth permission not granted", null);
            return;
        }

        // Update status
        sendStateUpdate(OmronDeviceManager.STATE_SCANNING, "Scanning...");

        // Perform transfer using AutoTransferData
        final int finalCategory = category;
        final boolean finalReadHistoricalData = readHistoricalData;
        
        new AutoTransferData(result, applicationContext) {
            @Override
            protected void onTransferComplete(List<HashMap<String, Object>> vitalDataList) {
                // Parse data using appropriate parser
                try {
                    Log.d(TAG, "DEBUG: Raw vitalDataList from AutoTransferData: " + vitalDataList.toString());

                    VitalDataParser parser = ParserFactory.getParser(finalCategory);
                    List<String> jsonList = new ArrayList<>();

                    for (HashMap<String, Object> reading : vitalDataList) {
                        Log.d(TAG, "DEBUG: Processing reading: " + reading.toString());
                        Map<String, Object> unifiedResult = parser.parseToUnifiedResult(reading);
                        Log.d(TAG, "DEBUG: Parsed Unified Result: " + unifiedResult.toString());
                        jsonList.add(gson.toJson(unifiedResult));
                    }

                    sendStateUpdate(OmronDeviceManager.STATE_IDLE, "Transfer complete");
                    result.success(jsonList);
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing data: " + e.getMessage());
                    e.printStackTrace();
                    result.success(vitalDataList.stream()
                            .map(gson::toJson)
                            .collect(java.util.stream.Collectors.toList()));
                }
            }
        }.initializeFun(localName, uuid, category);
    }

    private void handleTemperatureRecording(Result result) {
        if (!permissionHelper.isRecordAudioPermissionGranted()) {
            permissionHelper.checkRecordAudioPermission();
            result.error("PERMISSION_DENIED", "Microphone permission not granted", null);
            return;
        }

        sendStateUpdate(OmronDeviceManager.STATE_RECORDING, "Recording...");

        OmronPeripheral peripheral = new OmronPeripheral(OmronConstants.OMRONThermometerMC280B, "");
        
        OmronPeripheralManager.sharedManager(applicationContext).startRecording(
                peripheral,
                new OmronPeripheralManagerRecordSignalListener() {
                    @Override
                    public void onSignalStrength(double signalLevel) {
                        // Could send signal strength updates to Flutter if needed
                    }
                },
                new OmronPeripheralManagerRecordListener() {
                    @Override
                    public void onRecord(OmronPeripheral peripheral, OmronErrorInfo errorInfo) {
                        sendStateUpdate(OmronDeviceManager.STATE_IDLE, "Recording complete");

                        if (peripheral != null) {
                            Object output = peripheral.getVitalData();

                            if (output instanceof HashMap) {
                                HashMap<String, Object> vitalData = (HashMap<String, Object>) output;
                                ArrayList<HashMap<String, Object>> temperatureData =
                                        (ArrayList<HashMap<String, Object>>) vitalData.get(
                                                OmronConstants.OMRONVitalDataTemperatureKey);

                                if (temperatureData != null && !temperatureData.isEmpty()) {
                                    VitalDataParser parser = ParserFactory.getParser(
                                            OmronConstants.OMRONBLEDeviceCategory.TEMPERATURE);
                                    
                                    List<String> jsonList = new ArrayList<>();
                                    for (HashMap<String, Object> reading : temperatureData) {
                                        Map<String, Object> unifiedResult = parser.parseToUnifiedResult(reading);
                                        jsonList.add(gson.toJson(unifiedResult));
                                    }

                                    stopRecording();
                                    result.success(jsonList);
                                    return;
                                }
                            }
                        }

                        stopRecording();
                        result.error("NO_DATA", "No temperature data recorded", null);
                    }
                });
    }

    private void stopRecording() {
        OmronPeripheralManager.sharedManager(applicationContext).stopRecording(null);
    }

    private void handleWeight(MethodCall call, Result result) {
        if (!permissionHelper.isPermissionsGranted()) {
            permissionHelper.checkPermissions();
            result.error("PERMISSION_DENIED", "Bluetooth permission not granted", null);
            return;
        }

        OmronManager.device = (HashMap<String, String>) call.arguments();
        weightScale.init(result);
    }

    // ============================================================
    // STATE MANAGEMENT
    // ============================================================

    private void sendStateUpdate(int state, String message) {
        if (statusEventSink != null) {
            mainHandler.post(() -> {
                HashMap<String, Object> stateMap = new HashMap<>();
                stateMap.put("state", state);
                stateMap.put("message", message);
                statusEventSink.success(stateMap);
            });
        }
    }

    // ============================================================
    // LIFECYCLE
    // ============================================================

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
        if (deviceManager != null) {
            deviceManager.cleanup();
        }
    }

    @Override
    public void onAttachedToActivity(ActivityPluginBinding activityPluginBinding) {
        this.activity = activityPluginBinding.getActivity();
        permissionHelper = new PermissionHelper(activity, applicationContext);
        devicesList = new DevicesList(activity, applicationContext);
        scanningDevices = new ScanningDevices(activity, applicationContext);
        scanEventChannel.setStreamHandler(scanningDevices);
        stateChangesStatus = new StateChangesStatus(applicationContext, activity);
        weightScale = new WeightScale(applicationContext, activity);
        statusEventChannel.setStreamHandler(stateChangesStatus);
        
        // Initialize device manager
        deviceManager = OmronDeviceManager.getInstance(applicationContext);
        deviceManager.setStateChangeListener((newState, statusMessage) -> {
            sendStateUpdate(newState, statusMessage);
        });
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {
        // Handle configuration changes
    }

    @Override
    public void onReattachedToActivityForConfigChanges(ActivityPluginBinding activityPluginBinding) {
        this.activity = activityPluginBinding.getActivity();
    }

    @Override
    public void onDetachedFromActivity() {
        if (deviceManager != null) {
            deviceManager.cleanup();
        }
    }
}
