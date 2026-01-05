/// Unified vital result from any Omron device.
///
/// This model normalizes data from all Omron device types into a single
/// structure. Only fields relevant to the device type will be populated.
class VitalResult {
  /// Type of vital data
  final VitalType type;

  /// Measurement timestamp
  final DateTime? measurementDate;

  /// User ID on the device (for multi-user devices)
  final int? userId;

  /// Sequence number for tracking synced readings
  final int? sequenceNumber;

  // === Blood Pressure Fields ===

  /// Systolic blood pressure (mmHg)
  final int? systolic;

  /// Diastolic blood pressure (mmHg)
  final int? diastolic;

  /// Pulse/heart rate (bpm)
  final int? pulse;

  /// Irregular heartbeat detected
  final bool? irregularHeartbeat;

  /// Atrial fibrillation detected
  final bool? atrialFibrillation;

  /// Cuff wrap detection flag
  final bool? cuffWrapDetection;

  /// Movement detection flag
  final bool? movementDetection;

  /// Measurement mode (e.g., morning, evening)
  final int? measurementMode;

  // === Weight/Body Composition Fields ===

  /// Weight (kg)
  final double? weight;

  /// Body Mass Index
  final double? bmi;

  /// Body fat percentage
  final double? bodyFatPercentage;

  /// Skeletal muscle percentage
  final double? skeletalMusclePercentage;

  /// Visceral fat level
  final int? visceralFatLevel;

  /// Basal metabolic rate (kcal)
  final int? basalMetabolicRate;

  /// Body age
  final int? bodyAge;

  // === Pulse Oximeter Fields ===

  /// Blood oxygen saturation level (%)
  final int? spo2Level;

  /// Pulse rate from oximeter (bpm)
  final int? pulseOximeterRate;

  // === Temperature Fields ===

  /// Temperature value
  final double? temperature;

  /// Temperature unit (Celsius or Fahrenheit)
  final TemperatureUnit? temperatureUnit;

  // === Activity Fields ===

  /// Steps count
  final int? steps;

  /// Aerobic steps count
  final int? aerobicSteps;

  /// Distance (meters)
  final double? distance;

  /// Calories burned
  final int? calories;

  // === Wheeze Fields ===

  /// Wheeze detection result
  final WheezeResult? wheezeResult;

  // === Raw Data ===

  /// Original raw data from device for advanced use cases
  final Map<String, dynamic>? rawData;

  const VitalResult({
    required this.type,
    this.measurementDate,
    this.userId,
    this.sequenceNumber,
    // Blood Pressure
    this.systolic,
    this.diastolic,
    this.pulse,
    this.irregularHeartbeat,
    this.atrialFibrillation,
    this.cuffWrapDetection,
    this.movementDetection,
    this.measurementMode,
    // Weight
    this.weight,
    this.bmi,
    this.bodyFatPercentage,
    this.skeletalMusclePercentage,
    this.visceralFatLevel,
    this.basalMetabolicRate,
    this.bodyAge,
    // Pulse Oximeter
    this.spo2Level,
    this.pulseOximeterRate,
    // Temperature
    this.temperature,
    this.temperatureUnit,
    // Activity
    this.steps,
    this.aerobicSteps,
    this.distance,
    this.calories,
    // Wheeze
    this.wheezeResult,
    // Raw
    this.rawData,
  });

