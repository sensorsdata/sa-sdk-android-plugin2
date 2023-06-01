package com.sensorsdata.analytics.android.plugin.visitor

import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.commons.JSRInlinerAdapter

class SensorsAnalyticsJSRAdapter constructor(
    api: Int,
    mv: MethodVisitor?,
    access: Int,
    name: String?,
    desc: String?,
    signature: String?,
    exceptions: Array<String>?
) : JSRInlinerAdapter(api, mv, access, name, desc, signature, exceptions)