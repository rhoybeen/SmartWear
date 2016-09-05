package com.example.rhomeine.smartwear;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.support.wearable.view.CircledImageView;
import android.support.wearable.view.DismissOverlayView;
import android.support.wearable.view.WearableListView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.johnpersano.supertoasts.SuperActivityToast;
import com.github.johnpersano.supertoasts.SuperToast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class MainActivity extends WearableActivity{

    private static final SimpleDateFormat AMBIENT_DATE_FORMAT =
            new SimpleDateFormat("HH:mm", Locale.US);

    private BoxInsetLayout mContainerView;
    //private TextView mTextView;
    private TextView mClockView;
    private TextView textGo;
    private LinearLayout linearLayout;
    private Button button_setting;
    private Button button_profile;
    private Button button_trace;
    private CircledImageView circledImageView;
    private final int REQUEST_CODE = 200;
    private Map<String,String> userInfo;
    private String LOG_TAG = "MAIN ACTIVITY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setAmbientEnabled();

        mContainerView = (BoxInsetLayout) findViewById(R.id.container);
        mClockView = (TextView) findViewById(R.id.clock);
        initUI();
        if(!UserInfo.init_flag){
            enableUI(false);
            Intent intent = new Intent(this,UserInitActivity.class);
            startActivityForResult(intent,REQUEST_CODE);
        }else {
            enableUI(true);
            Log.i(LOG_TAG,"get UserData");
        //    userInfo = UserInfo.getUserData();
            SyncUserInfo.sendUserInfo(this);
            startService(new Intent(this,DataLayerListenerService.class));
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_CODE){
            if(resultCode != 0){
                UserInfo.initUser(data.getIntExtra("AGE",0));
                HeartRateTraining.initUser(this);
//                CreateToast(R.drawable.hearticon,"Welcome",R.color.grey_0);
                Toast.makeText(MainActivity.this,"Welcome!",Toast.LENGTH_SHORT).show();
                enableUI(true);
            }
        }
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

    private void updateDisplay() {
        if (isAmbient()) {
            mContainerView.setBackgroundColor(getResources().getColor(android.R.color.black));
        //    mTextView.setTextColor(getResources().getColor(android.R.color.white));
            mClockView.setVisibility(View.VISIBLE);

            mClockView.setText(AMBIENT_DATE_FORMAT.format(new Date()));
        } else {
            mContainerView.setBackground(getDrawable(R.drawable.stats));
            mClockView.setVisibility(View.GONE);
        }
    }

    private void initUI(){
        textGo = (TextView) findViewById(R.id.text_Go);
        linearLayout = (LinearLayout) findViewById(R.id.linear_layout);
        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,UserInitActivity.class);
                startActivityForResult(intent,REQUEST_CODE);
            }
        });
        linearLayout.setClickable(false);
        circledImageView = (CircledImageView) findViewById(R.id.btn_quick_start);
        circledImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //An quick start
                Intent intent = new Intent(MainActivity.this,BuiltinSensorActivity.class);
                startActivity(intent);
            }
        });

        button_setting = (Button) findViewById(R.id.btn_setting);
        button_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,DeviceSettingActivity.class);
                startActivity(intent);
            }
        });

        button_profile = (Button) findViewById(R.id.btn_profile);
        button_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,ProfileActivity.class);
                startActivity(intent);
            }
        });

        button_trace = (Button) findViewById(R.id.btn_trace);
        button_trace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,TraceActivity.class);
                startActivity(intent);
            }
        });
    }

    private void enableUI(boolean flag){

        button_profile.setClickable(flag);
        button_setting.setClickable(flag);
        button_trace.setClickable(flag);
        circledImageView.setClickable(flag);
        linearLayout.setClickable(!flag);

    }
    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onDestroy() {
        Log.i("MAIN","onDestroy");
        super.onDestroy();
    }

    public void CreateToast(int Icon, String msg, int bgcolor){
        Vibrator vibrator = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(500);

        SuperActivityToast superActivityToast = new SuperActivityToast(this);
        superActivityToast.setText(msg);
        superActivityToast.setBackground(bgcolor);
        superActivityToast.setIcon(Icon, SuperToast.IconPosition.LEFT);
        superActivityToast.setDuration(SuperToast.Duration.MEDIUM);
        superActivityToast.setTouchToDismiss(true);
        superActivityToast.setAnimations(SuperToast.Animations.POPUP);
        superActivityToast.show();
    }
}
