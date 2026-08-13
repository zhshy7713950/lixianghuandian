package com.ruimeng.things.voice;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class VoicePlaybackStateTest {

    @Test
    public void grantedFocus_startsOnlyWhenPreparedAndForeground() {
        VoicePlaybackState state = new VoicePlaybackState();
        state.reset(false);
        state.onPrepared();
        state.setFocusState(VoicePlaybackState.FocusState.GRANTED);

        assertFalse(state.canStart());

        state.setForeground(true);
        assertTrue(state.canStart());

        state.setForeground(false);
        assertFalse(state.canStart());
    }

    @Test
    public void delayedFocus_waitsUntilGainAndStopsAfterLoss() {
        VoicePlaybackState state = new VoicePlaybackState();
        state.reset(true);
        state.onPrepared();
        state.setFocusState(VoicePlaybackState.FocusState.DELAYED);

        assertTrue(state.isWaitingForFocus());
        assertFalse(state.canStart());

        state.setFocusState(VoicePlaybackState.FocusState.GRANTED);
        assertFalse(state.isWaitingForFocus());
        assertTrue(state.canStart());

        state.setFocusState(VoicePlaybackState.FocusState.LOST);
        assertFalse(state.canStart());
    }

    @Test
    public void reset_clearsPreparedAndFocusState() {
        VoicePlaybackState state = new VoicePlaybackState();
        state.reset(true);
        state.onPrepared();
        state.setFocusState(VoicePlaybackState.FocusState.GRANTED);
        assertTrue(state.canStart());

        state.reset(true);
        assertFalse(state.canStart());
        assertFalse(state.isWaitingForFocus());
    }
}
