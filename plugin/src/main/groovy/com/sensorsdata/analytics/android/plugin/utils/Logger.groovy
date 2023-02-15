/*
 * Created by wangzhuozhou on 2015/08/12.
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
package com.sensorsdata.analytics.android.plugin.utils


import com.sensorsdata.analytics.android.plugin.SensorsAnalyticsTransform
import org.objectweb.asm.Opcodes

import java.lang.reflect.Array
import java.lang.reflect.Field
import java.util.concurrent.ConcurrentHashMap

class Logger {
    private static boolean debug = false
    public static ConcurrentHashMap<Integer, String> accCodeMap = new ConcurrentHashMap<>()
    public static ConcurrentHashMap<Integer, String> opCodeMap = new ConcurrentHashMap<>()

    /**
     * 打印提示信息
     */
    static void printCopyright() {
        if (debug) {
            println()
            println("${LogUI.C_BLACK_GREEN.value}" + "####################################################################" + "${LogUI.E_NORMAL.value}")
            println("${LogUI.C_BLACK_GREEN.value}" + "###                                                              ###" + "${LogUI.E_NORMAL.value}")
            println("${LogUI.C_BLACK_GREEN.value}" + "###                                                              ###" + "${LogUI.E_NORMAL.value}")
            println("${LogUI.C_BLACK_GREEN.value}" + "###          欢迎使用 SensorsAnalytics® (v" + SensorsAnalyticsTransform.VERSION + ")编译插件         ###" + "${LogUI.E_NORMAL.value}")
            println("${LogUI.C_BLACK_GREEN.value}" + "###               使用过程中碰到任何问题请联系我们               ###" + "${LogUI.E_NORMAL.value}")
            println("${LogUI.C_BLACK_GREEN.value}" + "###                      QQ 群号：785122381                      ###" + "${LogUI.E_NORMAL.value}")
            println("${LogUI.C_BLACK_GREEN.value}" + "###                         GitHub 地址：                        ###" + "${LogUI.E_NORMAL.value}")
            println("${LogUI.C_BLACK_GREEN.value}" + "###     https://github.com/sensorsdata/sa-sdk-android-plugin2    ###" + "${LogUI.E_NORMAL.value}")
            println("${LogUI.C_BLACK_GREEN.value}" + "###                                                              ###" + "${LogUI.E_NORMAL.value}")
            println("${LogUI.C_BLACK_GREEN.value}" + "###                                                              ###" + "${LogUI.E_NORMAL.value}")
            println("${LogUI.C_BLACK_GREEN.value}" + "####################################################################" + "${LogUI.E_NORMAL.value}")
            println()
        }
    }

    static void printPluginConfig(boolean disableSensorsAnalyticsMultiThread, boolean disableSensorsAnalyticsIncremental,
                                  boolean isIncremental, boolean isHookOnMethodEnter) {
        if (debug) {
            println("[SensorsAnalytics]: 是否开启多线程编译:${!disableSensorsAnalyticsMultiThread}")
            println("[SensorsAnalytics]: 是否开启增量编译:${!disableSensorsAnalyticsIncremental}")
            println("[SensorsAnalytics]: 此次是否增量编译:$isIncremental")
            println("[SensorsAnalytics]: 是否在方法进入时插入代码:$isHookOnMethodEnter")
        }
    }

    /**
     * 设置是否打印日志
     */
    static void setDebug(boolean isDebug) {
        debug = isDebug
    }

    static boolean isDebug() {
        return debug
    }

    def static error(Object msg) {
        try {
            println("${LogUI.C_ERROR.value}[SensorsAnalytics]: ${msg}${LogUI.E_NORMAL.value}")
        } catch (Exception e) {
            e.printStackTrace()
        }
    }

    def static warn(Object msg) {
        try {
            println("${LogUI.C_WARN.value}[SensorsAnalytics]: ${msg}${LogUI.E_NORMAL.value}")
        } catch (Exception e) {
            e.printStackTrace()
        }
    }

    /**
     * 打印日志
     */
    def static info(Object msg) {
        if (debug)
            try {
                println "[SensorsAnalytics]: ${msg}"
            } catch (Exception e) {
                e.printStackTrace()
            }
    }

    def static logForEach(Object... msg) {
        if (!debug) {
            return
        }
        msg.each {
            Object m ->
                try {
                    if (m != null) {
                        if (m.class.isArray()) {
                            print "["
                            def length = Array.getLength(m);
                            if (length > 0) {
                                for (int i = 0; i < length; i++) {
                                    def get = Array.get(m, i);
                                    if (get != null) {
                                        print "${get}\t"
                                    } else {
                                        print "null\t"
                                    }
                                }
                            }
                            print "]\t"
                        } else {
                            print "${m}\t"
                        }
                    } else {
                        print "null\t"
                    }
                } catch (Exception e) {
                    e.printStackTrace()
                }
        }
        println ""
    }

    static String accCode2String(int access) {
        def builder = new StringBuilder()
        def map = getAccCodeMap()
        map.each { key, value ->
            if ((key.intValue() & access) > 0) {
                builder.append(value + ' ')
            }
        }
        return builder.toString()
    }

    private static Map<Integer, String> getAccCodeMap() {
        if (accCodeMap.isEmpty()) {
            Field[] fields = Opcodes.class.getDeclaredFields()
            HashMap<Integer, String> tmpMap = [:]
            fields.each {
                if (it.name.startsWith("ACC_")) {
                    if (it.type == Integer.class) {
                        tmpMap[it.get(null) as Integer] = it.name
                    } else {
                        tmpMap[it.getInt(null)] = it.name
                    }
                }
            }
            accCodeMap.putAll(tmpMap)
        }
        return accCodeMap
    }

    static Map<Integer, String> getOpMap() {
        if (opCodeMap.size() == 0) {
            HashMap<String, Integer> map = [:]
            Field[] fields = Opcodes.class.getDeclaredFields()
            fields.each {
                if (it.type == Integer.class) {
                    map[it.get(null) as Integer] = it.name
                } else {
                    map[it.getInt(null)] = it.name
                }
            }
            opCodeMap.putAll(map)
        }
        return opCodeMap
    }
}