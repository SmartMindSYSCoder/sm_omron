import 'dart:async';
import 'dart:convert';

import 'package:flutter/services.dart';
import 'package:get_storage/get_storage.dart';

import 'models/device_model.dart';
import 'models/enums.dart';
import 'models/personal_info.dart';
import 'models/scanned_device.dart';
import 'models/transfer_options.dart';
import 'models/vital_result.dart';

export 'models/device_model.dart';
export 'models/enums.dart';
export 'models/personal_info.dart';
export 'models/scanned_device.dart';
export 'models/transfer_options.dart';
export 'models/vital_result.dart';
export 'widgets/device_selector_dialog.dart';

/// Main class for interacting with Omron devices.
///
/// This plugin provides access to Omron health devices including:
/// - Blood Pressure Monitors (BLE)
/// - Weight Scales / Body Composition (BLE)
/// - Activity Trackers (BLE)
/// - Pulse Oximeters (BLE)
/// - Wheeze Detectors (BLE)
/// - Thermometers (Audio/Microphone)
class SMOmron {
  static const String _omronDevicesKey = "omronDevices";

  final _methodChannel = const MethodChannel('sm_omron');
  final _statusEventChannel = const EventChannel('sm_omron_status');

  final GetStorage _box = GetStorage();

  // Stream controller for connection state
  StreamController<OmronConnectionState>? _connectionStateController;
  StreamSubscription? _eventChannelSubscription;

  SMOmron() {
    // Initialize GetStorage if not already initialized
    // However, it is better to call initialize() explicitly
  }

  /// Initialize the plugin dependencies.
  Future<void> initialize() async {
    await GetStorage.init();
  }

  // ============================================================
  // PERMISSION METHODS
  // ============================================================

  /// Check and request Bluetooth permissions.
  ///
  /// Returns `true` if permissions are granted.
  Future<bool> checkBluetoothPermissions() async {
    final result =
        await _methodChannel.invokeMethod('checkBluetoothPermissions');
    return result == true;
  }

  /// Check and request microphone permissions (for temperature devices).
  ///
  /// Returns `true` if permissions are granted.
  Future<bool> checkMicrophonePermissions() async {
    final result = await _methodChannel.invokeMethod('checkRecordPermissions');
    return result == true;
  }

  /// Check if Bluetooth permissions are currently granted.
  Future<bool> isBluetoothPermissionGranted() async {
    final result = await _methodChannel.invokeMethod('isPermissionsGranted');
    return result == true;
  }

  // ============================================================
  // DEVICE DISCOVERY
  // ============================================================

  /// Get list of all supported Omron device models.
  ///
  /// Optionally filter by [category] to get only specific device types.
  Future<List<DeviceModel>> getSupportedDevices(
      {DeviceCategory? category}) async {
    dynamic data = await _methodChannel.invokeMethod('getDevicesList');

    List<DeviceModel> models = [];

    if (data is List) {
      for (var e in data) {
        var model = DeviceModel.fromJson(jsonDecode(e));

        // Apply category filter if specified
        if (category != null) {
          final modelCategory = int.tryParse(model.category);
          if (modelCategory != category.value) continue;
        }

        models.add(model);
      }
    }

    // Inject manual support for MC-280B-E if not present (Audio device)
    // Checking against identifier to safely avoid duplicates
    final hasTemp = models.any((m) => m.identifier == 'MC-280B-E');
    if (!hasTemp &&
        (category == null || category == DeviceCategory.temperature)) {
      models.add(DeviceModel(
        modelName: 'MC-280B-E',
        modelDisplayName: 'Omron MC-280B-E',
        identifier: 'MC-280B-E',
        category: '${DeviceCategory.temperature.value}',
        deviceProtocol: 'OMRONAudioProtocol', // Explicitly set protocol
        image: 'mc_280b_e', // Ensure this asset exists or handle null
        thumbnail: 'mc_280b_e',
      ));
    }

    return models;
  }

