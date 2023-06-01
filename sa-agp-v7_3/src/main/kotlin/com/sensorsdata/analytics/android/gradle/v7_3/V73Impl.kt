package com.sensorsdata.analytics.android.gradle.v7_3

import com.android.build.api.instrumentation.FramesComputationMode
import com.android.build.api.instrumentation.InstrumentationScope
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.Variant
import com.sensorsdata.analytics.android.gradle.AGPCompatInterface
import com.sensorsdata.analytics.android.gradle.AGPVersion
import com.sensorsdata.analytics.android.gradle.AsmCompatFactory
import org.gradle.api.Project
import kotlin.reflect.full.declaredMemberProperties

class V73Impl(project: Project, override val asmWrapperFactory: AsmCompatFactory) :
    AGPCompatInterface {
    override val agpVersion: AGPVersion
        get() = AGPVersion(7, 3, 1)

    init {
        val androidComponents = project.extensions.getByType(AndroidComponentsExtension::class.java)
        V73AGPContextImpl.asmCompatFactory = asmWrapperFactory
        androidComponents.onVariants { variant: Variant ->
            variant.instrumentation.transformClassesWith(
                SensorsDataAsmClassVisitorFactory::class.java,
                InstrumentationScope.ALL
            ) {

                var extension = asmWrapperFactory.extension
                val memberProperties = extension::class.declaredMemberProperties
                memberProperties.forEach { property ->
                    val result = property.getter.call(extension)
                    when (property.name) {
                        "useInclude" -> it.useInclude.set(result as Boolean)
                        "lambdaEnabled" -> it.lambdaEnabled.set(result as Boolean)
                        "addUCJavaScriptInterface" -> it.addUCJavaScriptInterface.set(result as Boolean)
                        "addXWalkJavaScriptInterface" -> it.addXWalkJavaScriptInterface.set(result as Boolean)
                        "lambdaParamOptimize" -> it.lambdaParamOptimize.set(result as Boolean)
                        "disableTrackKeyboard" -> it.disableTrackKeyboard.set(result as Boolean)
                        "exclude" -> it.exclude.set(result as MutableList<String>)
                        "include" -> it.include.set(result as MutableList<String>)
                        "disableModules" -> it.disableModules.set(result as MutableList<String>)
                    }

                    if (property.name == "sdk") {
                        val sdkResult = property.getter.call(extension)
                        val sdkProperties = sdkResult!!::class.declaredMemberProperties
                        sdkProperties.forEach { sdkProperty ->
                            val sdkPropertyValue = sdkProperty.getter.call(sdkResult)
                            when (sdkProperty.name) {
                                "disableIMEI" -> it.disableIMEI.set(sdkPropertyValue as Boolean)
                                "disableLog" -> it.disableLog.set(sdkPropertyValue as Boolean)
                                "disableJsInterface" -> it.disableJsInterface.set(sdkPropertyValue as Boolean)
                                "disableAndroidID" -> it.disableAndroidID.set(sdkPropertyValue as Boolean)
                                "disableMacAddress" -> it.disableMacAddress.set(sdkPropertyValue as Boolean)
                                "disableCarrier" -> it.disableCarrier.set(sdkPropertyValue as Boolean)
                                "disableOAID" -> it.disableOAID.set(sdkPropertyValue as Boolean)
                            }
                        }
                    }
                }
            }
            variant.instrumentation
                .setAsmFramesComputationMode(FramesComputationMode.COPY_FRAMES)
        }
    }
}

