package com.ruimeng.things.net_station

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class LocationUtilTest {

    @Test
    fun singleShotCompletion_ignoresLateCallbacks() {
        val cleanupCount = AtomicInteger()
        val callbackCount = AtomicInteger()
        val completion = SingleShotCompletion { cleanupCount.incrementAndGet() }

        assertTrue(completion.tryComplete { callbackCount.incrementAndGet() })
        assertFalse(completion.tryComplete { callbackCount.incrementAndGet() })
        completion.cancel()

        assertEquals(1, callbackCount.get())
        assertEquals(1, cleanupCount.get())
        assertTrue(completion.isCompleted)
    }

    @Test
    fun singleShotCompletion_cancellationCleansUpAndIgnoresCallback() {
        val cleanupCount = AtomicInteger()
        val callbackCount = AtomicInteger()
        val completion = SingleShotCompletion { cleanupCount.incrementAndGet() }

        completion.cancel()
        val callbackAccepted = completion.tryComplete { callbackCount.incrementAndGet() }

        assertFalse(callbackAccepted)
        assertEquals(0, callbackCount.get())
        assertEquals(1, cleanupCount.get())
    }

    @Test
    fun singleShotCompletion_allowsOnlyOneConcurrentCallback() {
        val workerCount = 32
        val executor = Executors.newFixedThreadPool(workerCount)
        val ready = CountDownLatch(workerCount)
        val start = CountDownLatch(1)
        val finished = CountDownLatch(workerCount)
        val cleanupCount = AtomicInteger()
        val callbackCount = AtomicInteger()
        val successfulCompletions = AtomicInteger()
        val completion = SingleShotCompletion { cleanupCount.incrementAndGet() }

        repeat(workerCount) {
            executor.execute {
                ready.countDown()
                start.await()
                if (completion.tryComplete { callbackCount.incrementAndGet() }) {
                    successfulCompletions.incrementAndGet()
                }
                finished.countDown()
            }
        }

        assertTrue(ready.await(5, TimeUnit.SECONDS))
        start.countDown()
        assertTrue(finished.await(5, TimeUnit.SECONDS))
        executor.shutdownNow()

        assertEquals(1, successfulCompletions.get())
        assertEquals(1, callbackCount.get())
        assertEquals(1, cleanupCount.get())
    }

    @Test
    fun locationMemoryCache_usesZeroUntilLiveLocationExists() {
        val cache = LocationMemoryCache()

        val result = cache.fallback(LocationFailure.TIMEOUT)

        assertEquals(Coordinates(0.0, 0.0), result.coordinates)
        assertEquals(LocationSource.DEFAULT_ZERO, result.source)
        assertEquals(LocationFailure.TIMEOUT, result.failure)
    }

    @Test
    fun locationMemoryCache_reusesLastLiveLocationAfterFailure() {
        val cache = LocationMemoryCache()
        val coordinates = Coordinates(30.5728, 104.0668)

        val liveResult = cache.live(coordinates)
        val fallbackResult = cache.fallback(LocationFailure.PROVIDER_DISABLED)

        assertEquals(coordinates, liveResult.coordinates)
        assertEquals(LocationSource.LIVE, liveResult.source)
        assertNull(liveResult.failure)
        assertEquals(coordinates, fallbackResult.coordinates)
        assertEquals(LocationSource.MEMORY_CACHE, fallbackResult.source)
        assertEquals(LocationFailure.PROVIDER_DISABLED, fallbackResult.failure)
    }
}
