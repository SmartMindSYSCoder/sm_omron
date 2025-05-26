package com.sm.sm_omron;


import static com.sm.sm_omron.OmronManager.mSelectedPeripheral;

import android.os.Handler;
import android.util.Log;
import android.content.Context;

import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.DeviceConfiguration.OmronPeripheralManagerConfig;
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.Interface.OmronPeripheralManagerConnectListener;
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.LibraryManager.OmronPeripheralManager;
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.Model.OmronErrorInfo;
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.Model.OmronPeripheral;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import io.flutter.plugin.common.MethodChannel;

public class ConnectDevice  {
    MethodChannel.Result result;
    OmronPeripheral omronPeripheral;
    private final   Context applicationContext;

    ConnectDevice(Context context,OmronPeripheral omronPeripheral) {
        this.applicationContext = context;
        this.omronPeripheral = omronPeripheral;
    };


    void connectPeripheralWithWait() {


//        mSelectedPeripheral = omronPeripheral;

             OmronPeripheralManager.sharedManager(applicationContext).connectPeripheral(omronPeripheral, true, new OmronPeripheralManagerConnectListener() {

                    @Override
                    public void onConnectCompleted(final OmronPeripheral peripheral, final OmronErrorInfo resultInfo) {

                        connectionUpdateWithPeripheral(peripheral, resultInfo, true);
                    }
                }
        );
    }

    private void connectionUpdateWithPeripheral(final OmronPeripheral peripheral, final OmronErrorInfo resultInfo, final boolean wait) {

        if (resultInfo.getResultCode() == 0 && peripheral != null) {

            omronPeripheral = peripheral;

            if (null != peripheral.getLocalName()) {


                HashMap<String, String> deviceInformation = (HashMap<String, String>) peripheral.getDeviceInformation();
                Log.d("sss", "Device Information : " + deviceInformation);

//                ArrayList<HashMap> deviceSettings = omronPeripheral.getDeviceSettings();
                ArrayList<Map> deviceSettings = new ArrayList<Map>(omronPeripheral.getDeviceSettings());


                if (deviceSettings != null) {
                    Log.d("TAGss", "Device Settings:" + deviceSettings.toString());
                }

                OmronPeripheralManagerConfig peripheralConfig = OmronPeripheralManager.sharedManager(applicationContext).getConfiguration();
                Log.d("TAG", "Device Config :  " + peripheralConfig.getDeviceConfigGroupIdAndGroupIncludedId(peripheral.getDeviceGroupIDKey(), peripheral.getDeviceGroupIncludedGroupIDKey()));

//                if (wait) {
//                    mHandler = new Handler();
//                    mHandler.postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            resumeConnection(peripheral);
//                        }
//                    }, 5000);
//                } else {
//
//                    if (peripheral.getVitalData() != null) {
//                        Log.d("dddde", "Vital data - " + peripheral.getVitalData().toString());
//                    }
////                            showMessage(getString(R.string.device_connected), getString(R.string.device_paired));
//                }

              //  result.success(true);

            }

        } else {

           // result.error("0","0","error");

        }
    }

//    private void resumeConnection(OmronPeripheral peripheral) {
//
//        if (selectedUsers.size() > 1) {
//            OmronPeripheralManager.sharedManager(App.getInstance().getApplicationContext()).resumeConnectPeripheral(
//                    omronPeripheral, selectedUsers, new OmronPeripheralManagerConnectListener() {
//                        @Override
//                        public void onConnectCompleted(final OmronPeripheral peripheral, final OmronErrorInfo resultInfo) {
//                            connectionUpdateWithPeripheral(peripheral, resultInfo, false);
//                        }
//
//                    });
//        } else {
//            OmronPeripheralManager.sharedManager(App.getInstance().getApplicationContext()).resumeConnectPeripheral(
//                    omronPeripheral, new ArrayList<>(Arrays.asList(selectedUsers.get(0))), new OmronPeripheralManagerConnectListener() {
//                        @Override
//                        public void onConnectCompleted(final OmronPeripheral peripheral, final OmronErrorInfo resultInfo) {
//                            connectionUpdateWithPeripheral(peripheral, resultInfo, false);
//                        }
//
//                    });
//        }
//    }


    void connectPeripheral() {

//        isScan = false;

       // mSelectedPeripheral = omronPeripheral;

        // Pair to Device using OmronPeripheralManager
        OmronPeripheralManager.sharedManager(applicationContext).connectPeripheral(omronPeripheral, new OmronPeripheralManagerConnectListener() {

                    @Override
                    public void onConnectCompleted(final OmronPeripheral peripheral, final OmronErrorInfo resultInfo) {


                      //  new  AutoTransferData(result,applicationContext).initializeFun();

//                        connectionUpdateWithPeripheral(peripheral, resultInfo, false);
                    }
                }
        );
    }
}
