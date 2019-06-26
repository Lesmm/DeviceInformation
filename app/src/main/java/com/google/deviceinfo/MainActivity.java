package com.google.deviceinfo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.deviceinfo.TelephonyManagerInfo;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        JSONObject telephonyInfo = TelephonyManagerInfo.getInfo(this);
    }
}
