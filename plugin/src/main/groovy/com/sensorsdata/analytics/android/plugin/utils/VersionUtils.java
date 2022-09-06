package com.sensorsdata.analytics.android.plugin.utils;

import com.sensorsdata.analytics.android.plugin.utils.Logger;

import java.lang.reflect.Field;
import java.net.URLClassLoader;

public class VersionUtils {
    // 是否打开 TV 开关
    public static boolean isAndroidTv;
    // 神策埋点 SDK 版本号
    public static String sensorsSDKVersion = "";

    /**
     * 是否是 TV 版本
     * @return true 是，false 不是
     */
    public static boolean isTvVersion() {
        return isAndroidTv && sensorsSDKVersion.endsWith("tv");
    }

    /**
     * 读取神策 Android 埋点 SDK 版本号
     * @param urlClassLoader ClassLoader
     */
    public static void loadAndroidSDKVersion(URLClassLoader urlClassLoader) {
        try {
            Class sensorsDataAPI = urlClassLoader.loadClass("com.sensorsdata.analytics.android.sdk.SensorsDataAPI");
            Field versionField = sensorsDataAPI.getDeclaredField("VERSION");
            versionField.setAccessible(true);
            sensorsSDKVersion = (String) versionField.get(null);
            Logger.info("神策埋点 SDK 版本号:" + sensorsSDKVersion);
        } catch(Throwable throwable) {
            Logger.info("神策埋点 SDK 版本号读取失败，reason: " + throwable.getMessage());
        }
    }
}
