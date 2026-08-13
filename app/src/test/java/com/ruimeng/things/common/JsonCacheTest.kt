package com.ruimeng.things.common

import com.ruimeng.things.bean.NoReadBean
import com.ruimeng.things.bean.UserInfoBean
import com.ruimeng.things.net_station.net_city_data.NetCityJsonBean
import com.ruimeng.things.shop.bean.TkConfigBean
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import wongxd.common.gson

class JsonCacheTest {

    data class CacheValue(var value: String = "")

    @Test
    fun validObject_returnsDecodedValueWithoutInvalidation() {
        var invalidationCount = 0

        val result = decodeCachedJson(
            key = "cache-key",
            rawValue = "{\"value\":\"ok\"}",
            clazz = CacheValue::class.java
        ) { _, _ -> invalidationCount++ }

        assertNotNull(result)
        assertEquals("ok", result?.value)
        assertEquals(0, invalidationCount)
    }

    @Test
    fun emptyObject_isAValidObject() {
        var invalidationCount = 0

        val result = decodeCachedJson(
            key = "cache-key",
            rawValue = "{}",
            clazz = CacheValue::class.java
        ) { _, _ -> invalidationCount++ }

        assertNotNull(result)
        assertEquals("", result?.value)
        assertEquals(0, invalidationCount)
    }

    @Test
    fun missingOrBlankValue_returnsNullWithoutInvalidation() {
        var invalidationCount = 0
        val onInvalid: (String, RuntimeException) -> Unit = { _, _ -> invalidationCount++ }

        assertNull(decodeCachedJson("cache-key", null, CacheValue::class.java, onInvalid))
        assertNull(decodeCachedJson("cache-key", "", CacheValue::class.java, onInvalid))
        assertNull(decodeCachedJson("cache-key", "   ", CacheValue::class.java, onInvalid))
        assertEquals(0, invalidationCount)
    }

    @Test
    fun invalidStructuredValues_returnNullAndInvokeInvalidation() {
        val invalidValues = listOf(
            "\"invalid\"",
            "\"{\\\"value\\\":\\\"ok\\\"}\"",
            "{\"value\":",
            "[]",
            "null"
        )
        val invalidatedKeys = mutableListOf<String>()

        invalidValues.forEach { rawValue ->
            val result = decodeCachedJson(
                key = "cache-key",
                rawValue = rawValue,
                clazz = CacheValue::class.java
            ) { key, _ -> invalidatedKeys.add(key) }

            assertNull(result)
        }

        assertEquals(invalidValues.size, invalidatedKeys.size)
        assertEquals(List(invalidValues.size) { "cache-key" }, invalidatedKeys)
    }

    @Test
    fun validBusinessCacheValues_remainReadable() {
        val userInfo = UserInfoBean.Data.UserInfo(id = "user-1")
        val noRead = NoReadBean.Data(ver = 7)
        val tkConfig = TkConfigBean.Data(token = "tk-token")
        val cityData = NetCityJsonBean(
            data = listOf(NetCityJsonBean.Data(id = "510000", name = "四川省"))
        )
        var invalidationCount = 0
        val onInvalid: (String, RuntimeException) -> Unit = { _, _ -> invalidationCount++ }

        val decodedUser = decodeCachedJson(
            "user",
            gson.toJson(userInfo),
            UserInfoBean.Data.UserInfo::class.java,
            onInvalid
        )
        val decodedNoRead = decodeCachedJson(
            "no-read",
            gson.toJson(noRead),
            NoReadBean.Data::class.java,
            onInvalid
        )
        val decodedTkConfig = decodeCachedJson(
            "tk-config",
            gson.toJson(tkConfig),
            TkConfigBean.Data::class.java,
            onInvalid
        )
        val decodedCityData = decodeCachedJson(
            "city-data",
            gson.toJson(cityData),
            NetCityJsonBean::class.java,
            onInvalid
        )

        assertEquals("user-1", decodedUser?.id)
        assertEquals(7, decodedNoRead?.ver)
        assertEquals("tk-token", decodedTkConfig?.token)
        assertEquals("四川省", decodedCityData?.data?.single()?.name)
        assertEquals(0, invalidationCount)
    }
}
