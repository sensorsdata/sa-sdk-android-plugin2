package com.sensorsdata.analytics.android.plugin.push

import com.sensorsdata.analytics.android.plugin.utils.SAUtils.appendDescBeforeGiven
import com.sensorsdata.analytics.android.plugin.utils.SAUtils.isStatic
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Type
import org.objectweb.asm.commons.AdviceAdapter
import java.util.function.Consumer

class SensorsAnalyticsPushMethodVisitor(
    api: Int,
    private val mMethodVisitor: MethodVisitor,
    access: Int,
    name: String?,
    descriptor: String?,
    superName: String?
) : AdviceAdapter(
    api,
    mMethodVisitor, access, name, descriptor
) {
    private val mNameDesc: String = name + descriptor
    private val mSuperName: String? = superName

    override fun onMethodEnter() {
        super.onMethodEnter()
        // Hook Push
        if (!isStatic(methodAccess)) {
            SensorsPushInjected.handlePush(mMethodVisitor, mSuperName, mNameDesc)
        }
    }

    override fun visitCode() {
        super.visitCode()
    }

    override fun visitMethodInsn(
        opcodeAndSource: Int,
        owner: String,
        name: String,
        descriptor: String,
        isInterface: Boolean
    ) {
        //PendingIntent.getActivity() before and after
        if (opcodeAndSource == INVOKESTATIC && PENDING_INTENT_OWNER == owner && checkPendingIntentName(
                name
            )
        ) {
            val argTypes = Type.getArgumentTypes(descriptor)
            val positionList: ArrayList<Int> = ArrayList()
            for (index in argTypes.indices.reversed()) {
                val position = newLocal(argTypes[index])
                storeLocal(position, argTypes[index])
                positionList.add(0, position)
            }
            positionList.forEach(Consumer { local: Int? ->
                this.loadLocal(
                    local!!
                )
            })
            mv.visitMethodInsn(
                INVOKESTATIC,
                SensorsPushInjected.PUSH_TRACK_OWNER,
                getIntentHookMethodName(argTypes.size, name),
                refactorHookBeforeMethodDescriptor(descriptor),
                false
            )
            positionList.forEach(Consumer { local: Int? ->
                this.loadLocal(
                    local!!
                )
            })
            super.visitMethodInsn(opcodeAndSource, owner, name, descriptor, isInterface)
            mv.visitInsn(DUP)
            positionList.forEach(Consumer { local: Int? ->
                this.loadLocal(
                    local!!
                )
            })
            mv.visitMethodInsn(
                INVOKESTATIC,
                SensorsPushInjected.PUSH_TRACK_OWNER,
                getPendingIntentHookMethodName(argTypes.size, name),
                refactorHookAfterMethodDescriptor(descriptor),
                false
            )
            return
        }

        //NotificationManager.notify()
        if (opcodeAndSource == INVOKEVIRTUAL && "android/app/NotificationManager" == owner && "notify" == name) {
            val argTypes = Type.getArgumentTypes(descriptor)
            val positionList: ArrayList<Int> = ArrayList()
            for (index in argTypes.indices.reversed()) {
                val position = newLocal(argTypes[index])
                storeLocal(position, argTypes[index])
                positionList.add(0, position)
            }
            mv.visitInsn(DUP)
            positionList.forEach(Consumer { local: Int? ->
                this.loadLocal(
                    local!!
                )
            })
            super.visitMethodInsn(opcodeAndSource, owner, name, descriptor, isInterface)
            positionList.forEach(Consumer { local: Int? ->
                this.loadLocal(
                    local!!
                )
            })
            mv.visitMethodInsn(
                INVOKESTATIC, SensorsPushInjected.PUSH_TRACK_OWNER, "onNotify",
                appendDescBeforeGiven(descriptor, "Landroid/app/NotificationManager;"), false
            )
            return
        }
        super.visitMethodInsn(opcodeAndSource, owner, name, descriptor, isInterface)
    }

    private fun checkPendingIntentName(methodName: String): Boolean {
        return "getActivity" == methodName || "getService" == methodName || "getBroadcast" == methodName || "getForegroundService" == methodName
    }

    private fun refactorHookBeforeMethodDescriptor(desc: String): String {
        //change return type to void
        return desc.substring(0, desc.lastIndexOf(")") + 1) + "V"
    }

    private fun refactorHookAfterMethodDescriptor(desc: String): String {
        //change return type to void and add PendingIntent prefix
        return "(Landroid/app/PendingIntent;" + desc.substring(1, desc.lastIndexOf(")") + 1) + "V"
    }

    private fun getIntentHookMethodName(argsLength: Int, name: String): String {
        return if ("getActivity" == name) {
            if (argsLength == 4) "hookIntentGetActivity" else "hookIntentGetActivityBundle"
        } else String.format("hookIntent%s", firstLetterUpper(name))
    }

    private fun getPendingIntentHookMethodName(argsLength: Int, name: String): String {
        return if ("getActivity" == name) {
            if (argsLength == 4) "hookPendingIntentGetActivity" else "hookPendingIntentGetActivityBundle"
        } else String.format("hookPendingIntent%s", firstLetterUpper(name))
    }

    companion object {
        private const val PENDING_INTENT_OWNER = "android/app/PendingIntent"
        private fun firstLetterUpper(name: String): String {
            name.uppercase()
            val cs = name.toCharArray()
            cs[0] = cs[0] - 32
            return String(cs)
        }
    }
}