  // ============================================================
  // BLE DEVICE OPERATIONS
  // ============================================================

  /// Scan for a specific BLE device.
  ///
  /// This initiates a Bluetooth scan for the specified [deviceIdentifier].
  /// Returns the [ScannedDevice] if found, or `null` if not found within timeout.
  ///
  /// Use this for pairing new devices. Once paired, save the device
  /// using [saveDevice] for future data transfers.
  Future<ScannedDevice?> scanBleDevice({
    required DeviceIdentifier deviceIdentifier,
    Duration timeout = const Duration(seconds: 30),
  }) async {
    // Lookup the device model to get full configuration required by native method
    final devices = await getSupportedDevices();
    final device =
        devices.firstWhereOrNull((d) => d.identifier == deviceIdentifier.key);

    if (device == null) {
      print("Device model not found for identifier: ${deviceIdentifier.key}");
      return null;
    }

    dynamic scanData =
        await _methodChannel.invokeMethod('scan', device.toJson());

    if (scanData is String && scanData.contains("modelName")) {
      var scannedDevice = ScannedDevice.fromJson(jsonDecode(scanData));
      scannedDevice.category = device.category;
      scannedDevice.deviceGroupIncludedGroupIDKey =
          device.deviceGroupIncludedGroupIdKey;
      scannedDevice.identifier = device.identifier;
      return scannedDevice;
    }

    return null;
  }

  /// Pair/Bond with a specific BLE device.
  ///
  /// This establishes the bonding without transferring data.
  /// Call this after scanning and before saving the device.
  Future<bool> pairBleDevice({required ScannedDevice device}) async {
    // Intercept recording wave device - no pairing needed
    if (device.isRecordingWave) {
      return true;
    }

    try {
      await _methodChannel.invokeMethod('connectToDevice', device.toJson());
      return true;
    } catch (e) {
      if (e is PlatformException) {
        print("Pairing error: ${e.code} - ${e.message} - ${e.details}");
      } else {
        print("Pairing error: $e");
      }
      return false;
    }
  }

  /// Transfer data from a paired BLE device.
  ///
  /// [device] - The previously paired and saved device.
  /// [options] - Transfer options including historical data flag (default: false).
  /// [personalInfo] - Required for activity trackers and weight scales to
  ///                  calculate body composition metrics.
  ///
  /// Returns a list of [VitalResult] objects with the transferred data.
  Future<List<VitalResult>> transferFromBleDevice({
    required ScannedDevice device,
    TransferOptions options = const TransferOptions(),
    PersonalInfo? personalInfo,
  }) async {
    // Get device list to find the device model
    dynamic deviceListData =
        await _methodChannel.invokeMethod('getDevicesList');
    DeviceModel? deviceModel;

    if (deviceListData is List) {
      for (var e in deviceListData) {
        var d = DeviceModel.fromJson(jsonDecode(e));
        if (d.identifier == device.identifier) {
          deviceModel = d;
          break;
        }
      }
    }

    if (deviceModel == null) {
      throw OmronException(
          'Device model not found for identifier: ${device.identifier}');
    }

    // Build transfer parameters
    final transferParams = {
      'localName': device.deviceInformation?.localName,
      'uuid': device.uuid,
      'category': device.deviceCategory.value,
      'model': deviceModel.toJson(),
      'options': options.toJson(),
      if (personalInfo != null) 'personalInfo': personalInfo.toJson(),
    };

    dynamic data =
        await _methodChannel.invokeMethod('autoTransferData', transferParams);

    List<VitalResult> results = [];

    if (data is List) {
      for (var e in data) {
        final jsonData = jsonDecode(e) as Map<String, dynamic>;
        final result = _parseVitalData(jsonData, device.deviceCategory);
        results.add(result);
      }
    }

    return results;
  }

