package me.tsukanov.counter.ui;

import com.xtremelabs.robolectric.RobolectricTestRunner;
import me.tsukanov.counter.R;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricTestRunner.class)
public class CounterActivityTest {

    @Test
    public void shouldHaveProperAppName() throws Exception {
        String appName = new CounterActivity().getResources().getString(R.string.app_name);
        assertThat(appName, equalTo("Simple Counter"));
    }
}