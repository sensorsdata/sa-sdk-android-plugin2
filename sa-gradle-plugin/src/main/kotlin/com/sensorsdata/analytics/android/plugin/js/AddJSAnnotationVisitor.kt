package com.sensorsdata.analytics.android.plugin.js

import com.sensorsdata.analytics.android.plugin.manager.SAPluginManager
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.commons.AdviceAdapter

class AddJSAnnotationVisitor(mv: MethodVisitor?, access: Int, name: String?, desc: String?, private val pluginManager: SAPluginManager) :
    AdviceAdapter(pluginManager.getASMVersion(), mv, access, name, desc) {
    var shouldAddUCJS = false
    var shouldAddXWalkJS = false
    override fun visitAnnotation(s: String, b: Boolean): AnnotationVisitor {
        if (s == "Landroid/webkit/JavascriptInterface;") {
            shouldAddUCJS = pluginManager.extension.addUCJavaScriptInterface
            shouldAddXWalkJS = pluginManager.extension.addXWalkJavaScriptInterface
        }
        return super.visitAnnotation(s, b)
    }

    override fun visitEnd() {
        super.visitEnd()
        if (shouldAddUCJS) {
            visitAnnotation("Lcom/uc/webview/export/JavascriptInterface;", true)
        }
        if (shouldAddXWalkJS) {
            visitAnnotation("Lorg/xwalk/core/JavascriptInterface;", true)
        }
    }
}