package com.sensorsdata.analytics.android.plugin

import jdk.internal.org.objectweb.asm.Opcodes

class SensorsAnalyticsHookConfig {
    public static
    final String sSensorsAnalyticsAPI = "com/sensorsdata/analytics/android/sdk/SensorsDataAutoTrackHelper"

    public final
    static HashMap<String, SensorsAnalyticsMethodCell> sInterfaceMethods = new HashMap<>()

    static {
        sInterfaceMethods.put('onCheckedChanged(Landroid/widget/CompoundButton;Z)V', new SensorsAnalyticsMethodCell(
                'onCheckedChanged',
                '(Landroid/widget/CompoundButton;Z)V',
                'android/widget/CompoundButton$OnCheckedChangeListener',
                'trackViewOnClick',
                '(Landroid/view/View;)V',
                1, 1,
                [Opcodes.ALOAD]))
        sInterfaceMethods.put('onRatingChanged(Landroid/widget/RatingBar;FZ)V', new SensorsAnalyticsMethodCell(
                'onRatingChanged',
                '(Landroid/widget/RatingBar;FZ)V',
                'android/widget/RatingBar$OnRatingBarChangeListener',
                'trackViewOnClick',
                '(Landroid/view/View;)V',
                1, 1,
                [Opcodes.ALOAD]))
        sInterfaceMethods.put('onStopTrackingTouch(Landroid/widget/SeekBar;)V', new SensorsAnalyticsMethodCell(
                'onStopTrackingTouch',
                '(Landroid/widget/SeekBar;)V',
                'android/widget/SeekBar$OnSeekBarChangeListener',
                'trackViewOnClick',
                '(Landroid/view/View;)V',
                1, 1,
                [Opcodes.ALOAD]))
        sInterfaceMethods.put('onCheckedChanged(Landroid/widget/RadioGroup;I)V', new SensorsAnalyticsMethodCell(
                'onCheckedChanged',
                '(Landroid/widget/RadioGroup;I)V',
                'android/widget/RadioGroup$OnCheckedChangeListener',
                'trackRadioGroup',
                '(Landroid/widget/RadioGroup;I)V',
                1, 2,
                [Opcodes.ALOAD, Opcodes.ILOAD]))
        sInterfaceMethods.put('onClick(Landroid/content/DialogInterface;I)V', new SensorsAnalyticsMethodCell(
                'onClick',
                '(Landroid/content/DialogInterface;I)V',
                'android/content/DialogInterface$OnClickListener',
                'trackDialog',
                '(Landroid/content/DialogInterface;I)V',
                1, 2,
                [Opcodes.ALOAD, Opcodes.ILOAD]))
        sInterfaceMethods.put('onItemClick(Landroid/widget/AdapterView;Landroid/view/View;IJ)V', new SensorsAnalyticsMethodCell(
                'onItemClick',
                '(Landroid/widget/AdapterView;Landroid/view/View;IJ)V',
                'android/widget/AdapterView$OnItemClickListener',
                'trackListView',
                '(Landroid/widget/AdapterView;Landroid/view/View;I)V',
                1, 3,
                [Opcodes.ALOAD, Opcodes.ALOAD, Opcodes.ILOAD]))
        sInterfaceMethods.put('onItemSelected(Landroid/widget/AdapterView;Landroid/view/View;IJ)V', new SensorsAnalyticsMethodCell(
                'onItemSelected',
                '(Landroid/widget/AdapterView;Landroid/view/View;IJ)V',
                'android/widget/AdapterView$OnItemSelectedListener',
                'trackListView',
                '(Landroid/widget/AdapterView;Landroid/view/View;I)V',
                1, 3,
                [Opcodes.ALOAD, Opcodes.ALOAD, Opcodes.ILOAD]))
        sInterfaceMethods.put('onGroupClick(Landroid/widget/ExpandableListView;Landroid/view/View;IJ)Z', new SensorsAnalyticsMethodCell(
                'onGroupClick',
                '(Landroid/widget/ExpandableListView;Landroid/view/View;IJ)Z',
                'android/widget/ExpandableListView$OnGroupClickListener',
                'trackExpandableListViewOnGroupClick',
                '(Landroid/widget/ExpandableListView;Landroid/view/View;I)V',
                1, 3,
                [Opcodes.ALOAD, Opcodes.ALOAD, Opcodes.ILOAD]))
        sInterfaceMethods.put('onChildClick(Landroid/widget/ExpandableListView;Landroid/view/View;IIJ)Z', new SensorsAnalyticsMethodCell(
                'onChildClick',
                '(Landroid/widget/ExpandableListView;Landroid/view/View;IIJ)Z',
                'android/widget/ExpandableListView$OnChildClickListener',
                'trackExpandableListViewOnChildClick',
                '(Landroid/widget/ExpandableListView;Landroid/view/View;II)V',
                1, 4,
                [Opcodes.ALOAD, Opcodes.ALOAD, Opcodes.ILOAD, Opcodes.ILOAD]))
        sInterfaceMethods.put('onTabChanged(Ljava/lang/String;)V', new SensorsAnalyticsMethodCell(
                'onTabChanged',
                '(Ljava/lang/String;)V',
                'android/widget/TabHost$OnTabChangeListener',
                'trackTabHost',
                '(Ljava/lang/String;)V',
                1, 1,
                [Opcodes.ALOAD]))

        sInterfaceMethods.put('onTabSelected(Landroid/support/design/widget/TabLayout$Tab;)V', new SensorsAnalyticsMethodCell(
                'onTabSelected',
                '(Landroid/support/design/widget/TabLayout$Tab;)V',
                'android/support/design/widget/TabLayout$OnTabSelectedListener',
                'trackTabLayoutSelected',
                '(Ljava/lang/Object;Ljava/lang/Object;)V',
                0, 2,
                [Opcodes.ALOAD, Opcodes.ALOAD]))

        sInterfaceMethods.put('onTabSelected(Lcom/google/android/material/tabs/TabLayout$Tab;)V', new SensorsAnalyticsMethodCell(
                'onTabSelected',
                '(Lcom/google/android/material/tabs/TabLayout$Tab;)V',
                'com/google/android/material/tabs/TabLayout$OnTabSelectedListener',
                'trackTabLayoutSelected',
                '(Ljava/lang/Object;Ljava/lang/Object;)V',
                0, 2,
                [Opcodes.ALOAD, Opcodes.ALOAD]))

        // Todo: 扩展
    }

