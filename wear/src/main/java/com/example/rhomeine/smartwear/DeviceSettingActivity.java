package com.example.rhomeine.smartwear;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.support.wearable.view.WearableListView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ShareActionProvider;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class DeviceSettingActivity extends WearableActivity implements WearableListView.ClickListener{

    private static final SimpleDateFormat AMBIENT_DATE_FORMAT =
            new SimpleDateFormat("HH:mm", Locale.US);

    private BoxInsetLayout mContainerView;
//    private TextView mTextView;
    private TextView mClockView;
    private TextView modeInfo;
    private TextView trInfo;
    private Spinner spinner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_setting);
        setAmbientEnabled();

        mContainerView = (BoxInsetLayout) findViewById(R.id.container);
        mClockView = (TextView) findViewById(R.id.clock);
        modeInfo = (TextView) findViewById(R.id.text_built_in_info);
        trInfo = (TextView) findViewById(R.id.text_tr_mode);


        final SharedPreferences sharedPreferences = getSharedPreferences("myPre", 0);
        String TrainingMode = sharedPreferences.getString("TRAINING_MODE", "EASY");
        Log.i("TRAINING_MODE", "mode is " + TrainingMode);

        modeInfo.setText(HeartRateTraining.getSecNum()+" sections / week \n"+HeartRateTraining.getSecDuration()+" mins / section\n" + HeartRateTraining.getTargetScores()+ " target scores");

        spinner = (Spinner) findViewById(R.id.spinner_tr_mode);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.mode, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(adapter.getPosition(TrainingMode), true);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                //    if(isfirst) {isfirst=false;}else {
                String mode = parent.getAdapter().getItem(position).toString().toUpperCase();
                HeartRateTraining.setTrainingMode(mode);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("TRAINING_MODE", mode);
                if(editor.commit())
                    Log.i("TRE_MODE", "SAVE successfully");
                else
                    Log.i("TRE_MODE", "SAVE failed");

                modeInfo.setText(HeartRateTraining.getSecNum() + " sections / week \n" + HeartRateTraining.getSecDuration() + " mins / section\n"+HeartRateTraining.getTargetScores()+" target scores");

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


    }

    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);
        updateDisplay();
    }

    @Override
    public void onClick(WearableListView.ViewHolder viewHolder) {

    }

    @Override
    public void onTopEmptyRegionClick() {

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
    //        mTextView.setTextColor(getResources().getColor(android.R.color.white));
            mClockView.setVisibility(View.VISIBLE);

            mClockView.setText(AMBIENT_DATE_FORMAT.format(new Date()));
        } else {
            mContainerView.setBackground(null);
    //        mTextView.setTextColor(getResources().getColor(android.R.color.black));
            mClockView.setVisibility(View.GONE);
        }
    }
}
