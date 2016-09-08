package com.example.rhomeine.smartwear;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rhomeine.smartwear.Login.SmartCustomLogoutListener;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.ArrayList;

import com.example.rhomeine.smartwear.Login.SmartCustomLoginListener;
import com.example.rhomeine.smartwear.Login.SmartLoginBuilder;
import com.example.rhomeine.smartwear.Login.SmartLoginConfig;
import com.example.rhomeine.smartwear.Login.manager.UserSessionManager;
import com.example.rhomeine.smartwear.Login.users.SmartFacebookUser;
import com.example.rhomeine.smartwear.Login.users.SmartGoogleUser;
import com.example.rhomeine.smartwear.Login.users.SmartUser;

public class LoginWebViewActivity extends AppCompatActivity implements SmartCustomLoginListener,SmartCustomLogoutListener {

    TextView loginResult;
    CheckBox customLogin, facebookLogin, googleLogin, appLogoCheckBox;
    SmartUser currentUser;
    GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_web_view);

        Button loginButton = (Button) findViewById(R.id.login_button);
        loginResult = (TextView) findViewById(R.id.login_result);
        customLogin = (CheckBox) findViewById(R.id.customCheckbox);
        facebookLogin = (CheckBox) findViewById(R.id.facebookCheckbox);
        googleLogin = (CheckBox) findViewById(R.id.googleCheckbox);
        appLogoCheckBox = (CheckBox) findViewById(R.id.appLogoCheckbox);

        //get the current user details
        currentUser = UserSessionManager.getCurrentUser(this);
        String display = "no user";
        if(currentUser != null) {
            if (currentUser instanceof SmartFacebookUser) {
                SmartFacebookUser facebookUser = (SmartFacebookUser) currentUser;
                display = facebookUser.getProfileName() + " (FacebookUser)is logged in";
            } else if (currentUser instanceof SmartGoogleUser) {
                display = ((SmartGoogleUser) currentUser).getDisplayName() + " (GoogleUser) is logged in";
            } else {
                display = currentUser.getUsername() + " (Custom User) is logged in";
            }
        }
        loginResult.setText(display);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, null)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        if (loginButton != null) {
            loginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (currentUser != null) {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(LoginWebViewActivity.this);
                        builder.setMessage(R.string.user_exists);
                        builder.setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                UserSessionManager.logout(LoginWebViewActivity.this, currentUser,LoginWebViewActivity.this,mGoogleApiClient);
                                currentUser = UserSessionManager.getCurrentUser(LoginWebViewActivity.this);
                            }
                        });
                        builder.setCancelable(true);

                        builder.create().show();
                    } else {

                        SmartLoginBuilder loginBuilder = new SmartLoginBuilder();

                        //Set facebook permissions
                        ArrayList<String> permissions = new ArrayList<>();
                        permissions.add("public_profile");
                        permissions.add("email");
                        permissions.add("user_birthday");
                        permissions.add("user_friends");


                        Intent intent = loginBuilder.with(getApplicationContext())
                                .setAppLogo(getlogo())
                                .isFacebookLoginEnabled(facebookLogin.isChecked())
                                .withFacebookAppId(getString(R.string.facebook_app_id)).withFacebookPermissions(permissions)
                                .isGoogleLoginEnabled(googleLogin.isChecked())
                                .isCustomLoginEnabled(customLogin.isChecked(),SmartLoginConfig.LoginType.withUsername)
                                .setSmartCustomLoginHelper(LoginWebViewActivity.this)
                                .build();

                        startActivityForResult(intent, SmartLoginConfig.LOGIN_REQUEST);
                        //startActivity(intent);
                    }
                }
            });
        }
    }

    private int getlogo() {
        if(appLogoCheckBox.isChecked()){
            return R.mipmap.ic_launcher;
        }
        return 0;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
  //      getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        String fail = "Login Failed";
        if(resultCode == SmartLoginConfig.FACEBOOK_LOGIN_REQUEST){
            SmartFacebookUser user;
            try {
                user = data.getParcelableExtra(SmartLoginConfig.USER);
                String userDetails = user.getProfileName() + " " + user.getEmail() + " " + user.getBirthday();
                loginResult.setText(userDetails);
            }catch (Exception e){
                loginResult.setText(fail);
            }
        }
        else if(resultCode == SmartLoginConfig.GOOGLE_LOGIN_REQUEST){
            SmartGoogleUser user = data.getParcelableExtra(SmartLoginConfig.USER);
            String userDetails = user.getEmail() + " " + user.getDisplayName();
            loginResult.setText(userDetails);
        }
        else if(resultCode == SmartLoginConfig.CUSTOM_LOGIN_REQUEST){
            SmartUser user = data.getParcelableExtra(SmartLoginConfig.USER);
            String userDetails = user.getUsername() + " (Custom User)";
            loginResult.setText(userDetails);
        }
        /*else if(resultCode == SmartLoginConfig.CUSTOM_SIGNUP_REQUEST){
            SmartUser user = data.getParcelableExtra(SmartLoginConfig.USER);
            String userDetails = user.getUsername() + " (Custom User)";
            loginResult.setText(userDetails);
        }*/
        else if(resultCode == RESULT_CANCELED){
            loginResult.setText(fail);
        }

    }

    @Override
    public boolean customUserSignout(SmartUser smartUser) {
        //Implement your logic
        UserSessionManager.logout(this, smartUser,this,mGoogleApiClient);
        return true;
    }


    @Override
    public boolean customSignin(SmartUser user) {
        //This "user" will have only username and password set.
        Toast.makeText(LoginWebViewActivity.this, user.getUsername() + " " + user.getEmail(), Toast.LENGTH_SHORT).show();
        return true;
    }

    @Override
    public boolean customSignup(SmartUser newUser) {
        //Implement your our custom sign up logic and return true if success
        Log.i("Login",newUser.getEmail()+"registered!");
        return true;
    }

//    private class MyTask extends AsyncTask<Void,Void,Boolean>{
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//        }
//
//        @Override
//        protected Boolean doInBackground(Void... params) {
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(Boolean aBoolean) {
//            final String url = "http://45.78.6.243:8081/member/login.php";
//            webView.loadUrl(url);
//
//        }
//    }


}
