package common.modules.util;

import java.util.ArrayList;

public class IArrayUtil {

	public static <T extends Object> ArrayList<T> arrayToList(T[] objects) {
        ArrayList<T> result = new ArrayList<T>();
        for (T k : objects) {
        	result.add(k);
        }
        return result;
	}
	
}
