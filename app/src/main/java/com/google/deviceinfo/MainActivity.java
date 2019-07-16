package com.google.deviceinfo;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.deviceinfo.info.PackageManagerInfo;
import com.deviceinfo.info.SubscriptionManagerInfo;
import com.deviceinfo.info.TelephonyManagerInfo;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 未捕捉异常
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable e) {
                e.printStackTrace();
            }
        });

        // 申请权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            android.support.v4.app.ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.INTERNET,

                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.CHANGE_WIFI_STATE,

                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_SMS,
                    Manifest.permission.READ_PHONE_STATE,

                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.WRITE_CONTACTS,

                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,

                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,

                    Manifest.permission.BODY_SENSORS,
                    Manifest.permission.RECORD_AUDIO,
            }, 1);
        }

        // 申请Root权限
        try {
            Runtime.getRuntime().exec("su");
        } catch (Exception e) {
            e.printStackTrace();
        }

        getInfo();
    }

    public void getInfo() {
        try {

            JSONObject result = new JSONObject();

            JSONObject telephonyInfo = TelephonyManagerInfo.getInfo(this);
            JSONObject subscriptionInfo = SubscriptionManagerInfo.getInfo(this);
            JSONObject packageInfo = PackageManagerInfo.getInfo(this);

            result.put("Telephony", telephonyInfo);
            result.put("Subscription", subscriptionInfo);
            result.put("Package", packageInfo);

            Log.d("", "");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
