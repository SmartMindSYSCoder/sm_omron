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
