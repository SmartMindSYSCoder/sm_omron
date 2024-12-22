package com.sm.sm_omron;

import static com.sm.sm_omron.OmronManager.mSelectedPeripheral;

import androidx.annotation.NonNull;
import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

import java.util.HashMap;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

/** SmOmronPlugin */
public class SmOmronPlugin implements FlutterPlugin, MethodCallHandler,ActivityAware {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private MethodChannel channel;

  private EventChannel scanEventChannel;
  private EventChannel statusEventChannel;


  public Context applicationContext;
  public Activity activity;
  public DevicesList devicesList;
  public PermissionHelper permissionHelper;
  public StateChangesStatus stateChangesStatus;
public  ScanningDevices scanningDevices;
public  WeightScale weightScale;
public   MethodChannel.Result result;

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "sm_omron");
    statusEventChannel = new EventChannel(flutterPluginBinding.getBinaryMessenger(), "sm_omron_status");
    scanEventChannel = new EventChannel(flutterPluginBinding.getBinaryMessenger(), "sm_omron_scan");


    channel.setMethodCallHandler(this);
    this.applicationContext = flutterPluginBinding.getApplicationContext();




  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {

    this.result=result;


    if (call.method.equals("getPlatformVersion")) {
      result.success("Android " + android.os.Build.VERSION.RELEASE);
    }


    else if(call.method.equals("checkBluetoothPermissions")){


      permissionHelper.checkPermissions();



    }
    else if(call.method.equals("checkRecordPermissions")){


      permissionHelper.checkRecordAudioPermission();



    }


    else if(call.method.equals("isPermissionsGranted")){


      permissionHelper.isPermissionsGranted();



    }

    else if(call.method.equals("getDevicesList")){

      if( permissionHelper.isPermissionsGranted()) {


          devicesList.initializeFun(result);


      }

      else{

        permissionHelper.checkPermissions();
        Toast.makeText(applicationContext, " Permission not granted\nPlease check permission first", Toast.LENGTH_SHORT).show();

      }

    }
    else if(call.method.equals("initScan")){

      if( permissionHelper.isPermissionsGranted()) {

        OmronManager.device=(HashMap<String, String>) call.arguments();
        scanningDevices.initializeFun(stateChangesStatus);

//        scanningDevices.startOrStopScanning(result);

       //  new  AutoTransferData(result,applicationContext).initializeFun();

      }

      else{

        permissionHelper.checkPermissions();
        Toast.makeText(applicationContext, " Permission not granted\nPlease check permission first", Toast.LENGTH_SHORT).show();

      }

    }
    else if(call.method.equals("weight")){

      if( permissionHelper.isPermissionsGranted()) {

        OmronManager.device=(HashMap<String, String>) call.arguments();

        weightScale.init(result);

      }

      else{

        permissionHelper.checkPermissions();
        Toast.makeText(applicationContext, " Permission not granted\nPlease check permission first", Toast.LENGTH_SHORT).show();

      }

    }
    else if(call.method.equals("scan")){

      if( permissionHelper.isPermissionsGranted()) {

        OmronManager.device=(HashMap<String, String>) call.arguments();
        scanningDevices.initializeFun(stateChangesStatus);

        scanningDevices.startOrStopScanning(result);

       //  new  AutoTransferData(result,applicationContext).initializeFun();

      }

      else{

        permissionHelper.checkPermissions();
        Toast.makeText(applicationContext, " Permission not granted\nPlease check permission first", Toast.LENGTH_SHORT).show();

      }

    }
    else if(call.method.equals("connect")){

      if( permissionHelper.isPermissionsGranted()) {

        OmronManager.device=(HashMap<String, String>) call.arguments();
        scanningDevices.initializeFun(stateChangesStatus);

        scanningDevices.startOrStopScanning(result);





                            ConnectDevice connectDevice=new ConnectDevice(applicationContext,mSelectedPeripheral);

                            connectDevice.connectPeripheral();


       //  new  AutoTransferData(result,applicationContext).initializeFun();

      }

      else{

        permissionHelper.checkPermissions();
        Toast.makeText(applicationContext, " Permission not granted\nPlease check permission first", Toast.LENGTH_SHORT).show();

      }

    }
    else if(call.method.equals("autoTransferData")){
      HashMap<String, Object> map;

      map =(HashMap<String, Object>) call.arguments();

      String uuid=(String) map.get("uuid");
      String localName=(String) map.get("localName");

      if(map.get("uuid").equals("MC-280B-E")){



        if (permissionHelper.isRecordAudioPermissionGranted()) {


          new AutoTransferData(result, applicationContext).initializeFun(localName,uuid);

        } else {
          Toast.makeText(applicationContext, " Permission not granted\nPlease check permission first", Toast.LENGTH_SHORT).show();

          permissionHelper.checkRecordAudioPermission();

        }

      }

      else {


        if (permissionHelper.isPermissionsGranted()) {


          new AutoTransferData(result, applicationContext).initializeFun(localName,uuid);

        } else {

          Toast.makeText(applicationContext, " Permission not granted\nPlease check permission first", Toast.LENGTH_SHORT).show();

        }
      }
    }



    else {
      result.notImplemented();
    }
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    channel.setMethodCallHandler(null);
  }






  @Override
  public void onAttachedToActivity(ActivityPluginBinding activityPluginBinding) {
    // TODO: your plugin is now attached to an Activity
//    this.activity = activityPluginBinding.getActivity();
    this.activity = activityPluginBinding.getActivity();
    permissionHelper = new PermissionHelper(activity, applicationContext);

    devicesList = new DevicesList(activity, applicationContext);
    scanningDevices = new ScanningDevices(activity, applicationContext);
    scanEventChannel.setStreamHandler(scanningDevices);
    stateChangesStatus=new StateChangesStatus(applicationContext,activity);
    weightScale=new WeightScale(applicationContext,activity);
    statusEventChannel.setStreamHandler(stateChangesStatus);

  }

  @Override
  public void onDetachedFromActivityForConfigChanges() {
    // This call will be followed by onReattachedToActivityForConfigChanges().
  }

  @Override
  public void onReattachedToActivityForConfigChanges(ActivityPluginBinding activityPluginBinding) {
  }

  @Override
  public void onDetachedFromActivity() {

  }


}
