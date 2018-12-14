package com.sensorsdata.analytics.android.plugin

import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory


class SensorsAnalyticsExtension {
    boolean debug = false
    boolean disableJar = false
    boolean useInclude = false
    boolean lambdaEnabled = true

    HashSet<String> exclude = []
    HashSet<String> include = []

    SensorsAnalyticsSDKExtension sdk

    @javax.inject.Inject
    SensorsAnalyticsExtension(ObjectFactory objectFactory) {
        sdk = objectFactory.newInstance(SensorsAnalyticsSDKExtension)
    }

    void sdk(Action<? super SensorsAnalyticsSDKExtension> action) {
        action.execute(sdk)
    }
}

