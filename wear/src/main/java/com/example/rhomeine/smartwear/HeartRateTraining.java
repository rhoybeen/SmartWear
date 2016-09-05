package com.example.rhomeine.smartwear;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Message;
import android.os.PowerManager;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import com.github.johnpersano.supertoasts.SuperActivityToast;
import com.github.johnpersano.supertoasts.SuperToast;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.PendingResults;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableStatusCodes;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by rhomeine on 16/3/11.
 * not in use
 */
public class HeartRateTraining {

    public static final int MODE_MAX = 6;
    public static final int MODE_HARD = 5;
    public static final int MODE_MODERATE = 4;
    public static final int MODE_LIGHT = 3;
    public static final int MODE_VERY_LIGHT = 2;
    public static final int MODE_PEACE = 1;
    public static final String TRAINING_HARD = "HARD";
    public static final String TRAINING_MODERATE = "MODERATE";
    public static final String TRAINING_EASY = "EASY";
    public static final int ZONE_MAX = 3;
    public static final int ZONE_2 = 2;
    public static final int ZONE_1 = 1;
    public static final int ZONE_0 = 0;
    public static final int ZONE_MIN = -1;
    private static final int HR_SCANNING_TIME_SEC = 5;
    private static final String LOG_TAG = "HRTraining";
    private static final int FLAG_SECTION_START = 1;
    private static final int FLAG_SECTION_END = 2;
    private static final int FLAG_SECTION_SUCCESS = 3;
    private static final int FLAG_SECTION_FAIL = 4;
    private static final int FLAG_HR_LOW = 5;
    private static final int FLAG_HR_HIGH = 6;
    private static final int FLAG_OVERTIME = 7;

    private static final String PATH_HR = "/heartrate";
    private static final String ITEM_KEY_HR = "heartrate";
    private static final String ITEM_KEY_SCORE = "score";
    private static final String ITEM_KEY_TIME = "time";
    private static final String ITEM_KEY_CALORIE = "calorie";

    private static Calendar StartDate;
    private static Calendar PeriodStartDate;
    private static Calendar PeriodEndDate;
    private static Calendar sec_start;
    private static Calendar sec_end;

    private static GoogleApiClient mGoogleApiClient;
    private static PowerManager.WakeLock wakeLock;
    private static Context context;

    private static Timer timer;
    private static int timer_count=0; //reset before starting a section
    private static int count=0;
    private static int ScorePerMin=0;

    private static String UserGroup;
    private static String TrainingMode;

    private static int target;
    private static int scores;
    private static int calorie;
    private static int SecDuration=0;
    private static int SectionID=0;
    private static int TimerDura=30;
    private static int StageNow=0;
    private static int Rating;

    private static int TargetScores;
    private static int UserScores;
    private static int CalConsumption;
    private static ArrayList<JSONObject> unSyncSections = new ArrayList<>();
    private static HashMap<String,Section> UserSections = new HashMap<String, Section>();
    private static ArrayList<Section> userSections;
    private static ArrayList<TrainingPeriod> weeklyLog;

    private static int pre_HRValue;
    private static int HRValue=0;
    private static ArrayList<Integer> heartRate = null;

    public static boolean is_init = false;
    public static boolean is_init_sec = false;
    public static boolean is_finished = false;
    private static boolean is_notified = false;

    //set Text_Mode true for testing. You can still get scores without running.
    public static boolean Test_Mode = false;

