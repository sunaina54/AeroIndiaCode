package com.aero.view.Receiver;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.aero.custom.utility.AppConstant;
import com.aero.view.activity.FeedbackActivity;

import java.security.Provider;

/**
 * Created by SUNAINA on 19-12-2018.
 */

public class FeedbackReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        Intent serviceintent = new Intent(context, UpdateFeedbackService.class);
        context.startService(serviceintent);

    }
}
