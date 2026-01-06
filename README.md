# SM Omron Plugin

A comprehensive Flutter plugin for integrating Omron healthcare devices on Android. This plugin supports a wide range of devices including Blood Pressure monitors, Weight Scales, Pulse Oximeters, and Activity Trackers via BLE, as well as Temperature measurement via audio frequency.

## Features

*   **Unified API**: Access data from various device types through a single, consistent interface.
*   **Normalized Data**: All results are returned as `VitalResult` objects, making it easy to handle diverse health data.
*   **Explicit Pairing**: Robust pairing workflow that ensures devices are bonded correctly with the Android system.
*   **Native UI**: Custom-built Android parsers and handlers for reliable data transfer.
*   **Audio Support**: Unique support for Omron microphone-based temperature devices (e.g., MC-280B-E).
*   **Asset Integration**: Built-in support for device thumbnails.

## Supported Device Categories

*   **Blood Pressure** (BLE) - e.g., BP7450, HEM-9200T
*   **Weight Scale / Body Composition** (BLE) - e.g., BCM-500, HBF-222T
*   **Pulse Oximeter** (BLE) - e.g., P300
*   **Activity Tracker** (BLE) - e.g., HJA-405T
*   **Temperature** (Audio) - e.g., MC-280B-E
*   **Wheeze Detector** (BLE) - e.g., HWZ-1000T

## Installation

Add the following to your `pubspec.yaml`:

```yaml
dependencies:
  sm_omron:
    git: https://github.com/SmartMindSYSCoder/sm_omron.git
```

## Getting Started

### 1. Import
You now only need to import one file to access all models, enums, and widgets:

```dart
import 'package:sm_omron/sm_omron.dart';
```

### 2. Android Setup

Ensure your `android/app/build.gradle` defines a minimum SDK version of at least **24** (26+ recommended for best BLE performance).

```gradle
defaultConfig {
    minSdkVersion 24
    ...
}
```

### 2. Permissions

Add the necessary permissions to your `AndroidManifest.xml`:

```xml
<!-- BLE Permissions -->
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" /> <!-- Required for BLE on older Android -->
<uses-permission android:name="android.permission.BLUETOOTH_SCAN" />
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />

<!-- Audio Permission (For Temperature Devices) -->
<uses-permission android:name="android.permission.RECORD_AUDIO" />
```



## Usage

### Initialization

```dart
import 'package:sm_omron/sm_omron.dart';

final _smOmron = SMOmron();

// Required: Initialize plugin storage
await _smOmron.initialize();
```

### 1. Permissions Check

Before starting, request the required permissions:

```dart
// For BLE Devices
await _smOmron.checkBluetoothPermissions();

// For Audio/Temperature Devices
await _smOmron.checkMicrophonePermissions();
```

### 2. Discover & Pair Device

Use the built-in `OmronDeviceSelectorDialog` to let users choose a compatible device, then scan and pair it.

```dart
// 1. Show Device Selector
final deviceModel = await OmronDeviceSelectorDialog.show(
  context,
  title: "Select Your Device",
  categoryFilter: DeviceCategory.bloodPressure, // Optional filter
);

if (deviceModel != null) {
  ScannedDevice? scannedDevice;

  // 2. Check Device Type (Audio vs BLE)
  if (deviceModel.isRecordingWave) {
    // For Audio/Temperature devices (e.g. MC-280B-E), create directly
    scannedDevice = _smOmron.addRecordingWaveDevice(deviceModel);
  } else {
    // For BLE devices, scan specifically for the selected model
    // Ensure the device has a valid identifier mapped
    if (deviceModel.deviceIdentifier != null) {
      scannedDevice = await _smOmron.scanBleDevice(deviceIdentifier: deviceModel.deviceIdentifier!);
    }
  }
  
  if (scannedDevice != null) {
     // 3. Explicitly pair (Bond) if needed (for BLE)
     // Audio devices skip this or return true immediately
     bool paired = await _smOmron.pairBleDevice(device: scannedDevice);
     
     if (paired) {
       // 4. Save device for future use
       await _smOmron.saveDevice(scannedDevice);
     }
  }
}
```

### 3. Transfer Data (BLE)

Once a device is paired and saved, you can transfer data from it.

```dart
try {
  List<VitalResult> results = await _smOmron.transferFromBleDevice(
    device: mySavedDevice,
    options: TransferOptions(
      readHistoricalData: true, // Set to true to read all past data
    ),
    // personalInfo is required for Body Composition devices (Weight scales)
    personalInfo: PersonalInfo(
        heightCm: 175, 
        weightKg: 70, 
        dateOfBirth: DateTime(1990, 1, 1), 
        gender: Gender.male
    ),
  );

  for (var result in results) {
     print("Sys: ${result.systolic}, Dia: ${result.diastolic}");
  }
} catch (e) {
  print("Transfer failed: $e");
}
```

### 4. Record Temperature (Audio)

For microphone-based devices like the MC-280B-E:

```dart
try {
  VitalResult? tempResult = await _smOmron.recordTemperature();
  
  if (tempResult != null) {
    print("Temp: ${tempResult.temperature} ${tempResult.temperatureUnit}");
  }
} catch (e) {
  print("Recording failed: $e");
}
```

### 5. Unpairing

To remove a device and unbond it from the Android system:

```dart
await _smOmron.removeDevice(mySavedDevice);
```

## Data Models

### VitalResult

The `VitalResult` class unifies all measurements. Check `result.type` to know which fields are populated.

*   **Blood Pressure**: `systolic`, `diastolic`, `pulse`, `irregularHeartbeat`
*   **Weight**: `weight`, `bodyFat`, `skeletalMuscle`, `bmi`
*   **Activity**: `steps`, `calories`, `distance`
*   **Pulse Ox**: `spo2Level`, `pulseRate`
*   **Temperature**: `temperature`

## Troubleshooting

*   **System Pairing Dialog Not Creating**: The plugin uses a native fix to force the pairing dialog. If it still doesn't appear, ensure the device is in "Pairing Mode" (usually hold the Bluetooth button for 3-5 seconds until it flashes 'P').
*   **"MissingPluginException"**: Ensure you have rebuilt the app (`flutter run`) after adding the plugin packages.
*   **Location Permission**: On Android 11 and below, `ACCESS_FINE_LOCATION` is required for BLE scanning to work. Ensure this is granted.