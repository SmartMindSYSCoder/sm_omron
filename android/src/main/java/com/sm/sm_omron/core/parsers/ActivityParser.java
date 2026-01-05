package com.sm.sm_omron.core.parsers;

import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.OmronUtility.OmronConstants;
import com.sm.sm_omron.core.VitalDataParser;

import java.util.HashMap;
import java.util.Map;

/**
 * Parser for Activity Tracker device data.
 */
public class ActivityParser implements VitalDataParser {

    @Override
    public String getVitalDataKey() {
        return OmronConstants.OMRONVitalDataActivityKey;
    }

    @Override
    public int getDeviceCategory() {
        return OmronConstants.OMRONBLEDeviceCategory.ACTIVITY;
    }

    @Override
    public String getVitalTypeName() {
        return "activity";
    }

    @Override
    public Map<String, Object> parseToUnifiedResult(HashMap<String, Object> reading) {
        Map<String, Object> result = new HashMap<>();
        
        result.put("type", getVitalTypeName());
        
        // Measurement date
        Object startDate = reading.get(OmronConstants.OMRONActivityData.StartDateKey);
        if (startDate != null) {
            result.put("measurementDate", startDate);
        }
        
        // User ID
        Object userId = reading.get(OmronConstants.OMRONActivityData.UserIdKey);
        if (userId != null) {
            result.put("userId", userId);
        }
        
        // Steps
        Object steps = reading.get(OmronConstants.OMRONActivityData.StepsPerDay);
        if (steps != null) {
            result.put("steps", steps);
        }
        
        // Aerobic steps
        Object aerobicSteps = reading.get(OmronConstants.OMRONActivityData.AerobicStepsPerDay);
        if (aerobicSteps != null) {
            result.put("aerobicSteps", aerobicSteps);
        }
        
        // Distance
        Object distance = reading.get(OmronConstants.OMRONActivityData.DistancePerDay);
        if (distance != null) {
            result.put("distance", distance);
        }
        
        // Calories
        Object calories = reading.get(OmronConstants.OMRONActivityData.WalkingCaloriesPerDay);
        if (calories != null) {
            result.put("calories", calories);
        }
        
        // Include raw data
        result.put("rawData", reading);
        
        return result;
    }
}
