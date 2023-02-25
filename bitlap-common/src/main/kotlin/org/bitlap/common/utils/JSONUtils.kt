/* Copyright (c) 2023 bitlap.org */
package org.bitlap.common.utils

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Desc: json utils
 *
 * Mail: chk19940609@gmail.com
 * Created by IceMimosa
 * Date: 2021/4/12
 */
object JSONUtils {

    private val gson = Gson()

    @JvmStatic
    fun toJson(src: Any): String {
        return gson.toJson(src)
    }

    @JvmStatic
    fun <T> fromJson(json: String, type: Class<T>): T {
        return gson.fromJson(json, type)
    }

    @JvmStatic
    fun fromJsonAsMap(json: String): Map<String, String> {
        return gson.fromJson(json, object : TypeToken<Map<String, String>>() {}.type)
    }
}
