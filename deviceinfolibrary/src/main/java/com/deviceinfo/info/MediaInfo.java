package com.deviceinfo.info;

import android.content.Context;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.media.AudioManager;
import android.media.MediaDrm;
import android.util.Base64;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.UUID;

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
        try {
            info.put("Audio", audioInfoJson);
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
            info.put("Camera", cameraInfoJson);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // MediaDrm
        JSONObject drmInfoJson = new JSONObject();
        try {

            UUID uuid = new UUID(-0x121074568629b532L, -0x5c37d8232ae2de13L);
            MediaDrm drm = new MediaDrm(uuid);
            String vendor = drm.getPropertyString(MediaDrm.PROPERTY_VENDOR);
            String version = drm.getPropertyString(MediaDrm.PROPERTY_VERSION);
            String description = drm.getPropertyString(MediaDrm.PROPERTY_DESCRIPTION);
            String algorithms = drm.getPropertyString(MediaDrm.PROPERTY_ALGORITHMS);

            byte[] bytes = drm.getPropertyByteArray(MediaDrm.PROPERTY_DEVICE_UNIQUE_ID);
            byte[] encodeBytes = Base64.encode(bytes, Base64.DEFAULT);
            String deviceUniqueId = new String(encodeBytes);

            drmInfoJson.put("vendor", vendor);
            drmInfoJson.put("version", version);
            drmInfoJson.put("description", description);
            drmInfoJson.put("algorithms", algorithms);
            drmInfoJson.put("deviceUniqueId", deviceUniqueId);
            drmInfoJson.put("deviceUniqueId_isBase64Encoded", true);

        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            info.put("DRM", drmInfoJson);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return info;
    }


}
