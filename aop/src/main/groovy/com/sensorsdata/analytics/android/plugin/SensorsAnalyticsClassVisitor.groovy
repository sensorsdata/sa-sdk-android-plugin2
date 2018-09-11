package com.sensorsdata.analytics.android.plugin

import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

class SensorsAnalyticsClassVisitor extends ClassVisitor implements Opcodes {
    private String mClassName
    private String mSuperName
    private String[] mInterfaces
    private HashSet<String> visitedFragMethods = new HashSet<>()// 无需判空
    private ClassVisitor classVisitor

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone()
    }

    SensorsAnalyticsClassVisitor(final ClassVisitor classVisitor) {
        super(Opcodes.ASM5, classVisitor)
        this.classVisitor = classVisitor
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
        super.visit(version, access, name, signature, superName, interfaces)
        Logger.info("开始扫描类：${mClassName}")
        Logger.info("类详情：version=${version};\taccess=${Logger.accCode2String(access)};\tname=${name};\tsignature=${signature};\tsuperName=${superName};\tinterfaces=${interfaces.toArrayString()}\n")
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
            Iterator<Map.Entry<String, SensorsAnalyticsMethodCell>> iterator = SensorsAnalyticsHookConfig.sFragmentMethods.entrySet().iterator()
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
                visitMethodWithLoadedParams(mv, Opcodes.INVOKESTATIC, SensorsAnalyticsHookConfig.sSensorsAnalyticsAPI, methodCell.agentName, methodCell.agentDesc, methodCell.paramsStart, methodCell.paramsCount, methodCell.opcodes)
                mv.visitInsn(Opcodes.RETURN)
                mv.visitMaxs(methodCell.paramsCount, methodCell.paramsCount)
                mv.visitEnd()
                mv.visitAnnotation("Lcom/sensorsdata/analytics/android/sdk/SensorsDataInstrumented;", false)
            }
        }

        Logger.info("结束扫描类：${mClassName}\n")
    }

    /**
     *  该方法是当扫描器扫描到类的方法时进行调用
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
        MethodVisitor methodVisitor = cv.visitMethod(access, name, desc, signature, exceptions)

        methodVisitor = new SensorsAnalyticsDefaultMethodVisitor(methodVisitor, access, name, desc) {
            boolean isSensorsDataTrackViewOnClickAnnotation = false
            boolean isSensorsDataIgnoreTrackOnClick = false
            boolean isHasInstrumented = false
            boolean isHasTracked = false

            @Override
            void visitEnd() {
                super.visitEnd()

                if (isHasTracked) {
                    visitAnnotation("Lcom/sensorsdata/analytics/android/sdk/SensorsDataInstrumented;", false)
                    Logger.info("Hooked method: ${name}${desc}\n")
                }
            }

            @Override
            protected void onMethodEnter() {
                super.onMethodEnter()

                if (isSensorsDataIgnoreTrackOnClick) {
                    return
                }

                if (!(SensorsAnalyticsUtil.isPublic(access) && !SensorsAnalyticsUtil.isStatic(access))) {
                    return
                }

                /**
                 * 之前已经添加过埋点代码，忽略
                 */
                if (isHasInstrumented) {
                    return
                }

                /**
                 * Method 描述信息
                 */
                String nameDesc = name + desc

                /**
                 * React Native
                 */
                if (mClassName == 'com/facebook/react/uimanager/NativeViewHierarchyManager') {
                    if (nameDesc == 'setJSResponder(IIZ)V') {
                        methodVisitor.visitVarInsn(ALOAD, 0)
                        methodVisitor.visitVarInsn(ILOAD, 1)
                        methodVisitor.visitVarInsn(ILOAD, 2)
                        methodVisitor.visitVarInsn(ILOAD, 3)
                        methodVisitor.visitMethodInsn(INVOKESTATIC, SensorsAnalyticsHookConfig.sSensorsAnalyticsAPI, "trackRN", "(Ljava/lang/Object;IIZ)V", false)
                        isHasTracked = true
                        return
                    }
                }

                /**
                 * 忽略 RN 的其它所有方法
                 */
                if (mClassName.startsWith('com/facebook/react')) {
                    //return
                }

                /**
                 * Fragment
                 * 目前支持 android/support/v4/app/ListFragment 和 android/support/v4/app/Fragment
                 */
                if (SensorsAnalyticsUtil.isInstanceOfFragment(mSuperName)) {
                    SensorsAnalyticsMethodCell sensorsAnalyticsMethodCell = SensorsAnalyticsHookConfig.sFragmentMethods.get(nameDesc)
                    if (sensorsAnalyticsMethodCell != null) {
                        visitedFragMethods.add(nameDesc)
                        visitMethodWithLoadedParams(methodVisitor, Opcodes.INVOKESTATIC, SensorsAnalyticsHookConfig.sSensorsAnalyticsAPI, sensorsAnalyticsMethodCell.agentName, sensorsAnalyticsMethodCell.agentDesc, sensorsAnalyticsMethodCell.paramsStart, sensorsAnalyticsMethodCell.paramsCount, sensorsAnalyticsMethodCell.opcodes)
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
                    methodVisitor.visitMethodInsn(INVOKESTATIC, SensorsAnalyticsHookConfig.sSensorsAnalyticsAPI, "trackMenuItem", "(Ljava/lang/Object;Landroid/view/MenuItem;)V", false)
                    isHasTracked = true
                    return
                }

                if (nameDesc == 'onDrawerOpened(Landroid/view/View;)V') {
                    methodVisitor.visitVarInsn(ALOAD, 1)
                    methodVisitor.visitMethodInsn(INVOKESTATIC, SensorsAnalyticsHookConfig.sSensorsAnalyticsAPI, "trackDrawerOpened", "(Landroid/view/View;)V", false)
                    isHasTracked = true
                    return
                } else if (nameDesc == 'onDrawerClosed(Landroid/view/View;)V') {
                    methodVisitor.visitVarInsn(ALOAD, 1)
                    methodVisitor.visitMethodInsn(INVOKESTATIC, SensorsAnalyticsHookConfig.sSensorsAnalyticsAPI, "trackDrawerClosed", "(Landroid/view/View;)V", false)
                    isHasTracked = true
                    return
                }

                if (mClassName == 'android/databinding/generated/callback/OnClickListener') {
                    if (nameDesc == 'onClick(Landroid/view/View;)V') {
                        methodVisitor.visitVarInsn(ALOAD, 1)
                        methodVisitor.visitMethodInsn(INVOKESTATIC, SensorsAnalyticsHookConfig.sSensorsAnalyticsAPI, "trackViewOnClick", "(Landroid/view/View;)V", false)
                        isHasTracked = true
                        return
                    }
                }

                if (mClassName.startsWith('android')) {
                    return
                }

                if (nameDesc == 'onItemSelected(Landroid/widget/AdapterView;Landroid/view/View;IJ)V' || nameDesc == "onListItemClick(Landroid/widget/ListView;Landroid/view/View;IJ)V") {
                    methodVisitor.visitVarInsn(ALOAD, 1)
                    methodVisitor.visitVarInsn(ALOAD, 2)
                    methodVisitor.visitVarInsn(ILOAD, 3)
                    methodVisitor.visitMethodInsn(INVOKESTATIC, SensorsAnalyticsHookConfig.sSensorsAnalyticsAPI, "trackListView", "(Landroid/widget/AdapterView;Landroid/view/View;I)V", false)
                    isHasTracked = true
                    return
                }

                if (isSensorsDataTrackViewOnClickAnnotation) {
                    if (desc == '(Landroid/view/View;)V') {
                        methodVisitor.visitVarInsn(ALOAD, 1)
                        methodVisitor.visitMethodInsn(INVOKESTATIC, SensorsAnalyticsHookConfig.sSensorsAnalyticsAPI, "trackViewOnClick", "(Landroid/view/View;)V", false)
                        isHasTracked = true
                        return
                    }
                }

                if (mInterfaces != null && mInterfaces.length > 0) {
                    SensorsAnalyticsMethodCell sensorsAnalyticsMethodCell = SensorsAnalyticsHookConfig.sInterfaceMethods.get(nameDesc)
                    if (sensorsAnalyticsMethodCell != null && mInterfaces.contains(sensorsAnalyticsMethodCell.parent)) {
                        visitMethodWithLoadedParams(methodVisitor, Opcodes.INVOKESTATIC, SensorsAnalyticsHookConfig.sSensorsAnalyticsAPI, sensorsAnalyticsMethodCell.agentName, sensorsAnalyticsMethodCell.agentDesc, sensorsAnalyticsMethodCell.paramsStart, sensorsAnalyticsMethodCell.paramsCount, sensorsAnalyticsMethodCell.opcodes)
                        isHasTracked = true
                    }
                }

                if (!isHasTracked) {
                    if (nameDesc == 'onClick(Landroid/view/View;)V') {
                        methodVisitor.visitVarInsn(ALOAD, 1)
                        methodVisitor.visitMethodInsn(INVOKESTATIC, SensorsAnalyticsHookConfig.sSensorsAnalyticsAPI, "trackViewOnClick", "(Landroid/view/View;)V", false)
                        isHasTracked = true
                    }
                }
            }

            /**
             * 该方法是当扫描器扫描到类注解声明时进行调用
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
                }

                if (s == 'Lcom/sensorsdata/analytics/android/sdk/SensorsDataIgnoreTrackOnClick;') {
                    isSensorsDataIgnoreTrackOnClick = true
                    Logger.info("发现 ${name}${desc} 有注解 @SensorsDataIgnoreTrackOnClick")
                }

                if (s == 'Lcom/sensorsdata/analytics/android/sdk/SensorsDataInstrumented;') {
                    isHasInstrumented = true
                }

                return super.visitAnnotation(s, b)
            }
        }

        return methodVisitor
    }
}