package com.sm.sm_omron;
import android.content.Context;
import android.app.Activity;

import android.util.Log;

import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.Interface.OmronPeripheralManagerConnectStateListener;
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.LibraryManager.OmronPeripheralManager;
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.OmronUtility.OmronConstants;

import io.flutter.plugin.common.EventChannel;

public class StateChangesStatus  implements EventChannel.StreamHandler{



    private final   Context applicationContext;
    private final   Activity activity;

    StateChangesStatus(  Context applicationContext,Activity activity){

        this.applicationContext =applicationContext;
        this.activity=activity;
    };

    private EventChannel.EventSink scanningDevicesEvent;

     void setStateChanges() {
        // Listen to Device state changes using OmronPeripheralManager
        OmronPeripheralManager.sharedManager(applicationContext).onConnectStateChange(new OmronPeripheralManagerConnectStateListener() {
            @Override
            public void onConnectStateChange(final int state) {




              activity.  runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        String status = "-";

                        if (state == OmronConstants.OMRONBLEConnectionState.CONNECTING) {
                            status = "Connecting";
                        } else if (state == OmronConstants.OMRONBLEConnectionState.CONNECTED) {
                            status = "Connected";
                        } else if (state == OmronConstants.OMRONBLEConnectionState.DISCONNECTING) {
                            status = "Disconnecting";
                        } else if (state == OmronConstants.OMRONBLEConnectionState.DISCONNECTED) {
                            status = "Disconnected";
                        }

                        if (scanningDevicesEvent != null) {
                            scanningDevicesEvent.success(
                                    status);
                        }
                        Log.d("status of connection",  "status of connection *********"+ status);
                    }
                });


//                String status = "-";
//
//                if (state == OmronConstants.OMRONBLEConnectionState.CONNECTING) {
//                    status = "Connecting";
//                } else if (state == OmronConstants.OMRONBLEConnectionState.CONNECTED) {
//                    status = "Connected";
//                } else if (state == OmronConstants.OMRONBLEConnectionState.DISCONNECTING) {
//                    status = "Disconnecting";
//                } else if (state == OmronConstants.OMRONBLEConnectionState.DISCONNECTED) {
//                    status = "Disconnected";
//                }
//
//                if (scanningDevicesEvent != null) {
//                    scanningDevicesEvent.success(
//                            status);
//                }
//                Log.d("status of connection", status);

            }
        });
    }

    @Override
    public void onListen(Object arguments, EventChannel.EventSink events) {
        scanningDevicesEvent = events;
    }

    @Override
    public void onCancel(Object arguments) {
        scanningDevicesEvent = null;
    }
}
