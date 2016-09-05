package com.example.rhomeine.smartwear;


import android.content.DialogInterface;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import ticwear.design.app.AlertDialog;

public class TraceActivity extends WearableActivity {

    private static final SimpleDateFormat AMBIENT_DATE_FORMAT =
            new SimpleDateFormat("HH:mm", Locale.US);

    private BoxInsetLayout mContainerView;
    private TextView mClockView;
    private ArcProgress arcProgress;
    private TextView textView;
    private TextView textView1;
    private int i=0;

    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trace);
        setAmbientEnabled();

        mContainerView = (BoxInsetLayout) findViewById(R.id.container);
        mClockView = (TextView) findViewById(R.id.clock);
        arcProgress = (ArcProgress) findViewById(R.id.arc_score);
        textView = (TextView) findViewById(R.id.text_sec_left);
        textView1 = (TextView) findViewById(R.id.text_tr_weeks);
        int scores=0,target=150,secNum=5,secNumNow=0;
        try{
            scores = HeartRateTraining.getUserScores();
            target = HeartRateTraining.getTargetScores();
            secNum = HeartRateTraining.getSecNum();
            secNumNow = HeartRateTraining.getSecLength();
        }catch (Exception e){
            Log.i("TRACE", "Error in get training values");
        }
        arcProgress.setMax(target);
        //arcProgress.setProgress(scores);
        arcProgress.setProgress(68);
        arcProgress.setSuffixText("/"+Integer.toString(target));

        if(HeartRateTraining.is_init) {
            textView.setText("Section/Week: " + Integer.toString(secNumNow) + "/" + Integer.toString(secNum));
            textView1.setText("Next period from: \n" + new SimpleDateFormat("E MMM d").format(HeartRateTraining.getPeriodEndDate().getTime()));
        }else {
            textView.setText("Section/Week: " + Integer.toString(secNumNow) + "/" + Integer.toString(secNum));
            textView1.setText("Next period from: ----");
        }

        arcProgress.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                HeartRateTraining.resetUser();
                Toast.makeText(TraceActivity.this,"App has been reset!",Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(Wearable.API).build();
        mGoogleApiClient.connect();

        arcProgress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(HeartRateTraining.Test_Mode = !HeartRateTraining.Test_Mode)
                Toast.makeText(TraceActivity.this,"Test Mode On",Toast.LENGTH_SHORT).show();
                else{
//                    new AlertDialog.Builder(TraceActivity.this)
//                            .setTitle("Test Mode Off")
//                            .setMessage("")
//                            .setPositiveButtonIcon(R.drawable.tic_ic_btn_ok, new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    // Do something for positive action.
//                                    dialog.dismiss();
//                                }
//                            })
//                            .show();
//                    HeartRateTraining.writeToJsonFile(HeartRateTraining.toJsonObject(HeartRateTraining.testSection()),TraceActivity.this);
                    Toast.makeText(TraceActivity.this,"Test Mode Off",Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGoogleApiClient.disconnect();
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

            mClockView.setVisibility(View.VISIBLE);
            mClockView.setText(AMBIENT_DATE_FORMAT.format(new Date()));
        } else {
            mContainerView.setBackground(getDrawable(R.drawable.bg03));
            mClockView.setVisibility(View.GONE);
        }
    }
    private void sendMessageToHandheld(final String message) {

        if (mGoogleApiClient == null)
            return;

        Log.d("GoogleApiClient","sending a message to handheld: "+message);

        // use the api client to send the heartbeat value to our handheld
        final PendingResult<NodeApi.GetConnectedNodesResult> nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient);
        nodes.setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
            @Override
            public void onResult(NodeApi.GetConnectedNodesResult result) {
                final List<Node> nodes = result.getNodes();
                if (nodes != null) {
                    for (int i=0; i<nodes.size(); i++) {
                        final Node node = nodes.get(i);
                        Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(), message, null);
                    }
                }
            }
        });
    }
}
