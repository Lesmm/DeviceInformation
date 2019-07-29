package common.modules.util;

import java.util.ArrayList;
import java.util.List;

public class IArrayUtil {

    public static <T extends Object> ArrayList<T> arrayToList(T[] array) {
        ArrayList<T> list = new ArrayList<T>();
        for (int i = 0; i < array.length; i++) {
            T k = array[i];
            list.add(k);
        }
        return list;
    }

    public static Object[] listToArray(List list) {
        Object[] array = new Object[list.size()];
        for (int i = 0; i < list.size(); i++) {
            Object k = list.get(i);
            array[i] = k;
        }
        return array;
    }

    public static int[] removeDuplicateValue(int[] array) {
        List<Integer> list = new ArrayList<Integer>();
        for (int i = 0; i < array.length; i++) {
            if (!list.contains(array[i])) {
                list.add(array[i]);
            }
        }

        int[] results = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            results[i] = list.get(i);
        }
        return results;

    }

}
