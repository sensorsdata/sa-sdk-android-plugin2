/*
 * Created by wangzhuozhou on 2015/08/12.
 * Copyright 2015－2023 Sensors Data Inc.
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

import com.android.build.gradle.AppExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.sensorsdata.analytics.android.plugin.utils.Logger
import com.sensorsdata.analytics.android.plugin.utils.VersionUtils
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.internal.reflect.Instantiator
import org.gradle.invocation.DefaultGradle

class SensorsAnalyticsPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        Instantiator ins = ((DefaultGradle) project.getGradle()).getServices().get(Instantiator)
        def args = [ins] as Object[]
        SensorsAnalyticsExtension extension = project.extensions.create("sensorsAnalytics", SensorsAnalyticsExtension, args)
        Map<String, ?> properties = project.getProperties()
        boolean disableSensorsAnalyticsPlugin = Boolean.parseBoolean(properties.getOrDefault("sensorsAnalytics.disablePlugin", "false")) ||
                Boolean.parseBoolean(properties.getOrDefault("disableSensorsAnalyticsPlugin", "false"))
        boolean disableSensorsAnalyticsMultiThreadBuild = Boolean.parseBoolean(properties.getOrDefault("sensorsAnalytics.disableMultiThreadBuild", "false"))
        boolean disableSensorsAnalyticsIncrementalBuild = Boolean.parseBoolean(properties.getOrDefault("sensorsAnalytics.disableIncrementalBuild", "false"))
        boolean isHookOnMethodEnter = Boolean.parseBoolean(properties.getOrDefault("sensorsAnalytics.isHookOnMethodEnter", "false"))
        boolean isAndroidTv = Boolean.parseBoolean(properties.getOrDefault("sensorsAnalytics.isAndroidTv", "false"))
        String asmVersion = properties.getOrDefault("sensorsAnalytics.asmVersion", "ASM7")
        SensorsAnalyticsUtil.updateASMVersion(asmVersion)

        if (!disableSensorsAnalyticsPlugin) {
            BaseExtension baseExtension
            if (project.getPlugins().hasPlugin("com.android.application")) {
                baseExtension = project.extensions.findByType(AppExtension.class)
            } else if (project.getPlugins().hasPlugin("com.android.library")) {
                baseExtension = project.extensions.findByType(LibraryExtension.class)
            }
            if (null != baseExtension) {
                SensorsAnalyticsTransformHelper transformHelper = new SensorsAnalyticsTransformHelper(extension, baseExtension)
                transformHelper.disableSensorsAnalyticsIncremental = disableSensorsAnalyticsIncrementalBuild
                transformHelper.disableSensorsAnalyticsMultiThread = disableSensorsAnalyticsMultiThreadBuild
                transformHelper.isHookOnMethodEnter = isHookOnMethodEnter
                VersionUtils.isAndroidTv = isAndroidTv

                baseExtension.registerTransform(new SensorsAnalyticsTransform(transformHelper, baseExtension instanceof LibraryExtension))
            } else {
                Logger.error("------------神策 plugin 当前不支持您的项目--------------")
            }
        } else {
            Logger.error("------------您已关闭了神策插件--------------")
        }
    }
}