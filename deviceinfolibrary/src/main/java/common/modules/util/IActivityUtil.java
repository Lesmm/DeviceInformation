package common.modules.util;

import android.app.Application;

import java.lang.reflect.Method;

public class IActivityUtil {

    public static Application getApplication() {
        try {
            Class<?> activityThreadClazz = String.class.getClassLoader().loadClass("android.app.ActivityThread");
            Method currentActivityThreadMethod = activityThreadClazz.getDeclaredMethod("currentActivityThread", new Class[]{});
            currentActivityThreadMethod.setAccessible(true);
            Object currentActivityThread = currentActivityThreadMethod.invoke(activityThreadClazz, new Object[]{});
            Method getApplicationMethod = activityThreadClazz.getDeclaredMethod("getApplication", new Class[]{});
            getApplicationMethod.setAccessible(true);
            Application application = (Application) getApplicationMethod.invoke(currentActivityThread, new Object[]{});
            return application;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