  /// Legacy method for backward compatibility.
  @Deprecated('Use transferFromBleDevice instead')
  Future<VitalResult?> readDevice({
    required String deviceIdentifier,
    required ScannedDevice scannedDevice,
  }) async {
    final results = await transferFromBleDevice(device: scannedDevice);
    return results.lastOrNull;
  }

  // ============================================================
  // AUDIO DEVICE OPERATIONS (TEMPERATURE)
  // ============================================================

  /// Record temperature from an audio-based thermometer (MC-280B-E).
  ///
  /// This uses the microphone to capture the temperature reading signal.
  /// Make sure microphone permissions are granted before calling this.
  ///
  /// [maxDuration] - Maximum recording duration (default: 60 seconds).
  ///
  /// Returns [VitalResult] with temperature data, or `null` if recording fails.
  Future<VitalResult?> recordTemperature({
    Duration maxDuration = const Duration(seconds: 60),
  }) async {
    // Temperature device requires a pre-configured ScannedDevice
    final savedDevices = await getSavedDevices();
    final tempDevice = savedDevices.firstWhereOrNull(
      (d) => d.identifier == 'MC-280B-E',
    );

    if (tempDevice == null) {
      throw OmronException(
          'Temperature device not configured. Please add MC-280B-E device first.');
    }

    final transferParams = {
      'localName': 'MODEL_MC_280B_E',
      'uuid': tempDevice.uuid,
      'category': DeviceCategory.temperature.value,
      'model': {'identifier': 'MC-280B-E'},
    };

    dynamic data =
        await _methodChannel.invokeMethod('autoTransferData', transferParams);

    if (data is List && data.isNotEmpty) {
      final jsonData = jsonDecode(data.last) as Map<String, dynamic>;
      return _parseVitalData(jsonData, DeviceCategory.temperature);
    }

    return null;
  }

  /// Create a recording wave device manually (since it uses audio, not BLE scanning).
  ScannedDevice addRecordingWaveDevice(DeviceModel device) {
    final uuid = DateTime.now().microsecondsSinceEpoch.toString();
    const localName = "MODEL_MC_280B_E"; // Or dynamic based on device

    return ScannedDevice(
      uuid: uuid,
      modelName: device.modelName,
      identifier: device.identifier,
      category: device.category,
      selectedUser: [1],
      deviceInformation: DeviceInformation(
        uuid: uuid,
        localName: localName,
        omronDeviceInformationCategoryKey: device.category,
        omronDeviceInformationLocalNameKey: localName,
        displayName: device.modelDisplayName,
      ),
    );
  }

  /// Legacy method for weight reading.
  @Deprecated('Use transferFromBleDevice instead')
  Future<VitalResult?> readWeight(ScannedDevice scannedDevice) async {
    final results = await transferFromBleDevice(device: scannedDevice);
    return results.lastOrNull;
  }

  // ============================================================
  // CONNECTION STATE STREAM
  // ============================================================

  /// Stream of connection state changes.
  ///
  /// Listen to this stream to update UI during device operations.
  Stream<OmronConnectionState> get connectionStateStream {
    _connectionStateController ??=
        StreamController<OmronConnectionState>.broadcast();

    // Cancel existing subscription if any to avoid duplicates
    _eventChannelSubscription?.cancel();
    _eventChannelSubscription =
        _statusEventChannel.receiveBroadcastStream().listen((event) {
      if (event is Map && event.containsKey('state')) {
        final stateIndex = event['state'] as int;
        if (stateIndex >= 0 &&
            stateIndex < OmronConnectionState.values.length) {
          _connectionStateController
              ?.add(OmronConnectionState.values[stateIndex]);
        }
      }
    });

    return _connectionStateController!.stream;
  }

  /// Dispose of resources.
  void dispose() {
    _eventChannelSubscription?.cancel();
    _eventChannelSubscription = null;
    _connectionStateController?.close();
    _connectionStateController = null;
  }

  // ============================================================
  // DEVICE STORAGE
  // ============================================================

