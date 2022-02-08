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

class SensorsAnalyticsSDKExtension {
    // 同SensorsAnalyticsSDKHookConfig中方法对应, disableIMEI,disableLog,disableJsInterface，disableAndroidID
    boolean disableIMEI = false
    boolean disableLog = false
    boolean disableJsInterface = false
    boolean disableAndroidID = false
    boolean disableMacAddress = false
    boolean disableCarrier = false
    boolean disableOAID = false

    @Override
    String toString() {
        return "\t\tdisableIMEI=" + disableIMEI + "\n" +
                "\t\tdisableLog=" + disableLog + "\n" +
                "\t\tdisableJsInterface=" + disableJsInterface + "\n" +
                "\t\tdisableAndroidID=" + disableAndroidID + "\n" +
                "\t\tdisableMacAddress=" + disableMacAddress + "\n" +
                "\t\tdisableCarrier=" + disableCarrier + "\n" +
                "\t\tdisableOAID=" + disableOAID
    }
}

