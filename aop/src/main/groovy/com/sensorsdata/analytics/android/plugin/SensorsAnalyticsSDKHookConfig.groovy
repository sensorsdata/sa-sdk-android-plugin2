package com.sensorsdata.analytics.android.plugin

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes


class SensorsAnalyticsSDKHookConfig {

    HashMap<String,HashMap<String,ArrayList<SensorsAnalyticsMethodCell>>> methodCells = new HashMap<>()

    void disableIMEI(String methodName) {
        def imei = new SensorsAnalyticsMethodCell('getIMEI','(Landroid/content/Context;)Ljava/lang/String;','createGetIMEI')
        def imeiMethods = [imei]
        def imeiMethodCells = new HashMap<String,ArrayList<SensorsAnalyticsMethodCell>>()
        imeiMethodCells.put("com/sensorsdata/analytics/android/sdk/util/SensorsDataUtils",imeiMethods)
        methodCells.put(methodName,imeiMethodCells)
    }

    void disableAndroidID(String methodName) {
        def androidID = new SensorsAnalyticsMethodCell('getAndroidID','(Landroid/content/Context;)Ljava/lang/String;','createGetAndroidID')
        def androidIDMethods = [androidID]
        def androidIdMethodCells = new HashMap<String,ArrayList<SensorsAnalyticsMethodCell>>()
        androidIdMethodCells.put('com/sensorsdata/analytics/android/sdk/util/SensorsDataUtils',androidIDMethods)
        methodCells.put(methodName,androidIdMethodCells)
    }

    void disableLog(String methodName) {
        def info = new SensorsAnalyticsMethodCell('info','(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)V',"createSALogInfo")
        def printStackTrace = new SensorsAnalyticsMethodCell('printStackTrace','(Ljava/lang/Exception;)V',"createPrintStackTrack")
        def sALogMethods = [info,printStackTrace]
        def sALogMethodCells = new HashMap<String,ArrayList<SensorsAnalyticsMethodCell>>()
        sALogMethodCells.put('com/sensorsdata/analytics/android/sdk/SALog',sALogMethods)
        methodCells.put(methodName,sALogMethodCells)
    }

    void disableJsInterface(String methodName) {
        def showUpWebView = new SensorsAnalyticsMethodCell("showUpWebView",'(Landroid/webkit/WebView;Lorg/json/JSONObject;ZZ)V',"createShowUpWebViewFour")
        def showUpX5WebView = new SensorsAnalyticsMethodCell("showUpX5WebView",'(Ljava/lang/Object;Lorg/json/JSONObject;ZZ)V',"createShowUpX5WebViewFour")
        def showUpX5WebView2 = new SensorsAnalyticsMethodCell("showUpX5WebView",'(Ljava/lang/Object;Z)V',"createShowUpX5WebViewTwo")
        def sensorsDataAPIMethods = [showUpWebView,showUpX5WebView,showUpX5WebView2]
        def sensorsDataAPIMethodCells = new HashMap<String,ArrayList<SensorsAnalyticsMethodCell>>()
        sensorsDataAPIMethodCells.put('com/sensorsdata/analytics/android/sdk/SensorsDataAPI',sensorsDataAPIMethods)
        methodCells.put(methodName,sensorsDataAPIMethodCells)
    }

    void disableMacAddress(String methodName) {
        def macAddress = new SensorsAnalyticsMethodCell('getMacAddress','(Landroid/content/Context;)Ljava/lang/String;','createGetMacAddress')
        def macMethods = [macAddress]
        def macMethodCells = new HashMap<String,ArrayList<SensorsAnalyticsMethodCell>>()
        macMethodCells.put("com/sensorsdata/analytics/android/sdk/util/SensorsDataUtils",macMethods)
        methodCells.put(methodName,macMethodCells)
    }

