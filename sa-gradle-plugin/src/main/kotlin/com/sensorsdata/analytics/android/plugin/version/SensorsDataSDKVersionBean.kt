package com.sensorsdata.analytics.android.plugin.version

import com.google.gson.JsonObject
import com.sensorsdata.analytics.android.plugin.utils.TextUtil.isEmpty

class SensorsDataSDKVersionBean(
    /**
     * 其他业务 SDK 的版本信息路径，也就是 SensorDataVersionOptions 类的路径
     * 通过 SensorDataVersionOptions 类的路径匹配，当前依赖 SDK 的版本和已有的 SDK 的关系（每一个 SDK 唯一，用于匹配）
     * （必须）
     */
    val sensorsDataSDKPath: String,
    /**
     * 当前 SDK 要求的最小版本（必须）
     */
    private val mMinSensorsDataSDKVersion: String,
    /**
     * 版本不匹配时，提醒信息，可为 null
     */
    private val mMessage: String
) {

    fun getSensorsDataSDKVersionMessage(version: String): String {
        return if (!isVersionValid(version, mMinSensorsDataSDKVersion)) String.format(
            if (isEmpty(mMessage)) SensorsDataSDKVersionHelper.DEFAULT_MESSAGE else mMessage,
            version,
            mMinSensorsDataSDKVersion
        ) else ""
    }

    private fun isVersionValid(saVersion: String, requiredVersion: String): Boolean {
        return try {
            if (saVersion == requiredVersion) {
                true
            } else {
                val saVersions = saVersion.split(".").toTypedArray()
                val requiredVersions = requiredVersion.split(".").toTypedArray()
                for (index in requiredVersions.indices) {
                    val saVersionsNum = saVersions[index].toInt()
                    val requiredVersionsNum = requiredVersions[index].toInt()
                    if (saVersionsNum != requiredVersionsNum) {
                        return saVersionsNum > requiredVersionsNum
                    }
                }
                false
            }
        } catch (ex: Exception) {
            // ignore
            false
        }
    }

    override fun toString(): String {
        return """	minSensorsDataSDKVersion=$mMinSensorsDataSDKVersion
	sensorsDataSDKPath=${sensorsDataSDKPath}
	message=$mMessage"""
    }

    companion object {
        fun createSensorDataSDKBean(jsonObject: JsonObject?): SensorsDataSDKVersionBean? {
            if (null != jsonObject) {
                try {
                    var path = jsonObject["SDK_VERSION_PATH"].asString
                    val minVersion = jsonObject["DEPENDENT_MIN_SDK_VERSIONS"].asString
                    if (!isEmpty(path) && !isEmpty(minVersion)) {
                        if (path.contains(".")) {
                            path = path.replace("\\.".toRegex(), "/")
                        }
                        val message = jsonObject["ERROR_MESSAGE"].asString
                        return SensorsDataSDKVersionBean(path, minVersion, message)
                    }
                } catch (e: Exception) {
                    // ignore
                }
            }
            return null
        }
    }
}