package me.tsukanov.counter;

import androidx.annotation.NonNull;

public enum SharedPrefKeys {
  ACTIVE_COUNTER("activeKey"),
  KEEP_SCREEN_ON("keepScreenOn"),
  LABEL_CONTROL_ON("labelControlOn"),
  HARDWARE_BTN_CONTROL_ON("hardControlOn"),
  VIBRATION_ON("vibrationOn"),
  SOUNDS_ON("soundsOn"),
  THEME("theme");

  @NonNull private final String name;

  SharedPrefKeys(@NonNull final String name) {
    this.name = name;
  }

  @NonNull
  public String getName() {
    return name;
  }
}
