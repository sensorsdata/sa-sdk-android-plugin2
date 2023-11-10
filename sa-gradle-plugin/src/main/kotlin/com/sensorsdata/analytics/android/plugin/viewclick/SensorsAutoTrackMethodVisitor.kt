package com.sensorsdata.analytics.android.plugin.viewclick

import com.sensorsdata.analytics.android.plugin.ClassNameAnalytics
import com.sensorsdata.analytics.android.plugin.configs.SensorsAnalyticsHookConfig
import com.sensorsdata.analytics.android.plugin.fragment.SensorsFragmentHookConfig
import com.sensorsdata.analytics.android.plugin.manager.SAPackageManager
import com.sensorsdata.analytics.android.plugin.manager.SAPluginManager
import com.sensorsdata.analytics.android.plugin.utils.Logger.warn
import com.sensorsdata.analytics.android.plugin.utils.SAUtils
import com.sensorsdata.analytics.android.plugin.visitor.SensorsAnalyticsMethodCell
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.Handle
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Type
import org.objectweb.asm.commons.AdviceAdapter

class SensorsAutoTrackMethodVisitor(
    mv: MethodVisitor,
    methodAccess: Int,
    methodName: String,
    var desc: String,
    private val classNameAnalytics: ClassNameAnalytics,
    private val visitedFragMethods: MutableSet<String>,
    lambdaMethodCells: MutableMap<String, SensorsAnalyticsMethodCell>,
    private val pluginManager: SAPluginManager
) : AdviceAdapter(
    pluginManager.getASMVersion(), mv,
    methodAccess,
    methodName,
    desc
) {
    private var isSensorsDataTrackViewOnClickAnnotation = false
    private var isSensorsDataIgnoreTrackOnClick = false
    private var eventName: String? = null
    private  var eventProperties = ""
    private var isHasInstrumented = false
    private var isHasTracked = false
    private var variableID = 0

    //nameDesc是'onClick(Landroid/view/View;)V'字符串
    private var isOnClickMethod = false
    private var isOnItemClickMethod = false

    //name + desc
    private var nameDesc = methodName + methodDesc

    //访问权限是public并且非静态
    private var pubAndNoStaticAccess = false
    private var protectedAndNotStaticAccess = false
    private var localIds: ArrayList<Int>? = null
    private val mMethodVisitor: MethodVisitor

    // 是否是 AndroidTV 版本
    private val isAndroidTv: Boolean
    private val mLambdaMethodCells: MutableMap<String, SensorsAnalyticsMethodCell>

    init {
        mMethodVisitor = mv
        mLambdaMethodCells = lambdaMethodCells
        isAndroidTv = pluginManager.isAndroidTV
    }

    override fun visitEnd() {
        super.visitEnd()
        if (isHasTracked) {
            if (pluginManager.extension.lambdaEnabled) {
                mLambdaMethodCells.remove(nameDesc)
            }
            visitAnnotation(
                "Lcom/sensorsdata/analytics/android/sdk/SensorsDataInstrumented;",
                false
            )
        }
    }

    override fun visitInvokeDynamicInsn(
        name1: String,
        desc1: String,
        bsm: Handle,
        vararg bsmArgs: Any
    ) {
        super.visitInvokeDynamicInsn(name1, desc1, bsm, *bsmArgs)
        if (!pluginManager.extension.lambdaEnabled) {
            return
        }
        try {
            val owner = bsm.owner
            if ("java/lang/invoke/LambdaMetafactory" != owner) {
                return
            }
            val desc2 = (bsmArgs[0] as Type).descriptor
            val sensorsAnalyticsMethodCell: SensorsAnalyticsMethodCell? =
                SensorsAnalyticsHookConfig.LAMBDA_METHODS.get(
                    Type.getReturnType(desc1).descriptor + name1 + desc2
                )
            if (sensorsAnalyticsMethodCell != null) {
                val it = bsmArgs[1] as Handle
                mLambdaMethodCells[it.name + it.desc] = sensorsAnalyticsMethodCell
            }
        } catch (e: Exception) {
            warn("Some exception happened when call visitInvokeDynamicInsn: className: " + classNameAnalytics.className + ", error message: " + e.localizedMessage)
        }
    }

    public override fun onMethodEnter() {
        super.onMethodEnter()
        pubAndNoStaticAccess =
            SAUtils.isPublic(access) && !SAUtils.isStatic(
                access
            )
        protectedAndNotStaticAccess =
            SAUtils.isProtected(access) && !SAUtils.isStatic(
                access
            )
        if (pubAndNoStaticAccess) {
            if (nameDesc == "onClick(Landroid/view/View;)V") {
                isOnClickMethod = true
                variableID = newLocal(Type.getObjectType("java/lang/Integer"))
                mMethodVisitor.visitVarInsn(ALOAD, 1)
                mMethodVisitor.visitVarInsn(ASTORE, variableID)
            } else if (nameDesc == "onItemClick(Landroid/widget/AdapterView;Landroid/view/View;IJ)V") {
                localIds = ArrayList()
                isOnItemClickMethod = true
                val first = newLocal(Type.getObjectType("android/widget/AdapterView"))
                mMethodVisitor.visitVarInsn(ALOAD, 1)
                mMethodVisitor.visitVarInsn(ASTORE, first)
                localIds!!.add(first)
                val second = newLocal(Type.getObjectType("android/view/View"))
                mMethodVisitor.visitVarInsn(ALOAD, 2)
                mMethodVisitor.visitVarInsn(ASTORE, second)
                localIds!!.add(second)
                val third = newLocal(Type.INT_TYPE)
                mMethodVisitor.visitVarInsn(ILOAD, 3)
                mMethodVisitor.visitVarInsn(ISTORE, third)
                localIds!!.add(third)
            } else if (SAPackageManager.isInstanceOfFragment(classNameAnalytics.superClass)
                && SensorsFragmentHookConfig.FRAGMENT_METHODS[nameDesc] != null
            ) {
                val sensorsAnalyticsMethodCell: SensorsAnalyticsMethodCell? =
                    SensorsFragmentHookConfig.FRAGMENT_METHODS[nameDesc]
                localIds = ArrayList()
                val types = Type.getArgumentTypes(sensorsAnalyticsMethodCell?.agentDesc)
                for (i in 0 until sensorsAnalyticsMethodCell!!.paramsCount) {
                    val localId = newLocal(types[i])
                    mMethodVisitor.visitVarInsn(sensorsAnalyticsMethodCell.opcodes[i], i)
                    mMethodVisitor.visitVarInsn(
                        SAUtils.convertOpcodes(
                            sensorsAnalyticsMethodCell.opcodes.get(i)
                        ), localId
                    )
                    localIds!!.add(localId)
                }
            } else if (nameDesc == "onCheckedChanged(Landroid/widget/RadioGroup;I)V") {
                localIds = ArrayList()
                val firstLocalId = newLocal(Type.getObjectType("android/widget/RadioGroup"))
                mMethodVisitor.visitVarInsn(ALOAD, 1)
                mMethodVisitor.visitVarInsn(ASTORE, firstLocalId)
                localIds!!.add(firstLocalId)
                val secondLocalId = newLocal(Type.INT_TYPE)
                mMethodVisitor.visitVarInsn(ILOAD, 2)
                mMethodVisitor.visitVarInsn(ISTORE, secondLocalId)
                localIds!!.add(secondLocalId)
            } else if (nameDesc == "onCheckedChanged(Landroid/widget/CompoundButton;Z)V") {
                localIds = ArrayList()
                val firstLocalId = newLocal(Type.getObjectType("android/widget/CompoundButton"))
                mMethodVisitor.visitVarInsn(ALOAD, 1)
                mMethodVisitor.visitVarInsn(ASTORE, firstLocalId)
                localIds!!.add(firstLocalId)
            } else if (nameDesc == "onClick(Landroid/content/DialogInterface;I)V") {
                localIds = ArrayList()
                val firstLocalId = newLocal(Type.getObjectType("android/content/DialogInterface"))
                mMethodVisitor.visitVarInsn(ALOAD, 1)
                mMethodVisitor.visitVarInsn(ASTORE, firstLocalId)
                localIds!!.add(firstLocalId)
                val secondLocalId = newLocal(Type.INT_TYPE)
                mMethodVisitor.visitVarInsn(ILOAD, 2)
                mMethodVisitor.visitVarInsn(ISTORE, secondLocalId)
                localIds!!.add(secondLocalId)
            } else if (SAPackageManager.isTargetMenuMethodDesc(nameDesc)) {
                localIds = ArrayList()
                val firstLocalId = newLocal(Type.getObjectType("java/lang/Object"))
                mMethodVisitor.visitVarInsn(ALOAD, 0)
                mMethodVisitor.visitVarInsn(ASTORE, firstLocalId)
                localIds!!.add(firstLocalId)
                val secondLocalId = newLocal(Type.getObjectType("android/view/MenuItem"))
                mMethodVisitor.visitVarInsn(ALOAD, 1)
                mMethodVisitor.visitVarInsn(ASTORE, secondLocalId)
                localIds!!.add(secondLocalId)
            } else if (nameDesc == "onMenuItemClick(Landroid/view/MenuItem;)Z") {
                localIds = ArrayList()
                val firstLocalId = newLocal(Type.getObjectType("android/view/MenuItem"))
                mMethodVisitor.visitVarInsn(ALOAD, 1)
                mMethodVisitor.visitVarInsn(ASTORE, firstLocalId)
                localIds!!.add(firstLocalId)
            } else if (nameDesc == "onGroupClick(Landroid/widget/ExpandableListView;Landroid/view/View;IJ)Z") {
                localIds = ArrayList()
                val firstLocalId = newLocal(Type.getObjectType("android/widget/ExpandableListView"))
                mMethodVisitor.visitVarInsn(ALOAD, 1)
                mMethodVisitor.visitVarInsn(ASTORE, firstLocalId)
                localIds!!.add(firstLocalId)
                val secondLocalId = newLocal(Type.getObjectType("android/view/View"))
                mMethodVisitor.visitVarInsn(ALOAD, 2)
                mMethodVisitor.visitVarInsn(ASTORE, secondLocalId)
                localIds!!.add(secondLocalId)
                val thirdLocalId = newLocal(Type.INT_TYPE)
                mMethodVisitor.visitVarInsn(ILOAD, 3)
                mMethodVisitor.visitVarInsn(ISTORE, thirdLocalId)
                localIds!!.add(thirdLocalId)
            } else if (nameDesc == "onChildClick(Landroid/widget/ExpandableListView;Landroid/view/View;IIJ)Z") {
                localIds = ArrayList()
                val firstLocalId = newLocal(Type.getObjectType("android/widget/ExpandableListView"))
                mMethodVisitor.visitVarInsn(ALOAD, 1)
                mMethodVisitor.visitVarInsn(ASTORE, firstLocalId)
                localIds!!.add(firstLocalId)
                val secondLocalId = newLocal(Type.getObjectType("android/view/View"))
                mMethodVisitor.visitVarInsn(ALOAD, 2)
                mMethodVisitor.visitVarInsn(ASTORE, secondLocalId)
                localIds!!.add(secondLocalId)
                val thirdLocalId = newLocal(Type.INT_TYPE)
                mMethodVisitor.visitVarInsn(ILOAD, 3)
                mMethodVisitor.visitVarInsn(ISTORE, thirdLocalId)
                localIds!!.add(thirdLocalId)
                val fourthLocalId = newLocal(Type.INT_TYPE)
                mMethodVisitor.visitVarInsn(ILOAD, 4)
                mMethodVisitor.visitVarInsn(ISTORE, fourthLocalId)
                localIds!!.add(fourthLocalId)
            } else if (nameDesc == "onItemSelected(Landroid/widget/AdapterView;Landroid/view/View;IJ)V" || nameDesc == "onListItemClick(Landroid/widget/ListView;Landroid/view/View;IJ)V") {
                localIds = ArrayList()
                val firstLocalId = newLocal(Type.getObjectType("java/lang/Object"))
                mMethodVisitor.visitVarInsn(ALOAD, 1)
                mMethodVisitor.visitVarInsn(ASTORE, firstLocalId)
                localIds!!.add(firstLocalId)
                val secondLocalId = newLocal(Type.getObjectType("android/view/View"))
                mMethodVisitor.visitVarInsn(ALOAD, 2)
                mMethodVisitor.visitVarInsn(ASTORE, secondLocalId)
                localIds!!.add(secondLocalId)
                val thirdLocalId = newLocal(Type.INT_TYPE)
                mMethodVisitor.visitVarInsn(ILOAD, 3)
                mMethodVisitor.visitVarInsn(ISTORE, thirdLocalId)
                localIds!!.add(thirdLocalId)
            } else if (nameDesc == "onStopTrackingTouch(Landroid/widget/SeekBar;)V") {
                localIds = ArrayList()
                val firstLocalId = newLocal(Type.getObjectType("android/widget/SeekBar"))
                mMethodVisitor.visitVarInsn(ALOAD, 1)
                mMethodVisitor.visitVarInsn(ASTORE, firstLocalId)
                localIds!!.add(firstLocalId)
            }
        } else if (protectedAndNotStaticAccess) {
            if (nameDesc == "onListItemClick(Landroid/widget/ListView;Landroid/view/View;IJ)V") {
                localIds = ArrayList()
                val firstLocalId = newLocal(Type.getObjectType("java/lang/Object"))
                mMethodVisitor.visitVarInsn(ALOAD, 1)
                mMethodVisitor.visitVarInsn(ASTORE, firstLocalId)
                localIds!!.add(firstLocalId)
                val secondLocalId = newLocal(Type.getObjectType("android/view/View"))
                mMethodVisitor.visitVarInsn(ALOAD, 2)
                mMethodVisitor.visitVarInsn(ASTORE, secondLocalId)
                localIds!!.add(secondLocalId)
                val thirdLocalId = newLocal(Type.INT_TYPE)
                mMethodVisitor.visitVarInsn(ILOAD, 3)
                mMethodVisitor.visitVarInsn(ISTORE, thirdLocalId)
                localIds!!.add(thirdLocalId)
            }
        }

        // Lambda 参数优化部分，对现有参数进行复制
        if (pluginManager.extension.lambdaEnabled) {
            val lambdaMethodCell: SensorsAnalyticsMethodCell? = mLambdaMethodCells[nameDesc]
            if (lambdaMethodCell != null) {
                //判断是否是在采样中，在采样中才会处理或者开关打开也统一处理
                if (pluginManager.extension.lambdaParamOptimize || SensorsAnalyticsHookConfig.SAMPLING_LAMBDA_METHODS.contains(
                        lambdaMethodCell
                    )
                ) {
                    val types: Array<Type> = Type.getArgumentTypes(lambdaMethodCell.desc)
                    val length = types.size
                    val lambdaTypes = Type.getArgumentTypes(
                        desc
                    )
                    // paramStart 为访问的方法参数的下标，从 0 开始
                    val paramStart = lambdaTypes.size - length
                    if (paramStart < 0) {
                        return
                    } else {
                        for (i in 0 until length) {
                            if (lambdaTypes[paramStart + i].descriptor != types[i].descriptor) {
                                return
                            }
                        }
                    }
                    val isStaticMethod: Boolean = SAUtils.isStatic(access)
                    localIds = ArrayList()
                    for (i in paramStart until paramStart + lambdaMethodCell.paramsCount) {
                        val localId = newLocal(types[i - paramStart])
                        mMethodVisitor.visitVarInsn(
                            lambdaMethodCell.opcodes.get(i - paramStart),
                            getVisitPosition(lambdaTypes, i, isStaticMethod)
                        )
                        mMethodVisitor.visitVarInsn(
                            SAUtils.convertOpcodes(
                                lambdaMethodCell.opcodes.get(i - paramStart)
                            ), localId
                        )
                        localIds!!.add(localId)
                    }
                }
            }
        }
        if (pluginManager.isHookOnMethodEnter) {
            handleCode()
        }
    }

    public override fun onMethodExit(opcode: Int) {
        super.onMethodExit(opcode)
        if (!pluginManager.isHookOnMethodEnter) {
            handleCode()
        }
    }

    override fun visitFieldInsn(opcode: Int, owner: String, fieldName: String, fieldDesc: String) {
        if (classNameAnalytics.isKeyboardViewUtil && !pluginManager.extension.disableTrackKeyboard && "isSensorsCheckKeyboard" == fieldName && opcode == PUTSTATIC) {
            mMethodVisitor.visitInsn(ICONST_0)
        }
        super.visitFieldInsn(opcode, owner, fieldName, fieldDesc)
    }

    private fun handleCode() {
        if (isHasInstrumented || classNameAnalytics.isSensorsDataAPI) {
            return
        }

        if (SAPackageManager.isInstanceOfFragment(classNameAnalytics.superClass)) {
            val sensorsAnalyticsMethodCell: SensorsAnalyticsMethodCell? =
                SensorsFragmentHookConfig.FRAGMENT_METHODS[nameDesc]
            if (sensorsAnalyticsMethodCell != null) {
                visitedFragMethods.add(nameDesc)
//                mMethodVisitor.visitVarInsn(ALOAD, 0)
                for (i in 0 until sensorsAnalyticsMethodCell.paramsCount) {
                    mMethodVisitor.visitVarInsn(
                        sensorsAnalyticsMethodCell.opcodes[i],
                        localIds!![i]
                    )
                }
                mMethodVisitor.visitMethodInsn(
                    INVOKESTATIC,
                    SensorsFragmentHookConfig.SENSORS_FRAGMENT_TRACK_HELPER_API,
                    sensorsAnalyticsMethodCell.agentName,
                    sensorsAnalyticsMethodCell.agentDesc,
                    false
                )
                isHasTracked = true
                return
            }
        }
        if (isSensorsDataIgnoreTrackOnClick) {
            return
        }

        /*
         * 在 android.gradle 的 3.2.1 版本中，针对 view 的 setOnClickListener 方法 的 lambda 表达式做特殊处理。
         */if (pluginManager.extension.lambdaEnabled) {
            val lambdaMethodCell: SensorsAnalyticsMethodCell? = mLambdaMethodCells[nameDesc]
            if (lambdaMethodCell != null) {
                val types: Array<Type> = Type.getArgumentTypes(lambdaMethodCell.desc)
                val length = types.size
                val lambdaTypes = Type.getArgumentTypes(
                    desc
                )
                // paramStart 为访问的方法参数的下标，从 0 开始
                val paramStart = lambdaTypes.size - length
                if (paramStart < 0) {
                    return
                } else {
                    for (i in 0 until length) {
                        if (lambdaTypes[paramStart + i].descriptor != types[i].descriptor) {
                            return
                        }
                    }
                }
                val isStaticMethod: Boolean = SAUtils.isStatic(access)
                if (!isStaticMethod) {
                    if (lambdaMethodCell.desc.equals("(Landroid/view/MenuItem;)Z")) {
                        mMethodVisitor.visitVarInsn(ALOAD, 0)
                        mMethodVisitor.visitVarInsn(
                            ALOAD,
                            getVisitPosition(lambdaTypes, paramStart, isStaticMethod)
                        )
                        mMethodVisitor.visitMethodInsn(
                            INVOKESTATIC,
                            SensorsAnalyticsHookConfig.SENSORS_ANALYTICS_API,
                            lambdaMethodCell.agentName,
                            "(Ljava/lang/Object;Landroid/view/MenuItem;)V",
                            false
                        )
                        isHasTracked = true
                        return
                    }
                }
                //如果在采样中，就按照最新的处理流程来操作
                if (pluginManager.extension.lambdaParamOptimize || SensorsAnalyticsHookConfig.SAMPLING_LAMBDA_METHODS.contains(
                        lambdaMethodCell
                    )
                ) {
                    for (i in paramStart until paramStart + lambdaMethodCell.paramsCount) {
                        mMethodVisitor.visitVarInsn(
                            lambdaMethodCell.opcodes.get(i - paramStart),
                            localIds!![i - paramStart]
                        )
                    }
                } else {
                    for (i in paramStart until paramStart + lambdaMethodCell.paramsCount) {
                        mMethodVisitor.visitVarInsn(
                            lambdaMethodCell.opcodes.get(i - paramStart),
                            getVisitPosition(lambdaTypes, i, isStaticMethod)
                        )
                    }
                }
                mMethodVisitor.visitMethodInsn(
                    INVOKESTATIC,
                    SensorsAnalyticsHookConfig.SENSORS_ANALYTICS_API,
                    lambdaMethodCell.agentName,
                    lambdaMethodCell.agentDesc,
                    false
                )
                isHasTracked = true
                return
            }
        }
        if (!pubAndNoStaticAccess) {
            //如果是 protected 那么也需要处理
            if (protectedAndNotStaticAccess) {
                if (nameDesc == "onListItemClick(Landroid/widget/ListView;Landroid/view/View;IJ)V") {
                    mMethodVisitor.visitVarInsn(ALOAD, localIds!![0])
                    mMethodVisitor.visitVarInsn(ALOAD, localIds!![1])
                    mMethodVisitor.visitVarInsn(ILOAD, localIds!![2])
                    mMethodVisitor.visitMethodInsn(
                        INVOKESTATIC,
                        SensorsAnalyticsHookConfig.SENSORS_ANALYTICS_API,
                        "trackListView",
                        "(Landroid/widget/AdapterView;Landroid/view/View;I)V",
                        false
                    )
                    isHasTracked = true
                    return
                }
            }
            return
        }
        if (isAndroidTv && SAPackageManager.isInstanceOfActivity(classNameAnalytics.superClass) && nameDesc == "dispatchKeyEvent(Landroid/view/KeyEvent;)Z") {
            mMethodVisitor.visitVarInsn(ALOAD, 0)
            mMethodVisitor.visitVarInsn(ALOAD, 1)
            mMethodVisitor.visitMethodInsn(
                INVOKESTATIC,
                SensorsAnalyticsHookConfig.SENSORS_ANALYTICS_API,
                "trackViewOnClick",
                "(Landroid/app/Activity;Landroid/view/KeyEvent;)V",
                false
            )
            isHasTracked = true
            return
        }
        if (handleRN()) {
            isHasTracked = true
            return
        }

        /*
         * Menu
         * 目前支持 onContextItemSelected(MenuItem item)、onOptionsItemSelected(MenuItem item)
         */if (SAPackageManager.isTargetMenuMethodDesc(nameDesc)) {
            mMethodVisitor.visitVarInsn(ALOAD, localIds!![0])
            mMethodVisitor.visitVarInsn(ALOAD, localIds!![1])
            mMethodVisitor.visitMethodInsn(
                INVOKESTATIC,
                SensorsAnalyticsHookConfig.SENSORS_ANALYTICS_API,
                "trackMenuItem",
                "(Ljava/lang/Object;Landroid/view/MenuItem;)V",
                false
            )
            isHasTracked = true
            return
        }
        if (nameDesc == "onDrawerOpened(Landroid/view/View;)V") {
            mMethodVisitor.visitVarInsn(ALOAD, 1)
            mMethodVisitor.visitMethodInsn(
                INVOKESTATIC,
                SensorsAnalyticsHookConfig.SENSORS_ANALYTICS_API,
                "trackDrawerOpened",
                "(Landroid/view/View;)V",
                false
            )
            isHasTracked = true
            return
        } else if (nameDesc == "onDrawerClosed(Landroid/view/View;)V") {
            mMethodVisitor.visitVarInsn(ALOAD, 1)
            mMethodVisitor.visitMethodInsn(
                INVOKESTATIC,
                SensorsAnalyticsHookConfig.SENSORS_ANALYTICS_API,
                "trackDrawerClosed",
                "(Landroid/view/View;)V",
                false
            )
            isHasTracked = true
            return
        }
        if (isOnClickMethod && classNameAnalytics.className == "android/databinding/generated/callback/OnClickListener") {
            trackViewOnClick(mMethodVisitor, 1)
            isHasTracked = true
            return
        }
        if (!pluginManager.packageManager.isTargetClassInSpecial(classNameAnalytics.className)) {
            if ((classNameAnalytics.className.startsWith("android/") || classNameAnalytics.className.startsWith(
                    "androidx/"
                )) && !(classNameAnalytics.className.startsWith(
                    "android/support/v17/leanback"
                ) || classNameAnalytics.className.startsWith("androidx/leanback"))
            ) {
                return
            }
        }
        if (nameDesc == "onItemSelected(Landroid/widget/AdapterView;Landroid/view/View;IJ)V" || nameDesc == "onListItemClick(Landroid/widget/ListView;Landroid/view/View;IJ)V") {
            mMethodVisitor.visitVarInsn(ALOAD, localIds!![0])
            mMethodVisitor.visitVarInsn(ALOAD, localIds!![1])
            mMethodVisitor.visitVarInsn(ILOAD, localIds!![2])
            mMethodVisitor.visitMethodInsn(
                INVOKESTATIC,
                SensorsAnalyticsHookConfig.SENSORS_ANALYTICS_API,
                "trackListView",
                "(Landroid/widget/AdapterView;Landroid/view/View;I)V",
                false
            )
            isHasTracked = true
            return
        }
        if (isSensorsDataTrackViewOnClickAnnotation && desc == "(Landroid/view/View;)V") {
            trackViewOnClick(mMethodVisitor, 1)
            isHasTracked = true
            return
        }
        if (eventName != null && eventName!!.length != 0) {
            mMethodVisitor.visitLdcInsn(eventName)
            mMethodVisitor.visitLdcInsn(eventProperties)
            mMethodVisitor.visitMethodInsn(
                INVOKESTATIC,
                SensorsAnalyticsHookConfig.SENSORS_ANALYTICS_API,
                "track",
                "(Ljava/lang/String;Ljava/lang/String;)V",
                false
            )
            isHasTracked = true
            return
        }
        if (classNameAnalytics.interfaces != null && classNameAnalytics.interfaces.size > 0) {
            if (isOnItemClickMethod && classNameAnalytics.interfaces.contains("android/widget/AdapterView\$OnItemClickListener")) {
                mMethodVisitor.visitVarInsn(ALOAD, localIds!![0])
                mMethodVisitor.visitVarInsn(ALOAD, localIds!![1])
                mMethodVisitor.visitVarInsn(ILOAD, localIds!![2])
                mMethodVisitor.visitMethodInsn(
                    INVOKESTATIC,
                    SensorsAnalyticsHookConfig.SENSORS_ANALYTICS_API,
                    "trackListView",
                    "(Landroid/widget/AdapterView;Landroid/view/View;I)V",
                    false
                )
                isHasTracked = true
                return
            } else if (classNameAnalytics.interfaces.contains("android/widget/RadioGroup\$OnCheckedChangeListener")
                && nameDesc == "onCheckedChanged(Landroid/widget/RadioGroup;I)V"
            ) {
                val sensorsAnalyticsMethodCell: SensorsAnalyticsMethodCell? =
                    SensorsAnalyticsHookConfig.INTERFACE_METHODS
                        .get("android/widget/RadioGroup\$OnCheckedChangeListeneronCheckedChanged(Landroid/widget/RadioGroup;I)V")
                if (sensorsAnalyticsMethodCell != null) {
                    mMethodVisitor.visitVarInsn(ALOAD, localIds!![0])
                    mMethodVisitor.visitVarInsn(ILOAD, localIds!![1])
                    mMethodVisitor.visitMethodInsn(
                        INVOKESTATIC,
                        SensorsAnalyticsHookConfig.SENSORS_ANALYTICS_API,
                        sensorsAnalyticsMethodCell.agentName,
                        sensorsAnalyticsMethodCell.agentDesc,
                        false
                    )
                    isHasTracked = true
                    return
                }
            } else if (classNameAnalytics.interfaces.contains("android/widget/CompoundButton\$OnCheckedChangeListener")
                && nameDesc == "onCheckedChanged(Landroid/widget/CompoundButton;Z)V"
            ) {
                val sensorsAnalyticsMethodCell: SensorsAnalyticsMethodCell? =
                    SensorsAnalyticsHookConfig.INTERFACE_METHODS
                        .get("android/widget/CompoundButton\$OnCheckedChangeListeneronCheckedChanged(Landroid/widget/CompoundButton;Z)V")
                if (sensorsAnalyticsMethodCell != null) {
                    mMethodVisitor.visitVarInsn(ALOAD, localIds!![0])
                    mMethodVisitor.visitMethodInsn(
                        INVOKESTATIC,
                        SensorsAnalyticsHookConfig.SENSORS_ANALYTICS_API,
                        sensorsAnalyticsMethodCell.agentName,
                        sensorsAnalyticsMethodCell.agentDesc,
                        false
                    )
                    isHasTracked = true
                    return
                }
            } else if (classNameAnalytics.interfaces.contains("android/content/DialogInterface\$OnClickListener")
                && nameDesc == "onClick(Landroid/content/DialogInterface;I)V"
            ) {
                val sensorsAnalyticsMethodCell: SensorsAnalyticsMethodCell? =
                    SensorsAnalyticsHookConfig.INTERFACE_METHODS
                        .get("android/content/DialogInterface\$OnClickListeneronClick(Landroid/content/DialogInterface;I)V")
                if (sensorsAnalyticsMethodCell != null) {
                    mMethodVisitor.visitVarInsn(ALOAD, localIds!![0])
                    mMethodVisitor.visitVarInsn(ILOAD, localIds!![1])
                    mMethodVisitor.visitMethodInsn(
                        INVOKESTATIC,
                        SensorsAnalyticsHookConfig.SENSORS_ANALYTICS_API,
                        sensorsAnalyticsMethodCell.agentName,
                        sensorsAnalyticsMethodCell.agentDesc,
                        false
                    )
                    isHasTracked = true
                    return
                }
            } else if (classNameAnalytics.interfaces.contains("android/widget/ExpandableListView\$OnGroupClickListener")
                && nameDesc == "onGroupClick(Landroid/widget/ExpandableListView;Landroid/view/View;IJ)Z"
            ) {
                val sensorsAnalyticsMethodCell: SensorsAnalyticsMethodCell? =
                    SensorsAnalyticsHookConfig.INTERFACE_METHODS
                        .get("android/widget/ExpandableListView\$OnGroupClickListeneronGroupClick(Landroid/widget/ExpandableListView;Landroid/view/View;IJ)Z")
                if (sensorsAnalyticsMethodCell != null) {
                    mMethodVisitor.visitVarInsn(ALOAD, localIds!![0])
                    mMethodVisitor.visitVarInsn(ALOAD, localIds!![1])
                    mMethodVisitor.visitVarInsn(ILOAD, localIds!![2])
                    mMethodVisitor.visitMethodInsn(
                        INVOKESTATIC,
                        SensorsAnalyticsHookConfig.SENSORS_ANALYTICS_API,
                        sensorsAnalyticsMethodCell.agentName,
                        sensorsAnalyticsMethodCell.agentDesc,
                        false
                    )
                    isHasTracked = true
                    return
                }
            } else if (classNameAnalytics.interfaces.contains("android/widget/ExpandableListView\$OnChildClickListener")
                && nameDesc == "onChildClick(Landroid/widget/ExpandableListView;Landroid/view/View;IIJ)Z"
            ) {
                val sensorsAnalyticsMethodCell: SensorsAnalyticsMethodCell? =
                    SensorsAnalyticsHookConfig.INTERFACE_METHODS
                        .get("android/widget/ExpandableListView\$OnChildClickListeneronChildClick(Landroid/widget/ExpandableListView;Landroid/view/View;IIJ)Z")
                if (sensorsAnalyticsMethodCell != null) {
                    mMethodVisitor.visitVarInsn(ALOAD, localIds!![0])
                    mMethodVisitor.visitVarInsn(ALOAD, localIds!![1])
                    mMethodVisitor.visitVarInsn(ILOAD, localIds!![2])
                    mMethodVisitor.visitVarInsn(ILOAD, localIds!![3])
                    mMethodVisitor.visitMethodInsn(
                        INVOKESTATIC,
                        SensorsAnalyticsHookConfig.SENSORS_ANALYTICS_API,
                        sensorsAnalyticsMethodCell.agentName,
                        sensorsAnalyticsMethodCell.agentDesc,
                        false
                    )
                    isHasTracked = true
                    return
                }
            } else if (nameDesc == "onMenuItemClick(Landroid/view/MenuItem;)Z") {
                for (interfaceName in classNameAnalytics.interfaces) {
                    val sensorsAnalyticsMethodCell: SensorsAnalyticsMethodCell? =
                        SensorsAnalyticsHookConfig.INTERFACE_METHODS.get(interfaceName + nameDesc)
                    if (sensorsAnalyticsMethodCell != null) {
                        mMethodVisitor.visitVarInsn(ALOAD, localIds!![0])
                        mMethodVisitor.visitMethodInsn(
                            INVOKESTATIC,
                            SensorsAnalyticsHookConfig.SENSORS_ANALYTICS_API,
                            sensorsAnalyticsMethodCell.agentName,
                            sensorsAnalyticsMethodCell.agentDesc,
                            false
                        )
                        isHasTracked = true
                        return
                    }
                }
            } else if (classNameAnalytics.interfaces.contains("android/widget/SeekBar\$OnSeekBarChangeListener")
                && nameDesc == "onStopTrackingTouch(Landroid/widget/SeekBar;)V"
            ) {
                val sensorsAnalyticsMethodCell: SensorsAnalyticsMethodCell? =
                    SensorsAnalyticsHookConfig.INTERFACE_METHODS
                        .get("android/widget/SeekBar\$OnSeekBarChangeListeneronStopTrackingTouch(Landroid/widget/SeekBar;)V")
                if (sensorsAnalyticsMethodCell != null) {
                    mMethodVisitor.visitVarInsn(ALOAD, localIds!![0])
                    mMethodVisitor.visitMethodInsn(
                        INVOKESTATIC,
                        SensorsAnalyticsHookConfig.SENSORS_ANALYTICS_API,
                        sensorsAnalyticsMethodCell.agentName,
                        sensorsAnalyticsMethodCell.agentDesc,
                        false
                    )
                    isHasTracked = true
                    return
                }
            } else {
                for (interfaceName in classNameAnalytics.interfaces) {
                    val sensorsAnalyticsMethodCell: SensorsAnalyticsMethodCell? =
                        SensorsAnalyticsHookConfig.INTERFACE_METHODS.get(interfaceName + nameDesc)
                    if (sensorsAnalyticsMethodCell != null) {
                        sensorsAnalyticsMethodCell.visitHookMethod(
                            mMethodVisitor,
                            INVOKESTATIC,
                            SensorsAnalyticsHookConfig.SENSORS_ANALYTICS_API
                        )
                        isHasTracked = true
                        return
                    }
                }
            }
        }
        handleClassMethod(classNameAnalytics.className, nameDesc)
        if (isOnClickMethod) {
            trackViewOnClick(mMethodVisitor, variableID)
            isHasTracked = true
        }
    }

    private fun handleClassMethod(className: String, nameDesc: String) {
        val sensorsAnalyticsMethodCell: SensorsAnalyticsMethodCell? =
            SensorsAnalyticsHookConfig.CLASS_METHODS.get(className + nameDesc)
        if (sensorsAnalyticsMethodCell != null) {
            sensorsAnalyticsMethodCell.visitHookMethod(
                mMethodVisitor,
                INVOKESTATIC,
                SensorsAnalyticsHookConfig.SENSORS_ANALYTICS_API
            )
            isHasTracked = true
        }
    }

    private fun handleRN(): Boolean {
        var result = false
        if (classNameAnalytics.superClass == "com/facebook/react/uimanager/ViewGroupManager"
            && nameDesc == "addView(Landroid/view/ViewGroup;Landroid/view/View;I)V" ) {
            mMethodVisitor.visitVarInsn(ALOAD, 2)
            mMethodVisitor.visitVarInsn(ILOAD, 3)
            mMethodVisitor.visitMethodInsn(
                INVOKESTATIC,
                "com/sensorsdata/analytics/RNAgent",
                "addView",
                "(Landroid/view/View;I)V",
                false
            )
            result = true
        }
        if (nameDesc == "handleTouchEvent(Landroid/view/MotionEvent;Lcom/facebook/react/uimanager/events/EventDispatcher;)V"
            && classNameAnalytics.className == "com/facebook/react/uimanager/JSTouchDispatcher") {
            mMethodVisitor.visitVarInsn(ALOAD, 0)
            mMethodVisitor.visitVarInsn(ALOAD, 1)
            mMethodVisitor.visitVarInsn(ALOAD, 2)
            mMethodVisitor.visitMethodInsn(
                INVOKESTATIC,
                "com/sensorsdata/analytics/RNAgent",
                "handleTouchEvent",
                "(Lcom/facebook/react/uimanager/JSTouchDispatcher;Landroid/view/MotionEvent;Lcom/facebook/react/uimanager/events/EventDispatcher;)V",
                false
            )
            result = true
        }
        return result
    }

    private fun trackViewOnClick(mv: MethodVisitor, index: Int) {
        mv.visitVarInsn(ALOAD, index)
        mv.visitMethodInsn(
            INVOKESTATIC,
            SensorsAnalyticsHookConfig.SENSORS_ANALYTICS_API,
            "trackViewOnClick",
            "(Landroid/view/View;)V",
            false
        )
    }

    /**
     * 该方法是当扫描器扫描到类注解声明时进行调用
     *
     * @param s 注解的类型。它使用的是（“L” + “类型路径” + “;”）形式表述
     * @param b 表示的是，该注解是否在 JVM 中可见
     * 1.RetentionPolicy.SOURCE：声明注解只保留在 Java 源程序中，在编译 Java 类时注解信息不会被写入到 Class。如果使用的是这个配置 ASM 也将无法探测到这个注解。
     * 2.RetentionPolicy.CLASS：声明注解仅保留在 Class 文件中，JVM 运行时并不会处理它，这意味着 ASM 可以在 visitAnnotation 时候探测到它，但是通过Class 反射无法获取到注解信息。
     * 3.RetentionPolicy.RUNTIME：这是最常用的一种声明，ASM 可以探测到这个注解，同时 Java 反射也可以取得注解的信息。所有用到反射获取的注解都会用到这个配置，就是这个原因。
     * @return AnnotationVisitor
     */
    override fun visitAnnotation(s: String, b: Boolean): AnnotationVisitor {
        if (s == "Lcom/sensorsdata/analytics/android/sdk/SensorsDataTrackViewOnClick;") {
            isSensorsDataTrackViewOnClickAnnotation = true
        } else if (s == "Lcom/sensorsdata/analytics/android/sdk/SensorsDataIgnoreTrackOnClick;") {
            isSensorsDataIgnoreTrackOnClick = true
        } else if (s == "Lcom/sensorsdata/analytics/android/sdk/SensorsDataInstrumented;") {
            isHasInstrumented = true
        } else if (s == "Lcom/sensorsdata/analytics/android/sdk/SensorsDataTrackEvent;") {
            return object : AnnotationVisitor(pluginManager.getASMVersion()) {
                override fun visit(key: String, value: Any) {
                    super.visit(key, value)
                    if ("eventName" == key) {
                        eventName = value as String
                    } else if ("properties" == key) {
                        eventProperties = value.toString()
                    }
                }
            }
        }
        return super.visitAnnotation(s, b)
    }

    /**
     * 获取方法参数下标为 index 的对应 ASM index
     *
     * @param types          方法参数类型数组
     * @param index          方法中参数下标，从 0 开始
     * @param isStaticMethod 该方法是否为静态方法
     * @return 访问该方法的 index 位参数的 ASM index
     */
    fun getVisitPosition(types: Array<Type>?, index: Int, isStaticMethod: Boolean): Int {
        if (types == null || index < 0 || index >= types.size) {
            throw Error("getVisitPosition error")
        }
        return if (index == 0) {
            if (isStaticMethod) 0 else 1
        } else {
            getVisitPosition(types, index - 1, isStaticMethod) + types[index - 1].size
        }
    }
}