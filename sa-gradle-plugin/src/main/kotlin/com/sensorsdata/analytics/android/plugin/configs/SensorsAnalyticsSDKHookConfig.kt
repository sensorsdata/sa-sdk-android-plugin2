package com.sensorsdata.analytics.android.plugin.configs

import com.sensorsdata.analytics.android.plugin.visitor.SensorsAnalyticsMethodCell
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes
import java.util.*

object SensorsAnalyticsSDKHookConfig {
    fun disableIMEI(): List<SensorsAnalyticsMethodCell> {
        val imei = SensorsAnalyticsMethodCell(
            "getInternationalIdentifier",
            "(Landroid/content/Context;)Ljava/lang/String;",
            "createGetIMEI"
        )
        val deviceID = SensorsAnalyticsMethodCell(
            "getPhoneIdentifier",
            "(Landroid/content/Context;I)Ljava/lang/String;",
            "createGetDeviceID"
        )
        return listOf(imei, deviceID)
    }

    fun disableAndroidID(): List<SensorsAnalyticsMethodCell> {
        val androidID = SensorsAnalyticsMethodCell(
            "getIdentifier",
            "(Landroid/content/Context;)Ljava/lang/String;",
            "createGetAndroidID"
        )
        return listOf(androidID)
    }

    fun disableLog(): List<SensorsAnalyticsMethodCell> {
        val info = SensorsAnalyticsMethodCell(
            "info",
            "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)V",
            "createSALogInfo"
        )
        val printStackTrace = SensorsAnalyticsMethodCell(
            "printStackTrace",
            "(Ljava/lang/Exception;)V",
            "createPrintStackTrack"
        )
        return listOf(info, printStackTrace)
    }

    fun disableJsInterface(): List<SensorsAnalyticsMethodCell> {
        val showUpWebView = SensorsAnalyticsMethodCell(
            "showUpWebView",
            "(Landroid/webkit/WebView;Lorg/json/JSONObject;ZZ)V",
            "createShowUpWebViewFour"
        )
        val showUpX5WebView = SensorsAnalyticsMethodCell(
            "showUpX5WebView",
            "(Ljava/lang/Object;Lorg/json/JSONObject;ZZ)V",
            "createShowUpX5WebViewFour"
        )
        val showUpX5WebView2 = SensorsAnalyticsMethodCell(
            "showUpX5WebView",
            "(Ljava/lang/Object;Z)V",
            "createShowUpX5WebViewTwo"
        )
        return listOf(showUpWebView, showUpX5WebView, showUpX5WebView2)
    }

    fun disableMacAddress(): List<SensorsAnalyticsMethodCell> {
        val macAddress = SensorsAnalyticsMethodCell(
            "getMediaAddress",
            "(Landroid/content/Context;)Ljava/lang/String;",
            "createGetMacAddress"
        )
        return listOf(macAddress)
    }

    fun disableCarrier(): List<SensorsAnalyticsMethodCell> {
        val carrier = SensorsAnalyticsMethodCell(
            "getOperator",
            "(Landroid/content/Context;)Ljava/lang/String;",
            "createGetCarrier"
        )
        return listOf(carrier)
    }

    fun disableOAID(): List<SensorsAnalyticsMethodCell> {
        val oaid = SensorsAnalyticsMethodCell(
            "getOpenAdIdentifier",
            "(Landroid/content/Context;)Ljava/lang/String;",
            "createGetOAID"
        )
        return listOf(oaid)
    }

    //todo 扩展
    fun createGetIMEI(classVisitor: ClassVisitor, methodCell: SensorsAnalyticsMethodCell) {
        val mv = classVisitor.visitMethod(
            Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC,
            methodCell.name,
            methodCell.desc,
            null,
            null
        )
        mv.visitCode()
        mv.visitLdcInsn("")
        mv.visitInsn(Opcodes.ARETURN)
        mv.visitMaxs(1, 1)
        mv.visitEnd()
    }

    fun createGetAndroidID(classVisitor: ClassVisitor, methodCell: SensorsAnalyticsMethodCell) {
        val mv = classVisitor.visitMethod(
            Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC,
            methodCell.name,
            methodCell.desc,
            null,
            null
        )
        mv.visitCode()
        mv.visitLdcInsn("")
        mv.visitInsn(Opcodes.ARETURN)
        mv.visitMaxs(1, 1)
        mv.visitEnd()
    }

