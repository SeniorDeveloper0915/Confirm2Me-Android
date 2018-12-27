package com.erik.confirm2me.helper;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.erik.confirm2me.AppData;
import com.erik.confirm2me.activity.MainActivity;
import com.erik.confirm2me.fragment.PendingFragment;
import com.parse.ParsePushBroadcastReceiver;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by happiness on 11/3/2015.
 */
public class Confirm2MeReceiver extends ParsePushBroadcastReceiver {
    private static final String TAG = "Confirm2MeReceiver";
    private Context mContext;

    @Override
    public void onPushReceive(Context context, Intent intent) {
        super.onPushReceive(context, intent);
        if (intent == null)
            return;

        try {
            JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));
            Log.e(TAG, "Push received: " + json);
            if (json != null) {
                if (json.has("alert")) {
                    String pushMessage = json.getString("alert");
                    // show notification for push message
                    Intent resultIntent = new Intent(context, MainActivity.class);
                    AppData.getInstance().showNotificationMessage("Confirm2Me", pushMessage, resultIntent);

                }
            }

        } catch (JSONException e) {
            Log.e(TAG, "Push message json exception: " + e.getMessage());
        }
    }

    @Override
    protected void onPushDismiss(Context context, Intent intent) {
        super.onPushDismiss(context, intent);
    }

    @Override
    protected void onPushOpen(Context context, Intent intent) {
        super.onPushOpen(context, intent);
    }


}