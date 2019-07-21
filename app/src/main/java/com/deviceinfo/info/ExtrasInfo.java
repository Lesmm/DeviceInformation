package com.deviceinfo.info;

import android.content.Context;
import android.os.Looper;
import android.webkit.WebSettings;
import android.webkit.WebView;

import org.json.JSONObject;

public class ExtrasInfo {

    public static JSONObject getInfo(final Context mContext) {

        final JSONObject info = new JSONObject();

        // WebKit.UserAgent
        boolean isInMainThread = Looper.getMainLooper() == Looper.myLooper();
        if (isInMainThread) {

            // -------------------- the same ------------------
            String userAgent = new WebView(mContext).getSettings().getUserAgentString();
            try {
                info.put("WebKit.UserAgent", userAgent);
            } catch (Exception e) {
                e.printStackTrace();
            }
            // -------------------- the same ------------------

        } else {

            new android.os.Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {

                    // -------------------- the same ------------------
                    String userAgent = new WebView(mContext).getSettings().getUserAgentString();
                    try {
                        info.put("WebKit.UserAgent", userAgent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    // -------------------- the same ------------------

                    // go on ...
                    synchronized (ExtrasInfo.class) {
                        try {
                            ExtrasInfo.class.notify();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                }
            });

            // wait ....
            synchronized (ExtrasInfo.class) {
                try {
                    ExtrasInfo.class.wait();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }


        return info;
    }

}
