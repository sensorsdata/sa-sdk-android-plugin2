package com.sensorsdata.analytics.android.plugin

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.internal.reflect.Instantiator
import org.gradle.invocation.DefaultGradle

class SensorsAnalyticsPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        Instantiator ins = ((DefaultGradle) project.getGradle()).getServices().get(
                Instantiator)
        def args = [ins] as Object[]
        SensorsAnalyticsExtension extension = project.extensions.create("sensorsAnalytics", SensorsAnalyticsExtension,args)

        boolean disableSensorsAnalyticsPlugin = false
        boolean disableSensorsAnalyticsPluginNew = false
        boolean disableSensorsAnalyticsMultiThread = false
        boolean disableSensorsAnalyticsIncremental = false
        Properties properties = new Properties()
        if (project.rootProject.file('gradle.properties').exists()) {
            properties.load(project.rootProject.file('gradle.properties').newDataInputStream())
            disableSensorsAnalyticsPlugin = Boolean.parseBoolean(properties.getProperty("disableSensorsAnalyticsPlugin", "false"))
            disableSensorsAnalyticsPluginNew = Boolean.parseBoolean(properties.getProperty("sensorsAnalytics.disablePlugin", "false"))
            disableSensorsAnalyticsMultiThread = Boolean.parseBoolean(properties.getProperty("sensorsAnalytics.disableMultiThread", "false"))
            disableSensorsAnalyticsIncremental = Boolean.parseBoolean(properties.getProperty("sensorsAnalytics.disableIncremental", "false"))
        }
        if (!disableSensorsAnalyticsPlugin && !disableSensorsAnalyticsPluginNew) {
            AppExtension appExtension = project.extensions.findByType(AppExtension.class)
            SensorsAnalyticsTransformHelper transformHelper = new SensorsAnalyticsTransformHelper(extension)
            transformHelper.disableSensorsAnalyticsIncremental = disableSensorsAnalyticsIncremental
            transformHelper.disableSensorsAnalyticsMultiThread = disableSensorsAnalyticsMultiThread
            appExtension.registerTransform(new SensorsAnalyticsTransform(transformHelper))

            project.afterEvaluate {
                Logger.setDebug(extension.debug)
            }
        } else {
            Logger.error("------------您已关闭了神策插件--------------")
        }

    }
}