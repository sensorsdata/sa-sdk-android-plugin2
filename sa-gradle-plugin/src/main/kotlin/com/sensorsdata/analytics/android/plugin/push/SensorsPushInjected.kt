package com.sensorsdata.analytics.android.plugin.push

import com.sensorsdata.analytics.android.plugin.utils.Logger.warn
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

object SensorsPushInjected {
    const val PUSH_TRACK_OWNER =
        "com/sensorsdata/analytics/android/sdk/aop/push/PushAutoTrackHelper"

    /**
     * Hook 推送逻辑
     *
     * @param methodVisitor MethodVisitor
     * @param superName 父类名
     * @param nameDesc 描述
     */
    fun handlePush(methodVisitor: MethodVisitor, superName: String?, nameDesc: String) {
        if ("cn/jpush/android/service/JPushMessageReceiver" == superName && "onNotifyMessageOpened(Landroid/content/Context;Lcn/jpush/android/api/NotificationMessage;)V" == nameDesc) {
            handleJPush(methodVisitor)
        } else if ("onNotificationClicked(Landroid/content/Context;Lcom/meizu/cloud/pushsdk/handler/MzPushMessage;)V" == nameDesc && "com/meizu/cloud/pushsdk/MzPushMessageReceiver" == superName) {
            handleMeizuPush(methodVisitor)
        } else if ("com/igexin/sdk/GTIntentService" == superName) {
            handleGTPush(methodVisitor, nameDesc)
        } else if ("com/umeng/message/UmengNotificationClickHandler" == superName) {
            handleUmengNotificationClickHandler(methodVisitor, nameDesc)
        } else if ("com/umeng/message/UmengNotifyClickActivity" == superName && "onMessage(Landroid/content/Intent;)V" == nameDesc) {
            handleUmengNotifyClickActivity(methodVisitor)
        } else if ("onNewIntent(Landroid/content/Intent;)V" == nameDesc) {
            handleOnNewIntent(methodVisitor, superName)
        } else if ("android/app/Service" == superName) {
            handleCustomService(methodVisitor, nameDesc)
        } else if ("android/content/BroadcastReceiver" == superName) {
            handleCustomBroadcast(methodVisitor, nameDesc)
        }
    }

