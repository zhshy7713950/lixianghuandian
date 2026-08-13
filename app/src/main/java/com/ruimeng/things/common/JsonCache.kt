package com.ruimeng.things.common

import android.util.Log
import wongxd.base.aCache.ACache
import wongxd.common.gson

private const val JSON_CACHE_TAG = "JsonCache"

/**
 * Reads structured JSON from an [ACache] entry without allowing a malformed cache value to
 * crash its caller. Invalid non-empty values are removed so the owning feature can repopulate
 * the cache from its authoritative data source.
 */
internal fun <T : Any> ACache.getJsonOrNull(key: String, clazz: Class<T>): T? {
    val rawValue = getAsString(key)
    return decodeCachedJson(key, rawValue, clazz) { invalidKey, error ->
        remove(invalidKey)
        // Cache payloads may contain personal data. Never include the raw value in this log.
        Log.w(
            JSON_CACHE_TAG,
            "Discarded invalid JSON cache: key=$invalidKey, error=${error.javaClass.simpleName}"
        )
    }
}

/**
 * Pure decoding layer kept separate from [ACache] so malformed-value behavior can be covered by
 * local unit tests without an Android cache directory.
 */
internal fun <T : Any> decodeCachedJson(
    key: String,
    rawValue: String?,
    clazz: Class<T>,
    onInvalid: (String, RuntimeException) -> Unit
): T? {
    if (rawValue.isNullOrBlank()) {
        return null
    }

    val decoded = try {
        gson.fromJson(rawValue, clazz)
    } catch (error: RuntimeException) {
        onInvalid(key, error)
        return null
    }

    if (decoded == null) {
        onInvalid(key, IllegalStateException("Decoded cache value was null"))
        return null
    }

    return decoded
}
