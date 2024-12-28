package me.tsukanov.counter.infrastructure;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import androidx.annotation.NonNull;
import me.tsukanov.counter.CounterApplication;

public class BroadcastHelper {

  public static final String EXTRA_COUNTER_NAME =
      String.format("%s.EXTRA_COUNTER_NAME", CounterApplication.PACKAGE_NAME);

  private static final String TAG = BroadcastHelper.class.getSimpleName();

  private final Context context;

  public BroadcastHelper(@NonNull final Context context) {
    this.context = context;
  }

  private void sendBroadcast(@NonNull final Intent intent) {
    this.context.sendBroadcast(intent);
  }

  public void sendBroadcast(@NonNull final Actions action) {
    final Intent intent = new Intent();
    intent.setAction(action.getActionName());
    intent.addCategory(Intent.CATEGORY_DEFAULT);
    sendBroadcast(intent);
  }

  public void sendSelectCounterBroadcast(@NonNull final String counterName) {
    final Intent intent = new Intent();
    intent.setAction(Actions.SELECT_COUNTER.getActionName());
    intent.putExtra(EXTRA_COUNTER_NAME, counterName);
    intent.addCategory(Intent.CATEGORY_DEFAULT);
    sendBroadcast(intent);

    Log.d(TAG, String.format("Sending counter selection broadcast [counterName=%s]", counterName));
  }
}
