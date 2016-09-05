package com.example.rhomeine.smartwear;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.wearable.activity.ConfirmationActivity;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.support.wearable.view.CircledImageView;
import android.support.wearable.view.DelayedConfirmationView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SettingActivity extends WearableActivity implements DelayedConfirmationView.DelayedConfirmationListener{

    private static final SimpleDateFormat AMBIENT_DATE_FORMAT =
            new SimpleDateFormat("HH:mm", Locale.US);

    private BoxInsetLayout mContainerView;
//    private TextView mTextView;
    private TextView mClockView;
    private TextView text_cm;
    private TextView text_kg;
    private TextView text_year;
    private PickerView agePicker;
    private PickerView weightPicker;
    private PickerView heightPicker;
    private PickerView genderPicker;
   // private Button button;
    private CircledImageView circledImageView;
    private int age;
    private int weight;
    private int height;
    private String gender;
    private String arg;
    private Vibrator vibrator;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        setAmbientEnabled();

        mContainerView = (BoxInsetLayout) findViewById(R.id.container);
        mClockView = (TextView) findViewById(R.id.clock);
        text_cm = (TextView) findViewById(R.id.text_cm);
        text_kg = (TextView) findViewById(R.id.text_kg);
        text_year = (TextView) findViewById(R.id.text_year);

        text_cm.setVisibility(View.INVISIBLE);
        text_kg.setVisibility(View.INVISIBLE);
        text_year.setVisibility(View.INVISIBLE);
     //   button = (Button) findViewById(R.id.btn_setting);
        circledImageView = (CircledImageView) findViewById(R.id.btn_s1);

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        initAgePicker();
        initHeightPicker();
        initWeightPicker();
        initGenderPicker();

        Intent intent = getIntent();
        arg = intent.getStringExtra("key");
        switch (arg){
            case "1":agePicker.setVisibility(View.VISIBLE);text_year.setVisibility(View.VISIBLE);break;
            case "2":heightPicker.setVisibility(View.VISIBLE);text_cm.setVisibility(View.VISIBLE);break;
            case "3":weightPicker.setVisibility(View.VISIBLE);text_kg.setVisibility(View.VISIBLE);break;
            case "4":genderPicker.setVisibility(View.VISIBLE);break;
        }
        circledImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (arg){
                    case "1":
                        if(age==0) age=UserInfo.getAge();
                        UserInfo.setAge(age);
                        vibrator.vibrate(500);
                        Toast.makeText(SettingActivity.this,"Age has been set to "+age,Toast.LENGTH_SHORT).show();
                        break;
                    case "2":
                        if(height==0) height=UserInfo.getHeight_cm();
                        UserInfo.setHeight_cm(height);
                        vibrator.vibrate(500);
                        Toast.makeText(SettingActivity.this, "Height has been set to  " + height+"cm", Toast.LENGTH_SHORT).show();
                        break;
                    case "3":
                        if(weight==0) weight=UserInfo.getWeight_kg();
                        UserInfo.setWeight_kg(weight);
                        vibrator.vibrate(500);
                        Toast.makeText(SettingActivity.this, "Weight has been set to " + weight+"kg", Toast.LENGTH_SHORT).show();
                        break;
                    case "4":
                        if(gender == null) gender = "Male";
                        UserInfo.setGender(gender);
                        vibrator.vibrate(500);
                        Toast.makeText(SettingActivity.this, "Gender has been set to " + gender.toUpperCase(), Toast.LENGTH_SHORT).show();
                }
                UserInfo.setTimeStamp(Calendar.getInstance().getTimeInMillis());
                SyncUserInfo.sendUserInfo(SettingActivity.this);
                Intent intent =  new Intent(SettingActivity.this, ProfileActivity.class);
                intent.putExtra("setting",true);
                startActivity(intent);
                finish();
            }
        });
    }
    public void initAgePicker(){
        agePicker = (PickerView) findViewById(R.id.picker_age);

        List<String> data = new ArrayList<String>();
        for(int i =0;i<111;i++){
            data.add(Integer.toString(i));
        }
        agePicker.setData(data);
        agePicker.setSelected(UserInfo.getAge());
        agePicker.setOnSelectListener(new PickerView.onSelectListener() {
            @Override
            public void onSelect(String text) {
                //    Toast.makeText(SettingActivity.this, "Your age is " + text, Toast.LENGTH_SHORT).show();
                //    UserInfo.setAge(Integer.valueOf(text));
                age = Integer.valueOf(text);
            }
        });
        agePicker.setVisibility(View.INVISIBLE);
    }

    public void initGenderPicker(){
        genderPicker = (PickerView) findViewById(R.id.picker_gender);
        genderPicker.setmMaxTextSize(30);
        genderPicker.setmMinTextSize(10);

        List<String> data3 = new ArrayList<String>();
        data3.add("Male");
        data3.add("Female");
        genderPicker.setData(data3);
        gender = UserInfo.getGender();
        if(gender == null) gender = "Male";
        genderPicker.setSelected(0);
        genderPicker.setScrollBarSize(200);
        genderPicker.setOnSelectListener(new PickerView.onSelectListener() {
            @Override
            public void onSelect(String text) {
                //    Toast.makeText(SettingActivity.this, "Your age is " + text, Toast.LENGTH_SHORT).show();
                //    UserInfo.setAge(Integer.valueOf(text));
                gender = text;
            }
        });
        genderPicker.setVisibility(View.INVISIBLE);
    }
    public void initWeightPicker(){
        weightPicker = (PickerView) findViewById(R.id.picker_weight);

        List<String> data1 = new ArrayList<String>();
        for(int i =30;i<201;i++){
            data1.add(Integer.toString(i));
        }
        weightPicker.setData(data1);
        weight = UserInfo.getWeight_kg();
        if(weight == 0) weight = 65;
        weightPicker.setSelected(weight-30);
        weightPicker.setOnSelectListener(new PickerView.onSelectListener() {
            @Override
            public void onSelect(String text) {
                //   Toast.makeText(SettingActivity.this, "Your weight is " + text, Toast.LENGTH_SHORT).show();
                //   UserInfo.setWeight_kg(Integer.valueOf(text));
                weight = Integer.valueOf(text);
            }
        });
        weightPicker.setVisibility(View.INVISIBLE);
    }
    public void initHeightPicker(){
        heightPicker = (PickerView) findViewById(R.id.picker_height);

        List<String> data2 = new ArrayList<String>();
        for(int i =100;i<301;i++){
            data2.add(Integer.toString(i));
        }

        heightPicker.setData(data2);
        height = UserInfo.getHeight_cm();
        if(height == 0) height = 175;
        heightPicker.setSelected(height - 100);
        heightPicker.setOnSelectListener(new PickerView.onSelectListener() {
            @Override
            public void onSelect(String text) {
                //   Toast.makeText(SettingActivity.this, "Your height is " + text, Toast.LENGTH_SHORT).show();
                //   UserInfo.setHeight_cm(Integer.valueOf(text));
                height = Integer.valueOf(text);
            }
        });
        heightPicker.setVisibility(View.INVISIBLE);
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
    public void onTimerSelected(View view) {

    }

    @Override
    public void onTimerFinished(View view) {

    }

    private void updateDisplay() {
        if (isAmbient()) {
            mContainerView.setBackgroundColor(getResources().getColor(android.R.color.black));
            mClockView.setVisibility(View.VISIBLE);

            mClockView.setText(AMBIENT_DATE_FORMAT.format(new Date()));
        } else {
            mContainerView.setBackground(null);
            mClockView.setVisibility(View.GONE);
        }
    }

}
