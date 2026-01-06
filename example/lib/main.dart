import 'dart:async';

import 'package:flutter/material.dart';
import 'package:sm_omron/sm_omron.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.deepPurple),
        useMaterial3: true,
      ),
      home: const HomePage(),
    );
  }
}

class HomePage extends StatefulWidget {
  const HomePage({super.key});

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  final _smOmron = SMOmron();

  List<ScannedDevice> savedDevices = [];
  OmronConnectionState _connectionState = OmronConnectionState.idle;
  String _result = "";
  StreamSubscription<OmronConnectionState>? _stateSubscription;

  @override
  void initState() {
    super.initState();
    _initPlugin();
  }

  Future<void> _initPlugin() async {
    await _smOmron.initialize();
    // Listen to connection state changes
    _stateSubscription = _smOmron.connectionStateStream.listen((state) {
      if (mounted) {
        setState(() {
          _connectionState = state;
        });
      }
    });

    await _loadSavedDevices();
  }

  @override
  void dispose() {
    _stateSubscription?.cancel();
    _smOmron.dispose();
    super.dispose();
  }

  Future<void> _loadSavedDevices() async {
    final devices = await _smOmron.getSavedDevices();
    if (mounted) {
      setState(() {
        savedDevices = devices;
      });
    }
  }

  Future<void> _addDevice() async {
    // Use the new device selector dialog
    final device = await OmronDeviceSelectorDialog.show(
      context,
      title: 'Select Omron Device',
    );

    if (device == null) return;

    await _handleAddDevice(device);
  }

  Future<void> _handleAddDevice(DeviceModel device) async {
    setState(() {
      _result = "Scanning for ${device.modelName}...";
    });

    try {
      ScannedDevice? scannedDevice;

      // Check if it's a recording wave device (e.g. MC-280B-E)
      if (device.isRecordingWave) {
        scannedDevice = _smOmron.addRecordingWaveDevice(device);
      } else {
        // Otherwise, perform BLE scan
        if (device.deviceIdentifier != null) {
          scannedDevice = await _smOmron.scanBleDevice(
              deviceIdentifier: device.deviceIdentifier!);
        } else {
          // Fallback or error if identifier not mapped
          if (mounted) {
            setState(() {
              _result =
                  "Error: Device identifier not found for ${device.modelName}";
            });
          }
        }
      }

      final sDevice = scannedDevice;
      if (sDevice != null) {
        if (mounted) {
          setState(() {
            _result = "Pairing with ${sDevice.modelName}...";
          });
        }

        final paired = await _smOmron.pairBleDevice(device: sDevice);

        if (paired) {
          await _smOmron.saveDevice(sDevice);
          await _loadSavedDevices();

          if (mounted) {
            setState(() {
              _result = "Device paired & saved: ${sDevice.modelName}";
            });
          }
        } else {
          if (mounted) {
            setState(() {
              _result = "Pairing failed for ${sDevice.modelName}";
            });
          }
        }
      } else {
        if (mounted) {
          setState(() {
            _result = "Device not found. Make sure it's in pairing mode.";
          });
        }
      }
    } catch (e) {
      if (mounted) {
        setState(() {
          _result = "Error: $e";
        });
      }
    }
  }

  Future<void> _transferData(ScannedDevice device) async {
    setState(() {
      _result = "Transferring data from ${device.modelName}...";
    });

    try {
      // Check if temperature device
      if (device.deviceCategory == DeviceCategory.temperature) {
        final result = await _smOmron.recordTemperature();
        if (result != null) {
          _displayResult(result);
        } else {
          if (mounted) {
            setState(() {
              _result = "No temperature data recorded";
            });
          }
        }
        return;
      }

      // BLE device transfer
      final results = await _smOmron.transferFromBleDevice(
        device: device,
        options: const TransferOptions(
          readHistoricalData: true, // Only new readings
          userIds: [1],
        ),
      );
      print(
          "**************************  results length ${results.length}*************************");

      if (results.isNotEmpty) {
        print(
            "**************************  results ${results.first.toJson()}*************************");

        _displayResult(results.last);
      } else {
        if (mounted) {
          setState(() {
            _result = "No new readings available";
          });
        }
      }
    } catch (e) {
      if (mounted) {
        setState(() {
          _result = "Error: $e";
        });
      }
    }
  }