    public static void StartTrainingSection(Context con){

       context = con;
        //inintiate when starts a section the first time
        if(!is_init){
            StartDate = Calendar.getInstance();
            StartDate.set(Calendar.HOUR_OF_DAY,1);
            StartDate.set(Calendar.MINUTE,0);
            StartDate.set(Calendar.SECOND,0);
            Log.i(LOG_TAG, "Training start date is " + StartDate.getTime());
            initTrainingPeriod(StartDate);
            userSections = new ArrayList<>();
            weeklyLog = new ArrayList<>();
            weeklyLog.add(new TrainingPeriod());

            SectionID=0;
            is_init = true;
        }


        scores = 0;
        calorie = 0;
        ScorePerMin = Section.getScorePerMin(HRValue);
        Rating = 0;
        StageNow = 0;
        heartRate = new ArrayList<>();
        is_notified = false;

        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock((PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP), "TAG");
        mGoogleApiClient = new GoogleApiClient.Builder(context).addApi(Wearable.API).build();
        mGoogleApiClient.connect();
        Wearable.MessageApi.addListener(mGoogleApiClient, messageListener);
        Log.i(LOG_TAG, "init message listener");

        timer = new Timer();
        timer.schedule(new MyTimerTask(), 0, 1000);
        timer_count = 0;
        count = 0;

        target = getUserSections().get(TrainingMode).TargetScore;
        Log.i(LOG_TAG, "Target is " + target);
        updateTraningPeriod();
        TrainingAlert(FLAG_SECTION_START);
        Log.i(LOG_TAG, "Week " + weeklyLog.size());
        sec_start = Calendar.getInstance();
        Log.i(LOG_TAG, "Section start at " + sec_start.getTime());

        Section section = new Section(userSections.size() + 1, getSecDuration(), 0, SectionID++);
        userSections.add(section);
        SecDuration = getSecDuration();
        TimerDura = SecDuration;

        sendMessageToHandheld("/sec_dura", intToByteArray(SecDuration));
        sendMessageToHandheld("/sec_target", intToByteArray(target));
        Log.i(LOG_TAG, "This is the No." + (userSections.size()) + " section");

        is_init_sec = true;
        is_finished = false;

  //      syncUserInfo();
   }

    static class MyTimerTask extends TimerTask{
        @Override
        public void run() {

            if((count%120==0) && (count>0)){
                TrainingAlert();
                Log.i(LOG_TAG, "Training alert:"+count);
            }
            heartRate.add(HRValue);
            calorie += CalConsumptionRate(HRValue);
            Log.i(LOG_TAG, "calorie :"+calorie+" count :"+count);
            syncDataItem();

            if(count++==60*TimerDura){
                if(!isGoalAchieved()) {

                    if(StageNow++>=2) {
                        stopSection(false);
                        return;
                    }
                    Log.i(LOG_TAG, "Add more time. Stage now is " + StageNow);
                    TimerDura +=10;
                    Message message = new Message();
                    message.what = 1;
                    message.arg1 = TimerDura;
                    message.arg2 = StageNow;
                    BuiltinSensorActivity.myHandler.sendMessage(message);
                    sendMessageToHandheld("/sec_dura",intToByteArray(TimerDura));
                    TrainingAlert(FLAG_OVERTIME);
                }else {
                    TrainingAlert(FLAG_SECTION_END);
                    stopSection(true);
                }
            }else {
                if(count%HR_SCANNING_TIME_SEC==0){
                 if (HRValueToZoneNum(HRValue) == ZONE_0) {
                        timer_count = 0;
                        return;
                    } else if (timer_count == 0) {
                        ScorePerMin = Section.getScorePerMin(HRValue);
                        timer_count++;
                    } else if (HRValueToZoneNum(HRValue) - HRValueToZoneNum(pre_HRValue) < 0) {
                        ScorePerMin = Section.getScorePerMin(HRValue);
                        timer_count++;
                    } else {
                        timer_count++;
                    }
                    if (timer_count >= 60 / HR_SCANNING_TIME_SEC) {
                        timer_count = 0;
                        scores += ScorePerMin;
                    }
                    Log.i(LOG_TAG, "Score now is " + scores + "  Count is " + count);
                    if(isGoalAchieved()&&!is_notified) {
                        TrainingAlert(FLAG_SECTION_SUCCESS);
                        if(StageNow!=0) stopSection(true);
                        is_notified = true;
                    }

                }
                Message message = new Message();
                message.what = 0;
                message.arg1 = scores;
                message.arg2 = count;
                BuiltinSensorActivity.myHandler.sendMessage(message);
            }

        }
    }

    private static void stopTimer(){

        if (timer != null) {
            timer.cancel();
            timer = null;
        }

    }

    private static void stopSection(boolean is_successful){

        if(is_successful) TrainingAlert(FLAG_SECTION_END);
        else TrainingAlert(FLAG_SECTION_FAIL);

        stopTimer();

        sec_end = Calendar.getInstance();
        Log.i(LOG_TAG, "Section ends at " + sec_end.getTime());
        Section sec = null;
        try {
            sec = userSections.get(userSections.size() - 1);
            sec.User_Scores = scores;
            sec.User_CalConsumption = calorie;
            sec.HRLog = new ArrayList<>(heartRate);
            UserScores += scores;
            CalConsumption += calorie;
            sec.User_Duration_min = (int)(sec_end.getTime().getTime() - sec_start.getTime().getTime())/(1000*60);
            Log.i(LOG_TAG, "Section lasts for " + sec.User_Duration_min +"mins");
            Log.i(LOG_TAG, "Section Info:" + "Dur--" + sec.User_Duration_min + " Score--" + sec.User_Scores);
            Log.i(LOG_TAG, "Final scores: " + scores+" Totoal scores: "+UserScores);
        }catch (Exception e){
            Log.i(LOG_TAG, "Ended with on section");
        }
        is_finished = true;
        writeToJsonFile(toJsonObject(sec),context);
    }

