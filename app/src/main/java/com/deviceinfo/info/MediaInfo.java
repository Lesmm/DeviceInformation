package com.deviceinfo.info;

import android.content.Context;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.media.AudioManager;

import org.json.JSONArray;
import org.json.JSONObject;

public class MediaInfo {

    // Audio, Camera ...
    public static JSONObject getInfo(Context mContext) {

        JSONObject info = new JSONObject();

        // Audio -----------------------------
        JSONObject audioInfoJson = new JSONObject();
        try {
            AudioManager cameraManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Camera -----------------------------
        JSONObject cameraInfoJson = new JSONObject();
        try {
            CameraManager cameraManager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);

            int numberOfCameras = android.hardware.Camera.getNumberOfCameras();
            cameraInfoJson.put("numberOfCameras", numberOfCameras);

            JSONArray cameraCharacteristicsArray = new JSONArray();
            String[] cameraIdList = cameraManager.getCameraIdList();
            for (int i = 0; i < cameraIdList.length; i++) {
                String cameraId = cameraIdList[i];
                CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);
                // Map<?, ?> map = IReflectUtil.objectFieldNameValues(cameraCharacteristics);
                // cameraCharacteristicsArray.put(map);
            }
            cameraInfoJson.put("characteristics", cameraCharacteristicsArray);

        } catch (Exception e) {
            e.printStackTrace();
        }


        try {
            info.put("Audio", audioInfoJson);
            info.put("Camera", cameraInfoJson);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return info;
    }


}
