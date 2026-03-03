package com.sm.sm_omron.core.parsers;

import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.OmronUtility.OmronConstants;
import com.sm.sm_omron.core.VitalDataParser;

import java.util.HashMap;
import java.util.Map;

/**
 * Parser for Pulse Oximeter device data.
 */
public class PulseOximeterParser implements VitalDataParser {

    @Override
    public String getVitalDataKey() {
        return OmronConstants.OMRONVitalDataPulseOximeterKey;
    }

    @Override
    public int getDeviceCategory() {
        return OmronConstants.OMRONBLEDeviceCategory.PULSEOXIMETER;
    }

    @Override
    public String getVitalTypeName() {
        return "pulseOximeter";
    }

    @Override
    public Map<String, Object> parseToUnifiedResult(HashMap<String, Object> reading) {
        Map<String, Object> result = new HashMap<>();
        
        result.put("type", getVitalTypeName());
        
        // Measurement date
        Object startDate = reading.get("OMRONPulseOximeterDataStartDateKey");
        if (startDate == null) {
            startDate = reading.get(OmronConstants.OMRONPulseOximeterData.StartDateKey);
        }
        if (startDate != null) {
            result.put("measurementDate", startDate);
        }
        
        // User ID
        Object userId = reading.get("OMRONPulseOximeterDataUserIdKey");
        if (userId == null) {
            userId = reading.get(OmronConstants.OMRONPulseOximeterData.UserIdKey);
        }
        if (userId != null) {
            result.put("userId", userId);
        }

        // Sequence number
        Object sequence = reading.get("OMRONPulseOximeterDataSequenceKey");
        if (sequence != null) {
            result.put("sequenceNumber", sequence);
        }
        
        // SpO2 Level
        Object spo2 = reading.get("OMRONPulseOximeterSPO2LevelKey");
        if (spo2 != null) {
            result.put("spo2Level", spo2);
        }
        
        // Pulse Rate
        Object pulseRate = reading.get("OMRONPulseOximeterPulseRateKey");
        if (pulseRate != null) {
            result.put("pulseOximeterRate", pulseRate);
        }
        
        // Include raw data
        result.put("rawData", reading);
        
        return result;
    }
}
