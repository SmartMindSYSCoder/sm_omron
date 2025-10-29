package com.sm.sm_omron;


import static com.sm.sm_omron.OmronManager.personalSettings;
import static com.sm.sm_omron.OmronManager.mSelectedPeripheral;
import static com.sm.sm_omron.OmronManager.selectedUsers;
//import static com.lean.haj_vital_controller_screen.MainActivity.selectedUsers;
import static com.sm.sm_omron.OmronSharedMethods.startOmronPeripheralManager;

import static com.sm.sm_omron.OmronManager.device;

import android.app.Activity;
import android.os.Handler;
import android.util.Log;
import android.content.Context;
import com.google.gson.Gson;
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.Interface.OmronPeripheralManagerDataTransferListener;
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.Interface.*;
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.Interface.OmronPeripheralManagerDisconnectListener;
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.LibraryManager.OmronPeripheralManager;
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.Model.OmronErrorInfo;
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.Model.OmronPeripheral;
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.OmronUtility.OmronConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.Objects;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

public class AutoTransferData  {

    final MethodChannel.Result result;

    final int TIME_INTERVAL = 1000;
//    HashMap<String, Object> map;

    private final   Context applicationContext;


    public AutoTransferData(MethodChannel.Result result, Context context) {
        this.result = result;
        this.applicationContext=context;
    }

    void initializeFun(  String localName,String uuid  ) {

//        this.map=map;

      //  Log.d("map ****** "," **********  from init "+map.toString());
         mSelectedPeripheral = new OmronPeripheral(localName, uuid);

//mSelectedPeripheral=peripheralLocal;

        selectedUsers.add(1);


        if (
                mSelectedPeripheral.getUuid() == null ||mSelectedPeripheral. getDeviceInformation() == null || mSelectedPeripheral. getLocalName() == null) {
            Log.d("message", "Device Not Paired");
            return;
        }

        // Disclaimer: Read definition before usage
        if (Integer.parseInt(device.get(OmronConstants.OMRONBLEConfigDevice.Category)) == OmronConstants.OMRONBLEDeviceCategory.ACTIVITY || Integer.parseInt(device.get(OmronConstants.OMRONBLEConfigDevice.Category)) == OmronConstants.OMRONBLEDeviceCategory.PULSEOXIMETER) {
            startOmronPeripheralManager(false, false,applicationContext);
            Log.d("message", "Device Not sssss");
            performDataTransfer();
        } else {
            startOmronPeripheralManager(false, false,applicationContext);
            performDataTransfer();

        }
    }

    private void performDataTransfer() {

        Log.d("performDataTransfer", "Call performDataTransfer");

        // Set State Change Listener
//        setStateChanges();

        //Create peripheral object with localname and UUID
       if(Objects.equals((String) mSelectedPeripheral.getLocalName(), "MODEL_MC_280B_E")){

           transferAudioDataWithPeripheral();

       }

       else {

//           OmronPeripheral peripheralLocal = new OmronPeripheral(mSelectedPeripheral.getLocalName(), mSelectedPeripheral.getUuid());

           if (selectedUsers.size() > 1) {
               transferUsersDataWithPeripheral(mSelectedPeripheral);
           } else {
//               transferUserDataWithPeripheral(mSelectedPeripheral);
               transferUsersDataWithPeripheral(mSelectedPeripheral);
           }
       }


    }

