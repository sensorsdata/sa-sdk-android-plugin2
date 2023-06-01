package com.sensorsdata.analytics.android.plugin

import com.sensorsdata.analytics.android.plugin.common.HookConstant
import com.sensorsdata.analytics.android.plugin.version.SensorsDataSDKVersionHelper

class ClassNameAnalytics(
    val className: String,
    val superClass: String?,
    val interfaces: List<String>? = null
) {
    val isSensorsDataAPI: Boolean by lazy {
        className == HookConstant.SENSORS_DATA_API
    }

    val isSensorsDataUtils: Boolean by lazy {
        className == HookConstant.SENSORS_DATA_UTILS
    }

    val isOAIDHelper by lazy {
        className == HookConstant.OAID_HELPER
    }

    val isSensorsDataVersion by lazy {
        className.endsWith(SensorsDataSDKVersionHelper.VERSION_KEY_SENSORDATA_VERSION_CONFIG)
    }

    val isSALog by lazy { className == HookConstant.SA_LOG }

    val isAppWebViewInterface by lazy {
        className == HookConstant.APP_JS_INTERFACE || className == HookConstant.VISUAL_JS_INTERFACE
    }

    val isKeyboardViewUtil by lazy {
        className.startsWith(PACKAGE_START) && className.endsWith(KEY_KEYBOARD)
    }

    companion object {
        private const val PACKAGE_START = "com/sensorsdata/analytics"
        private const val KEY_KEYBOARD = "KeyboardViewUtil"
    }
}