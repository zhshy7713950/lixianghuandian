package com.view;

import android.os.Parcel;
import android.view.View;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class MyTextViewSavedStateTest {

    private static final int SENTINEL = 0x12345678;

    @Test
    public void parcelRoundTrip_whenScrolling_consumesEntireSavedState() {
        assertRoundTrip(true, 2594.0f);
    }

    @Test
    public void parcelRoundTrip_whenStopped_consumesEntireSavedState() {
        assertRoundTrip(false, 0.0f);
    }

    private void assertRoundTrip(boolean isStarting, float step) {
        MyTextView.SavedState source =
                new MyTextView.SavedState(View.BaseSavedState.EMPTY_STATE);
        source.isStarting = isStarting;
        source.step = step;

        Parcel parcel = Parcel.obtain();
        try {
            source.writeToParcel(parcel, 0);
            parcel.writeInt(SENTINEL);
            parcel.setDataPosition(0);

            MyTextView.SavedState restored = MyTextView.SavedState.CREATOR
                    .createFromParcel(parcel);

            if (isStarting) {
                assertTrue(restored.isStarting);
            } else {
                assertFalse(restored.isStarting);
            }
            assertEquals(step, restored.step, 0.0f);
            assertEquals(SENTINEL, parcel.readInt());
            assertEquals(parcel.dataSize(), parcel.dataPosition());
        } finally {
            parcel.recycle();
        }
    }
}
