package androidx.fragment.app;

import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class FragmentationMagicianTest {

    @Test
    public void hookStateSavedTemporarilyClearsAndAlwaysRestoresState() throws Exception {
        final FragmentManager fragmentManager = new FragmentManagerImpl();
        final Field stateSaved = savedStateField("mStateSaved");
        final Field stopped = savedStateField("mStopped");
        stateSaved.setBoolean(fragmentManager, true);
        stopped.setBoolean(fragmentManager, true);

        try {
            FragmentationMagician.hookStateSaved(fragmentManager, new Runnable() {
                @Override
                public void run() {
                    try {
                        assertFalse(stateSaved.getBoolean(fragmentManager));
                        assertFalse(stopped.getBoolean(fragmentManager));
                    } catch (IllegalAccessException e) {
                        throw new AssertionError(e);
                    }
                    throw new ExpectedException();
                }
            });
            fail("Expected the test runnable to throw");
        } catch (ExpectedException expected) {
            assertTrue(stateSaved.getBoolean(fragmentManager));
            assertTrue(stopped.getBoolean(fragmentManager));
        }
    }

    private static Field savedStateField(String name) throws NoSuchFieldException {
        Field field = FragmentManager.class.getDeclaredField(name);
        field.setAccessible(true);
        return field;
    }

    private static final class ExpectedException extends RuntimeException {
    }
}
