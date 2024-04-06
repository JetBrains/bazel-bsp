package com.example.myapplication;

import android.widget.TextView;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
public class MainActivityTest {
    @Test
    public void testTextView() {
        try (ActivityController<MainActivity> controller = Robolectric.buildActivity(MainActivity.class)) {
            controller.setup();
            MainActivity activity = controller.get();
            TextView textView = activity.findViewById(R.id.text_view);
            assertEquals("Search", textView.getText());
        }
    }
}
