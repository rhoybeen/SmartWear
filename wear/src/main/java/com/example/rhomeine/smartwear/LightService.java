package com.example.rhomeine.smartwear;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by eugenio on 10/03/16.
 */

public class LightService extends Service {

    private BluetoothAdapter mBluetoothAdapter;
    private int REQUEST_ENABLE_BT = 1;
    private static final long SCAN_PERIOD = 10000;
    private BluetoothLeScanner mLEScanner;
    private ScanSettings settings;
    private List<ScanFilter> filters;
    private BluetoothGatt mGatt;
    private BluetoothDevice btDevice;
    String TAG="LightService";
    LightUtils light_utils;


    String LIGHT_SERVICE="0000ffe5-0000-1000-8000-00805f9b34fb";
    String LIGHT_CHARACTERISTIC="0000ffe9";

    void log(String m){
        Log.i(TAG, "" + m);
    }

    private void scanLeDevice(final boolean enable) {
        Log.i(TAG, "Scan: " + enable);
        if (enable) {
            //mLEScanner.startScan(mScanCallback); //this works
            //TEst it
            //mLEScanner.startScan(mScanCallback);
           mLEScanner.startScan(filters, settings, mScanCallback);
        }
        else mLEScanner.stopScan(mScanCallback);


    }


    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {

            btDevice = result.getDevice();
            log("onLeScan try to connect to device: " + btDevice.getName());
            /*
             It needs to check if device name is a light
             */
            if(btDevice.getName().startsWith("LEDBlue")) connectToDevice(btDevice);
            else log("Search for light.");

        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult sr : results) {
                log("ScanResult - Results: " + sr.toString());
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            log("Scan Failed: Error Code: " + errorCode);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        light_utils=LightUtils.getLightUtils();
        //Define filter
        filters = new ArrayList<ScanFilter>();
        ScanFilter.Builder builder = new ScanFilter.Builder();
        ScanFilter light_service=builder.setServiceUuid(ParcelUuid.fromString(LIGHT_SERVICE)).build();
        filters.add(light_service);
        //Define scanner
        mLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
        //Define scanmode
        settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();

        (new Thread(new Runnable() {
            @Override
            public void run() {
                scanLeDevice(true);
            }
        })).start();
    }



    public void connectToDevice(BluetoothDevice device) {
        if (mGatt == null) {
            log("Connect to GATT: " + device.getName());
            mGatt = device.connectGatt(this, false, gattCallback);
            scanLeDevice(false);// will stop after first device detection
        }
    }



    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {


            log("onConnectionStateChange Status: " + newState);
            light_utils.setStatus(newState);
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    log("gattCallback STATE_CONNECTED, discoverServices");
                    gatt.discoverServices();
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    log("gattCallback STATE_DISCONNECTED");
                    break;
                default:
                    log("gattCallback STATE_OTHER");
            }

        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            List<BluetoothGattService> services = gatt.getServices();
            log("onServicesDiscovered: " + services.size());
            //gatt.readCharacteristic(services.get(1).getCharacteristics().get(0));
            // Loops through available GATT Services.
            for (BluetoothGattService gattService : services) {
                //HR Service
                log("Service: " + gattService.getUuid());
                if (gattService.getUuid().toString().equalsIgnoreCase(LIGHT_SERVICE)) {
                    // and then call
                    log("Light chars: " + gattService.getCharacteristics().size());
                    int i=0;
                    for (BluetoothGattCharacteristic characteristic : gattService.getCharacteristics()) {
                         log("Light Chars: " + characteristic.getUuid().toString()+" "+i);
                         if (characteristic.getUuid().toString().startsWith(LIGHT_CHARACTERISTIC)){
                            light_utils.setBluetoothGatt(gatt);
                            light_utils.setBluetoothGattCharacteristic(characteristic);
                            log("Now you can send commands to the light.");
                            //light_utils.sendRGBColorCommand(255, 255, 255);
                            light_utils.sendTurnOnCommand();
                            light_utils.onLightListener.onLightReady();
                            return;

                        }
                        i++;
                    }

                }
            }
        }


        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            log("onCharacteristicChanged: "+characteristic.toString());

        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic
                                                 characteristic, int status) {

            log("onCharacteristicRead: "+characteristic.toString());

        }
    };



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mLEScanner!=null){
            mLEScanner.stopScan(mScanCallback);
            mLEScanner=null;
        }
        if(mGatt!=null){
            mGatt.disconnect();
            mGatt.close();
            mGatt = null;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        //TODO for communication return IBinder implementation
        return null;
    }
}
