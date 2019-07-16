package common.modules.util;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class IArrayUtil {

	public static <T extends Object> ArrayList<T> arrayToList(T[] array) {
        ArrayList<T> list = new ArrayList<T>();
        for(int i = 0; i < array.length; i++) {
            T k = array[i];
            list.add(k);
        }
        return list;
	}

	public static Object[] listToArray(ArrayList list) {
        Object[] array = new Object[list.size()];
        for (int i = 0; i < list.size(); i++) {
            Object k = list.get(i);
            array[i] = k;
        }
        return array;
    }
	
}
