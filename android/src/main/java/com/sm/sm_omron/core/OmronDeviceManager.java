package com.sm.sm_omron.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.util.Log;

import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.DeviceConfiguration.OmronPeripheralManagerConfig;
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.Interface.OmronPeripheralManagerConnectStateListener;
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.Interface.OmronPeripheralManagerDataTransferListener;
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.Interface.OmronPeripheralManagerDisconnectListener;
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.Interface.OmronPeripheralManagerStopScanListener;
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.LibraryManager.OmronPeripheralManager;
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.Model.OmronErrorInfo;
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.Model.OmronPeripheral;
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.OmronUtility.OmronConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Centralized manager for all Omron device interactions.
 * Wraps OmronPeripheralManager with a simplified callback-based API.
 */
public class OmronDeviceManager {

    private static final String TAG = "OmronDeviceManager";
    private static volatile OmronDeviceManager instance;

    private final Context context;
    private BroadcastReceiver bluetoothStateReceiver;
    private boolean isReceiverRegistered = false;

    // Connection state constants (matching Flutter ConnectionState enum)
    public static final int STATE_IDLE = 0;
    public static final int STATE_SCANNING = 1;
    public static final int STATE_CONNECTING = 2;
    public static final int STATE_CONNECTED = 3;
    public static final int STATE_TRANSFERRING = 4;
    public static final int STATE_RECORDING = 5;
    public static final int STATE_DISCONNECTING = 6;
    public static final int STATE_DISCONNECTED = 7;
    public static final int STATE_ERROR = 8;

    private int currentState = STATE_IDLE;
    private OmronPeripheral currentPeripheral;

    // Listeners
    private StateChangeListener stateChangeListener;
    private BluetoothStateListener bluetoothStateListener;

    // ============================================================
    // CALLBACK INTERFACES
    // ============================================================

    public interface StateChangeListener {
        void onStateChanged(int newState, String statusMessage);
    }

    public interface TransferListener {
        void onTransferComplete(OmronPeripheral peripheral, TransferResult result);
        void onTransferError(OmronErrorInfo error);
    }

    public interface BluetoothStateListener {
        void onBluetoothStateChanged(int state);
    }

    // ============================================================
    // SINGLETON
    // ============================================================

    private OmronDeviceManager(Context context) {
        this.context = context.getApplicationContext();
        initBluetoothStateReceiver();
    }

    public static OmronDeviceManager getInstance(Context context) {
        if (instance == null) {
            synchronized (OmronDeviceManager.class) {
                if (instance == null) {
                    instance = new OmronDeviceManager(context);
                }
            }
        }
        return instance;
    }

    // ============================================================
    // CONFIGURATION
    // ============================================================

    /**
     * Initialize and configure the Omron Peripheral Manager.
     */
    public void configure(Map<String, String> deviceFilter,
                          List<HashMap> deviceSettings,
                          boolean enableAllDataRead,
                          int timeoutSeconds) {

        OmronPeripheralManagerConfig config = OmronPeripheralManager
                .sharedManager(context)
                .getConfiguration();

        Log.d(TAG, "Library Identifier: " + config.getLibraryIdentifier());

        // Set device filter if provided
        if (deviceFilter != null &&
            deviceFilter.get(OmronConstants.OMRONBLEConfigDevice.GroupID) != null &&
            deviceFilter.get(OmronConstants.OMRONBLEConfigDevice.GroupIncludedGroupID) != null) {

            List<HashMap<String, String>> filterDevices = new ArrayList<>();
            filterDevices.add((HashMap<String, String>) deviceFilter);
            config.deviceFilters = filterDevices;
        }

        // Set device settings
        if (deviceSettings != null) {
            config.deviceSettings = new ArrayList<>(deviceSettings);
        }

        // Set timeout and configuration
        config.timeoutInterval = timeoutSeconds;
        config.userHashId = "<user_hash>";
        config.enableAllDataRead = enableAllDataRead;

        // Apply configuration
        OmronPeripheralManager.sharedManager(context).setConfiguration(config);

        // Start the manager
        OmronPeripheralManager.sharedManager(context).startManager();

        // Register bluetooth state receiver
        registerBluetoothReceiver();
    }

