/*
 * Created by wangzhuohou on 2015/08/01.
 * Copyright 2015－2019 Sensors Data Inc.
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

import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.Handle
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type

class SensorsAnalyticsClassVisitor extends ClassVisitor {
    private String mClassName
    private String mSuperName
    private String[] mInterfaces
    private HashSet<String> visitedFragMethods = new HashSet<>()// 无需判空
    private ClassVisitor classVisitor

    private SensorsAnalyticsTransformHelper transformHelper

    private ClassNameAnalytics classNameAnalytics

    private ArrayList<SensorsAnalyticsMethodCell> methodCells = new ArrayList<>()

    private int version

    private HashMap<String, SensorsAnalyticsMethodCell> mLambdaMethodCells = new HashMap<>()

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone()
    }

    SensorsAnalyticsClassVisitor(final ClassVisitor classVisitor, ClassNameAnalytics classNameAnalytics, SensorsAnalyticsTransformHelper transformHelper) {
        super(Opcodes.ASM6, classVisitor)
        this.classVisitor = classVisitor
        this.classNameAnalytics = classNameAnalytics
        this.transformHelper = transformHelper
    }

    private
    static void visitMethodWithLoadedParams(MethodVisitor methodVisitor, int opcode, String owner, String methodName, String methodDesc, int start, int count, List<Integer> paramOpcodes) {
        for (int i = start; i < start + count; i++) {
            methodVisitor.visitVarInsn(paramOpcodes[i - start], i)
        }
        methodVisitor.visitMethodInsn(opcode, owner, methodName, methodDesc, false)
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
        mInterfaces = interfaces
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

        if (SensorsAnalyticsUtil.isInstanceOfFragment(mSuperName)) {
            MethodVisitor mv
            // 添加剩下的方法，确保super.onHiddenChanged(hidden);等先被调用
            Iterator<Map.Entry<String, SensorsAnalyticsMethodCell>> iterator = SensorsAnalyticsHookConfig.FRAGMENT_METHODS.entrySet().iterator()
            while (iterator.hasNext()) {
                Map.Entry<String, SensorsAnalyticsMethodCell> entry = iterator.next()
                String key = entry.getKey()
                SensorsAnalyticsMethodCell methodCell = entry.getValue()
                if (visitedFragMethods.contains(key)) {
                    continue
                }
                mv = classVisitor.visitMethod(Opcodes.ACC_PUBLIC, methodCell.name, methodCell.desc, null, null)
                mv.visitCode()
                // call super
                visitMethodWithLoadedParams(mv, Opcodes.INVOKESPECIAL, mSuperName, methodCell.name, methodCell.desc, methodCell.paramsStart, methodCell.paramsCount, methodCell.opcodes)
                // call injected method
                visitMethodWithLoadedParams(mv, Opcodes.INVOKESTATIC, SensorsAnalyticsHookConfig.SENSORS_ANALYTICS_API, methodCell.agentName, methodCell.agentDesc, methodCell.paramsStart, methodCell.paramsCount, methodCell.opcodes)
                mv.visitInsn(Opcodes.RETURN)
                mv.visitMaxs(methodCell.paramsCount, methodCell.paramsCount)
                mv.visitEnd()
                mv.visitAnnotation("Lcom/sensorsdata/analytics/android/sdk/SensorsDataInstrumented;", false)
            }
        }

        for (cell in methodCells) {
            transformHelper.sensorsAnalyticsHookConfig."${cell.agentName}"(classVisitor, cell)
        }
        if (Logger.debug) {
            Logger.info("结束扫描类：${mClassName}\n")
        }
    }


    @Override
    FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        if (classNameAnalytics.isSensorsDataAPI) {
            if ('VERSION' == name) {
                String version = (String) value
                if (SensorsAnalyticsTransform.MIN_SDK_VERSION > version) {
                    String errMessage = "你目前集成的神策埋点 SDK 版本号为 v${version}，请升级到 v${SensorsAnalyticsTransform.MIN_SDK_VERSION} 及以上的版本。详情请参考：https://github.com/sensorsdata/sa-sdk-android"
                    Logger.error(errMessage)
                    throw new Error(errMessage)
                }
            } else if ('MIN_PLUGIN_VERSION' == name) {
                String minPluginVersion = (String) value
                if (minPluginVersion != "" && minPluginVersion != null) {
                    if (SensorsAnalyticsTransform.VERSION < minPluginVersion) {
                        String errMessage = "你目前集成的神策插件版本号为 v${SensorsAnalyticsTransform.VERSION}，请升级到 v${minPluginVersion} 及以上的版本。详情请参考：https://github.com/sensorsdata/sa-sdk-android-plugin2"
                        Logger.error(errMessage)
                        throw new Error(errMessage)
                    }
                }
            }
        }
        return super.visitField(access, name, descriptor, signature, value)
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
        for (methodCell in classNameAnalytics.methodCells) {
            if (methodCell.name == name && methodCell.desc == desc) {
                methodCells.add(methodCell)
                return null
            }
        }

        MethodVisitor methodVisitor = cv.visitMethod(access, name, desc, signature, exceptions)
        SensorsAnalyticsDefaultMethodVisitor sensorsAnalyticsDefaultMethodVisitor = new SensorsAnalyticsDefaultMethodVisitor(methodVisitor, access, name, desc) {
            boolean isSensorsDataTrackViewOnClickAnnotation = false
            boolean isSensorsDataIgnoreTrackOnClick = false
            String eventName = null
            String eventProperties = null
            boolean isHasInstrumented = false
            boolean isHasTracked = false
            int variableID = 0
            //nameDesc是'onClick(Landroid/view/View;)V'字符串
            boolean isOnClickMethod = false
            boolean isOnItemClickMethod = false
            //name + desc
            String nameDesc
            boolean isSetUserVisibleHint = false

            //访问权限是public并且非静态
            boolean pubAndNoStaticAccess

            @Override
            void visitEnd() {
                super.visitEnd()

                if (isHasTracked) {
                    if (transformHelper.extension.lambdaEnabled && mLambdaMethodCells.containsKey(nameDesc)) {
                        mLambdaMethodCells.remove(nameDesc)
                    }
                    visitAnnotation("Lcom/sensorsdata/analytics/android/sdk/SensorsDataInstrumented;", false)
                    Logger.info("Hooked method: ${name}${desc}\n")
                }
            }

            @Override
            void visitInvokeDynamicInsn(String name1, String desc1, Handle bsm, Object... bsmArgs) {
                super.visitInvokeDynamicInsn(name1, desc1, bsm, bsmArgs)
                if (!transformHelper.extension.lambdaEnabled) {
                    return
                }
                try {
                    String desc2 = (String) bsmArgs[0]
                    SensorsAnalyticsMethodCell sensorsAnalyticsMethodCell = SensorsAnalyticsHookConfig.LAMBDA_METHODS.get(Type.getReturnType(desc1).getDescriptor() + name1 + desc2)
                    if (sensorsAnalyticsMethodCell != null) {
                        Handle it = (Handle) bsmArgs[1]
                        mLambdaMethodCells.put(it.name + it.desc, sensorsAnalyticsMethodCell)
                    }
                } catch (Exception e) {
                    e.printStackTrace()
                }
            }

            @Override
            protected void onMethodEnter() {
                super.onMethodEnter()
                nameDesc = name + desc
                pubAndNoStaticAccess = SensorsAnalyticsUtil.isPublic(access) && !SensorsAnalyticsUtil.isStatic(access)
                if ((nameDesc == 'onClick(Landroid/view/View;)V') && pubAndNoStaticAccess) {
                    isOnClickMethod = true
                    variableID = newLocal(Type.getObjectType("java/lang/Integer"))
                    methodVisitor.visitVarInsn(ALOAD, 1)
                    methodVisitor.visitVarInsn(ASTORE, variableID)
                } else if (nameDesc == 'onItemClick(Landroid/widget/AdapterView;Landroid/view/View;IJ)V' && pubAndNoStaticAccess) {
                    isOnItemClickMethod = true
                    variableID = newLocal(Type.getObjectType("java/lang/Integer"))
                    methodVisitor.visitVarInsn(ALOAD, 1)
                    methodVisitor.visitVarInsn(ASTORE, variableID)
                } else if (nameDesc == 'setUserVisibleHint(Z)V' && pubAndNoStaticAccess) {
                    isSetUserVisibleHint = true
                    variableID = newLocal(Type.getObjectType("java/lang/Integer"))
                    methodVisitor.visitVarInsn(ILOAD, 1)
                    methodVisitor.visitVarInsn(ISTORE, variableID)
                }
            }

            @Override
            protected void onMethodExit(int opcode) {
                super.onMethodExit(opcode)

                if (isSensorsDataIgnoreTrackOnClick || isHasInstrumented || classNameAnalytics.isSensorsDataAPI) {
                    return
                }

                /**
                 * 在 android.gradle 的 3.2.1 版本中，针对 view 的 setOnClickListener 方法 的 lambda 表达式做特殊处理。
                 */
                if (transformHelper.extension.lambdaEnabled) {
                    SensorsAnalyticsMethodCell lambdaMethodCell = mLambdaMethodCells.get(nameDesc)
                    if (lambdaMethodCell != null) {
                        Type[] types = Type.getArgumentTypes(lambdaMethodCell.desc)
                        int length = types.length
                        Type[] lambdaTypes = Type.getArgumentTypes(desc)
                        // paramStart 为访问的方法参数的下标，从 0 开始
                        int paramStart = lambdaTypes.length - length
                        if (paramStart < 0) {
                            return
                        } else {
                            for (int i = 0; i < length; i++) {
                                if (lambdaTypes[paramStart + i].descriptor != types[i].descriptor) {
                                    return
                                }
                            }
                        }
                        boolean isStaticMethod = SensorsAnalyticsUtil.isStatic(access)
                        if (!isStaticMethod) {
                            if (lambdaMethodCell.desc == '(Landroid/view/MenuItem;)Z') {
                                methodVisitor.visitVarInsn(Opcodes.ALOAD, 0)
                                methodVisitor.visitVarInsn(Opcodes.ALOAD, getVisitPosition(lambdaTypes, paramStart, isStaticMethod))
                                methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, SensorsAnalyticsHookConfig.SENSORS_ANALYTICS_API, lambdaMethodCell.agentName, '(Ljava/lang/Object;Landroid/view/MenuItem;)V', false)
                                isHasTracked = true
                                return
                            }
                        }
                        for (int i = paramStart; i < paramStart + lambdaMethodCell.paramsCount; i++) {
                            methodVisitor.visitVarInsn(lambdaMethodCell.opcodes.get(i - paramStart), getVisitPosition(lambdaTypes, i, isStaticMethod))
                        }
                        methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, SensorsAnalyticsHookConfig.SENSORS_ANALYTICS_API, lambdaMethodCell.agentName, lambdaMethodCell.agentDesc, false)
                        isHasTracked = true
                        return
                    }
                }

                if (!pubAndNoStaticAccess) {
                    return
                }

                /**
                 * React Native
                 */
                if (nameDesc == 'setJSResponder(IIZ)V' && mClassName == 'com/facebook/react/uimanager/NativeViewHierarchyManager') {
                    methodVisitor.visitVarInsn(ALOAD, 0)
                    methodVisitor.visitVarInsn(ILOAD, 1)
                    methodVisitor.visitVarInsn(ILOAD, 2)
                    methodVisitor.visitVarInsn(ILOAD, 3)
                    methodVisitor.visitMethodInsn(INVOKESTATIC, SensorsAnalyticsHookConfig.SENSORS_ANALYTICS_API, "trackRN", "(Ljava/lang/Object;IIZ)V", false)
                    isHasTracked = true
                    return
                }

                /**
                 * Fragment
                 * 目前支持以下 Fragment 页面浏览事件：
                 * android/app/Fragment，android/app/ListFragment， android/app/DialogFragment，
                 * android/support/v4/app/Fragment，android/support/v4/app/ListFragment，android/support/v4/app/DialogFragment，
                 * androidx/fragment/app/Fragment，androidx/fragment/app/ListFragment，androidx/fragment/app/DialogFragment
                 */
                if (SensorsAnalyticsUtil.isInstanceOfFragment(mSuperName)) {
                    SensorsAnalyticsMethodCell sensorsAnalyticsMethodCell = SensorsAnalyticsHookConfig.FRAGMENT_METHODS.get(nameDesc)
                    if (sensorsAnalyticsMethodCell != null) {
                        visitedFragMethods.add(nameDesc)
                        if (isSetUserVisibleHint){
                            methodVisitor.visitVarInsn(ALOAD, 0)
                            methodVisitor.visitVarInsn(ILOAD, variableID)
                            methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, SensorsAnalyticsHookConfig.SENSORS_ANALYTICS_API, sensorsAnalyticsMethodCell.agentName, sensorsAnalyticsMethodCell.agentDesc, false)
                        } else {
                            visitMethodWithLoadedParams(methodVisitor, Opcodes.INVOKESTATIC, SensorsAnalyticsHookConfig.SENSORS_ANALYTICS_API, sensorsAnalyticsMethodCell.agentName, sensorsAnalyticsMethodCell.agentDesc, sensorsAnalyticsMethodCell.paramsStart, sensorsAnalyticsMethodCell.paramsCount, sensorsAnalyticsMethodCell.opcodes)
                        }
                        isHasTracked = true
                    }
                }

                /**
                 * Menu
                 * 目前支持 onContextItemSelected(MenuItem item)、onOptionsItemSelected(MenuItem item)
                 */
                if (SensorsAnalyticsUtil.isTargetMenuMethodDesc(nameDesc)) {
                    methodVisitor.visitVarInsn(ALOAD, 0)
                    methodVisitor.visitVarInsn(ALOAD, 1)
                    methodVisitor.visitMethodInsn(INVOKESTATIC, SensorsAnalyticsHookConfig.SENSORS_ANALYTICS_API, "trackMenuItem", "(Ljava/lang/Object;Landroid/view/MenuItem;)V", false)
                    isHasTracked = true
                    return
                }

                if (nameDesc == 'onDrawerOpened(Landroid/view/View;)V') {
                    methodVisitor.visitVarInsn(ALOAD, 1)
                    methodVisitor.visitMethodInsn(INVOKESTATIC, SensorsAnalyticsHookConfig.SENSORS_ANALYTICS_API, "trackDrawerOpened", "(Landroid/view/View;)V", false)
                    isHasTracked = true
                    return
                } else if (nameDesc == 'onDrawerClosed(Landroid/view/View;)V') {
                    methodVisitor.visitVarInsn(ALOAD, 1)
                    methodVisitor.visitMethodInsn(INVOKESTATIC, SensorsAnalyticsHookConfig.SENSORS_ANALYTICS_API, "trackDrawerClosed", "(Landroid/view/View;)V", false)
                    isHasTracked = true
                    return
                }

                if (isOnClickMethod && mClassName == 'android/databinding/generated/callback/OnClickListener') {
                    trackViewOnClick(methodVisitor, 1)
                    isHasTracked = true
                    return
                }

                if ((mClassName.startsWith('android/') || mClassName.startsWith('androidx/')) && !(mClassName.startsWith("android/support/v17/leanback") || mClassName.startsWith("androidx/leanback"))) {
                    return
                }

                if (nameDesc == 'onItemSelected(Landroid/widget/AdapterView;Landroid/view/View;IJ)V' || nameDesc == "onListItemClick(Landroid/widget/ListView;Landroid/view/View;IJ)V") {
                    methodVisitor.visitVarInsn(ALOAD, 1)
                    methodVisitor.visitVarInsn(ALOAD, 2)
                    methodVisitor.visitVarInsn(ILOAD, 3)
                    methodVisitor.visitMethodInsn(INVOKESTATIC, SensorsAnalyticsHookConfig.SENSORS_ANALYTICS_API, "trackListView", "(Landroid/widget/AdapterView;Landroid/view/View;I)V", false)
                    isHasTracked = true
                    return
                }

                if (isSensorsDataTrackViewOnClickAnnotation && desc == '(Landroid/view/View;)V') {
                    trackViewOnClick(methodVisitor, 1)
                    isHasTracked = true
                    return
                }

                if (eventName != null && eventName.length() != 0) {
                    methodVisitor.visitLdcInsn(eventName)
                    methodVisitor.visitLdcInsn(eventProperties)
                    methodVisitor.visitMethodInsn(INVOKESTATIC, SensorsAnalyticsHookConfig.SENSORS_ANALYTICS_API, "track", "(Ljava/lang/String;Ljava/lang/String;)V", false)
                    isHasTracked = true
                    return
                }

                if (mInterfaces != null && mInterfaces.length > 0) {
                    if (isOnItemClickMethod && mInterfaces.contains('android/widget/AdapterView$OnItemClickListener')) {
                        methodVisitor.visitVarInsn(ALOAD, variableID)
                        methodVisitor.visitVarInsn(ALOAD, 2)
                        methodVisitor.visitVarInsn(ILOAD, 3)
                        methodVisitor.visitMethodInsn(INVOKESTATIC, SensorsAnalyticsHookConfig.SENSORS_ANALYTICS_API, "trackListView", "(Landroid/widget/AdapterView;Landroid/view/View;I)V", false)
                        isHasTracked = true
                        return
                    } else {
                        SensorsAnalyticsMethodCell sensorsAnalyticsMethodCell = SensorsAnalyticsHookConfig.INTERFACE_METHODS.get(nameDesc)
                        if (sensorsAnalyticsMethodCell != null && mInterfaces.contains(sensorsAnalyticsMethodCell.parent)) {
                            visitMethodWithLoadedParams(methodVisitor, INVOKESTATIC, SensorsAnalyticsHookConfig.SENSORS_ANALYTICS_API, sensorsAnalyticsMethodCell.agentName, sensorsAnalyticsMethodCell.agentDesc, sensorsAnalyticsMethodCell.paramsStart, sensorsAnalyticsMethodCell.paramsCount, sensorsAnalyticsMethodCell.opcodes)
                            isHasTracked = true
                            return
                        }
                    }
                }

                if (isOnClickMethod) {
                    trackViewOnClick(methodVisitor, variableID)
                    isHasTracked = true
                }
            }

            void trackViewOnClick(MethodVisitor mv, int index) {
                mv.visitVarInsn(ALOAD, index)
                mv.visitMethodInsn(INVOKESTATIC, SensorsAnalyticsHookConfig.SENSORS_ANALYTICS_API, "trackViewOnClick", "(Landroid/view/View;)V", false)
            }

            /**
             * 该方法是当扫描器扫描到类注解声明时进行调用
             *
             * @param s 注解的类型。它使用的是（“L” + “类型路径” + “;”）形式表述
             * @param b 表示的是，该注解是否在 JVM 中可见
             * 1.RetentionPolicy.SOURCE：声明注解只保留在 Java 源程序中，在编译 Java 类时注解信息不会被写入到 Class。如果使用的是这个配置 ASM 也将无法探测到这个注解。
             * 2.RetentionPolicy.CLASS：声明注解仅保留在 Class 文件中，JVM 运行时并不会处理它，这意味着 ASM 可以在 visitAnnotation 时候探测到它，但是通过Class 反射无法获取到注解信息。
             * 3.RetentionPolicy.RUNTIME：这是最常用的一种声明，ASM 可以探测到这个注解，同时 Java 反射也可以取得注解的信息。所有用到反射获取的注解都会用到这个配置，就是这个原因。
             * @return
             */
            @Override
            AnnotationVisitor visitAnnotation(String s, boolean b) {
                if (s == 'Lcom/sensorsdata/analytics/android/sdk/SensorsDataTrackViewOnClick;') {
                    isSensorsDataTrackViewOnClickAnnotation = true
                    Logger.info("发现 ${name}${desc} 有注解 @SensorsDataTrackViewOnClick")
                } else if (s == 'Lcom/sensorsdata/analytics/android/sdk/SensorsDataIgnoreTrackOnClick;') {
                    isSensorsDataIgnoreTrackOnClick = true
                    Logger.info("发现 ${name}${desc} 有注解 @SensorsDataIgnoreTrackOnClick")
                } else if (s == 'Lcom/sensorsdata/analytics/android/sdk/SensorsDataInstrumented;') {
                    isHasInstrumented = true
                } else if (s == 'Lcom/sensorsdata/analytics/android/sdk/SensorsDataTrackEvent;') {
                    return new AnnotationVisitor(Opcodes.ASM6) {
                        @Override
                        void visit(String key, Object value) {
                            super.visit(key, value)
                            if ("eventName" == key) {
                                eventName = (String) value
                            } else if ("properties" == key) {
                                eventProperties = value.toString()
                            }
                        }
                    }
                }

                return super.visitAnnotation(s, b)
            }
        }
        //如果java version 为1.5以前的版本，则使用JSRInlinerAdapter来删除JSR,RET指令
        if (version <= Opcodes.V1_5) {
            return new SensorsAnalyticsJSRAdapter(Opcodes.ASM6, sensorsAnalyticsDefaultMethodVisitor, access, name, desc, signature, exceptions)
        }
        return sensorsAnalyticsDefaultMethodVisitor
    }

    /**
     * 获取方法参数下标为 index 的对应 ASM index
     * @param types 方法参数类型数组
     * @param index 方法中参数下标，从 0 开始
     * @param isStaticMethod 该方法是否为静态方法
     * @return 访问该方法的 index 位参数的 ASM index
     */
    int getVisitPosition(Type[] types, int index, boolean isStaticMethod) {
        if (types == null || index < 0 || index >= types.length) {
            throw new Error("getVisitPosition error")
        }
        if (index == 0) {
            return isStaticMethod ? 0 : 1
        } else {
            return getVisitPosition(types, index - 1, isStaticMethod) + types[index - 1].getSize()
        }
    }
}