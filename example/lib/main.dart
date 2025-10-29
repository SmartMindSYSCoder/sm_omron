import 'dart:async';

import 'package:flutter/material.dart';
import 'package:sm_omron/models/device_model.dart';
import 'package:sm_omron/models/omron_data.dart';
import 'package:sm_omron/models/scanned_device.dart';
import 'package:sm_omron/sm_omron.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();

  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  final _smOmronPlugin = SMOmron();

  List<ScannedDevice> savedDevices = [];
  List<DeviceModel> models = [];

  getSavedDevices() async {
    savedDevices.clear();
    savedDevices = await _smOmronPlugin.getSavedDevices();
    savedDevices.forEach((e) {
      print(e.toJson());
    });

    print(savedDevices.length);
    setState(() {});
  }

  getDevicesModels(BuildContext context) async {
    models.clear();
    models = await _smOmronPlugin.getDevicesModelsList();

    showModelsDialog(context);
  }

  late Stream stream;
  @override
  void initState() {
    super.initState();

    getSavedDevices();
  }

  String data = "";

  // Platform messages are asynchronous, so we initialize in an async method.

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Builder(builder: (context) {
          return Center(
            child: Column(
              children: [
                Expanded(
                  child: SingleChildScrollView(
                    child: Column(
                      mainAxisAlignment: MainAxisAlignment.center,
                      crossAxisAlignment: CrossAxisAlignment.center,
                      children: [
                        TextButton(
                            onPressed: () {
                              _smOmronPlugin.checkBluetoothPermissions();
                              _smOmronPlugin.checkRecordPermissions();
                            },
                            child: const Text("checkPermissions")),
                        // const SizedBox(height: 10,),

                        TextButton(
                            onPressed: () async {
                              getSavedDevices();
                            },
                            child: const Text("Get Saved Devices")),

                        const SizedBox(
                          height: 10,
                        ),

                        TextButton(
                            onPressed: () async {
                              var omronData = await _smOmronPlugin
                                  .readWeight(savedDevices.first);
                              //   print("**************     Data  $omronData");

                              if (omronData != null) {
                                parseData(omronData);
                                print(omronData);
                              } else {
                                //    print("**************    No Data");
                              }
                            },
                            child: Container(
                                padding: EdgeInsets.symmetric(
                                    horizontal: 30, vertical: 5),
                                margin: EdgeInsets.symmetric(vertical: 20),
                                decoration: BoxDecoration(
                                    color: Colors.deepPurple,
                                    borderRadius: BorderRadius.circular(10)),
                                child: const Text(
                                  "Read Weight",
                                  style: TextStyle(color: Colors.white),
                                ))),

                        TextButton(
                            onPressed: () async {
                              var omronData =
                                  await _smOmronPlugin.connectDevice(
                                      scannedDevice: savedDevices.first);
                            },
                            child: Container(
                                padding: EdgeInsets.symmetric(
                                    horizontal: 30, vertical: 5),
                                margin: EdgeInsets.symmetric(vertical: 20),
                                decoration: BoxDecoration(
                                    color: Colors.deepPurple,
                                    borderRadius: BorderRadius.circular(10)),
                                child: const Text(
                                  "Connect  Device",
                                  style: TextStyle(color: Colors.white),
                                ))),

                        const SizedBox(
                          height: 10,
                        ),

                        const Text(
                          "Saved Devices ",
                          style: TextStyle(fontSize: 18),
                        ),

                        if (savedDevices.isNotEmpty)
                          ListView.builder(
                              physics: const NeverScrollableScrollPhysics(),
                              shrinkWrap: true,
                              itemCount: savedDevices.length,
                              itemBuilder: (bc, index) {
                                return ListTile(
                                  onTap: () async {
                                    var omronData =
                                        await _smOmronPlugin.readDevice(
                                            deviceIdentifier:
                                                savedDevices[index]
                                                        .identifier ??
                                                    '',
                                            scannedDevice: savedDevices[index]);

                                    if (omronData != null) {
                                      parseData(omronData);
                                    } else {
                                      print("**************    No Data");
                                    }
                                  },
                                  title: Text(
                                    savedDevices[index].modelName ?? "",
                                  ),
                                  subtitle: Text(
                                    savedDevices[index]
                                        .deviceInformation!
                                        .localName
                                        .toString(),
                                  ),
                                  trailing: IconButton(
                                    onPressed: () async {
                                      await _smOmronPlugin
                                          .removeDevice(savedDevices[index]);

                                      getSavedDevices();
                                    },
                                    icon: const Icon(Icons.delete_forever),
                                  ),
                                );
                              })
                        else
                          Column(
                            children: [
                              const Padding(
                                padding: EdgeInsets.all(20.0),
                                child: Text("No devices added yet "
                                    ""
                                    "\n\n"
                                    "To add new device press button below or in the right bottom to add device  \n"
                                    ""
                                    "make sure the device must be in bluetooth mode except temperature device because it use microphone to record data"
                                    ""),
                              ),
                              // const SizedBox(height: 30,),

                              TextButton(
                                  onPressed: () async {
                                    await getDevicesModels(context);
                                  },
                                  child: Container(
                                      padding: EdgeInsets.symmetric(
                                          horizontal: 30, vertical: 5),
                                      margin:
                                          EdgeInsets.symmetric(vertical: 20),
                                      decoration: BoxDecoration(
                                          color: Colors.deepPurple,
                                          borderRadius:
                                              BorderRadius.circular(10)),
                                      child: const Text(
                                        "add Device",
                                        style: TextStyle(color: Colors.white),
                                      ))),
                            ],
                          ),
                      ],
                    ),
                  ),
                ),
                Text("Result :\n$data"),
                const SizedBox(
                  height: 30,
                ),
              ],
            ),
          );
        }),
        floatingActionButton: Container(
          width: 50,
          height: 50,
          decoration:
              BoxDecoration(color: Colors.deepPurple, shape: BoxShape.circle),
          child: Builder(builder: (context) {
            return GestureDetector(
                onTap: () async {
                  await getDevicesModels(context);
                },
                child: Icon(
                  Icons.add,
                  color: Colors.white,
                ));
          }),
        ),
      ),
    );
  }

  parseData(OmronData od) {
    data = "";

    if (od.omronVitalDataSystolicKey > 0) {
      data +=
          "BP: ${od.omronVitalDataSystolicKey}/${od.omronVitalDataDiastolicKey} \nHeart Rate: ${od.omronVitalDataPulseKey}";
    }

    if (od.oMRONPulseOximeterSPO2LevelKey != null &&
        od.oMRONPulseOximeterSPO2LevelKey! > 0) {
      data +=
          "\nSpo2: ${od.oMRONPulseOximeterSPO2LevelKey}\nSpo2 Rate: ${od.oMRONPulseOximeterPulseRateKey}";
    }
    if (od.omronWeightKey > 0) {
      data = "\nWeight:${od.omronWeightKey} ";
    }
    if (od.omronTemperatureKey > 0) {
      data +=
          " \nTemperature: ${od.omronTemperatureKey}  ${od.omronTemperatureUnitKey == 1 ? '°F' : '°C'}";
    }
    data += "\n\n";

    setState(() {});
  }

  showModelsDialog(BuildContext context) {
    showDialog(
        context: context,
        builder: (bc) {
          return AlertDialog(
            title: const Text("Omron Models"),
            content: SizedBox(
              width: double.maxFinite,
              height: 500,
              child: ListView.builder(
                  itemCount: models.length,
                  itemBuilder: (bc, index) {
                    DeviceModel model = models[index];

                    return ListTile(
                      onTap: () async {
                        print(model.toJson());

                        Navigator.pop(context);

                        /// temperature
                        if (model.identifier == "MC-280B-E") {
                          var uuid =
                              DateTime.now().microsecondsSinceEpoch.toString();

                          String localName = "MODEL_MC_280B_E";
                          // var uuid=model.identifier;
                          ScannedDevice scannedDevice = ScannedDevice(
                              uuid: uuid,
                              modelName: model.modelName,
                              selectedUser: [1],
                              imageAsset: model.imageAsset,
                              deviceInformation: DeviceInformation(
                                uuid: uuid,
                                omronDeviceInformationCategoryKey:
                                    model.category,
                                omronDeviceInformationLocalNameKey: localName,
                                localName: localName,
                                displayName: model.modelDisplayName,
                                identityName: model.modelSeries,
                                omronDeviceInformationUuidKey: uuid,
                                omronDeviceInformationIdentityNameKey:
                                    model.modelSeries,
                                omronDeviceInformationDisplayNameKey:
                                    model.modelDisplayName,
                              ));

                          print("before save ***********************");
                          print(scannedDevice.toJson());
                          print("before save ***********************");

                          await _smOmronPlugin.saveDevice(scannedDevice);

                          getSavedDevices();
                        } else {
                          var device = await _smOmronPlugin.scan(
                              deviceIdentifier: model.identifier!);

                          if (device != null) {
                            print("before save ***********************");
                            print(device.toJson());
                            print("before save ***********************");
                            await _smOmronPlugin.saveDevice(device);

                            getSavedDevices();
                          } else {
                            print("No Device Found");
                          }
                        }
                      },
                      title: Text(model.modelName ?? ""),
                      subtitle: Text(model.identifier ?? ""),
                    );
                  }),
            ),
          );
        });
  }
}
