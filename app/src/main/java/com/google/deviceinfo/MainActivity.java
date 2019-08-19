package com.google.deviceinfo;

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

import com.deviceinfo.Manager;

import org.json.JSONObject;

import common.modules.util.IFileUtil;
import common.modules.util.IHandlerUtil;

public class MainActivity extends Activity {

    public boolean isForGuiApp = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        if (!Manager.IS_DEBUG) {
//            return;
//        }
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


        // 申请Root权限
//        try {
//            Runtime.getRuntime().exec("su");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }


        // 申请权限
        boolean isAllRuntimePermissionGranted = checkRuntimePermissions();


        // Views
        TextView buildModelTextView = findViewById(R.id.buildModelTextView);
        buildModelTextView.setText(Build.MANUFACTURER + " - " + Build.MODEL);
        buildModelTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isForGuiApp) {
                    return;
                }
                // ------------------------------------------------>>>
                final TextView textView = (TextView) v;
                final int KEY_IS_GETTING = R.id.buildModelTextView;
                Boolean isGetting = (Boolean) textView.getTag(KEY_IS_GETTING);
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
                                int seconds = (int) (diff / 1000);
                                textView.setText("获取中... " + seconds + "s");

                                if ((Boolean) textView.getTag(KEY_IS_GETTING)) {
                                    IHandlerUtil.postToMainThread(this);
                                } else {
                                    textView.setText(Build.MANUFACTURER + " - " + Build.MODEL);
                                }
                            }
                        });
                        // ------------------------------------------------<<<


                        // Manager.grabInfoSync();

//                        JSONObject jsonObject = Manager.getInfo();
//                        IFileUtil.writeTextToFile(jsonObject.toString(), "/sdcard/phoneInfo.json");

                        // JSONObject filesInfos = com.deviceinfo.info.HardwareInfo.getInfoInFiles(Manager.getApplication());
                        // JSONObject mountInfos = com.deviceinfo.info.MountServiceInfo.getInfo(Manager.getApplication());

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

        final TextView deviceIdTextView = findViewById(R.id.deviceIdTextView);
        final TextView subscriberIdTextView = findViewById(R.id.subscriberIdTextView);
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

        // 测试代码
        // Manager.checkContextLoadedApkResources(this);

        // 开始
        final MainActivityLoadingView loadingView = findViewById(R.id.loadingView);
        if (!isForGuiApp) {
            loadingView.setVisibility(View.INVISIBLE);
            return;
        }
        loadingView.setVisibility(View.VISIBLE);
        loadingView.selectedShapeId = R.drawable.shape_circle_deeppink;
        loadingView.selectedSvgId = R.drawable.svg_loading_tadpole_green;
        loadingView.startAnimation();
        loadingView.setLoadingText("评分中...");

        final int maxSecond = new java.util.Random().nextInt(8) + 5;
        final int[] count = new int[1];
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                if (count[0] < maxSecond) {
                    count[0]++;
                    loadingView.setLoadingText("评分中" + count[0] + "s...");
                    new Handler(Looper.getMainLooper()).postDelayed(this, 1000);
                } else {
                    int mark = new java.util.Random().nextInt(40) + 60;
                    loadingView.setLoadingText("本次得分: " + mark);
                    loadingView.stopAnimation();

                    deviceIdTextView.setText("");
                    subscriberIdTextView.setText("本次得分: " + mark + "分");
                }
            }
        }, 1000);

        if (isAllRuntimePermissionGranted) {
            Manager.grabInfoAsync();
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
            if (isAllgranted == false) {
                Toast.makeText(this, "请先赋予所有权限并重新打开APP，再跑分!", Toast.LENGTH_LONG).show();
                if (isForGuiApp) {
                    finish();
                }
            } else {
                if (isForGuiApp) {
                    Manager.grabInfoAsync();
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

}
