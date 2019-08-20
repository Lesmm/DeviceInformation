package com.google.applicationsocket.utils;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.widget.EditText;

public class AlertDialogUtils {

    // AlertDialog.Builder 的创建倒不用在主线程, 但show必须在主线程

    // 普通消息对话框
    public static AlertDialog.Builder show(Context mContext, String title, String message) {
        return show(mContext, title, message, "确定", null);
    }

    public static AlertDialog.Builder show(Context mContext, String title, String message,
                                           String positiveButtonText, DialogInterface.OnClickListener positiveListener) {
        return show(mContext, title, message, positiveButtonText, positiveListener, null, null);
    }

    public static AlertDialog.Builder show(Context mContext, String title, String message,
                                           String positiveButtonText, DialogInterface.OnClickListener positiveListener,
                                           String negativeButtonText, DialogInterface.OnClickListener negativeListener) {
        if (mContext == null) {
            return null;
        }
        final AlertDialog.Builder normalDialog = new AlertDialog.Builder(mContext);
        normalDialog.setTitle(title);
        normalDialog.setMessage(message);

        normalDialog.setPositiveButton(positiveButtonText, positiveListener);

        if (negativeButtonText != null) {
            normalDialog.setNegativeButton(negativeButtonText, negativeListener);
        }

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                normalDialog.show();
            }
        });

        return normalDialog;
    }

    // 单选对话框
    public static void showSingleChoiceDialog(Context mContext, String title, String[] items, int checkedItem, DialogInterface.OnClickListener itemsListener,
                                              String positiveButtonText, DialogInterface.OnClickListener positiveListener) {
        showSingleChoiceDialog(mContext, title, items, checkedItem, itemsListener, positiveButtonText, positiveListener, null, null);

    }

    public static void showSingleChoiceDialog(Context mContext, String title, String[] items, int checkedItem, DialogInterface.OnClickListener itemsListener,
                                              String positiveButtonText, DialogInterface.OnClickListener positiveListener,
                                              String negativeButtonText, DialogInterface.OnClickListener negativeListener) {
        if (mContext == null) {
            return ;
        }
        final AlertDialog.Builder normalDialog = new AlertDialog.Builder(mContext);
        normalDialog.setTitle(title);

        final DialogInterface.OnClickListener fItemsListener = itemsListener;
        final DialogInterface.OnClickListener fPositiveListener = positiveListener;

        final int[] youSelected = new int[1];
        normalDialog.setSingleChoiceItems(items, checkedItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                youSelected[0] = which;
                if (fItemsListener != null) {
                    fItemsListener.onClick(dialog, which);
                }
            }
        });

        normalDialog.setPositiveButton(positiveButtonText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (fPositiveListener != null) {
                    fPositiveListener.onClick(dialog, youSelected[0]);
                }
            }
        });

        if (negativeButtonText != null) {
            normalDialog.setNegativeButton(negativeButtonText, negativeListener);
        }

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                normalDialog.show();
            }
        });

    }

    // 操作对话框
    public static void showActionsDialog(Context mContext, String title, String[] actionItems, DialogInterface.OnClickListener itemsListener) {
        if (mContext == null) {
            return ;
        }
        final AlertDialog.Builder listDialog = new AlertDialog.Builder(mContext);
        if (title != null) {
            listDialog.setTitle(title);
        }
        listDialog.setItems(actionItems, itemsListener);

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                listDialog.show();
            }
        });
    }


    // 输入框对话框
    public static void showEditTextDialog(Context mContext, String title, String hints, String positiveButtonText, DialogInterface.OnClickListener positiveListener) {
        if (mContext == null) {
            return ;
        }
        EditText editText = new EditText(mContext);
        if (hints != null) {
            editText.setHint(hints);
        }
        final AlertDialog.Builder inputDialog = new AlertDialog.Builder(mContext);
        inputDialog.setTitle(title).setView(editText);
        inputDialog.setPositiveButton(positiveButtonText, positiveListener);

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                inputDialog.show();
            }
        });
    }

    // ProgressDialog 的 create与show 都必须在主线程

    // 进度条对话框
    public static ProgressDialog showProgressDialog(Context mContext, String title) {
        return showProgressDialog(mContext, title, ProgressDialog.STYLE_SPINNER);
    }

    public static ProgressDialog showProgressDialog(Context mContext, String title, int progressStyle) {
        if (mContext == null) {
            return null;
        }
        int MAX_PROGRESS = 100;
        final ProgressDialog progressDialog = new ProgressDialogEx(mContext);// 必须在主线程 或者 调用了Looper.prepare()的线程: Can't create handler inside thread that has not called Looper.prepare()
        progressDialog.setProgress(0);      // 这个方法调用倒不用必须在主线程
        progressDialog.setTitle(title);     // 这个也是不必在主线程，也可以
        // progressDialog.setMessage("xxx"); // 必须在主线程
        progressDialog.setProgressStyle(progressStyle);
        progressDialog.setMax(MAX_PROGRESS);

        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);

        progressDialog.show();      // 必须在主线程

        return progressDialog;
    }

    public static class ProgressDialogEx extends ProgressDialog {
        public ProgressDialogEx(Context context) {
            super(context);
        }

        @Override
        public void setMessage(CharSequence message) {
            final CharSequence text = message;
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    superSetMessage(text);
                }
            });
        }

        private void superSetMessage(CharSequence message) {
            super.setMessage(message);
        }
    }

}
