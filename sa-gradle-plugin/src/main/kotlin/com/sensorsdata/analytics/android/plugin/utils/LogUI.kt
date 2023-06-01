package com.sensorsdata.analytics.android.plugin.utils

/**
 * reference doc：https://zhuanlan.zhihu.com/p/208768786
 */
enum class LogUI(val value:String) {
    //color
    C_ERROR("\u001B[0;31;40m"),
    C_WARN("\u001B[0;33;40m"),
    C_BLACK_GREEN("\u001B[0;32;340m"),
    //end
    E_NORMAL("\u001B[0m")
}