package com.example.rhomeine.smartwear;

import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.support.wearable.view.WearableListView;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ProfileActivity extends WearableActivity {

    private static final SimpleDateFormat AMBIENT_DATE_FORMAT =
            new SimpleDateFormat("HH:mm", Locale.US);

    private BoxInsetLayout mContainerView;
    private TextView mClockView;

    //private WearableListView listView;
    private ListView listView;
    private SimpleAdapter simpleAdapter;
    private List<String> data;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        setAmbientEnabled();
        mContainerView = (BoxInsetLayout) findViewById(R.id.container);
        mClockView = (TextView) findViewById(R.id.clock);
        listView = (ListView) findViewById(R.id.list_profile);
        simpleAdapter = new SimpleAdapter(this,UserInfo.getMyData2(),R.layout.layout_lst_profile,new String[]{"title","key"},new int[]{R.id.title,R.id.key});
        listView.setAdapter(simpleAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:startActivity(new Intent(ProfileActivity.this, SettingActivity.class).putExtra("key", "4"));break;
                    case 1:startActivity(new Intent(ProfileActivity.this, SettingActivity.class).putExtra("key", "1"));break;
                    case 2:startActivity(new Intent(ProfileActivity.this, SettingActivity.class).putExtra("key", "2"));break;
                    case 3:startActivity(new Intent(ProfileActivity.this, SettingActivity.class).putExtra("key", "3"));break;
                }
                finish();
            }
        });
//        data = UserInfo.getMyData1();
//        arrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_single_choice,data);
//        listView.setAdapter(arrayAdapter);
//        listView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
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
            mClockView.setVisibility(View.VISIBLE);

            mClockView.setText(AMBIENT_DATE_FORMAT.format(new Date()));
        } else {
            mContainerView.setBackground(null);
            mClockView.setVisibility(View.GONE);
        }
    }
}
