package com.android.chavah;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
    private SQLiteDatabase mDatabase;
    public void onReceive(Context context, Intent intent){
        Log.d(TAG,"onReceive: "+intent.getAction());
        mContext=context;
        LauncherProvider.DatabaseHelper databaseHelper=new LauncherProvider.DatabaseHelper(mContext);
        mDatabase=databaseHelper.getWritableDatabase();
        String packageName=intent.getDataString().split(":")[1];
        //Проверяем установлена ли уже программа, если да то не добавляем ее на главный экран
        if(packageAlreadyInHome(packageName))
            Log.d(TAG,"package already installed");
        else
            addShortcut(packageName);
    }
    private void addShortcut(String intent){
        PackageManager manager=mContext.getPackageManager();
        Intent shortcutIntent =manager.getLaunchIntentForPackage(intent);
        shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        Intent addIntent = new Intent();
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, "Untitled");
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(mContext.getApplicationContext(), R.drawable.all_apps_button_icon));
        addIntent.setAction(EXTRA_INSTALL_SHORTCUT);
        mContext.getApplicationContext().sendBroadcast(addIntent);
    }
    private boolean packageNotInstalled(String packageName){
        PackageManager pm = mContext.getPackageManager();
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return false;
        }catch (PackageManager.NameNotFoundException e){
            return true;
        }
    }
    private boolean packageAlreadyInHome(String packageName){
        Cursor cursor=mDatabase.query(LauncherProvider.TABLE_FAVORITES,new String[]{ LauncherSettings.Favorites.INTENT},null,null,null,null,null);
        try{
            cursor.moveToFirst();
            while(!cursor.isAfterLast()){
                String intentString=cursor.getString(0);
                String searchString="component=";
                int i=intentString.indexOf(searchString)+searchString.length();
                String currentPackageName="";
                while (i<intentString.length()&&intentString.charAt(i)!='/'){
                    currentPackageName+=intentString.charAt(i++);
                }
                if(packageName.equals(currentPackageName)){
                    return true;
                }
                cursor.moveToNext();
            }
        }finally {
            cursor.close();
        }
        return false;
    }
}
