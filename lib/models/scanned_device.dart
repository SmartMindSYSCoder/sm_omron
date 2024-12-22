import 'dart:convert';

class ScannedDevice {
  String? uuid;
  String? modelName,imageAsset;
  DeviceInformation? deviceInformation;
  String? localName;
  String? category;
  String? deviceGroupIncludedGroupIDKey;
  String? identifier;

  List<int> selectedUser;

  ScannedDevice({
    this.uuid,
    this.modelName,this.imageAsset,this.localName,this.category,this.deviceGroupIncludedGroupIDKey,
    this.deviceInformation,
    this.selectedUser=const[]
  });

  Map<String, dynamic> toJson() {
    return {
      'uuid': uuid,
      'modelName': modelName,
      'localName': localName,
      'category': category,
      'identifier': identifier,
      'deviceGroupIncludedGroupIDKey': deviceGroupIncludedGroupIDKey,
      // 'imageAsset': imageAsset,

      'deviceInformation': deviceInformation?.toJson(),
      'selectedUser': selectedUser,
      'noOfUsers': "1",
    };
  }

  factory ScannedDevice.fromJson(Map<String, dynamic> map) {
    var deviceInfo=jsonDecode(map['deviceInformation']);
    return ScannedDevice(
      uuid: map['uuid'],
      modelName: map['modelName'],
      category: map['category'],
      deviceGroupIncludedGroupIDKey: map['deviceGroupIncludedGroupIDKey'],

      localName:deviceInfo !=null ? deviceInfo['modelName'] :"",
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

  factory DeviceInformation.fromJson(dynamic json) =>
      DeviceInformation(
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
