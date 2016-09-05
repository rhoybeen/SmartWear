package com.example.rhomeine.smartwear;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.google.android.gms.wearable.DataEvent.TYPE_CHANGED;

public class DataLayerListenerService extends WearableListenerService {

    private GoogleApiClient mGoogleApiClient;
    private String LOG_TAG = "DataLayerListener";
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
        con.startService(new Intent(con,DataLayerListenerService.class));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(LOG_TAG,"#onStartCommand");
        return super.onStartCommand(intent, flags, startId);
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
                SyncUserInfo.receiveUserInfo(messageEvent.getData());
                Log.i(LOG_TAG,"handle userinfo message");
                break;
            default:
                break;
        }
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents)
    {
        Log.d(LOG_TAG, "##DataService Data changed");
    }

    public void syncTrainingDataWithHandheld()
    {

    }

}