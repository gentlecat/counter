package me.tsukanov.counter.test;

import android.test.ActivityInstrumentationTestCase2;
import com.jayway.android.robotium.solo.Solo;
import me.tsukanov.counter.ui.MainActivity;

public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {
    private Solo solo;

    public MainActivityTest() {
        super(MainActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        solo = new Solo(getInstrumentation(), getActivity());
    }

    public void test() throws Exception {
        assertEquals(1, 1);
    }

    @Override
    protected void tearDown() throws Exception {
        solo.finishOpenedActivities();
    }

}
