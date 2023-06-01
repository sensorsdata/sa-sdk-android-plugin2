package com.sensorsdata.analytics.android.gradle.legacy

import com.sensorsdata.analytics.android.gradle.AGPContext
import com.sensorsdata.analytics.android.gradle.AsmCompatFactory

object AGPLegacyContextImpl : AGPContext {
    override var asmCompatFactory: AsmCompatFactory? = null
}