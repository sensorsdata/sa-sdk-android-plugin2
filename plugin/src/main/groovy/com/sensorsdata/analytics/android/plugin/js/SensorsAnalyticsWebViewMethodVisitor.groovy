/*
 * Created by zhangwei on 2020/05/21.
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
package com.sensorsdata.analytics.android.plugin.js

import com.sensorsdata.analytics.android.plugin.utils.Logger
import com.sensorsdata.analytics.android.plugin.SensorsAnalyticsHookConfig
import com.sensorsdata.analytics.android.plugin.SensorsAnalyticsTransformHelper
import com.sensorsdata.analytics.android.plugin.SensorsAnalyticsUtil
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.commons.AdviceAdapter

/**
 * 判断逻辑：
 *
 * 如果当前方法所在的类是 WebView 的子类，并且被处理的方法是目标方法中的一个就不处理；
 * 否则就判断 owner 是否是 WebView 的子类，如果是就处理，否则不处理。
 */
class SensorsAnalyticsWebViewMethodVisitor extends AdviceAdapter implements Opcodes {
    private static final String JS_BRIDGE_API = "com/sensorsdata/analytics/android/sdk/jsbridge/JSHookAop"
    private SensorsAnalyticsTransformHelper transformHelper
    private Class webView
    private Class x5WebView
    private boolean isPreviousX5WebView = false
    private static X5WebViewStatus x5WebViewStatus = X5WebViewStatus.NOT_INITIAL
    //目标方法
    private static final List<String> TARGET_NAME_DESC = ["loadUrl(Ljava/lang/String;)V", "loadUrl(Ljava/lang/String;Ljava/util/Map;)V",
                                                          "loadData(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V",
                                                          "loadDataWithBaseURL(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V",
                                                          "postUrl(Ljava/lang/String;[B)V"]
    private static final String VIEW_DESC = "Landroid/view/View;"
    private static final HashSet OWNER_WHITE_SET = new HashSet(["android/webkit/WebView", "com/tencent/smtt/sdk/WebView"])
    private String className
    private String superName
    private String methodNameDesc
    private boolean shouldSkip = false

    SensorsAnalyticsWebViewMethodVisitor(MethodVisitor mv, int access, String name, String desc, SensorsAnalyticsTransformHelper transformHelper, String className, String superName) {
        super(SensorsAnalyticsUtil.ASM_VERSION, mv, access, name, desc)
        this.transformHelper = transformHelper
        this.className = className
        this.superName = superName
        this.methodNameDesc = name + desc
        //如果当前方法符合目标定义，而且此类是 WebView 的子类，那么就跳过 visitMethodInsn 的指令
        if (TARGET_NAME_DESC.contains(methodNameDesc) && isAssignableWebView(className)) {
            shouldSkip = true
        }
    }