    /**
     * Configure for specific device category.
     */
    public void configureForCategory(int category, boolean enableAllDataRead, int timeoutSeconds) {
        // Category-specific configuration
        boolean sequenceMismatchMode = category != OmronConstants.OMRONBLEDeviceCategory.ACTIVITY &&
                                       category != OmronConstants.OMRONBLEDeviceCategory.PULSEOXIMETER;

        OmronPeripheralManagerConfig config = OmronPeripheralManager
                .sharedManager(context)
                .getConfiguration();

        config.timeoutInterval = timeoutSeconds;
        config.enableAllDataRead = enableAllDataRead;

        OmronPeripheralManager.sharedManager(context).setConfiguration(config);
        OmronPeripheralManager.sharedManager(context).startManager();

        registerBluetoothReceiver();
    }

    // ============================================================
    // DATA TRANSFER
    // ============================================================

    /**
     * Start data transfer from a paired peripheral.
     */
    public void transferData(OmronPeripheral peripheral,
                             List<Integer> selectedUsers,
                             boolean waitForPairing,
                             final TransferListener listener) {

        currentPeripheral = peripheral;
        updateState(STATE_SCANNING, "Scanning...");

        // Set up state change listener
        setupStateChangeListener();

        OmronPeripheralManager.sharedManager(context).startDataTransferFromPeripheral(
                peripheral,
                selectedUsers,
                waitForPairing,
                new OmronPeripheralManagerDataTransferListener() {
                    @Override
                    public void onDataTransferCompleted(OmronPeripheral peripheral, OmronErrorInfo resultInfo) {
                        if (resultInfo.isSuccess() && peripheral != null) {
                            currentPeripheral = peripheral;
                            updateState(STATE_TRANSFERRING, "Transferring data...");

                            // End transfer and get final data
                            endTransfer(peripheral, listener);
                        } else {
                            updateState(STATE_ERROR, "Transfer failed");
                            if (listener != null) {
                                listener.onTransferError(resultInfo);
                            }
                        }
                    }
                });
    }

    private void endTransfer(OmronPeripheral peripheral, final TransferListener listener) {
        OmronPeripheralManager.sharedManager(context).endDataTransferFromPeripheral(
                new OmronPeripheralManagerDataTransferListener() {
                    @Override
                    public void onDataTransferCompleted(OmronPeripheral finalPeripheral, OmronErrorInfo endResultInfo) {
                        updateState(STATE_IDLE, "Transfer complete");

                        if (endResultInfo.isSuccess() && finalPeripheral != null) {
                            TransferResult result = new TransferResult(finalPeripheral);
                            if (listener != null) {
                                listener.onTransferComplete(finalPeripheral, result);
                            }
                        } else {
                            if (listener != null) {
                                listener.onTransferError(endResultInfo);
                            }
                        }
                    }
                });
    }

    // ============================================================
    // DISCONNECT
    // ============================================================

    /**
     * Disconnect from current peripheral or stop scanning.
     */
    public void disconnect() {
        if (currentState == STATE_SCANNING) {
            OmronPeripheralManager.sharedManager(context).stopScanPeripherals(
                    new OmronPeripheralManagerStopScanListener() {
                        @Override
                        public void onStopScanCompleted(OmronErrorInfo resultInfo) {
                            updateState(STATE_IDLE, "Scan stopped");
                        }
                    });
        } else if (currentState == STATE_CONNECTING || 
                   currentState == STATE_CONNECTED ||
                   currentState == STATE_TRANSFERRING) {
            if (currentPeripheral != null) {
                OmronPeripheralManager.sharedManager(context).disconnectPeripheral(
                        currentPeripheral,
                        new OmronPeripheralManagerDisconnectListener() {
                            @Override
                            public void onDisconnectCompleted(OmronPeripheral peripheral, OmronErrorInfo resultInfo) {
                                updateState(STATE_DISCONNECTED, "Disconnected");
                            }
                        });
            }
        }
        currentState = STATE_IDLE;
    }

