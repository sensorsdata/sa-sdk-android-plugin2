package com.sensorsdata.analytics.android.plugin

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class SensorsAnalyticsPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        Object extension = project.extensions.create("sensorsAnalytics", SensorsAnalyticsExtension,project.objects)

        boolean disableSensorsAnalyticsPlugin = false
        Properties properties = new Properties()
        if (project.rootProject.file('gradle.properties').exists()) {
            properties.load(project.rootProject.file('gradle.properties').newDataInputStream())
            disableSensorsAnalyticsPlugin = Boolean.parseBoolean(properties.getProperty("disableSensorsAnalyticsPlugin", "false"))
        }
        if (!disableSensorsAnalyticsPlugin) {
            AppExtension appExtension = project.extensions.findByType(AppExtension.class)
            SensorsAnalyticsTransformHelper transformHelper = new SensorsAnalyticsTransformHelper(extension)
            appExtension.registerTransform(new SensorsAnalyticsTransform(transformHelper))

            project.afterEvaluate {
                Logger.setDebug(extension.debug)
            }
        } else {
            Logger.error("------------您已关闭了神策插件--------------")
        }

    }
}