  /// Create VitalResult from JSON map (typically from native platform)
  factory VitalResult.fromJson(Map<String, dynamic> json) {
    return VitalResult(
      type: VitalType.values.firstWhere(
        (e) => e.name == json['type'],
        orElse: () => VitalType.unknown,
      ),
      measurementDate: json['measurementDate'] != null
          ? DateTime.fromMillisecondsSinceEpoch(json['measurementDate'] as int)
          : null,
      userId: json['userId'] as int?,
      sequenceNumber: json['sequenceNumber'] as int?,
      // Blood Pressure
      systolic: json['systolic'] as int?,
      diastolic: json['diastolic'] as int?,
      pulse: json['pulse'] as int?,
      irregularHeartbeat: json['irregularHeartbeat'] as bool?,
      atrialFibrillation: json['atrialFibrillation'] as bool?,
      cuffWrapDetection: json['cuffWrapDetection'] as bool?,
      movementDetection: json['movementDetection'] as bool?,
      measurementMode: json['measurementMode'] as int?,
      // Weight
      weight: (json['weight'] as num?)?.toDouble(),
      bmi: (json['bmi'] as num?)?.toDouble(),
      bodyFatPercentage: (json['bodyFatPercentage'] as num?)?.toDouble(),
      skeletalMusclePercentage:
          (json['skeletalMusclePercentage'] as num?)?.toDouble(),
      visceralFatLevel: json['visceralFatLevel'] as int?,
      basalMetabolicRate: json['basalMetabolicRate'] as int?,
      bodyAge: json['bodyAge'] as int?,
      // Pulse Oximeter
      spo2Level: json['spo2Level'] as int?,
      pulseOximeterRate: json['pulseOximeterRate'] as int?,
      // Temperature
      temperature: (json['temperature'] as num?)?.toDouble(),
      temperatureUnit: json['temperatureUnit'] != null
          ? TemperatureUnit.values.firstWhere(
              (e) => e.index == json['temperatureUnit'],
              orElse: () => TemperatureUnit.celsius,
            )
          : null,
      // Activity
      steps: json['steps'] as int?,
      aerobicSteps: json['aerobicSteps'] as int?,
      distance: (json['distance'] as num?)?.toDouble(),
      calories: json['calories'] as int?,
      // Wheeze
      wheezeResult: json['wheezeResult'] != null
          ? WheezeResult.values.firstWhere(
              (e) => e.index == json['wheezeResult'],
              orElse: () => WheezeResult.unknown,
            )
          : null,
      // Raw
      rawData: json['rawData'] as Map<String, dynamic>?,
    );
  }

  /// Convert to JSON map
  Map<String, dynamic> toJson() {
    return {
      'type': type.name,
      'measurementDate': measurementDate?.millisecondsSinceEpoch,
      'userId': userId,
      'sequenceNumber': sequenceNumber,
      // Blood Pressure
      'systolic': systolic,
      'diastolic': diastolic,
      'pulse': pulse,
      'irregularHeartbeat': irregularHeartbeat,
      'atrialFibrillation': atrialFibrillation,
      'cuffWrapDetection': cuffWrapDetection,
      'movementDetection': movementDetection,
      'measurementMode': measurementMode,
      // Weight
      'weight': weight,
      'bmi': bmi,
      'bodyFatPercentage': bodyFatPercentage,
      'skeletalMusclePercentage': skeletalMusclePercentage,
      'visceralFatLevel': visceralFatLevel,
      'basalMetabolicRate': basalMetabolicRate,
      'bodyAge': bodyAge,
      // Pulse Oximeter
      'spo2Level': spo2Level,
      'pulseOximeterRate': pulseOximeterRate,
      // Temperature
      'temperature': temperature,
      'temperatureUnit': temperatureUnit?.index,
      // Activity
      'steps': steps,
      'aerobicSteps': aerobicSteps,
      'distance': distance,
      'calories': calories,
      // Wheeze
      'wheezeResult': wheezeResult?.index,
      // Raw
      'rawData': rawData,
    };
  }

  @override
  String toString() {
    switch (type) {
      case VitalType.bloodPressure:
        return 'VitalResult(BP: $systolic/$diastolic mmHg, Pulse: $pulse bpm)';
      case VitalType.weight:
        return 'VitalResult(Weight: $weight kg, BMI: $bmi)';
      case VitalType.pulseOximeter:
        return 'VitalResult(SpO2: $spo2Level%, Pulse: $pulseOximeterRate bpm)';
      case VitalType.temperature:
        final unit =
            temperatureUnit == TemperatureUnit.fahrenheit ? '°F' : '°C';
        return 'VitalResult(Temp: $temperature$unit)';
      case VitalType.activity:
        return 'VitalResult(Steps: $steps, Calories: $calories)';
      case VitalType.wheeze:
        return 'VitalResult(Wheeze: ${wheezeResult?.name})';
      default:
        return 'VitalResult(type: $type)';
    }
  }
}

/// Type of vital data measurement
enum VitalType {
  bloodPressure,
  weight,
  pulseOximeter,
  temperature,
  activity,
  wheeze,
  unknown,
}

/// Temperature unit
enum TemperatureUnit {
  celsius,
  fahrenheit,
}

/// Wheeze detection result
enum WheezeResult {
  noWheeze,
  wheezeDetected,
  unknown,
}
