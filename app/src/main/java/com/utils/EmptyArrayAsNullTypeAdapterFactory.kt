package com.utils

import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter

/**
 * 解决服务端字段类型不稳定导致 Gson 解析失败的问题：
 *
 * - 同一个字段，有数据时返回对象：{"k":"v"}
 * - 无数据时却返回空数组：[]
 *
 * 对于声明为对象类型（例如 `SomeObj?`）的字段，Gson 遇到 `[]` 会抛出类型不匹配异常。
 * 该适配器会将“空数组”视为“无数据”，直接解析成 `null`，从而兼容这种不规范返回。
 *
 * 使用方式（按字段启用，最小影响范围）：
 *
 * ```kotlin
 * @JsonAdapter(EmptyArrayAsNullTypeAdapterFactory::class)
 * val someField: SomeObj? = null
 * ```
 *
 * 注意：
 * - 仅对“对象类型字段”生效；对 `List/Map/数组/基础类型` 不做处理。
 * - 只有当服务端返回 `[]` 时才会转换为 `null`；正常对象返回仍走原始解析逻辑。
 */
class EmptyArrayAsNullTypeAdapterFactory : TypeAdapterFactory {
    override fun <T> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T>? {
        val raw = type.rawType
        if (raw.isPrimitive ||
            raw == String::class.java ||
            Number::class.java.isAssignableFrom(raw) ||
            raw == java.lang.Boolean::class.java ||
            raw.isArray ||
            java.util.Collection::class.java.isAssignableFrom(raw) ||
            java.util.Map::class.java.isAssignableFrom(raw)
        ) {
            return null
        }

        val delegate = gson.getDelegateAdapter(this, type)
        return object : TypeAdapter<T>() {
            override fun write(out: JsonWriter, value: T?) {
                delegate.write(out, value)
            }

            override fun read(reader: JsonReader): T? {
                return when (reader.peek()) {
                    JsonToken.BEGIN_ARRAY -> {
                        reader.beginArray()
                        while (reader.hasNext()) {
                            reader.skipValue()
                        }
                        reader.endArray()
                        null
                    }

                    else -> delegate.read(reader)
                }
            }
        }
    }
}

