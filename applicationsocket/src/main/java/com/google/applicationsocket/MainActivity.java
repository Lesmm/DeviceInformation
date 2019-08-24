package com.google.applicationsocket;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.applicationsocket.events.MainActivityEvent;
import com.google.applicationsocket.utils.AlertDialogUtils;
import com.google.applicationsocket.utils.AndroidIdGenerator;
import com.google.applicationsocket.utils.MicroMsgGuid;
import com.google.applicationsocket.utils.NetworkUtils;

import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity {

    private com.github.ybq.android.spinkit.SpinKitView spinKitView;
    private BroadcastReceiver broadcastReceiver;

    private EditText guidEditText;
    private EditText deviceIdEditText;
    private EditText submitIpEditText;
    private EditText submitPortEditText;

    private TextView acceptingTextView;
    private Button sendButton;

    private MainActivityEvent event;

    static class StatusHandler extends android.os.Handler {

        private WeakReference<MainActivity> activity;

        public StatusHandler(MainActivity activity) {
            this.activity = new WeakReference<MainActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            TextView textView = activity.get().acceptingTextView;

            int what = msg.what;
            if (what == -1) {
                textView.setText("");
            }

            String string = (String) msg.obj;
            textView.append(string);
            if (textView.getLineCount() >= 8) {
                String contents = textView.getText().toString();
                int firstLine = contents.indexOf("\n");
                contents = contents.substring(firstLine + 1);
                textView.setText(contents);
            }
        }
    }

    public StatusHandler acceptingStatusHandler = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        acceptingStatusHandler = new StatusHandler(this);
        event = new MainActivityEvent(this);

        // 申请权限
        event.checkRuntimePermissions();

        // view
        spinKitView = findViewById(R.id.spin_kit);
        guidEditText = findViewById(R.id.guidEditText);
        deviceIdEditText = findViewById(R.id.deviceIdEditText);
        submitIpEditText = findViewById(R.id.submitIpEditText);
        submitPortEditText = findViewById(R.id.submitPortEditText);
        acceptingTextView = findViewById(R.id.acceptingTextView);
        sendButton = findViewById(R.id.sendButton);

        // data
        setData();

        // event
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String ipString = submitIpEditText.getText().toString();
                final String portString = submitPortEditText.getText().toString();

                if (ipString == null || ipString.isEmpty() || portString == null || portString.isEmpty()) {
                    AlertDialogUtils.show(MainActivity.this, "提示", "请填写地址及端口",
                            "确定", null);
                } else {
                    AlertDialogUtils.show(MainActivity.this, "提示", "请确保地址及端口填写正确!?",
                            "确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    event.sendEvent(ipString, portString, MicroMsgGuid.getDeviceId(MainActivity.this));
                                }
                            },
                            "取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // nothing ...
                                }
                            });
                }
            }
        });

//        event.startScanOpenPort();

        // 处理Loading View -----------------------------
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                    if (event.isSending) {
                        spinKitView.getIndeterminateDrawable().start();
                    }
                }
            }
        };
        registerReceiver(broadcastReceiver, intentFilter);

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                if (event.isSending) {
                    spinKitView.getIndeterminateDrawable().start();
                } else {
                    spinKitView.getIndeterminateDrawable().stop();
                }
                new Handler().postDelayed(this, 500);
            }
        });
        // 处理Loading View -----------------------------

        // AndroidIdGenerator.test();
    }

    private void setData() {
        String guid = MicroMsgGuid.get_guid(this);
        guidEditText.setText(guid);

        String imei = null; // MicroMsgGuid.getDeviceId(MainActivity.this)
        try {
            imei = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
            deviceIdEditText.setText(imei);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (imei == null || imei.isEmpty()) {
            Message.obtain(acceptingStatusHandler, 0, "获取IMEI失败\n").sendToTarget();
        }
        final String fIMEI = imei;

        submitIpEditText.setText(NetworkUtils.getLocalIpAddress(this));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // 处理Loading View -----------------------------
        unregisterReceiver(broadcastReceiver);
        // 处理Loading View -----------------------------
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1000) {
            boolean isAllgranted = true;

            String[] permissionsWeNeed = MainActivityEvent.getPermissionWeNeed();
            String permissionsWeNeedString = "";
            for (String permission : permissionsWeNeed) {
                permissionsWeNeedString += permission;
            }

            for (int i = 0; i < grantResults.length; i++) {
                if (permissionsWeNeedString.contains(permissions[i])) {
                    int grantResult = grantResults[i];
                    isAllgranted = isAllgranted && (grantResult == PackageManager.PERMISSION_GRANTED);
                }
            }

            // refresh data
            setData();

            if (isAllgranted == false) {
                Toast.makeText(this, "请先赋予所有权限并重新打开APP!", Toast.LENGTH_LONG).show();
                finish();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

}
