package common.modules.util;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Map;

public class IBroadcastReciverWaitor {

    private static Context mContext = null;

    public static void setContext(Context context) {
        mContext = context;
    }

    public static Context getContext() {
        if (mContext == null) {
            try {
                Class<?> ActivityThread = Class.forName("android.app.ActivityThread");
                Method currentApplication = ActivityThread.getDeclaredMethod("currentApplication", new Class[]{});
                currentApplication.setAccessible(true);
                Application application = (Application) currentApplication.invoke(ActivityThread, new Object[]{});
                mContext = application;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return mContext;
    }

    /**
     * AsyncWaitor
     */

    public static abstract class AsyncWaitor {

        public boolean onReceive(Context context, Intent intent) {
            return false;
        }

    }

    public static Intent sendWithWait(String sendAction, Map<String, Serializable> sendParameters,
                                      long waitTimeout, String waitAction, AsyncWaitor asyncWaitor) {
        send(sendAction, sendParameters);
        return waitFor(waitTimeout, waitAction, asyncWaitor);
    }

    public static void send(String action, Map<String, Serializable> parameters) {
        Intent intent = new Intent(action);
        if (parameters != null) {
            for (String key : parameters.keySet()) {
                Serializable value = parameters.get(key);
                intent.putExtra(key, value);
            }
        }
        getContext().sendBroadcast(intent);
    }

    public static Intent waitFor(long timeoutMillis, String action, final AsyncWaitor asyncWaitor) {
        final Object waitObject = asyncWaitor == null ? new Object() : null;
        final Intent[] intentResult = new Intent[]{null};
        final BroadcastReceiver[] broadcastReceivers = new BroadcastReceiver[]{null};

        // handle timeout
        if (timeoutMillis > 0) {
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (broadcastReceivers[0] != null) {
                        try {
                            getContext().unregisterReceiver(broadcastReceivers[0]);
                            broadcastReceivers[0] = null;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    if (waitObject != null) {
                        synchronized (waitObject) {
                            waitObject.notify();
                        }
                    }

                }
            }, timeoutMillis);
        }

        // register
        broadcastReceivers[0] = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // important!!! onReceive call back is on Main Thread
                boolean isContinueListening = false;
                try {
                    Log.d("DLog", "Get are you ok response ~~~~");
                    intentResult[0] = intent;

                    if (asyncWaitor != null) {
                        isContinueListening = asyncWaitor.onReceive(context, intent);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (isContinueListening) {
                        return;
                    }

                    if (broadcastReceivers[0] != null) {
                        try {
                            getContext().unregisterReceiver(broadcastReceivers[0]);
                            broadcastReceivers[0] = null;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    if (waitObject != null) {
                        synchronized (waitObject) {
                            waitObject.notify();
                        }
                    }
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(action);
        intentFilter.setPriority(Integer.MAX_VALUE);
        getContext().registerReceiver(broadcastReceivers[0], intentFilter);

        // go on and unregister
        if (waitObject != null) {
            synchronized (waitObject) {
                try {
                    waitObject.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        return intentResult[0];
    }


    /**
     * AsyncListener
     */

    public static interface AsyncListener {
        public boolean onReceive(Context context, Intent intent);
    }

    public static void listen(String action, final AsyncListener asyncListener) {
        final BroadcastReceiver[] broadcastReceivers = new BroadcastReceiver[]{null};

        // register
        broadcastReceivers[0] = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // important!!! onReceive call back is on Main Thread
                boolean isTerminate = false;
                try {
                    if (asyncListener != null) {
                        isTerminate = asyncListener.onReceive(context, intent);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (isTerminate && broadcastReceivers[0] != null) {
                        try {
                            getContext().unregisterReceiver(broadcastReceivers[0]);
                            broadcastReceivers[0] = null;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(action);
        intentFilter.setPriority(Integer.MAX_VALUE);
        getContext().registerReceiver(broadcastReceivers[0], intentFilter);
    }

}
