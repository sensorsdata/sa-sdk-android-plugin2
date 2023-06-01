package com.sensorsdata.analytics.android.gradle.legacy

import org.objectweb.asm.ClassVisitor

class BaseClassVisitor(api: Int, classVisitor: ClassVisitor?) : ClassVisitor(api, classVisitor) {
}