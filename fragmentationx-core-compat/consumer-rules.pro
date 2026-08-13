# FragmentationX compatibility bridge reads these AndroidX Fragment 1.3.6
# fields by name while executing back-stack operations after state is saved.
-keepclassmembernames class androidx.fragment.app.FragmentManager {
    boolean mStateSaved;
    boolean mStopped;
}

# Preserve the upstream FragmentationX 1.0.2 consumer rule after AndroidX migration.
-keep class * extends androidx.fragment.app.FragmentManager { *; }