    // Audio data transfer
    private void transferAudioDataWithPeripheral() {

        OmronPeripheral peripheral = new
                OmronPeripheral(OmronConstants.OMRONThermometerMC280B, "");
        OmronPeripheralManager.sharedManager(applicationContext).startRecording(peripheral, new OmronPeripheralManagerRecordSignalListener() {
            @Override
            public void onSignalStrength(double signalLevel) {
            }
        }, new OmronPeripheralManagerRecordListener() {
            @Override
            public void onRecord(OmronPeripheral peripheral, OmronErrorInfo errorInfo) {



                if(   peripheral !=null){



                    Object output = peripheral.getVitalData();

                    if (output instanceof OmronErrorInfo) {

                      //  final OmronErrorInfo errorInfo = (OmronErrorInfo) output;

//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//
//                               /* mTvErrorCode.setText(errorInfo.getResultCode() + " / " + errorInfo.getDetailInfo());
//                                mTvErrorDesc.setText(errorInfo.getMessageInfo());
//
//                                enableDisableButton(true);*/
//
//                            }
//                        });


                    }
                    else {

                        HashMap<String, Object> vitalData = (HashMap<String, Object>) output;


                        ArrayList<HashMap<String, Object>> temperatureData = (ArrayList<HashMap<String, Object>>)
                                vitalData.get(OmronConstants.OMRONVitalDataTemperatureKey);


                        if (temperatureData != null  && !temperatureData.isEmpty()) {
                            Log.d("temperatureItemList","  this is from temperatureItemList check\n temperatureItemList  *********************************  "+ temperatureData.toString());

                            Gson gson = new Gson();

                            List<String> jsonList = new ArrayList<String>();

                            for (HashMap<String, Object> element : temperatureData) {
                                jsonList.add(gson.toJson(element));
                            }
                            Log.d("temperatureItemList","  this is from temperatureItemList check\n json list *********************************  "+ jsonList.toString());


                            stopRecording();

                            result.success(jsonList);
                        }


//                            uploadData(vitalData, peripheral, true);

                    }



                }


            }
        });



    }

private  void  stopRecording(){
    OmronPeripheralManager.sharedManager(applicationContext).stopRecording(null);
}

    // Single User data transfer
    private void transferUserDataWithPeripheral(OmronPeripheral peripheral) {

        // Data Transfer from Device using OmronPeripheralManager
        OmronPeripheralManager.sharedManager(applicationContext).startDataTransferFromPeripheral(peripheral, selectedUsers.get(0), true, OmronConstants.OMRONVitalDataTransferCategory.BloodPressure, new OmronPeripheralManagerDataTransferListener() {
            @Override
            public void onDataTransferCompleted(OmronPeripheral peripheral, final OmronErrorInfo resultInfo) {

                if (resultInfo.getResultCode() == 0 && peripheral != null) {

//                    HashMap<String, String> deviceInformation = peripheral.getDeviceInformation();
                    HashMap<String, String> deviceInformation = (HashMap<String, String>) peripheral.getDeviceInformation();


                    Log.d("TAGss", "Device Information : " + deviceInformation);

//                    ArrayList<HashMap> allSettings = (ArrayList<HashMap>) peripheral.getDeviceSettings();
                    ArrayList<Map> allSettings = new ArrayList<Map>(peripheral.getDeviceSettings());


                    Log.i("TAGas", "Device settings : " + allSettings.toString());

                    mSelectedPeripheral = peripheral; // Saving for Transfer Function

                    // Save Device to List
                    // To change based on data available
//                    preferencesManager.addDataStoredDeviceList(peripheral.getLocalName(), Integer.parseInt(device.get(OmronConstants.OMRONBLEConfigDevice.Category)), peripheral.getModelName());

                    // Get vital data for previously selected user using OmronPeripheral
                    Object output = peripheral.getVitalData();

                    if (output instanceof OmronErrorInfo) {

                        final OmronErrorInfo errorInfo = (OmronErrorInfo) output;

//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//
//                               /* mTvErrorCode.setText(errorInfo.getResultCode() + " / " + errorInfo.getDetailInfo());
//                                mTvErrorDesc.setText(errorInfo.getMessageInfo());
//
//                                enableDisableButton(true);*/
//
//                            }
//                        });

                        disconnectDevice();

                    } else {

                        HashMap<String, Object> vitalData = (HashMap<String, Object>) output;

                        if (vitalData != null) {
                            uploadData(vitalData, peripheral, true);
                        }
                    }

                }
                else {

//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//
//
//                            if (mHandler != null)
//                                mHandler.removeCallbacks(mRunnable);
//
//                        }
//                    });
                    result.error("0", "Device Scan time out", "error in transfer data");

                }
            }
        });
    }


    private void disconnectDevice() {

        // Disconnect device using OmronPeripheralManager
        OmronPeripheralManager.sharedManager(applicationContext).disconnectPeripheral(mSelectedPeripheral, new OmronPeripheralManagerDisconnectListener() {
            @Override
            public void onDisconnectCompleted(OmronPeripheral peripheral, OmronErrorInfo resultInfo) {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
////                        Toast.makeText(MainActivity.this, "Device disconnected", Toast.LENGTH_SHORT).show();
//
//                    }
//                });
            }
        });
    }

