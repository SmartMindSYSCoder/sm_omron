import 'dart:convert';

import 'enums.dart';

class ScannedDevice {
  String? uuid;
  String? modelName, imageAsset;
  DeviceInformation? deviceInformation;
  String? localName;
  String? category;
  String? deviceGroupIncludedGroupIDKey;
  String? deviceGroupIDKey;
  String? identifier;

  /// Get the device identifier as an enum
  DeviceIdentifier? get deviceIdentifier {
    if (identifier == null) return null;
    return DeviceIdentifier.fromKey(identifier);
  }

  /// Check if the device is a recording wave device (e.g. Temperature)
  bool get isRecordingWave => deviceIdentifier == DeviceIdentifier.MC_280B_E;

  /// Get the device category as typed enum
  DeviceCategory get deviceCategory {
    if (category == null) return DeviceCategory.bloodPressure;
    final catValue = int.tryParse(category!);
    if (catValue == null) return DeviceCategory.bloodPressure;
    return DeviceCategory.fromValue(catValue);
  }

  List<int> selectedUser;

  ScannedDevice(
      {this.uuid,
      this.modelName,
      this.imageAsset,
      this.localName,
      this.category,
      this.identifier,
      this.deviceGroupIncludedGroupIDKey,
      this.deviceGroupIDKey,
      this.deviceInformation,
      this.selectedUser = const []});

  Map<String, dynamic> toJson() {
    return {
      'uuid': uuid,
      'modelName': modelName,
      'localName': localName,
      'category': category,
      'identifier': identifier,
      'deviceGroupIncludedGroupIDKey': deviceGroupIncludedGroupIDKey,
      'deviceGroupIDKey': deviceGroupIDKey,
      // 'imageAsset': imageAsset,

      'deviceInformation': deviceInformation?.toJson(),
      'selectedUser': selectedUser,
      'noOfUsers': "1",
    };
  }

  factory ScannedDevice.fromJson(Map<String, dynamic> map) {
    var deviceInfo = jsonDecode(map['deviceInformation']);

    // print("from model $deviceInfo");
    return ScannedDevice(
      uuid: map['uuid'],
      modelName: map['modelName'],
      category:
          map['category'] ?? deviceInfo['OMRONDeviceInformationCategoryKey'],
      deviceGroupIncludedGroupIDKey: map['deviceGroupIncludedGroupIDKey'],
      deviceGroupIDKey: map['deviceGroupIDKey'],

      localName: map['localName'] ?? deviceInfo['localName'],
      identifier: map['identifier'],

      // imageAsset: map['imageAsset'],
      deviceInformation: map['deviceInformation'] == null
          ? null
          : DeviceInformation.fromJson(
              deviceInfo,
            ),
    );
  }
}

class DeviceInformation {
  DeviceInformation({
    this.localName,
    this.identityName,
    this.displayName,
    this.omronDeviceInformationLocalNameKey,
    this.omronDeviceInformationUuidKey,
    this.omronDeviceInformationIdentityNameKey,
    this.uuid,
    this.omronDeviceInformationDisplayNameKey,
    this.omronDeviceInformationCategoryKey,
  });

  String? localName;
  String? identityName;
  String? displayName;
  String? omronDeviceInformationLocalNameKey;
  String? omronDeviceInformationUuidKey;
  String? omronDeviceInformationIdentityNameKey;
  String? uuid;
  String? omronDeviceInformationDisplayNameKey;
  String? omronDeviceInformationCategoryKey;

  factory DeviceInformation.fromJson(dynamic json) => DeviceInformation(
        localName: json["localName"],
        identityName: json["identityName"],
        displayName: json["displayName"],
        omronDeviceInformationLocalNameKey:
            json["OMRONDeviceInformationLocalNameKey"],
        omronDeviceInformationUuidKey: json["OMRONDeviceInformationUUIDKey"],
        omronDeviceInformationIdentityNameKey:
            json["OMRONDeviceInformationIdentityNameKey"],
        uuid: json["uuid"],
        omronDeviceInformationDisplayNameKey:
            json["OMRONDeviceInformationDisplayNameKey"],
        omronDeviceInformationCategoryKey:
            json["OMRONDeviceInformationCategoryKey"],
      );

  Map<String, dynamic> toJson() => {
        "localName": localName,
        "identityName": identityName,
        "displayName": displayName,
        "OMRONDeviceInformationLocalNameKey":
            omronDeviceInformationLocalNameKey,
        "OMRONDeviceInformationUUIDKey": omronDeviceInformationUuidKey,
        "OMRONDeviceInformationIdentityNameKey":
            omronDeviceInformationIdentityNameKey,
        "uuid": uuid,
        "OMRONDeviceInformationDisplayNameKey":
            omronDeviceInformationDisplayNameKey,
        "OMRONDeviceInformationCategoryKey": omronDeviceInformationCategoryKey,
      };
}
