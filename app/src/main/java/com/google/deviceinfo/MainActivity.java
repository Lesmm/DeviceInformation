package com.google.deviceinfo;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.ConfigurationInfo;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.deviceinfo.InvokerOfService;
import com.deviceinfo.info.AndroidInternalResourcesInfo;
import com.deviceinfo.info.BuildInfo;
import com.deviceinfo.info.ConnectivityManagerInfo;
import com.deviceinfo.info.HardwareInfo;
import com.deviceinfo.info.PackageManagerInfo;
import com.deviceinfo.info.SubscriptionManagerInfo;
import com.deviceinfo.info.TelephonyManagerInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import common.modules.util.IReflectUtil;

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

        // 开始
        checkContextLoadedApkResources();

        getInfo();
    }

    public void checkContextLoadedApkResources() {
        Application application = getApplication();
        Context baseContext = getBaseContext();
        Context applicationContext = getApplicationContext();

        Context baseContextApplicationContext =  baseContext.getApplicationContext();
        Context applicationContextApplicationContext = applicationContext.getApplicationContext();

        Context applicationBaseContext = application.getBaseContext();
        Context applicationApplicationContext = application.getApplicationContext();

        Resources resources = getResources();
        Resources applicationResources = application.getResources();
        Resources baseContextResources = baseContext.getResources();
        Resources applicationContextResources = applicationContext.getResources();
        Resources applicationBaseContextResources = applicationBaseContext.getResources();
        Resources applicationApplicationContextResources = applicationApplicationContext.getResources();


        Object loadedApk1 = IReflectUtil.getFieldValue(baseContext, "mPackageInfo");
        Object loadedApk2 = IReflectUtil.getFieldValue(applicationBaseContext, "mPackageInfo");

        Object mResources1 = IReflectUtil.getFieldValue(loadedApk1, "mResources");
        Object mResources2 = IReflectUtil.getFieldValue(loadedApk2, "mResources");

        Log.d("DeviceInfo","_set_debug_here_");
    }

    public void getInfo() {
        try {
            JSONObject result = new JSONObject();

            JSONObject buildInfo = BuildInfo.getBuildInfo(this);
            JSONObject buildVersionInfo = BuildInfo.getBuildVersionInfo(this);
            result.put("Build", buildInfo);
            result.put("Build.VERSION", buildVersionInfo);

            JSONObject telephonyInfo = TelephonyManagerInfo.getInfo(this);
            JSONObject subscriptionInfo = SubscriptionManagerInfo.getInfo(this);
            JSONObject packageInfo = PackageManagerInfo.getInfo(this);
            JSONObject connectivityInfo = ConnectivityManagerInfo.getInfo(this);
            result.put("Telephony", telephonyInfo);
            result.put("Subscription", subscriptionInfo);
            result.put("Package", packageInfo);
            result.put("Connectivity", connectivityInfo);

            JSONObject androidInternalResourcesInfo = AndroidInternalResourcesInfo.getInfo(this);
            result.put("ResourcesValues", androidInternalResourcesInfo);

            JSONObject filesInfos = HardwareInfo.getInfoInFiles();
            JSONObject commandsInfos = HardwareInfo.getInfoInCommands();
            result.put("Files.Contents", filesInfos);
            result.put("Commands.Contents", commandsInfos);

            Log.d("DeviceInfo","_set_debug_here_");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