    @Override
    void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        if (shouldSkip) {
            super.visitMethodInsn(opcode, owner, name, desc, itf)
            return
        }
        try {
            if (opcode != INVOKESTATIC && TARGET_NAME_DESC.contains(name + desc)) {
                // 解决 NoClassDefError 问题
                if (superName == "com/tencent/smtt/sdk/WebViewClient") {
                    super.visitMethodInsn(opcode, owner, name, desc, itf)
                    return
                }
                if (isAssignableWebView(owner)) {
                    Type[] argTypes = Type.getArgumentTypes(desc)
                    List<Integer> positionList = new ArrayList<>()
                    //依次复制操作数栈顶的元素到局部变量表中保存
                    for (int index = 0; index < argTypes.length; index++) {
                        int position = newLocal(argTypes[index])
                        storeLocal(position)
                        positionList.add(position)
                    }
                    int ownerPosition = newLocal(Type.getObjectType(owner))
                    storeLocal(ownerPosition)
                    positionList.add(ownerPosition)
                    //将局部变量表中的数据压入操作数栈中触发原有的方法
                    positionList.reverseEach { tmp ->
                        loadLocal(tmp)
                    }
                    super.visitMethodInsn(opcode, owner, name, desc, itf)
                    //处理 Dcloud 中 WebView 的写法，缩小范围为 AdaWebView
                    if (isDCloud(owner, className)) {
                        hookDCloud(positionList, name, desc)
                    } else {
                        //将局部变量表中的数据压入操作数栈中触发我们需要插入的方法
                        positionList.reverseEach { tmp ->
                            loadLocal(tmp)
                        }
                        desc = SensorsAnalyticsUtil.appendDescBeforeGiven(desc, VIEW_DESC)
                        mv.visitMethodInsn(INVOKESTATIC, JS_BRIDGE_API, name, desc, false)
                    }
                    return
                }
            }
        } catch (Throwable throwable) {
            Logger.warn("Can not auto handle webview, if you have any questions, please contact our technical services: classname:${className}, method:${methodNameDesc}, exception: ${throwable}")
        }
        super.visitMethodInsn(opcode, owner, name, desc, itf)
    }

    private boolean isDCloud(String owner, String className) {
        return owner == "io/dcloud/common/adapter/ui/webview/DCWebView" && className.contains("AdaWebview")
    }

    private void hookDCloud(List<Integer> positionList, String name, String desc) {
        boolean isCask = false
        int tmpPosition = positionList.last()
        loadLocal(tmpPosition)
        mv.visitTypeInsn(INSTANCEOF, "android/view/View")
        Label label = new Label()
        mv.visitJumpInsn(IFEQ, label)
        positionList.reverseEach { tmp ->
            loadLocal(tmp)
            if (!isCask) {
                isCask = true
                mv.visitTypeInsn(CHECKCAST, "android/view/View")
            }
        }
        desc = SensorsAnalyticsUtil.appendDescBeforeGiven(desc, VIEW_DESC)
        //为保持新 SDK 使用旧版插件问题，会使用新 SDK loadUrl + 2 后缀的方法
        mv.visitMethodInsn(INVOKESTATIC, SensorsAnalyticsHookConfig.SENSORS_ANALYTICS_API, name + "2", desc, false)
        mv.visitLabel(label)
    }

    /**
     * 判断方法的 owner 是否是 WebView 的子类
     *
     * @param owner
     * @return true 表示是 WebView 的子类，否则不是
     */
    private boolean isAssignableWebView(String owner) {
        try {
            if (OWNER_WHITE_SET.contains(owner)) {
                return true
            }
            if (isDCloud(owner, className)) {
                return true
            }
            Class ownerClass = transformHelper.urlClassLoader.loadClass(owner.replace("/", "."))
            if (x5WebViewStatus == X5WebViewStatus.FOUND && isPreviousX5WebView) {
                if (checkX5WebView(ownerClass, owner)) {
                    return true
                }
            }
            return checkAndroidWebView(ownerClass, owner) || checkX5WebView(ownerClass, owner)
        } catch (Throwable throwable) {
            Logger.warn("Can not load class, if you have any questions, please contact our technical services: classname:${className}, exception: ${throwable}")
        }
        return false
    }

    private boolean checkAndroidWebView(Class ownerClass, String owner) {
        if (webView == null) {
            webView = transformHelper.urlClassLoader.loadClass("android.webkit.WebView")
        }
        if (webView.isAssignableFrom(ownerClass)) {
            OWNER_WHITE_SET << owner
            isPreviousX5WebView = false
            return true
        }
    }

    private boolean checkX5WebView(Class ownerClass, String owner) {
        if (x5WebViewStatus == X5WebViewStatus.NOT_FOUND) {
            return false
        }
        if (x5WebView == null) {
            try {
                x5WebView = transformHelper.urlClassLoader.loadClass("com.tencent.smtt.sdk.WebView")
                x5WebViewStatus = X5WebViewStatus.FOUND
            } catch (Throwable ignored) {
                x5WebViewStatus = X5WebViewStatus.NOT_FOUND
                return false
            }
        }
        if (x5WebView.isAssignableFrom(ownerClass)) {
            OWNER_WHITE_SET << owner
            return (isPreviousX5WebView = true)
        }
    }

    private enum X5WebViewStatus {
        NOT_INITIAL, FOUND, NOT_FOUND
    }
}