    // ============================================================
    // STATE MANAGEMENT
    // ============================================================

    public void setStateChangeListener(StateChangeListener listener) {
        this.stateChangeListener = listener;
    }

    public void setBluetoothStateListener(BluetoothStateListener listener) {
        this.bluetoothStateListener = listener;
    }

    public int getCurrentState() {
        return currentState;
    }

    public OmronPeripheral getCurrentPeripheral() {
        return currentPeripheral;
    }

    private void updateState(int newState, String statusMessage) {
        currentState = newState;
        Log.d(TAG, "State changed to: " + newState + " - " + statusMessage);
        if (stateChangeListener != null) {
            stateChangeListener.onStateChanged(newState, statusMessage);
        }
    }

    private void setupStateChangeListener() {
        OmronPeripheralManager.sharedManager(context).onConnectStateChange(
                new OmronPeripheralManagerConnectStateListener() {
                    @Override
                    public void onConnectStateChange(int state) {
                        String statusMessage = "-";
                        int newState = currentState;

                        switch (state) {
                            case OmronConstants.OMRONBLEConnectionState.CONNECTING:
                                newState = STATE_CONNECTING;
                                statusMessage = "Connecting...";
                                break;
                            case OmronConstants.OMRONBLEConnectionState.CONNECTED:
                                newState = STATE_CONNECTED;
                                statusMessage = "Connected";
                                break;
                            case OmronConstants.OMRONBLEConnectionState.DISCONNECTING:
                                newState = STATE_DISCONNECTING;
                                statusMessage = "Disconnecting...";
                                break;
                            case OmronConstants.OMRONBLEConnectionState.DISCONNECTED:
                                newState = STATE_DISCONNECTED;
                                statusMessage = "Disconnected";
                                break;
                        }

                        updateState(newState, statusMessage);
                    }
                });
    }

    // ============================================================
    // BLUETOOTH STATE
    // ============================================================

    private void initBluetoothStateReceiver() {
        bluetoothStateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int status = intent.getIntExtra(OmronConstants.OMRONBLEBluetoothStateKey, 0);

                if (bluetoothStateListener != null) {
                    bluetoothStateListener.onBluetoothStateChanged(status);
                }
            }
        };
    }

    private void registerBluetoothReceiver() {
        if (!isReceiverRegistered) {
            IntentFilter filter = new IntentFilter(OmronConstants.OMRONBLEBluetoothStateNotification);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(bluetoothStateReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
            } else {
                context.registerReceiver(bluetoothStateReceiver, filter);
            }
            isReceiverRegistered = true;
        }
    }

    private void unregisterBluetoothReceiver() {
        if (isReceiverRegistered && bluetoothStateReceiver != null) {
            try {
                context.unregisterReceiver(bluetoothStateReceiver);
            } catch (Exception e) {
                Log.e(TAG, "Error unregistering receiver: " + e.getMessage());
            }
            isReceiverRegistered = false;
        }
    }

    // ============================================================
    // CLEANUP
    // ============================================================

    public void cleanup() {
        unregisterBluetoothReceiver();
        disconnect();
    }

    // ============================================================
    // HELPER METHODS
    // ============================================================

    /**
     * Build default scan settings.
     */
    public static HashMap<String, HashMap> buildScanSettings() {
        HashMap<String, Object> scanModeSettings = new HashMap<>();
        scanModeSettings.put(OmronConstants.OMRONDeviceScanSettings.ModeKey,
                OmronConstants.OMRONDeviceScanSettingsMode.MismatchSequence);

        HashMap<String, HashMap> scanSettings = new HashMap<>();
        scanSettings.put(OmronConstants.OMRONDeviceScanSettingsKey, scanModeSettings);

        return scanSettings;
    }
}
