package common.modules.util;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class IJSONObjectUtil {

	/*
	 * Format JSON String
	 */
	public static String formatJsonString(String string) {
		int level = 0;
		StringBuffer jsonFormatString = new StringBuffer();
		for (int index = 0; index < string.length(); index++) {
			char c = string.charAt(index);
			if (level > 0 && '\n' == jsonFormatString.charAt(jsonFormatString.length() - 1)) {
				jsonFormatString.append(getJsonElementLevelString(level));
			}
			// 遇到"{"和"["要增加空格和换行，遇到"}"和"]"要减少空格，以对应，遇到","要换行
			switch (c) {
			case '{':
			case '[':
				jsonFormatString.append(c + "\n");
				level++;
				break;
			case ',':
				jsonFormatString.append(c + "\n");
				break;
			case '}':
			case ']':
				jsonFormatString.append("\n");
				level--;
				jsonFormatString.append(getJsonElementLevelString(level));
				jsonFormatString.append(c);
				break;
			default:
				jsonFormatString.append(c);
				break;
			}
		}
		return jsonFormatString.toString();
	}

	private static String getJsonElementLevelString(int level) {
		StringBuffer levelString = new StringBuffer();
		for (int levelI = 0; levelI < level; levelI++) {
			levelString.append("\t");
		}
		return levelString.toString();
	}

	// put replaceJson element to source with the same structure, like merge but with depth
	public static void replaceJsonElementsValues(JSONObject sourceJson, JSONObject replaceJson) {
		if (sourceJson == null || replaceJson == null) {
			return;
		}

		Iterator<?> keys = replaceJson.keys();

		while (keys.hasNext()) {
			try {
				String replaceKey = (String) keys.next();
				Object elementReplace = replaceJson.opt(replaceKey);
				Object elementSource = sourceJson.opt(replaceKey);

				if ((elementSource instanceof JSONObject) && (elementReplace instanceof JSONObject)) {
					IJSONObjectUtil.replaceJsonElementsValues((JSONObject) elementSource, (JSONObject) elementReplace);
				} else if ((elementSource instanceof JSONArray) && (elementReplace instanceof JSONArray)) {
					IJSONArrayUtil.replaceJsonElementsValues((JSONArray) elementSource, (JSONArray) elementReplace);
				} else {
					if (replaceKey.startsWith("delete-")) {
						String deleteKey = replaceKey.replaceFirst("delete-", "");
						if (sourceJson.has(deleteKey)) {
							sourceJson.remove(deleteKey);
						}
						if (replaceJson.has(deleteKey)) {
							sourceJson.put(deleteKey, replaceJson.opt(deleteKey));
						}
					} else {
						// replace or add
						sourceJson.put(replaceKey, elementReplace);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}
	
	/*
	 * convenient methods
	 */
    public static void mergeJSONObject(JSONObject destination, JSONObject source) {
    	if (destination == null || source == null) {
			return;
		}
        Iterator<?> iteratorSource = source.keys();
        while (iteratorSource.hasNext()) {
            try {
                String name = (String) iteratorSource.next();
                Object value = source.opt(name);
                destination.put(name, value);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
	public static JSONObject getJSONWithPrefix(JSONObject source, String prefix) {
		JSONObject result = new JSONObject();
		try {
			Iterator<?> iterator = source.keys();
			while (iterator.hasNext()) {
				String key = (String) iterator.next();
				if (key.startsWith(prefix)) {
					Object value = source.opt(key);
					result.put(key, value);
				}
			}
		} catch (Exception e) {
			Log.d("HLog", e.toString());
		}
		return result;
	}
	
	public static JSONArray getValues(JSONObject json) {
		JSONArray array = new JSONArray();
		if (json != null) {
			Iterator<?> iterator = json.keys();
			while (iterator.hasNext()) {
				String key = (String) iterator.next();
				Object value = json.opt(key);
				array.put(value);
			}
		}
		return array;
	}

	/*
	 * Json to object 
	 */
	@SuppressWarnings("unchecked")
	public static <T> void setJsonValuesToObjectFields(Object object, JSONObject json) {
		if (object == null || json == null) {
			return;
		}

		Boolean isClass = object instanceof Class;
		Class<?> clazz = isClass ? (Class<?>) object : object.getClass();
		Field[] fields = clazz.getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			Field field = fields[i];
			field.setAccessible(true);

			String name = field.getName();
			Object value = json.opt(name);
			if (value != null) {
				try {
					Object instance = field.get(object);

					if (value instanceof JSONObject) {
						setJsonValuesToObjectFields(instance, (JSONObject) value);

					} else if (value instanceof JSONArray) {

						if (field.getType().isArray()) {
							setJsonValuesToArrayFields((T[]) instance, (JSONArray) value);
						} else if (List.class.isAssignableFrom(field.getType())) {
							setJsonValuesToListFields((List<?>) instance, (JSONArray) value);
						}

					} else {

						// if is Enum, transfer String to Enum
						if (field.getType().isEnum()) {
							Class<?> fieldEnumClazz = instance.getClass();
							Method valueOfMethod = fieldEnumClazz.getDeclaredMethod("valueOf", String.class);
							value = valueOfMethod.invoke(fieldEnumClazz, value);
						}

						// set the value
						field.set(object, value);

					}
				} catch (Exception e) {
					Log.d("HLog", object.getClass().toString() + " Set field failed: " + e.toString() + ". " + name + " -> " + value);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> void setJsonValuesToArrayFields(T[] objects, JSONArray jsonArray) {
		if (objects == null || jsonArray == null) {
			return;
		}

		for (int i = 0; i < objects.length; i++) {
			if (jsonArray.length() > i) {

				T obj = objects[i];
				Object val = jsonArray.opt(i);

				if (obj.getClass().isPrimitive()) { // int, boolean, double, long, float ...
					objects[i] = (T) val; // auto boxing
				} else if (obj instanceof Object && val instanceof JSONObject) {
					setJsonValuesToObjectFields(obj, (JSONObject) val);
				}

			}
		}
	}

	public static void setJsonValuesToListFields(List<?> objects, JSONArray jsonArray) {
		if (objects == null || jsonArray == null) {
			return;
		}

		for (int i = 0; i < objects.size(); i++) {
			if (jsonArray.length() > i) {

				Object obj = objects.get(i);
				Object val = jsonArray.opt(i);

				if (obj instanceof Object && val instanceof JSONObject) {
					setJsonValuesToObjectFields(obj, (JSONObject) val);
				}

			}
		}
	}

	public static HashMap<String, Object> jsonToMap(JSONObject json) {
		HashMap<String, Object> result = new HashMap<String, Object>();
		Iterator<?> iterator = json != null ? json.keys() : null;
		while (json != null && iterator != null && iterator.hasNext()) {
			String key = (String) iterator.next();
			Object value = json.opt(key);

			if (value instanceof JSONObject) {
				value = jsonToMap((JSONObject) value);
			} else if (value instanceof JSONArray) {
				value = IJSONArrayUtil.jsonToList((JSONArray) value);
			}

			result.put(key, value);
		}
		return result;
	}

	public static void putJSONObject(JSONObject json, String key, Object value) {
		if (json == null) {
			return;
		}
		try {
			json.put(key, value);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void put(JSONObject json, Object... keysValues) {
		if (json == null) {
			return;
		}
		for (int i = 0; keysValues != null && i < keysValues.length; i = i + 2) {
			String key = (String) keysValues[i];
			Object value = (i + 1) < keysValues.length ? keysValues[i + 1] : null;
			try {
				json.put(key, value);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void putAll(JSONObject TO, JSONObject FROM) {
		java.util.Iterator<String> iterator = FROM.keys();
		while (iterator.hasNext()) {
			String key = iterator.next();
			Object value = FROM.opt(key);

			try {
				TO.put(key, value);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	/*
	 * Iterate Json
	 */
	public interface IterateHandler {
		void iterateAction(String key, Object value);
	}

	public static void iterateJSONObject(JSONObject jsonObject, IterateHandler handler) {
		try {
			java.util.Iterator iterator = jsonObject.keys();
			while (iterator.hasNext()) {
				String key = (String) iterator.next();
				Object value = jsonObject.opt(key);
				handler.iterateAction(key, value);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
