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

/**
 * An AGP compatible context, which contains base ability for user.
 */
interface AGPContext {
    var asmCompatFactory: AsmCompatFactory?
}

interface ClassInheritance {
    fun isAssignableFrom(subClass: String, superClass: String): Boolean

    fun loadClass(className: String): ClassInfo?
}