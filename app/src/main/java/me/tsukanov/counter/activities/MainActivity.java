package me.tsukanov.counter.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;
import java.util.List;
import me.tsukanov.counter.CounterApplication;
import me.tsukanov.counter.R;
import me.tsukanov.counter.SharedPrefKeys;
import me.tsukanov.counter.domain.IntegerCounter;
import me.tsukanov.counter.infrastructure.Actions;
import me.tsukanov.counter.infrastructure.BroadcastHelper;
import me.tsukanov.counter.repository.CounterStorage;
import me.tsukanov.counter.repository.exceptions.MissingCounterException;
import me.tsukanov.counter.view.CounterFragment;
import me.tsukanov.counter.view.CountersListFragment;
import me.tsukanov.counter.view.Themes;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();

  private ActionBar actionBar;
  private DrawerLayout navigationLayout;
  private ActionBarDrawerToggle navigationToggle;
  private SharedPreferences sharedPrefs;
  private FrameLayout menuFrame;
  private String selectedCounterName;
  private CounterFragment selectedCounterFragment;

  @Override
  public void onCreate(final Bundle savedInstanceState) {

    actionBar = getSupportActionBar();
    sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
    registerIntentReceivers(getApplicationContext());

    super.onCreate(savedInstanceState);

    // Enable ActionBar home button to behave as action to toggle navigation drawer
    actionBar.setDisplayHomeAsUpEnabled(true);
    actionBar.setHomeButtonEnabled(true);

    setContentView(R.layout.drawer_layout);

    navigationLayout = findViewById(R.id.drawer_layout);
    navigationToggle = generateActionBarToggle(this.actionBar, navigationLayout);
    navigationLayout.addDrawerListener(navigationToggle);

    menuFrame = findViewById(R.id.menu_frame);
  }

  private void registerIntentReceivers(@NonNull final Context context) {
    final IntentFilter counterSelectionFilter =
        new IntentFilter(Actions.SELECT_COUNTER.getActionName());
    counterSelectionFilter.addCategory(Intent.CATEGORY_DEFAULT);
    ContextCompat.registerReceiver(
        context,
        new CounterChangeReceiver(),
        counterSelectionFilter,
        ContextCompat.RECEIVER_NOT_EXPORTED);
  }

  private ActionBarDrawerToggle generateActionBarToggle(
      @NonNull final ActionBar actionBar, @NonNull final DrawerLayout drawerLayout) {

    return new ActionBarDrawerToggle(
        this, drawerLayout, null, R.string.drawer_open, R.string.drawer_close) {

      public void onDrawerClosed(View view) {
        actionBar.setTitle(selectedCounterName);
        supportInvalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
      }

      public void onDrawerOpened(View drawerView) {
        actionBar.setTitle(getTitle());
        supportInvalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
      }
    };
  }

  @Override
  protected void onResume() {
    super.onResume();

    Themes.initCurrentTheme(sharedPrefs);

    initCountersList();
    switchCounter(findActiveCounter().getName());

    if (sharedPrefs.getBoolean(SharedPrefKeys.KEEP_SCREEN_ON.getName(), false)) {
      getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
    } else {
      getWindow().clearFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    final boolean isGooglePlayWarningSeen =
        sharedPrefs.getBoolean(SharedPrefKeys.GOOGLE_PLAY_WARNING.getName(), false);
    if (!isGooglePlayWarningSeen && isInstalledViaGooglePlay(this)) {
      showGooglePlayDialog();
    }
  }

  private void initCountersList() {
    final FragmentTransaction transaction = this.getSupportFragmentManager().beginTransaction();
    transaction.replace(R.id.menu_frame, new CountersListFragment());
    transaction.commit();
  }

  private void switchCounter(@NonNull final String counterName) {
    this.selectedCounterName = counterName;

    final Bundle bundle = new Bundle();
    bundle.putString(CounterFragment.COUNTER_NAME_ATTRIBUTE, counterName);

    selectedCounterFragment = new CounterFragment();
    selectedCounterFragment.setArguments(bundle);
    getSupportFragmentManager()
        .beginTransaction()
        .replace(R.id.content_frame, selectedCounterFragment)
        .commitAllowingStateLoss();

    actionBar.setTitle(counterName);

    if (isNavigationOpen()) {
      closeNavigation();
    }
  }

  /**
   * Finds which counter is selected based on priority.
   *
   * <ol>
   *   <li>From value stored during the previous session
   *   <li>First of the stored counters <em>(default one will be generated if none are present)</em>
   * </ol>
   */
  private IntegerCounter findActiveCounter() {
    CounterStorage<IntegerCounter> storage = CounterApplication.getComponent().localStorage();

    final String savedActiveCounter =
        sharedPrefs.getString(SharedPrefKeys.ACTIVE_COUNTER.getName(), null);

    // Checking whether it still exists...
    if (savedActiveCounter != null) {
      try {
        return storage.read(savedActiveCounter);
      } catch (MissingCounterException e) {
        // No need to do anything.
      }
    }

    return storage.readAll(true).get(0);
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    // Since there's no way to keep track of these events from the fragment side, we have to pass
    // them to it for processing.
    return super.onKeyDown(keyCode, event) || selectedCounterFragment.onKeyDown(keyCode);
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

  @SuppressLint("ApplySharedPref")
  @Override
  protected void onPause() {
    super.onPause();

    final SharedPreferences.Editor prefsEditor = sharedPrefs.edit();
    prefsEditor.putString(SharedPrefKeys.ACTIVE_COUNTER.getName(), selectedCounterName);
    prefsEditor.commit();
  }

  @Override
  public void onConfigurationChanged(@NonNull final Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    navigationToggle.onConfigurationChanged(newConfig);
  }

  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        if (isNavigationOpen()) {
          closeNavigation();
        } else {
          openDrawer();
        }
        return true;

      case R.id.menu_settings:
        startActivity(new Intent(this, SettingsActivity.class));
        return true;

      default:
        return super.onOptionsItemSelected(item);
    }
  }

  private boolean isInstalledViaGooglePlay(@NonNull final Context context) {
    final String installer =
        context.getPackageManager().getInstallerPackageName(context.getPackageName());
    return installer != null
        && List.of("com.android.vending", "com.google.android.feedback").contains(installer);
  }

  @SuppressWarnings("checkstyle:linelength")
  private void showGooglePlayDialog() {
    final SpannableString message =
        new SpannableString(
            """
            Apologies for the interruption.
      
            In January 2025 my developer account will most likely be suspended and this app will be delisted from the Google Play Store due to policy changes. I'm now required to list my home address, which I'm not willing to do.
      
            You can still get this app from F-Droid, GitHub and other sources. Go to https://counter.roman.zone for details.
      
            Thank you for using the app! You will only see this message once.
            """);
    Linkify.addLinks(message, Linkify.ALL);

    final AlertDialog d =
        new AlertDialog.Builder(this)
            .setTitle("A note from the developer")
            .setMessage(message)
            .setPositiveButton(
                "I understand",
                (dialog, id) -> {
                  sharedPrefs.edit()
                          .putBoolean(SharedPrefKeys.GOOGLE_PLAY_WARNING.getName(), true)
                          .apply();
                  Toast.makeText(getBaseContext(), "Thanks for using the app.", Toast.LENGTH_SHORT)
                      .show();
                })
            .create();

    d.show();

    // Make the textview clickable. Must be called after show()
    ((TextView) d.findViewById(android.R.id.message))
        .setMovementMethod(LinkMovementMethod.getInstance());

    //
    //
    //
    //    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
    //    builder.setTitle("A note from the developer");
    //    builder.setMessage(
    //        """
    //        Apologies for the interruption. A note from the developer of this app:
    //
    //        In January of 2025 my developer account will most likely be suspended and this app
    // will be delisted from Google Play Store.
    //
    //        You can still get this app from F-Droid, GitHub and other sources.
    //
    //        ADD URL NOT GITHUB
    //
    //        You will only see this message once.
    //        """);
    //    builder.setPositiveButton(
    //        "I understand",
    //        (dialog, id) -> {
    //          Toast.makeText(
    //                  getBaseContext(), "Thanks for using the app.", Toast.LENGTH_SHORT)
    //              .show();
    //        });
    //
    //    builder.create().show();
  }

  public boolean isNavigationOpen() {
    return navigationLayout.isDrawerOpen(menuFrame);
  }

  private void closeNavigation() {
    navigationLayout.closeDrawer(menuFrame);
  }

  private void openDrawer() {
    navigationLayout.openDrawer(menuFrame);
  }

  private class CounterChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
      final String counterName = intent.getStringExtra(BroadcastHelper.EXTRA_COUNTER_NAME);
      Log.d(
          TAG,
          String.format(
              "Received counter change broadcast. Switching to counter \"%s\"", counterName));
      switchCounter(counterName);
    }
  }
}
