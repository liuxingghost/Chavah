package com.android.chavah.list;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.chavah.InvariantDeviceProfile;
import com.android.chavah.Launcher;
import com.android.chavah.LauncherSettings;
import com.android.chavah.OverviewSettingsPanel;
import com.android.chavah.R;
import com.android.chavah.Utilities;
import com.android.chavah.effects.BaseEffectAnimation;
import com.android.chavah.settings.SettingsProvider;

public class SettingsPinnedHeaderAdapter extends PinnedHeaderListAdapter {

    public static final String ACTION_SEARCH_BAR_VISIBILITY_CHANGED =
            "cyanogenmod.intent.action.SEARCH_BAR_VISIBILITY_CHANGED";

    private Launcher mLauncher;
    private Context mContext;

    class SettingsPosition {
        int partition = 0;
        int position = 0;

        SettingsPosition (int partition, int position) {
            this.partition = partition;
            this.position = position;
        }
    }

    public SettingsPinnedHeaderAdapter(Context context) {
        super(context);
        mLauncher = (Launcher) context;
        mContext = context;
    }

    private String[] mHeaders;
    public int mPinnedHeaderCount;

    public void setHeaders(String[] headers) {
        this.mHeaders = headers;
    }

    @Override
    protected View newHeaderView(Context context, int partition, Cursor cursor,
                                 ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(R.layout.settings_pane_list_header, null);
    }

    @Override
    protected void bindHeaderView(View view, int partition, Cursor cursor) {
        TextView textView = (TextView) view.findViewById(R.id.item_name);
        textView.setText(mHeaders[partition]);
    }

    @Override
    protected View newView(Context context, int partition, Cursor cursor, int position,
                           ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(R.layout.settings_pane_list_item, null);
    }

    @Override
    protected void bindView(View v, int partition, Cursor cursor, int position) {
        TextView nameView = (TextView)v.findViewById(R.id.item_name);
        TextView stateView = (TextView)v.findViewById(R.id.item_state);
        Switch settingSwitch = (Switch)v.findViewById(R.id.setting_switch);
        settingSwitch.setClickable(false);

        // RTL
        Configuration config = mLauncher.getResources().getConfiguration();
        if (config.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
            nameView.setGravity(Gravity.RIGHT);
        }

        String title = cursor.getString(1);
        nameView.setText(title);

        v.setTag(new SettingsPosition(partition, position));

        Resources res = mLauncher.getResources();

        boolean current;
        String state;

        switch (partition) {
            case OverviewSettingsPanel.HOME_SETTINGS_POSITION:
                switch (position) {
                    case 0:
                        current = SettingsProvider.getBoolean(mContext,
                                SettingsProvider.SETTINGS_UI_HOMESCREEN_HIDE_ICON_LABELS,
                                R.bool.preferences_interface_homescreen_hide_icon_labels_default);
                        // Reversed logic here. Boolean is hideLabels, where setting is show labels
                        setSettingSwitch(stateView, settingSwitch, !current);
                        break;
                    case 1:
                        current = SettingsProvider.getBoolean(mContext,
                                SettingsProvider.SETTINGS_UI_HOMESCREEN_SCROLLING_WALLPAPER_SCROLL,
                                R.bool.preferences_interface_homescreen_scrolling_wallpaper_scroll_default);
                        setSettingSwitch(stateView, settingSwitch, current);
                        break;
                    case 2:
                        current = SettingsProvider.getBoolean(mContext,
                                SettingsProvider.SETTINGS_UI_ALLOW_ROTATION,
                                R.bool.preferences_interface_allow_rotation);
                        setSettingSwitch(stateView, settingSwitch, current);
                        break;
                    case 3:
                        current = SettingsProvider.getBoolean(mContext,
                                SettingsProvider.SETTINGS_UI_HOMESCREEN_REMOTE_FOLDER,
                                R.bool.preferences_interface_homescreen_remote_folder_default);
                        setSettingSwitch(stateView, settingSwitch, current);
                        break;
                    case 4:
                        updateWorkspaceAnimSettingsItem(stateView, settingSwitch);
                        break;
                    default:
                        hideStates(stateView, settingSwitch);
                }
                break;
        }

        v.setOnClickListener(mSettingsItemListener);
    }

