import 'dart:convert';
import 'dart:developer';

import 'package:flutter/services.dart';
import 'package:get_storage/get_storage.dart';
import 'package:sm_omron/models/omron_data.dart';

import 'models/device_model.dart';
import 'models/scanned_device.dart';

class SMOmron {
  static const String _omronDevicesKey = "omronDevices";

  final _methodChannel = const MethodChannel('sm_omron');

  final statusStream = const EventChannel("sm_omron_status");

  final GetStorage box = GetStorage();

  SMOmron() {
    GetStorage.init();
  }

  Future<dynamic> checkBluetoothPermissions() {
    return _methodChannel.invokeMethod('checkBluetoothPermissions');
  }

  Future<dynamic> checkRecordPermissions() {
    return _methodChannel.invokeMethod('checkRecordPermissions');
  }

  Future<List<DeviceModel>> getDevicesModelsList() async {
    dynamic data = await _methodChannel.invokeMethod('getDevicesList');

    List<DeviceModel> models = [];

    if (data is List) {
      for (var e in data) {
        var model = DeviceModel.fromJson(jsonDecode(e));

        models.add(model);
      }
    }

    return models;
  }

  Future<ScannedDevice?> scan({required String deviceIdentifier}) async {
    print("scan");

    dynamic data = await _methodChannel.invokeMethod('getDevicesList');

    DeviceModel? deviceModel;

    if (data is List) {
      // logger.i('list');
      // var jsonDecodedData = jsonDecode(jsonEncode(result));
      // logger.d(jsonDecodedData.runtimeType);

      for (var e in data) {
        var d = DeviceModel.fromJson(jsonDecode(e));

        if (d.identifier == deviceIdentifier) {
          // print("**********************  from scan");
          // print(e);
          // print("**********************");
          deviceModel = d;
          break;
        }
      }

      if (deviceModel != null) {
        dynamic scanData =
            await _methodChannel.invokeMethod('scan', deviceModel.toJson());

        if (scanData is String && scanData.toString().contains("modelName")) {
          // print(jsonDecode(scanData));

          // print("**********************  from scanned device");
          // print(jsonDecode(scanData));
          // print("**********************");

          var scannedDevice = ScannedDevice.fromJson(jsonDecode(scanData));
          // print("**********************  before return");
          //
          // print(scannedDevice.toJson());
          // print("**********************");

          // print(scannedDevice.uuid);
          scannedDevice.category = deviceModel.category;
          scannedDevice.deviceGroupIncludedGroupIDKey =
              deviceModel.deviceGroupIncludedGroupIdKey;
          scannedDevice.identifier = deviceModel.identifier;
          return scannedDevice;

//         dynamic data=  await methodChannel.invokeMethod('autoTransferData', scannedDevice.toJson());
//
//
//
//         List<OmronData> omronData=[];
//
//         if(data is List){
//
// omronData.clear();
//           for (var e in data) {
//
//             var d=  OmronData.fromJson(jsonDecode(e));
//
//
//             omronData.add(d);
//
//
//
//
//
//           }
//
//           return omronData.lastOrNull;
//
//
//         }
        }
      }
    }

    return null;
  }

  Future<dynamic> connectDevice({required ScannedDevice scannedDevice}) async {
    return await _methodChannel.invokeMethod('connect', scannedDevice.toJson());
  }

  Future<OmronData?> readDevice(
      {required String deviceIdentifier,
      required ScannedDevice scannedDevice}) async {
    print("scan");

    dynamic data = await _methodChannel.invokeMethod('getDevicesList');

    DeviceModel? deviceModel;

    print(deviceIdentifier);
    print(scannedDevice.toJson());
    if (data is List) {
      // logger.i('list');
      // var jsonDecodedData = jsonDecode(jsonEncode(result));
      // logger.d(jsonDecodedData.runtimeType);

      for (var e in data) {
        var d = DeviceModel.fromJson(jsonDecode(e));

        // print(d.toJson());
        print(
            "**************** deviceIdentifier:$deviceIdentifier     current identifier :${d.identifier}");
        if (d.identifier == deviceIdentifier) {
          print(d.toJson());
          deviceModel = d;
          break;
        }
      }

      if (deviceModel != null) {
        _methodChannel.invokeMethod('initScan', deviceModel.toJson());

        // var scannedDevice=  ScannedDevice.fromJson(jsonDecode(scanData));

        print(
            "*********************  before transfer \n${scannedDevice.toJson()}");

        dynamic data = await _methodChannel.invokeMethod('autoTransferData', {
          'localName': scannedDevice.deviceInformation!.localName,
          'uuid': scannedDevice.uuid
        });

        print(
            "data  type  is ${data.runtimeType}  *****************************");

        List<OmronData> omronData = [];

        if (data is List) {
          omronData.clear();
          for (var e in data) {
            var d = OmronData.fromJson(jsonDecode(e));

            omronData.add(d);
          }

          print("*************************    length is :${omronData.length}");

          return omronData.lastOrNull;
        }
      }
    }

    return null;
  }