    // Data transfer with multiple users
    private void transferUsersDataWithPeripheral(OmronPeripheral peripheral) {

        OmronPeripheralManager.sharedManager(applicationContext).startDataTransferFromPeripheral(peripheral, selectedUsers, true, new OmronPeripheralManagerDataTransferListener() {
            @Override
            public void onDataTransferCompleted(OmronPeripheral peripheral, final OmronErrorInfo resultInfo) {

                if (resultInfo.getResultCode() == 0 && peripheral != null) {

                    HashMap<String, String> deviceInformation = (HashMap<String, String>) peripheral.getDeviceInformation();
                    Log.d("TAG", "Device Information : " + deviceInformation);

                    ArrayList<HashMap> allSettings = (ArrayList<HashMap>) peripheral.getDeviceSettings();
                    Log.i("TAG", "Device settings : " + allSettings.toString());

                    mSelectedPeripheral = peripheral; // Saving for Transfer Function

                    // Save Device to List
                    // To change based on data available
//                    preferencesManager.addDataStoredDeviceList(peripheral.getLocalName(), Integer.parseInt(device.get(OmronConstants.OMRONBLEConfigDevice.Category)), peripheral.getModelName());

                    // Get vital data for previously selected user using OmronPeripheral
                    Object output = peripheral.getVitalData();

                    if (output instanceof OmronErrorInfo) {

                        final OmronErrorInfo errorInfo = (OmronErrorInfo) output;

//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//
////                                mTvErrorCode.setText(errorInfo.getResultCode() + " / " + errorInfo.getDetailInfo());
////                                mTvErrorDesc.setText(errorInfo.getMessageInfo());
////
////                                enableDisableButton(true);
//
//                            }
//                        });

                        disconnectDevice();

                    } else {

                        HashMap<String, Object> vitalData = (HashMap<String, Object>) output;

                        if (vitalData != null) {
                            uploadData(vitalData, peripheral, true);
                        }
                    }

                } else {

//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//
//                         /*   setStatus("-");
//                            mTvErrorCode.setText(resultInfo.getResultCode() + " / " + resultInfo.getDetailInfo());
//                            mTvErrorDesc.setText(resultInfo.getMessageInfo());*/
//
//                            if (mHandler != null)
//                                mHandler.removeCallbacks(mRunnable);
//
//
////                            enableDisableButton(true);
//                        }
//                    });
                    result.error("0", "Device Scan time out", "error in transfer data");

                }
            }

        });
    }