    /**
     * Hook 极光推送
     *
     * @param methodVisitor MethodVisitor
     */
    private fun handleJPush(methodVisitor: MethodVisitor) {
        try {
            val l1 = Label()
            // 参数空判断
            methodVisitor.visitVarInsn(Opcodes.ALOAD, 1)
            methodVisitor.visitJumpInsn(Opcodes.IFNULL, l1)
            methodVisitor.visitVarInsn(Opcodes.ALOAD, 2)
            methodVisitor.visitJumpInsn(Opcodes.IFNULL, l1)
            // 读取参数
            methodVisitor.visitVarInsn(Opcodes.ALOAD, 2)
            methodVisitor.visitFieldInsn(
                Opcodes.GETFIELD,
                "cn/jpush/android/api/NotificationMessage",
                "notificationExtras",
                "Ljava/lang/String;"
            )
            methodVisitor.visitVarInsn(Opcodes.ALOAD, 2)
            methodVisitor.visitFieldInsn(
                Opcodes.GETFIELD,
                "cn/jpush/android/api/NotificationMessage",
                "notificationTitle",
                "Ljava/lang/String;"
            )
            methodVisitor.visitVarInsn(Opcodes.ALOAD, 2)
            methodVisitor.visitFieldInsn(
                Opcodes.GETFIELD,
                "cn/jpush/android/api/NotificationMessage",
                "notificationContent",
                "Ljava/lang/String;"
            )
            methodVisitor.visitInsn(Opcodes.ACONST_NULL)
            methodVisitor.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                PUSH_TRACK_OWNER,
                "trackJPushAppOpenNotification",
                "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V",
                false
            )
            methodVisitor.visitLabel(l1)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    /**
     * Hook 魅族推送
     *
     * @param methodVisitor MethodVisitor
     */
    private fun handleMeizuPush(methodVisitor: MethodVisitor) {
        try {
            methodVisitor.visitVarInsn(Opcodes.ALOAD, 1)
            val l1 = Label()
            // 参数空判断
            methodVisitor.visitJumpInsn(Opcodes.IFNULL, l1)
            methodVisitor.visitVarInsn(Opcodes.ALOAD, 2)
            methodVisitor.visitJumpInsn(Opcodes.IFNULL, l1)
            // 读取参数
            methodVisitor.visitVarInsn(Opcodes.ALOAD, 2)
            methodVisitor.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL,
                "com/meizu/cloud/pushsdk/handler/MzPushMessage",
                "getSelfDefineContentString",
                "()Ljava/lang/String;",
                false
            )
            methodVisitor.visitVarInsn(Opcodes.ALOAD, 2)
            methodVisitor.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL,
                "com/meizu/cloud/pushsdk/handler/MzPushMessage",
                "getTitle",
                "()Ljava/lang/String;",
                false
            )
            methodVisitor.visitVarInsn(Opcodes.ALOAD, 2)
            methodVisitor.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL,
                "com/meizu/cloud/pushsdk/handler/MzPushMessage",
                "getContent",
                "()Ljava/lang/String;",
                false
            )
            methodVisitor.visitInsn(Opcodes.ACONST_NULL)
            methodVisitor.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                PUSH_TRACK_OWNER,
                "trackMeizuAppOpenNotification",
                "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V",
                false
            )
            methodVisitor.visitLabel(l1)
        } catch (e: Throwable) {
            e.printStackTrace() //TODO 异常输出使用 log 日志的方式来处理
        }
    }

    /**
     * Hook 个推
     *
     * @param methodVisitor MethodVisitor
     * @param nameDesc 方法描述符
     */
    private fun handleGTPush(methodVisitor: MethodVisitor, nameDesc: String) {
        try {
            if ("onReceiveMessageData(Landroid/content/Context;Lcom/igexin/sdk/message/GTTransmitMessage;)V" == nameDesc) {
                methodVisitor.visitVarInsn(Opcodes.ALOAD, 2)
                methodVisitor.visitMethodInsn(
                    Opcodes.INVOKESTATIC,
                    PUSH_TRACK_OWNER,
                    "onGeTuiReceiveMessageData",
                    "(Ljava/lang/Object;)V",
                    false
                )
            } else if ("onNotificationMessageClicked(Landroid/content/Context;Lcom/igexin/sdk/message/GTNotificationMessage;)V" == nameDesc) {
                methodVisitor.visitVarInsn(Opcodes.ALOAD, 2)
                methodVisitor.visitMethodInsn(
                    Opcodes.INVOKESTATIC,
                    PUSH_TRACK_OWNER,
                    "onGeTuiNotificationClicked",
                    "(Ljava/lang/Object;)V",
                    false
                )
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    /**
     * Hook UmengNotificationClickHandler
     *
     * @param methodVisitor MethodVisitor
     * @param nameDesc 方法描述符
     */
    private fun handleUmengNotificationClickHandler(
        methodVisitor: MethodVisitor,
        nameDesc: String
    ) {
        try {
            if ("openActivity(Landroid/content/Context;Lcom/umeng/message/entity/UMessage;)V" == nameDesc || "dealWithCustomAction(Landroid/content/Context;Lcom/umeng/message/entity/UMessage;)V" == nameDesc || "launchApp(Landroid/content/Context;Lcom/umeng/message/entity/UMessage;)V" == nameDesc || "openUrl(Landroid/content/Context;Lcom/umeng/message/entity/UMessage;)V" == nameDesc) {
                methodVisitor.visitVarInsn(Opcodes.ALOAD, 2)
                methodVisitor.visitMethodInsn(
                    Opcodes.INVOKESTATIC,
                    PUSH_TRACK_OWNER,
                    "onUMengNotificationClick",
                    "(Ljava/lang/Object;)V",
                    false
                )
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    /**
     * Hook UmengNotifyClickActivity
     *
     * @param methodVisitor MethodVisitor
     */
    private fun handleUmengNotifyClickActivity(methodVisitor: MethodVisitor) {
        try {
            methodVisitor.visitVarInsn(Opcodes.ALOAD, 1)
            methodVisitor.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                PUSH_TRACK_OWNER,
                "onUMengActivityMessage",
                "(Landroid/content/Intent;)V",
                false
            )
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    private fun handleOnNewIntent(methodVisitor: MethodVisitor, superName: String?) {
        try {
            if ("android/app/Activity" == superName) {
                methodVisitor.visitVarInsn(Opcodes.ALOAD, 0)
                methodVisitor.visitVarInsn(Opcodes.ALOAD, 1)
                methodVisitor.visitMethodInsn(
                    Opcodes.INVOKESTATIC,
                    PUSH_TRACK_OWNER,
                    "onNewIntent",
                    "(Ljava/lang/Object;Landroid/content/Intent;)V",
                    false
                )
            }
        } catch (throwable: Throwable) {
            warn("Can not load class for [onNewIntent] hook, if you have any questions, please contact our technical services: classname:\${className}, exception: \${throwable}")
        }
    }

    private fun handleCustomService(methodVisitor: MethodVisitor, nameDesc: String) {
        if ("onStart(Landroid/content/Intent;I)V" == nameDesc) {
            methodVisitor.visitVarInsn(Opcodes.ALOAD, 0)
            methodVisitor.visitVarInsn(Opcodes.ALOAD, 1)
            methodVisitor.visitVarInsn(Opcodes.ILOAD, 2)
            methodVisitor.visitMethodInsn(
                Opcodes.INVOKESTATIC, PUSH_TRACK_OWNER, "onServiceStart",
                "(Landroid/app/Service;Landroid/content/Intent;I)V", false
            )
        } else if ("onStartCommand(Landroid/content/Intent;II)I" == nameDesc) {
            methodVisitor.visitVarInsn(Opcodes.ALOAD, 0)
            methodVisitor.visitVarInsn(Opcodes.ALOAD, 1)
            methodVisitor.visitVarInsn(Opcodes.ILOAD, 2)
            methodVisitor.visitVarInsn(Opcodes.ILOAD, 3)
            methodVisitor.visitMethodInsn(
                Opcodes.INVOKESTATIC, PUSH_TRACK_OWNER, "onServiceStartCommand",
                "(Landroid/app/Service;Landroid/content/Intent;II)V", false
            )
        }
    }

    private fun handleCustomBroadcast(methodVisitor: MethodVisitor, nameDesc: String) {
        if ("onReceive(Landroid/content/Context;Landroid/content/Intent;)V" == nameDesc) {
            methodVisitor.visitVarInsn(Opcodes.ALOAD, 0)
            methodVisitor.visitVarInsn(Opcodes.ALOAD, 1)
            methodVisitor.visitVarInsn(Opcodes.ALOAD, 2)
            methodVisitor.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                PUSH_TRACK_OWNER,
                "onBroadcastReceiver",
                "(Landroid/content/BroadcastReceiver;Landroid/content/Context;Landroid/content/Intent;)V",
                false
            )
        }
    }

    fun addOnNewIntent(classVisitor: ClassVisitor) {
        val mv = classVisitor.visitMethod(
            Opcodes.ACC_PROTECTED,
            "onNewIntent",
            "(Landroid/content/Intent;)V",
            null,
            null
        )
        mv.visitAnnotation("Lcom/sensorsdata/analytics/android/sdk/SensorsDataInstrumented;", false)
        mv.visitCode()
        mv.visitVarInsn(Opcodes.ALOAD, 0)
        mv.visitVarInsn(Opcodes.ALOAD, 1)
        mv.visitMethodInsn(
            Opcodes.INVOKESPECIAL,
            "android/app/Activity",
            "onNewIntent",
            "(Landroid/content/Intent;)V",
            false
        )
        mv.visitVarInsn(Opcodes.ALOAD, 0)
        mv.visitVarInsn(Opcodes.ALOAD, 1)
        mv.visitMethodInsn(
            Opcodes.INVOKESTATIC,
            PUSH_TRACK_OWNER,
            "onNewIntent",
            "(Ljava/lang/Object;Landroid/content/Intent;)V",
            false
        )
        mv.visitInsn(Opcodes.RETURN)
        mv.visitMaxs(2, 2)
        mv.visitEnd()
    }
}