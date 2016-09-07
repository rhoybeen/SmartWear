package com.example.rhomeine.smartwear;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.support.wearable.view.DismissOverlayView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import android.os.Handler;

public class TrainingActivity extends WearableActivity implements HeartBeatService.OnChangeListener, LightUtils.OnLightListener{

    private static final SimpleDateFormat AMBIENT_DATE_FORMAT =
            new SimpleDateFormat("HH:mm", Locale.US);

    private String LOGTAG = "Built-in";
    private int HRvalue = 0;
    private BoxInsetLayout mContainerView;
    private TextView mClockView;
    private TextView mTextView;
    private TextView mSection;
    private static TextView mTextMins;
    private static TextView mScore;
    private static TextView mTime;
    private TextView mBPM;

    private ImageView img_ok;
    private ImageView img_up;
    private ImageView img_down;

    private LinearLayout linearLayout;
    private ProgressBar proBar;
    private static ProgressBar progressBar;
    private static ProgressBar progressBar_score;
    private static String target;
    private static final String LOG_TAG = "MyHeart";
    private static RatingBar ratingBar;
    LightUtils light_manager;
    public static final int MODE_MAX = 6;
    public static final int MODE_HARD = 5;
    public static final int MODE_MODERATE = 4;
    public static final int MODE_LIGHT = 3;
    public static final int MODE_VERY_LIGHT = 2;
    public static final int MODE_PEACE = 1;

    private DismissOverlayView dismissOverlayView;
    private GestureDetector mDetector;

    boolean isLight_available = false;
    boolean isLightChangeable = false;
    boolean isHRsensor_available = false;
    android.os.Handler messenger = new 	android.os.Handler();
    android.os.Handler messenger1 = new android.os.Handler();

    //service management
    ServiceConnection connection;
    boolean isBound = false;

    Runnable chech_light=new Runnable() {
        @Override
        public void run() {
            Log.i("Runnable", "Messenger run");
            if(!isLight_available){
                Log.i("Runnable", "No light");
                isLightChangeable = false;
                light_manager.resetConnection();
                startService(new Intent(getApplicationContext(), LightService.class));
            }else isLightChangeable = true;

            if(isLight_available&&isLightChangeable) {
                try {
                    UpdateLightColor(HRvalue);
                    Log.i("Handler", "update light color");
                } catch (Exception e) {
                    Log.i("Handler", "update light color error");
                }
            }
            messenger.postDelayed(chech_light,5000);
        }
    };
    Runnable chech_HRsensor=new Runnable() {
        @Override
        public void run() {
            Log.i("Runnable","Messenger1 run");
            if(isHRsensor_available){
                Log.i("Runnable", "HRsensor is initiated now; HRvalue" + HeartRateTraining.getHRValue());
                proBar.setVisibility(View.INVISIBLE);
                linearLayout.setVisibility(View.VISIBLE);
                HeartRateTraining.StartTrainingSection(TrainingActivity.this);
                mSection.setText("Section/Week: " + Integer.toString(HeartRateTraining.getSecLength()) + "/" + Integer.toString(HeartRateTraining.getSecNum()));
                target = Integer.toString(HeartRateTraining.getTarget());
                mScore.setText("Score/Target: 0"+"/"+target);
                messenger1.removeCallbacksAndMessages(null);
            }else {
                Log.i("Runnable", "HRsensor not initiated");
                messenger1.postDelayed(chech_HRsensor, 4000);
            }
        }
    };
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    public void onLightReady() {
        Log.i("LightService", "onLightReady");
        Log.i("LightService", "onLightReady");

        isLight_available = true;
        isLightChangeable = true;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.built_in_sensor_layout);
        setAmbientEnabled();
        Log.i("Lifecycle", "onCreate");
        mContainerView = (BoxInsetLayout) findViewById(R.id.container);
        mClockView = (TextView) findViewById(R.id.clock);
        mTextView = (TextView) findViewById(R.id.heartbeat);
        mScore = (TextView) findViewById(R.id.text_score);
        mSection = (TextView) findViewById(R.id.text_section);
        mTime = (TextView) findViewById(R.id.text_time);
        mTime.setText("0'0");
        mBPM = (TextView) findViewById(R.id.text_bpm);
        mTextMins = (TextView) findViewById(R.id.text_min);
        mTextMins.setText(Integer.toString(HeartRateTraining.getSecDuration()) + "'");
        linearLayout = (LinearLayout) findViewById(R.id.linear_1);
        linearLayout.setVisibility(View.INVISIBLE);
        img_ok = (ImageView) findViewById(R.id.icon_ok);
        img_ok.setVisibility(View.INVISIBLE);
        img_up = (ImageView) findViewById(R.id.icon_up);
        img_up.setVisibility(View.INVISIBLE);
        img_down = (ImageView) findViewById(R.id.icon_down);
        img_down.setVisibility(View.INVISIBLE);

