package com.ruimeng.things.voice;

/** Pure state holder so audio start decisions can be unit tested without Android runtime. */
final class VoicePlaybackState {

    enum FocusState {
        NONE,
        DELAYED,
        GRANTED,
        LOST
    }

    private boolean foreground;
    private boolean prepared;
    private FocusState focusState = FocusState.NONE;

    synchronized void reset(boolean foreground) {
        this.foreground = foreground;
        prepared = false;
        focusState = FocusState.NONE;
    }

    synchronized void setForeground(boolean foreground) {
        this.foreground = foreground;
    }

    synchronized void onPrepared() {
        prepared = true;
    }

    synchronized void setFocusState(FocusState focusState) {
        this.focusState = focusState;
    }

    synchronized boolean canStart() {
        return foreground && prepared && focusState == FocusState.GRANTED;
    }

    synchronized boolean isWaitingForFocus() {
        return foreground && prepared && focusState == FocusState.DELAYED;
    }
}
