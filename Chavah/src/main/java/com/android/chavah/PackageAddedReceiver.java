package com.android.chavah;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

/**
 * Created by hapsi on 02.05.2017.
 *
 * This class receive PACKAGE_ADDED action and just send broadcast intent to self
 * I know it's bad style, but this working!
 */
public class PackageAddedReceiver extends BroadcastReceiver {
    private static final String TAG="PackageAddedReceiver";
    private static final String EXTRA_INSTALL_SHORTCUT="com.android.launcher.action.INSTALL_SHORTCUT_HIDDEN";

    private Context mContext;
    public void onReceive(Context context, Intent intent){
        mContext=context;
        Log.d(TAG,"onReceive: "+intent.getDataString().split(":")[1]);
        addShortcut(intent.getDataString().split(":")[1]);
    }
    private void addShortcut(String intent){
        PackageManager manager=mContext.getPackageManager();
        Intent shortcutIntent =manager.getLaunchIntentForPackage(intent);
        shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        Intent addIntent = new Intent();
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, "Test");
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(mContext.getApplicationContext(), R.drawable.all_apps_button_icon));
        addIntent.setAction(EXTRA_INSTALL_SHORTCUT);
        mContext.getApplicationContext().sendBroadcast(addIntent);
    }
}
