package com.sensorsdata.analytics.android.plugin.visitor

import org.objectweb.asm.MethodVisitor

class SensorsAnalyticsMethodCell(val name: String, val desc: String, val agentName: String) {
    /**
     * 方法所在的接口或类
     */
    lateinit var parent: String

    /**
     * 采集数据的方法描述
     */
    lateinit var agentDesc: String

    /**
     * 采集数据的方法参数起始索引（ 0：this，1+：普通参数 ）
     */
    var paramsStart: Int = -1

    /**
     * 采集数据的方法参数个数
     */
    var paramsCount = 0

    /**
     * 参数类型对应的ASM指令，加载不同类型的参数需要不同的指令
     */
    lateinit var opcodes: List<Int>

    constructor(
        name: String, desc: String, parent: String, agentName: String,
        agentDesc: String, paramsStart: Int, paramsCount: Int, opcodes: List<Int>
    ) : this(name, desc, agentName) {
        this.parent = parent
        this.agentDesc = agentDesc
        this.paramsStart = paramsStart
        this.paramsCount = paramsCount
        this.opcodes = opcodes
    }

    override fun equals(other: Any?): Boolean {
        if (other is SensorsAnalyticsMethodCell) {
            return this.name == other.name && this.desc == other.desc && this.parent == other.parent
        }
        return false
    }

    /**
     * 插入对应的原方法
     */
    fun visitMethod(methodVisitor: MethodVisitor, opcode: Int, owner: String) {
        for (i in paramsStart until paramsStart + paramsCount step 1) {
            methodVisitor.visitVarInsn(opcodes[i - paramsStart], i)
        }
        methodVisitor.visitMethodInsn(opcode, owner, name, desc, false)
    }

    /**
     * 插入 Hook 的方法
     */
    fun visitHookMethod(methodVisitor: MethodVisitor, opcode: Int, owner: String) {
        for (i in paramsStart until paramsStart + paramsCount step 1) {
            methodVisitor.visitVarInsn(opcodes[i - paramsStart], i)
        }
        methodVisitor.visitMethodInsn(opcode, owner, agentName, agentDesc, false)
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + desc.hashCode()
        result = 31 * result + parent.hashCode()
        return result
    }

}