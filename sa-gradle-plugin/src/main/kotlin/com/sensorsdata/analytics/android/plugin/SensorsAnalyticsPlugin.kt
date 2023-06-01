package com.sensorsdata.analytics.android.plugin

import com.sensorsdata.analytics.android.gradle.loadCompatImpl
import com.sensorsdata.analytics.android.plugin.manager.SAPluginManager
import org.gradle.api.Plugin
import org.gradle.api.Project

class SensorsAnalyticsPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        loadCompatImpl(project, AsmCompatFactoryImpl(SAPluginManager(project)))
    }
}