package com.sm.sm_omron.core.parsers;

import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.OmronUtility.OmronConstants;
import com.sm.sm_omron.core.VitalDataParser;

import java.util.HashMap;
import java.util.Map;

/**
 * Parser for Temperature device data.
 * Note: Temperature devices use audio recording, not BLE.
 */
public class TemperatureParser implements VitalDataParser {

    @Override
    public String getVitalDataKey() {
        return OmronConstants.OMRONVitalDataTemperatureKey;
    }

    @Override
    public int getDeviceCategory() {
        return OmronConstants.OMRONBLEDeviceCategory.TEMPERATURE;
    }

    @Override
    public String getVitalTypeName() {
        return "temperature";
    }

    @Override
    public Map<String, Object> parseToUnifiedResult(HashMap<String, Object> reading) {
        Map<String, Object> result = new HashMap<>();
        
        result.put("type", getVitalTypeName());
        
        // Measurement date
        Object startDate = reading.get(OmronConstants.OMRONTemperatureData.StartDateKey);
        if (startDate != null) {
            result.put("measurementDate", startDate);
        }
        
        // Temperature value
        Object temperature = reading.get(OmronConstants.OMRONTemperatureData.TemperatureKey);
        if (temperature != null) {
            result.put("temperature", temperature);
        }
        
        // Temperature unit (0 = Celsius, 1 = Fahrenheit)
        Object unit = reading.get(OmronConstants.OMRONTemperatureData.TemperatureUnitKey);
        if (unit != null) {
            result.put("temperatureUnit", unit);
        }
        
        // Include raw data
        result.put("rawData", reading);
        
        return result;
    }
}