    @Override
    public View getPinnedHeaderView(int viewIndex, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.settings_pane_list_header, parent, false);
        view.setFocusable(false);
        view.setEnabled(false);
        bindHeaderView(view, viewIndex, null);
        return view;
    }

    @Override
    public int getPinnedHeaderCount() {
        return mPinnedHeaderCount;
    }

    public void updateDynamicGridSizeSettingsItem(TextView stateView, Switch settingSwitch) {
        InvariantDeviceProfile.GridSize gridSize = InvariantDeviceProfile.GridSize.getModeForValue(
                SettingsProvider.getIntCustomDefault(mLauncher,
                        SettingsProvider.SETTINGS_UI_DYNAMIC_GRID_SIZE, 0));
        String state = "";

        switch (gridSize) {
            case Comfortable:
                state = mLauncher.getResources().getString(R.string.grid_size_comfortable);
                break;
            case Cozy:
                state = mLauncher.getResources().getString(R.string.grid_size_cozy);
                break;
            case Condensed:
                state = mLauncher.getResources().getString(R.string.grid_size_condensed);
                break;
            case Custom:
                int rows = SettingsProvider.getIntCustomDefault(mLauncher,
                        SettingsProvider.SETTINGS_UI_HOMESCREEN_ROWS, 0);
                int columns = SettingsProvider.getIntCustomDefault(mLauncher,
                        SettingsProvider.SETTINGS_UI_HOMESCREEN_COLUMNS, 0);
                state = rows + " " + "\u00d7" + " " + columns;
                break;
        }
        setStateText(stateView, settingSwitch, state);
    }

    public void updateWorkspaceAnimSettingsItem(TextView stateView, Switch settingSwitch) {

        BaseEffectAnimation.Effect effect = BaseEffectAnimation.Effect.getEffectForType(
                SettingsProvider.getIntCustomDefault(mLauncher,
                        SettingsProvider.SETTINGS_UI_WORKSPACE_EFFECT, 0));
        String state = mLauncher.getResources().getString(BaseEffectAnimation.Effect.getEffectStringResId(effect.getEffectType()));

        setStateText(stateView, settingSwitch, state);
    }

    OnClickListener mSettingsItemListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            int partition = ((SettingsPosition) v.getTag()).partition;
            int position = ((SettingsPosition) v.getTag()).position;

            switch (partition) {
                case OverviewSettingsPanel.HOME_SETTINGS_POSITION:
                    switch (position) {
                        case 0:
                            updateSearchBarVisibility(v);
                            break;
                        case 1:
                            onSettingsBooleanChanged(v,
                                    SettingsProvider.SETTINGS_UI_HOMESCREEN_HIDE_ICON_LABELS,
                                    R.bool.preferences_interface_homescreen_hide_icon_labels_default,
                                    true);
                            mLauncher.reloadLauncher(false, false);
                            break;
                        case 2:
                            onSettingsBooleanChanged(v,
                                    SettingsProvider
                                            .SETTINGS_UI_HOMESCREEN_SCROLLING_WALLPAPER_SCROLL,
                                    R.bool.preferences_interface_homescreen_scrolling_wallpaper_scroll_default,
                                    false);
                            mLauncher.reloadLauncher(false, false);
                            break;
                        case 3:
                            String key = SettingsProvider.SETTINGS_UI_ALLOW_ROTATION;
                            boolean newValue = onSettingsBooleanChanged(v, key,
                                    R.bool.preferences_interface_allow_rotation, false);

                            Bundle extras = new Bundle();
                            extras.putBoolean(LauncherSettings.Settings.EXTRA_VALUE, newValue);

                            // Required for system to pickup rotation change.
                            mContext.getContentResolver().call(
                                    LauncherSettings.Settings.CONTENT_URI,
                                    LauncherSettings.Settings.METHOD_SET_BOOLEAN,
                                    key, extras);

                            break;
                        case 4:
                            onSettingsBooleanChanged(v,
                                    SettingsProvider.SETTINGS_UI_HOMESCREEN_REMOTE_FOLDER,
                                    R.bool.preferences_interface_homescreen_remote_folder_default,
                                    false);
                            mLauncher.getRemoteFolderManager().onSettingChanged();
                            break;
                        case 5:
                            mLauncher.onClickEffectSettingButton();
                            break;
                    }
                    break;
            }

            View defaultHome = mLauncher.findViewById(R.id.default_home_screen_panel);
            defaultHome.setVisibility(getCursor(0).getCount() > 1 ? View.VISIBLE : View.GONE);
        }
    };

    private void updateSearchBarVisibility(View v) {
        boolean isSearchEnabled = SettingsProvider.getBoolean(mContext,
                SettingsProvider.SETTINGS_UI_HOMESCREEN_SEARCH,
                R.bool.preferences_interface_homescreen_search_default);

        if (!isSearchEnabled) {
            if (!Utilities.searchActivityExists(mContext)) {
                Toast.makeText(mContext, mContext.getString(R.string.search_activity_not_found),
                        Toast.LENGTH_SHORT).show();
                return;
            }
        }

        onSettingsBooleanChanged(v,
                SettingsProvider.SETTINGS_UI_HOMESCREEN_SEARCH,
                R.bool.preferences_interface_homescreen_search_default, false);

        Intent intent = new Intent(ACTION_SEARCH_BAR_VISIBILITY_CHANGED);
        mContext.sendBroadcast(intent);
    }

    private boolean onSettingsBooleanChanged(View v, String key, int res, boolean invert) {
        boolean newValue = SettingsProvider.changeBoolean(mContext, key, res);
        ((Switch)v.findViewById(R.id.setting_switch)).setChecked(invert != newValue);
        return newValue;
    }

    private void onTextSettingsBooleanChanged(View v, String key, int defRes,
                                              int trueRes, int falseRes) {
        boolean newValue = SettingsProvider.changeBoolean(mContext, key, defRes);
        int state = newValue ? trueRes : falseRes;
        ((TextView) v.findViewById(R.id.item_state)).setText(state);
    }

    private void setStateText(TextView stateView, Switch settingSwitch, String state) {
        stateView.setText(state);
        stateView.setVisibility(View.VISIBLE);
        settingSwitch.setVisibility(View.INVISIBLE);
    }

    private void setSettingSwitch(TextView stateView, Switch settingSwitch, boolean isChecked) {
        settingSwitch.setChecked(isChecked);
        settingSwitch.setVisibility(View.VISIBLE);
        stateView.setVisibility(View.INVISIBLE);
    }

    private void hideStates(TextView stateView, Switch settingSwitch) {
        settingSwitch.setVisibility(View.INVISIBLE);
        stateView.setVisibility(View.INVISIBLE);
    }
}