    public static int finish(){

        stopTimer();
        if(!is_finished && is_init && is_init_sec) {

            //Quit section without finishing it
            if(mGoogleApiClient!=null){
                mGoogleApiClient.disconnect();
                Wearable.MessageApi.removeListener(mGoogleApiClient,messageListener);
                mGoogleApiClient = null;
                Log.i(LOG_TAG,"mGoogleApiClient is not null");
            }
            sec_end = Calendar.getInstance();
            int sec_last_time = (int)(sec_end.getTime().getTime() - sec_start.getTime().getTime())/(1000*60);
            Log.i(LOG_TAG, "Section lasts for " + sec_last_time +"mins\nScores: "+scores);
            userSections.remove(userSections.size()-1);
            is_init_sec = false;
            Log.i(LOG_TAG,"mGoogleApiClient is not null");
        }

        return scores;
    }

    public static void initUser(Context con){

        UpdateUserGroup();
        UserScores = 0;
        CalConsumption = 0;
        final SharedPreferences sharedPreferences = con.getSharedPreferences("myPre", 0);
        TrainingMode = sharedPreferences.getString("TRAINING_MODE", "EASY");
    }

    public static int updateTraningPeriod(){
        Calendar calendar = Calendar.getInstance();
        if(calendar.after(PeriodEndDate)){
            TrainingPeriod trainingPeriod = getTrainingPeriod();
            trainingPeriod.Sections = new ArrayList<>(userSections);
            trainingPeriod.StartDate = Calendar.getInstance();
            trainingPeriod.StartDate.setTime(PeriodStartDate.getTime());
            trainingPeriod.EndDate = Calendar.getInstance();
            trainingPeriod.EndDate.setTime(PeriodEndDate.getTime());
            trainingPeriod.PeriodScores = UserScores;
            trainingPeriod.PeriodTarget = TargetScores;
            trainingPeriod.PeriodCalorie = CalConsumption;
            CalConsumption = 0;
            UserScores = 0;
            initTrainingPeriod(PeriodEndDate);
            weeklyLog.add(new TrainingPeriod());
        }
        return weeklyLog.size();
    }

    public static void initTrainingPeriod(Calendar startDate){

        PeriodStartDate = (Calendar) startDate.clone();
    //    PeriodStartDate.setTimeZone(TimeZone.getTimeZone("GMT+2"));
        Log.i(LOG_TAG, "Period start Date is " + PeriodStartDate.getTime());
        PeriodEndDate = (Calendar) PeriodStartDate.clone();
        PeriodEndDate.set(Calendar.DAY_OF_YEAR, startDate.get(Calendar.DAY_OF_YEAR) + 7);
        Log.i(LOG_TAG, "Period end Date is " + PeriodEndDate.getTime());
    }

    public static void resetUser(){
        UserScores = 0;
        CalConsumption = 0;
        userSections = null;
        is_init = false;
        userSections = null;
        weeklyLog = null;

    }

    public static void UpdateUserGroup(){
        if(UserInfo.getAge()<18) {
            UserGroup = "YOUNG";
            TargetScores = 300;
        }
        else if(UserInfo.getAge()<65) {
            UserGroup = "ADULT";
            TargetScores = 150;
        }
        else {
            UserGroup = "OLD";
            TargetScores = 150;
        }
        initSections();
    }