  /// Get all saved/paired devices.
  Future<List<ScannedDevice>> getSavedDevices() async {
    List<ScannedDevice> devices = [];

    dynamic d = _box.read(_omronDevicesKey);

    if (d != null) {
      for (var element in d) {
        if (element is ScannedDevice) {
          devices.add(element);
        } else {
          var s = jsonEncode(element);
          var di = jsonEncode(element['deviceInformation']);
          var su = jsonEncode(element['selectedUser']);

          Map<String, dynamic> tt = json.decode(s);
          Map<String, dynamic> ttt = json.decode(di);
          var sut = json.decode(su) as List;

          var dvv = DeviceInformation.fromJson(ttt);

          var sd = ScannedDevice(
            uuid: tt['uuid'],
            modelName: tt['modelName'],
            identifier: tt['identifier'],
            deviceGroupIncludedGroupIDKey: tt['deviceGroupIncludedGroupIDKey'],
            imageAsset: tt['imageAsset'],
            localName: dvv.localName,
            category: dvv.omronDeviceInformationCategoryKey,
            selectedUser: [sut.isNotEmpty ? sut.first : 1],
            deviceInformation: dvv,
          );

          devices.add(sd);
        }
      }
    }

    return devices;
  }

  /// Save a paired device for future use.
  Future<void> saveDevice(ScannedDevice scannedDevice) async {
    List<ScannedDevice> savedDevices = await getSavedDevices();

    // Check if device already exists
    for (var element in savedDevices) {
      if (element.uuid == scannedDevice.uuid) {
        return; // Already saved
      }
    }

    savedDevices.add(scannedDevice);
    _box.write(_omronDevicesKey, savedDevices.map((e) => e.toJson()).toList());
  }

  /// Remove a saved device.
  Future<void> removeDevice(ScannedDevice scannedDevice) async {
    List<ScannedDevice> savedDevices = await getSavedDevices();

    // Attempt to unpair natively (best effort)
    try {
      if (scannedDevice.uuid != null) {
        await _methodChannel
            .invokeMethod('unpairDevice', {'uuid': scannedDevice.uuid});
        print("Explicit unpairing requested for ${scannedDevice.uuid}");
      }
    } catch (e) {
      print("Error unpairing device: $e");
    }

    if (savedDevices.isNotEmpty) {
      savedDevices.removeWhere((element) => element.uuid == scannedDevice.uuid);
      _box.write(
          _omronDevicesKey, savedDevices.map((e) => e.toJson()).toList());
    }
  }

  // ============================================================
  // LEGACY API (DEPRECATED - FOR BACKWARD COMPATIBILITY)
  // ============================================================

  @Deprecated('Use getSupportedDevices instead')
  Future<List<DeviceModel>> getDevicesModelsList() => getSupportedDevices();

  @Deprecated('Use scanBleDevice instead')
  Future<ScannedDevice?> scan({required String deviceIdentifier}) async {
    final identifierEnum = DeviceIdentifier.fromKey(deviceIdentifier);
    // If unknown, we can't reliably scan without a model match, but scanBleDevice handles lookup.
    // scanBleDevice expects a valid DeviceIdentifier.
    if (identifierEnum == DeviceIdentifier.UNKNOWN) return null;

    return scanBleDevice(deviceIdentifier: identifierEnum);
  }

  @Deprecated('Use checkBluetoothPermissions instead')
  Future<dynamic> checkRecordPermissions() => checkMicrophonePermissions();

  @Deprecated('Not needed with new API')
  Future<dynamic> connectDevice({required ScannedDevice scannedDevice}) async {
    return await _methodChannel.invokeMethod('connect', scannedDevice.toJson());
  }

  // ============================================================
  // PRIVATE HELPERS
  // ============================================================

