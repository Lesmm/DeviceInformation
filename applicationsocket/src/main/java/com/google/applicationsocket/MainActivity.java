package com.google.applicationsocket;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.deviceinfo.ManagerInfo;
import com.google.applicationsocket.utils.AlertDialogUtils;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {

    private boolean isDebug = true;
    private boolean isSending = false;

    private com.github.ybq.android.spinkit.SpinKitView spinKitView;
    private BroadcastReceiver broadcastReceiver;

    private EditText deviceIdEditText;
    private EditText submitIpEditText;
    private EditText submitPortEditText;

    private TextView acceptingTextView;
    private Button sendButton;

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

    private StatusHandler acceptingStatusHandler = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        acceptingStatusHandler = new StatusHandler(this);

        // 申请权限
        boolean isAllRuntimePermissionGranted = checkRuntimePermissions();

        // view
        spinKitView = findViewById(R.id.spin_kit);

        deviceIdEditText = findViewById(R.id.deviceIdEditText);
        submitIpEditText = findViewById(R.id.submitIpEditText);
        submitPortEditText = findViewById(R.id.submitPortEditText);
        acceptingTextView = findViewById(R.id.acceptingTextView);
        sendButton = findViewById(R.id.sendButton);

        // data
        try {
            String imei = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
            deviceIdEditText.setText(imei);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (isDebug) {
            submitIpEditText.setText("192.168.4.128");
        }

        // event
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String ipString = submitIpEditText.getText().toString();
                String portString = submitPortEditText.getText().toString();

                if (ipString == null || ipString.isEmpty() || portString == null || portString.isEmpty()) {
                    AlertDialogUtils.show(MainActivity.this, "提示", "请填写地址及端口",
                            "确定", null);
                    return;
                }

                sendEvent();
            }
        });

        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {

                } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {

                    if (isSending) {
                        spinKitView.getIndeterminateDrawable().start();
                    }

                }
            }
        };
        registerReceiver(broadcastReceiver, intentFilter);

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                if (isSending) {
                    spinKitView.getIndeterminateDrawable().start();
                } else {
                    spinKitView.getIndeterminateDrawable().stop();
                }

                new Handler().postDelayed(this, 500);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }

    private void sendEvent() {
        if (isSending) {
            return;
        }

        final String ipString = submitIpEditText.getText().toString();
        final String portString = submitPortEditText.getText().toString();

        Thread sendingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                isSending = true;

                Message.obtain(acceptingStatusHandler, -1, "").sendToTarget();
                Message.obtain(acceptingStatusHandler, 0, "正在获取信息...\n").sendToTarget();


                JSONObject jsonObject = ManagerInfo.getInfo(ManagerInfo.getApplication());
                String contents = jsonObject.toString();
                // IFileUtil.writeTextToFile(contents, "/sdcard/phoneInfo.json");
                Message.obtain(acceptingStatusHandler, 0, "正在发送信息...\n").sendToTarget();

                Socket socket = null;
                InputStream in = null;
                OutputStream out = null;

                try {

                    try {
                        socket = new Socket(InetAddress.getByName(ipString), Integer.valueOf(portString));
                        in = socket.getInputStream();
                        out = socket.getOutputStream();
                    } catch (Exception e) {
                        Message.obtain(acceptingStatusHandler, 0, "请检查地址及端口, 连接失败!\n").sendToTarget();
                        throw e;
                    }

                    byte[] buffer = new byte[128 * 1024];
                    int len = -1;


                    // 判断是否可以发送
                    while ((len = in.read(buffer, 0, buffer.length)) != -1) {
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        byteArrayOutputStream.write(buffer, 0, len);
                        String str = new String(byteArrayOutputStream.toByteArray(), "utf-8");
                        if (str.contains("ready2Send")) {
                            break;
                        }
                    }


                    // 发送
                    out.write(contents.getBytes("UTF-8"));
                    out.flush();

                    out.write("___I_Write_Done___".getBytes("UTF-8"));
                    out.flush();


                    // 判断是否为结束标记
                    while ((len = in.read(buffer, 0, buffer.length)) != -1) {
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        byteArrayOutputStream.write(buffer, 0, len);
                        String str = new String(byteArrayOutputStream.toByteArray(), "utf-8");
                        if (str.contains("ready2Exit")) {
                            break;
                        }
                    }
                    Message.obtain(acceptingStatusHandler, 0, "发送成功!\n").sendToTarget();

                } catch (Exception e) {
                    e.printStackTrace();
                    Message.obtain(acceptingStatusHandler, 0, "发送失败!\n").sendToTarget();

                } finally {
                    try {
                        if (out != null) {
                            out.close();
                        }
                        if (socket != null) {
                            socket.close();
                        }
                    } catch (IOException e3) {
                        e3.printStackTrace();
                    }
                    isSending = false;
                }

            }
        });
        sendingThread.setName("sending-thread");
        sendingThread.start();
    }

    public boolean checkRuntimePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED
//                    || checkSelfPermission(Manifest.permission.BODY_SENSORS) != PackageManager.PERMISSION_GRANTED
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

                        Manifest.permission.CAMERA,
//                    Manifest.permission.READ_SMS,
//
                        Manifest.permission.READ_CONTACTS,
//                    Manifest.permission.WRITE_CONTACTS,
//
                        Manifest.permission.BODY_SENSORS,
                        Manifest.permission.RECORD_AUDIO,

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
                Toast.makeText(this, "请先赋予所有权限并重新打开APP!", Toast.LENGTH_LONG).show();
                finish();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

}