  void _displayResult(VitalResult result) {
    String display = "";

    switch (result.type) {
      case VitalType.bloodPressure:
        display = "Blood Pressure:\n"
            "  Systolic: ${result.systolic} mmHg\n"
            "  Diastolic: ${result.diastolic} mmHg\n"
            "  Pulse: ${result.pulse} bpm";
        if (result.irregularHeartbeat == true) {
          display += "\n  ⚠️ Irregular heartbeat detected";
        }
        break;

      case VitalType.weight:
        display = "Weight:\n"
            "  Weight: ${result.weight} kg";
        if (result.bmi != null) {
          display += "\n  BMI: ${result.bmi}";
        }
        if (result.bodyFatPercentage != null) {
          display += "\n  Body Fat: ${result.bodyFatPercentage}%";
        }
        break;

      case VitalType.pulseOximeter:
        display = "Pulse Oximeter:\n"
            "  SpO2: ${result.spo2Level}%\n"
            "  Pulse: ${result.pulseOximeterRate} bpm";
        break;

      case VitalType.temperature:
        final unit =
            result.temperatureUnit == TemperatureUnit.fahrenheit ? '°F' : '°C';
        display = "Temperature:\n"
            "  ${result.temperature}$unit";
        break;

      case VitalType.activity:
        display = "Activity:\n"
            "  Steps: ${result.steps}\n"
            "  Calories: ${result.calories}";
        break;

      case VitalType.wheeze:
        final detected = result.wheezeResult == WheezeResult.wheezeDetected;
        display = "Wheeze Detection:\n"
            "  ${detected ? '⚠️ Wheeze Detected' : '✓ No Wheeze'}";
        break;

      default:
        display = result.toString();
    }

    if (mounted) {
      setState(() {
        _result = display;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('SM Omron Plugin Demo'),
        backgroundColor: Theme.of(context).colorScheme.inversePrimary,
        actions: [
          // Connection state indicator
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 16),
            child: Center(
              child: Row(
                children: [
                  Icon(
                    _connectionState.isActive
                        ? Icons.bluetooth_connected
                        : Icons.bluetooth,
                    color: _connectionState.isActive ? Colors.green : null,
                  ),
                  const SizedBox(width: 4),
                  Text(_connectionState.statusMessage),
                ],
              ),
            ),
          ),
        ],
      ),
      body: Column(
        children: [
          // Permissions section
          Card(
            margin: const EdgeInsets.all(16),
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Row(
                children: [
                  Expanded(
                    child: ElevatedButton.icon(
                      onPressed: () => _smOmron.checkBluetoothPermissions(),
                      icon: const Icon(Icons.bluetooth),
                      label: const Text('Bluetooth'),
                    ),
                  ),
                  const SizedBox(width: 8),
                  Expanded(
                    child: ElevatedButton.icon(
                      onPressed: () => _smOmron.checkMicrophonePermissions(),
                      icon: const Icon(Icons.mic),
                      label: const Text('Microphone'),
                    ),
                  ),
                ],
              ),
            ),
          ),

          // Devices list
          Expanded(
            child:
                savedDevices.isEmpty ? _buildEmptyState() : _buildDevicesList(),
          ),

          // Result display
          if (_result.isNotEmpty)
            Container(
              width: double.infinity,
              margin: const EdgeInsets.only(
                  top: 16, bottom: 80, right: 16, left: 16),
              padding: const EdgeInsets.all(16),
              decoration: BoxDecoration(
                color: Theme.of(context).colorScheme.surfaceContainerHighest,
                borderRadius: BorderRadius.circular(12),
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    'Result:',
                    style: Theme.of(context).textTheme.titleSmall,
                  ),
                  const SizedBox(height: 8),
                  Text(_result),
                ],
              ),
            ),
        ],
      ),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: _addDevice,
        icon: const Icon(Icons.add),
        label: const Text('Add Device'),
      ),
    );
  }

  Widget _buildEmptyState() {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Icon(
            Icons.devices,
            size: 64,
            color: Theme.of(context).colorScheme.outline,
          ),
          const SizedBox(height: 16),
          Text(
            'No devices added yet',
            style: Theme.of(context).textTheme.titleMedium,
          ),
          const SizedBox(height: 8),
          Text(
            'Tap the button below to add an Omron device',
            style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                  color: Theme.of(context).colorScheme.outline,
                ),
          ),
        ],
      ),
    );
  }

  Widget _buildDevicesList() {
    return ListView.builder(
      padding: const EdgeInsets.symmetric(horizontal: 16),
      itemCount: savedDevices.length,
      itemBuilder: (context, index) {
        final device = savedDevices[index];
        final category = device.deviceCategory;

        return Card(
          child: ListTile(
            leading: Container(
              width: 48,
              height: 48,
              decoration: BoxDecoration(
                color: Theme.of(context).colorScheme.primaryContainer,
                borderRadius: BorderRadius.circular(8),
              ),
              child: Icon(
                _getCategoryIcon(category),
                color: Theme.of(context).colorScheme.onPrimaryContainer,
              ),
            ),
            title: Text(device.modelName ?? 'Unknown Device'),
            subtitle: Text(category.displayName),
            trailing: Row(
              mainAxisSize: MainAxisSize.min,
              children: [
                IconButton(
                  icon: const Icon(Icons.sync),
                  onPressed: () => _transferData(device),
                  tooltip: 'Transfer Data',
                ),
                IconButton(
                  icon: const Icon(Icons.delete_outline),
                  onPressed: () async {
                    await _smOmron.removeDevice(device);
                    await _loadSavedDevices();
                  },
                  tooltip: 'Remove Device',
                ),
              ],
            ),
            onTap: () => _transferData(device),
          ),
        );
      },
    );
  }

  IconData _getCategoryIcon(DeviceCategory category) {
    switch (category) {
      case DeviceCategory.bloodPressure:
        return Icons.favorite;
      case DeviceCategory.weight:
        return Icons.monitor_weight;
      case DeviceCategory.activity:
        return Icons.directions_walk;
      case DeviceCategory.pulseOximeter:
        return Icons.air;
      case DeviceCategory.temperature:
        return Icons.thermostat;
      case DeviceCategory.wheeze:
        return Icons.air;
    }
  }
}
