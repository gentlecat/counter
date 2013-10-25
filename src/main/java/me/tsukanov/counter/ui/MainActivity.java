package me.tsukanov.counter.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import me.tsukanov.counter.CounterApplication;
import me.tsukanov.counter.R;
import me.tsukanov.counter.ui.dialogs.AboutDialog;

public class MainActivity extends ActionBarActivity {

    private static final String STATE_TITLE = "title";
    private static final String STATE_IS_NAV_OPEN = "is_nav_open";
    private static final String STATE_ACTIVE_COUNTER = "activeKey";
    public CountersListFragment countersListFragment;
    public CounterFragment currentCounterFragment;
    private CounterApplication app;
    private ActionBar actionBar;
    private DrawerLayout navigationLayout;
    private ActionBarDrawerToggle navigationToggle;
    private CharSequence title;
    private SharedPreferences sharedPref;
    private FrameLayout menuFrame;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawer_layout);

        app = (CounterApplication) getApplication();

        final CharSequence drawerTitle = title = getTitle();
        navigationLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        menuFrame = (FrameLayout) findViewById(R.id.menu_frame);

        actionBar = getSupportActionBar();

        // Enable ActionBar home button to behave as action to toggle navigation drawer
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        navigationToggle = new ActionBarDrawerToggle(
                this,
                navigationLayout,
                R.drawable.ic_navigation_drawer,
                R.string.drawer_open,
                R.string.drawer_close
        ) {
            public void onDrawerClosed(View view) {
                actionBar.setTitle(title);
            }

            public void onDrawerOpened(View drawerView) {
                title = actionBar.getTitle();
                actionBar.setTitle(drawerTitle);
            }
        };
        navigationLayout.setDrawerListener(navigationToggle);

        if (savedInstanceState != null) {
            title = savedInstanceState.getCharSequence(STATE_TITLE);
            if (savedInstanceState.getBoolean(STATE_IS_NAV_OPEN)) {
                actionBar.setTitle(drawerTitle);
            } else {
                actionBar.setTitle(title);
            }
        }

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

        String previousCounter = sharedPref.getString(STATE_ACTIVE_COUNTER, getString(R.string.default_counter_name));
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
        settingsEditor.putString(STATE_ACTIVE_COUNTER, currentCounterFragment.getCounterName());
        settingsEditor.commit();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return currentCounterFragment.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        navigationToggle.syncState();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putCharSequence(STATE_TITLE, title);
        savedInstanceState.putBoolean(STATE_IS_NAV_OPEN, navigationLayout.isDrawerOpen(menuFrame));
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        navigationToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (navigationLayout.isDrawerOpen(menuFrame)) {
                    closeDrawer();
                } else {
                    openDrawer();
                }
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

    public void switchCounter(final CounterFragment counterFragment) {
        currentCounterFragment = counterFragment;
        getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, currentCounterFragment).commit();
        closeDrawer();
    }

    public void closeDrawer() {
        navigationLayout.closeDrawer(menuFrame);
    }

    public void openDrawer() {
        navigationLayout.openDrawer(menuFrame);
    }

}
