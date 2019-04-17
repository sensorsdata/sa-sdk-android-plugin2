/**Created by renqingyou on 2018/11/01.
 * Copyright © 2015－2019 Sensors Data Inc. All rights reserved. */

package com.sensorsdata.analytics.android.plugin

class ClassNameAnalytics {

    public String className

    boolean isShouldModify = false

    boolean isSensorsDataAPI = false

    boolean isSensorsDataUtils = false

    boolean isSALog = false

    def methodCells = new ArrayList<SensorsAnalyticsMethodCell>()

    ClassNameAnalytics (String className) {
        this.className = className
        isSensorsDataAPI = (className == 'com.sensorsdata.analytics.android.sdk.SensorsDataAPI')
        isSensorsDataUtils = (className == 'com.sensorsdata.analytics.android.sdk.util.SensorsDataUtils')
        isSALog = (className == 'com.sensorsdata.analytics.android.sdk.SALog')
    }

    boolean isSDKFile() {
        return isSALog || isSensorsDataAPI || isSensorsDataUtils
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