package com.sensorsdata.analytics.android.plugin.extension

open class SASDKExtension {
    var disableIMEI = false
    var disableLog = false
    var disableJsInterface = false
    var disableAndroidID = false
    var disableMacAddress = false
    var disableCarrier = false
    var disableOAID = false

    override fun toString(): String {
        return "\t\tdisableIMEI=" + disableIMEI + "\n" +
                "\t\tdisableLog=" + disableLog + "\n" +
                "\t\tdisableJsInterface=" + disableJsInterface + "\n" +
                "\t\tdisableAndroidID=" + disableAndroidID + "\n" +
                "\t\tdisableMacAddress=" + disableMacAddress + "\n" +
                "\t\tdisableCarrier=" + disableCarrier + "\n" +
                "\t\tdisableOAID=" + disableOAID
    }
}