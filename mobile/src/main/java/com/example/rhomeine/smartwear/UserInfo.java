package com.example.rhomeine.smartwear;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by rhomeine on 16/2/29.
 */
public class UserInfo {

    private static int Weight_kg;
    private static int Age;
    private static int Height_cm;
    private static String Gender;
    private static int MAX_HEART_RATE;
    private static boolean init_flag = Boolean.FALSE;
    private static boolean sync_flag = Boolean.FALSE;
    public static long TimeStamp = 0;

    public static void initUserData(){

        Weight_kg = 70;
        Age = 22;
        Height_cm = 180;
        Gender = "Male";
        MAX_HEART_RATE = 220-Age;

        init_flag = true;
//        TimeStamp = Calendar.getInstance().getTimeInMillis();
    }

    public static Map<String,String> getUserData(){

        if(!init_flag) initUserData();

        Map<String, String> userMap = new HashMap<>();
        userMap.put("Gender", Gender);
        userMap.put("Weight", Integer.toString(Weight_kg));
        userMap.put("Height", Integer.toString(Height_cm));
        userMap.put("Age", Integer.toString(Age));
        userMap.put("Timestamp",Long.toString(TimeStamp));
        return userMap;

    }

    public static List<Map<String,String>> getMyData(){

        if(!init_flag) {initUserData();init_flag=Boolean.TRUE;}
        List<Map<String,String>> list_maps = new ArrayList<Map<String,String>>();
        Map<String,String> map1 = new HashMap<String,String>();
        map1.put("title","Gender");
        map1.put("key",Gender);

        Map<String,String> map2 = new HashMap<String,String>();
        map2.put("title","Age");
        map2.put("key",Integer.toString(Age));

        Map<String,String> map3 = new HashMap<String,String>();
        map3.put("title", "Height(cm)");
        map3.put("key", Integer.toString(Height_cm));

        Map<String,String> map4 = new HashMap<String,String>();
        map4.put("title", "Weight(kg)");
        map4.put("key", Integer.toString(Weight_kg));

        list_maps.add(map1);
        list_maps.add(map2);
        list_maps.add(map3);
        list_maps.add(map4);

        return list_maps;
    }

    public static boolean updateUserInfo(JSONObject json){
        try{
            long timestamp = json.getLong("TIMESTAMP");
            if(TimeStamp>timestamp) return false;
            else {
                setAge(json.getInt("AGE"));
                setGender(json.getString("GENDER"));
                setHeight_cm(json.getInt("HEIGHT"));
                setWeight_kg(json.getInt("WEIGHT"));
            }
        }catch (JSONException E){
            Log.i("UserInfo","updateUserInfo Exception");
        }
        return true;
    }

    public static JSONObject toJsonObject(){
        try{
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("AGE",getAge());
            jsonObject.put("GENDER",getGender());
            jsonObject.put("HEIGHT",getHeight_cm());
            jsonObject.put("WEIGHT",getWeight_kg());
            jsonObject.put("TIMESTAMP",getTimeStamp());

            return jsonObject;
        }catch (JSONException E){

        }
        return null;
    }

    public static int getWeight_kg(){
        return Weight_kg;
    }

    public static int getAge() {
        return Age;
    }

    public static int getHeight_cm() {
        return Height_cm;
    }

    public static String getGender() {
        return Gender;
    }

    public static int getMaxHeartRate() {
        return MAX_HEART_RATE;
    }

    public static void setAge(int age) {
        Age = age;
        MAX_HEART_RATE = 220-Age;
    }

    public static void setGender(String gender) {
        Gender = gender;
    }

    public static void setHeight_cm(int height_cm) {
        Height_cm = height_cm;
    }

    public static void setWeight_kg(int weight_kg) {
        Weight_kg = weight_kg;
    }

    public static void setTimeStamp(long timeStamp) {
        TimeStamp = timeStamp;
    }

    public static Map<String,String> getUserData2(){

        if(!init_flag) initUserData();

            Map<String, String> userMap = new HashMap<>();
            userMap.put("Gender", Gender);
            userMap.put("Weight", Integer.toString(Weight_kg));
            userMap.put("Height", Integer.toString(Height_cm));
            userMap.put("Age", Integer.toString(Age));

            return userMap;

    }

    public static long getTimeStamp() {
        return TimeStamp;
    }

}
