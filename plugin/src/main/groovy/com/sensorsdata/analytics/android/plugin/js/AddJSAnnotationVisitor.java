package com.sensorsdata.analytics.android.plugin.js;


import com.sensorsdata.analytics.android.plugin.SensorsAnalyticsTransformHelper;
import com.sensorsdata.analytics.android.plugin.SensorsAnalyticsUtil;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.AdviceAdapter;

public class AddJSAnnotationVisitor extends AdviceAdapter {
    private final SensorsAnalyticsTransformHelper mTransformHelper;
    boolean shouldAddUCJS = false;
    boolean shouldAddXWalkJS = false;

    public AddJSAnnotationVisitor(MethodVisitor mv, int access, String name, String desc, SensorsAnalyticsTransformHelper transformHelper) {
        super(SensorsAnalyticsUtil.ASM_VERSION, mv, access, name, desc);
        this.mTransformHelper = transformHelper;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String s, boolean b) {
        if (s.equals("Landroid/webkit/JavascriptInterface;")) {
            shouldAddUCJS = mTransformHelper.getExtension().addUCJavaScriptInterface;
            shouldAddXWalkJS = mTransformHelper.getExtension().addXWalkJavaScriptInterface;
        }

        return super.visitAnnotation(s, b);
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
        if (shouldAddUCJS) {
            visitAnnotation("Lcom/uc/webview/export/JavascriptInterface;", true);
        }
        if (shouldAddXWalkJS) {
            visitAnnotation("Lorg/xwalk/core/JavascriptInterface;", true);
        }
    }
}
