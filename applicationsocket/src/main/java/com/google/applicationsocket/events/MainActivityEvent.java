package com.google.applicationsocket.events;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Message;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.deviceinfo.ManagerInfo;
import com.google.applicationsocket.MainActivity;
import com.google.applicationsocket.utils.IpScanner;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Set;

import common.modules.util.IActivityUtil;

public class MainActivityEvent {

    private MainActivity activity;

    public boolean isSending = false;

    public MainActivityEvent(MainActivity activity) {
        this.activity = activity;
    }

    public void sendEvent(final String ipString, final String portString, final String imei) {
        if (isSending) {
            return;
        }

        if (imei == null || imei.isEmpty()) {
            Message.obtain(activity.acceptingStatusHandler, 0, "请赋予权限，获取IMEI失败!\n").sendToTarget();
            return;
        }

        Thread sendingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                isSending = true;

                Message.obtain(activity.acceptingStatusHandler, -1, "").sendToTarget();
                Message.obtain(activity.acceptingStatusHandler, 0, "正在获取信息...\n").sendToTarget();


                JSONObject jsonObject = ManagerInfo.getInfo(IActivityUtil.getApplication());
                String contents = jsonObject.toString();
                // IFileUtil.writeTextToFile(contents, "/sdcard/phoneInfo.json");
                Message.obtain(activity.acceptingStatusHandler, 0, "正在发送信息...\n").sendToTarget();

                Socket socket = null;
                InputStream in = null;
                OutputStream out = null;

                try {

                    try {
                        socket = new Socket(InetAddress.getByName(ipString), Integer.valueOf(portString));
                        in = socket.getInputStream();
                        out = socket.getOutputStream();
                    } catch (Exception e) {
                        Message.obtain(activity.acceptingStatusHandler, 0, "请检查地址及端口, 连接失败!\n").sendToTarget();
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
                    Message.obtain(activity.acceptingStatusHandler, 0, "发送成功!\n").sendToTarget();

                } catch (Exception e) {
                    e.printStackTrace();
                    Message.obtain(activity.acceptingStatusHandler, 0, "发送失败!\n").sendToTarget();

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

    public void startScanOpenPort() {
        IpScanner mIpScanner = new IpScanner(44566, new IpScanner.ScanCallback() {
            @Override
            public void onFoundOne(String hostIp, int scanPort, String foundIp) {
                Log.d("-----", "" + foundIp);
            }

            @Override
            public void onFoundDone(String hostIp, int scanPort, Set<String> foundIps) {
                Log.d("-----", "" + foundIps.toString());
            }
            @Override
            public void onFoundError(String hostIp, int scanPort, Exception e) {
                Log.d("----", "" + e.toString());
            }
        });
        mIpScanner.startScan();
    }

    public boolean checkRuntimePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] permissionWeNeed = getPermissionWeNeed();
            boolean isAllGranted = true;
            for (String permission : permissionWeNeed) {
                isAllGranted = isAllGranted && (activity.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED);
            }
            if (!isAllGranted) {
                String[] permissionsAskToGrant = getPermissionWeAskToGrant();
                ActivityCompat.requestPermissions(activity, permissionsAskToGrant, 1000);
                return false;
            } else {
                return true;
            }
        }

        return true;
    }

    public static String[] getPermissionWeNeed() {
        return new String[]{
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_WIFI_STATE
        };
    }

    public static String[] getPermissionWeAskToGrant() {
        return new String[]{
                Manifest.permission.INTERNET,

                Manifest.permission.READ_PHONE_STATE,

                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE,

                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,

                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,

                Manifest.permission.CAMERA,

                Manifest.permission.READ_CONTACTS,

                Manifest.permission.BODY_SENSORS,

                Manifest.permission.RECORD_AUDIO,

        };
    }

}
