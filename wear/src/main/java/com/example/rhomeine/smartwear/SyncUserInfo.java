package com.example.rhomeine.smartwear;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Map;

/**
 * Created by rhomeine on 16/5/9.
 */
public class SyncUserInfo {

    public static GoogleApiClient mGoogleApiClient;
    public static String LOG_TAG = "SyncUserInfo";

    public static void sendUserInfo(Context con){
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        try{
            ObjectOutputStream out = new ObjectOutputStream(bytesOut);
            if(UserInfo.getUserData() == null) return;
            out.writeObject(UserInfo.getUserData());
            byte[] bytes = bytesOut.toByteArray();
            sendMessageToHandheld("/userinfo",bytes,con);
        }catch (IOException E){
            Log.i(LOG_TAG,"IO Exception");
        }
    }

    public static void receiveUserInfo(byte[] bytes){
        ByteArrayInputStream bytesInput = new ByteArrayInputStream(bytes);
        try{
            ObjectInput objectInput = new ObjectInputStream(bytesInput);
            try{
                Map<String,String> userMap = (Map<String,String>) objectInput.readObject();
                long timestamp = Long.valueOf(userMap.get("Timestamp"));
                long tmp = UserInfo.getTimeStamp()-timestamp;
                if(tmp<0){
                    UserInfo.setAge(Integer.valueOf(userMap.get("Age")));
                    UserInfo.setHeight_cm(Integer.valueOf(userMap.get("Height")));
                    UserInfo.setWeight_kg(Integer.valueOf(userMap.get("Weight")));
                    UserInfo.setGender(userMap.get("Gender"));
                    UserInfo.setTimeStamp(timestamp);
                }
            }catch (ClassNotFoundException e){
                Log.i(LOG_TAG,"Class Not Found Exception");
            }
        }catch (IOException e){
            Log.i(LOG_TAG,"IO Exception");
        }
    }

    public static boolean sendMessageToHandheld(final String path, final byte[] data,Context con){


        mGoogleApiClient = new GoogleApiClient.Builder(con).addApi(Wearable.API).build();
        mGoogleApiClient.connect();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                // use the api client to send the heartbeat value to our handheld
                final PendingResult<NodeApi.GetConnectedNodesResult> nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient);
                nodes.setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                    @Override
                    public void onResult(NodeApi.GetConnectedNodesResult result) {
                        final List<Node> nodes = result.getNodes();
                        if (nodes != null) {
                            for (int i=0; i<nodes.size(); i++) {
                                final Node node = nodes.get(i);
                                PendingResult results = Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(), path, data);
                            }
                        }
                    }
                });

            }
        });
        thread.start();
        return true;
    }

}