    private void uploadData(HashMap<String, Object> vitalData, OmronPeripheral peripheral, boolean isWait) {

//        HashMap<String, String> deviceInfo = peripheral.getDeviceInformation();
        HashMap<String, String> deviceInfo = (HashMap<String, String>) peripheral.getDeviceInformation();




        // Blood Pressure Data
        final ArrayList<HashMap<String, Object>> bloodPressureItemList = (ArrayList<HashMap<String, Object>>) vitalData.get(OmronConstants.OMRONVitalDataBloodPressureKey);


//        if (bloodPressureItemList != null) {
//
//            for (HashMap<String, Object> bpItem : bloodPressureItemList) {
//                Log.d("Blood Pressure - ", bpItem.toString());
//            }
////            insertVitalDataToDB(bloodPressureItemList, deviceInfo);
//        }


        if (bloodPressureItemList != null  && !bloodPressureItemList.isEmpty()) {

            Gson gson = new Gson();

            List<String> jsonList = new ArrayList<String>();

            for (HashMap<String, Object> element : bloodPressureItemList) {
                jsonList.add(gson.toJson(element));
            }
            //   Log.d("weightItemList","  this is from weightItemList check\n json list *********************************  "+ jsonList.toString());

            result.success(jsonList);
            disconnectDevice();
            return;






//            for (HashMap<String, Object> weightItem : weightData) {
//                Log.d("Weight - ", " Weight \n *****************   "+weightItem.toString());
//            }
//            insertRecordToDB(recordData, deviceInfo);
        }




        // Activity Data
        ArrayList<HashMap<String, Object>> activityList = (ArrayList<HashMap<String, Object>>) vitalData.get(OmronConstants.OMRONVitalDataActivityKey);
        if (activityList != null) {

            for (HashMap<String, Object> activityItem : activityList) {

                List<String> list = new ArrayList<String>(activityItem.keySet());

                for (String key : list) {

                    Log.d("Activity key - ", key);
                    Log.d("Activity key - ", key);
                    Log.d("Activity Data - ", activityItem.get(key).toString());

                    if (key.equalsIgnoreCase(OmronConstants.OMRONActivityData.AerobicStepsPerDay) || key.equalsIgnoreCase(OmronConstants.OMRONActivityData.StepsPerDay) || key.equalsIgnoreCase(OmronConstants.OMRONActivityData.DistancePerDay) || key.equalsIgnoreCase(OmronConstants.OMRONActivityData.WalkingCaloriesPerDay)) {
//                        insertActivityToDB((HashMap<String, Object>) activityItem.get(key), deviceInfo, key);
                    }
                }
            }
        }

        // Sleep Data
        ArrayList<HashMap<String, Object>> sleepingData = (ArrayList<HashMap<String, Object>>) vitalData.get(OmronConstants.OMRONVitalDataSleepKey);
        if (sleepingData != null) {

            for (HashMap<String, Object> sleepitem : sleepingData) {
                Log.d("Sleep - ", sleepitem.toString());
            }
//            insertSleepToDB(sleepingData, deviceInfo);
        }

        // Records Data
        ArrayList<HashMap<String, Object>> recordData = (ArrayList<HashMap<String, Object>>) vitalData.get(OmronConstants.OMRONVitalDataRecordKey);
        if (recordData != null) {

            for (HashMap<String, Object> recordItem : recordData) {
                Log.d("Record - ", recordItem.toString());
            }
//            insertRecordToDB(recordData, deviceInfo);
        }

        // Weight Data
        ArrayList<HashMap<String, Object>> weightData = (ArrayList<HashMap<String, Object>>) vitalData.get(OmronConstants.OMRONVitalDataWeightKey);
        if (weightData != null  && !weightData.isEmpty()) {

            Gson gson = new Gson();

            List<String> jsonList = new ArrayList<String>();

            for (HashMap<String, Object> element : weightData) {
                jsonList.add(gson.toJson(element));
            }
         //   Log.d("weightItemList","  this is from weightItemList check\n json list *********************************  "+ jsonList.toString());

            result.success(jsonList);
            disconnectDevice();
            return;

        }


        // Temperature Data
        ArrayList<HashMap<String, Object>> temperatureData = (ArrayList<HashMap<String, Object>>)
                vitalData.get(OmronConstants.OMRONVitalDataTemperatureKey);
        if (temperatureData != null) {


            for (HashMap<String, Object> temperatureItem : temperatureData) {
             //   Log.d("TemperatureKey", "\n***********************************\n" +temperatureItem.toString());
            }

        }




        // Pulse oxximeter Data
        ArrayList<HashMap<String, Object>> pulseOximeterData = (ArrayList<HashMap<String, Object>>) vitalData.get(OmronConstants.OMRONVitalDataPulseOximeterKey);
//        if (pulseOximeterData != null) {
//
//            for (HashMap<String, Object> pulseOximeterItem : pulseOximeterData) {
//                Log.d("Pulse Oximeter - ", pulseOximeterItem.toString());
//            }
//        }

        if (pulseOximeterData != null  && !pulseOximeterData.isEmpty()) {

            Gson gson = new Gson();

            List<String> jsonList = new ArrayList<String>();

            for (HashMap<String, Object> element : pulseOximeterData) {
                jsonList.add(gson.toJson(element));
            }
            //   Log.d("weightItemList","  this is from weightItemList check\n json list *********************************  "+ jsonList.toString());

            result.success(jsonList);
            disconnectDevice();
            return;






//            for (HashMap<String, Object> weightItem : weightData) {
//                Log.d("Weight - ", " Weight \n *****************   "+weightItem.toString());
//            }
//            insertRecordToDB(recordData, deviceInfo);
        }




        if (isWait) {

//            mHandler = new Handler();
//            mRunnable = new Runnable() {
//                @Override
//                public void run() {
//                    continueDataTransfer();
//                }
//            };
//
//            mHandler.postDelayed(mRunnable, TIME_INTERVAL);

        } else {

//            if (mHandler != null)
//                mHandler.removeCallbacks(mRunnable);

          //  continueDataTransfer();
        }
    }

