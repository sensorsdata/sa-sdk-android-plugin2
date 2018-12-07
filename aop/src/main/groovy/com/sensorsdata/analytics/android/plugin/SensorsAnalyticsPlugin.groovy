package com.sensorsdata.analytics.android.plugin

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class SensorsAnalyticsPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.extensions.create("sensorsAnalytics", SensorsAnalyticsExtension)

        boolean disableSensorsAnalyticsPlugin = false
        Properties properties = new Properties()
        if (project.rootProject.file('gradle.properties').exists()) {
            properties.load(project.rootProject.file('gradle.properties').newDataInputStream())
            disableSensorsAnalyticsPlugin = Boolean.parseBoolean(properties.getProperty("disableSensorsAnalyticsPlugin", "false"))
        }

        if (!disableSensorsAnalyticsPlugin) {
            AppExtension appExtension = project.extensions.findByType(AppExtension.class)
            appExtension.registerTransform(new SensorsAnalyticsTransform(project))

            project.afterEvaluate {
                Logger.setDebug(project.sensorsAnalytics.debug)
            }
        } else {
            println "[SensorsAnalytics]: ------------您已关闭了神策插件--------------"
        }
    }
}