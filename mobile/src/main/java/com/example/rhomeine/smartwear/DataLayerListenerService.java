package com.example.rhomeine.smartwear;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.security.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.google.android.gms.wearable.DataEvent.TYPE_CHANGED;

public class DataLayerListenerService extends WearableListenerService {

    private GoogleApiClient mGoogleApiClient;
    private SharedPreferences prefs;
    private String LOG_TAG = "DataLayerListener";
    private static MainActivity mainActivity;
    private final String PATH_HR = "/heartrate";
    private static final String ITEM_KEY_HR = "heartrate";
    private static final String ITEM_KEY_SCORE = "score";
    private static final String ITEM_KEY_TIME = "time";
    private static final String ITEM_KEY_CALORIE = "calorie";

    @Override
    public void onCreate()
    {
        Log.d(LOG_TAG, "##DataService created");
        super.onCreate();
    }

    public static void startService(Context con){
        mainActivity = (MainActivity) con;
        con.startService(new Intent(con,DataLayerListenerService.class));
    }

    @Override
    public void onDestroy()
    {
        Log.d(LOG_TAG, "##DataService destroyed");
        super.onDestroy();
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent)
    {
        Log.d(LOG_TAG, "##DataService received " + messageEvent.getPath());

        String path = messageEvent.getPath();
        switch (path){
            case "/userinfo":
                syncUserInfo(messageEvent.getData());
                Log.i(LOG_TAG,"handle /userinfo message");
                break;
            default:
                if(mainActivity!=null){
                    Handler handler = mainActivity.getHandler();
                    android.os.Message message = new android.os.Message();
                    message.what = 1;
                    message.obj = messageEvent;
                    handler.sendMessage(message);}
                else Log.i(LOG_TAG,"mainactivity is null");
                break;
        }


    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents)
    {
        Log.d(LOG_TAG, "##DataService Data changed");
        if(mainActivity!=null) {

            final ArrayList<DataEvent> events = new ArrayList<DataEvent>();
            for (DataEvent event : dataEvents) {
                events.add(event.freeze());
            }
            dataEvents.release();
            for (DataEvent event : events) {
                if (event.getType() == TYPE_CHANGED) {
                    // DataItem changed
                    DataItem item = event.getDataItem();
                    if (item.getUri().getPath().compareTo(PATH_HR) == 0) {
                        DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();

                        ArrayList<Integer> array = new ArrayList<>();
                        array.add(dataMap.getInt(ITEM_KEY_HR));
                        array.add(dataMap.getInt(ITEM_KEY_SCORE));
                        array.add(dataMap.getInt(ITEM_KEY_TIME));
                        array.add(dataMap.getInt(ITEM_KEY_CALORIE));
                        Handler handler = mainActivity.getHandler();
                        android.os.Message message = new android.os.Message();
                        message.what = 2;
                        message.obj = array;
//                        updateDataList(dataMap.getInt(ITEM_KEY_HR),dataMap.getInt(ITEM_KEY_TIME));
                        handler.sendMessage(message);
//                        Log.i(LOG_TAG,"onDataChanged");
                    }
                } else if (event.getType() == DataEvent.TYPE_DELETED) {
                    // DataItem deleted
                }
            }


        }else
            Log.i(LOG_TAG,"mainactivity is null");
    }

    public void syncUserInfo(byte[] bytes){

        ByteArrayInputStream bytesInput = new ByteArrayInputStream(bytes);
        try{
            ObjectInput objectInput = new ObjectInputStream(bytesInput);
            try{
                Map<String,String> userMap = (Map<String,String>) objectInput.readObject();
                long timestamp = Long.valueOf(userMap.get("Timestamp"));
                long tmp = UserInfo.getTimeStamp()-timestamp;
                if(tmp>0){
                    ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
                    try{
                        ObjectOutputStream out = new ObjectOutputStream(bytesOut);
                        out.writeObject(UserInfo.getUserData());
                        byte[] data = bytesOut.toByteArray();
                        sendMessageWearable("/userinfo",data);
                    }catch (IOException E){
                        Log.i(LOG_TAG,"IO Exception");
                    }
                }else if(tmp<0){
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

    public boolean sendMessageWearable(final String path, final byte[] data){

        if (mGoogleApiClient == null){
            mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(Wearable.API).build();
            mGoogleApiClient.connect();
        }

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