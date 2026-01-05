import 'enums.dart';

class DeviceModel {
  DeviceModel({
    this.modelName,
    this.deviceGroupIncludedGroupIdKey,
    this.identifier,
    this.deviceProtocol,
    this.image,
    this.modelSeries,
    this.thumbnail,
    this.deviceGroupIdKey,
    this.noOfUsers = '-1',
    this.id,
    this.category = '-1',
    this.modelDisplayName,
  });

  String? modelName;
  String? deviceGroupIncludedGroupIdKey;
  String? identifier;
  String? deviceProtocol;
  String? image;
  String? modelSeries;
  String? thumbnail;
  String? deviceGroupIdKey;
  String noOfUsers;
  String? id;
  String category;
  String? modelDisplayName;

  List<int>? selectedUser;

  factory DeviceModel.fromJson(Map<String, dynamic> json) => DeviceModel(
        modelName: json["modelName"],
        deviceGroupIncludedGroupIdKey: json["deviceGroupIncludedGroupIDKey"],
        identifier: json["identifier"],
        deviceProtocol: json["deviceProtocol"],
        image: json["image"],
        modelSeries: json["modelSeries"],
        thumbnail: json["thumbnail"] ?? json["Thumbnail"],
        deviceGroupIdKey: json["deviceGroupIDKey"],
        noOfUsers: json["noOfUsers"] ?? '-1',
        id: json["id"],
        category: json["category"] ?? '-1',
        modelDisplayName: json["modelDisplayName"],
      );

  Map<String, dynamic> toJson() => {
        "modelName": modelName,
        "deviceGroupIncludedGroupIDKey": deviceGroupIncludedGroupIdKey,
        "identifier": identifier,
        "deviceProtocol": deviceProtocol,
        "image": image,
        "modelSeries": modelSeries,
        "thumbnail": thumbnail,
        "deviceGroupIDKey": deviceGroupIdKey,
        "noOfUsers": noOfUsers,
        "id": id,
        "category": category,
        "modelDisplayName": modelDisplayName,
      };

  String? get imageAsset {
    if (image == null) {
      return null;
    }
    return 'assets/drawable/$image.png';
  }

  /// Get the device identifier as an enum
  DeviceIdentifier? get deviceIdentifier {
    if (identifier == null) return null;
    return DeviceIdentifier.fromKey(identifier);
  }

  /// Check if the device is a recording wave device (e.g. Temperature, Wheeze)
  /// Currently only Temperature (Audio) is handled this way in the context of "RecordingWave"
  /// logically, but the user specifically asked for "RecordingWave".
  /// MC-280B-E is the temperature device that uses audio recording.
  bool get isRecordingWave {
    return deviceIdentifier == DeviceIdentifier.MC_280B_E;
  }
}
