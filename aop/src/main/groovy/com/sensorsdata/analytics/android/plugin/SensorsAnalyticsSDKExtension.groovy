/**Created by renqingyou on 2018/12/01.
 * Copyright © 2015－2019 Sensors Data Inc. All rights reserved. */

package com.sensorsdata.analytics.android.plugin

class SensorsAnalyticsSDKExtension {
    // 同SensorsAnalyticsSDKHookConfig中方法对应, disableIMEI,disableLog,disableJsInterface，disableAndroidID
    boolean disableIMEI = false
    boolean disableLog = false
    boolean disableJsInterface = false
    boolean disableAndroidID = false
    boolean disableMacAddress = false
    boolean disableCarrier = false

    @Override
    String toString() {
        return  " disableIMEI=" + disableIMEI + "\n" +
                " disableLog=" + disableLog + "\n" +
                " disableJsInterface=" + disableJsInterface + "\n" +
                " disableAndroidID=" + disableAndroidID + "\n" +
                " disableMacAddress=" + disableMacAddress + "\n" +
                " disableCarrier=" + disableCarrier
    }
}

