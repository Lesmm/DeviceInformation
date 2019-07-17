package com.deviceinfo.info;

import android.content.Context;
import android.os.Build;

import org.json.JSONObject;

import common.modules.util.IReflectUtil;

public class BuildInfo {

    public static JSONObject getBuildInfo(Context mContext) {

        JSONObject buildJson = new JSONObject(IReflectUtil.objectFieldNameValues(Build.class));

        return buildJson;
    }

    public static JSONObject getBuildVersionInfo(Context mContext) {

        JSONObject buildVersionJson = new JSONObject(IReflectUtil.objectFieldNameValues(Build.VERSION.class));

        // 这个就不需要了，版本代码都约定好了的
        JSONObject buildVersionCodesJson = new JSONObject(IReflectUtil.objectFieldNameValues(Build.VERSION_CODES.class));

        return buildVersionJson;
    }


}
