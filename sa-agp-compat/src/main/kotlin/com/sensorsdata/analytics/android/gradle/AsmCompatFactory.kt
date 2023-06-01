/*
 * Created by zhangwei on 2023/05/06.
 * Copyright 2015－2023 Sensors Data Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sensorsdata.analytics.android.gradle

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes

/**
 * A factory to create class visitor objects to instrument classes.
 */
abstract class AsmCompatFactory {

    /**
     * Using the given [classVisitor] for a transform, and return a new [ClassVisitor] for next transform.
     *
     * @param classVisitor the [ClassVisitor] to which the created [ClassVisitor] must delegate method calls
     */
    abstract fun transform(classVisitor: ClassVisitor, classInheritance: ClassInheritance): ClassVisitor

    /**
     * Whether the factory wants to instrument the class with the given [classInfo].
     */
    abstract fun isInstrumentable(classInfo: ClassInfo): Boolean

    /**
     * should build incremental
     */
    abstract val isIncremental: Boolean

    /**
     * interface name. it used be Transform's name
     */
    abstract val name: String

    /**
     * before transform
     */
    abstract fun onBeforeTransform()

    open var asmAPI = Opcodes.ASM7

    abstract val extension:Any

}