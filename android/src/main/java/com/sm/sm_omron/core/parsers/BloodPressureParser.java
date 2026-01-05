package com.sm.sm_omron.core.parsers;

import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.OmronUtility.OmronConstants;
import com.sm.sm_omron.core.VitalDataParser;

import java.util.HashMap;
import java.util.Map;

/**
 * Parser for Blood Pressure device data.
 */
public class BloodPressureParser implements VitalDataParser {

    @Override
    public String getVitalDataKey() {
        return OmronConstants.OMRONVitalDataBloodPressureKey;
    }

    @Override
    public int getDeviceCategory() {
        return OmronConstants.OMRONBLEDeviceCategory.BLOODPRESSURE;
    }

    @Override
    public String getVitalTypeName() {
        return "bloodPressure";
    }

    @Override
    public Map<String, Object> parseToUnifiedResult(HashMap<String, Object> reading) {
        Map<String, Object> result = new HashMap<>();
        
        result.put("type", getVitalTypeName());
        
        // Measurement date
        Object startDate = reading.get(OmronConstants.OMRONVitalData.StartDateKey);
        if (startDate != null) {
            result.put("measurementDate", startDate);
        }
        
        // User ID
        Object userId = reading.get(OmronConstants.OMRONVitalData.UserIdKey);
        if (userId != null) {
            result.put("userId", userId);
        }
        
        // Sequence number
        Object sequence = reading.get(OmronConstants.OMRONVitalData.SequenceKey);
        if (sequence != null) {
            result.put("sequenceNumber", sequence);
        }
        
        // Blood pressure values
        Object systolic = reading.get(OmronConstants.OMRONVitalData.SystolicKey);
        if (systolic != null) {
            result.put("systolic", systolic);
        }
        
        Object diastolic = reading.get(OmronConstants.OMRONVitalData.DiastolicKey);
        if (diastolic != null) {
            result.put("diastolic", diastolic);
        }
        
        Object pulse = reading.get(OmronConstants.OMRONVitalData.PulseKey);
        if (pulse != null) {
            result.put("pulse", pulse);
        }
        
        // Flags
        Object irregularFlag = reading.get(OmronConstants.OMRONVitalData.IrregularFlagKey);
        if (irregularFlag != null) {
            result.put("irregularHeartbeat", irregularFlag.equals(1));
        }
        
        Object afibFlag = reading.get(OmronConstants.OMRONVitalData.AtrialFibrillationDetectionFlagKey);
        if (afibFlag != null) {
            result.put("atrialFibrillation", afibFlag.equals(1));
        }
        
        Object cuffFlag = reading.get(OmronConstants.OMRONVitalData.CuffWrapDetectionFlagKey);
        if (cuffFlag != null) {
            result.put("cuffWrapDetection", cuffFlag.equals(1));
        }
        
        Object movementFlag = reading.get(OmronConstants.OMRONVitalData.MovementFlagKey);
        if (movementFlag != null) {
            result.put("movementDetection", movementFlag.equals(1));
        }
        
        Object measurementMode = reading.get(OmronConstants.OMRONVitalData.MeasurementModeKey);
        if (measurementMode != null) {
            result.put("measurementMode", measurementMode);
        }
        
        // Include raw data for advanced use
        result.put("rawData", reading);
        
        return result;
    }
}
