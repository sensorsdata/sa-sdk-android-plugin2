package com.sensorsdata.analytics.android.plugin.hook;

import com.sensorsdata.analytics.android.plugin.Logger;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class SensorsPushInjected {
    /**
     * Hook 极光推送
     *
     * @param methodVisitor MethodVisitor
     * @param superName 父类名
     * @param className 类名
     * @param nameDesc 描述
     */
    public static void handleJPush(MethodVisitor methodVisitor, String superName, String className, String nameDesc) {
        try {
            if ("cn/jpush/android/service/JPushMessageReceiver".equals(superName)
                    && "onNotifyMessageOpened(Landroid/content/Context;Lcn/jpush/android/api/NotificationMessage;)V".equals(nameDesc)) {
                Logger.info("JPush hook " + className + "," + nameDesc);
                Label l1 = new Label();
                // 参数空判断
                methodVisitor.visitVarInsn(Opcodes.ALOAD, 1);
                methodVisitor.visitJumpInsn(Opcodes.IFNULL, l1);
                methodVisitor.visitVarInsn(Opcodes.ALOAD, 2);
                methodVisitor.visitJumpInsn(Opcodes.IFNULL, l1);
                // 读取参数
                methodVisitor.visitVarInsn(Opcodes.ALOAD, 2);
                methodVisitor.visitFieldInsn(Opcodes.GETFIELD, "cn/jpush/android/api/NotificationMessage", "notificationExtras", "Ljava/lang/String;");
                methodVisitor.visitVarInsn(Opcodes.ALOAD, 2);
                methodVisitor.visitFieldInsn(Opcodes.GETFIELD, "cn/jpush/android/api/NotificationMessage", "notificationTitle", "Ljava/lang/String;");
                methodVisitor.visitVarInsn(Opcodes.ALOAD, 2);
                methodVisitor.visitFieldInsn(Opcodes.GETFIELD, "cn/jpush/android/api/NotificationMessage", "notificationContent", "Ljava/lang/String;");
                methodVisitor.visitInsn(Opcodes.ACONST_NULL);
                methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, "com/sensorsdata/analytics/android/sdk/aop/push/PushAutoTrackHelper", "trackJPushAppOpenNotification", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", false);
                methodVisitor.visitLabel(l1);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * Hook 魅族推送
     *
     * @param methodVisitor MethodVisitor
     * @param superName 父类名
     * @param className 类名
     * @param nameDesc 描述
     */
    public static void handleMeizuPush(MethodVisitor methodVisitor, String superName, String className, String nameDesc) {
        try {
            if ("onNotificationClicked(Landroid/content/Context;Lcom/meizu/cloud/pushsdk/handler/MzPushMessage;)V".equals(nameDesc)
                    && "com/meizu/cloud/pushsdk/MzPushMessageReceiver".equals(superName)) {
                Logger.info("Meizu hook " + className + "," + nameDesc);
                methodVisitor.visitVarInsn(Opcodes.ALOAD, 1);
                Label l1 = new Label();
                // 参数空判断
                methodVisitor.visitJumpInsn(Opcodes.IFNULL, l1);
                methodVisitor.visitVarInsn(Opcodes.ALOAD, 2);
                methodVisitor.visitJumpInsn(Opcodes.IFNULL, l1);
                // 读取参数
                methodVisitor.visitVarInsn(Opcodes.ALOAD, 2);
                methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "com/meizu/cloud/pushsdk/handler/MzPushMessage", "getSelfDefineContentString", "()Ljava/lang/String;", false);
                methodVisitor.visitVarInsn(Opcodes.ALOAD, 2);
                methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "com/meizu/cloud/pushsdk/handler/MzPushMessage", "getTitle", "()Ljava/lang/String;", false);
                methodVisitor.visitVarInsn(Opcodes.ALOAD, 2);
                methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "com/meizu/cloud/pushsdk/handler/MzPushMessage", "getContent", "()Ljava/lang/String;", false);
                methodVisitor.visitInsn(Opcodes.ACONST_NULL);
                methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, "com/sensorsdata/analytics/android/sdk/aop/push/PushAutoTrackHelper",
                        "trackMeizuAppOpenNotification", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", false);
                methodVisitor.visitLabel(l1);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
