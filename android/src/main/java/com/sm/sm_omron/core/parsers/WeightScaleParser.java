package com.sm.sm_omron.core.parsers;

import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.OmronUtility.OmronConstants;
import com.sm.sm_omron.core.VitalDataParser;

import java.util.HashMap;
import java.util.Map;

/**
 * Parser for Weight Scale / Body Composition device data.
 */
public class WeightScaleParser implements VitalDataParser {

    @Override
    public String getVitalDataKey() {
        return OmronConstants.OMRONVitalDataWeightKey;
    }

    @Override
    public int getDeviceCategory() {
        return OmronConstants.OMRONBLEDeviceCategory.BODYCOMPOSITION;
    }

    @Override
    public String getVitalTypeName() {
        return "weight";
    }

    @Override
    public Map<String, Object> parseToUnifiedResult(HashMap<String, Object> reading) {
        Map<String, Object> result = new HashMap<>();
        
        result.put("type", getVitalTypeName());
        
        // Measurement date
        Object startDate = reading.get(OmronConstants.OMRONWeightData.StartDateKey);
        if (startDate != null) {
            result.put("measurementDate", startDate);
        }
        
        // User ID
        Object userId = reading.get(OmronConstants.OMRONWeightData.UserIdKey);
        if (userId != null) {
            result.put("userId", userId);
        }
        
        // Sequence number
        Object sequence = reading.get(OmronConstants.OMRONWeightData.SequenceKey);
        if (sequence != null) {
            result.put("sequenceNumber", sequence);
        }
        
        // Weight
        Object weight = reading.get(OmronConstants.OMRONWeightData.WeightKey);
        if (weight != null) {
            result.put("weight", weight);
        }
        
        // Body composition data
        Object bmi = reading.get(OmronConstants.OMRONWeightData.BMIKey);
        if (bmi != null) {
            result.put("bmi", bmi);
        }
        
        Object bodyFat = reading.get(OmronConstants.OMRONWeightData.BodyFatPercentageKey);
        if (bodyFat != null) {
            result.put("bodyFatPercentage", bodyFat);
        }
        
        Object skeletalMuscle = reading.get(OmronConstants.OMRONWeightData.SkeletalMusclePercentageKey);
        if (skeletalMuscle != null) {
            result.put("skeletalMusclePercentage", skeletalMuscle);
        }
        
        // Object visceralFat = reading.get(OmronConstants.OMRONWeightData.VisceralFatLevelKey);
        // if (visceralFat != null) {
        //     result.put("visceralFatLevel", visceralFat);
        // }
        // 
        // Object basalMetabolism = reading.get(OmronConstants.OMRONWeightData.BasalMetabolismKey);
        // if (basalMetabolism != null) {
        //     result.put("basalMetabolicRate", basalMetabolism);
        // }
        
        Object bodyAge = reading.get(OmronConstants.OMRONWeightData.BodyAgeKey);
        if (bodyAge != null) {
            result.put("bodyAge", bodyAge);
        }
        
        // Include raw data
        result.put("rawData", reading);
        
        return result;
    }
}
