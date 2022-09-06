/*
 * Created by renqingyou on 2018/12/01.
 * Copyright 2015－2022 Sensors Data Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sensorsdata.analytics.android.plugin

import com.sensorsdata.analytics.android.plugin.core.HookConstant
import com.sensorsdata.analytics.android.plugin.version.SensorsDataSDKVersionHelper

class ClassNameAnalytics {
    private static final String PACKAGE_START = "com.sensorsdata.analytics"
    private static final String KEY_KEYBOARD = "KeyboardViewUtil"
    public String className
    boolean isShouldModify = false
    public boolean isSensorsDataAPI = false
    public boolean isSensorsDataUtils = false
    public boolean isOAIDHelper = false
    public boolean isSensorsDataVersion = false
    public boolean isSALog = false
    public boolean isKeyboardViewUtil = false
    public boolean isAppWebViewInterface = false

    ClassNameAnalytics(String className) {
        this.className = className
        isSensorsDataAPI = (className == HookConstant.SENSORS_DATA_API)
        isSensorsDataUtils = (className == HookConstant.SENSORS_DATA_UTILS)
        isOAIDHelper = (className == HookConstant.OAID_HELPER)
        isSALog = (className == HookConstant.SA_LOG)
        isAppWebViewInterface = ((className == HookConstant.APP_JS_INTERFACE)
                || (className == HookConstant.VISUAL_JS_INTERFACE))
        isKeyboardViewUtil = (className.startsWith(PACKAGE_START) && className.endsWith(KEY_KEYBOARD))
        isSensorsDataVersion = className.endsWith(SensorsDataSDKVersionHelper.VERSION_KEY_SENSORDATA_VERSION_CONFIG)
    }

    boolean isSDKFile() {
        return isSALog || isSensorsDataAPI || isSensorsDataUtils || isAppWebViewInterface || isOAIDHelper || isKeyboardViewUtil || isSensorsDataVersion
    }

    boolean isLeanback() {
        return className.startsWith("android.support.v17.leanback") || className.startsWith("androidx.leanback")
    }

    boolean isAndroidGenerated() {
        return className.contains('R$') ||
                className.contains('R2$') ||
                className.contains('R.class') ||
                className.contains('R2.class') ||
                className.contains('BuildConfig.class')
    }

}