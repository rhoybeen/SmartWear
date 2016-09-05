package com.example.rhomeine.smartwear;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class TestActivity extends AppCompatActivity{

    private Button button;
    private TextView textView;
    private String LOG_TAG = "HTTP";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        textView = (TextView) findViewById(R.id.text_net);
        button = (Button) findViewById(R.id.btn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendToHttpServer();
            }
        });


    }

    private void sendToHttpServer() {
        new Thread(){
            @Override
            public void run() {
                try {
                    URL url = new URL("http://45.78.6.243:8081");
                    String param = "rhoybeen";
                    Log.i(LOG_TAG,"try http conn");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(3 * 1000);
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Charset", "UTF-8");
                    conn.setDoOutput(true);
                    conn.connect();

                    DataOutputStream dataOutputStream = new DataOutputStream(conn.getOutputStream());
                    dataOutputStream.writeBytes(param);
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
                        responseReader.close();
                        Log.i(LOG_TAG,"HTTP OK! Response=>" + stringBuffer);
                    }else {
                        Log.i(LOG_TAG,"Result code:" + Integer.toString(resultCode));
                    }

                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }.start();

    }
}
