package me.tsukanov.counter

enum class SharedPrefKeys(val key: String) {
    ACTIVE_COUNTER("activeKey"),
    KEEP_SCREEN_ON("keepScreenOn"),
    LABEL_CONTROL_ON("labelControlOn"),
    HARDWARE_BTN_CONTROL_ON("hardControlOn"),
    VIBRATION_ON("vibrationOn"),
    SOUNDS_ON("soundsOn"),
    THEME("theme"),
}