    fun createSALogInfo(classVisitor: ClassVisitor, methodCell: SensorsAnalyticsMethodCell) {
        val mv = classVisitor.visitMethod(
            Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC,
            methodCell.name,
            methodCell.desc,
            null,
            null
        )
        mv.visitCode()
        mv.visitInsn(Opcodes.RETURN)
        mv.visitMaxs(0, 3)
        mv.visitEnd()
    }

    fun createPrintStackTrack(classVisitor: ClassVisitor, methodCell: SensorsAnalyticsMethodCell) {
        val mv = classVisitor.visitMethod(
            Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC,
            methodCell.name,
            methodCell.desc,
            null,
            null
        )
        mv.visitCode()
        mv.visitInsn(Opcodes.RETURN)
        mv.visitMaxs(0, 1)
        mv.visitEnd()
    }

    fun createShowUpWebViewFour(
        classVisitor: ClassVisitor,
        methodCell: SensorsAnalyticsMethodCell
    ) {
        val mv = classVisitor.visitMethod(
            Opcodes.ACC_PUBLIC,
            methodCell.name,
            methodCell.desc,
            null,
            null
        )
        mv.visitCode()
        mv.visitInsn(Opcodes.RETURN)
        mv.visitMaxs(0, 5)
        mv.visitEnd()
    }

    fun createShowUpX5WebViewFour(
        classVisitor: ClassVisitor,
        methodCell: SensorsAnalyticsMethodCell
    ) {
        val mv = classVisitor.visitMethod(
            Opcodes.ACC_PUBLIC,
            methodCell.name,
            methodCell.desc,
            null,
            null
        )
        mv.visitCode()
        mv.visitInsn(Opcodes.RETURN)
        mv.visitMaxs(0, 5)
        mv.visitEnd()
    }

    fun createShowUpX5WebViewTwo(
        classVisitor: ClassVisitor,
        methodCell: SensorsAnalyticsMethodCell
    ) {
        val mv = classVisitor.visitMethod(
            Opcodes.ACC_PUBLIC,
            methodCell.name,
            methodCell.desc,
            null,
            null
        )
        mv.visitCode()
        mv.visitInsn(Opcodes.RETURN)
        mv.visitMaxs(0, 3)
        mv.visitEnd()
    }

    fun createGetMacAddress(classVisitor: ClassVisitor, methodCell: SensorsAnalyticsMethodCell) {
        val mv = classVisitor.visitMethod(
            Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC,
            methodCell.name,
            methodCell.desc,
            null,
            null
        )
        mv.visitCode()
        mv.visitLdcInsn("")
        mv.visitInsn(Opcodes.ARETURN)
        mv.visitMaxs(1, 1)
        mv.visitEnd()
    }

    fun createGetCarrier(classVisitor: ClassVisitor, methodCell: SensorsAnalyticsMethodCell) {
        val mv = classVisitor.visitMethod(
            Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC,
            methodCell.name,
            methodCell.desc,
            null,
            null
        )
        mv.visitCode()
        mv.visitLdcInsn("")
        mv.visitInsn(Opcodes.ARETURN)
        mv.visitMaxs(1, 1)
        mv.visitEnd()
    }

    fun createGetDeviceID(classVisitor: ClassVisitor, methodCell: SensorsAnalyticsMethodCell) {
        val mv = classVisitor.visitMethod(
            Opcodes.ACC_PRIVATE + Opcodes.ACC_STATIC,
            methodCell.name,
            methodCell.desc,
            null,
            null
        )
        mv.visitCode()
        mv.visitLdcInsn("")
        mv.visitInsn(Opcodes.ARETURN)
        mv.visitMaxs(1, 1)
        mv.visitEnd()
    }

    fun createGetOAID(classVisitor: ClassVisitor, methodCell: SensorsAnalyticsMethodCell) {
        val mv = classVisitor.visitMethod(
            Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC,
            methodCell.name,
            methodCell.desc,
            null,
            null
        )
        mv.visitCode()
        mv.visitLdcInsn("")
        mv.visitInsn(Opcodes.ARETURN)
        mv.visitMaxs(1, 1)
        mv.visitEnd()
    }
}