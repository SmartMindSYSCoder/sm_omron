package com.sm.sm_omron.core;

import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.Model.OmronPeripheral;
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.OmronUtility.OmronConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Wrapper class for transfer results containing parsed vital data,
 * device settings, and device information.
 */
public class TransferResult {

    private final OmronPeripheral peripheral;
    private final HashMap<String, Object> vitalData;
    private final HashMap<String, String> deviceInfo;
    private final ArrayList<Map> deviceSettings;

    public TransferResult(OmronPeripheral peripheral) {
        this.peripheral = peripheral;
        this.vitalData = (HashMap<String, Object>) peripheral.getVitalData();
        this.deviceInfo = (HashMap<String, String>) peripheral.getDeviceInformation();
        this.deviceSettings = new ArrayList<>(peripheral.getDeviceSettings());
    }

    public OmronPeripheral getPeripheral() {
        return peripheral;
    }

    public HashMap<String, Object> getVitalData() {
        return vitalData;
    }

    public HashMap<String, String> getDeviceInfo() {
        return deviceInfo;
    }

    public ArrayList<Map> getDeviceSettings() {
        return deviceSettings;
    }

    /**
     * Get vital data list for a specific data key.
     */
    @SuppressWarnings("unchecked")
    public ArrayList<HashMap<String, Object>> getVitalDataList(String vitalDataKey) {
        if (vitalData != null && vitalData.containsKey(vitalDataKey)) {
            return (ArrayList<HashMap<String, Object>>) vitalData.get(vitalDataKey);
        }
        return new ArrayList<>();
    }

    /**
     * Get battery remaining percentage.
     */
    public String getBatteryRemaining() {
        if (deviceInfo != null) {
            return deviceInfo.get(OmronConstants.OMRONDeviceInformation.BatteryRemainingKey);
        }
        return null;
    }

    /**
     * Get device local name.
     */
    public String getLocalName() {
        if (deviceInfo != null) {
            return deviceInfo.get(OmronConstants.OMRONDeviceInformation.LocalNameKey);
        }
        return peripheral != null ? peripheral.getLocalName() : null;
    }

    /**
     * Check if transfer was successful with data.
     */
    public boolean hasData() {
        return vitalData != null && !vitalData.isEmpty();
    }

    /**
     * Get the most recent reading from a vital data list.
     */
    @SuppressWarnings("unchecked")
    public HashMap<String, Object> getLatestReading(String vitalDataKey) {
        ArrayList<HashMap<String, Object>> dataList = getVitalDataList(vitalDataKey);
        if (dataList != null && !dataList.isEmpty()) {
            return dataList.get(dataList.size() - 1);
        }
        return null;
    }
}
