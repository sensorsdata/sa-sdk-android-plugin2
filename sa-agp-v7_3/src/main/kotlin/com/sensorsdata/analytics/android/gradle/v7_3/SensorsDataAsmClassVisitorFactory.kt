package com.sensorsdata.analytics.android.gradle.v7_3

import com.android.build.api.instrumentation.AsmClassVisitorFactory
import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import com.android.build.api.instrumentation.InstrumentationParameters
import com.sensorsdata.analytics.android.gradle.ClassInfo
import com.sensorsdata.analytics.android.gradle.ClassInheritance
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.objectweb.asm.ClassVisitor

@Suppress("UnstableApiUsage")
abstract class SensorsDataAsmClassVisitorFactory :
    AsmClassVisitorFactory<ConfigInstrumentParams> {

    override fun createClassVisitor(
        classContext: ClassContext,
        nextClassVisitor: ClassVisitor
    ): ClassVisitor {
        V73AGPContextImpl.asmCompatFactory!!.onBeforeTransform()
        val classInheritance = object : ClassInheritance {
            override fun isAssignableFrom(subClass: String, superClass: String): Boolean {
                return classContext.loadClassData(subClass)?.let {
                    it.className == superClass || it.superClasses.contains(superClass) || it.interfaces.contains(superClass)
                } ?: false
            }

            override fun loadClass(className: String): ClassInfo? {
                return classContext.loadClassData(className)?.let {
                    ClassInfo(
                        it.className,
                        interfaces = it.interfaces,
                        superClasses = it.superClasses
                    )
                }
            }
        }

        return V73AGPContextImpl.asmCompatFactory!!.transform(
            nextClassVisitor, classInheritance
        )
    }

    override fun isInstrumentable(classData: ClassData): Boolean {
        return V73AGPContextImpl.asmCompatFactory!!.isInstrumentable(
            ClassInfo(
                classData.className,
                interfaces = classData.interfaces,
                superClasses = classData.superClasses
            )
        )
    }
}

interface ConfigInstrumentParams:InstrumentationParameters{
    @get:Input
    val useInclude: Property<Boolean>
    @get:Input
    val lambdaEnabled: Property<Boolean>
    @get:Input
    val addUCJavaScriptInterface: Property<Boolean>
    @get:Input
    val addXWalkJavaScriptInterface: Property<Boolean>
    @get:Input
    val lambdaParamOptimize: Property<Boolean>
    @get:Input
    val disableTrackKeyboard: Property<Boolean>
    @get:Input
    val exclude:ListProperty<String>
    @get:Input
    val include:ListProperty<String>
    @get:Input
    val disableModules:ListProperty<String>

    //SDK
    @get:Input
    val disableIMEI: Property<Boolean>
    @get:Input
    val disableLog: Property<Boolean>
    @get:Input
    val disableJsInterface: Property<Boolean>
    @get:Input
    val disableAndroidID: Property<Boolean>
    @get:Input
    val disableMacAddress: Property<Boolean>
    @get:Input
    val disableCarrier: Property<Boolean>
    @get:Input
    val disableOAID: Property<Boolean>
}