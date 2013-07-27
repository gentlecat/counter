package me.tsukanov.counter.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import me.tsukanov.counter.CounterApplication;
import me.tsukanov.counter.R;
import me.tsukanov.counter.ui.dialogs.AboutDialog;

public class MainActivity extends SherlockFragmentActivity {
    private static final long HARD_DEALY = 700; // Milliseconds
    public CountersListFragment countersListFragment;
    public CounterFragment currentCounterFragment;
    private CounterApplication app;
    private SharedPreferences sharedPref;
    private long lastHardIncrementationTime = System.currentTimeMillis();
    private long lastHardDecrementationTime = System.currentTimeMillis();
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private FrameLayout menuFrame;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawer_layout);

        app = (CounterApplication) getApplication();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        menuFrame = (FrameLayout) findViewById(R.id.menu_frame);

        drawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                R.drawable.ic_drawer,
                R.string.drawer_open,
                R.string.drawer_close
        ) {
            public void onDrawerClosed(View view) {
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                invalidateOptionsMenu();
            }
        };
        drawerLayout.setDrawerListener(drawerToggle);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPref.getBoolean("isFirstLaunch", true)) {
            sharedPref.edit().putBoolean("isFirstLaunch", false).commit();
            showAboutDialog();
        }
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
                    if ((System.currentTimeMillis() - lastHardIncrementationTime) > HARD_DEALY) {
                        currentCounterFragment.increment();
                        lastHardIncrementationTime = System.currentTimeMillis();
                    }
                    return true;
                }
                return false;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (prefs.getBoolean("hardControlOn", true)) {
                    if ((System.currentTimeMillis() - lastHardDecrementationTime) > HARD_DEALY) {
                        currentCounterFragment.decrement();
                        lastHardDecrementationTime = System.currentTimeMillis();
                    }
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
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        switch (item.getItemId()) {
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
        drawerLayout.closeDrawer(menuFrame);
    }

}
