package com.sm.sm_omron.core;

import java.util.HashMap;
import java.util.Map;

/**
 * Interface for device-specific data parsing.
 * Each device category implements this to convert raw SDK data
 * to a unified format for Flutter.
 */
public interface VitalDataParser {

    /**
     * Get the vital data key used by the SDK for this device type.
     * E.g., OmronConstants.OMRONVitalDataBloodPressureKey
     */
    String getVitalDataKey();

    /**
     * Get the device category value.
     */
    int getDeviceCategory();

    /**
     * Parse raw SDK data to unified result format for Flutter.
     *
     * @param reading Raw reading from SDK
     * @return Unified map with standardized keys
     */
    Map<String, Object> parseToUnifiedResult(HashMap<String, Object> reading);

    /**
     * Get the vital type name for Flutter (matches VitalType enum).
     */
    String getVitalTypeName();
}
