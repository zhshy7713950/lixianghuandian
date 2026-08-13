package androidx.fragment.app;


import java.lang.reflect.Field;
import java.util.List;

/**
 * Created by YoKey on 16/1/22.
 *
 * Modified locally to support AndroidX Fragment 1.3.6, where the saved-state
 * fields are private members of FragmentManager.
 */
public class FragmentationMagician {

    private static final Field STATE_SAVED_FIELD = findFragmentManagerField("mStateSaved");
    private static final Field STOPPED_FIELD = findFragmentManagerField("mStopped");

    public static boolean isStateSaved(FragmentManager fragmentManager) {
        return fragmentManager != null && fragmentManager.isStateSaved();
    }

    /**
     * Like {@link FragmentManager#popBackStack()} but allows the commit to be executed after an
     * activity's state is saved.  This is dangerous because the action can
     * be lost if the activity needs to later be restored from its state, so
     * this should only be used for cases where it is okay for the UI state
     * to change unexpectedly on the user.
     */
    public static void popBackStackAllowingStateLoss(final FragmentManager fragmentManager) {
        FragmentationMagician.hookStateSaved(fragmentManager, new Runnable() {
            @Override
            public void run() {
                fragmentManager.popBackStack();
            }
        });
    }

    /**
     * Like {@link FragmentManager#popBackStackImmediate()} but allows the commit to be executed after an
     * activity's state is saved.
     */
    public static void popBackStackImmediateAllowingStateLoss(final FragmentManager fragmentManager) {
        FragmentationMagician.hookStateSaved(fragmentManager, new Runnable() {
            @Override
            public void run() {
                fragmentManager.popBackStackImmediate();
            }
        });
    }

    /**
     * Like {@link FragmentManager#popBackStackImmediate(String, int)} but allows the commit to be executed after an
     * activity's state is saved.
     */
    public static void popBackStackAllowingStateLoss(final FragmentManager fragmentManager, final String name, final int flags) {
        FragmentationMagician.hookStateSaved(fragmentManager, new Runnable() {
            @Override
            public void run() {
                fragmentManager.popBackStack(name, flags);
            }
        });
    }

    /**
     * Like {@link FragmentManager#executePendingTransactions()} but allows the commit to be executed after an
     * activity's state is saved.
     */
    public static void executePendingTransactionsAllowingStateLoss(final FragmentManager fragmentManager) {
        FragmentationMagician.hookStateSaved(fragmentManager, new Runnable() {
            @Override
            public void run() {
                fragmentManager.executePendingTransactions();
            }
        });
    }

    public static List<Fragment> getActiveFragments(FragmentManager fragmentManager) {
        return fragmentManager.getFragments();
    }

    static void hookStateSaved(FragmentManager fragmentManager, Runnable runnable) {
        if (!isStateSaved(fragmentManager)) {
            runnable.run();
            return;
        }

        if (STATE_SAVED_FIELD == null || STOPPED_FIELD == null) {
            throw new IllegalStateException(
                    "FragmentationX is incompatible with the resolved AndroidX Fragment version");
        }

        try {
            boolean tempStateSaved = STATE_SAVED_FIELD.getBoolean(fragmentManager);
            boolean tempStopped = STOPPED_FIELD.getBoolean(fragmentManager);
            STATE_SAVED_FIELD.setBoolean(fragmentManager, false);
            STOPPED_FIELD.setBoolean(fragmentManager, false);

            try {
                runnable.run();
            } finally {
                STOPPED_FIELD.setBoolean(fragmentManager, tempStopped);
                STATE_SAVED_FIELD.setBoolean(fragmentManager, tempStateSaved);
            }
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(
                    "Unable to access AndroidX FragmentManager saved-state fields", e);
        }
    }

    private static Field findFragmentManagerField(String name) {
        try {
            Field field = FragmentManager.class.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException e) {
            return null;
        }
    }
}