    /**
     * Fragment中的方法
     */
    public final
    static HashMap<String, SensorsAnalyticsMethodCell> sFragmentMethods = new HashMap<>()
    static {
        sFragmentMethods.put('onResume()V', new SensorsAnalyticsMethodCell(
                'onResume',
                '()V',
                '',// parent省略，均为 android/app/Fragment 或 android/support/v4/app/Fragment
                'trackFragmentResume',
                '(Ljava/lang/Object;)V',
                0, 1,
                [Opcodes.ALOAD]))
        sFragmentMethods.put('setUserVisibleHint(Z)V', new SensorsAnalyticsMethodCell(
                'setUserVisibleHint',
                '(Z)V',
                '',// parent省略，均为 android/app/Fragment 或 android/support/v4/app/Fragment
                'trackFragmentSetUserVisibleHint',
                '(Ljava/lang/Object;Z)V',
                0, 2,
                [Opcodes.ALOAD, Opcodes.ILOAD]))
        sFragmentMethods.put('onHiddenChanged(Z)V', new SensorsAnalyticsMethodCell(
                'onHiddenChanged',
                '(Z)V',
                '',
                'trackOnHiddenChanged',
                '(Ljava/lang/Object;Z)V',
                0, 2,
                [Opcodes.ALOAD, Opcodes.ILOAD]))
        sFragmentMethods.put('onViewCreated(Landroid/view/View;Landroid/os/Bundle;)V', new SensorsAnalyticsMethodCell(
                'onViewCreated',
                '(Landroid/view/View;Landroid/os/Bundle;)V',
                '',
                'onFragmentViewCreated',
                '(Ljava/lang/Object;Landroid/view/View;Landroid/os/Bundle;)V',
                0, 3,
                [Opcodes.ALOAD, Opcodes.ALOAD, Opcodes.ALOAD]))
    }

    /**
     * android.gradle 3.2.1 版本中，针对 Lambda 表达式处理
     */

    public final
    static HashMap<String, SensorsAnalyticsMethodCell> sLambdaMethods = new HashMap<>()

