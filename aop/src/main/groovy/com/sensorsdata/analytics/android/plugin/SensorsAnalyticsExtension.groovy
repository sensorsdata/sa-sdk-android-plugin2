/**Created by wangzhuozhou on 2015/08/01.
 * Copyright © 2015－2019 Sensors Data Inc. All rights reserved. */

package com.sensorsdata.analytics.android.plugin

import org.gradle.api.Action
import org.gradle.internal.reflect.Instantiator


class SensorsAnalyticsExtension {
    boolean debug = false
    boolean disableJar = false
    boolean useInclude = false
    boolean lambdaEnabled = true

    ArrayList<String> exclude = []
    ArrayList<String> include = []

    SensorsAnalyticsSDKExtension sdk

    SensorsAnalyticsExtension(Instantiator ins) {
        sdk = ins.newInstance(SensorsAnalyticsSDKExtension)
    }

    void sdk(Action<? super SensorsAnalyticsSDKExtension> action) {
        action.execute(sdk)
    }

    @Override
    String toString() {
        StringBuilder excludeBuilder = new StringBuilder()
        int length = exclude.size()
        for (int i = 0; i < length; i++) {
            excludeBuilder.append("'").append(exclude.get(i)).append("'")
            if (i != length - 1) {
                excludeBuilder.append(",")
            }
        }

        StringBuilder includeBuilder = new StringBuilder()
        length = include.size()
        for (int i = 0; i < length; i++) {
            includeBuilder.append("'").append(include.get(i)).append("'")
            if (i != length - 1) {
                includeBuilder.append(",")
            }
        }
        return  " debug=" + debug + "\n" +
                " disableJar=" + disableJar + "\n" +
                " useInclude=" + useInclude + "\n" +
                " lambdaEnabled=" + lambdaEnabled + "\n" +
                " exclude=[" + excludeBuilder.toString() +"]"+ "\n" +
                " include=[" + includeBuilder.toString() +"]"+ "\n" +
                " sdk {\n" + sdk + "\n"+
                "}"
    }
}