  Future<List<ScannedDevice>> getSavedDevices() async {
    List<ScannedDevice> devices = [];

    dynamic d = box.read(_omronDevicesKey);

    print("************ $d");
    if (d != null) {
      for (var element in d) {
        if (element is ScannedDevice) {
          devices.add(element);
        } else {
          var s = (jsonEncode(element));
          var di = (jsonEncode(element['deviceInformation']));
          var su = (jsonEncode(element['selectedUser']));

          Map<String, dynamic> tt = (json.decode(s));
          Map<String, dynamic> ttt = (json.decode(di));
          var sut = (json.decode(su)) as List;

          var dvv = DeviceInformation.fromJson(ttt);

          var sd = ScannedDevice(
              uuid: tt['uuid'],
              modelName: tt['modelName'],
              identifier: tt['identifier'],
              deviceGroupIncludedGroupIDKey:
                  tt['deviceGroupIncludedGroupIDKey'],
              imageAsset: tt['imageAsset'],
              localName: dvv.localName,
              category: dvv.omronDeviceInformationCategoryKey,
              selectedUser: [sut.isNotEmpty ? sut.first : 1],
              deviceInformation: dvv);

          devices.add(sd);
        }
      }
    }

    return devices;
  }

  saveDevice(ScannedDevice scannedDevice) async {
    /// getting all saved devices
    List<ScannedDevice> savedDevices = await getSavedDevices();

    /// in case there is already saved devices
    if (savedDevices.isNotEmpty) {
      /// we will check if device is already saved

      for (var element in savedDevices) {
        if (element.uuid == scannedDevice.uuid) {
          /// this return will stop code here , no need to continue  check or implement any code
          return;
        }
      }

      savedDevices.add(scannedDevice);

      box.write(_omronDevicesKey, savedDevices.map((e) => e.toJson()).toList());
    } else {
      /// No already devices saved , will save first device
      box.write(_omronDevicesKey, [scannedDevice.toJson()]);
    }
  }

  removeDevice(ScannedDevice scannedDevice) async {
    /// getting all saved data
    List<ScannedDevice> oldSavedData = await getSavedDevices();

    // print('oldSavedData :************************************  $oldSavedData');

    // List<dynamic>? oldSavedData = box.read('devices1');

    /// in case there is saved data
    if (oldSavedData.isNotEmpty) {
      oldSavedData.removeWhere(
          (element) => element.modelName! == scannedDevice.modelName);

      /// save the new collection
      box.write(_omronDevicesKey, oldSavedData.map((e) => e.toJson()).toList());

      // List<ScannedDevice> devices= await getSavedDevices() ;
    }
  }

  Future<OmronData?> readWeight(ScannedDevice scannedDevice) async {
    await _methodChannel.invokeMethod('getDevicesList');

    var sd = scannedDevice;
    sd.localName = scannedDevice.deviceInformation!.localName;
    sd.category =
        scannedDevice.deviceInformation!.omronDeviceInformationCategoryKey;

    dynamic data =
        await _methodChannel.invokeMethod('weight', scannedDevice.toJson());

    List<OmronData> omronData = [];

    log(data.toString());
    if (data is List) {
      omronData.clear();
      for (var e in data) {
        var d = OmronData.fromJson(jsonDecode(e));

        omronData.add(d);
      }

      return omronData.lastOrNull;
    }

    return null;
  }
}
