package me.tsukanov.counter.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.text.InputFilter;
import android.text.InputType;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.*;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import me.tsukanov.counter.CounterApplication;
import me.tsukanov.counter.R;

public class CounterFragment extends SherlockFragment {
    private static final int MAX_VALUE = 9999;
    private static final int MIN_VALUE = 0;
    private static final int DEFAULT_VALUE = MIN_VALUE;
    private static final long DEFAULT_VIBRATION_DURATION = 30; // Milliseconds
    private String name = null;
    private int value = DEFAULT_VALUE;
    private CounterApplication app;
    private SharedPreferences settings;
    private Vibrator vibrator;
    private SoundPool soundPool;
    private SparseIntArray soundsMap;
    private TextView counterLabel;
    private Button incrementButton;
    private Button decrementButton;

    public CounterFragment() {
    }

    public CounterFragment(String name) {
        this.name = name;
    }

    public CounterFragment(String name, int value) {
        this.name = name;
        this.value = value;
    }

    public static int getMaxValue() {
        return MAX_VALUE;
    }

    public static int getMinValue() {
        return MIN_VALUE;
    }

    public static int getDefaultValue() {
        return DEFAULT_VALUE;
    }

    public String getName() {
        return name;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        app = (CounterApplication) getActivity().getApplication();
        vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        settings = PreferenceManager.getDefaultSharedPreferences(getActivity());

        getActivity().setVolumeControlStream(AudioManager.STREAM_MUSIC);
        soundPool = new SoundPool(4, AudioManager.STREAM_MUSIC, 0);
        soundsMap = new SparseIntArray(3);
        soundsMap.put(Sound.INCREMENT_SOUND.ordinal(), soundPool.load(getActivity(), R.raw.increment_sound, 1));
        soundsMap.put(Sound.DECREMENT_SOUND.ordinal(), soundPool.load(getActivity(), R.raw.decrement_sound, 1));
        soundsMap.put(Sound.REFRESH_SOUND.ordinal(), soundPool.load(getActivity(), R.raw.refresh_sound, 1));

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View view = inflater.inflate(R.layout.counter, container, false);

        counterLabel = (TextView) view.findViewById(R.id.counterLabel);
        incrementButton = (Button) view.findViewById(R.id.incrementButton);
        incrementButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                increment();
            }
        });
        decrementButton = (Button) view.findViewById(R.id.decrementButton);
        decrementButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                decrement();
            }
        });

        if (name == null) name = getActivity().getString(R.string.default_counter_name);
        if (app.counters.containsKey(name)) {
            setValue(app.counters.get(name));
        } else {
            app.counters.put(name, value);
        }

        getSherlockActivity().getSupportActionBar().setTitle(name);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.counter_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_refresh:
                refresh();
                return true;
            case R.id.menu_edit:
                showEditDialog();
                return true;
            case R.id.menu_delete:
                showDeleteDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showEditDialog() {
        Context context = getSherlockActivity();

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(getResources().getText(R.string.dialog_edit_title));

        LinearLayout editDialogLayout = (LinearLayout) getSherlockActivity().getLayoutInflater()
                .inflate(R.layout.editor_layout, null);

        // Name input label
        TextView nameInputLabel = new TextView(getSherlockActivity().getApplicationContext());
        nameInputLabel.setText(getResources()
                .getText(R.string.dialog_edit_name));
        editDialogLayout.addView(nameInputLabel);

        // Name input
        final EditText nameInput = new EditText(context);
        nameInput.setInputType(InputType.TYPE_CLASS_TEXT);
        nameInput.setText(name);
        editDialogLayout.addView(nameInput);

        // Value input label
        TextView valueInputLabel = new TextView(context);
        valueInputLabel.setText(getResources().getText(
                R.string.dialog_edit_value));
        valueInputLabel.setPadding(0, 10, 0, 0);
        editDialogLayout.addView(valueInputLabel);

        // Value input
        final EditText valueInput = new EditText(context);
        valueInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        valueInput.setText(String.valueOf(value));
        InputFilter[] valueFilter = new InputFilter[1];
        valueFilter[0] = new InputFilter.LengthFilter(getValueCharLimit());
        valueInput.setFilters(valueFilter);
        editDialogLayout.addView(valueInput);

        builder.setView(editDialogLayout)
                .setPositiveButton(getResources().getText(R.string.dialog_button_apply),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                String newName = nameInput.getText().toString();
                                if (newName.equals("")) {
                                    Toast.makeText(getSherlockActivity().getApplicationContext(),
                                            getResources().getText(R.string.toast_no_name_message),
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    app.counters.remove(name);
                                    int newValue;
                                    String valueInputContents = valueInput.getText().toString();
                                    if (!valueInputContents.equals("")) {
                                        newValue = Integer.parseInt(valueInputContents);
                                    } else {
                                        newValue = DEFAULT_VALUE;
                                    }
                                    CounterActivity activity = (CounterActivity) getActivity();
                                    activity.countersListFragment.updateList();
                                    switchFragment(new CounterFragment(newName, newValue));
                                }
                            }
                        })
                .setNegativeButton(getResources().getText(R.string.dialog_button_cancel), null);
        builder.create().show();
    }

    private void showDeleteDialog() {
        Context context = getSherlockActivity();

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(getResources().getText(R.string.dialog_delete_title))
                .setCancelable(false)
                .setPositiveButton(getResources().getText(R.string.dialog_button_delete),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                app.counters.remove(name);
                                CounterActivity activity = (CounterActivity) getActivity();
                                activity.countersListFragment.updateList();
                                Toast.makeText(
                                        getSherlockActivity().getApplicationContext(),
                                        getResources().getText(R.string.toast_remove_success_1) + " \"" + name + "\" "
                                                + getResources().getText(R.string.toast_remove_success_2),
                                        Toast.LENGTH_SHORT).show();
                                if (app.counters.isEmpty()) {
                                    switchFragment(new CounterFragment(getSherlockActivity().getString(R.string.default_counter_name)));
                                } else {
                                    switchFragment(new CounterFragment(app.counters.keySet().iterator().next()));
                                }
                            }
                        })
                .setNegativeButton(getResources().getText(R.string.dialog_button_cancel), null);
        builder.create().show();
    }

    private void switchFragment(CounterFragment fragment) {
        if (getActivity() == null) return;
        if (getActivity() instanceof CounterActivity) {
            CounterActivity activity = (CounterActivity) getActivity();
            activity.switchCounter(fragment);
        }
    }

    public void increment() {
        if (value < MAX_VALUE) {
            setValue(++value);
            vibrate(DEFAULT_VIBRATION_DURATION);
            playSound(Sound.INCREMENT_SOUND);
        }
    }

    public void decrement() {
        if (value > MIN_VALUE) {
            setValue(--value);
            vibrate(DEFAULT_VIBRATION_DURATION + 20);
            playSound(Sound.DECREMENT_SOUND);
        }
    }

    public void refresh() {
        setValue(DEFAULT_VALUE);
        vibrate(DEFAULT_VIBRATION_DURATION + 40);
        playSound(Sound.REFRESH_SOUND);
    }

    public void setValue(int value) {
        if (value > MAX_VALUE) value = MAX_VALUE;
        else if (value < MIN_VALUE) value = MIN_VALUE;
        this.value = value;
        counterLabel.setText(Integer.toString(value));
        checkStateOfButtons();
        saveValue();
    }

    private void saveValue() {
        app.counters.remove(name);
        app.counters.put(name, value);
    }

    private void checkStateOfButtons() {
        if (value >= MAX_VALUE) incrementButton.setEnabled(false);
        else incrementButton.setEnabled(true);
        if (value <= MIN_VALUE) decrementButton.setEnabled(false);
        else decrementButton.setEnabled(true);
    }

    private void vibrate(long duration) {
        if (settings.getBoolean("vibrationOn", true)) {
            vibrator.vibrate(duration);
        }
    }

    private void playSound(Sound sound) {
        if (settings.getBoolean("soundsOn", false)) {
            AudioManager audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
            float volume = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            soundPool.play(soundsMap.get(sound.ordinal()), volume, volume, 1, 0, 1f);
        }
    }

    private int getValueCharLimit() {
        return String.valueOf(CounterFragment.getMaxValue()).length();
    }

    private enum Sound {INCREMENT_SOUND, DECREMENT_SOUND, REFRESH_SOUND}

    private enum DialogType {EDIT, DELETE}

}
