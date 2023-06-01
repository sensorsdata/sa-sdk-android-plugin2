package com.sensorsdata.analytics.android.plugin

import com.sensorsdata.analytics.android.gradle.AsmCompatFactory
import com.sensorsdata.analytics.android.gradle.ClassInfo
import com.sensorsdata.analytics.android.gradle.ClassInheritance
import com.sensorsdata.analytics.android.plugin.manager.SAPluginManager
import com.sensorsdata.analytics.android.plugin.utils.Logger
import com.sensorsdata.analytics.android.plugin.visitor.SAPrimaryClassVisitor
import org.objectweb.asm.ClassVisitor

class AsmCompatFactoryImpl(private val pluginManager: SAPluginManager) : AsmCompatFactory() {

    private var isInit = false

    override fun transform(
        classVisitor: ClassVisitor,
        classInheritance: ClassInheritance
    ): ClassVisitor {
        return SAPrimaryClassVisitor(classVisitor, pluginManager, classInheritance)
    }

    override fun isInstrumentable(classInfo: ClassInfo): Boolean {
        return pluginManager.packageManager.isInstrument(
            classInfo.qualifiedName,
            pluginManager.extension
        )
    }

    override val isIncremental: Boolean
        get() = true

    override val name: String
        get() = "sensorsAnalyticsAutoTrack"

    @Synchronized
    override fun onBeforeTransform() {
        if (!isInit) {
            isInit = true
            printPluginInfo()
        }
    }

    override var asmAPI: Int = pluginManager.getASMVersion()
    override val extension: Any
        get() = pluginManager.extension

    private fun printPluginInfo() {
        Logger.printCopyright()
        Logger.info("plugin config detail:\nsensorsAnalytics {\n ${pluginManager.extension} \n}")
        Logger.info("是否在方法进入时插入代码: ${pluginManager.isHookOnMethodEnter}")
        Logger.info("是否添加 TV 支持: ${pluginManager.isAndroidTV}")
        Logger.info("ASM 版本为: ${pluginManager.getASMVersionStr()}")
        Logger.printNoLimit("");
    }
}