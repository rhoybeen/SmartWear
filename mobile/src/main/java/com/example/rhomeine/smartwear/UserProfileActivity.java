package com.example.rhomeine.smartwear;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

public class UserProfileActivity extends AppCompatActivity {

    private Button btn_modify;
    private Button btn_save;
    private Button btn_cancel;
    private RadioButton r_btn_male;
    private RadioButton r_btn_female;
    private EditText edit_age;
    private EditText edit_height;
    private EditText edit_weight;
    private TextView textTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        initViews();
        showUserInfo();
    }

    public void initViews(){

        textTime = (TextView) findViewById(R.id.text_timestamp);
        r_btn_male = (RadioButton) findViewById(R.id.btn_male);
        r_btn_female = (RadioButton) findViewById(R.id.btn_female);

        edit_age = (EditText) findViewById(R.id.edit_age);
        edit_height = (EditText) findViewById(R.id.edit_height);
        edit_weight = (EditText) findViewById(R.id.edit_weight);

        btn_modify = (Button) findViewById(R.id.btn_modify);
        btn_modify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enableViews(true);
                btn_save.setVisibility(View.VISIBLE);
                btn_cancel.setVisibility(View.VISIBLE);
                btn_modify.setVisibility(View.INVISIBLE);

                edit_age.setText(Integer.toString(UserInfo.getAge()));
                edit_height.setText(Integer.toString(UserInfo.getHeight_cm()));
                edit_weight.setText(Integer.toString(UserInfo.getWeight_kg()));
            }
        });
        btn_save = (Button) findViewById(R.id.btn_save);
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //save everything then...
                if(validateInput())
                {
                    updateUserInfo();
                    showUserInfo();
                    enableViews(false);
                    btn_save.setVisibility(View.INVISIBLE);
                    btn_cancel.setVisibility(View.INVISIBLE);
                    btn_modify.setVisibility(View.VISIBLE);
                }
            }
        });
        btn_save.setVisibility(View.INVISIBLE);
        btn_cancel = (Button) findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //dont save

                enableViews(false);
                btn_save.setVisibility(View.INVISIBLE);
                btn_cancel.setVisibility(View.INVISIBLE);
                btn_modify.setVisibility(View.VISIBLE);
                showUserInfo();
            }
        });
        btn_cancel.setVisibility(View.INVISIBLE);
        enableViews(false);
    }

    public void showUserInfo(){
        if(UserInfo.getGender().toLowerCase().equals("male")){
            r_btn_male.setChecked(true);
            r_btn_female.setChecked(false);
        }else {
            r_btn_male.setChecked(false);
            r_btn_female.setChecked(true);
        }
        if(UserInfo.getTimeStamp()!=0) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd kk:mm");
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(UserInfo.getTimeStamp());
            calendar.setTimeZone(TimeZone.getTimeZone("GMT+2"));
            String date = simpleDateFormat.format(calendar.getTime());
            textTime.setText("Last Update: " + date);
        }else textTime.setText("Last Update: --/--/--");
        edit_age.setHint(Integer.toString(UserInfo.getAge()));
        edit_height.setHint(Integer.toString(UserInfo.getHeight_cm())+" cm");
        edit_weight.setHint(Integer.toString(UserInfo.getWeight_kg())+" kg");
    }

    public void enableViews(boolean flag){
        if(!flag){
            r_btn_male.setEnabled(false);
            r_btn_female.setEnabled(false);
            edit_age.setEnabled(false);
            edit_height.setEnabled(false);
            edit_weight.setEnabled(false);
        }else{
            r_btn_male.setEnabled(true);
            r_btn_female.setEnabled(true);
            edit_age.setEnabled(true);
            edit_height.setEnabled(true);
            edit_weight.setEnabled(true);
        }
    }

    public void onRadioButtonChecked(View view){
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.btn_male:
                if (checked) {
                    // Pirates are the best
                    r_btn_female.setChecked(false);
                }
                    break;
            case R.id.btn_female:
                if (checked){
                    // Ninjas rule
                    r_btn_male.setChecked(false);
                }
                    break;
        }
    }

    public boolean validateInput(){
        String tmp = edit_age.getText().toString();
        if(!tmp.equals("")){
            try {
                Integer.valueOf(tmp);
            }catch (NumberFormatException e){
                edit_age.setError(getString(R.string.val_illegal));
                return false;
            }
        }else{
            edit_age.setError(getString(R.string.val_null));
            return false;
        }
        tmp = edit_height.getText().toString();
        if(!tmp.equals("")){
            try {
                Integer.valueOf(tmp);
            }catch (NumberFormatException e){
                edit_height.setError(getString(R.string.val_illegal));
                return false;
            }
        }else{
            edit_height.setError(getString(R.string.val_null));
            return false;
        }
        tmp = edit_weight.getText().toString();
        if(!tmp.equals("")){
            try {
                Integer.valueOf(tmp);
            }catch (NumberFormatException e){
                edit_weight.setError(getString(R.string.val_illegal));
                return false;
            }
        }else{
            edit_weight.setError(getString(R.string.val_null));
            return false;
        }
        return true;
    }

    public void updateUserInfo(){
        if(r_btn_male.isChecked())
            UserInfo.setGender("Male");
        else    UserInfo.setGender("Female");
        UserInfo.setAge(Integer.valueOf(edit_age.getText().toString()));
        UserInfo.setHeight_cm(Integer.valueOf(edit_height.getText().toString()));
        UserInfo.setWeight_kg(Integer.valueOf(edit_weight.getText().toString()));
        UserInfo.setTimeStamp(Calendar.getInstance().getTimeInMillis());
    }
}
