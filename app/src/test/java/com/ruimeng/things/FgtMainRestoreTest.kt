package com.ruimeng.things

import com.ruimeng.things.home.FgtHome
import com.ruimeng.things.me.FgtMe
import com.ruimeng.things.me.FgtTicket
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test

class FgtMainRestoreTest {
    @Test
    fun missingRestoredChildFallsBackToFreshTabs() {
        assertNull(
            restoredMainTabFragments(
                FgtHome(),
                null,
                FgtTicket(),
                FgtMe()
            )
        )
    }

    @Test
    fun completeRestoredChildrenAreReused() {
        val home = FgtHome()
        val ticket = FgtTicket()

        val restored = restoredMainTabFragments(home, ticket)

        assertSame(home, restored?.get(0))
        assertSame(ticket, restored?.get(1))
    }
}
