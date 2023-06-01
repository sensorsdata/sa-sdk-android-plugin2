package com.sensorsdata.analytics.android.plugin.utils

import org.apache.commons.io.IOUtils
import org.objectweb.asm.Opcodes
import java.io.InputStream

object SAUtils {
    fun isPublic(access: Int): Boolean {
        return (access and Opcodes.ACC_PUBLIC) != 0;
    }

    fun isStatic(access: Int): Boolean {
        return (access and Opcodes.ACC_STATIC) != 0;
    }

    fun isProtected(access: Int): Boolean {
        return (access and Opcodes.ACC_PROTECTED) != 0;
    }

    /**
     * 比较两个字符串版本信息大小，例如 2.01.10 > 2.1.9.1.2
     *
     * @param version1 版本信息字符串
     * @param version2 版本信息字符串
     * @return 如果返回值为 0，表示版本相等；如果返回值为 1 表示 version1 大于 version2；如果返回值为 -1，表示 version1 小于 version2。
     */
    fun compareVersion(version1: String, version2: String): Int {
        val v1Array = version1.replace("-pre", "").split(".");
        val v2Array = version2.replace("-pre", "").split(".");
        val maxLength = Math.max(v1Array.size, v2Array.size);
        var str1: String?
        var str2: String?
        for (index in 0..maxLength) {
            if (v1Array.size > index) {
                str1 = v1Array[index]
            } else {
                return -1;
            }
            if (v2Array.size > index) {
                str2 = v2Array[index];
            } else {
                return 1;
            }
            try {
                val num1 = Integer.valueOf(str1);
                val num2 = Integer.valueOf(str2);
                if (num1 != num2) {
                    return if (num1 - num2 > 0) 1 else -1
                }
            } catch (ignored: Exception) {
                return str1.compareTo(str2)
            }
        }
        return 0
    }

    fun toByteArrayAndAutoCloseStream(input: InputStream): ByteArray {
        try {
            return input.readBytes()
        } catch (e: Exception) {
            throw e
        } finally {
            IOUtils.closeQuietly(input)
        }
    }

    /**
     * 获取 LOAD 或 STORE 的相反指令，例如 ILOAD => ISTORE，ASTORE => ALOAD
     *
     * @param LOAD 或 STORE 指令
     * @return 返回相对应的指令
     */
    fun convertOpcodes(code: Int): Int {
        var result = code
        when (code) {
            Opcodes.ILOAD -> result = Opcodes.ISTORE
            Opcodes.ALOAD -> result = Opcodes.ASTORE
            Opcodes.LLOAD ->
                result = Opcodes.LSTORE
            Opcodes.FLOAD ->
                result = Opcodes.FSTORE
            Opcodes.DLOAD ->
                result = Opcodes.DSTORE
            Opcodes.ISTORE ->
                result = Opcodes.ILOAD
            Opcodes.ASTORE ->
                result = Opcodes.ALOAD
            Opcodes.LSTORE ->
                result = Opcodes.LLOAD
            Opcodes.FSTORE ->
                result = Opcodes.FLOAD
            Opcodes.DSTORE ->
                result = Opcodes.DLOAD
        }
        return result
    }

    fun appendDescBeforeGiven(givenDesc: String, appendDesc: String): String {
        return givenDesc.replaceFirst("(", "($appendDesc")
    }

}