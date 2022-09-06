/*
 * Created by wangzhuozhou on 2015/08/12.
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

import com.sensorsdata.analytics.android.plugin.configs.SensorsAnalyticsSDKExtension
import org.gradle.api.Action
import org.gradle.internal.reflect.Instantiator


class SensorsAnalyticsExtension {
    public boolean debug = false
    public boolean disableJar = false
    public boolean useInclude = false
    public boolean lambdaEnabled = true
    public boolean autoHandleWebView = true
    public boolean addUCJavaScriptInterface = false
    public boolean addXWalkJavaScriptInterface = false
    public boolean lambdaParamOptimize = false
    public boolean disableTrackPush = false
    public boolean disableCheckSDK = false
    public boolean disableTrackKeyboard = true
    public ArrayList<String> exclude = []
    public ArrayList<String> include = []

    public SensorsAnalyticsSDKExtension sdk

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
        return "\tdebug=" + debug + "\n" +
                "\tdisableJar=" + disableJar + "\n" +
                "\tuseInclude=" + useInclude + "\n" +
                "\tdisableTrackPush=" + disableTrackPush + "\n" +
                "\tautoHandleWebView=" + autoHandleWebView + "\n" +
                "\taddUCJavaScriptInterface=" + addUCJavaScriptInterface + "\n" +
                "\taddXWalkJavaScriptInterface=" + addXWalkJavaScriptInterface + "\n" +
                "\tlambdaParamOptimize=" + lambdaParamOptimize + "\n" +
                "\tlambdaEnabled=" + lambdaEnabled + "\n" +
                "\tdisableCheckSDK=" + disableCheckSDK + "\n" +
                "\tdisableHookKeyboard=" + disableTrackKeyboard + "\n" +
                "\texclude=[" + excludeBuilder.toString() + "]" + "\n" +
                "\tinclude=[" + includeBuilder.toString() + "]" + "\n" +
                "\tsdk {\n" + sdk + "\n" +
                "\t}"
    }
}

