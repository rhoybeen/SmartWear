package com.example.rhomeine.smartwear;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.Xml;

import com.example.rhomeine.smartwear.Login.SmartCustomLoginListener;
import com.example.rhomeine.smartwear.Login.SmartCustomLogoutListener;
import com.example.rhomeine.smartwear.Login.SmartLoginBuilder;
import com.example.rhomeine.smartwear.Login.SmartLoginConfig;
import com.example.rhomeine.smartwear.Login.manager.UserSessionManager;
import com.example.rhomeine.smartwear.Login.users.SmartUser;
import com.example.rhomeine.smartwear.Login.util.DialogUtil;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by WSPN on 2016/9/8.
 */
public class UserLogin implements SmartCustomLoginListener,SmartCustomLogoutListener{

    final static String ADMIN = "rhoybeen";
    final static String ADMIN_PWD = "rhoy2012";
    final String LOG_TAG = "UserLogin";
    final static String LOGIN_OK = "customloginok";
    final static String LOGIN_FAIL = "customloginfails";

    Context context;
    UserLogin(){
    }
    UserLogin(Context con){
        this.context = con;
    }

    public boolean login(){
        SmartUser user = UserSessionManager.getCurrentUser(context);
        if(user==null){
            startLoginActivity();
            if(UserSessionManager.getCurrentUser(context)==null)
                return false;
        }else {
            SmartLoginBuilder.smartCustonLogoutListener = this;
            DialogUtil.getSignOutDialog(user,(Activity) context).show();
        }
        return true;
    }

    public void startLoginActivity(){
        SmartLoginBuilder loginBuilder = new SmartLoginBuilder();
        Intent intent = loginBuilder.with(this.context)
                .isFacebookLoginEnabled(false)
                .isGoogleLoginEnabled(false)
                .isCustomLoginEnabled(true, SmartLoginConfig.LoginType.withUsername)
                .setSmartCustomLoginHelper(this)
                .setSmartCustomLogoutHelper(this)
                .build();

        context.startActivity(intent);
    }


    //verify the username & password
    @Override
    public boolean customSignin(SmartUser user){
        ExecutorService pool = Executors.newFixedThreadPool(1);
        Callable callable = new signinWithServerCallable(user);
        Future future = pool.submit(callable);
        boolean result = false;
        try{
            result = (Boolean) future.get();
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public boolean customSignup(SmartUser newUser) {
        return true;
    }

    @Override
    public boolean customUserSignout(SmartUser smartUser) {
        Log.i("LOG_OUT","customUserSignout");
        UserSessionManager.logout((Activity) context,smartUser,this,null);
        return true;
    }

    class signinWithServerCallable implements Callable<Boolean>{
        private boolean resultSignin;
        private SmartUser user;
        public signinWithServerCallable(){
            resultSignin = false;
        }
        public signinWithServerCallable(SmartUser user){
            resultSignin = false;
            this.user = user;
        }
        @Override
        public Boolean call() throws Exception {
            try {
                Log.i(LOG_TAG,"Connect to remote server");
                URL url = new URL("http://45.78.6.243:8081/members/login.php");

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(3 * 1000);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Charset", "UTF-8");
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.connect();

                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("REQUEST_TYPE",SmartLoginConfig.CUSTOM_LOGIN_REQUEST);
                jsonObject.addProperty("USER_NAME",user.getUsername());
                jsonObject.addProperty("PASSWORD",user.getPassword());

                byte[] requestStrBytes = jsonObject.toString().getBytes("UTF-8");

                conn.setRequestProperty("Content-length", "" + requestStrBytes.length);
                conn.setRequestProperty("Content-Type", "application/octet-stream");
                conn.setRequestProperty("Connection", "Keep-Alive");// 维持长连接
                conn.setRequestProperty("Charset", "UTF-8");

                DataOutputStream dataOutputStream = new DataOutputStream(conn.getOutputStream());
                dataOutputStream.write(requestStrBytes);
                dataOutputStream.flush();
                dataOutputStream.close();

                int resultCode = conn.getResponseCode();
                if(resultCode == HttpURLConnection.HTTP_OK){
                    //Handle the response
                    StringBuffer stringBuffer = new StringBuffer();
                    String readLine = new String();
                    BufferedReader responseReader = new BufferedReader(new InputStreamReader(conn.getInputStream(),"UTF-8"));
                    while((readLine=responseReader.readLine()) != null){
                        stringBuffer.append(readLine);
                    }
                    if(stringBuffer.toString().trim().equals(LOGIN_OK)){
                        resultSignin = true;
                        Log.i(LOG_TAG,"LOGIN_OK "+ stringBuffer);
                    }else{
                        resultSignin = false;
                        Log.i(LOG_TAG,"LOGIN_FAIL "+ stringBuffer);
                    }
                        responseReader.close();
                    Log.i(LOG_TAG,"HTTP OK! Response=>" + stringBuffer);
                }else{
                    Log.i(LOG_TAG,"Result code:" + Integer.toString(resultCode));
                    resultSignin = false;
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            return resultSignin;
        }
    }

}
