# sm_omron

A new Flutter project.

## Getting Started

This plugin for using omron devices

### PreRequests:
-Make sure bluetooth is enabled.
-You have the omron device that you want to read data from and it is in the supported models from omron.


### usage

First step: declare instance of plugin like below:
                
    final _smOmronPlugin = SMOmron();

Second step : check permission of Record for temperature device or  bluetooth for others  ,as you need  like below:


    _smOmronPlugin.checkBluetoothPermissions();
    _smOmronPlugin.checkRecordPermissions();

Third step : now you need to add device to save it and use it again 
- press the bluetooth button of device about 5 or 10 sec  to enable bluetooth pairing mode
- show all supported devices models then press the device you want to add    /// show example for more details

      await getDevicesModels(context);

Forth step : now device must be added in savedDevices list you can start measure then press on the saved device  item and send the scannedDevice object



     var omronData=await     _smOmronPlugin.readDevice(deviceIdentifier: savedDevices[index].deviceInformation!.omronDeviceInformationIdentityNameKey!, scannedDevice: savedDevices[index]);

        if(omronData !=null) {


           /// use data as you want 
                               
                          }

                               else {


                              ///   print("**************    No Data");

                               }