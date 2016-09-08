package com.example.rhomeine.smartwear.Login.util;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import com.example.rhomeine.smartwear.Login.SmartLoginActivity;
import com.example.rhomeine.smartwear.Login.SmartLoginBuilder;
import com.example.rhomeine.smartwear.Login.users.SmartUser;

/**
 * Created by Kalyan on 9/27/2015.
 */
public class DialogUtil {
    public static Dialog getErrorDialog(int errorCode, Activity activity){
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(errorCode);
        builder.setPositiveButton("OK", null);
        return builder.create();
    }

    public static Dialog getSignOutDialog(SmartUser user,Activity activity){
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        final SmartUser smartUser = user;
        builder.setTitle("Notice");
        builder.setMessage(user.getUsername()+" has signed in already");
        builder.setPositiveButton("Log out", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                SmartLoginBuilder.smartCustonLogoutListener.customUserSignout(smartUser);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setCancelable(true);
        return builder.create();
    }
}
