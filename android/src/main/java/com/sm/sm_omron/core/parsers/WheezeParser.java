package com.sm.sm_omron.core.parsers;

import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.OmronUtility.OmronConstants;
import com.sm.sm_omron.core.VitalDataParser;

import java.util.HashMap;
import java.util.Map;

/**
 * Parser for Wheeze Detector device data.
 */
public class WheezeParser implements VitalDataParser {

    @Override
    public String getVitalDataKey() {
        return OmronConstants.OMRONVitalDataWheezeKey;
    }

    @Override
    public int getDeviceCategory() {
        return OmronConstants.OMRONBLEDeviceCategory.WHEEZE;
    }

    @Override
    public String getVitalTypeName() {
        return "wheeze";
    }

    @Override
    public Map<String, Object> parseToUnifiedResult(HashMap<String, Object> reading) {
        Map<String, Object> result = new HashMap<>();
        
        result.put("type", getVitalTypeName());
        
        // Measurement date
        Object startDate = reading.get(OmronConstants.OMRONWheezeData.StartDateKey);
        if (startDate != null) {
            result.put("measurementDate", startDate);
        }
        
        // Wheeze detection result
        Object wheezeDetected = reading.get(OmronConstants.OMRONWheezeData.WheezeKey);
        if (wheezeDetected != null) {
            // 0 = no wheeze, 1 = wheeze detected
            int wheezeValue = wheezeDetected.equals(1) ? 1 : 0;
            result.put("wheezeResult", wheezeValue);
        }
        
        // Sequence
        Object sequence = reading.get(OmronConstants.OMRONWheezeData.SequenceKey);
        if (sequence != null) {
            result.put("sequenceNumber", sequence);
        }
        
        // Include raw data
        result.put("rawData", reading);
        
        return result;
    }
}
