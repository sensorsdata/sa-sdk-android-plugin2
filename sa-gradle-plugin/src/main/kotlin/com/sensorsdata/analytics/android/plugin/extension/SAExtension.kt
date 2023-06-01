package com.sensorsdata.analytics.android.plugin.extension

import org.gradle.api.Action
import org.gradle.internal.reflect.Instantiator

open class SAExtension {
    var debug = false
    var useInclude = false
    var lambdaEnabled = true
    var addUCJavaScriptInterface = false
    var addXWalkJavaScriptInterface = false
    var lambdaParamOptimize = false
    var disableTrackKeyboard = true
    var exclude = mutableListOf<String>()
    var include = mutableListOf<String>()
    var disableModules = mutableListOf<String>()
    val sdk: SASDKExtension = SASDKExtension()

    fun sdk(action: Action<SASDKExtension>) {
        action.execute(sdk)
    }

    override fun toString(): String {
        return "\tdebug=" + debug + "\n" +
                "\tuseInclude=" + useInclude + "\n" +
                "\taddUCJavaScriptInterface=" + addUCJavaScriptInterface + "\n" +
                "\taddXWalkJavaScriptInterface=" + addXWalkJavaScriptInterface + "\n" +
                "\tlambdaParamOptimize=" + lambdaParamOptimize + "\n" +
                "\tlambdaEnabled=" + lambdaEnabled + "\n" +
                "\tdisableTrackKeyboard=" + disableTrackKeyboard + "\n" +
                "\texclude=[" + exclude.joinToString(",") + "]" + "\n" +
                "\tinclude=[" + include.joinToString(",") + "]" + "\n" +
                "\tdisableModules=[" + disableModules.joinToString(",") + "]" + "\n" +
                "\tsdk {\n" + sdk + "\n" +
                "\t}"
    }
}