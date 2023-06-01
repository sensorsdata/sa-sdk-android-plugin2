package com.sensorsdata.analytics.android.gradle.legacy

import com.sensorsdata.analytics.android.gradle.replaceSlashByDot
import com.sensorsdata.analytics.android.gradle.ClassInfo
import com.sensorsdata.analytics.android.gradle.ClassInheritance
import java.io.File
import java.net.URLClassLoader

class AGPClassInheritance : ClassInheritance {

    private var urlclassloader: URLClassLoader? = null

    val bootClassPath: MutableList<File> = mutableListOf()

    private fun getClassLoader(): URLClassLoader {
        if (urlclassloader == null) {
            urlclassloader = URLClassLoader(bootClassPath.map { it.toURI().toURL() }.toTypedArray())
        }
        return urlclassloader!!
    }

    override fun isAssignableFrom(subClass: String, superClass: String): Boolean {
        try {
            val subClazz = getClassLoader().loadClass(subClass.replaceSlashByDot())
            val superClazz = getClassLoader().loadClass(superClass.replaceSlashByDot())
            if (superClazz.isAssignableFrom(subClazz)) {
                return true
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            return false
        }
        return false
    }

    override fun loadClass(className: String): ClassInfo? {
        return try {
            val clazz = getClassLoader().loadClass(className)
            val interfaces = clazz.interfaces.map {
                it.name
            }.toList()
            return if (clazz != null) ClassInfo(
                className,
                interfaces,
                mutableListOf(clazz.superclass.name)
            ) else null
        } catch (e: Throwable) {
            null
        }
    }
}