  /// Parse vital data from native JSON to VitalResult
  VitalResult _parseVitalData(
      Map<String, dynamic> json, DeviceCategory category) {
    switch (category) {
      case DeviceCategory.bloodPressure:
        return VitalResult(
          type: VitalType.bloodPressure,
          measurementDate: _parseDate(json['measurementDate']),
          userId: json['userId'] as int?,
          sequenceNumber: json['sequenceNumber'] as int?,
          systolic: json['systolic'] as int?,
          diastolic: json['diastolic'] as int?,
          pulse: json['pulse'] as int?,
          irregularHeartbeat: json['irregularHeartbeat'] == true,
          atrialFibrillation: json['atrialFibrillation'] == true,
          cuffWrapDetection: json['cuffWrapDetection'] == true,
          movementDetection: json['movementDetection'] == true,
          measurementMode: json['measurementMode'] as int?,
          rawData: json['rawData'],
        );

      case DeviceCategory.weight:
        return VitalResult(
          type: VitalType.weight,
          measurementDate: _parseDate(json['measurementDate']),
          userId: json['userId'] as int?,
          sequenceNumber: json['sequenceNumber'] as int?,
          weight: (json['weight'] as num?)?.toDouble(),
          bmi: (json['bmi'] as num?)?.toDouble(),
          bodyFatPercentage: (json['bodyFatPercentage'] as num?)?.toDouble(),
          skeletalMusclePercentage:
              (json['skeletalMusclePercentage'] as num?)?.toDouble(),
          visceralFatLevel: json['visceralFatLevel'] as int?,
          basalMetabolicRate: json['basalMetabolicRate'] as int?,
          bodyAge: json['bodyAge'] as int?,
          rawData: json['rawData'],
        );

      case DeviceCategory.pulseOximeter:
        return VitalResult(
          type: VitalType.pulseOximeter,
          measurementDate: _parseDate(json['measurementDate']),
          userId: json['userId'] as int?,
          spo2Level: json['spo2Level'] as int?,
          pulseOximeterRate: json['pulseOximeterRate'] as int?,
          rawData: json['rawData'],
        );

      case DeviceCategory.temperature:
        final tempUnit = json['temperatureUnit'] as int?;
        return VitalResult(
          type: VitalType.temperature,
          measurementDate: _parseDate(json['measurementDate']),
          temperature: (json['temperature'] as num?)?.toDouble(),
          temperatureUnit: tempUnit == 1
              ? TemperatureUnit.fahrenheit
              : TemperatureUnit.celsius,
          rawData: json['rawData'],
        );

      case DeviceCategory.activity:
        return VitalResult(
          type: VitalType.activity,
          measurementDate: _parseDate(json['measurementDate']),
          userId: json['userId'] as int?,
          steps: json['steps'] as int?,
          aerobicSteps: json['aerobicSteps'] as int?,
          distance: (json['distance'] as num?)?.toDouble(),
          calories: json['calories'] as int?,
          rawData: json['rawData'],
        );

      case DeviceCategory.wheeze:
        final wheezeValue = json['wheezeResult'] as int?;
        return VitalResult(
          type: VitalType.wheeze,
          measurementDate: _parseDate(json['measurementDate']),
          wheezeResult: wheezeValue == 1
              ? WheezeResult.wheezeDetected
              : WheezeResult.noWheeze,
          rawData: json['rawData'],
        );
    }
  }

  DateTime? _parseDate(dynamic value) {
    if (value == null) return null;
    if (value is int) return DateTime.fromMillisecondsSinceEpoch(value);
    if (value is String) {
      try {
        return DateTime.parse(value);
      } catch (_) {
        return null;
      }
    }
    return null;
  }
}

/// Exception thrown by Omron operations.
class OmronException implements Exception {
  final String message;
  final String? code;
  final dynamic details;

  OmronException(this.message, {this.code, this.details});

  @override
  String toString() =>
      'OmronException: $message${code != null ? ' (code: $code)' : ''}';
}

/// Extension for List to provide firstWhereOrNull
extension ListExtension<T> on List<T> {
  T? firstWhereOrNull(bool Function(T) test) {
    for (var element in this) {
      if (test(element)) return element;
    }
    return null;
  }
}
