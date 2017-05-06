package com.android.chavah;

import android.content.res.Resources;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.widget.ListView;

import com.android.chavah.list.PinnedHeaderListView;
import com.android.chavah.list.SettingsPinnedHeaderAdapter;

import java.util.ArrayList;
import java.util.Arrays;

public class OverviewSettingsPanel {
    public static final String ANDROID_SETTINGS = "com.android.settings";
    public static final String ANDROID_PROTECTED_APPS =
            "com.android.settings.applications.ProtectedAppsActivity";
    public static final int HOME_SETTINGS_POSITION = 0;

    private Launcher mLauncher;
    private SettingsPinnedHeaderAdapter mSettingsAdapter;
    private PinnedHeaderListView mListView;

    OverviewSettingsPanel(Launcher launcher) {
        mLauncher = launcher;
    }

    // One time initialization of the SettingsPinnedHeaderAdapter
    public void initializeAdapter() {
        // Settings pane Listview
        mListView = (PinnedHeaderListView) mLauncher
                .findViewById(R.id.settings_home_screen_listview);
        mListView.setOverScrollMode(ListView.OVER_SCROLL_NEVER);
        Resources res = mLauncher.getResources();
        String[] headers = new String[]{
                res.getString(R.string.home_screen_settings)};


        mSettingsAdapter = new SettingsPinnedHeaderAdapter(mLauncher);
        mSettingsAdapter.setHeaders(headers);
        mSettingsAdapter.addPartition(false, true);
        mSettingsAdapter.addPartition(false, true);
        mSettingsAdapter.mPinnedHeaderCount = headers.length;

        mSettingsAdapter.changeCursor(HOME_SETTINGS_POSITION,
                createCursor(headers[0], getValuesHome()));
        mListView.setAdapter(mSettingsAdapter);
    }

    private Cursor createCursor(String header, String[] values) {
        MatrixCursor cursor = new MatrixCursor(new String[]{"_id", header});
        int count = values.length;
        for (int i = 0; i < count; i++) {
            cursor.addRow(new Object[]{i, values[i]});
        }
        return cursor;
    }

    private String[] getValuesHome() {
        Resources res = mLauncher.getResources();
        ArrayList<String> values = new ArrayList<String>(Arrays.asList(new String[]{
                res.getString(R.string.icon_labels),
                res.getString(R.string.scrolling_wallpaper),
                res.getString(R.string.allow_rotation_title),
                res.getString(R.string.allow_remote_folder),
                res.getString(R.string.workspace_anim)}));

        // Add additional external settings.
        RemoteFolderManager.onInitializeHomeSettings(values, mLauncher);

        String[] valuesArr = new String[values.size()];
        values.toArray(valuesArr);
        return valuesArr;
    }

    public void notifyDataSetInvalidated() {
        mSettingsAdapter.notifyDataSetInvalidated();
    }
}
