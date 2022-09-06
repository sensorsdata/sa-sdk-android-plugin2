/*
 * Created by wangzhuohou on 2015/08/01.
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
package com.sensorsdata.analytics.android.plugin

import com.sensorsdata.analytics.android.plugin.configs.SAConfigHookHelper
import com.sensorsdata.analytics.android.plugin.core.DefaultMethodVisitor
import com.sensorsdata.analytics.android.plugin.fragment.FragmentHookHelper
import com.sensorsdata.analytics.android.plugin.js.AddJSAnnotationVisitor
import com.sensorsdata.analytics.android.plugin.js.SensorsAnalyticsWebViewMethodVisitor
import com.sensorsdata.analytics.android.plugin.push.SensorsAnalyticsPushMethodVisitor
import com.sensorsdata.analytics.android.plugin.push.SensorsPushInjected
import com.sensorsdata.analytics.android.plugin.utils.Logger
import com.sensorsdata.analytics.android.plugin.utils.ModuleUtils
import com.sensorsdata.analytics.android.plugin.version.SensorsAnalyticsVersionFieldVisitor
import com.sensorsdata.analytics.android.plugin.version.SensorsDataSDKVersionHelper
import com.sensorsdata.analytics.android.plugin.viewclick.SensorsAnalyticsMethodCell
import com.sensorsdata.analytics.android.plugin.viewclick.SensorsAutoTrackMethodVisitor
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

class SensorsAnalyticsClassVisitor extends ClassVisitor {
    private String mClassName
    private String mSuperName
    private List<String> mInterfaces
    private HashSet<String> visitedFragMethods = new HashSet<>()// 无需判空
    private boolean isFoundOnNewIntent = false
    private ClassVisitor classVisitor
    private SensorsAnalyticsTransformHelper transformHelper
    private ClassNameAnalytics classNameAnalytics
    private SAConfigHookHelper mSDKConfigHelper
    private HashMap<String, SensorsAnalyticsMethodCell> mLambdaMethodCells = new HashMap<>()
    private int version
    private SensorsDataSDKVersionHelper sdkVersionHelper

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone()
    }

    SensorsAnalyticsClassVisitor(final ClassVisitor classVisitor, ClassNameAnalytics classNameAnalytics, SensorsAnalyticsTransformHelper transformHelper, SensorsDataSDKVersionHelper sdkVersionHelper) {
        super(SensorsAnalyticsUtil.ASM_VERSION, classVisitor)
        this.classVisitor = classVisitor
        this.classNameAnalytics = classNameAnalytics
        this.transformHelper = transformHelper
        this.sdkVersionHelper = sdkVersionHelper
        this.mSDKConfigHelper = new SAConfigHookHelper()
    }

    /**
     * 该方法是当扫描类时第一个拜访的方法，主要用于类声明使用
     * @param version 表示类版本：51，表示 “.class” 文件的版本是 JDK 1.7
     * @param access 类的修饰符：修饰符在 ASM 中是以 “ACC_” 开头的常量进行定义。
     *                          可以作用到类级别上的修饰符有：ACC_PUBLIC（public）、ACC_PRIVATE（private）、ACC_PROTECTED（protected）、
     *                          ACC_FINAL（final）、ACC_SUPER（extends）、ACC_INTERFACE（接口）、ACC_ABSTRACT（抽象类）、
     *                          ACC_ANNOTATION（注解类型）、ACC_ENUM（枚举类型）、ACC_DEPRECATED（标记了@Deprecated注解的类）、ACC_SYNTHETIC
     * @param name 类的名称：通常我们的类完整类名使用 “org.test.mypackage.MyClass” 来表示，但是到了字节码中会以路径形式表示它们 “org/test/mypackage/MyClass” 。
     *                      值得注意的是虽然是路径表示法但是不需要写明类的 “.class” 扩展名。
     * @param signature 表示泛型信息，如果类并未定义任何泛型该参数为空
     * @param superName 表示所继承的父类：由于 Java 的类是单根结构，即所有类都继承自 java.lang.Object。 因此可以简单的理解为任何类都会具有一个父类。
     *                  虽然在编写 Java 程序时我们没有去写 extends 关键字去明确继承的父类，但是 JDK在编译时 总会为我们加上 “ extends Object”。
     * @param interfaces 表示类实现的接口，在 Java 中类是可以实现多个不同的接口因此此处是一个数组。
     */
    @Override
    void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        mClassName = name
        mSuperName = superName
        mInterfaces = Arrays.asList(interfaces)
        this.version = version
        super.visit(version, access, name, signature, superName, interfaces)
        if (Logger.debug) {
            Logger.info("开始扫描类：${mClassName}")
            Logger.info("类详情：version=${version};\taccess=${Logger.accCode2String(access)};\tname=${name};\tsignature=${signature};\tsuperName=${superName};\tinterfaces=${interfaces.toArrayString()}\n")
        }
    }

    /**
     * 该方法是当扫描器完成类扫描时才会调用，如果想在类中追加某些方法，可以在该方法中实现。
     */
    @Override
    void visitEnd() {
        super.visitEnd()
        if (!transformHelper.extension.disableTrackPush && !isFoundOnNewIntent
                && mSuperName == "android/app/Activity" && ModuleUtils.isPushModuleInstall) {
            SensorsPushInjected.addOnNewIntent(classVisitor)
        }

        if (ModuleUtils.isAutoTrackInstall) {
            FragmentHookHelper.hookFragment(classVisitor, mSuperName, visitedFragMethods)
        }

        mSDKConfigHelper.disableIdentifierMethod(classVisitor)
        if (Logger.debug) {
            Logger.info("结束扫描类：${mClassName}\n")
        }
    }


    @Override
    FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        FieldVisitor fieldVisitor = super.visitField(access, name, descriptor, signature, value)
        if (classNameAnalytics.isSensorsDataAPI || classNameAnalytics.isSensorsDataVersion) {
            return new SensorsAnalyticsVersionFieldVisitor(SensorsAnalyticsUtil.ASM_VERSION, fieldVisitor, name, value, sdkVersionHelper, mClassName, classNameAnalytics)
        }
        return fieldVisitor
    }

    /**
     * 该方法是当扫描器扫描到类的方法时进行调用
     * @param access 表示方法的修饰符
     * @param name 表示方法名，在 ASM 中 “visitMethod” 方法会处理（构造方法、静态代码块、私有方法、受保护的方法、共有方法、native类型方法）。
     *                  在这些范畴中构造方法的方法名为 “<init>”，静态代码块的方法名为 “<clinit>”。
     * @param desc 表示方法签名，方法签名的格式如下：“(参数列表)返回值类型”
     * @param signature 凡是具有泛型信息的方法，该参数都会有值。并且该值的内容信息基本等于第三个参数的拷贝，只不过不同的是泛型参数被特殊标记出来
     * @param exceptions 用来表示将会抛出的异常，如果方法不会抛出异常，则该参数为空
     * @return
     */
    @Override
    MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if (mSDKConfigHelper.isConfigsMethod(name, desc)) {
            return null
        }

        if (mSuperName == "android/app/Activity" && name == "onNewIntent" && desc == "(Landroid/content/Intent;)V") {
            isFoundOnNewIntent = true
        }

        MethodVisitor methodVisitor = cv.visitMethod(access, name, desc, signature, exceptions)
        if (transformHelper.extension != null && transformHelper.extension.autoHandleWebView && transformHelper.urlClassLoader != null) {
            methodVisitor = new SensorsAnalyticsWebViewMethodVisitor(methodVisitor, access, name, desc, transformHelper, mClassName, mSuperName)
        }

        if (transformHelper.extension != null && !transformHelper.extension.disableTrackPush && ModuleUtils.isPushModuleInstall) {
            methodVisitor = new SensorsAnalyticsPushMethodVisitor(methodVisitor, access, name, desc, mSuperName)
        }

        if (transformHelper.extension != null && classNameAnalytics.isAppWebViewInterface) {
            // add JavaScriptInterface
            methodVisitor = new AddJSAnnotationVisitor(methodVisitor, access, name, desc, transformHelper)
        }

        if (ModuleUtils.isAutoTrackInstall) {
            methodVisitor = new SensorsAutoTrackMethodVisitor(methodVisitor, access, name, desc, classNameAnalytics,
                    transformHelper, mSuperName, mClassName, mInterfaces, visitedFragMethods, mLambdaMethodCells)
        }

        methodVisitor = new DefaultMethodVisitor(methodVisitor, access, name, desc, classNameAnalytics)

        //如果java version 为1.5以前的版本，则使用JSRInlinerAdapter来删除JSR,RET指令
        if (version <= Opcodes.V1_5) {
            return new SensorsAnalyticsJSRAdapter(SensorsAnalyticsUtil.ASM_VERSION, methodVisitor, access, name, desc, signature, exceptions)
        }
        return methodVisitor
    }
}