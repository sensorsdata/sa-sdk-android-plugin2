package com.sensorsdata.analytics.android.gradle

import com.sensorsdata.analytics.android.gradle.legacy.AGPLegacyImpl
import com.sensorsdata.analytics.android.gradle.v7_3.V73Impl
import org.gradle.api.Project

fun loadCompatImpl(project: Project, asmWrapperFactory: AsmCompatFactory): AGPCompatInterface {
    val version = AGPVersion.CURRENT_AGP_VERSION
    return if (version >= AGPVersion.AGP_7_3_1) {
        V73Impl(project, asmWrapperFactory)
    } else {
        AGPLegacyImpl(project, asmWrapperFactory)
    }
}