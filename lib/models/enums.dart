/// Device category for Omron devices.
///
/// Maps to OmronConstants.OMRONBLEDeviceCategory values on native side.
enum DeviceCategory {
  /// Blood Pressure Monitor (BLE)
  /// Blood Pressure Monitor (BLE)
  bloodPressure(0),

  /// Body Composition / Weight Scale (BLE)
  weight(1),

  /// Activity Tracker (BLE)
  activity(2),

  /// Pulse Oximeter (BLE)
  pulseOximeter(6),

  /// Temperature (Audio/Microphone - not BLE)
  temperature(19),

  /// Wheeze Detector (BLE)
  wheeze(14);

  const DeviceCategory(this.value);

  /// Native SDK category value
  final int value;

  /// Whether this device uses Bluetooth LE
  bool get isBleDevice => this != temperature;

  /// Whether this device uses audio recording
  bool get isAudioDevice => this == temperature;

  /// Create from native SDK category value
  static DeviceCategory fromValue(int value) {
    return DeviceCategory.values.firstWhere(
      (e) => e.value == value,
      orElse: () => bloodPressure,
    );
  }

  /// Human-readable display name
  String get displayName {
    switch (this) {
      case DeviceCategory.bloodPressure:
        return 'Blood Pressure';
      case DeviceCategory.weight:
        return 'Weight Scale';
      case DeviceCategory.activity:
        return 'Activity Tracker';
      case DeviceCategory.pulseOximeter:
        return 'Pulse Oximeter';
      case DeviceCategory.temperature:
        return 'Thermometer';
      case DeviceCategory.wheeze:
        return 'Wheeze Detector';
    }
  }

  /// Asset path for the category thumbnail
  String get imagePath {
    switch (this) {
      case DeviceCategory.bloodPressure:
        return 'assets/images/bp_monitor.png';
      case DeviceCategory.weight:
        return 'assets/images/weight_scale.png';
      case DeviceCategory.activity:
        return 'assets/images/activity_tracker.png';
      case DeviceCategory.pulseOximeter:
        return 'assets/images/pulse_oximeter.png';
      case DeviceCategory.temperature:
        return 'assets/images/thermometer.png';
      case DeviceCategory.wheeze:
        return 'assets/images/wheeze_monitor.png';
    }
  }
}

/// Connection state for device communication.
enum OmronConnectionState {
  /// No active connection or operation
  idle,

  /// Scanning for devices
  scanning,

  /// Connecting to device
  connecting,

  /// Connected and ready
  connected,

  /// Transferring data
  transferring,

  /// Recording audio (temperature device)
  recording,

  /// Disconnecting from device
  disconnecting,

  /// Disconnected
  disconnected,

  /// Error occurred
  error;

  /// Whether currently in an active operation
  bool get isActive =>
      this == scanning ||
      this == connecting ||
      this == connected ||
      this == transferring ||
      this == recording;

  /// Human-readable status message
  String get statusMessage {
    switch (this) {
      case OmronConnectionState.idle:
        return 'Ready';
      case OmronConnectionState.scanning:
        return 'Scanning...';
      case OmronConnectionState.connecting:
        return 'Connecting...';
      case OmronConnectionState.connected:
        return 'Connected';
      case OmronConnectionState.transferring:
        return 'Transferring data...';
      case OmronConnectionState.recording:
        return 'Recording...';
      case OmronConnectionState.disconnecting:
        return 'Disconnecting...';
      case OmronConnectionState.disconnected:
        return 'Disconnected';
      case OmronConnectionState.error:
        return 'Error';
    }
  }
}

/// Device Identifiers
enum DeviceIdentifier {
  /// Blood Pressure - HPO-300T
  HPO_300T('HPO-300T'),

  /// Blood Pressure - HEM-7280T_TI-D/E
  HEM_7280T_TI_D_E('HEM-7280T_TI-D/E'),

  /// Blood Pressure - HEM-7280T-E
  HEM_7280T_E('HEM-7280T-E'),

  /// Blood Pressure - HEM-7322T-D
  HEM_7322T_D('HEM-7322T-D'),

  /// Blood Pressure - HEM-7322T-E
  HEM_7322T_E('HEM-7322T-E'),

  /// Blood Pressure - HEM-7600T-E
  HEM_7600T_E('HEM-7600T-E'),

  /// Blood Pressure - HEM-6161T-D/E
  HEM_6161T_D_E('HEM-6161T-D/E'),

  /// Blood Pressure - HEM-6232T-D/E
  HEM_6232T_D_E('HEM-6232T-D/E'),

  /// Weight Scale - HBF-222T_E
  HBF_222T_E('HBF-222T_E'),

  /// Blood Pressure - HEM-7155T-EBK
  HEM_7155T_EBK('HEM-7155T-EBK'),

  /// Blood Pressure - HEM-7155T_ESL
  HEM_7155T_ESL('HEM-7155T_ESL'),

  /// Blood Pressure - HEM-7155T-D
  HEM_7155T_D('HEM-7155T-D'),

  /// Blood Pressure - HEM-7361T-EBK
  HEM_7361T_EBK('HEM-7361T-EBK'),

  /// Blood Pressure - HEM-7361T_ESL
  HEM_7361T_ESL('HEM-7361T_ESL'),

  /// Blood Pressure - HEM-7361T-D
  HEM_7361T_D('HEM-7361T-D'),

  /// Blood Pressure - HEM-9601T_E3
  HEM_9601T_E3('HEM-9601T_E3'),

  /// Blood Pressure - HEM-7530T-E3
  HEM_7530T_E3('HEM-7530T-E3'),

  /// Wheeze - HWZ-1000T-E
  HWZ_1000T_E('HWZ-1000T-E'),

  /// Temperature - MC-280B-E
  MC_280B_E('MC-280B-E'),

  /// Weight Scale - HN-300T2-EBK
  HN_300T2_EBK('HN-300T2-EBK'),

  /// Weight Scale - HN-300T2-EGY
  HN_300T2_EGY('HN-300T2-EGY'),

  /// Blood Pressure - HEM-7143T2-ESL
  HEM_7143T2_ESL('HEM-7143T2-ESL'),

  /// Blood Pressure - HEM-7143T1-EBK
  HEM_7143T1_EBK('HEM-7143T1-EBK'),

  /// Blood Pressure - HEM-7143T1-D
  HEM_7143T1_D('HEM-7143T1-D'),

  /// Blood Pressure - BP7450 (Kept as common reference if needed, though not in scan list)
  BP7450('BP7450'),

  /// Weight Scale - BCM-500
  BCM_500('BCM-500'),

  /// Pulse Oximeter - P300
  P300('P300'),

  /// Activity Tracker - HJA-405T
  HJA_405T('HJA-405T'),

  /// Unknown device
  UNKNOWN('Unknown');

  final String key;
  const DeviceIdentifier(this.key);

  static DeviceIdentifier fromKey(String? key) {
    return DeviceIdentifier.values.firstWhere(
      (e) => e.key == key,
      orElse: () => UNKNOWN,
    );
  }
}
