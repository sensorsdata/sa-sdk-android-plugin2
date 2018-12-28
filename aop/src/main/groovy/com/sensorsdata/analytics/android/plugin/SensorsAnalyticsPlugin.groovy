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
        Object extension = project.extensions.create("sensorsAnalytics", SensorsAnalyticsExtension,args)

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