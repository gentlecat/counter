package me.tsukanov.counter.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.slidingmenu.lib.SlidingMenu;
import com.slidingmenu.lib.app.SlidingFragmentActivity;
import me.tsukanov.counter.CounterApplication;
import me.tsukanov.counter.R;
import me.tsukanov.counter.ui.dialogs.AboutDialog;

public class MainActivity extends SlidingFragmentActivity {
    public CountersListFragment countersListFragment;
    public CounterFragment currentCounterFragment;
    private CounterApplication app;
    private SharedPreferences sharedPref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_frame);

        app = (CounterApplication) getApplication();

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPref.getBoolean("isFirstLaunch", true)) {
            sharedPref.edit().putBoolean("isFirstLaunch", false).commit();
            showAboutDialog();
        }

        setBehindContentView(R.layout.menu_frame);

        SlidingMenu sm = getSlidingMenu();
        sm.setShadowWidthRes(R.dimen.shadow_width);
        sm.setShadowDrawable(R.drawable.shadow);
        sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        sm.setBehindWidthRes(R.dimen.behind_width);
        sm.setFadeDegree(0.1f);
        sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onResume() {
        super.onResume();

        FragmentTransaction transaction = this.getSupportFragmentManager().beginTransaction();
        countersListFragment = new CountersListFragment();
        transaction.replace(R.id.menu_frame, countersListFragment);
        transaction.commit();

        String previousCounter = sharedPref.getString("activeKey", getString(R.string.default_counter_name));
        switchCounter(new CounterFragment(previousCounter));

        if (sharedPref.getBoolean("keepScreenOn", false)) {
            getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        app.saveCounters();
        SharedPreferences.Editor settingsEditor = sharedPref.edit();
        settingsEditor.putString("activeKey", currentCounterFragment.getCounterName());
        settingsEditor.commit();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (prefs.getBoolean("hardControlOn", true)) {
                    currentCounterFragment.increment();
                    return true;
                }
                return false;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (prefs.getBoolean("hardControlOn", true)) {
                    currentCounterFragment.decrement();
                    return true;
                }
                return false;
            case KeyEvent.KEYCODE_CAMERA:
                if (prefs.getBoolean("hardControlOn", true)) {
                    currentCounterFragment.refresh();
                    return true;
                }
                return false;
            default:
                return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                toggle();
                return true;
            case R.id.menu_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.menu_info:
                showAboutDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showAboutDialog() {
        AboutDialog dialog = new AboutDialog();
        dialog.show(getSupportFragmentManager(), AboutDialog.TAG);
    }

    public void switchCounter(final CounterFragment fragment) {
        currentCounterFragment = fragment;
        getSupportFragmentManager().beginTransaction().replace(android.R.id.content, currentCounterFragment).commit();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                getSlidingMenu().showContent();
            }
        }, 50);
    }

    private void refreshActivity() {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

}
