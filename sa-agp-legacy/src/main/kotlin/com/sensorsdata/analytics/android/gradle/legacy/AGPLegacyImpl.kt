package com.sensorsdata.analytics.android.gradle.legacy

import com.android.build.gradle.AppExtension
import com.sensorsdata.analytics.android.gradle.AGPCompatInterface
import com.sensorsdata.analytics.android.gradle.AGPVersion
import com.sensorsdata.analytics.android.gradle.AsmCompatFactory
import org.gradle.api.Project

@Suppress("DEPRECATION")
class AGPLegacyImpl(
    project: Project,
    override val asmWrapperFactory: AsmCompatFactory
) : AGPCompatInterface {

    init {
        if (project.plugins.hasPlugin("com.android.application")) {
            val android = project.extensions.findByType(AppExtension::class.java)
            android?.registerTransform(AGPLegacyTransform(asmWrapperFactory, android))
        }
    }

    override val agpVersion: AGPVersion
        //not real exists, it represents agp < 7.3.1
        get() = AGPVersion(7, 3, 0)

}