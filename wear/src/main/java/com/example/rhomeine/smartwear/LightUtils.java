package com.example.rhomeine.smartwear;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;

/**
 * Created by eugenio on 10/03/16.
 */
public class LightUtils {


    String TAG="LightUtils";
    BluetoothGatt gatt;
    BluetoothGattCharacteristic characteristic;

    int LIGHT_STATUS=-1;

    protected void setStatus(int s){
        LIGHT_STATUS=s;
    }

    public int getStatus(){
        return LIGHT_STATUS;
    }

    /*
    Forward behaviour to Activity
     */
    public OnLightListener onLightListener;


    // interface to pass a value to the implementing class
    public interface OnLightListener {
        void onLightReady();
    }

    /**
     * Callback for commands read/write or notify
     */
    public void setLightListener(OnLightListener listener) {
        onLightListener = listener;
    }
     /*
     End Forward behaviour to Activity
     */

    private LightUtils(){}

    private static LightUtils INSTANCE;

    public static LightUtils getLightUtils(){
        if(INSTANCE==null){
            INSTANCE=new LightUtils();
        }
        return INSTANCE;
    }

    private void log(String message){
        Log.i(TAG, "" + message);
    }

    protected void setBluetoothGatt(BluetoothGatt bg){
        gatt=bg;
    }

    protected void setBluetoothGattCharacteristic(BluetoothGattCharacteristic bg){
        characteristic=bg;
    }

    private static byte fromIntToByte(int i){
        byte b = (byte)(i & 0xFF);
        return b;
    }

    public void sendTurnOnCommand(){
        byte[] value = new byte[3];
        value[0] = (byte) (0xCC);
        value[1] = (byte) (0x23);//23 ON - 24 OFF
        value[2] = (byte) (0x33);
        characteristic.setValue(value);
        boolean stat = gatt.writeCharacteristic(characteristic);
        log("Status sendTurnOnCommand write: "+stat);
    }


    public void sendTurnOffCommand(){
        byte[] value = new byte[3];
        value[0] = (byte) (0xCC);
        value[1] = (byte) (0x24);//23 ON - 24 OFF
        value[2] = (byte) (0x33);
        characteristic.setValue(value);
        boolean stat = gatt.writeCharacteristic(characteristic);
        log("Status sendTurnOffCommand write: "+stat);

    }

    public void sendRGBColorCommand(int r,int g,int b){
        //Packet to change color
        byte[] value = new byte[7];
        value[0] = (byte) (0x56);
        value[1] = fromIntToByte(r);
        value[2] = fromIntToByte(g);
        value[3] = fromIntToByte(b);
        value[4] = (byte) (0x00);
        value[5] = (byte) (0xF0);//RGB F0 - Warm 0F
        value[6] = (byte) (0xAA);
        characteristic.setValue(value);
        boolean stat = gatt.writeCharacteristic(characteristic);
        log("Status sendRGBColorCommand write: "+stat);

    }

    public void sendWarmCommand(int w){
        //Packet to change warm
        byte[] value = new byte[7];
        value[0] = (byte) (0x56);
        value[1] = (byte) (0x00);
        value[2] = (byte) (0xFF);
        value[3] = (byte) (0x00);
        value[4] = fromIntToByte(w);
        value[5] = (byte) (0x0F);//RGB F0 - Warm 0F
        value[6] = (byte) (0xAA);
        characteristic.setValue(value);
        boolean stat = gatt.writeCharacteristic(characteristic);
        log("Status sendWarmCommand write: "+stat);

    }

    public void resetConnection(){
        log("resetConnection");
        try {
            if (gatt != null) gatt.disconnect();
        }catch(Exception e){};
    }


}
