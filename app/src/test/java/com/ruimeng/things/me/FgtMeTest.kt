package com.ruimeng.things.me

import org.junit.Test

class FgtMeTest {
    @Test
    fun destroyedViewIgnoresRefreshCompletion() {
        FgtMe().finishRefresh()
    }
}
