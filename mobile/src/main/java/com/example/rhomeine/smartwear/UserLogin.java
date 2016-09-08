package com.example.rhomeine.smartwear;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.rhomeine.smartwear.Login.SmartCustomLoginListener;
import com.example.rhomeine.smartwear.Login.SmartCustomLogoutListener;
import com.example.rhomeine.smartwear.Login.SmartLoginBuilder;
import com.example.rhomeine.smartwear.Login.SmartLoginConfig;
import com.example.rhomeine.smartwear.Login.manager.UserSessionManager;
import com.example.rhomeine.smartwear.Login.users.SmartUser;
import com.example.rhomeine.smartwear.Login.util.DialogUtil;

/**
 * Created by WSPN on 2016/9/8.
 */
public class UserLogin implements SmartCustomLoginListener,SmartCustomLogoutListener{

    final static String ADMIN = "rhoybeen";
    final static String ADMIN_PWD = "rhoy2012";

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
    public boolean customSignin(SmartUser user) {
        if(user.getUsername().equals(this.ADMIN)&& user.getPassword().equals(this.ADMIN_PWD)) return true;
        return false;
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
}
