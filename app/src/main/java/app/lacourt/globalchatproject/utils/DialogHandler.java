package app.lacourt.globalchatproject.utils;

import android.content.DialogInterface;
import android.os.Build;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import app.lacourt.globalchatproject.R;

public class DialogHandler {
    private static DialogHandler dialogHandler;
    private LoadingBarDialog loadingBarDialog = null;
    private AlertDialog timeOutDialog = null;
    public DialogHandler() {}

    public static DialogHandler getInstance() {
        if (dialogHandler == null) {
            dialogHandler = new DialogHandler();
        }
        return dialogHandler;
    }

    public void showTimeOutDialog(final AppCompatActivity activity) {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(activity, android.R.style.Theme_Material_Light_Dialog);
        } else {
            builder = new AlertDialog.Builder(activity);
        }
        builder.setTitle(activity.getString(R.string.dialog_time_out_title))
                .setMessage(activity.getString(R.string.dialog_time_out_message))
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

        timeOutDialog = builder.create();
        timeOutDialog.show();
    }

    public void dismissTimeOutDialog(AppCompatActivity activity){
        if(timeOutDialog != null && !activity.isDestroyed() && !activity.isFinishing() && timeOutDialog.isShowing()) {
            timeOutDialog.dismiss();
        }
    }

    public void showLoadingBarDialog(AppCompatActivity activity, String message) {
        loadingBarDialog = new LoadingBarDialog(activity, R.layout.loading_bar_dialog, message);
        loadingBarDialog.setCancelable(false);
        loadingBarDialog.setCanceledOnTouchOutside(false);
        loadingBarDialog.show();
    }

    public void dismissLoadingBarDialog(AppCompatActivity activity) {
        if (!activity.isDestroyed() && !activity.isFinishing() && loadingBarDialog != null && loadingBarDialog.isShowing()) {
            loadingBarDialog.dismiss();
        }
    }

}

