
class OmronData {
  OmronData({
    this.omronVitalDataSystolicKey=0,
    this.omronVitalDataIrregularFlagKey,
    this.omronVitalDataIrregularPulseDetectionFlagKey,
    this.omronVitalDataUserIdKey,
    this.omronVitalDataAtrialFibrillationDetectionFlagKey,
    this.omronVitalDataCuffFlagKey,
    this.omronVitalDataMeasurementStartDateKey,
    this.omronVitalDataDiastolicKey=0,
    this.omronVitalDataPulseKey=0,
    this.omronVitalDataMovementFlagKey,
    this.omronVitalDataMovementDetectionFlagKey,
    this.omronVitalDataSequenceKey,
    this.omronVitalDataArtifactDetectionKey,
    this.omronVitalDataIhbDetectionKey,
    this.omronVitalDataCuffWrapDetectionFlagKey,
    this.omronVitalDataConsecutiveMeasurementKey,
    this.omronVitalDataAtrialFibrillationModeKey,
    this.omronVitalDataMeasurementDateKey,
    this.oMRONWeightDataStartDateKey,this.oMRONPulseOximeterSPO2LevelKey,this.oMRONPulseOximeterPulseRateKey,
    this.omronVitalDataMeasurementModeKey,this.omronWeightKey=0,this.omronTemperatureKey=0,this.omronTemperatureUnitKey
  });

  int omronVitalDataSystolicKey;
  int omronVitalDataDiastolicKey;

  int? omronVitalDataIrregularFlagKey;
  int? omronVitalDataIrregularPulseDetectionFlagKey;
  int? omronVitalDataUserIdKey;
  int? omronVitalDataAtrialFibrillationDetectionFlagKey;
  int? omronVitalDataCuffFlagKey;
  int? omronVitalDataMeasurementStartDateKey;
  int omronVitalDataPulseKey;
  int? omronVitalDataMovementFlagKey;
  int? omronVitalDataMovementDetectionFlagKey;
  int? omronVitalDataSequenceKey;
  int? omronVitalDataArtifactDetectionKey;
  int? omronVitalDataIhbDetectionKey;
  int? omronVitalDataCuffWrapDetectionFlagKey;
  int? omronVitalDataConsecutiveMeasurementKey;
  int? omronVitalDataAtrialFibrillationModeKey,oMRONPulseOximeterSPO2LevelKey,oMRONPulseOximeterPulseRateKey;
  DateTime? omronVitalDataMeasurementDateKey;
  int? omronVitalDataMeasurementModeKey,omronTemperatureUnitKey,oMRONWeightDataStartDateKey;
  double omronWeightKey,omronTemperatureKey;

  factory OmronData.fromJson(Map<String, dynamic> json) => OmronData(
        omronVitalDataSystolicKey: json["OMRONVitalDataSystolicKey"]??0,
        omronVitalDataIrregularFlagKey: json["OMRONVitalDataIrregularFlagKey"],
        omronVitalDataIrregularPulseDetectionFlagKey:
            json["OMRONVitalDataIrregularPulseDetectionFlagKey"],
        omronVitalDataUserIdKey: json["OMRONVitalDataUserIdKey"],
        omronVitalDataAtrialFibrillationDetectionFlagKey:
            json["OMRONVitalDataAtrialFibrillationDetectionFlagKey"],
        omronVitalDataCuffFlagKey: json["OMRONVitalDataCuffFlagKey"],
        // omronVitalDataMeasurementStartDateKey: json["OMRONVitalDataMeasurementStartDateKey"],
        omronVitalDataDiastolicKey: json["OMRONVitalDataDiastolicKey"]??0,
        omronVitalDataPulseKey: json["OMRONVitalDataPulseKey"]??0,
        omronVitalDataMovementFlagKey: json["OMRONVitalDataMovementFlagKey"],
        omronVitalDataMovementDetectionFlagKey:
            json["OMRONVitalDataMovementDetectionFlagKey"],
        omronVitalDataSequenceKey: json["OMRONVitalDataSequenceKey"],
        omronVitalDataArtifactDetectionKey:
            json["OMRONVitalDataArtifactDetectionKey"],
        omronVitalDataIhbDetectionKey: json["OMRONVitalDataIHBDetectionKey"],
        omronVitalDataCuffWrapDetectionFlagKey:
            json["OMRONVitalDataCuffWrapDetectionFlagKey"],
        omronVitalDataConsecutiveMeasurementKey:
            json["OMRONVitalDataConsecutiveMeasurementKey"],
        omronVitalDataAtrialFibrillationModeKey:
            json["OMRONVitalDataAtrialFibrillationModeKey"],
        // omronVitalDataMeasurementDateKey: DateTime.parse(  handelDate( json["OMRONVitalDataMeasurementDateKey"])),
        omronVitalDataMeasurementModeKey: json["OMRONVitalDataMeasurementModeKey"],
        omronWeightKey: json["OMRONWeightKey"] ??0,
    // oMRONWeightDataStartDateKey: json["OMRONWeightDataStartDateKey"],
        omronTemperatureKey: json["OMRONTemperatureKey"]??0,
        omronTemperatureUnitKey: json["OMRONTemperatureUnitKey"] ??0,
    oMRONPulseOximeterSPO2LevelKey: json["OMRONPulseOximeterSPO2LevelKey"] ?? 0,
    oMRONPulseOximeterPulseRateKey: json["OMRONPulseOximeterPulseRateKey"] ??0,
      );

