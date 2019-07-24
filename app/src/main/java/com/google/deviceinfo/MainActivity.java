package com.google.deviceinfo;

import android.Manifest;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.deviceinfo.Manager;

import common.modules.util.IHandlerUtil;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (!Manager.IS_DEBUG) {
            return;
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // 未捕捉异常
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable e) {
                Log.d("DeviceInfo","-------------- uncaughtException --------------");
                e.printStackTrace();
                Log.d("DeviceInfo","-------------- uncaughtException --------------");
            }
        });

        // 申请Root权限
        try {
            Runtime.getRuntime().exec("su");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 申请权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            android.support.v4.app.ActivityCompat.requestPermissions(this, new String[]{

                    Manifest.permission.INTERNET,

                    Manifest.permission.READ_PHONE_STATE,

                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.CHANGE_WIFI_STATE,

                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_SMS,

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

        // Views
        TextView buildModelTextView = findViewById(R.id.buildModelTextView);
        buildModelTextView.setText(Build.MANUFACTURER + " - " + Build.MODEL);
        buildModelTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // ------------------------------------------------>>>
                final TextView textView = (TextView)v;
                final int KEY_IS_GETTING = R.id.buildModelTextView;
                Boolean isGetting = (Boolean)textView.getTag(KEY_IS_GETTING);
                if (isGetting != null && isGetting.booleanValue()) {
                    return;
                } else {
                    textView.setTag(KEY_IS_GETTING, true);
                }
                // ------------------------------------------------<<<

                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        // ------------------------------------------------>>>
                        final long startTime = new java.util.Date().getTime();
                        IHandlerUtil.postToMainThread(new Runnable() {
                            @Override
                            public void run() {
                                long diff = new java.util.Date().getTime() - startTime;
                                int seconds = (int)(diff / 1000);
                                textView.setText("获取中... " + seconds + "s");

                                if ((Boolean)textView.getTag(KEY_IS_GETTING)) {
                                    IHandlerUtil.postToMainThread(this);
                                } else {
                                    textView.setText(Build.MANUFACTURER + " - " + Build.MODEL);
                                }
                            }
                        });
                        // ------------------------------------------------<<<


                        Manager.grabInfoSync();


                        // ------------------------------------------------>>>
                        IHandlerUtil.postToMainThread(new Runnable() {
                            @Override
                            public void run() {
                                textView.setTag(KEY_IS_GETTING, false);
                            }
                        });
                        // ------------------------------------------------<<<

                    }
                }).start();
            }
        });

        // 开始
        Manager.checkContextLoadedApkResources(this);

        // Manager.getInfo(this);
    }

}
