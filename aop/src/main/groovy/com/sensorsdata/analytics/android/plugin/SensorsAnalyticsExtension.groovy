package com.sensorsdata.analytics.android.plugin

import org.gradle.api.Action
import org.gradle.internal.reflect.Instantiator


class SensorsAnalyticsExtension {
    boolean debug = false
    boolean disableJar = false
    boolean useInclude = false
    boolean lambdaEnabled = true

    HashSet<String> exclude = []
    HashSet<String> include = []

    SensorsAnalyticsSDKExtension sdk

    SensorsAnalyticsExtension(Instantiator ins) {
        sdk = ins.newInstance(SensorsAnalyticsSDKExtension)
    }

    void sdk(Action<? super SensorsAnalyticsSDKExtension> action) {
        action.execute(sdk)
    }
}

