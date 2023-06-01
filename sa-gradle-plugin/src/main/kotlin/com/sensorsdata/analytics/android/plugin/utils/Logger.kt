package com.sensorsdata.analytics.android.plugin.utils

import com.sensorsdata.analytics.android.plugin.common.VersionConstant

object Logger {
    var debug = false

    /**
     * 打印提示信息
     */
    fun printCopyright() {
        if (debug) {
            println()
            println("${LogUI.C_BLACK_GREEN.value}####################################################################${LogUI.E_NORMAL.value}")
            println("${LogUI.C_BLACK_GREEN.value}###                                                              ###${LogUI.E_NORMAL.value}")
            println("${LogUI.C_BLACK_GREEN.value}###                                                              ###${LogUI.E_NORMAL.value}")
            println("${LogUI.C_BLACK_GREEN.value}###          欢迎使用 SensorsAnalytics® (v${VersionConstant.VERSION} 编译插件)        ###${LogUI.E_NORMAL.value}")
            println("${LogUI.C_BLACK_GREEN.value}###               使用过程中碰到任何问题请联系我们               ###${LogUI.E_NORMAL.value}")
            println("${LogUI.C_BLACK_GREEN.value}###                      QQ 群号：785122381                      ###${LogUI.E_NORMAL.value}")
            println("${LogUI.C_BLACK_GREEN.value}###                         GitHub 地址：                        ###${LogUI.E_NORMAL.value}")
            println("${LogUI.C_BLACK_GREEN.value}###     https://github.com/sensorsdata/sa-sdk-android-plugin2    ###${LogUI.E_NORMAL.value}")
            println("${LogUI.C_BLACK_GREEN.value}###                                                              ###${LogUI.E_NORMAL.value}")
            println("${LogUI.C_BLACK_GREEN.value}###                                                              ###${LogUI.E_NORMAL.value}")
            println("${LogUI.C_BLACK_GREEN.value}####################################################################${LogUI.E_NORMAL.value}")
            println()
        } else {
            println("${LogUI.C_BLACK_GREEN.value}SensorsAnalytic Plugin v${VersionConstant.VERSION}${LogUI.E_NORMAL.value}")
        }
    }
    fun error(msg: Any) {
        try {
            println("${LogUI.C_ERROR.value}[SensorsAnalytics]: $msg")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun warn(msg: Any) {
        if (debug) {
            try {
                println("${LogUI.C_WARN.value}[SensorsAnalytics]: $msg")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 打印日志
     */
    fun info(msg: Any) {
        if (debug) {
            try {
                println("[SensorsAnalytics]: $msg")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun printNoLimit(msg: String) {
        try {
            println("$msg")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}