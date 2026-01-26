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
        Log.d("AutoTransferData", "Initialized AutoTransferData");
    }

    void initializeFun(  String localName,String uuid ,int category ) {
      Log.d("AutoTransferData", "initializeFun: localName=" + localName + ", uuid=" + uuid + ", category=" + category);

//        this.map=map;

      //  Log.d("map ****** "," **********  from init "+map.toString());
         mSelectedPeripheral = new OmronPeripheral(localName, uuid);

//mSelectedPeripheral=peripheralLocal;

        selectedUsers.add(1);


        if (mSelectedPeripheral.getUuid() == null || mSelectedPeripheral.getLocalName() == null) {
            Log.d("message", "Device Not Paired");
            return;
        }

        // Disclaimer: Read definition before usage
        if (category == OmronConstants.OMRONBLEDeviceCategory.ACTIVITY || category == OmronConstants.OMRONBLEDeviceCategory.PULSEOXIMETER) {
            startOmronPeripheralManager(false, false,applicationContext);
           // Log.d("message", "Device Not sssss");
            performDataTransfer();
        } else {
             Log.d("category", "************************ categoty = "+category);

            startOmronPeripheralManager(true, false,applicationContext);
            
            // Add delay to allow Manager to initialize state before transfer
            new Handler(android.os.Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    performDataTransfer();
                }
            }, 1000);

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
                    Log.e("AutoTransferData", "Data transfer failed. ResultCode: " + resultInfo.getResultCode() + 
                          ", Detail: " + resultInfo.getDetailInfo() + 
                          ", Message: " + resultInfo.getMessageInfo());

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
                    result.error(String.valueOf(resultInfo.getResultCode()), resultInfo.getMessageInfo(), resultInfo.getDetailInfo());

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
                    Log.e("AutoTransferData", "Users Data transfer failed. ResultCode: " + resultInfo.getResultCode() + 
                          ", Detail: " + resultInfo.getDetailInfo() + 
                          ", Message: " + resultInfo.getMessageInfo());

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
//                            enableDisableButton(true);
//                        }
//                    });
                    result.error(String.valueOf(resultInfo.getResultCode()), resultInfo.getMessageInfo(), resultInfo.getDetailInfo());

                }
            }

        });
    }


    protected void onTransferComplete(List<HashMap<String, Object>> vitalDataList) {
        Gson gson = new Gson();
        List<String> jsonList = new ArrayList<>();
        if (vitalDataList != null) {
            for (HashMap<String, Object> element : vitalDataList) {
                jsonList.add(gson.toJson(element));
            }
        }
        result.success(jsonList);
    }

    private void uploadData(HashMap<String, Object> vitalData, OmronPeripheral peripheral, boolean isWait) {
        HashMap<String, String> deviceInfo = (HashMap<String, String>) peripheral.getDeviceInformation();

        // Blood Pressure Data
        final ArrayList<HashMap<String, Object>> bloodPressureItemList = (ArrayList<HashMap<String, Object>>) vitalData.get(OmronConstants.OMRONVitalDataBloodPressureKey);
        if (bloodPressureItemList != null && !bloodPressureItemList.isEmpty()) {
            onTransferComplete(bloodPressureItemList);
            disconnectDevice();
            return;
        }

        // Weight Data
        ArrayList<HashMap<String, Object>> weightData = (ArrayList<HashMap<String, Object>>) vitalData.get(OmronConstants.OMRONVitalDataWeightKey);
        if (weightData != null && !weightData.isEmpty()) {
            onTransferComplete(weightData);
            disconnectDevice();
            return;
        }

        // Pulse Oximeter Data
        ArrayList<HashMap<String, Object>> pulseOximeterData = (ArrayList<HashMap<String, Object>>) vitalData.get(OmronConstants.OMRONVitalDataPulseOximeterKey);
        if (pulseOximeterData != null && !pulseOximeterData.isEmpty()) {
            onTransferComplete(pulseOximeterData);
            disconnectDevice();
            return;
        }
        
        // Activity Data
        ArrayList<HashMap<String, Object>> activityList = (ArrayList<HashMap<String, Object>>) vitalData.get(OmronConstants.OMRONVitalDataActivityKey);
        if (activityList != null && !activityList.isEmpty()) {
             // Activity is complicated, it has sub-keys. For now, pass the main list.
             // But wait, the original code looked for specific keys inside activityItem. 
             // To simplify for the new parser, let's just pass the list.
             onTransferComplete(activityList);
             disconnectDevice();
             return;
        }
        
        // Wheeze Data? (Original code didn't handle it explicitly but new parser does)
        // If we want to support it, we should add it here.
        ArrayList<HashMap<String, Object>> wheezeList = (ArrayList<HashMap<String, Object>>) vitalData.get(OmronConstants.OMRONVitalDataWheezeKey);
        if (wheezeList != null && !wheezeList.isEmpty()) {
            onTransferComplete(wheezeList);
            disconnectDevice();
            return; 
        }

        // Default: just disconnect if nothing found/handled
        disconnectDevice();
        // Maybe error? 
        if (!isWait) {
            result.error("NO_DATA", "No data found", null);
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
