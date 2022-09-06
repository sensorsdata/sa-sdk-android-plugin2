package com.sensorsdata.analytics.android.plugin.push;

import com.sensorsdata.analytics.android.plugin.utils.Logger;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class SensorsPushInjected {

    public static final String PUSH_TRACK_OWNER = "com/sensorsdata/analytics/android/sdk/aop/push/PushAutoTrackHelper";

    /**
     * Hook 推送逻辑
     *
     * @param methodVisitor MethodVisitor
     * @param superName 父类名
     * @param nameDesc 描述
     */
    public static void handlePush(MethodVisitor methodVisitor, String superName, String nameDesc) {
        if ("cn/jpush/android/service/JPushMessageReceiver".equals(superName)
                && "onNotifyMessageOpened(Landroid/content/Context;Lcn/jpush/android/api/NotificationMessage;)V".equals(nameDesc)) {
            handleJPush(methodVisitor);
        } else if ("onNotificationClicked(Landroid/content/Context;Lcom/meizu/cloud/pushsdk/handler/MzPushMessage;)V".equals(nameDesc)
                && "com/meizu/cloud/pushsdk/MzPushMessageReceiver".equals(superName)) {
            handleMeizuPush(methodVisitor);
        } else if ("com/igexin/sdk/GTIntentService".equals(superName)) {
            handleGTPush(methodVisitor, nameDesc);
        } else if ("com/umeng/message/UmengNotificationClickHandler".equals(superName)) {
            handleUmengNotificationClickHandler(methodVisitor, nameDesc);
        } else if ("com/umeng/message/UmengNotifyClickActivity".equals(superName)
                && "onMessage(Landroid/content/Intent;)V".equals(nameDesc)) {
            handleUmengNotifyClickActivity(methodVisitor);
        } else if ("onNewIntent(Landroid/content/Intent;)V".equals(nameDesc)) {
            handleOnNewIntent(methodVisitor, superName);
        } else if ("android/app/Service".equals(superName)) {
            handleCustomService(methodVisitor, nameDesc);
        } else if ("android/content/BroadcastReceiver".equals(superName)) {
            handleCustomBroadcast(methodVisitor, nameDesc);
        }
    }

    /**
     * Hook 极光推送
     *
     * @param methodVisitor MethodVisitor
     */
    private static void handleJPush(MethodVisitor methodVisitor) {
        try {
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
            methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, PUSH_TRACK_OWNER, "trackJPushAppOpenNotification", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", false);
            methodVisitor.visitLabel(l1);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * Hook 魅族推送
     *
     * @param methodVisitor MethodVisitor
     */
    private static void handleMeizuPush(MethodVisitor methodVisitor) {
        try {
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
            methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, PUSH_TRACK_OWNER, "trackMeizuAppOpenNotification", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", false);
            methodVisitor.visitLabel(l1);
        } catch (Throwable e) {
            e.printStackTrace(); //TODO 异常输出使用 log 日志的方式来处理
        }
    }

    /**
     * Hook 个推
     *
     * @param methodVisitor MethodVisitor
     * @param nameDesc 方法描述符
     */
    private static void handleGTPush(MethodVisitor methodVisitor, String nameDesc) {
        try {
            if ("onReceiveMessageData(Landroid/content/Context;Lcom/igexin/sdk/message/GTTransmitMessage;)V".equals(nameDesc)) {
                methodVisitor.visitVarInsn(Opcodes.ALOAD, 2);
                methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, PUSH_TRACK_OWNER, "onGeTuiReceiveMessageData", "(Ljava/lang/Object;)V", false);
            } else if ("onNotificationMessageClicked(Landroid/content/Context;Lcom/igexin/sdk/message/GTNotificationMessage;)V".equals(nameDesc)) {
                methodVisitor.visitVarInsn(Opcodes.ALOAD, 2);
                methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, PUSH_TRACK_OWNER, "onGeTuiNotificationClicked", "(Ljava/lang/Object;)V", false);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * Hook UmengNotificationClickHandler
     *
     * @param methodVisitor MethodVisitor
     * @param nameDesc 方法描述符
     */
    private static void handleUmengNotificationClickHandler(MethodVisitor methodVisitor, String nameDesc) {
        try {
            if ("openActivity(Landroid/content/Context;Lcom/umeng/message/entity/UMessage;)V".equals(nameDesc)
                    || "dealWithCustomAction(Landroid/content/Context;Lcom/umeng/message/entity/UMessage;)V".equals(nameDesc)
                    || "launchApp(Landroid/content/Context;Lcom/umeng/message/entity/UMessage;)V".equals(nameDesc)
                    || "openUrl(Landroid/content/Context;Lcom/umeng/message/entity/UMessage;)V".equals(nameDesc)) {
                methodVisitor.visitVarInsn(Opcodes.ALOAD, 2);
                methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, PUSH_TRACK_OWNER, "onUMengNotificationClick", "(Ljava/lang/Object;)V", false);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * Hook UmengNotifyClickActivity
     *
     * @param methodVisitor MethodVisitor
     */
    private static void handleUmengNotifyClickActivity(MethodVisitor methodVisitor) {
        try {
            methodVisitor.visitVarInsn(Opcodes.ALOAD, 1);
            methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, PUSH_TRACK_OWNER, "onUMengActivityMessage", "(Landroid/content/Intent;)V", false);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private static void handleOnNewIntent(MethodVisitor methodVisitor, String superName) {
        try {
            if ("android/app/Activity".equals(superName)) {
                methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
                methodVisitor.visitVarInsn(Opcodes.ALOAD, 1);
                methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, PUSH_TRACK_OWNER, "onNewIntent", "(Ljava/lang/Object;Landroid/content/Intent;)V", false);
            }
        } catch (Throwable throwable) {
            Logger.warn("Can not load class for [onNewIntent] hook, if you have any questions, please contact our technical services: classname:${className}, exception: ${throwable}");
        }
    }

    private static void handleCustomService(MethodVisitor methodVisitor, String nameDesc) {
        if ("onStart(Landroid/content/Intent;I)V".equals(nameDesc)) {
            methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
            methodVisitor.visitVarInsn(Opcodes.ALOAD, 1);
            methodVisitor.visitVarInsn(Opcodes.ILOAD, 2);
            methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, PUSH_TRACK_OWNER, "onServiceStart",
                    "(Landroid/app/Service;Landroid/content/Intent;I)V", false);
        } else if ("onStartCommand(Landroid/content/Intent;II)I".equals(nameDesc)) {
            methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
            methodVisitor.visitVarInsn(Opcodes.ALOAD, 1);
            methodVisitor.visitVarInsn(Opcodes.ILOAD, 2);
            methodVisitor.visitVarInsn(Opcodes.ILOAD, 3);
            methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, PUSH_TRACK_OWNER, "onServiceStartCommand",
                    "(Landroid/app/Service;Landroid/content/Intent;II)V", false);
        }
    }

    private static void handleCustomBroadcast(MethodVisitor methodVisitor, String nameDesc) {
        if ("onReceive(Landroid/content/Context;Landroid/content/Intent;)V".equals(nameDesc)) {
            methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
            methodVisitor.visitVarInsn(Opcodes.ALOAD, 1);
            methodVisitor.visitVarInsn(Opcodes.ALOAD, 2);
            methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, PUSH_TRACK_OWNER, "onBroadcastReceiver",
                    "(Landroid/content/BroadcastReceiver;Landroid/content/Context;Landroid/content/Intent;)V", false);
        }
    }

    public static void addOnNewIntent(ClassVisitor classVisitor) {
        MethodVisitor mv = classVisitor.visitMethod(Opcodes.ACC_PROTECTED, "onNewIntent", "(Landroid/content/Intent;)V", null, null);
        mv.visitAnnotation("Lcom/sensorsdata/analytics/android/sdk/SensorsDataInstrumented;", false);

        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitVarInsn(Opcodes.ALOAD, 1);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "android/app/Activity", "onNewIntent", "(Landroid/content/Intent;)V", false);

        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitVarInsn(Opcodes.ALOAD, 1);
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, PUSH_TRACK_OWNER, "onNewIntent", "(Ljava/lang/Object;Landroid/content/Intent;)V", false);

        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(2, 2);
        mv.visitEnd();
    }
}
