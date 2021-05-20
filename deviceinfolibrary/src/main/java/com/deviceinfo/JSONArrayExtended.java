package com.deviceinfo;

import org.json.JSONArray;
import org.json.JSONException;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class JSONArrayExtended extends JSONArray {

    public JSONArrayExtended(Collection copyFrom) {
        super();
        if (copyFrom == null) {
            return;
        }
        if (copyFrom instanceof List) {
            // for keep the order
            List list = (List) copyFrom;
            for (int i = 0; i < list.size(); i++) {
                put(new JSONObjectExtended().__wrap__(list.get(i)));
            }
        } else {
            for (Iterator it = copyFrom.iterator(); it.hasNext(); ) {
                put(new JSONObjectExtended().__wrap__(it.next()));
            }
        }
    }

    public JSONArrayExtended(Object array) throws JSONException {
        if (array == null || !array.getClass().isArray()) {
            throw new JSONException("Not a primitive array: " + array.getClass());
        }
        final int length = Array.getLength(array);

        try {
            Field field = this.getClass().getSuperclass().getDeclaredField("values");
            field.setAccessible(true);

            ArrayList<Object> values = new ArrayList<Object>(length);

            field.set(this, values);
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (int i = 0; i < length; ++i) {
            put(new JSONObjectExtended().__wrap__(Array.get(array, i)));
        }
    }
}