    public static void initSections(){
        if(UserGroup==null){
            Log.i(LOG_TAG, "usergroup not initiated");return;
        }else{
            String usergroup = UserGroup.toUpperCase();
            switch (usergroup){
                case "YOUNG":
                    UserSections.put(TRAINING_HARD,new Section(6,30,50));
                    UserSections.put(TRAINING_MODERATE,new Section(8,30,37));
                    UserSections.put(TRAINING_EASY,new Section(10,30,30));
                    break;
                case "ADULT":
                    UserSections.put(TRAINING_HARD,new Section(3,30,50));
                    UserSections.put(TRAINING_MODERATE,new Section(4,30,37));
                    UserSections.put(TRAINING_EASY,new Section(5,30,30));
                    break;
                case "OLD":
                    UserSections.put(TRAINING_HARD,new Section(3,40,50));
                    UserSections.put(TRAINING_MODERATE,new Section(4,40,37));
                    UserSections.put(TRAINING_EASY,new Section(5,40,30));
                    break;
                default:break;
            }
        }
    }

    public static int HRValueToLevel(int hr_value){

        int heartRateLevel=1;
        try{
            heartRateLevel = (int) (hr_value/UserInfo.getMaxHeartRate()*10);
        }catch (Error e){
            Log.i(LOG_TAG,"Error in converting maxHRValue");
        }
        switch (heartRateLevel) {
            case 9:
                return MODE_MAX;
            case 8:
                return MODE_HARD;
            case 7:
                return MODE_MODERATE;
            case 6:
                return MODE_LIGHT;
            case 5:
                return MODE_VERY_LIGHT;
            default:
                return MODE_PEACE;
        }
    }

    public static byte[] intToByteArray(int a)
    {
        byte[] ret = new byte[4];
        ret[3] = (byte) (a & 0xFF);
        ret[2] = (byte) ((a >> 8) & 0xFF);
        ret[1] = (byte) ((a >> 16) & 0xFF);
        ret[0] = (byte) ((a >> 24) & 0xFF);
        return ret;
    }

    public static int HRValueToZoneNum(int hr_value){
        int heartRateLevel=1;
        try{
            heartRateLevel = (int) (hr_value/UserInfo.getMaxHeartRate()*10);
        }catch (Error e){
            Log.i(LOG_TAG,"Error in converting maxHRValue");
        }
        switch (heartRateLevel) {
            case 9:
                return ZONE_MAX;
            case 8:
                return ZONE_2;
            case 7:
                return ZONE_1;
            default:
                if(Test_Mode) return ZONE_1;
                else return ZONE_0;
        }
    }

    //Calorie Consumption Rate per Second
    public static int CalConsumptionRate(int hr){
        int hrlevel = (int) (hr/UserInfo.getMaxHeartRate()*10);
        switch (hrlevel){
            case 9:
                return 283;
            case 8:
                return 217;
            case 7:
                return 167;
            case 6:
                return 117;
            case 5:
                return 67;
            default:
                return 37;
        }
    }

    public static int ChangeTextColor(int hrvalue) {
        switch (HRValueToZoneNum(hrvalue)) {
            case ZONE_0:
                return Color.parseColor("#35b93b");
            case ZONE_1:
                return Color.parseColor("#FFEB3B");
            case ZONE_2:
            case ZONE_MAX:
                return Color.parseColor("#E91E63");
            default:
                return Color.parseColor("#35b93b");

        }
    }

    public static void TrainingAlert(){

        int Zone = HRValueToZoneNum(HRValue);
        switch (Zone){
            case ZONE_0:
                CreateToast(R.drawable.twohearts, "Speed Up Your Heart Rate!!!",R.color.blue_1);
                sendMessageToHandheld("/path_low",null);
                Log.i(LOG_TAG, "Call TrainingAlert method at ZONE0");break;
            case ZONE_MAX:
                CreateToast(R.drawable.brokenheart, "Heart Rate Too High\nSlow Down!!!",R.color.blue_1);
                sendMessageToHandheld("/path_high",null);
                Log.i(LOG_TAG, "Call TrainingAlert method at ZONEmax");break;
            default:break;
        }
    }

