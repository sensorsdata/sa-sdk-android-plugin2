/*
 * Created by wangzhuozhou on 2015/08/12.
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
package com.sensorsdata.analytics.android.plugin.configs

import com.sensorsdata.analytics.android.plugin.visitor.SensorsAnalyticsMethodCell
import org.objectweb.asm.Opcodes

object SensorsAnalyticsHookConfig {
    const val SENSORS_ANALYTICS_API =
        "com/sensorsdata/analytics/android/autotrack/aop/SensorsDataAutoTrackHelper"
    val INTERFACE_METHODS = mutableMapOf<String, SensorsAnalyticsMethodCell>()
    val CLASS_METHODS = mutableMapOf<String, SensorsAnalyticsMethodCell>()

    init {
        addInterfaceMethod(
            SensorsAnalyticsMethodCell(
                "onCheckedChanged",
                "(Landroid/widget/CompoundButton;Z)V",
                "android/widget/CompoundButton\$OnCheckedChangeListener",
                "trackViewOnClick",
                "(Landroid/view/View;)V",
                1, 1,
                listOf(Opcodes.ALOAD)
            )
        )
        addInterfaceMethod(
            SensorsAnalyticsMethodCell(
                "onRatingChanged",
                "(Landroid/widget/RatingBar;FZ)V",
                "android/widget/RatingBar\$OnRatingBarChangeListener",
                "trackViewOnClick",
                "(Landroid/view/View;)V",
                1, 1,
                listOf(Opcodes.ALOAD)
            )
        )
        addInterfaceMethod(
            SensorsAnalyticsMethodCell(
                "onStopTrackingTouch",
                "(Landroid/widget/SeekBar;)V",
                "android/widget/SeekBar\$OnSeekBarChangeListener",
                "trackViewOnClick",
                "(Landroid/view/View;)V",
                1, 1,
                listOf(Opcodes.ALOAD)
            )
        )
        addInterfaceMethod(
            SensorsAnalyticsMethodCell(
                "onCheckedChanged",
                "(Landroid/widget/RadioGroup;I)V",
                "android/widget/RadioGroup\$OnCheckedChangeListener",
                "trackRadioGroup",
                "(Landroid/widget/RadioGroup;I)V",
                1, 2,
                listOf(Opcodes.ALOAD, Opcodes.ILOAD)
            )
        )
        addInterfaceMethod(
            SensorsAnalyticsMethodCell(
                "onClick",
                "(Landroid/content/DialogInterface;I)V",
                "android/content/DialogInterface\$OnClickListener",
                "trackDialog",
                "(Landroid/content/DialogInterface;I)V",
                1, 2,
                listOf(Opcodes.ALOAD, Opcodes.ILOAD)
            )
        )
        addInterfaceMethod(
            SensorsAnalyticsMethodCell(
                "onItemSelected",
                "(Landroid/widget/AdapterView;Landroid/view/View;IJ)V",
                "android/widget/AdapterView\$OnItemSelectedListener",
                "trackListView",
                "(Landroid/widget/AdapterView;Landroid/view/View;I)V",
                1, 3,
                listOf(Opcodes.ALOAD, Opcodes.ALOAD, Opcodes.ILOAD)
            )
        )
        addInterfaceMethod(
            SensorsAnalyticsMethodCell(
                "onGroupClick",
                "(Landroid/widget/ExpandableListView;Landroid/view/View;IJ)Z",
                "android/widget/ExpandableListView\$OnGroupClickListener",
                "trackExpandableListViewOnGroupClick",
                "(Landroid/widget/ExpandableListView;Landroid/view/View;I)V",
                1, 3,
                listOf(Opcodes.ALOAD, Opcodes.ALOAD, Opcodes.ILOAD)
            )
        )
        addInterfaceMethod(
            SensorsAnalyticsMethodCell(
                "onChildClick",
                "(Landroid/widget/ExpandableListView;Landroid/view/View;IIJ)Z",
                "android/widget/ExpandableListView\$OnChildClickListener",
                "trackExpandableListViewOnChildClick",
                "(Landroid/widget/ExpandableListView;Landroid/view/View;II)V",
                1, 4,
                listOf(Opcodes.ALOAD, Opcodes.ALOAD, Opcodes.ILOAD, Opcodes.ILOAD)
            )
        )
        addInterfaceMethod(
            SensorsAnalyticsMethodCell(
                "onTabChanged",
                "(Ljava/lang/String;)V",
                "android/widget/TabHost\$OnTabChangeListener",
                "trackTabHost",
                "(Ljava/lang/String;)V",
                1, 1,
                listOf(Opcodes.ALOAD)
            )
        )
        addInterfaceMethod(
            SensorsAnalyticsMethodCell(
                "onTabSelected",
                "(Landroid/support/design/widget/TabLayout\$Tab;)V",
                "android/support/design/widget/TabLayout\$OnTabSelectedListener",
                "trackTabLayoutSelected",
                "(Ljava/lang/Object;Ljava/lang/Object;)V",
                0, 2,
                listOf(Opcodes.ALOAD, Opcodes.ALOAD)
            )
        )
        addInterfaceMethod(
            SensorsAnalyticsMethodCell(
                "onTabSelected",
                "(Lcom/google/android/material/tabs/TabLayout\$Tab;)V",
                "com/google/android/material/tabs/TabLayout\$OnTabSelectedListener",
                "trackTabLayoutSelected",
                "(Ljava/lang/Object;Ljava/lang/Object;)V",
                0, 2,
                listOf(Opcodes.ALOAD, Opcodes.ALOAD)
            )
        )
        addInterfaceMethod(
            SensorsAnalyticsMethodCell(
                "onMenuItemClick",
                "(Landroid/view/MenuItem;)Z",
                "android/widget/Toolbar\$OnMenuItemClickListener",
                "trackMenuItem",
                "(Landroid/view/MenuItem;)V",
                1, 1,
                listOf(Opcodes.ALOAD)
            )
        )
        addInterfaceMethod(
            SensorsAnalyticsMethodCell(
                "onMenuItemClick",
                "(Landroid/view/MenuItem;)Z",
                "android/support/v7/widget/Toolbar\$OnMenuItemClickListener",
                "trackMenuItem",
                "(Landroid/view/MenuItem;)V",
                1, 1,
                listOf(Opcodes.ALOAD)
            )
        )
        addInterfaceMethod(
            SensorsAnalyticsMethodCell(
                "onMenuItemClick",
                "(Landroid/view/MenuItem;)Z",
                "androidx/appcompat/widget/Toolbar\$OnMenuItemClickListener",
                "trackMenuItem",
                "(Landroid/view/MenuItem;)V",
                1, 1,
                listOf(Opcodes.ALOAD)
            )
        )
        addInterfaceMethod(
            SensorsAnalyticsMethodCell(
                "onClick",
                "(Landroid/content/DialogInterface;IZ)V",
                "android/content/DialogInterface\$OnMultiChoiceClickListener",
                "trackDialog",
                "(Landroid/content/DialogInterface;I)V",
                1, 2,
                listOf(Opcodes.ALOAD, Opcodes.ILOAD)
            )
        )
        addInterfaceMethod(
            SensorsAnalyticsMethodCell(
                "onMenuItemClick",
                "(Landroid/view/MenuItem;)Z",
                "android/widget/PopupMenu\$OnMenuItemClickListener",
                "trackMenuItem",
                "(Landroid/view/MenuItem;)V",
                1, 1,
                listOf(Opcodes.ALOAD)
            )
        )
        addInterfaceMethod(
            SensorsAnalyticsMethodCell(
                "onMenuItemClick",
                "(Landroid/view/MenuItem;)Z",
                "androidx/appcompat/widget/PopupMenu\$OnMenuItemClickListener",
                "trackMenuItem",
                "(Landroid/view/MenuItem;)V",
                1, 1,
                listOf(Opcodes.ALOAD)
            )
        )
        addInterfaceMethod(
            SensorsAnalyticsMethodCell(
                "onMenuItemClick",
                "(Landroid/view/MenuItem;)Z",
                "android/support/v7/widget/PopupMenu\$OnMenuItemClickListener",
                "trackMenuItem",
                "(Landroid/view/MenuItem;)V",
                1, 1,
                listOf(Opcodes.ALOAD)
            )
        )
        addInterfaceMethod(
            SensorsAnalyticsMethodCell(
                "onNavigationItemSelected",
                "(Landroid/view/MenuItem;)Z",
                "com/google/android/material/navigation/NavigationView\$OnNavigationItemSelectedListener",
                "trackMenuItem",
                "(Landroid/view/MenuItem;)V",
                1, 1,
                listOf(Opcodes.ALOAD)
            )
        )
        addInterfaceMethod(
            SensorsAnalyticsMethodCell(
                "onNavigationItemSelected",
                "(Landroid/view/MenuItem;)Z",
                "android/support/design/widget/NavigationView\$OnNavigationItemSelectedListener",
                "trackMenuItem",
                "(Landroid/view/MenuItem;)V",
                1, 1,
                listOf(Opcodes.ALOAD)
            )
        )
        addInterfaceMethod(
            SensorsAnalyticsMethodCell(
                "onNavigationItemSelected",
                "(Landroid/view/MenuItem;)Z",
                "android/support/design/widget/BottomNavigationView\$OnNavigationItemSelectedListener",
                "trackMenuItem",
                "(Landroid/view/MenuItem;)V",
                1, 1,
                listOf(Opcodes.ALOAD)
            )
        )
        addInterfaceMethod(
            SensorsAnalyticsMethodCell(
                "onNavigationItemSelected",
                "(Landroid/view/MenuItem;)Z",
                "com/google/android/material/bottomnavigation/BottomNavigationView\$OnNavigationItemSelectedListener",
                "trackMenuItem",
                "(Landroid/view/MenuItem;)V",
                1, 1,
                listOf(Opcodes.ALOAD)
            )
        )
    }

    init {
        addClassMethod(
            SensorsAnalyticsMethodCell(
                "performClick",
                "()Z",
                "androidx/appcompat/widget/ActionMenuPresenter\$OverflowMenuButton",
                "trackViewOnClick",
                "(Landroid/view/View;)V",
                0, 1,
                listOf(Opcodes.ALOAD)
            )
        )

        addClassMethod(
            SensorsAnalyticsMethodCell(
                "performClick",
                "()Z",
                "android/support/v7/widget/ActionMenuPresenter\$OverflowMenuButton",
                "trackViewOnClick",
                "(Landroid/view/View;)V",
                0, 1,
                listOf(Opcodes.ALOAD)
            )
        )

        addClassMethod(
            SensorsAnalyticsMethodCell(
                "performClick",
                "()Z",
                "android/widget/ActionMenuPresenter\$OverflowMenuButton",
                "trackViewOnClick",
                "(Landroid/view/View;)V",
                0, 1,
                listOf(Opcodes.ALOAD)
            )
        )
    }

    private fun addInterfaceMethod(sensorsAnalyticsMethodCell: SensorsAnalyticsMethodCell?) {
        if (sensorsAnalyticsMethodCell != null) {
            INTERFACE_METHODS.put(
                sensorsAnalyticsMethodCell.parent + sensorsAnalyticsMethodCell.name + sensorsAnalyticsMethodCell.desc,
                sensorsAnalyticsMethodCell
            )
        }
    }

    private fun addClassMethod(sensorsAnalyticsMethodCell: SensorsAnalyticsMethodCell?) {
        if (sensorsAnalyticsMethodCell != null) {
            CLASS_METHODS.put(
                sensorsAnalyticsMethodCell.parent + sensorsAnalyticsMethodCell.name + sensorsAnalyticsMethodCell.desc,
                sensorsAnalyticsMethodCell
            )
        }
    }

    /**
     * android.gradle 3.2.1 版本中，针对 Lambda 表达式处理
     */

    val LAMBDA_METHODS = mutableMapOf<String, SensorsAnalyticsMethodCell>()

    //lambda 参数优化取样
    val SAMPLING_LAMBDA_METHODS = mutableListOf<SensorsAnalyticsMethodCell>()

    init {
        addLambdaMethod(
            SensorsAnalyticsMethodCell(
                "onClick",
                "(Landroid/view/View;)V",
                "Landroid/view/View\$OnClickListener;",
                "trackViewOnClick",
                "(Landroid/view/View;)V",
                1, 1,
                listOf(Opcodes.ALOAD)
            )
        )
        SAMPLING_LAMBDA_METHODS.add(
            SensorsAnalyticsMethodCell(
                "onClick",
                "(Landroid/view/View;)V",
                "Landroid/view/View\$OnClickListener;",
                "trackViewOnClick",
                "(Landroid/view/View;)V",
                1, 1,
                listOf(Opcodes.ALOAD)
            )
        )
        addLambdaMethod(
            SensorsAnalyticsMethodCell(
                "onCheckedChanged",
                "(Landroid/widget/CompoundButton;Z)V",
                "Landroid/widget/CompoundButton\$OnCheckedChangeListener;",
                "trackViewOnClick",
                "(Landroid/view/View;)V",
                1, 1,
                listOf(Opcodes.ALOAD)
            )
        )
        addLambdaMethod(
            SensorsAnalyticsMethodCell(
                "onRatingChanged",
                "(Landroid/widget/RatingBar;FZ)V",
                "Landroid/widget/RatingBar\$OnRatingBarChangeListener;",
                "trackViewOnClick",
                "(Landroid/view/View;)V",
                1, 1,
                listOf(Opcodes.ALOAD)
            )
        )
        addLambdaMethod(
            SensorsAnalyticsMethodCell(
                "onCheckedChanged",
                "(Landroid/widget/RadioGroup;I)V",
                "Landroid/widget/RadioGroup\$OnCheckedChangeListener;",
                "trackRadioGroup",
                "(Landroid/widget/RadioGroup;I)V",
                1, 2,
                listOf(Opcodes.ALOAD, Opcodes.ILOAD)
            )
        )
        SAMPLING_LAMBDA_METHODS.add(
            SensorsAnalyticsMethodCell(
                "onCheckedChanged",
                "(Landroid/widget/RadioGroup;I)V",
                "Landroid/widget/RadioGroup\$OnCheckedChangeListener;",
                "trackRadioGroup",
                "(Landroid/widget/RadioGroup;I)V",
                1, 2,
                listOf(Opcodes.ALOAD, Opcodes.ILOAD)
            )
        )
        addLambdaMethod(
            SensorsAnalyticsMethodCell(
                "onClick",
                "(Landroid/content/DialogInterface;I)V",
                "Landroid/content/DialogInterface\$OnClickListener;",
                "trackDialog",
                "(Landroid/content/DialogInterface;I)V",
                1, 2,
                listOf(Opcodes.ALOAD, Opcodes.ILOAD)
            )
        )
        addLambdaMethod(
            SensorsAnalyticsMethodCell(
                "onItemClick",
                "(Landroid/widget/AdapterView;Landroid/view/View;IJ)V",
                "Landroid/widget/AdapterView\$OnItemClickListener;",
                "trackListView",
                "(Landroid/widget/AdapterView;Landroid/view/View;I)V",
                1, 3,
                listOf(Opcodes.ALOAD, Opcodes.ALOAD, Opcodes.ILOAD)
            )
        )
        SAMPLING_LAMBDA_METHODS.add(
            SensorsAnalyticsMethodCell(
                "onItemClick",
                "(Landroid/widget/AdapterView;Landroid/view/View;IJ)V",
                "Landroid/widget/AdapterView\$OnItemClickListener;",
                "trackListView",
                "(Landroid/widget/AdapterView;Landroid/view/View;I)V",
                1, 3,
                listOf(Opcodes.ALOAD, Opcodes.ALOAD, Opcodes.ILOAD)
            )
        )
        addLambdaMethod(
            SensorsAnalyticsMethodCell(
                "onGroupClick",
                "(Landroid/widget/ExpandableListView;Landroid/view/View;IJ)Z",
                "Landroid/widget/ExpandableListView\$OnGroupClickListener;",
                "trackExpandableListViewOnGroupClick",
                "(Landroid/widget/ExpandableListView;Landroid/view/View;I)V",
                1, 3,
                listOf(Opcodes.ALOAD, Opcodes.ALOAD, Opcodes.ILOAD)
            )
        )
        addLambdaMethod(
            SensorsAnalyticsMethodCell(
                "onChildClick",
                "(Landroid/widget/ExpandableListView;Landroid/view/View;IIJ)Z",
                "Landroid/widget/ExpandableListView\$OnChildClickListener;",
                "trackExpandableListViewOnChildClick",
                "(Landroid/widget/ExpandableListView;Landroid/view/View;II)V",
                1, 4,
                listOf(Opcodes.ALOAD, Opcodes.ALOAD, Opcodes.ILOAD, Opcodes.ILOAD)
            )
        )
        addLambdaMethod(
            SensorsAnalyticsMethodCell(
                "onTabChanged",
                "(Ljava/lang/String;)V",
                "Landroid/widget/TabHost\$OnTabChangeListener;",
                "trackTabHost",
                "(Ljava/lang/String;)V",
                1, 1,
                listOf(Opcodes.ALOAD)
            )
        )
        addLambdaMethod(
            SensorsAnalyticsMethodCell(
                "onNavigationItemSelected",
                "(Landroid/view/MenuItem;)Z",
                "Lcom/google/android/material/navigation/NavigationView\$OnNavigationItemSelectedListener;",
                "trackMenuItem",
                "(Landroid/view/MenuItem;)V",
                1, 1,
                listOf(Opcodes.ALOAD)
            )
        )
        addLambdaMethod(
            SensorsAnalyticsMethodCell(
                "onNavigationItemSelected",
                "(Landroid/view/MenuItem;)Z",
                "Landroid/support/design/widget/NavigationView\$OnNavigationItemSelectedListener;",
                "trackMenuItem",
                "(Landroid/view/MenuItem;)V",
                1, 1,
                listOf(Opcodes.ALOAD)
            )
        )
        addLambdaMethod(
            SensorsAnalyticsMethodCell(
                "onNavigationItemSelected",
                "(Landroid/view/MenuItem;)Z",
                "Landroid/support/design/widget/BottomNavigationView\$OnNavigationItemSelectedListener;",
                "trackMenuItem",
                "(Landroid/view/MenuItem;)V",
                1, 1,
                listOf(Opcodes.ALOAD)
            )
        )
        addLambdaMethod(
            SensorsAnalyticsMethodCell(
                "onNavigationItemSelected",
                "(Landroid/view/MenuItem;)Z",
                "Lcom/google/android/material/bottomnavigation/BottomNavigationView\$OnNavigationItemSelectedListener;",
                "trackMenuItem",
                "(Landroid/view/MenuItem;)V",
                1, 1,
                listOf(Opcodes.ALOAD)
            )
        )
        addLambdaMethod(
            SensorsAnalyticsMethodCell(
                "onMenuItemClick",
                "(Landroid/view/MenuItem;)Z",
                "Landroid/widget/Toolbar\$OnMenuItemClickListener;",
                "trackMenuItem",
                "(Landroid/view/MenuItem;)V",
                1, 1,
                listOf(Opcodes.ALOAD)
            )
        )
        addLambdaMethod(
            SensorsAnalyticsMethodCell(
                "onMenuItemClick",
                "(Landroid/view/MenuItem;)Z",
                "Landroid/support/v7/widget/Toolbar\$OnMenuItemClickListener;",
                "trackMenuItem",
                "(Landroid/view/MenuItem;)V",
                1, 1,
                listOf(Opcodes.ALOAD)
            )
        )
        addLambdaMethod(
            SensorsAnalyticsMethodCell(
                "onMenuItemClick",
                "(Landroid/view/MenuItem;)Z",
                "Landroidx/appcompat/widget/Toolbar\$OnMenuItemClickListener;",
                "trackMenuItem",
                "(Landroid/view/MenuItem;)V",
                1, 1,
                listOf(Opcodes.ALOAD)
            )
        )
        addLambdaMethod(
            SensorsAnalyticsMethodCell(
                "onClick",
                "(Landroid/content/DialogInterface;IZ)V",
                "Landroid/content/DialogInterface\$OnMultiChoiceClickListener;",
                "trackDialog",
                "(Landroid/content/DialogInterface;I)V",
                1, 2,
                listOf(Opcodes.ALOAD, Opcodes.ILOAD)
            )
        )
        addLambdaMethod(
            SensorsAnalyticsMethodCell(
                "onMenuItemClick",
                "(Landroid/view/MenuItem;)Z",
                "Landroid/widget/PopupMenu\$OnMenuItemClickListener;",
                "trackMenuItem",
                "(Landroid/view/MenuItem;)V",
                1, 1,
                listOf(Opcodes.ALOAD)
            )
        )
        addLambdaMethod(
            SensorsAnalyticsMethodCell(
                "onMenuItemClick",
                "(Landroid/view/MenuItem;)Z",
                "Landroidx/appcompat/widget/PopupMenu\$OnMenuItemClickListener;",
                "trackMenuItem",
                "(Landroid/view/MenuItem;)V",
                1, 1,
                listOf(Opcodes.ALOAD)
            )
        )
        addLambdaMethod(
            SensorsAnalyticsMethodCell(
                "onMenuItemClick",
                "(Landroid/view/MenuItem;)Z",
                "Landroid/support/v7/widget/PopupMenu\$OnMenuItemClickListener;",
                "trackMenuItem",
                "(Landroid/view/MenuItem;)V",
                1, 1,
                listOf(Opcodes.ALOAD)
            )
        )

        // Todo: 扩展
    }

    private fun addLambdaMethod(sensorsAnalyticsMethodCell: SensorsAnalyticsMethodCell?) {
        if (sensorsAnalyticsMethodCell != null) {
            LAMBDA_METHODS.put(
                sensorsAnalyticsMethodCell.parent + sensorsAnalyticsMethodCell.name + sensorsAnalyticsMethodCell.desc,
                sensorsAnalyticsMethodCell
            )
        }
    }
}