    static {
        sLambdaMethods.put('(Landroid/view/View;)V', new SensorsAnalyticsMethodCell(
                'onClick',
                '(Landroid/view/View;)V',
                'android/view/View$OnClickListener',
                'trackViewOnClick',
                '(Landroid/view/View;)V',
                1, 1,
                [Opcodes.ALOAD]))
        sLambdaMethods.put('(Landroid/widget/CompoundButton;Z)V', new SensorsAnalyticsMethodCell(
                'onCheckedChanged',
                '(Landroid/widget/CompoundButton;Z)V',
                'android/widget/CompoundButton$OnCheckedChangeListener',
                'trackViewOnClick',
                '(Landroid/view/View;)V',
                1, 1,
                [Opcodes.ALOAD]))
        sLambdaMethods.put('(Landroid/widget/RatingBar;FZ)V', new SensorsAnalyticsMethodCell(
                'onRatingChanged',
                '(Landroid/widget/RatingBar;FZ)V',
                'android/widget/RatingBar$OnRatingBarChangeListener',
                'trackViewOnClick',
                '(Landroid/view/View;)V',
                1, 1,
                [Opcodes.ALOAD]))
        sLambdaMethods.put('(Landroid/widget/SeekBar;)V', new SensorsAnalyticsMethodCell(
                'onStopTrackingTouch',
                '(Landroid/widget/SeekBar;)V',
                'android/widget/SeekBar$OnSeekBarChangeListener',
                'trackViewOnClick',
                '(Landroid/view/View;)V',
                1, 1,
                [Opcodes.ALOAD]))
        sLambdaMethods.put('(Landroid/widget/RadioGroup;I)V', new SensorsAnalyticsMethodCell(
                'onCheckedChanged',
                '(Landroid/widget/RadioGroup;I)V',
                'android/widget/RadioGroup$OnCheckedChangeListener',
                'trackRadioGroup',
                '(Landroid/widget/RadioGroup;I)V',
                1, 2,
                [Opcodes.ALOAD, Opcodes.ILOAD]))
        sLambdaMethods.put('(Landroid/content/DialogInterface;I)V', new SensorsAnalyticsMethodCell(
                'onClick',
                '(Landroid/content/DialogInterface;I)V',
                'android/content/DialogInterface$OnClickListener',
                'trackDialog',
                '(Landroid/content/DialogInterface;I)V',
                1, 2,
                [Opcodes.ALOAD, Opcodes.ILOAD]))
        sLambdaMethods.put('(Landroid/widget/AdapterView;Landroid/view/View;IJ)V', new SensorsAnalyticsMethodCell(
                'onItemClick',
                '(Landroid/widget/AdapterView;Landroid/view/View;IJ)V',
                'android/widget/AdapterView$OnItemClickListener',
                'trackListView',
                '(Landroid/widget/AdapterView;Landroid/view/View;I)V',
                1, 3,
                [Opcodes.ALOAD, Opcodes.ALOAD, Opcodes.ILOAD]))
        sLambdaMethods.put('(Landroid/widget/AdapterView;Landroid/view/View;IJ)V', new SensorsAnalyticsMethodCell(
                'onItemSelected',
                '(Landroid/widget/AdapterView;Landroid/view/View;IJ)V',
                'android/widget/AdapterView$OnItemSelectedListener',
                'trackListView',
                '(Landroid/widget/AdapterView;Landroid/view/View;I)V',
                1, 3,
                [Opcodes.ALOAD, Opcodes.ALOAD, Opcodes.ILOAD]))
        sLambdaMethods.put('(Landroid/widget/ExpandableListView;Landroid/view/View;IJ)Z', new SensorsAnalyticsMethodCell(
                'onGroupClick',
                '(Landroid/widget/ExpandableListView;Landroid/view/View;IJ)Z',
                'android/widget/ExpandableListView$OnGroupClickListener',
                'trackExpandableListViewOnGroupClick',
                '(Landroid/widget/ExpandableListView;Landroid/view/View;I)V',
                1, 3,
                [Opcodes.ALOAD, Opcodes.ALOAD, Opcodes.ILOAD]))
        sLambdaMethods.put('(Landroid/widget/ExpandableListView;Landroid/view/View;IIJ)Z', new SensorsAnalyticsMethodCell(
                'onChildClick',
                '(Landroid/widget/ExpandableListView;Landroid/view/View;IIJ)Z',
                'android/widget/ExpandableListView$OnChildClickListener',
                'trackExpandableListViewOnChildClick',
                '(Landroid/widget/ExpandableListView;Landroid/view/View;II)V',
                1, 4,
                [Opcodes.ALOAD, Opcodes.ALOAD, Opcodes.ILOAD, Opcodes.ILOAD]))
        sLambdaMethods.put('(Ljava/lang/String;)V', new SensorsAnalyticsMethodCell(
                'onTabChanged',
                '(Ljava/lang/String;)V',
                'android/widget/TabHost$OnTabChangeListener',
                'trackTabHost',
                '(Ljava/lang/String;)V',
                1, 1,
                [Opcodes.ALOAD]))

        sLambdaMethods.put('(Landroid/view/MenuItem;)Z', new SensorsAnalyticsMethodCell(
                'onNavigationItemSelected',
                '(Landroid/view/MenuItem;)Z',
                'android/support/design/widget/NavigationView$OnNavigationItemSelectedListener',
                'trackMenuItem',
                '(Ljava/lang/Object;Landroid/view/MenuItem;)V',
                0, 2,
                [Opcodes.ALOAD, Opcodes.ALOAD]))

        //static
        sLambdaMethods.put('(Landroid/view/MenuItem;)Z2', new SensorsAnalyticsMethodCell(
                'onNavigationItemSelected',
                '(Landroid/view/MenuItem;)Z',
                'android/support/design/widget/NavigationView$OnNavigationItemSelectedListener',
                'trackMenuItem',
                '(Landroid/view/MenuItem;)V',
                0, 1,
                [Opcodes.ALOAD]))

        // Todo: 扩展
    }
}