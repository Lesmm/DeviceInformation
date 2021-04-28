package com.deviceinfo.info;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import common.modules.util.IJSONObjectUtil;
import common.modules.util.IProcessUtil;

public class StatInfo {

    public static JSONObject getPackageSizeStatInfo(Context mContext, JSONObject PackageJson) {
        JSONObject getPackageInfo = PackageJson.optJSONObject("getPackageInfo");
        if (getPackageInfo == null) {
            getPackageInfo = new JSONObject();
            IJSONObjectUtil.putJSONObject(PackageJson, "getPackageInfo", getPackageInfo);
        }

        // ------------ 获取所有安装包大小 ------------
        // JSONObject getPackageInfo = PackageJson.optJSONObject("getPackageInfo");
        JSONArray packageNames = getPackageInfo.names();
        for (int i = 0; packageNames != null && i < packageNames.length(); i++) {
            String packageName = packageNames.optString(i);
            JSONObject elementPackageJson = getPackageInfo.optJSONObject(packageName);
            if (elementPackageJson == null) {
                continue;
            }
            JSONObject applicationInfo = elementPackageJson.optJSONObject("applicationInfo");
            if (applicationInfo == null) {
                continue;
            }
            JSONObject json = StatInfo.getApkFileInfoThroughStat(applicationInfo);
            if (json == null) {
                continue;
            }

            StatInfo.putValueInPackageElementExtraJson(getPackageInfo, packageName, json);
        }

        if (getPackageInfo.length() == 0) {
            JSONArray getInstalledPackages = PackageJson.optJSONArray("getInstalledPackages");
            for (int i = 0; getInstalledPackages != null && i < getInstalledPackages.length(); i++) {
                JSONObject elementPackageJson = getInstalledPackages.optJSONObject(i);
                if (elementPackageJson == null) {
                    continue;
                }
                String packageName = elementPackageJson.optString("packageName");
                JSONObject applicationInfo = elementPackageJson.optJSONObject("applicationInfo");
                if (applicationInfo == null) {
                    continue;
                }
                JSONObject json = StatInfo.getApkFileInfoThroughStat(applicationInfo);
                if (json == null) {
                    continue;
                }


                // NOTE: put in getPackageInfo
                StatInfo.putValueInPackageElementExtraJson(getPackageInfo, packageName, json);
            }
        }

        if (getPackageInfo.length() == 0) {
            JSONArray getInstalledApplications = PackageJson.optJSONArray("getInstalledApplications");
            for (int i = 0; getInstalledApplications != null && i < getInstalledApplications.length(); i++) {
                JSONObject applicationInfo = getInstalledApplications.optJSONObject(i);
                String packageName = applicationInfo.optString("packageName");
                JSONObject json = StatInfo.getApkFileInfoThroughStat(applicationInfo);
                if (json == null) {
                    continue;
                }

                // NOTE: put in getPackageInfo
                StatInfo.putValueInPackageElementExtraJson(getPackageInfo, packageName, json);
            }
        }
        // ------------ 获取所有安装包大小 ------------

        return getPackageInfo;
    }

    public static void putValueInPackageElementExtraJson(JSONObject getPackageInfo, String packageName, JSONObject json) {
        JSONObject elementPackageJson = getPackageInfo.optJSONObject(packageName);
        if (elementPackageJson == null) {
            elementPackageJson = new JSONObject();
            IJSONObjectUtil.putJSONObject(getPackageInfo, packageName, elementPackageJson);
        }

        JSONObject elementExtrasJson = elementPackageJson.optJSONObject("kilosExtras");
        if (elementExtrasJson == null) {
            elementExtrasJson = new JSONObject();
            IJSONObjectUtil.putJSONObject(elementPackageJson, "kilosExtras", elementExtrasJson);
        }

        IJSONObjectUtil.putAll(elementExtrasJson, json);
    }

    private static JSONObject getApkFileInfoThroughStat(JSONObject applicationInfo) {
        String sourceDir = applicationInfo.optString("sourceDir", applicationInfo.optString("publicSourceDir"));
        if (sourceDir.isEmpty()) {
            return null;
        }

        JSONObject result = new JSONObject();

        // StatFs stat = new StatFs(sourceDir);
        // long blockSize = stat.getBlockSizeLong();
        // long blockCount = stat.getBlockCountLong();

        try {
            File file = new File(sourceDir);
            if (file.isFile()) {
                long size = file.length();
                long lastModified = file.lastModified();
                if (size != 0) {
                    IJSONObjectUtil.putJSONObject(result, "size", size);
                }
                if (lastModified != 0) {
                    IJSONObjectUtil.putJSONObject(result, "lastModified", lastModified);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        try {
            // TODO ... handle the Access/Modify/Change
            /**
             File: /data/app/com.huawei.intelligent--6jeD3gYYP8ohCY9isq-vA==/base.apk
             Size: 49952375	 Blocks: 97672	 IO Blocks: 512	regular file
             Device: 10335h/66357d	 Inode: 199467	 Links: 1
             Access: (0644/-rw-r--r--)	Uid: ( 1000/  system)	Gid: ( 1000/  system)
             Access: 2021-02-26 20:53:36.750000000 +0800
             Modify: 2021-02-26 20:53:37.600000000 +0800
             Change: 2021-02-26 20:53:38.590000000 +0800
             */
            String content = IProcessUtil.execCommands("stat " + sourceDir);
            if (content != null) {
                Matcher matcher = Pattern.compile("Size:\\s+(\\d+)\\s+").matcher(content);
                if (matcher.find()) {
                    int groupCount = matcher.groupCount();
                    if (groupCount > 0) {
                        String sizeStr = matcher.group(1);
                        long size = Long.parseLong(sizeStr);
                        IJSONObjectUtil.putJSONObject(result, "size", size);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

}
