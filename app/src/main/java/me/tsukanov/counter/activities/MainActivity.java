package me.tsukanov.counter.activities;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager.LayoutParams;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;
import com.google.android.material.appbar.MaterialToolbar;
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

  private MaterialToolbar toolbar;
  private DrawerLayout navigationLayout;
  private SharedPreferences sharedPrefs;
  private FrameLayout menuFrame;
  private String selectedCounterName;
  private CounterFragment selectedCounterFragment;

  @Override
  public void onCreate(final Bundle savedInstanceState) {

    sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
    registerIntentReceivers(getApplicationContext());

    super.onCreate(savedInstanceState);

    setContentView(R.layout.layout_main);

    toolbar = findViewById(R.id.mainToolbar);
    setSupportActionBar(toolbar);

    toolbar.setNavigationOnClickListener(view -> openDrawer());

    navigationLayout = findViewById(R.id.mainNavigationLayout);
    menuFrame = findViewById(R.id.mainMenuFrame);
  }

  private void registerIntentReceivers(@NonNull final Context context) {
    final IntentFilter counterSelectionFilter =
        new IntentFilter(Actions.SELECT_COUNTER.getActionName());
    counterSelectionFilter.addCategory(Intent.CATEGORY_DEFAULT);
    ContextCompat.registerReceiver(
        context,
        new CounterChangeReceiver(),
        counterSelectionFilter,
        ContextCompat.RECEIVER_EXPORTED);
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
  }

  private void initCountersList() {
    final FragmentTransaction transaction = this.getSupportFragmentManager().beginTransaction();
    transaction.replace(R.id.mainMenuFrame, new CountersListFragment());
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
        .replace(R.id.mainContentFrame, selectedCounterFragment)
        .commitAllowingStateLoss();

    toolbar.setTitle(counterName);

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

  @SuppressLint("ApplySharedPref")
  @Override
  protected void onPause() {
    super.onPause();

    final SharedPreferences.Editor prefsEditor = sharedPrefs.edit();
    prefsEditor.putString(SharedPrefKeys.ACTIVE_COUNTER.getName(), selectedCounterName);
    prefsEditor.commit();
  }

  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    return switch (item.getItemId()) {
      case android.R.id.home -> {
        if (isNavigationOpen()) {
          closeNavigation();
        } else {
          openDrawer();
        }
        yield true;
      }
      case R.id.menu_settings -> {
        startActivity(new Intent(this, SettingsActivity.class));
        yield true;
      }
      default -> super.onOptionsItemSelected(item);
    };
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