    public static void TrainingAlert(int type){
        switch (type){
            case FLAG_SECTION_START:
                CreateToast(R.drawable.running2,"Training Start!!\nSpeed Up!!",R.color.green);
                sendMessageToHandheld("/path_start",null);
                break;
            case FLAG_SECTION_END:
                switch (StageNow){
                    case 0:
                        CreateToast(R.drawable.threestars, "Congratulations!\nYou Get "+scores+" Scores~", R.color.blue_1);break;
                    case 1:
                        CreateToast(R.drawable.twostars, "Congratulations!\nYou Get "+scores+" Scores~", R.color.blue_1);break;
                    case 2:
                        CreateToast(R.drawable.onestar, "Congratulations!\nYou Get "+scores+" Scores~", R.color.blue_1);break;
                }
                sendMessageToHandheld("/path_end",null);
                Log.i(LOG_TAG,"send /path_end message");
                break;
            case FLAG_SECTION_SUCCESS:
                CreateToast(R.drawable.thumbup, "Goal Achieved!\nCongratulation~", R.color.green);
                sendMessageToHandheld("/path_success",null);
                break;
            case FLAG_SECTION_FAIL:
                CreateToast(R.drawable.sadface,"Time's Up!\nSection Fails :(",R.color.yellow_0);
                sendMessageToHandheld("/path_fail",null);
                break;
            case FLAG_OVERTIME:
                CreateToast(StageNow==1?R.drawable.twostars:R.drawable.onestar,"Time Exceeded!\n10mins More~ Hurry up!",R.color.blue_1);
                sendMessageToHandheld("/path_otime",null);
                break;

        }
    }

    public static void CreateToast(int Icon, String msg, int bgcolor){
        Activity activity = (Activity) context;
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(500);

        wakeLock.acquire();

        SuperActivityToast superActivityToast = new SuperActivityToast(activity);
        superActivityToast.setText(msg);
        superActivityToast.setBackground(bgcolor);
        superActivityToast.setIcon(Icon, SuperToast.IconPosition.LEFT);
        superActivityToast.setDuration(SuperToast.Duration.MEDIUM);
        superActivityToast.setTouchToDismiss(true);
        superActivityToast.setAnimations(SuperToast.Animations.POPUP);
        superActivityToast.show();

        wakeLock.release();
    }

    public static class TrainingPeriod{
        public static ArrayList<Section> Sections = new ArrayList<>();
        public Calendar StartDate;
        public Calendar EndDate;
        public int PeriodTarget;
        public int PeriodScores;
        public int PeriodCalorie;
        public TrainingPeriod(){
            Sections = null;
            StartDate = null;
            EndDate = null;
            PeriodTarget = 0;
            PeriodScores = 0;
            PeriodCalorie = 0;
        }
    }

    public static class Section{
        protected int SectionNum;
        protected int Duration_min;
        protected int TargetScore;

        protected int User_SectionIndex;
        protected int User_Duration_min;
        protected int User_Scores;
        protected int User_SectionID;
        protected int User_CalConsumption;
        protected Calendar User_Date;

        protected ArrayList<Integer> HRLog;

        public Section(int num,int min,int score){
            SectionNum = num;
            Duration_min = min;
            TargetScore = score;
            User_SectionIndex = -1;
            User_Duration_min = -1;
            User_Scores = -1;
            User_SectionID = -1;
        }

        public Section(int index,int time,int score,int id){
            SectionNum = getSecNum();
 //           Log.i(LOG_TAG,"Sec num is "+SectionNum);
            Duration_min = getSecDuration();
            TargetScore = getTargetScores();

            User_SectionIndex = index;
            User_Duration_min = time;
            User_Scores = score;
            User_SectionID = id;
            User_Date = null;
            User_CalConsumption = 0;
            HRLog = new ArrayList<>();
        }

        public static int getScorePerMin(int HRVal){
            int temp = HRValueToZoneNum(HRVal);
            switch (temp){
                case ZONE_1:return 1;
                case ZONE_2:
                case ZONE_MAX:
                    return 2;
                default:return 0;
            }
        }

        public int getTargetScore() {
            return TargetScore;
        }

        @Override
        public String toString() {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss");
            String date = simpleDateFormat.format(this.User_Date);
            String string = "Sec_id:" + this.User_SectionIndex + " Score:" + this.User_Scores + " Date:" + date +
                    "Calorie_Consumption" + this.User_CalConsumption;
            return string;
        }
    }

