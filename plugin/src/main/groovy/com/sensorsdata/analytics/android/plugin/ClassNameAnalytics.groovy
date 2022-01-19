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

class ClassNameAnalytics {

    public String className
    boolean isShouldModify = false
    boolean isSensorsDataAPI = false
    boolean isSensorsDataUtils = false
    boolean isSALog = false
    def methodCells = new ArrayList<SensorsAnalyticsMethodCell>()
    boolean isAppWebViewInterface = false

    ClassNameAnalytics(String className) {
        this.className = className
        isSensorsDataAPI = (className == 'com.sensorsdata.analytics.android.sdk.SensorsDataAPI')
        isSensorsDataUtils = (className == 'com.sensorsdata.analytics.android.sdk.util.SensorsDataUtils')
        isSALog = (className == 'com.sensorsdata.analytics.android.sdk.SALog')
        isAppWebViewInterface = ((className == 'com.sensorsdata.analytics.android.sdk.AppWebViewInterface')
                || (className == 'com.sensorsdata.analytics.android.sdk.visual.WebViewVisualInterface'))
    }

    boolean isSDKFile() {
        return isSALog || isSensorsDataAPI || isSensorsDataUtils || isAppWebViewInterface
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