package com.sensorsdata.analytics.android.plugin.configs

import com.sensorsdata.analytics.android.plugin.common.HookConstant
import com.sensorsdata.analytics.android.plugin.extension.SAExtension
import com.sensorsdata.analytics.android.plugin.visitor.SensorsAnalyticsMethodCell
import org.objectweb.asm.ClassVisitor
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList

class SAConfigHookHelper() {
    // 当前 class 文件对应的控制项，单个 class 文件共用
    private var sClassInConfigCells = CopyOnWriteArrayList<SensorsAnalyticsMethodCell>()

    // 扫描当前类命中的控制项
    private val mHookMethodCells = CopyOnWriteArrayList<SensorsAnalyticsMethodCell>()

    fun initConfigCellInClass(className: String) {
        sClassInConfigCells.clear()
        for (cell in mConfigCells.values) {
            if (cell.containsKey(className)) {
                sClassInConfigCells.addAll(cell[className]!!)
            }
        }
    }

    /**
     * 判断方法是不是 disablexx 配置方法
     */
    fun isConfigsMethod(name: String?, desc: String?): Boolean {
        for (methodCell in sClassInConfigCells) {
            if (methodCell.name == name && methodCell.desc == desc) {
                mHookMethodCells.add(methodCell)
                return true
            }
        }
        return false
    }

    /**
     * 清空方法体
     */
    fun disableIdentifierMethod(classVisitor: ClassVisitor) {
        for (cell in mHookMethodCells) {
            when (cell.agentName) {
                "createGetIMEI" -> {
                    SensorsAnalyticsSDKHookConfig.createGetIMEI(classVisitor, cell)
                }
                "createGetDeviceID" -> {
                    SensorsAnalyticsSDKHookConfig.createGetDeviceID(classVisitor, cell)
                }
                "createGetAndroidID" -> {
                    SensorsAnalyticsSDKHookConfig.createGetAndroidID(classVisitor, cell)
                }
                "createGetMacAddress" -> {
                    SensorsAnalyticsSDKHookConfig.createGetMacAddress(classVisitor, cell)
                }
                "createGetCarrier" -> {
                    SensorsAnalyticsSDKHookConfig.createGetCarrier(classVisitor, cell)
                }
                "createGetOAID" -> {
                    SensorsAnalyticsSDKHookConfig.createGetOAID(classVisitor, cell)
                }
                "createSALogInfo" -> {
                    SensorsAnalyticsSDKHookConfig.createSALogInfo(classVisitor, cell)
                }
                "createPrintStackTrack" -> {
                    SensorsAnalyticsSDKHookConfig.createPrintStackTrack(classVisitor, cell)
                }
                "createShowUpWebViewFour" -> {
                    SensorsAnalyticsSDKHookConfig.createShowUpWebViewFour(classVisitor, cell)
                }
                "createShowUpX5WebViewFour" -> {
                    SensorsAnalyticsSDKHookConfig.createShowUpX5WebViewFour(classVisitor, cell)
                }
                "createShowUpX5WebViewTwo" -> {
                    SensorsAnalyticsSDKHookConfig.createShowUpX5WebViewTwo(classVisitor, cell)
                }
            }
        }
    }

    companion object{
        // 插件配置项，全局共用
        val mConfigCells = HashMap<String, HashMap<String, List<SensorsAnalyticsMethodCell>>>()

        fun initSDKConfigCells(extension: SAExtension) {
            val sdkExtension = Objects.requireNonNull(extension).sdk
            mConfigCells.clear()
            if (sdkExtension.disableAndroidID) {
                val cells = HashMap<String, List<SensorsAnalyticsMethodCell>>()
                cells[HookConstant.SENSORS_DATA_UTILS] =
                    SensorsAnalyticsSDKHookConfig.disableAndroidID()
                mConfigCells["disableAndroidID"] = cells
            }
            if (sdkExtension.disableCarrier) {
                val cells = HashMap<String, List<SensorsAnalyticsMethodCell>>()
                cells[HookConstant.SENSORS_DATA_UTILS] = SensorsAnalyticsSDKHookConfig.disableCarrier()
                mConfigCells["disableCarrier"] = cells
            }
            if (sdkExtension.disableIMEI) {
                val cells = HashMap<String, List<SensorsAnalyticsMethodCell>>()
                cells[HookConstant.SENSORS_DATA_UTILS] = SensorsAnalyticsSDKHookConfig.disableIMEI()
                mConfigCells["disableIMEI"] = cells
            }
            if (sdkExtension.disableMacAddress) {
                val cells = HashMap<String, List<SensorsAnalyticsMethodCell>>()
                cells[HookConstant.SENSORS_DATA_UTILS] =
                    SensorsAnalyticsSDKHookConfig.disableMacAddress()
                mConfigCells["disableMacAddress"] = cells
            }
            if (sdkExtension.disableOAID) {
                val cells = HashMap<String, List<SensorsAnalyticsMethodCell>>()
                cells[HookConstant.OAID_HELPER] = SensorsAnalyticsSDKHookConfig.disableOAID()
                mConfigCells["disableOAID"] = cells
            }
            if (sdkExtension.disableJsInterface) {
                val cells = HashMap<String, List<SensorsAnalyticsMethodCell>>()
                cells[HookConstant.SENSORS_DATA_API] =
                    SensorsAnalyticsSDKHookConfig.disableJsInterface()
                mConfigCells["disableJsInterface"] = cells
            }
            if (sdkExtension.disableLog) {
                val cells = HashMap<String, List<SensorsAnalyticsMethodCell>>()
                cells[HookConstant.SA_LOG] = SensorsAnalyticsSDKHookConfig.disableLog()
                mConfigCells["disableLog"] = cells
            }
        }
    }
}
