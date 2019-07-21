package com.deviceinfo.info;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;

import com.deviceinfo.JSONArrayExtended;

import org.json.JSONArray;

import java.util.List;

public class SensorsInfo {

    // TODO ... Hook 那边 可从 SystemServiceRegistry 入手，把 getSystemService 的值给替换了，把 SystemSensorManager 的属性 mFullSensorsList 给换了。
    public static JSONArray getInfo(Context mContext) {

        // SystemSensorManager
        SensorManager sensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ALL);

        return new JSONArrayExtended(sensors);

    }

}
