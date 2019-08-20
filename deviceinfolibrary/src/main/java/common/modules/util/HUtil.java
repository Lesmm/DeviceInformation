package common.modules.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("rawtypes")
public class HUtil {

	public static Map<String, Class<?>> getClassGenericReturnType(Class<?> clazz) {
		Map<String, Class<?>> map = new HashMap<String, Class<?>>();

		try {
			for (Method method : clazz.getDeclaredMethods()) {
				String methodName = method.getName();
				Class returnClass = method.getReturnType();
				if (Collection.class.isAssignableFrom(returnClass)) {
					Type returnType = method.getGenericReturnType();
					if (returnType instanceof ParameterizedType) {
						ParameterizedType paramType = (ParameterizedType) returnType;
						Type[] argTypes = paramType.getActualTypeArguments();
						if (argTypes.length > 0) {
							map.put(methodName, (Class<?>) argTypes[0]);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return map;
	}

	public static Map<String, List<String>> getClassStruct(String clazzName) {
		try {
			Class<?> clazz = String.class.getClassLoader().loadClass(clazzName);
			return getClassStruct(clazz);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Map<String, List<String>> getClassStruct(Class<?> clazz) {
		Field[] stubClazzFields = clazz.getDeclaredFields();

		Class<?> superClazz = clazz.getSuperclass();
		Class<?>[] interfaces = clazz.getInterfaces();
		Method[] clazzMethods = clazz.getDeclaredMethods();

		Map<String, List<String>> map = new HashMap<String, List<String>>();

		// super class
		if (superClazz != null) {
			List<String> superClasseNames = new ArrayList<String>();
			superClasseNames.add(superClazz.getName());
			map.put("superClass", superClasseNames);
		}

		// interfaces
		List<String> interfacesNames = new ArrayList<String>();
		for (int i = 0; i < interfaces.length; i++) {
			Class<?> iClazz = interfaces[i];
			String name = iClazz.getName();
			interfacesNames.add(name);
		}
		map.put("interfaces", interfacesNames);

		// methods
		List<String> methodsNames = new ArrayList<String>();
		for (int i = 0; i < clazzMethods.length; i++) {
			Method method = clazzMethods[i];
			String methodName = method.getName();
			methodsNames.add(methodName);
		}
		map.put("methods", methodsNames);

		// methods
		List<String> fieldsNames = new ArrayList<String>();
		for (int i = 0; i < stubClazzFields.length; i++) {
			Field field = stubClazzFields[i];
			String fieldName = field.getName();
			fieldsNames.add(fieldName);
		}
		map.put("fields", fieldsNames);
		return map;
	}

	public static boolean isContainsMethod(Class cls, String methodName) {
		Method[] methods = cls.getDeclaredMethods();
		for (int i = 0; i < methods.length; i++) {
			Method mt = methods[i];
			String name = mt.getName();
			if (name.equals(methodName)) {
				return true;
			}
		}
		return false;
	}

	public static Class getPrimitiveClass(Class parameterType) {
		if (parameterType == Boolean.class) {
			parameterType = boolean.class;
		} else if (parameterType == Integer.class) {
			parameterType = int.class;
		} else if (parameterType == Long.class) {
			parameterType = long.class;
		} else if (parameterType == Double.class) {
			parameterType = double.class;
		} else if (parameterType == Float.class) {
			parameterType = float.class;
		}
		return parameterType;
	}

}