    private static boolean sendMessageToHandheld(final String message, final byte[] value) {

        if (mGoogleApiClient == null)
            return false;

        Log.d("GoogleApiClient","sending a message to handheld: "+message);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                // use the api client to send the heartbeat value to our handheld
                final PendingResult<NodeApi.GetConnectedNodesResult> nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient);
                nodes.setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                    @Override
                    public void onResult(NodeApi.GetConnectedNodesResult result) {
                        final List<Node> nodes = result.getNodes();
                        if (nodes != null) {
                            for (int i=0; i<nodes.size(); i++) {
                                final Node node = nodes.get(i);
                                PendingResult results = Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(), message, value);
                            }
                        }
                    }
                });

            }
        });
        thread.start();
        return true;
    }

    private static void syncDataItem(final int value,final String Key){
        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(PATH_HR);
                putDataMapRequest.getDataMap().putInt(Key,value);
                putDataMapRequest.setUrgent();
                PutDataRequest putDataRequest = putDataMapRequest.asPutDataRequest();
                PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi.putDataItem(mGoogleApiClient,putDataRequest);
                DataApi.DataItemResult result = pendingResult.await();
                if(result.getStatus().isSuccess()) {
                    Log.d("SyncDataItem", "Data item set: " + result.getDataItem().getUri());
                }else Log.d("SyncDataItem", "Data item no set");
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
        Log.d("GoogleApiClient","SyncDataItems");
    }

    private static void syncDataItem(){
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(PATH_HR);
                putDataMapRequest.getDataMap().putInt(ITEM_KEY_HR,HRValue);
                putDataMapRequest.getDataMap().putInt(ITEM_KEY_TIME,count);
                putDataMapRequest.getDataMap().putInt(ITEM_KEY_SCORE,scores);
                putDataMapRequest.getDataMap().putInt(ITEM_KEY_CALORIE,calorie);
                putDataMapRequest.setUrgent();
                PutDataRequest putDataRequest = putDataMapRequest.asPutDataRequest();
                PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi.putDataItem(mGoogleApiClient,putDataRequest);
                DataApi.DataItemResult result = pendingResult.await();
                if(result.getStatus().isSuccess()) {
                    Log.d("SyncDataItem", "Data item set: " + result.getDataItem().getUri());
                }else Log.d("SyncDataItem", "Data item no set");
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
        Log.d("GoogleApiClient","SyncDataItems");
    }

    /*Create data for testing*/
    public static Section testSection(){

        Section section = new Section(1, getSecDuration(), 0, 0);
        Calendar calendar = Calendar.getInstance();
        section.User_Date = calendar;
        section.User_Scores = 45;
        section.User_CalConsumption = 10000;
        section.User_Duration_min = 30;
        section.TargetScore = 45;
        ArrayList<Integer> hrArray = new ArrayList<>();
        Random random = new Random();
        for(int i=0;i<500;i++){
            int tmp = random.nextInt(150) + 50;
            hrArray.add(tmp);
        }
        section.HRLog = hrArray;
        return section;
    }

    public static JSONObject toJsonObject(Section sec){

        try{
            JSONObject sectionObj = new JSONObject();
            sectionObj.put("GENDER",UserInfo.getGender());
            sectionObj.put("AGE",UserInfo.getAge());
            sectionObj.put("HEIGHT",UserInfo.getHeight_cm());
            sectionObj.put("WEIGHT",UserInfo.getWeight_kg());
            sectionObj.put("GROUP",UserGroup);

            JSONObject secInfo = new JSONObject();
            secInfo.put("ID",sec.User_SectionID);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmm");
            secInfo.put("START_DATE",simpleDateFormat.format(sec.User_Date.getTime()));
            secInfo.put("USER_SCORE",sec.User_Scores);
            secInfo.put("CALORIE_CONSUMPTION",sec.User_CalConsumption);
            secInfo.put("TARGET_SCORE",sec.TargetScore);
            secInfo.put("USER_DURATION",sec.User_Duration_min);
            secInfo.put("SEC_DURATION",SecDuration);
            sectionObj.put("SEC_INFO",secInfo);

            JSONArray heartrate = new JSONArray(sec.HRLog);
            sectionObj.put("HEARTRATE",heartrate);
            sectionObj.put("isSynced",false);
            return sectionObj;
        }catch (JSONException e){
            throw new RuntimeException(e);
        }
    }

    public static String writeToJsonFile(JSONObject jObj,Context context){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmm");
        String fileName = simpleDateFormat.format(Calendar.getInstance().getTime()) + ".json";
//        Log.i(LOG_TAG,"FileDir-->" + context.getFilesDir());
        Log.i(LOG_TAG,"File name--> " + fileName);
        try {
            FileOutputStream outputStream = context.openFileOutput(fileName, Activity.MODE_APPEND);
            try{
                outputStream.write(jObj.toString().getBytes());
                outputStream.close();
            }catch (IOException E){
                Log.i(LOG_TAG,"IO Exception");
            }
        }catch (FileNotFoundException e){
            Log.i(LOG_TAG,"File not found");
            return null;
        }
        return fileName;
    }

    private static void syncWithMobile(){
        for(JSONObject jsonObject:unSyncSections){
            sendMessageToHandheld("/section",jsonObject.toString().getBytes());
            Toast.makeText(context,"sync section",Toast.LENGTH_SHORT).show();
            Log.i(LOG_TAG,"sync one section");
        }
    }

    static MessageApi.MessageListener messageListener = new MessageApi.MessageListener() {
        @Override
        public void onMessageReceived(MessageEvent messageEvent) {
            Log.i("Message","onMessageReceived");
            String str = messageEvent.getPath();
            switch (str){
                case "/ack":
                    try{
                        Log.i("Sync","sync successfully: "+unSyncSections.get(0).getJSONObject("SEC_INFO").get("START_DATE"));
                        unSyncSections.remove(0);
                        Toast.makeText(context,"ack message",Toast.LENGTH_SHORT).show();
                    }catch (JSONException e){
                        Log.i("Sync","sync successfully: json exception");
                    }
                    break;
                case "/userinfo":
       //             updateUserInfo(messageEvent.getData());
                default:break;
            }
        }
    };

    static void syncUserInfo(){
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        try{
            ObjectOutputStream out = new ObjectOutputStream(bytesOut);
            out.writeObject(UserInfo.getUserData());
            byte[] bytes = bytesOut.toByteArray();
            sendMessageToHandheld("/userinfo",bytes);
        }catch (IOException E){
            Log.i(LOG_TAG,"IO Exception");
        }
    }

    static void updateUserInfo(byte[] bytes){
        ByteArrayInputStream bytesInput = new ByteArrayInputStream(bytes);
        try{
            ObjectInput objectInput = new ObjectInputStream(bytesInput);
            try{
                Map<String,String> userMap = (Map<String,String>) objectInput.readObject();
                Long timestamp = Long.valueOf(userMap.get("Timestamp"));
                UserInfo.setAge(Integer.valueOf(userMap.get("Age")));
                UserInfo.setHeight_cm(Integer.valueOf(userMap.get("Height")));
                UserInfo.setWeight_kg(Integer.valueOf(userMap.get("Weight")));
                UserInfo.setGender(userMap.get("Gender"));
                UserInfo.setTimeStamp(timestamp);
            }catch (ClassNotFoundException e){
                Log.i(LOG_TAG,"Class Not Found Exception");
            }
        }catch (IOException e){
            Log.i(LOG_TAG,"IO Exception");
        }
    }

    public static HashMap<String, Section> getUserSections() {
        return UserSections;
    }

    public static int getHRValue() {
        return HRValue;
    }

    public static void setHRValue(int HRValue) {

        HeartRateTraining.pre_HRValue = HeartRateTraining.HRValue;
        HeartRateTraining.HRValue = HRValue;

    }

    public static int getUserScores() {
        return UserScores;
    }

    public static void setUserScores(int userScores) {
        UserScores = userScores;
    }

    public static int getCalConsumption() {
        return CalConsumption;
    }

    public static int getTargetScores() {
        return TargetScores;
    }

    public static String getTrainingMode() {
        return TrainingMode;
    }

    public static void setTrainingMode(String trainingMode) {
        TrainingMode = trainingMode;
    }

    public static int getSecNum(){
        return UserSections.get(TrainingMode).SectionNum;
    }

    public static int getSecDuration(){
        return UserSections.get(TrainingMode).Duration_min;
    }

    public static int getTarget() {
        return target;
    }

    public static int getTrainingWeeks(){
        return weeklyLog.size();
    }

    public static Calendar getPeriodEndDate() {
        return PeriodEndDate;
    }

    public static void AdjustCount(int sec) {
        HeartRateTraining.count += sec;
    }

    public static void AdjustScores(int scores){
        HeartRateTraining.scores += scores;
        Message message = new Message();
        message.what = 0;
        message.arg1 = scores;
        message.arg2 = count;
        BuiltinSensorActivity.myHandler.sendMessage(message);
    }

    public static ArrayList<TrainingPeriod> getWeeklyLog() {
        return weeklyLog;
    }

    public static TrainingPeriod getTrainingPeriod(){
        if(weeklyLog!=null)
            return weeklyLog.get(weeklyLog.size()-1);
        return null;
    }

    public static int getSecLength(){
        return userSections==null?0:userSections.size();
    }

    public static boolean isGoalAchieved(){
        return scores>=target?true:false;
    }

}

