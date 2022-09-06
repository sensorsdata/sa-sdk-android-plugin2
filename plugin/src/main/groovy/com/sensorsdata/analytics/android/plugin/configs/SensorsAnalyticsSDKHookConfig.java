/*
 * Created by renqingyou on 2018/12/01.
 * Copyright 2015－2022 Sensors Data Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sensorsdata.analytics.android.plugin.configs;

import com.sensorsdata.analytics.android.plugin.viewclick.SensorsAnalyticsMethodCell;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class SensorsAnalyticsSDKHookConfig {

    public static List<SensorsAnalyticsMethodCell> disableIMEI() {
        SensorsAnalyticsMethodCell imei = new SensorsAnalyticsMethodCell("getInternationalIdentifier", "(Landroid/content/Context;)Ljava/lang/String;", "createGetIMEI");
        SensorsAnalyticsMethodCell deviceID = new SensorsAnalyticsMethodCell("getPhoneIdentifier", "(Landroid/content/Context;I)Ljava/lang/String;", "createGetDeviceID");
        return Arrays.asList(imei, deviceID);
    }

    public static List<SensorsAnalyticsMethodCell> disableAndroidID() {
        SensorsAnalyticsMethodCell androidID = new SensorsAnalyticsMethodCell("getIdentifier", "(Landroid/content/Context;)Ljava/lang/String;", "createGetAndroidID");
        return Collections.singletonList(androidID);
    }

    public static List<SensorsAnalyticsMethodCell> disableLog() {
        SensorsAnalyticsMethodCell info = new SensorsAnalyticsMethodCell("info", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)V", "createSALogInfo");
        SensorsAnalyticsMethodCell printStackTrace = new SensorsAnalyticsMethodCell("printStackTrace", "(Ljava/lang/Exception;)V", "createPrintStackTrack");
        return Arrays.asList(info, printStackTrace);
    }

    public static List<SensorsAnalyticsMethodCell> disableJsInterface() {
        SensorsAnalyticsMethodCell showUpWebView = new SensorsAnalyticsMethodCell("showUpWebView", "(Landroid/webkit/WebView;Lorg/json/JSONObject;ZZ)V", "createShowUpWebViewFour");
        SensorsAnalyticsMethodCell showUpX5WebView = new SensorsAnalyticsMethodCell("showUpX5WebView", "(Ljava/lang/Object;Lorg/json/JSONObject;ZZ)V", "createShowUpX5WebViewFour");
        SensorsAnalyticsMethodCell showUpX5WebView2 = new SensorsAnalyticsMethodCell("showUpX5WebView", "(Ljava/lang/Object;Z)V", "createShowUpX5WebViewTwo");
        return Arrays.asList(showUpWebView, showUpX5WebView, showUpX5WebView2);
    }

    public static List<SensorsAnalyticsMethodCell> disableMacAddress() {
        SensorsAnalyticsMethodCell macAddress = new SensorsAnalyticsMethodCell("getMediaAddress", "(Landroid/content/Context;)Ljava/lang/String;", "createGetMacAddress");
        return Collections.singletonList(macAddress);
    }

    public static List<SensorsAnalyticsMethodCell> disableCarrier() {
        SensorsAnalyticsMethodCell carrier = new SensorsAnalyticsMethodCell("getOperator", "(Landroid/content/Context;)Ljava/lang/String;", "createGetCarrier");
        return Collections.singletonList(carrier);
    }

    public static List<SensorsAnalyticsMethodCell> disableOAID() {
        SensorsAnalyticsMethodCell oaid = new SensorsAnalyticsMethodCell("getOpenAdIdentifier", "(Landroid/content/Context;)Ljava/lang/String;", "createGetOAID");
        return Collections.singletonList(oaid);
    }

    //todo 扩展

    public static void createGetIMEI(ClassVisitor classVisitor, SensorsAnalyticsMethodCell methodCell) {
        MethodVisitor mv = classVisitor.visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, methodCell.getName(), methodCell.getDesc(), null, null);
        mv.visitCode();
        mv.visitLdcInsn("");
        mv.visitInsn(Opcodes.ARETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }

    public static void createGetAndroidID(ClassVisitor classVisitor, SensorsAnalyticsMethodCell methodCell) {
        MethodVisitor mv = classVisitor.visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, methodCell.getName(), methodCell.getDesc(), null, null);
        mv.visitCode();
        mv.visitLdcInsn("");
        mv.visitInsn(Opcodes.ARETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }

    public static void createSALogInfo(ClassVisitor classVisitor, SensorsAnalyticsMethodCell methodCell) {
        MethodVisitor mv = classVisitor.visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, methodCell.getName(), methodCell.getDesc(), null, null);
        mv.visitCode();
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(0, 3);
        mv.visitEnd();
    }

    public static void createPrintStackTrack(ClassVisitor classVisitor, SensorsAnalyticsMethodCell methodCell) {
        MethodVisitor mv = classVisitor.visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, methodCell.getName(), methodCell.getDesc(), null, null);
        mv.visitCode();
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(0, 1);
        mv.visitEnd();
    }

    public static void createShowUpWebViewFour(ClassVisitor classVisitor, SensorsAnalyticsMethodCell methodCell) {
        MethodVisitor mv = classVisitor.visitMethod(Opcodes.ACC_PUBLIC, methodCell.getName(), methodCell.getDesc(), null, null);
        mv.visitCode();
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(0, 5);
        mv.visitEnd();
    }

    public static void createShowUpX5WebViewFour(ClassVisitor classVisitor, SensorsAnalyticsMethodCell methodCell) {
        MethodVisitor mv = classVisitor.visitMethod(Opcodes.ACC_PUBLIC, methodCell.getName(), methodCell.getDesc(), null, null);
        mv.visitCode();
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(0, 5);
        mv.visitEnd();
    }

    public static void createShowUpX5WebViewTwo(ClassVisitor classVisitor, SensorsAnalyticsMethodCell methodCell) {
        MethodVisitor mv = classVisitor.visitMethod(Opcodes.ACC_PUBLIC, methodCell.getName(), methodCell.getDesc(), null, null);
        mv.visitCode();
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(0, 3);
        mv.visitEnd();
    }

    public static void createGetMacAddress(ClassVisitor classVisitor, SensorsAnalyticsMethodCell methodCell) {
        MethodVisitor mv = classVisitor.visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, methodCell.getName(), methodCell.getDesc(), null, null);
        mv.visitCode();
        mv.visitLdcInsn("");
        mv.visitInsn(Opcodes.ARETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }

    public static void createGetCarrier(ClassVisitor classVisitor, SensorsAnalyticsMethodCell methodCell) {
        MethodVisitor mv = classVisitor.visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, methodCell.getName(), methodCell.getDesc(), null, null);
        mv.visitCode();
        mv.visitLdcInsn("");
        mv.visitInsn(Opcodes.ARETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }

    public static void createGetDeviceID(ClassVisitor classVisitor, SensorsAnalyticsMethodCell methodCell) {
        MethodVisitor mv = classVisitor.visitMethod(Opcodes.ACC_PRIVATE + Opcodes.ACC_STATIC, methodCell.getName(), methodCell.getDesc(), null, null);
        mv.visitCode();
        mv.visitLdcInsn("");
        mv.visitInsn(Opcodes.ARETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }

    public static void createGetOAID(ClassVisitor classVisitor, SensorsAnalyticsMethodCell methodCell) {
        MethodVisitor mv = classVisitor.visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, methodCell.getName(), methodCell.getDesc(), null, null);
        mv.visitCode();
        mv.visitLdcInsn("");
        mv.visitInsn(Opcodes.ARETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }

    //todo 扩展
}
