package com.sensorsdata.analytics.android.plugin.fragment;

import com.sensorsdata.analytics.android.plugin.SensorsAnalyticsUtil;
import com.sensorsdata.analytics.android.plugin.utils.ModuleUtils;
import com.sensorsdata.analytics.android.plugin.viewclick.SensorsAnalyticsMethodCell;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.HashSet;
import java.util.Map;

public class FragmentHookHelper {

    /**
     * add Fragment life callback
     */
    public static void hookFragment(ClassVisitor classVisitor, String superName, HashSet<String> visitedFragMethods) {
        if (SensorsAnalyticsUtil.isInstanceOfFragment(superName)) {
            MethodVisitor mv;
            // 添加剩下的方法，确保super.onHiddenChanged(hidden);等先被调用
            for (Map.Entry<String, SensorsAnalyticsMethodCell> entry : SensorsFragmentHookConfig.FRAGMENT_METHODS.entrySet()) {
                String key = entry.getKey();
                SensorsAnalyticsMethodCell methodCell = entry.getValue();
                if (visitedFragMethods.contains(key)) {
                    continue;
                }
                mv = classVisitor.visitMethod(Opcodes.ACC_PUBLIC, methodCell.getName(), methodCell.getDesc(), null, null);
                mv.visitCode();
                // call super
                methodCell.visitMethod(mv, Opcodes.INVOKESPECIAL, superName);
                // call injected method
                methodCell.visitHookMethod(mv, Opcodes.INVOKESTATIC, SensorsFragmentHookConfig.SENSORS_FRAGMENT_TRACK_HELPER_API);
                mv.visitInsn(Opcodes.RETURN);
                mv.visitMaxs(methodCell.getParamsCount(), methodCell.getParamsCount());
                mv.visitEnd();
                mv.visitAnnotation("Lcom/sensorsdata/analytics/android/sdk/SensorsDataInstrumented;", false);
            }
        }
    }
}