    void disableCarrier(String methodName) {
        def carrier = new SensorsAnalyticsMethodCell('getCarrier','(Landroid/content/Context;)Ljava/lang/String;','createGetCarrier')
        def macMethods = [carrier]
        def macMethodCells = new HashMap<String,ArrayList<SensorsAnalyticsMethodCell>>()
        macMethodCells.put("com/sensorsdata/analytics/android/sdk/util/SensorsDataUtils",macMethods)
        methodCells.put(methodName,macMethodCells)
    }

    //todo 扩展

    void createGetIMEI(ClassVisitor classVisitor, SensorsAnalyticsMethodCell methodCell) {
        def mv = classVisitor.visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, methodCell.name, methodCell.desc, null, null)
        mv.visitCode()
        mv.visitLdcInsn("")
        mv.visitInsn(Opcodes.ARETURN)
        mv.visitMaxs(1, 1)
        mv.visitEnd()
    }

    void createGetAndroidID(ClassVisitor classVisitor, SensorsAnalyticsMethodCell methodCell) {
        def mv = classVisitor.visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, methodCell.name, methodCell.desc, null, null)
        mv.visitCode()
        mv.visitLdcInsn("")
        mv.visitInsn(Opcodes.ARETURN)
        mv.visitMaxs(1, 1)
        mv.visitEnd()
    }

    void createSALogInfo(ClassVisitor classVisitor, SensorsAnalyticsMethodCell methodCell) {
        def mv = classVisitor.visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, methodCell.name, methodCell.desc, null, null)
        mv.visitCode()
        mv.visitInsn(Opcodes.RETURN)
        mv.visitMaxs(0, 3)
        mv.visitEnd()
    }

    void createPrintStackTrack(ClassVisitor classVisitor, SensorsAnalyticsMethodCell methodCell) {
        def mv = classVisitor.visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, methodCell.name, methodCell.desc, null, null)
        mv.visitCode()
        mv.visitInsn(Opcodes.RETURN)
        mv.visitMaxs(0, 1)
        mv.visitEnd()
    }

    void createShowUpWebViewFour(ClassVisitor classVisitor, SensorsAnalyticsMethodCell methodCell) {
        def mv = classVisitor.visitMethod(Opcodes.ACC_PUBLIC, methodCell.name, methodCell.desc, null, null)
        mv.visitCode()
        mv.visitInsn(Opcodes.RETURN)
        mv.visitMaxs(0, 5)
        mv.visitEnd()
    }

    void createShowUpX5WebViewFour(ClassVisitor classVisitor, SensorsAnalyticsMethodCell methodCell) {
        def mv = classVisitor.visitMethod(Opcodes.ACC_PUBLIC, methodCell.name, methodCell.desc, null, null)
        mv.visitCode()
        mv.visitInsn(Opcodes.RETURN)
        mv.visitMaxs(0, 5)
        mv.visitEnd()
    }

    void createShowUpX5WebViewTwo(ClassVisitor classVisitor, SensorsAnalyticsMethodCell methodCell) {
        def mv = classVisitor.visitMethod(Opcodes.ACC_PUBLIC, methodCell.name, methodCell.desc, null, null)
        mv.visitCode()
        mv.visitInsn(Opcodes.RETURN)
        mv.visitMaxs(0, 3)
        mv.visitEnd()
    }

    void createGetMacAddress(ClassVisitor classVisitor, SensorsAnalyticsMethodCell methodCell) {
        def mv = classVisitor.visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, methodCell.name, methodCell.desc, null, null)
        mv.visitCode()
        mv.visitLdcInsn("")
        mv.visitInsn(Opcodes.ARETURN)
        mv.visitMaxs(1, 1)
        mv.visitEnd()
    }

    void createGetCarrier(ClassVisitor classVisitor, SensorsAnalyticsMethodCell methodCell) {
        def mv = classVisitor.visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, methodCell.name, methodCell.desc, null, null)
        mv.visitCode()
        mv.visitLdcInsn("")
        mv.visitInsn(Opcodes.ARETURN)
        mv.visitMaxs(1, 1)
        mv.visitEnd()
    }

    //todo 扩展

}
