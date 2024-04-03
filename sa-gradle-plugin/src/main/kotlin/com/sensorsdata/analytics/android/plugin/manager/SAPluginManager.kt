package com.sensorsdata.analytics.android.plugin.manager

import com.sensorsdata.analytics.android.gradle.AGPVersion
import com.sensorsdata.analytics.android.plugin.configs.SAConfigHookHelper
import com.sensorsdata.analytics.android.plugin.extension.SAExtension
import com.sensorsdata.analytics.android.plugin.utils.Logger
import com.sensorsdata.analytics.android.plugin.version.SensorsDataSDKVersionHelper
import org.gradle.api.Project
import org.objectweb.asm.Opcodes

class SAPluginManager(private val project: Project) {
    lateinit var extension: SAExtension
    var isHookOnMethodEnter = false
    var isAndroidTV = false
    private var asmVersion = "ASM7"
    val packageManager: SAPackageManager by lazy {
        SAPackageManager().apply {
            exclude.addAll(extension.exclude)
            include.addAll(extension.include)
        }
    }
    private val supportedModules: Set<SAModule> by lazy {
        val set = mutableSetOf<SAModule>()
        extension.disableModules.forEach {
            set.add(SAModule.valueOf(it.uppercase()))
        }
        set
    }

    init {
        createExtension()
        parseProperty()
        checkDependency()
        otherInit()
    }

    val sdkVersionHelper: SensorsDataSDKVersionHelper by lazy {
        SensorsDataSDKVersionHelper()
    }

    private fun checkDependency() {
        project.afterEvaluate {
            if (!project.plugins.hasPlugin("com.android.application")) {
                check(false) {
                    "SensorsData Android Plugin must used at Android App Project"
                }
            }
            /*//需要充分考虑到模块中依赖 sdk 和 aar 依赖
            val dependencies = project.configurations.flatMap { it.dependencies }
            dependencies.forEach {
                val name = it.name
                val group = it.group
                val version = it.version
                if ("SensorsAnalyticsSDK" == name) {
                    isAutoTrackInstall = true
                    isPushModuleInstall = true
                    if (version != null && version != "unspecified" && group == "com.sensorsdata.analytics.android") {
                        check(
                            SensorsAnalyticsUtil.compareVersion(
                                VersionConstant.MIN_SDK_VERSION,
                                version
                            ) <= 0
                        ) {
                            "你目前集成的神策插件版本号为 v$version，请升级到 v${VersionConstant.MIN_SDK_VERSION} 及以上的版本。" +
                                    "详情请参考：https://github.com/sensorsdata/sa-sdk-android-plugin2"
                        }
                    }
                }
            }*/
        }
    }

    private fun createExtension() {
        extension = project.extensions.create("sensorsAnalytics", SAExtension::class.java)
    }

    private fun parseProperty() {
        project.properties.apply {
            isHookOnMethodEnter =
                (getOrDefault("sensorsAnalytics.isHookOnMethodEnter", "") as String).toBoolean()
            isAndroidTV =
                (getOrDefault("sensorsAnalytics.isAndroidTv", "false") as String).toBoolean()
            asmVersion = getOrDefault("sensorsAnalytics.asmVersion", "ASM7") as String
            if (AGPVersion.CURRENT_AGP_VERSION >= AGPVersion(8, 0, 0)) {
                asmVersion = "ASM9"
            }
        }
    }

    private fun otherInit() {
        project.afterEvaluate {
            SAConfigHookHelper.initSDKConfigCells(extension)
            Logger.debug = extension.debug
            //module check
        }
    }

    fun getASMVersion(): Int {
        return when (asmVersion) {
            "ASM6" -> Opcodes.ASM6
            "ASM7" -> Opcodes.ASM7
            "ASM8" -> Opcodes.ASM8
            "ASM9" -> Opcodes.ASM9
            else -> Opcodes.ASM7
        }
    }

    internal fun getASMVersionStr(): String = asmVersion

    fun isModuleEnable(module: SAModule) = !supportedModules.contains(module)
}

enum class SAModule {
    AUTOTRACK, PUSH, WEB_VIEW, REACT_NATIVE
}