  Map<String, dynamic> toJson() => {
        "OMRONVitalDataSystolicKey": omronVitalDataSystolicKey,
        "OMRONVitalDataIrregularFlagKey": omronVitalDataIrregularFlagKey,
        "OMRONVitalDataIrregularPulseDetectionFlagKey":
            omronVitalDataIrregularPulseDetectionFlagKey,
        "OMRONVitalDataUserIdKey": omronVitalDataUserIdKey,
        "OMRONVitalDataAtrialFibrillationDetectionFlagKey":
            omronVitalDataAtrialFibrillationDetectionFlagKey,
        "OMRONVitalDataCuffFlagKey": omronVitalDataCuffFlagKey,
        // "OMRONVitalDataMeasurementStartDateKey":
        //     omronVitalDataMeasurementStartDateKey,
        "OMRONVitalDataDiastolicKey": omronVitalDataDiastolicKey,
        "OMRONVitalDataPulseKey": omronVitalDataPulseKey,
        "OMRONVitalDataMovementFlagKey": omronVitalDataMovementFlagKey,
        "OMRONVitalDataMovementDetectionFlagKey":
            omronVitalDataMovementDetectionFlagKey,
        "OMRONVitalDataSequenceKey": omronVitalDataSequenceKey,
        "OMRONVitalDataArtifactDetectionKey":
            omronVitalDataArtifactDetectionKey,
        "OMRONVitalDataIHBDetectionKey": omronVitalDataIhbDetectionKey,
        "OMRONVitalDataCuffWrapDetectionFlagKey":
            omronVitalDataCuffWrapDetectionFlagKey,
        "OMRONVitalDataConsecutiveMeasurementKey":
            omronVitalDataConsecutiveMeasurementKey,
        "OMRONVitalDataAtrialFibrillationModeKey":
            omronVitalDataAtrialFibrillationModeKey,
        // "OMRONVitalDataMeasurementDateKey":
        //     omronVitalDataMeasurementDateKey?.toIso8601String(),
        "OMRONVitalDataMeasurementModeKey": omronVitalDataMeasurementModeKey,
        "OMRONWeightKey": omronWeightKey,
        // "OMRONWeightDataStartDateKey": oMRONWeightDataStartDateKey,
        "OMRONTemperatureKey": omronTemperatureKey,
        "OMRONTemperatureUnitKey": omronTemperatureUnitKey ?? 0,
      };


static String handelDate(dynamic d){


    if(d is String && d.isNotEmpty){


      if(_arabic.contains(d.toString()[1])){

        return _replaceArabicNumberToEnglish(d.toString());

      }
      else {
        return d.toString();
      }



    }
    else{
      return '2023-01-01 00:00:00';
    }
  }


  static const _english = ['0', '1', '2', '3', '4', '5', '6', '7', '8', '9'];
  static const _arabic = ['٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩'];

 static String _replaceArabicNumberToEnglish(String input) {

    for (int i = 0; i < _english.length; i++) {
      input = input.replaceAll(_arabic[i], _english[i]);
    }

    return input;
  }

}
