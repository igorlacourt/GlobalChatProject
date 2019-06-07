package app.lacourt.globalchatproject.utils;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyboardShortcutGroup;
import android.view.Menu;
import android.view.Window;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import app.lacourt.globalchatproject.R;

public class LoadingBarDialog extends Dialog {
    private int layoutId;
    private TextView messageTextView;
    private String message = "";

    public LoadingBarDialog(@NonNull Context context, int layoutId, String message) {
        super(context);
        this.layoutId = layoutId;
        this.message = message;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(layoutId);

        messageTextView = (TextView)findViewById(R.id.tv_dialog_loading_bar);
        messageTextView.setText(message);

    }
    
    @Override
    public void onProvideKeyboardShortcuts(List<KeyboardShortcutGroup> data, @Nullable Menu menu, int deviceId) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    public void setDialogMessage(String message) {
        this.message = message;
        this.messageTextView.setText(message);
    }

    public String getMessage() {
        return message;
    }
//    public void setTextMessage(String message) {
//        this.message.setText(message);
//    }
}