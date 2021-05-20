package com.deviceinfo.higher;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

import com.deviceinfo.JSONObjectExtended;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import common.modules.util.android.IHTTPUtil;

public class HiLocationManager extends HiBase {

    @Override
    public JSONObject getInfo(Context mContext) {
        try {
            Map<String, Object> map = __getInfo__(mContext);
            return new JSONObjectExtended(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new JSONObject();
    }

    public Map<String, Object> __getInfo__(Context mContext) {
        return null;
    }

    public JSONObject getInExtrasInfo(Context mContext) {

        final Map<String, Object> map = new HashMap<>();

        try {
            LocationManager locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if (location != null) {
                String city = HiLocationManager.getAddressInformationUsingQQApi(location.getLatitude(), location.getLongitude());
                map.put("Location.city", city);
                map.put("Location.longitude", location.getLongitude());
                map.put("Location.latitude", location.getLatitude());
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }

        return new JSONObjectExtended(map);
    }

    @Override
    protected JSONObject keysMappings() {
        JSONObject mapping = new JSONObject();

        return mapping;
    }

    /**
     * Utils
     *
     * @param latitude
     * @param longitude
     * @return
     */
    public static String getAddressInformationUsingQQApi(double latitude, double longitude) {
        // String api = "https://apis.map.qq.com/jsapi?qt=rgeoc&lnglat=116.405909%2C39.983592&key=FBOBZ-VODWU-C7SVF-B2BDI-UK3JE-YBFUS&output=jsonp&pf=jsapi&ref=jsapi";
        // String api = "https://apis.map.qq.com/jsapi?qt=rgeoc&lnglat=116.405909%2C39.983592&key=FBOBZ-VODWU-C7SVF-B2BDI-UK3JE-YBFUS&output=jsonp&pf=jsapi&ref=jsapi";
        // FBOBZ-VODWU-C7SVF-B2BDI-UK3JE-YBFUS 别人的
        // VHIBZ-6GBWW-TSORV-OVH4B-SBHGE-IWFPR
        String api = "https://apis.map.qq.com/ws/geocoder/v1/?location=" + latitude + "," + longitude + "&key=VHIBZ-6GBWW-TSORV-OVH4B-SBHGE-IWFPR";
        IHTTPUtil.Results results = IHTTPUtil.get(api);
        JSONObject result = results.getJson() != null ? results.getJson().optJSONObject("result") : null;
        if (result != null) {
            return result.optString("address");
        }
        return null;
    }

}
