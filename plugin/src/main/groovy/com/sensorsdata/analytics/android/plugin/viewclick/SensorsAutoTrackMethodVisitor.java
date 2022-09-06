package com.sensorsdata.analytics.android.plugin.viewclick;

import com.sensorsdata.analytics.android.plugin.ClassNameAnalytics;
import com.sensorsdata.analytics.android.plugin.utils.Logger;
import com.sensorsdata.analytics.android.plugin.SensorsAnalyticsHookConfig;
import com.sensorsdata.analytics.android.plugin.viewclick.SensorsAnalyticsMethodCell;
import com.sensorsdata.analytics.android.plugin.SensorsAnalyticsTransformHelper;
import com.sensorsdata.analytics.android.plugin.SensorsAnalyticsUtil;
import com.sensorsdata.analytics.android.plugin.fragment.SensorsFragmentHookConfig;
import com.sensorsdata.analytics.android.plugin.utils.VersionUtils;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class SensorsAutoTrackMethodVisitor extends AutoTrackMethodVisitor {
    boolean isSensorsDataTrackViewOnClickAnnotation = false;
    boolean isSensorsDataIgnoreTrackOnClick = false;
    String eventName = null;
    String eventProperties = "";
    boolean isHasInstrumented = false;
    boolean isHasTracked = false;
    int variableID = 0;
    //nameDesc是'onClick(Landroid/view/View;)V'字符串
    boolean isOnClickMethod = false;
    boolean isOnItemClickMethod = false;
    //name + desc
    String nameDesc;
    String name, desc;
    int access;
    //访问权限是public并且非静态
    boolean pubAndNoStaticAccess;
    boolean protectedAndNotStaticAccess;
    ArrayList<Integer> localIds;
    private final SensorsAnalyticsTransformHelper transformHelper;
    private final MethodVisitor mMethodVisitor;
    // 是否是 AndroidTV 版本
    private final boolean isAndroidTv;
    private final String mClassName;
    private final String mSuperName;
    private final ClassNameAnalytics mClassNameAnalytics;
    private final List<String> mInterfaces;
    private final HashMap<String, SensorsAnalyticsMethodCell> mLambdaMethodCells;
    private final HashSet<String> mVisitedFragMethods;// 无需判空

    public SensorsAutoTrackMethodVisitor(MethodVisitor mv, int access, String name, String desc, ClassNameAnalytics classNameAnalytics, SensorsAnalyticsTransformHelper transformHelper,
                                         String superClassName, String className, List<String> interfaces, HashSet<String> visitedFragMethods, HashMap<String, SensorsAnalyticsMethodCell> lambdaMethodCells) {
        super(mv, access, name, desc);
        this.name = name;
        this.desc = desc;
        this.access = access;
        this.nameDesc = name + desc;
        this.mMethodVisitor = mv;
        this.mClassName = className;
        this.mSuperName = superClassName;
        this.transformHelper = transformHelper;
        this.mVisitedFragMethods = visitedFragMethods;
        this.mClassNameAnalytics = classNameAnalytics;
        this.mInterfaces = interfaces;
        this.mLambdaMethodCells = lambdaMethodCells;
        isAndroidTv = VersionUtils.isTvVersion();
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
        if (isHasTracked) {
            if (transformHelper.getExtension().lambdaEnabled) {
                mLambdaMethodCells.remove(nameDesc);
            }
            visitAnnotation("Lcom/sensorsdata/analytics/android/sdk/SensorsDataInstrumented;", false);
            Logger.info("Hooked method: " + name + desc + "\n");
        }
    }

    @Override
    public void visitInvokeDynamicInsn(String name1, String desc1, Handle bsm, Object... bsmArgs) {
        super.visitInvokeDynamicInsn(name1, desc1, bsm, bsmArgs);
        if (!transformHelper.getExtension().lambdaEnabled) {
            return;
        }
        try {
            String desc2 = ((Type)bsmArgs[0]).getDescriptor();
            SensorsAnalyticsMethodCell sensorsAnalyticsMethodCell = SensorsAnalyticsHookConfig.LAMBDA_METHODS.get(Type.getReturnType(desc1).getDescriptor() + name1 + desc2);
            if (sensorsAnalyticsMethodCell != null) {
                Handle it = (Handle) bsmArgs[1];
                mLambdaMethodCells.put(it.getName() + it.getDesc(), sensorsAnalyticsMethodCell);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMethodEnter() {
        super.onMethodEnter();
        pubAndNoStaticAccess = SensorsAnalyticsUtil.isPublic(access) && !SensorsAnalyticsUtil.isStatic(access);
        protectedAndNotStaticAccess = SensorsAnalyticsUtil.isProtected(access) && !SensorsAnalyticsUtil.isStatic(access);
        if (pubAndNoStaticAccess) {
            if ((nameDesc.equals("onClick(Landroid/view/View;)V"))) {
                isOnClickMethod = true;
                variableID = newLocal(Type.getObjectType("java/lang/Integer"));
                mMethodVisitor.visitVarInsn(ALOAD, 1);
                mMethodVisitor.visitVarInsn(ASTORE, variableID);
            } else if (nameDesc.equals("onItemClick(Landroid/widget/AdapterView;Landroid/view/View;IJ)V")) {
                localIds = new ArrayList<>();
                isOnItemClickMethod = true;

                int first = newLocal(Type.getObjectType("android/widget/AdapterView"));
                mMethodVisitor.visitVarInsn(ALOAD, 1);
                mMethodVisitor.visitVarInsn(ASTORE, first);
                localIds.add(first);

                int second = newLocal(Type.getObjectType("android/view/View"));
                mMethodVisitor.visitVarInsn(ALOAD, 2);
                mMethodVisitor.visitVarInsn(ASTORE, second);
                localIds.add(second);

                int third = newLocal(Type.INT_TYPE);
                mMethodVisitor.visitVarInsn(ILOAD, 3);
                mMethodVisitor.visitVarInsn(ISTORE, third);
                localIds.add(third);
            } else if (SensorsAnalyticsUtil.isInstanceOfFragment(mSuperName)
                    && SensorsFragmentHookConfig.FRAGMENT_METHODS.get(nameDesc) != null) {
                SensorsAnalyticsMethodCell sensorsAnalyticsMethodCell = SensorsFragmentHookConfig.FRAGMENT_METHODS.get(nameDesc);
                localIds = new ArrayList<>();
                Type[] types = Type.getArgumentTypes(desc);
                for (int i = 1; i < sensorsAnalyticsMethodCell.getParamsCount(); i++) {
                    int localId = newLocal(types[i - 1]);
                    mMethodVisitor.visitVarInsn(sensorsAnalyticsMethodCell.getOpcodes().get(i), i);
                    mMethodVisitor.visitVarInsn(SensorsAnalyticsUtil.convertOpcodes(sensorsAnalyticsMethodCell.getOpcodes().get(i)), localId);
                    localIds.add(localId);
                }
            } else if (nameDesc.equals("onCheckedChanged(Landroid/widget/RadioGroup;I)V")) {
                localIds = new ArrayList<>();
                int firstLocalId = newLocal(Type.getObjectType("android/widget/RadioGroup"));
                mMethodVisitor.visitVarInsn(ALOAD, 1);
                mMethodVisitor.visitVarInsn(ASTORE, firstLocalId);
                localIds.add(firstLocalId);
                int secondLocalId = newLocal(Type.INT_TYPE);
                mMethodVisitor.visitVarInsn(ILOAD, 2);
                mMethodVisitor.visitVarInsn(ISTORE, secondLocalId);
                localIds.add(secondLocalId);
            } else if (nameDesc.equals("onCheckedChanged(Landroid/widget/CompoundButton;Z)V")) {
                localIds = new ArrayList<>();
                int firstLocalId = newLocal(Type.getObjectType("android/widget/CompoundButton"));
                mMethodVisitor.visitVarInsn(ALOAD, 1);
                mMethodVisitor.visitVarInsn(ASTORE, firstLocalId);
                localIds.add(firstLocalId);
            } else if (nameDesc.equals("onClick(Landroid/content/DialogInterface;I)V")) {
                localIds = new ArrayList<>();
                int firstLocalId = newLocal(Type.getObjectType("android/content/DialogInterface"));
                mMethodVisitor.visitVarInsn(ALOAD, 1);
                mMethodVisitor.visitVarInsn(ASTORE, firstLocalId);
                localIds.add(firstLocalId);
                int secondLocalId = newLocal(Type.INT_TYPE);
                mMethodVisitor.visitVarInsn(ILOAD, 2);
                mMethodVisitor.visitVarInsn(ISTORE, secondLocalId);
                localIds.add(secondLocalId);
            } else if (SensorsAnalyticsUtil.isTargetMenuMethodDesc(nameDesc)) {
                localIds = new ArrayList<>();
                int firstLocalId = newLocal(Type.getObjectType("java/lang/Object"));
                mMethodVisitor.visitVarInsn(ALOAD, 0);
                mMethodVisitor.visitVarInsn(ASTORE, firstLocalId);
                localIds.add(firstLocalId);
                int secondLocalId = newLocal(Type.getObjectType("android/view/MenuItem"));
                mMethodVisitor.visitVarInsn(ALOAD, 1);
                mMethodVisitor.visitVarInsn(ASTORE, secondLocalId);
                localIds.add(secondLocalId);
            } else if (nameDesc.equals("onMenuItemClick(Landroid/view/MenuItem;)Z")) {
                localIds = new ArrayList<>();
                int firstLocalId = newLocal(Type.getObjectType("android/view/MenuItem"));
                mMethodVisitor.visitVarInsn(ALOAD, 1);
                mMethodVisitor.visitVarInsn(ASTORE, firstLocalId);
                localIds.add(firstLocalId);
            } else if (nameDesc.equals("onGroupClick(Landroid/widget/ExpandableListView;Landroid/view/View;IJ)Z")) {
                localIds = new ArrayList<>();
                int firstLocalId = newLocal(Type.getObjectType("android/widget/ExpandableListView"));
                mMethodVisitor.visitVarInsn(ALOAD, 1);
                mMethodVisitor.visitVarInsn(ASTORE, firstLocalId);
                localIds.add(firstLocalId);

                int secondLocalId = newLocal(Type.getObjectType("android/view/View"));
                mMethodVisitor.visitVarInsn(ALOAD, 2);
                mMethodVisitor.visitVarInsn(ASTORE, secondLocalId);
                localIds.add(secondLocalId);

                int thirdLocalId = newLocal(Type.INT_TYPE);
                mMethodVisitor.visitVarInsn(ILOAD, 3);
                mMethodVisitor.visitVarInsn(ISTORE, thirdLocalId);
                localIds.add(thirdLocalId);
            } else if (nameDesc.equals("onChildClick(Landroid/widget/ExpandableListView;Landroid/view/View;IIJ)Z")) {
                localIds = new ArrayList<>();
                int firstLocalId = newLocal(Type.getObjectType("android/widget/ExpandableListView"));
                mMethodVisitor.visitVarInsn(ALOAD, 1);
                mMethodVisitor.visitVarInsn(ASTORE, firstLocalId);
                localIds.add(firstLocalId);

                int secondLocalId = newLocal(Type.getObjectType("android/view/View"));
                mMethodVisitor.visitVarInsn(ALOAD, 2);
                mMethodVisitor.visitVarInsn(ASTORE, secondLocalId);
                localIds.add(secondLocalId);

                int thirdLocalId = newLocal(Type.INT_TYPE);
                mMethodVisitor.visitVarInsn(ILOAD, 3);
                mMethodVisitor.visitVarInsn(ISTORE, thirdLocalId);
                localIds.add(thirdLocalId);

                int fourthLocalId = newLocal(Type.INT_TYPE);
                mMethodVisitor.visitVarInsn(ILOAD, 4);
                mMethodVisitor.visitVarInsn(ISTORE, fourthLocalId);
                localIds.add(fourthLocalId);
            } else if (nameDesc.equals("onItemSelected(Landroid/widget/AdapterView;Landroid/view/View;IJ)V")
                    || nameDesc.equals("onListItemClick(Landroid/widget/ListView;Landroid/view/View;IJ)V")) {
                localIds = new ArrayList<>();
                int firstLocalId = newLocal(Type.getObjectType("java/lang/Object"));
                mMethodVisitor.visitVarInsn(ALOAD, 1);
                mMethodVisitor.visitVarInsn(ASTORE, firstLocalId);
                localIds.add(firstLocalId);

                int secondLocalId = newLocal(Type.getObjectType("android/view/View"));
                mMethodVisitor.visitVarInsn(ALOAD, 2);
                mMethodVisitor.visitVarInsn(ASTORE, secondLocalId);
                localIds.add(secondLocalId);

                int thirdLocalId = newLocal(Type.INT_TYPE);
                mMethodVisitor.visitVarInsn(ILOAD, 3);
                mMethodVisitor.visitVarInsn(ISTORE, thirdLocalId);
                localIds.add(thirdLocalId);
            } else if (nameDesc.equals("onStopTrackingTouch(Landroid/widget/SeekBar;)V")) {
                localIds = new ArrayList<>();
                int firstLocalId = newLocal(Type.getObjectType("android/widget/SeekBar"));
                mMethodVisitor.visitVarInsn(ALOAD, 1);
                mMethodVisitor.visitVarInsn(ASTORE, firstLocalId);
                localIds.add(firstLocalId);
            }
        } else if (protectedAndNotStaticAccess) {
            if (nameDesc.equals("onListItemClick(Landroid/widget/ListView;Landroid/view/View;IJ)V")) {
                localIds = new ArrayList<>();
                int firstLocalId = newLocal(Type.getObjectType("java/lang/Object"));
                mMethodVisitor.visitVarInsn(ALOAD, 1);
                mMethodVisitor.visitVarInsn(ASTORE, firstLocalId);
                localIds.add(firstLocalId);

                int secondLocalId = newLocal(Type.getObjectType("android/view/View"));
                mMethodVisitor.visitVarInsn(ALOAD, 2);
                mMethodVisitor.visitVarInsn(ASTORE, secondLocalId);
                localIds.add(secondLocalId);

                int thirdLocalId = newLocal(Type.INT_TYPE);
                mMethodVisitor.visitVarInsn(ILOAD, 3);
                mMethodVisitor.visitVarInsn(ISTORE, thirdLocalId);
                localIds.add(thirdLocalId);
            }
        }

        // Lambda 参数优化部分，对现有参数进行复制
        if (transformHelper.getExtension().lambdaEnabled) {
            SensorsAnalyticsMethodCell lambdaMethodCell = mLambdaMethodCells.get(nameDesc);
            if (lambdaMethodCell != null) {
                //判断是否是在采样中，在采样中才会处理或者开关打开也统一处理
                if (transformHelper.getExtension().lambdaParamOptimize || SensorsAnalyticsHookConfig.SAMPLING_LAMBDA_METHODS.contains(lambdaMethodCell)) {
                    Type[] types = Type.getArgumentTypes(lambdaMethodCell.getDesc());
                    int length = types.length;
                    Type[] lambdaTypes = Type.getArgumentTypes(desc);
                    // paramStart 为访问的方法参数的下标，从 0 开始
                    int paramStart = lambdaTypes.length - length;
                    if (paramStart < 0) {
                        return;
                    } else {
                        for (int i = 0; i < length; i++) {
                            if (!lambdaTypes[paramStart + i].getDescriptor().equals(types[i].getDescriptor())) {
                                return;
                            }
                        }
                    }
                    boolean isStaticMethod = SensorsAnalyticsUtil.isStatic(access);
                    localIds = new ArrayList<>();
                    for (int i = paramStart; i < paramStart + lambdaMethodCell.getParamsCount(); i++) {
                        int localId = newLocal(types[i - paramStart]);
                        mMethodVisitor.visitVarInsn(lambdaMethodCell.getOpcodes().get(i - paramStart), getVisitPosition(lambdaTypes, i, isStaticMethod));
                        mMethodVisitor.visitVarInsn(SensorsAnalyticsUtil.convertOpcodes(lambdaMethodCell.getOpcodes().get(i - paramStart)), localId);
                        localIds.add(localId);
                    }
                }
            }
        }

        if (transformHelper.isHookOnMethodEnter) {
            handleCode();
        }
    }

    @Override
    public void onMethodExit(int opcode) {
        super.onMethodExit(opcode);
        if (!transformHelper.isHookOnMethodEnter) {
            handleCode();
        }
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String fieldName, String fieldDesc) {
        if (mClassNameAnalytics.isKeyboardViewUtil && !transformHelper.getExtension().disableTrackKeyboard && "isSensorsCheckKeyboard".equals(fieldName) && opcode == PUTSTATIC) {
            mMethodVisitor.visitInsn(ICONST_0);
        }
        super.visitFieldInsn(opcode, owner, fieldName, fieldDesc);
    }

    void handleCode() {
        if (isHasInstrumented || mClassNameAnalytics.isSensorsDataAPI) {
            return;
        }

        /*
         * Fragment
         * 目前支持以下 Fragment 页面浏览事件：
         * android/app/Fragment，android/app/ListFragment， android/app/DialogFragment，
         * android/support/v4/app/Fragment，android/support/v4/app/ListFragment，android/support/v4/app/DialogFragment，
         * androidx/appcompat/app/AppCompatDialogFragment
         * androidx/fragment/app/Fragment，androidx/fragment/app/ListFragment，androidx/fragment/app/DialogFragment
         * com/google/android/material/bottomsheet/BottomSheetDialogFragment
         */
        if (SensorsAnalyticsUtil.isInstanceOfFragment(mSuperName)) {
            SensorsAnalyticsMethodCell sensorsAnalyticsMethodCell = SensorsFragmentHookConfig.FRAGMENT_METHODS.get(nameDesc);
            if (sensorsAnalyticsMethodCell != null) {
                mVisitedFragMethods.add(nameDesc);
                mMethodVisitor.visitVarInsn(ALOAD, 0);
                for (int i = 1; i < sensorsAnalyticsMethodCell.getParamsCount(); i++) {
                    mMethodVisitor.visitVarInsn(sensorsAnalyticsMethodCell.getOpcodes().get(i), localIds.get(i - 1));
                }
                mMethodVisitor.visitMethodInsn(INVOKESTATIC, SensorsFragmentHookConfig.SENSORS_FRAGMENT_TRACK_HELPER_API, sensorsAnalyticsMethodCell.getAgentName(), sensorsAnalyticsMethodCell.getAgentDesc(), false);
                isHasTracked = true;
                return;
            }
        }

        if (isSensorsDataIgnoreTrackOnClick) {
            return;
        }

        /*
         * 在 android.gradle 的 3.2.1 版本中，针对 view 的 setOnClickListener 方法 的 lambda 表达式做特殊处理。
         */
        if (transformHelper.getExtension().lambdaEnabled) {
            SensorsAnalyticsMethodCell lambdaMethodCell = mLambdaMethodCells.get(nameDesc);
            if (lambdaMethodCell != null) {
                Type[] types = Type.getArgumentTypes(lambdaMethodCell.getDesc());
                int length = types.length;
                Type[] lambdaTypes = Type.getArgumentTypes(desc);
                // paramStart 为访问的方法参数的下标，从 0 开始
                int paramStart = lambdaTypes.length - length;
                if (paramStart < 0) {
                    return;
                } else {
                    for (int i = 0; i < length; i++) {
                        if (!lambdaTypes[paramStart + i].getDescriptor().equals(types[i].getDescriptor())) {
                            return;
                        }
                    }
                }
                boolean isStaticMethod = SensorsAnalyticsUtil.isStatic(access);
                if (!isStaticMethod) {
                    if (lambdaMethodCell.getDesc().equals("(Landroid/view/MenuItem;)Z")) {
                        mMethodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
                        mMethodVisitor.visitVarInsn(Opcodes.ALOAD, getVisitPosition(lambdaTypes, paramStart, isStaticMethod));
                        mMethodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, SensorsAnalyticsHookConfig.SENSORS_ANALYTICS_API, lambdaMethodCell.getAgentName(), "(Ljava/lang/Object;Landroid/view/MenuItem;)V", false);
                        isHasTracked = true;
                        return;
                    }
                }
                //如果在采样中，就按照最新的处理流程来操作
                if (transformHelper.getExtension().lambdaParamOptimize || SensorsAnalyticsHookConfig.SAMPLING_LAMBDA_METHODS.contains(lambdaMethodCell)) {
                    for (int i = paramStart; i < paramStart + lambdaMethodCell.getParamsCount(); i++) {
                        mMethodVisitor.visitVarInsn(lambdaMethodCell.getOpcodes().get(i - paramStart), localIds.get(i - paramStart));
                    }
                } else {
                    for (int i = paramStart; i < paramStart + lambdaMethodCell.getParamsCount(); i++) {
                        mMethodVisitor.visitVarInsn(lambdaMethodCell.getOpcodes().get(i - paramStart), getVisitPosition(lambdaTypes, i, isStaticMethod));
                    }
                }
                mMethodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, SensorsAnalyticsHookConfig.SENSORS_ANALYTICS_API, lambdaMethodCell.getAgentName(), lambdaMethodCell.getAgentDesc(), false);
                isHasTracked = true;
                return;
            }
        }

        if (!pubAndNoStaticAccess) {
            //如果是 protected 那么也需要处理
            if (protectedAndNotStaticAccess) {
                if (nameDesc.equals("onListItemClick(Landroid/widget/ListView;Landroid/view/View;IJ)V")) {
                    mMethodVisitor.visitVarInsn(ALOAD, localIds.get(0));
                    mMethodVisitor.visitVarInsn(ALOAD, localIds.get(1));
                    mMethodVisitor.visitVarInsn(ILOAD, localIds.get(2));
                    mMethodVisitor.visitMethodInsn(INVOKESTATIC, SensorsAnalyticsHookConfig.SENSORS_ANALYTICS_API, "trackListView", "(Landroid/widget/AdapterView;Landroid/view/View;I)V", false);
                    isHasTracked = true;
                    return;
                }
            }
            return;
        }

        if (isAndroidTv && SensorsAnalyticsUtil.isInstanceOfActivity(mSuperName) && nameDesc.equals("dispatchKeyEvent(Landroid/view/KeyEvent;)Z")) {
            mMethodVisitor.visitVarInsn(ALOAD, 0);
            mMethodVisitor.visitVarInsn(ALOAD, 1);
            mMethodVisitor.visitMethodInsn(INVOKESTATIC, SensorsAnalyticsHookConfig.SENSORS_ANALYTICS_API, "trackViewOnClick", "(Landroid/app/Activity;Landroid/view/KeyEvent;)V", false);
            isHasTracked = true;
            return;
        }

        if (handleRN()) {
            isHasTracked = true;
            return;
        }

        /*
         * Menu
         * 目前支持 onContextItemSelected(MenuItem item)、onOptionsItemSelected(MenuItem item)
         */
        if (SensorsAnalyticsUtil.isTargetMenuMethodDesc(nameDesc)) {
            mMethodVisitor.visitVarInsn(ALOAD, localIds.get(0));
            mMethodVisitor.visitVarInsn(ALOAD, localIds.get(1));
            mMethodVisitor.visitMethodInsn(INVOKESTATIC, SensorsAnalyticsHookConfig.SENSORS_ANALYTICS_API, "trackMenuItem", "(Ljava/lang/Object;Landroid/view/MenuItem;)V", false);
            isHasTracked = true;
            return;
        }

        if (nameDesc.equals("onDrawerOpened(Landroid/view/View;)V")) {
            mMethodVisitor.visitVarInsn(ALOAD, 1);
            mMethodVisitor.visitMethodInsn(INVOKESTATIC, SensorsAnalyticsHookConfig.SENSORS_ANALYTICS_API, "trackDrawerOpened", "(Landroid/view/View;)V", false);
            isHasTracked = true;
            return;
        } else if (nameDesc.equals("onDrawerClosed(Landroid/view/View;)V")) {
            mMethodVisitor.visitVarInsn(ALOAD, 1);
            mMethodVisitor.visitMethodInsn(INVOKESTATIC, SensorsAnalyticsHookConfig.SENSORS_ANALYTICS_API, "trackDrawerClosed", "(Landroid/view/View;)V", false);
            isHasTracked = true;
            return;
        }

        if (isOnClickMethod && mClassName.equals("android/databinding/generated/callback/OnClickListener")) {
            trackViewOnClick(mMethodVisitor, 1);
            isHasTracked = true;
            return;
        }

        if (!SensorsAnalyticsUtil.isTargetClassInSpecial(mClassName)) {
            if ((mClassName.startsWith("android/") || mClassName.startsWith("androidx/")) && !(mClassName.startsWith("android/support/v17/leanback") || mClassName.startsWith("androidx/leanback"))) {
                return;
            }
        }

        if (nameDesc.equals("onItemSelected(Landroid/widget/AdapterView;Landroid/view/View;IJ)V") || nameDesc.equals("onListItemClick(Landroid/widget/ListView;Landroid/view/View;IJ)V")) {
            mMethodVisitor.visitVarInsn(ALOAD, localIds.get(0));
            mMethodVisitor.visitVarInsn(ALOAD, localIds.get(1));
            mMethodVisitor.visitVarInsn(ILOAD, localIds.get(2));
            mMethodVisitor.visitMethodInsn(INVOKESTATIC, SensorsAnalyticsHookConfig.SENSORS_ANALYTICS_API, "trackListView", "(Landroid/widget/AdapterView;Landroid/view/View;I)V", false);
            isHasTracked = true;
            return;
        }

        if (isSensorsDataTrackViewOnClickAnnotation && desc.equals("(Landroid/view/View;)V")) {
            trackViewOnClick(mMethodVisitor, 1);
            isHasTracked = true;
            return;
        }

        if (eventName != null && eventName.length() != 0) {
            mMethodVisitor.visitLdcInsn(eventName);
            mMethodVisitor.visitLdcInsn(eventProperties);
            mMethodVisitor.visitMethodInsn(INVOKESTATIC, SensorsAnalyticsHookConfig.SENSORS_ANALYTICS_API, "track", "(Ljava/lang/String;Ljava/lang/String;)V", false);
            isHasTracked = true;
            return;
        }

        if (mInterfaces != null && mInterfaces.size() > 0) {
            if (isOnItemClickMethod && mInterfaces.contains("android/widget/AdapterView$OnItemClickListener")) {
                mMethodVisitor.visitVarInsn(ALOAD, localIds.get(0));
                mMethodVisitor.visitVarInsn(ALOAD, localIds.get(1));
                mMethodVisitor.visitVarInsn(ILOAD, localIds.get(2));
                mMethodVisitor.visitMethodInsn(INVOKESTATIC, SensorsAnalyticsHookConfig.SENSORS_ANALYTICS_API, "trackListView", "(Landroid/widget/AdapterView;Landroid/view/View;I)V", false);
                isHasTracked = true;
                return;
            } else if (mInterfaces.contains("android/widget/RadioGroup$OnCheckedChangeListener")
                    && nameDesc.equals("onCheckedChanged(Landroid/widget/RadioGroup;I)V")) {
                SensorsAnalyticsMethodCell sensorsAnalyticsMethodCell = SensorsAnalyticsHookConfig.INTERFACE_METHODS
                        .get("android/widget/RadioGroup$OnCheckedChangeListeneronCheckedChanged(Landroid/widget/RadioGroup;I)V");
                if (sensorsAnalyticsMethodCell != null) {
                    mMethodVisitor.visitVarInsn(ALOAD, localIds.get(0));
                    mMethodVisitor.visitVarInsn(ILOAD, localIds.get(1));
                    mMethodVisitor.visitMethodInsn(INVOKESTATIC, SensorsAnalyticsHookConfig.SENSORS_ANALYTICS_API, sensorsAnalyticsMethodCell.getAgentName(), sensorsAnalyticsMethodCell.getAgentDesc(), false);
                    isHasTracked = true;
                    return;
                }
            } else if (mInterfaces.contains("android/widget/CompoundButton$OnCheckedChangeListener")
                    && nameDesc.equals("onCheckedChanged(Landroid/widget/CompoundButton;Z)V")) {
                SensorsAnalyticsMethodCell sensorsAnalyticsMethodCell = SensorsAnalyticsHookConfig.INTERFACE_METHODS
                        .get("android/widget/CompoundButton$OnCheckedChangeListeneronCheckedChanged(Landroid/widget/CompoundButton;Z)V");
                if (sensorsAnalyticsMethodCell != null) {
                    mMethodVisitor.visitVarInsn(ALOAD, localIds.get(0));
                    mMethodVisitor.visitMethodInsn(INVOKESTATIC, SensorsAnalyticsHookConfig.SENSORS_ANALYTICS_API, sensorsAnalyticsMethodCell.getAgentName(), sensorsAnalyticsMethodCell.getAgentDesc(), false);
                    isHasTracked = true;
                    return;
                }
            } else if (mInterfaces.contains("android/content/DialogInterface$OnClickListener")
                    && nameDesc.equals("onClick(Landroid/content/DialogInterface;I)V")) {
                SensorsAnalyticsMethodCell sensorsAnalyticsMethodCell = SensorsAnalyticsHookConfig.INTERFACE_METHODS
                        .get("android/content/DialogInterface$OnClickListeneronClick(Landroid/content/DialogInterface;I)V");
                if (sensorsAnalyticsMethodCell != null) {
                    mMethodVisitor.visitVarInsn(ALOAD, localIds.get(0));
                    mMethodVisitor.visitVarInsn(ILOAD, localIds.get(1));
                    mMethodVisitor.visitMethodInsn(INVOKESTATIC, SensorsAnalyticsHookConfig.SENSORS_ANALYTICS_API, sensorsAnalyticsMethodCell.getAgentName(), sensorsAnalyticsMethodCell.getAgentDesc(), false);
                    isHasTracked = true;
                    return;
                }
            } else if (mInterfaces.contains("android/widget/ExpandableListView$OnGroupClickListener")
                    && nameDesc.equals("onGroupClick(Landroid/widget/ExpandableListView;Landroid/view/View;IJ)Z")) {
                SensorsAnalyticsMethodCell sensorsAnalyticsMethodCell = SensorsAnalyticsHookConfig.INTERFACE_METHODS
                        .get("android/widget/ExpandableListView$OnGroupClickListeneronGroupClick(Landroid/widget/ExpandableListView;Landroid/view/View;IJ)Z");
                if (sensorsAnalyticsMethodCell != null) {
                    mMethodVisitor.visitVarInsn(ALOAD, localIds.get(0));
                    mMethodVisitor.visitVarInsn(ALOAD, localIds.get(1));
                    mMethodVisitor.visitVarInsn(ILOAD, localIds.get(2));
                    mMethodVisitor.visitMethodInsn(INVOKESTATIC, SensorsAnalyticsHookConfig.SENSORS_ANALYTICS_API, sensorsAnalyticsMethodCell.getAgentName(), sensorsAnalyticsMethodCell.getAgentDesc(), false);
                    isHasTracked = true;
                    return;
                }
            } else if (mInterfaces.contains("android/widget/ExpandableListView$OnChildClickListener")
                    && nameDesc.equals("onChildClick(Landroid/widget/ExpandableListView;Landroid/view/View;IIJ)Z")) {
                SensorsAnalyticsMethodCell sensorsAnalyticsMethodCell = SensorsAnalyticsHookConfig.INTERFACE_METHODS
                        .get("android/widget/ExpandableListView$OnChildClickListeneronChildClick(Landroid/widget/ExpandableListView;Landroid/view/View;IIJ)Z");
                if (sensorsAnalyticsMethodCell != null) {
                    mMethodVisitor.visitVarInsn(ALOAD, localIds.get(0));
                    mMethodVisitor.visitVarInsn(ALOAD, localIds.get(1));
                    mMethodVisitor.visitVarInsn(ILOAD, localIds.get(2));
                    mMethodVisitor.visitVarInsn(ILOAD, localIds.get(3));
                    mMethodVisitor.visitMethodInsn(INVOKESTATIC, SensorsAnalyticsHookConfig.SENSORS_ANALYTICS_API, sensorsAnalyticsMethodCell.getAgentName(), sensorsAnalyticsMethodCell.getAgentDesc(), false);
                    isHasTracked = true;
                    return;
                }
            } else if (nameDesc.equals("onMenuItemClick(Landroid/view/MenuItem;)Z")) {
                for (String interfaceName : mInterfaces) {
                    SensorsAnalyticsMethodCell sensorsAnalyticsMethodCell = SensorsAnalyticsHookConfig.INTERFACE_METHODS.get(interfaceName + nameDesc);
                    if (sensorsAnalyticsMethodCell != null) {
                        mMethodVisitor.visitVarInsn(ALOAD, localIds.get(0));
                        mMethodVisitor.visitMethodInsn(INVOKESTATIC, SensorsAnalyticsHookConfig.SENSORS_ANALYTICS_API, sensorsAnalyticsMethodCell.getAgentName(), sensorsAnalyticsMethodCell.getAgentDesc(), false);
                        isHasTracked = true;
                        return;
                    }
                }
            } else if (mInterfaces.contains("android/widget/SeekBar$OnSeekBarChangeListener")
                    && nameDesc.equals("onStopTrackingTouch(Landroid/widget/SeekBar;)V")) {
                SensorsAnalyticsMethodCell sensorsAnalyticsMethodCell = SensorsAnalyticsHookConfig.INTERFACE_METHODS
                        .get("android/widget/SeekBar$OnSeekBarChangeListeneronStopTrackingTouch(Landroid/widget/SeekBar;)V");
                if (sensorsAnalyticsMethodCell != null) {
                    mMethodVisitor.visitVarInsn(ALOAD, localIds.get(0));
                    mMethodVisitor.visitMethodInsn(INVOKESTATIC, SensorsAnalyticsHookConfig.SENSORS_ANALYTICS_API, sensorsAnalyticsMethodCell.getAgentName(), sensorsAnalyticsMethodCell.getAgentDesc(), false);
                    isHasTracked = true;
                    return;
                }
            } else {
                for (String interfaceName : mInterfaces) {
                    SensorsAnalyticsMethodCell sensorsAnalyticsMethodCell = SensorsAnalyticsHookConfig.INTERFACE_METHODS.get(interfaceName + nameDesc);
                    if (sensorsAnalyticsMethodCell != null) {
                        sensorsAnalyticsMethodCell.visitHookMethod(mMethodVisitor, INVOKESTATIC, SensorsAnalyticsHookConfig.SENSORS_ANALYTICS_API);
                        isHasTracked = true;
                        return;
                    }
                }
            }
        }
        handleClassMethod(mClassName, nameDesc);
        if (isOnClickMethod) {
            trackViewOnClick(mMethodVisitor, variableID);
            isHasTracked = true;
        }
    }

    void handleClassMethod(String className, String nameDesc) {
        SensorsAnalyticsMethodCell sensorsAnalyticsMethodCell = SensorsAnalyticsHookConfig.CLASS_METHODS.get(className + nameDesc);
        if (sensorsAnalyticsMethodCell != null) {
            sensorsAnalyticsMethodCell.visitHookMethod(mMethodVisitor, INVOKESTATIC, SensorsAnalyticsHookConfig.SENSORS_ANALYTICS_API);
            isHasTracked = true;
        }
    }

    boolean handleRN() {
        boolean result = false;
        switch (transformHelper.rnState) {
            case NOT_FOUND:
                break;
            case HAS_VERSION:
                if (SensorsAnalyticsUtil.compareVersion(transformHelper.rnVersion, "2.0.0") > 0 && mSuperName.equals("com/facebook/react/uimanager/ViewGroupManager")
                        && nameDesc.equals("addView(Landroid/view/ViewGroup;Landroid/view/View;I)V")) {
                    mMethodVisitor.visitVarInsn(ALOAD, 2);
                    mMethodVisitor.visitVarInsn(ILOAD, 3);
                    mMethodVisitor.visitMethodInsn(INVOKESTATIC, "com/sensorsdata/analytics/RNAgent", "addView", "(Landroid/view/View;I)V", false);
                    result = true;
                }

                if (nameDesc.equals("handleTouchEvent(Landroid/view/MotionEvent;Lcom/facebook/react/uimanager/events/EventDispatcher;)V' && mClassName == 'com/facebook/react/uimanager/JSTouchDispatcher")) {
                    mMethodVisitor.visitVarInsn(ALOAD, 0);
                    mMethodVisitor.visitVarInsn(ALOAD, 1);
                    mMethodVisitor.visitVarInsn(ALOAD, 2);
                    mMethodVisitor.visitMethodInsn(INVOKESTATIC, "com/sensorsdata/analytics/RNAgent", "handleTouchEvent", "(Lcom/facebook/react/uimanager/JSTouchDispatcher;Landroid/view/MotionEvent;Lcom/facebook/react/uimanager/events/EventDispatcher;)V", false);
                    result = true;
                }
                break;
            case NO_VERSION:
                if (nameDesc.equals("setJSResponder(IIZ)V") && mClassName.equals("com/facebook/react/uimanager/NativeViewHierarchyManager")) {
                    mMethodVisitor.visitVarInsn(ALOAD, 0);
                    mMethodVisitor.visitVarInsn(ILOAD, 1);
                    mMethodVisitor.visitVarInsn(ILOAD, 2);
                    mMethodVisitor.visitVarInsn(ILOAD, 3);
                    mMethodVisitor.visitMethodInsn(INVOKESTATIC, SensorsAnalyticsHookConfig.SENSORS_ANALYTICS_API, "trackRN", "(Ljava/lang/Object;IIZ)V", false);
                    result = true;
                }
                break;
        }
        return result;
    }

    void trackViewOnClick(MethodVisitor mv, int index) {
        mv.visitVarInsn(ALOAD, index);
        mv.visitMethodInsn(INVOKESTATIC, SensorsAnalyticsHookConfig.SENSORS_ANALYTICS_API, "trackViewOnClick", "(Landroid/view/View;)V", false);
    }

    /**
     * 该方法是当扫描器扫描到类注解声明时进行调用
     *
     * @param s 注解的类型。它使用的是（“L” + “类型路径” + “;”）形式表述
     * @param b 表示的是，该注解是否在 JVM 中可见
     *          1.RetentionPolicy.SOURCE：声明注解只保留在 Java 源程序中，在编译 Java 类时注解信息不会被写入到 Class。如果使用的是这个配置 ASM 也将无法探测到这个注解。
     *          2.RetentionPolicy.CLASS：声明注解仅保留在 Class 文件中，JVM 运行时并不会处理它，这意味着 ASM 可以在 visitAnnotation 时候探测到它，但是通过Class 反射无法获取到注解信息。
     *          3.RetentionPolicy.RUNTIME：这是最常用的一种声明，ASM 可以探测到这个注解，同时 Java 反射也可以取得注解的信息。所有用到反射获取的注解都会用到这个配置，就是这个原因。
     * @return AnnotationVisitor
     */
    @Override
    public AnnotationVisitor visitAnnotation(String s, boolean b) {
        if (s.equals("Lcom/sensorsdata/analytics/android/sdk/SensorsDataTrackViewOnClick;")) {
            isSensorsDataTrackViewOnClickAnnotation = true;
            Logger.info("发现 " + nameDesc + " 有注解 @SensorsDataTrackViewOnClick");
        } else if (s.equals("Lcom/sensorsdata/analytics/android/sdk/SensorsDataIgnoreTrackOnClick;")) {
            isSensorsDataIgnoreTrackOnClick = true;
            Logger.info("发现 " + nameDesc + " 有注解 @SensorsDataIgnoreTrackOnClick");
        } else if (s.equals("Lcom/sensorsdata/analytics/android/sdk/SensorsDataInstrumented;")) {
            isHasInstrumented = true;
        } else if (s.equals("Lcom/sensorsdata/analytics/android/sdk/SensorsDataTrackEvent;")) {
            return new AnnotationVisitor(SensorsAnalyticsUtil.ASM_VERSION) {
                @Override
                public void visit(String key, Object value) {
                    super.visit(key, value);
                    if ("eventName".equals(key)) {
                        eventName = (String) value;
                    } else if ("properties".equals(key)) {
                        eventProperties = value.toString();
                    }
                }
            };
        }

        return super.visitAnnotation(s, b);
    }

    /**
     * 获取方法参数下标为 index 的对应 ASM index
     *
     * @param types          方法参数类型数组
     * @param index          方法中参数下标，从 0 开始
     * @param isStaticMethod 该方法是否为静态方法
     * @return 访问该方法的 index 位参数的 ASM index
     */
    int getVisitPosition(Type[] types, int index, boolean isStaticMethod) {
        if (types == null || index < 0 || index >= types.length) {
            throw new Error("getVisitPosition error");
        }
        if (index == 0) {
            return isStaticMethod ? 0 : 1;
        } else {
            return getVisitPosition(types, index - 1, isStaticMethod) + types[index - 1].getSize();
        }
    }
}
