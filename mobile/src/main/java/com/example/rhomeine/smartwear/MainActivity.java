package com.example.rhomeine.smartwear;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.HorizontalScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import android.os.Handler;

import org.json.JSONException;
import org.json.JSONObject;

import static com.google.android.gms.wearable.DataEvent.TYPE_CHANGED;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener,DataApi.DataListener,NavigationView.OnNavigationItemSelectedListener{

    private static final String ITEM_KEY_HR = "heartrate";
    private static final String ITEM_KEY_SCORE = "score";
    private static final String ITEM_KEY_TIME = "time";
    private static final String ITEM_KEY_CALORIE = "calorie";
    String LOG_TAG = "HRT_Mobile";
    private TextView textView;
    private TextView textHR;
    private TextView textGoogleApi;
    private TextView textTime;
    private TextView textScore;
    private TextView textCalorie;
    private CardView cardView;
    private Button button;
    private Switch aSwitch;
    private TextToSpeech textToSpeech;
    private MessageApi.MessageListener messageListener;
    private GoogleApiClient googleApiClient;
    private int sec_dura=30;
    private int sec_target=30;
    private static ArrayList<Integer> HRdatalist = null;
    private static ArrayList<String> HRbottomlist = null;
    private static ArrayList<ArrayList<Integer>> dataLists = null;
    private HorizontalScrollView horizontalScrollView;
    private LineView lineView;
    private int dataCount=0;
    private String[] path = {"/path_low","/path_high","/path_otime","/path_fail","/path_end"};
    private int path_count = 0;

    String filename;

    String PATH = "/hrtraining";
    private final String PATH_HR = "/heartrate";
    private final String ITEM_KEY = "heartrate";

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1:
                    aSwitch.setEnabled(false);
                    Log.i(LOG_TAG,"Disable switcher");
                    textView.setText("Please pair Mobile with your Android Watch.");
                    cardView.setVisibility(View.INVISIBLE);
                    horizontalScrollView.setVisibility(View.INVISIBLE);
                    break;
                case 2:
                    aSwitch.setEnabled(true);
                    if(aSwitch.isChecked()) {
                        cardView.setVisibility(View.VISIBLE);
                        horizontalScrollView.setVisibility(View.VISIBLE);
                    }
                    break;
            }
            postDelayed(runnable,3000);
        }
    };

    private Handler handler_msg = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.i(LOG_TAG,"handle msg");
            if(aSwitch.isChecked()) {
                switch (msg.what) {
                    case 1:
                        MessageEvent messageEvent = (MessageEvent) msg.obj;
                        handleWearableMessage(messageEvent.getPath(), messageEvent.getData());
                        break;
                    case 2:
                        ArrayList<Integer> arrayList = (ArrayList<Integer>) msg.obj;
                        Log.i(LOG_TAG, "Data: "+ arrayList.toString());
                        updateData(arrayList.get(0), arrayList.get(1), arrayList.get(2), arrayList.get(3));
                        updateDataList(arrayList.get(0), arrayList.get(2));
                }
            }
        }
    };

    Runnable runnable = new Runnable() {
        @Override
        public void run() {

            Wearable.NodeApi.getConnectedNodes(googleApiClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                @Override
                public void onResult(@NonNull NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                    final List<Node> node=getConnectedNodesResult.getNodes();
                    if(node.size()<1)
                        handler.sendEmptyMessage(1);
                    else
                        handler.sendEmptyMessage(2);
    //                Log.i(LOG_TAG,"Node size is"+Integer.toString(node.size()));
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigate);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        textView = (TextView) findViewById(R.id.textview);
        textHR = (TextView) findViewById(R.id.text_hr);
        textScore = (TextView) findViewById(R.id.text_score);
        textScore.setText("Score: --/--");
        textTime = (TextView) findViewById(R.id.text_time);
        textTime.setText("Time: --/-- mins");
        textCalorie = (TextView) findViewById(R.id.text_calorie);
        textCalorie.setText("Calorie: -- kcal");

        cardView = (CardView) findViewById(R.id.cardview);
        cardView.setVisibility(View.INVISIBLE);
        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readDataFromFile();
            }
        });

        textGoogleApi = (TextView) findViewById(R.id.text_googleapi);
        initSwitch();
        // ATTENTION: This "addApi(AppIndex.API)"was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.

        googleApiClient = new GoogleApiClient.Builder(this).addApi(Wearable.API).addApi(AppIndex.API).build();

        textToSpeech = new TextToSpeech(this, this);
        Thread thread = new Thread(runnable);
        thread.start();
        initLineChart();

        String format = "yyyyMMdd";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        filename = "data"+simpleDateFormat.format(new Date());

        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               handleWearableMessage(path[path_count],null);
                if(++path_count >= path.length)
                    path_count = 0;
            }
        });

        //start DataLayerListenerService
        DataLayerListenerService.startService(this);
        UserInfo.getUserData();
    }

    private void initLineChart(){
        HRdatalist = new ArrayList<Integer>();
        HRdatalist.add(0);
        HRbottomlist = new ArrayList<String>();
        HRbottomlist.add("0");
        horizontalScrollView = (HorizontalScrollView) findViewById(R.id.horizontalScrollView);
        lineView= (LineView) findViewById(R.id.line_view);
        lineView.setBottomTextList(HRbottomlist);
        lineView.setDrawDotLine(true);
        lineView.setShowPopup(LineView.SHOW_POPUPS_All);
        dataLists = new ArrayList<ArrayList<Integer>>();
        dataLists.add(HRdatalist);
        lineView.setDataList(dataLists);
        horizontalScrollView.fullScroll(View.FOCUS_RIGHT);
        horizontalScrollView.setVisibility(View.INVISIBLE);
    }

    public void resetView(){
        textScore.setText("Score: --/--");
        textTime = (TextView) findViewById(R.id.text_time);
        textTime.setText("Time: --/-- mins");
        HRdatalist = new ArrayList<Integer>();
        HRdatalist.add(0);
        HRbottomlist = new ArrayList<String>();
        HRbottomlist.add("0");
        lineView.setBottomTextList(HRbottomlist);
        dataLists = new ArrayList<ArrayList<Integer>>();
        dataLists.add(HRdatalist);
        lineView.setDataList(dataLists);
    }


    private void updateDataList(int value,int count){

        if(count>(dataCount+1)){
            while(++dataCount<count){
                HRdatalist.add(0);
                String time = Integer.toString(dataCount/60)+"'"+Integer.toString(dataCount%60);
                HRbottomlist.add(time);
            }
            Log.i(LOG_TAG,"Fill the missing data");
        }
        dataCount = count;
        String time = Integer.toString(count/60)+"'"+Integer.toString(count%60);
        HRbottomlist.add(time);
        HRdatalist.add(value);

        lineView.setBottomTextList(HRbottomlist);
        lineView.setDataList(dataLists);
        horizontalScrollView.fullScroll(View.FOCUS_RIGHT);
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(aSwitch.isChecked()) {

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        Wearable.MessageApi.removeListener(googleApiClient,messageListener);
//        Wearable.DataApi.removeListener(googleApiClient,MainActivity.this);
        googleApiClient.disconnect();
        textToSpeech.shutdown();
        stopService(new Intent(this,DataLayerListenerService.class));
    }

    @Override
    public void onInit(int status) {
        if(status == TextToSpeech.SUCCESS){
            Log.i("Mobile","TTS onInit "+textToSpeech.getLanguage().toString());
        }
    }

    public void initSwitch(){
        aSwitch = (Switch) findViewById(R.id.switcher);
        aSwitch.setChecked(false);
        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    googleApiClient.connect();
//                    Wearable.MessageApi.addListener(googleApiClient,messageListener);
//                    Wearable.DataApi.addListener(googleApiClient,MainActivity.this);
                    if(googleApiClient.isConnecting()){
                        textGoogleApi.setText("GoogleApiClient Connected!");
                    }
                    else {
                        textGoogleApi.setText("Connection failed. Please check it!");
                    }
                    cardView.setVisibility(View.VISIBLE);
                    horizontalScrollView.setVisibility(View.VISIBLE);
                    resetView();
                }
                else{
//                    Wearable.MessageApi.removeListener(googleApiClient,messageListener);
//                    Wearable.DataApi.removeListener(googleApiClient,MainActivity.this);
                    googleApiClient.disconnect();
                    textGoogleApi.setText("Switch on to receivce Wear messages.");
                    cardView.setVisibility(View.INVISIBLE);
                    horizontalScrollView.setVisibility(View.INVISIBLE);
                }


            }
        });
    }

    public void handleWearableMessage(String path,byte[] data){
        switch (path){
            case "/path_low":
                textToSpeech.speak(getString(R.string.sound_low_HR1),TextToSpeech.QUEUE_FLUSH,null);break;
            case "/path_high":
                textToSpeech.speak(getString(R.string.sound_high_HR1),TextToSpeech.QUEUE_FLUSH,null);break;
            case "/path_otime":
                textToSpeech.speak(getString(R.string.sound_time_overceed1),TextToSpeech.QUEUE_FLUSH,null);break;
            case "/path_fail":
                textToSpeech.speak(getString(R.string.sound_section_failed1),TextToSpeech.QUEUE_FLUSH,null);break;
            case "/path_success":
                textToSpeech.speak(getString(R.string.sound_goal_achieved1),TextToSpeech.QUEUE_FLUSH,null);break;
            case "/path_end":
                textToSpeech.speak(getString(R.string.sound_section_end1),TextToSpeech.QUEUE_FLUSH,null);break;
            case "/path_start":
                textToSpeech.speak(getString(R.string.sound_section_start1),TextToSpeech.QUEUE_FLUSH,null);break;
            case "/sec_dura":
                int dura = byteArrayToInt(data);
                sec_dura = dura;
                break;
            case "/sec_target":
                int target = byteArrayToInt(data);
                sec_target = target;
                break;
            case "/direct_quit":
                textToSpeech.speak(getString(R.string.sound_section_dir_quit),TextToSpeech.QUEUE_FLUSH,null);
                stopMobileMode();
                resetView();
                break;
            case "/section":
                try{
                    JSONObject jsonObject = new JSONObject(new String(data));
                    writeToFile(jsonObject);
                    sendMessageToWearable("/ack",null);
                }catch (JSONException e){
                    Log.i(LOG_TAG,"on receiving json object error");
                }
                break;
            default:break;
        }
    }

    private void stopMobileMode(){

    }

    public int byteArrayToInt(byte[] b)
    {
        int value = 0;
        for (int i = 0; i < 4; i++) {
            int shift = (4 - 1 - i) * 8;
            value += (b[i] & 0x000000FF) << shift;
        }
        return value;
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {
        for (DataEvent event : dataEventBuffer) {
            if (event.getType() == TYPE_CHANGED) {
                // DataItem changed
                DataItem item = event.getDataItem();
                if (item.getUri().getPath().compareTo(PATH_HR) == 0) {
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                    updateData(dataMap.getInt(ITEM_KEY_HR),dataMap.getInt(ITEM_KEY_SCORE),dataMap.getInt(ITEM_KEY_TIME),dataMap.getInt(ITEM_KEY_CALORIE));
                    updateDataList(dataMap.getInt(ITEM_KEY_HR),dataMap.getInt(ITEM_KEY_TIME));
                }
            } else if (event.getType() == DataEvent.TYPE_DELETED) {
                // DataItem deleted
            }
        }
        dataEventBuffer.release();
    }

    public void updateData(int hr,int score,int count,int calorie){

        textHR.setText(Integer.toString(hr));
        textScore.setText("Score: "+Integer.toString(score)+" /"+sec_target);

        int left = count;
        int mins = count / 60;
        int secs = count % 60;
        textTime.setText("Time: "+Integer.toString(mins) + "'" + Integer.toString(secs)+" /"+sec_dura+" mins");

        int calorie_kcal = (int) calorie/1000;
        textCalorie.setText("Calorie: "+Integer.toString(calorie_kcal)+" kcal");

        String data = Integer.toString(hr)+ "\n";
        FileOutputStream outputStream;
        try {
            outputStream = openFileOutput(filename, Context.MODE_APPEND);
            outputStream.write(data.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String readDataFromFile(){
        Log.i(LOG_TAG,"file name is:"+filename);
        String ret = "";
        try {
            InputStream inputStream = openFileInput(filename);

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
                Log.i(LOG_TAG,Integer.toString(ret.length()));
                Log.i(LOG_TAG,ret);
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return ret;
    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.navigate, menu);
        return true;
    }

    private boolean sendMessageToWearable(final String message, final byte[] value) {

        if (googleApiClient == null)
            return false;

        Log.d("GoogleApiClient","sending a message to handheld: "+message);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                // use the api client to send the heartbeat value to our handheld
                final PendingResult<NodeApi.GetConnectedNodesResult> nodes = Wearable.NodeApi.getConnectedNodes(googleApiClient);
                nodes.setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                    @Override
                    public void onResult(NodeApi.GetConnectedNodesResult result) {
                        final List<Node> nodes = result.getNodes();
                        if (nodes != null) {
                            for (int i=0; i<nodes.size(); i++) {
                                final Node node = nodes.get(i);
                                PendingResult results = Wearable.MessageApi.sendMessage(googleApiClient, node.getId(), message, value);
                            }
                        }
                    }
                });

            }
        });
        thread.start();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_user) {
            // Handle the camera action
            startActivity(new Intent(MainActivity.this,UserProfileActivity.class));
        } else if (id == R.id.nav_statics) {

        } else if (id == R.id.nav_schedule) {

        } else if (id == R.id.nav_likeus) {

        }else if(id == R.id.nav_setting){

        }else if (id == R.id.nav_share) {
            startActivity(new Intent(MainActivity.this,TestActivity.class));
        } else if (id == R.id.nav_send) {
            sendMailToAuthor();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void sendMailToAuthor(){
        Intent mail = new Intent(Intent.ACTION_SENDTO);
        mail.setData(Uri.parse("mailto:luomingtibo@gmail.com"));
        mail.putExtra(Intent.EXTRA_SUBJECT,"App: Heart-rate Training Feedback");
        mail.putExtra(Intent.EXTRA_TEXT,"Please write your opinion here...\n");
        startActivity(mail);
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.

        googleApiClient.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.example.rhomeine.smartwear/http/host/path")
        );
        AppIndex.AppIndexApi.start(googleApiClient, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.example.rhomeine.smartwear/http/host/path")
        );
        AppIndex.AppIndexApi.end(googleApiClient, viewAction);
    }

    public void writeToFile(JSONObject json){

        String filename = "unknown";
        try {
            filename = json.getJSONObject("SEC").getString("START_DATE")+".json";
            Log.i(LOG_TAG,"JSON filename:"+filename);
            Log.i(LOG_TAG,"JSON content"+json.toString());
            Toast.makeText(this,filename,Toast.LENGTH_SHORT).show();
        }catch (JSONException e){
            Log.i(LOG_TAG,"JSON exception");
        }
        FileOutputStream outputStream;
        try {
            outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(json.toString().getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Handler getHandler(){
        return this.handler_msg;
    }
}
