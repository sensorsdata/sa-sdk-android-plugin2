/*
 * Created by zhangwei on 2021/08/09.
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

package com.sensorsdata.analytics.android.plugin.push;

import com.sensorsdata.analytics.android.plugin.SensorsAnalyticsUtil;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;

import java.util.ArrayList;
import java.util.List;

public class SensorsAnalyticsPushMethodVisitor extends AdviceAdapter {
    private static final String PENDING_INTENT_OWNER = "android/app/PendingIntent";
    private final String mNameDesc;
    private final MethodVisitor mMethodVisitor;
    private final String mSuperName;
    public SensorsAnalyticsPushMethodVisitor(MethodVisitor methodVisitor, int access, String name, String descriptor, String superName) {
        super(SensorsAnalyticsUtil.ASM_VERSION, methodVisitor, access, name, descriptor);
        this.mMethodVisitor = methodVisitor;
        mNameDesc = name + descriptor;
        mSuperName = superName;
    }

    @Override
    protected void onMethodEnter() {
        super.onMethodEnter();
        // Hook Push
        if (!SensorsAnalyticsUtil.isStatic(methodAccess)) {
            SensorsPushInjected.handlePush(mMethodVisitor, mSuperName, mNameDesc);
        }
    }

    @Override
    public void visitCode() {
        super.visitCode();
    }

    @Override
    public void visitMethodInsn(int opcodeAndSource, String owner, String name, String descriptor, boolean isInterface) {
        //PendingIntent.getActivity() before and after
        if (opcodeAndSource == Opcodes.INVOKESTATIC && PENDING_INTENT_OWNER.equals(owner) && checkPendingIntentName(name)) {
            Type[] argTypes = Type.getArgumentTypes(descriptor);
            List<Integer> positionList = new ArrayList<>();
            for (int index = argTypes.length - 1; index >= 0; index--) {
                int position = newLocal(argTypes[index]);
                storeLocal(position, argTypes[index]);
                positionList.add(0, position);
            }
            positionList.forEach(this::loadLocal);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, SensorsPushInjected.PUSH_TRACK_OWNER, getIntentHookMethodName(argTypes.length, name),
                    refactorHookBeforeMethodDescriptor(descriptor), false);
            positionList.forEach(this::loadLocal);
            super.visitMethodInsn(opcodeAndSource, owner, name, descriptor, isInterface);
            mv.visitInsn(Opcodes.DUP);
            positionList.forEach(this::loadLocal);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, SensorsPushInjected.PUSH_TRACK_OWNER, getPendingIntentHookMethodName(argTypes.length, name),
                    refactorHookAfterMethodDescriptor(descriptor), false);
            return;
        }

        //NotificationManager.notify()
        if (opcodeAndSource == Opcodes.INVOKEVIRTUAL && "android/app/NotificationManager".equals(owner) && "notify".equals(name)) {
            Type[] argTypes = Type.getArgumentTypes(descriptor);
            List<Integer> positionList = new ArrayList<>();
            for (int index = argTypes.length - 1; index >= 0; index--) {
                int position = newLocal(argTypes[index]);
                storeLocal(position, argTypes[index]);
                positionList.add(0, position);
            }
            mv.visitInsn(Opcodes.DUP);
            positionList.forEach(this::loadLocal);
            super.visitMethodInsn(opcodeAndSource, owner, name, descriptor, isInterface);
            positionList.forEach(this::loadLocal);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, SensorsPushInjected.PUSH_TRACK_OWNER, "onNotify",
                    SensorsAnalyticsUtil.appendDescBeforeGiven(descriptor, "Landroid/app/NotificationManager;"), false);
            return;
        }

        super.visitMethodInsn(opcodeAndSource, owner, name, descriptor, isInterface);
    }

    private boolean checkPendingIntentName(String methodName) {
        return "getActivity".equals(methodName) || "getService".equals(methodName) || "getBroadcast".equals(methodName) || "getForegroundService".equals(methodName);
    }

    private String refactorHookBeforeMethodDescriptor(String desc) {
        //change return type to void
        return desc.substring(0, desc.lastIndexOf(")") + 1) + "V";
    }

    private String refactorHookAfterMethodDescriptor(String desc) {
        //change return type to void and add PendingIntent prefix
        return "(Landroid/app/PendingIntent;" + desc.substring(1, desc.lastIndexOf(")") + 1) + "V";
    }

    private String getIntentHookMethodName(int argsLength, String name) {
        if ("getActivity".equals(name)) {
            return argsLength == 4 ? "hookIntentGetActivity" : "hookIntentGetActivityBundle";
        }
        return String.format("hookIntent%s", firstLetterUpper(name));
    }

    private String getPendingIntentHookMethodName(int argsLength, String name) {
        if ("getActivity".equals(name)) {
            return argsLength == 4 ? "hookPendingIntentGetActivity" : "hookPendingIntentGetActivityBundle";
        }
        return String.format("hookPendingIntent%s", firstLetterUpper(name));
    }

    private static String firstLetterUpper(String name) {
        char[] cs = name.toCharArray();
        cs[0] -= 32;
        return String.valueOf(cs);
    }
}
