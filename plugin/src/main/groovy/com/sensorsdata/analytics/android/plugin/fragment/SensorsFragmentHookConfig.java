package com.sensorsdata.analytics.android.plugin.fragment;

import com.sensorsdata.analytics.android.plugin.viewclick.SensorsAnalyticsMethodCell;

import org.objectweb.asm.Opcodes;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

public class SensorsFragmentHookConfig {
    public static final String SENSORS_FRAGMENT_TRACK_HELPER_API = "com/sensorsdata/analytics/android/autotrack/aop/FragmentTrackHelper";

    /**
     * Fragment中的方法
     */
    public final static HashMap<String, SensorsAnalyticsMethodCell> FRAGMENT_METHODS = new HashMap<>();

    static {
        FRAGMENT_METHODS.put("onResume()V", new SensorsAnalyticsMethodCell(
                "onResume",
                "()V",
                "",// parent省略，均为 android/app/Fragment 或 android/support/v4/app/Fragment
                "trackFragmentResume",
                "(Ljava/lang/Object;)V",
                0, 1,
                Collections.singletonList(Opcodes.ALOAD)));
        FRAGMENT_METHODS.put("setUserVisibleHint(Z)V", new SensorsAnalyticsMethodCell(
                "setUserVisibleHint",
                "(Z)V",
                "",// parent省略，均为 android/app/Fragment 或 android/support/v4/app/Fragment
                "trackFragmentSetUserVisibleHint",
                "(Ljava/lang/Object;Z)V",
                0, 2,
                Arrays.asList(Opcodes.ALOAD, Opcodes.ILOAD)));
        FRAGMENT_METHODS.put("onHiddenChanged(Z)V", new SensorsAnalyticsMethodCell(
                "onHiddenChanged",
                "(Z)V",
                "",
                "trackOnHiddenChanged",
                "(Ljava/lang/Object;Z)V",
                0, 2,
                Arrays.asList(Opcodes.ALOAD, Opcodes.ILOAD)));
        FRAGMENT_METHODS.put("onViewCreated(Landroid/view/View;Landroid/os/Bundle;)V", new SensorsAnalyticsMethodCell(
                "onViewCreated",
                "(Landroid/view/View;Landroid/os/Bundle;)V",
                "",
                "onFragmentViewCreated",
                "(Ljava/lang/Object;Landroid/view/View;Landroid/os/Bundle;)V",
                0, 3,
                Arrays.asList(Opcodes.ALOAD, Opcodes.ALOAD, Opcodes.ALOAD)));
        FRAGMENT_METHODS.put("onPause()V", new SensorsAnalyticsMethodCell(
                "onPause",
                "()V",
                "",// parent省略，均为 android/app/Fragment 或 android/support/v4/app/Fragment
                "trackFragmentPause",
                "(Ljava/lang/Object;)V",
                0, 1,
                Collections.singletonList(Opcodes.ALOAD)));
    }
}
