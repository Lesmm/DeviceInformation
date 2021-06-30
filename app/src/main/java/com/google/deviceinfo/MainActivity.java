package com.google.deviceinfo;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;

import common.modules.util.IBroadcastReciverWaitor;
import common.modules.util.IFileUtil;
import common.modules.util.IFileUtilEx;
import common.modules.util.IHandlerUtil;
import common.modules.util.IReflectUtil;
import dalvik.system.DexClassLoader;

public class MainActivity extends Activity {

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

// -------- TODO ... Comment Here while Build DeviceInfo.apk/dex/jar --------
        // 申请权限
        boolean isAllRuntimePermissionGranted = checkRuntimePermissions();
        if (isAllRuntimePermissionGranted) {
            setupViewsData();
        } else {
            IBroadcastReciverWaitor.waitFor(2 * 60 * 1000, "__permissions_granted__", new IBroadcastReciverWaitor.AsyncWaitor() {
                @Override
                public boolean onReceive(Context context, Intent intent) {
                    setupViewsData();
                    return super.onReceive(context, intent);
                }
            });
        }
// -------- TODO ... Comment Here while Build DeviceInfo.apk/dex/jar --------

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1000) {
            boolean isAllgranted = true;
            for (int grantResult : grantResults) {
                isAllgranted = isAllgranted && (grantResult == PackageManager.PERMISSION_GRANTED);
            }
            if (!isAllgranted) {
                Toast.makeText(this, "请先赋予所有权限并重新打开APP，再跑分!", Toast.LENGTH_LONG).show();
            }
        }

        Intent intent = new Intent("__permissions_granted__")
                .putExtra("requestCode", requestCode)
                .putExtra("permissions", permissions)
                .putExtra("grantResults", grantResults);
        this.sendBroadcast(intent);

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    // -------- TODO ... Comment Here while Build DeviceInfo.apk/dex/jar --------
    public boolean checkRuntimePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
            ) {

                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.INTERNET,

                        Manifest.permission.READ_PHONE_STATE,

                        Manifest.permission.ACCESS_WIFI_STATE,
                        Manifest.permission.CHANGE_WIFI_STATE,

                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,

                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                }, 1000);

                return false;
            } else {
                return true;
            }
        }

        return true;
    }
// -------- TODO ... Comment Here while Build DeviceInfo.apk/dex/jar --------


    public void setupViewsData() {
        TextView buildModelTextView = findViewById(R.id.buildModelTextView);
        buildModelTextView.setText(Build.MANUFACTURER + " - " + Build.MODEL);

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

        final TextView statusTextView = findViewById(R.id.statusTextView);
        statusTextView.setText("获取中...");
        final long startTime = new java.util.Date().getTime();
        IHandlerUtil.postToMainThread(new Runnable() {
            @Override
            public void run() {
                if (statusTextView.getText().toString().contains("获取完毕")) {
                    return;
                }
                int seconds = (int) ((System.currentTimeMillis() - startTime) / 1000);
                statusTextView.setText("获取中... " + seconds + "s");
                IHandlerUtil.postToMainThread(this, 1000);
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                IBroadcastReciverWaitor.waitFor(2 * 60 * 1000, "__grab_progress__", new IBroadcastReciverWaitor.AsyncWaitor() {
                    @Override
                    public boolean onReceive(Context context, Intent intent) {
                        Log.d("DeviceInfo", "receiver ~~~");
                        if (intent != null) {
                            String message = intent.getStringExtra("message");
                            Log.d("DeviceInfo", "receiver: " + message);
                            if (message != null) {
                                if (message.contains("Done")) {
                                    IHandlerUtil.postToMainThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            statusTextView.setText("获取完毕!");
                                            Log.d("DeviceInfo", "receiver: set 获取完毕! ");
                                        }
                                    });
                                    return false;
                                }
                            }
                        }
                        return true;
                    }
                });

                Log.d("DeviceInfo", "receiver thread done ~~~");
            }
        }).start();

        // 1. check api
        // call grab api immediately -------------------
        Log.d("DeviceInfo", "call dex immediately success ~~~");
        com.facade.Manager.grabInfoAsync();
        // call grab api immediately -------------------


        // 2. check dex
        // ------------------- call grab api using dexloader -------------------
//        String dexFileName = "DeviceInfo.jar";
//        String cacheDirectory = getCacheDir() + "/";
//        String deviceInfoDexFile = cacheDirectory + dexFileName;
//        boolean isFileExisted = new File(deviceInfoDexFile).exists();
//        try {
//            // or user assets or just push jar file to cache dir
//            if (!isFileExisted) {
//                AssetManager assetManager = getAssets();
//                if (Arrays.asList(assetManager.list("dex/")).contains(dexFileName)) {
//                    InputStream inputStream = assetManager.open("dex/" + dexFileName);
//                    IFileUtil.writeInputStreamToFile(inputStream, deviceInfoDexFile);
//                    IFileUtilEx.chmod777(deviceInfoDexFile);
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        isFileExisted = new File(deviceInfoDexFile).exists();
//        Log.d("DeviceInfo", "is dex file existed " + deviceInfoDexFile + " : " + isFileExisted);
//        try {
//            if (isFileExisted) {
//                DexClassLoader dexLoader = new DexClassLoader(deviceInfoDexFile, cacheDirectory, null,
//                        MainActivity.class.getClassLoader());
//                Class<?> managerClass = dexLoader.loadClass("com.facade.Manager");
//                IReflectUtil.invokeMethod(managerClass, "grabInfoAsync", null, null);
//                Log.d("DeviceInfo", "call dex api success ~~~");
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        // ------------------- call grab api using dexloader -------------------

    }

}
