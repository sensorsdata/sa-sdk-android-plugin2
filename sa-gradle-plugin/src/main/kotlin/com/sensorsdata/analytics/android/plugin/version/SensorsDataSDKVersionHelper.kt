package com.sensorsdata.analytics.android.plugin.version

import com.google.gson.JsonParser
import com.sensorsdata.analytics.android.plugin.common.VersionConstant
import com.sensorsdata.analytics.android.plugin.utils.TextUtil.isEmpty

class SensorsDataSDKVersionHelper {
    private val mDependentVersionMap: MutableMap<String, ArrayList<SensorsDataSDKVersionBean>> =
        HashMap()

    private val mCurrentVersionMap: MutableMap<String, String> = HashMap()

    init {
        mCurrentVersionMap[VERSION_KEY_PLUGIN_SDK_PATH] = VersionConstant.VERSION
    }

    /**
     * 获取版本兼容提示信息（在 SDK 获取依赖版本信息时调用）
     *
     * @param classname      当前 SDK 版本信息路径
     * @param relatedVersion 当前 SDK 依赖 SDK 的版本信息
     * @return 提示 message，如果为 "" 表示版本兼容。
     */
    fun getMessageBySDKRelyVersion(classname: String, relatedVersion: String?): String {
        if (!isEmpty(classname) && !isEmpty(relatedVersion)) {
            val jsonElement = JsonParser.parseString(relatedVersion)
            if (null != jsonElement) {
                if (jsonElement.isJsonArray) {
                    val jsonArray = jsonElement.asJsonArray
                    if (null != jsonArray && jsonArray.size() > 0) {
                        val sensorSDKBeanList = ArrayList<SensorsDataSDKVersionBean>()
                        for (i in 0 until jsonArray.size()) {
                            val jsonElementSon = jsonArray[i]
                            if (jsonElementSon.isJsonObject) {
                                val jsonObjectSon = jsonElementSon.asJsonObject
                                val sensorSDKBean =
                                    SensorsDataSDKVersionBean.createSensorDataSDKBean(jsonObjectSon)
                                if (null != sensorSDKBean) {
                                    sensorSDKBeanList.add(sensorSDKBean)
                                }
                            }
                        }
                        if (sensorSDKBeanList.size > 0) {
                            mDependentVersionMap[classname] = sensorSDKBeanList
                        }
                    }
                }
            }
        }
        return if (mCurrentVersionMap.isNotEmpty()) {
            //对比版本了
            checkSensorsSDKVersionOnAnalytic()
        } else ""
    }

    /**
     * 获取版本兼容提示信息（在 SDK 获取版本时调用 ）
     *
     * @param classname      当前 SDK 版本的信息的类路径，每一个 SDK 唯一，用于匹配
     * @param currentVersion 当前的版本
     * @return 提示 message，如果为 "" 表示版本兼容。
     */
    fun getMessageBySDKCurrentVersion(classname: String, currentVersion: String): String {
        mCurrentVersionMap[classname] = currentVersion
        return if (mDependentVersionMap.isNotEmpty()) checkSensorsSDKVersionOnAnalytic() else ""
    }

    /**
     * 真正检测 SDK 版本匹配
     *
     * @return 提示 message，如果为 "" 表示版本兼容。
     */
    private fun checkSensorsSDKVersionOnAnalytic(): String {
        for (beanList in mDependentVersionMap.values) {
            if (beanList.size > 0) {
                val itList = beanList.iterator()
                while (itList.hasNext()) {
                    val sensorSDKBean = itList.next()
                    if (mCurrentVersionMap.containsKey(sensorSDKBean.sensorsDataSDKPath)) {
                        val message = sensorSDKBean.getSensorsDataSDKVersionMessage(
                            mCurrentVersionMap[sensorSDKBean.sensorsDataSDKPath]!!
                        )
                        itList.remove()
                        if (!isEmpty(message)) {
                            return message
                        }
                    }
                }
            }
        }
        return ""
    }

    companion object {
        const val VERSION_KEY_SENSORDATA_VERSION_CONFIG = "SensorsDataVersionConfig"
        const val VERSION_KEY_CURRENT_VERSION = "SDK_VERSION"
        const val VERSION_KEY_DEPENDENT_SDK_VERSION = "DEPENDENT_SDK_VERSIONS"
        const val VERSION_KEY_PLUGIN_SDK_PATH = "com/sensorsdata/analytics/android/plugin"
        const val DEFAULT_MESSAGE = "当前神策 Android SDK 版本 %s 过低，请升级至 %s 及其以上版本后进行使用"
    }
}