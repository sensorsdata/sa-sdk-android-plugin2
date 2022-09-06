package com.sensorsdata.analytics.android.plugin.core;

import com.sensorsdata.analytics.android.plugin.ClassNameAnalytics;
import com.sensorsdata.analytics.android.plugin.SensorsAnalyticsTransform;
import com.sensorsdata.analytics.android.plugin.SensorsAnalyticsUtil;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.AdviceAdapter;

public class DefaultMethodVisitor extends AdviceAdapter {
    private final ClassNameAnalytics mClassNameAnalytics;
    private final MethodVisitor mMethodVisitor;

    public DefaultMethodVisitor(MethodVisitor mv, int access, String name, String desc, ClassNameAnalytics classNameAnalytics) {
        super(SensorsAnalyticsUtil.ASM_VERSION, mv, access, name, desc);
        this.mClassNameAnalytics = classNameAnalytics;
        this.mMethodVisitor = mv;
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String fieldName, String descriptor) {
        if (mClassNameAnalytics.isSensorsDataAPI && "ANDROID_PLUGIN_VERSION".equals(fieldName) && opcode == PUTSTATIC) {
            mMethodVisitor.visitLdcInsn(SensorsAnalyticsTransform.VERSION);
        }
        super.visitFieldInsn(opcode, owner, fieldName, descriptor);
    }
}
