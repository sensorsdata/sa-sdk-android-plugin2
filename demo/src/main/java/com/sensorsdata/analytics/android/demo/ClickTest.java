/*
 * Created by zhangwei on 2022/12/23.
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

package com.sensorsdata.analytics.android.demo;

import android.view.View;

public class ClickTest {


    void test1(View view){
        view.setOnClickListener(view1 -> {
            int a = 10;
        });
    }

    void test2(View view){
        view.setOnClickListener(view1 -> {
            int a = 20;
        });
    }
}
