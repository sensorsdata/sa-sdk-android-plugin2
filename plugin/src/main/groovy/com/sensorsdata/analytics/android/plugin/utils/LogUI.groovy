/*
 * Created by zhangwei on 2020/05/21.
 * Copyright 2015－2022 Sensors Data Inc.
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

/**
 * 具体规则
 * 字背景颜色范围: 40--49                   字颜色: 30--39
 *             40: 黑                         30: 黑
 *             41:红                          31: 红
 *             42:绿                          32: 绿
 *             43:黄                          33: 黄
 *             44:蓝                          34: 蓝
 *             45:紫                          35: 紫
 *             46:深绿                        36: 深绿
 *             47:白色                        37: 白色
 *
 * 输出特效格式控制
 *             033[0m  关闭所有属性
 *             033[1m   设置高亮度
 *             03[4m   下划线
 *             033[5m   闪烁
 *             033[7m   反显
 *             033[8m   消隐
 *             033[30m   --   \033[37m   设置前景色
 *             033[40m   --   \033[47m   设置背景色
 */
enum LogUI {
    //color
    C_ERROR("\033[40;31m"),
    C_WARN("\033[40;33m"),
    C_BLACK_GREEN("\033[40;32m"),
    //end
    E_NORMAL("\033[0m");

    private final String value

    LogUI(String value) {
        this.value = value
    }

    String getValue() {
        return this.value
    }
}