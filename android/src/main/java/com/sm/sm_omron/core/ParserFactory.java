package com.sm.sm_omron.core;

import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.OmronUtility.OmronConstants;
import com.sm.sm_omron.core.parsers.*;

/**
 * Factory class to get the appropriate VitalDataParser based on device category.
 */
public class ParserFactory {

    /**
     * Get a parser instance for the given device category.
     *
     * @param category The Omron device category constant
     * @return Appropriate VitalDataParser implementation
     * @throws IllegalArgumentException if category is not supported
     */
    public static VitalDataParser getParser(int category) {
        switch (category) {
            case OmronConstants.OMRONBLEDeviceCategory.BLOODPRESSURE:
                return new BloodPressureParser();

            case OmronConstants.OMRONBLEDeviceCategory.BODYCOMPOSITION:
                return new WeightScaleParser();

            case OmronConstants.OMRONBLEDeviceCategory.ACTIVITY:
                return new ActivityParser();

            case OmronConstants.OMRONBLEDeviceCategory.PULSEOXIMETER:
                return new PulseOximeterParser();

            case OmronConstants.OMRONBLEDeviceCategory.TEMPERATURE:
                return new TemperatureParser();

            case OmronConstants.OMRONBLEDeviceCategory.WHEEZE:
                return new WheezeParser();

            default:
                throw new IllegalArgumentException("Unsupported device category: " + category);
        }
    }

    /**
     * Check if a device category is supported.
     */
    public static boolean isSupportedCategory(int category) {
        return category == OmronConstants.OMRONBLEDeviceCategory.BLOODPRESSURE ||
               category == OmronConstants.OMRONBLEDeviceCategory.BODYCOMPOSITION ||
               category == OmronConstants.OMRONBLEDeviceCategory.ACTIVITY ||
               category == OmronConstants.OMRONBLEDeviceCategory.PULSEOXIMETER ||
               category == OmronConstants.OMRONBLEDeviceCategory.TEMPERATURE ||
               category == OmronConstants.OMRONBLEDeviceCategory.WHEEZE;
    }

    /**
     * Check if device uses audio recording (temperature) instead of BLE.
     */
    public static boolean isAudioDevice(int category) {
        return category == OmronConstants.OMRONBLEDeviceCategory.TEMPERATURE;
    }
}