    private void continueDataTransfer() {

        OmronPeripheralManager.sharedManager(applicationContext).endDataTransferFromPeripheral(new OmronPeripheralManagerDataTransferListener() {
            @Override
            public void onDataTransferCompleted(final OmronPeripheral peripheral, final OmronErrorInfo errorInfo) {

                if (errorInfo.getResultCode() == 0 && peripheral != null) {

                    HashMap<String, String> deviceInformation = (HashMap<String, String>) peripheral.getDeviceInformation();
                    Log.d("TAG", "Device Information : " + deviceInformation);

                    ArrayList<HashMap> allSettings = (ArrayList<HashMap>) peripheral.getDeviceSettings();
                    Log.i("TAG", "Device settings : " + allSettings.toString());


                    // Get vital data for previously selected  using OmronPeripheral
                    Object output = peripheral.getVitalData();

                    if (output instanceof OmronErrorInfo) {

                        //final OmronErrorInfo errorInfo = (OmronErrorInfo) output;

//                                mTvErrorCode.setText(errorInfo.getResultCode() + " / " + errorInfo.getDetailInfo());
//                                mTvErrorDesc.setText(errorInfo.getMessageInfo());

                    }
                    else {

                        HashMap<String, Object> vitalData = (HashMap<String, Object>) output;

                        if (vitalData != null) {

                            // Blood Pressure Data
                            final ArrayList<HashMap<String, Object>> bloodPressureItemList = (ArrayList<HashMap<String, Object>>) vitalData.get(OmronConstants.OMRONVitalDataBloodPressureKey);
                            final ArrayList<HashMap<String, Object>> weightItemList = (ArrayList<HashMap<String, Object>>) vitalData.get(OmronConstants.OMRONVitalDataWeightKey);
                            final ArrayList<HashMap<String, Object>> temperatureItemList = (ArrayList<HashMap<String, Object>>) vitalData.get(OmronConstants.OMRONVitalDataTemperatureKey);
                            final ArrayList<HashMap<String, Object>> pulseOximeterItemList = (ArrayList<HashMap<String, Object>>) vitalData.get(OmronConstants.OMRONVitalDataPulseOximeterKey);

                            if (bloodPressureItemList != null  && !bloodPressureItemList.isEmpty()) {




                                Log.d("bloodPressure", " *******************8   this is from bloodPressure "+bloodPressureItemList.toString());

                                Gson gson = new Gson();

                                List<String> jsonList = new ArrayList<String>();

                                for (HashMap<String, Object> element : bloodPressureItemList) {
                                    jsonList.add(gson.toJson(element));
                                }

                                result.success(jsonList);
                                return;
                            }



                            else  if (weightItemList != null  && !weightItemList.isEmpty()) {
                              //  Log.d("weightItemList","  this is from weightItemList check\n weightItemList  *********************************  "+ weightItemList.toString());

                                Gson gson = new Gson();

                                List<String> jsonList = new ArrayList<String>();

                                for (HashMap<String, Object> element : weightItemList) {
                                    jsonList.add(gson.toJson(element));
                                }
                                Log.d("weightItemList","  this is from weightItemList check\n json list *********************************  "+ jsonList.toString());

                                result.success(jsonList);
                                return;
                            }




                            else  if (pulseOximeterItemList != null  && !pulseOximeterItemList.isEmpty()) {
                                Log.d("weightItemList","  this is from weightItemList check\n weightItemList  *********************************  "+ weightItemList.toString());

                                Gson gson = new Gson();

                                List<String> jsonList = new ArrayList<String>();

                                for (HashMap<String, Object> element : pulseOximeterItemList) {
                                    jsonList.add(gson.toJson(element));
                                }
                                Log.d("pulseOximeterItemList","  this is from pulseOximeterItemList check\n json list *********************************  "+ jsonList.toString());

                                result.success(jsonList);
                                return;
                            }




//                                  else  if (temperatureItemList != null  && !temperatureItemList.isEmpty()) {
//                                        Log.d("temperatureItemList","  this is from temperatureItemList check\n temperatureItemList  *********************************  "+ weightItemList.toString());
//
//                                        Gson gson = new Gson();
//
//                                        List<String> jsonList = new ArrayList<String>();
//
//                                        for (HashMap<String, Object> element : temperatureItemList) {
//                                            jsonList.add(gson.toJson(element));
//                                        }
//                                        Log.d("weightItemList","  this is from temperatureItemList check\n json list *********************************  "+ jsonList.toString());
//
//                                        result.success(jsonList);
//                                    }
                            else{
                                List<String> jsonList = new ArrayList<String>();

                                //   result.success(jsonList);

                            }



                        }
                    }

                }
                else {
                    result.error("0", "Error in transfer data", "error in transfer data");

                }
            }

        });
    }


}