        proBar = (ProgressBar) findViewById(R.id.proBar_circle);
        proBar.setVisibility(View.VISIBLE);

        progressBar = (ProgressBar) findViewById(R.id.proBar);
        progressBar.setProgress(0);
        progressBar.setMax(60 * HeartRateTraining.getSecDuration());

        progressBar_score = (ProgressBar) findViewById(R.id.proBar_score);
        progressBar_score.setProgress(0);
        progressBar_score.setMax(HeartRateTraining.getTarget());

        ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        ratingBar.setRating(3);

        dismissOverlayView = (DismissOverlayView) findViewById(R.id.dismissOverlay);
        dismissOverlayView.setIntroText("Quit the app?");
        dismissOverlayView.showIntroIfNecessary();


        light_manager = LightUtils.getLightUtils();
        light_manager.setLightListener(this);

        mDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            public void onLongPress(MotionEvent ev) {
                dismissOverlayView.show();
            }
        });
        // bind to our service.
        connection=new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder binder) {
                Log.d(LOG_TAG, "connected to service.");
                // set our change listener to get change events
                ((HeartBeatService.HeartbeatServiceBinder) binder).setChangeListener(TrainingActivity.this);
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                Log.d(LOG_TAG, "service disconnected.");
            }
        };

        isBound=getApplicationContext().bindService(new Intent(TrainingActivity.this, HeartBeatService.class),connection , Service.BIND_AUTO_CREATE);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
        messenger1.post(chech_HRsensor);

        mSection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HeartRateTraining.AdjustScores(2);
            }
        });
        mBPM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HeartRateTraining.AdjustCount(10);
            }
        });
        mTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HeartRateTraining.AdjustCount(120);
            }
        });
    }

    Random r = new Random(255);

    public static Handler myHandler = new android.os.Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    progressBar_score.setProgress(msg.arg1);
                    mScore.setText("Score:\n" + Integer.toString(msg.arg1) + "/" + target);

                    progressBar.setProgress(msg.arg2);
                    int left = msg.arg2;
                    int mins = left / 60;
                    int secs = left % 60;
                    mTime.setText(Integer.toString(mins) + "'" + Integer.toString(secs));
                    break;
                case 1:
                    progressBar.setMax(60*msg.arg1);
                    mTextMins.setText(Integer.toString(msg.arg1)+"'");
                    ChangeRating(msg.arg2);
                    break;
                case 2:
                    break;
                default:
                    break;

            }
        }
    };


    public static void ChangeRating(int stage){
        switch (stage){
            case 0:
                ratingBar.setNumStars(3);
                break;
            case 1:
                ratingBar.setNumStars(2);
                break;
            case 2:
                ratingBar.setNumStars(1);
                break;
            default:break;
        }
        Log.i(LOG_TAG,"Rating now is "+Integer.toString((int)ratingBar.getRating()));
    }
    @Override
    public void onValueChanged(int newValue) {
        Log.i("onValueChanged", "" + newValue + " ts: " + SystemClock.currentThreadTimeMillis());
        mTextView.setText(Integer.toString(newValue));
//        if (!isLight_available) {
//            Log.i("onValueChanged", "No light is available");
//        }
        HRvalue = newValue;
//        mTextView.setTextColor(HeartRateTraining.ChangeTextColor(newValue));
        HeartRateTraining.setHRValue(newValue);
        if(newValue!=0) {
            isHRsensor_available = true;
            UpdateIndicator(newValue);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.i("Lifecycle", "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i("Lifecycle", "onPause");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.i("Lifecycle", "onStart");
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "BuiltinSensor Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.example.rhomeine.smartwear/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
        startService(new Intent(this, LightService.class));
        messenger.postDelayed(chech_light, 10000);


    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i("Lifecycle", "onStop");
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "BuiltinSensor Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.example.rhomeine.smartwear/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i("Lifecycle", "onRestart");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("Lifecycle", "onDestroy");

        HeartRateTraining.finish();
        light_manager.resetConnection();
        stopService(new Intent(this, LightService.class));
        isLight_available = false;
        messenger.removeCallbacks(chech_light);
        messenger1.removeCallbacks(chech_HRsensor);

        if (isBound) getApplicationContext().unbindService(connection);
    }

    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);
        updateDisplay();
    }

    @Override
    public void onUpdateAmbient() {
        super.onUpdateAmbient();
        updateDisplay();
    }

    @Override
    public void onExitAmbient() {
        updateDisplay();
        super.onExitAmbient();
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        dismissOverlayView.bringToFront();
        return mDetector.onTouchEvent(ev) || super.onTouchEvent(ev);
    }

    private void updateDisplay() {
        if (isAmbient()) {
            mContainerView.setBackgroundColor(getResources().getColor(android.R.color.black));
            mClockView.setVisibility(View.VISIBLE);

            mClockView.setText(AMBIENT_DATE_FORMAT.format(new Date()));
        } else {
            mContainerView.setBackground(getDrawable(R.drawable.bg03));
            mClockView.setVisibility(View.GONE);
        }
    }

    public void UpdateLightColor(int newValue) {

        int div=UserInfo.getMaxHeartRate();
        int heartRateLevel=50;
        float temp_value=(newValue / div) * 10;
        heartRateLevel = Math.round(temp_value);

        Log.i("UpdateLightColor", "HR level is" + heartRateLevel);
        switch (HeartRateTraining.HRValueToZoneNum(newValue)) {
            case HeartRateTraining.ZONE_2:
                light_manager.sendRGBColorCommand(0xff, 0x00, 0x00);break;
            case HeartRateTraining.ZONE_1:
                light_manager.sendRGBColorCommand(0xff, 0xff, 0x00);break;
            case HeartRateTraining.ZONE_0:
                light_manager.sendRGBColorCommand(0x00, 0x80, 0xff);break;
            default:
                light_manager.sendRGBColorCommand(0xff, 0xff, 0xff);break;
        }
    }

    public void UpdateIndicator(int newValue){
        int zone = HeartRateTraining.HRValueToZoneNum(newValue);
        switch (zone){
            case HeartRateTraining.ZONE_MAX:
                img_ok.setVisibility(View.INVISIBLE);
                img_up.setVisibility(View.INVISIBLE);
                img_down.setVisibility(View.VISIBLE);
                break;
            case HeartRateTraining.ZONE_2:
            case HeartRateTraining.ZONE_1:
                img_down.setVisibility(View.INVISIBLE);
                img_up.setVisibility(View.INVISIBLE);
                img_ok.setVisibility(View.VISIBLE);
                break;
            case HeartRateTraining.ZONE_0:
                img_ok.setVisibility(View.INVISIBLE);
                img_down.setVisibility(View.INVISIBLE);
                img_up.setVisibility(View.VISIBLE);
                break;
        }
    }

}
