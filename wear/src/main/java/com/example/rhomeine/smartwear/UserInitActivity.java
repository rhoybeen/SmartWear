package com.example.rhomeine.smartwear;

import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.support.wearable.view.CircledImageView;
import android.view.View;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class UserInitActivity extends WearableActivity {

    private static final SimpleDateFormat AMBIENT_DATE_FORMAT =
            new SimpleDateFormat("HH:mm", Locale.US);

    private BoxInsetLayout mContainerView;
    private TextView textView;
    private PickerView agePicker;
    private CircledImageView circledImageView;
    private int age;
    private final int RESULT_CODE = 100;
    private final int DEFAULT_AGE = 22;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_init);

        mContainerView = (BoxInsetLayout) findViewById(R.id.container);

        textView = (TextView) findViewById(R.id.text);
        initAgePicker();

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
        //    mContainerView.setBackgroundColor(getResources().getColor(android.R.color.black));

        } else {
            mContainerView.setBackground(null);
        }
    }

    public void initAgePicker(){
        agePicker = (PickerView) findViewById(R.id.picker_age);
        List<String> data = new ArrayList<String>();
        for(int i =0;i<111;i++){
            data.add(Integer.toString(i));
        }
        agePicker.setData(data);
        agePicker.setSelected(DEFAULT_AGE);
        agePicker.setOnSelectListener(new PickerView.onSelectListener() {
            @Override
            public void onSelect(String text) {
                //    Toast.makeText(SettingActivity.this, "Your age is " + text, Toast.LENGTH_SHORT).show();
                //    UserInfo.setAge(Integer.valueOf(text));
                age = Integer.valueOf(text);
            }
        });
        agePicker.setVisibility(View.VISIBLE);

        circledImageView = (CircledImageView) findViewById(R.id.btn_s1);
        circledImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                if(age == 0) age = DEFAULT_AGE;
                intent.putExtra("AGE",age);
                setResult(RESULT_CODE,intent);
                finish();
            }
        });
    }


}
