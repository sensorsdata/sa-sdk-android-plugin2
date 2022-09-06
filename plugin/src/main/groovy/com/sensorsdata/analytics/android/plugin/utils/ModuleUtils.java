package com.sensorsdata.analytics.android.plugin.utils;

import com.sensorsdata.analytics.android.plugin.SensorsAnalyticsHookConfig;
import com.sensorsdata.analytics.android.plugin.push.SensorsPushInjected;

import java.net.URLClassLoader;

public class ModuleUtils {
    public static boolean isAutoTrackInstall = false;

    public static boolean isPushModuleInstall = false;

    public static void checkModuleStatus(URLClassLoader classLoader) {
        try {
            isAutoTrackInstall = classLoader.loadClass(SensorsAnalyticsHookConfig.SENSORS_ANALYTICS_API.replace("/", ".")) != null;
            Logger.info("全埋点模块集成状态 = " + isAutoTrackInstall);
        } catch (Exception e) {
            Logger.warn("全埋点模块未集成");
            isAutoTrackInstall = false;
        }

        try {
            isPushModuleInstall = classLoader.loadClass(SensorsPushInjected.PUSH_TRACK_OWNER.replace("/", ".")) != null;
            Logger.info("推送模块集成状态 = " + isAutoTrackInstall);
        } catch (Exception e) {
            Logger.warn("推送模块未集成");
            isPushModuleInstall = false;
        }
    }
}
