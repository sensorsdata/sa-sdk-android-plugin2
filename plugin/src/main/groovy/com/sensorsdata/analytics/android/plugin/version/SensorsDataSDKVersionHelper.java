package com.sensorsdata.analytics.android.plugin.version;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sensorsdata.analytics.android.plugin.SensorsAnalyticsTransform;
import com.sensorsdata.analytics.android.plugin.utils.TextUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SensorsDataSDKVersionHelper {
    public static final String VERSION_KEY_SENSORDATA_VERSION_CONFIG = "SensorsDataVersionConfig";
    public static final String VERSION_KEY_CURRENT_VERSION = "SDK_VERSION";
    public static final String VERSION_KEY_DEPENDENT_SDK_VERSION = "DEPENDENT_SDK_VERSIONS";
    public static final String VERSION_KEY_PLUGIN_SDK_PATH = "com/sensorsdata/analytics/android/plugin";
    public static final String DEFAULT_MESSAGE = "当前神策 Android SDK 版本 %s 过低，请升级至 %s 及其以上版本后进行使用";

    private Map<String, List<SensorsDataSDKVersionBean>> mDependentVersionMap = new HashMap<>();
    private Map<String, String> mCurrentVersionMap = new HashMap<>();

    public SensorsDataSDKVersionHelper() {
        mCurrentVersionMap.put(VERSION_KEY_PLUGIN_SDK_PATH, SensorsAnalyticsTransform.VERSION);
    }

    /**
     * 获取版本兼容提示信息（在 SDK 获取依赖版本信息时调用）
     *
     * @param classname 当前 SDK 版本信息路径
     * @param relatedVersion 当前 SDK 依赖 SDK 的版本信息
     * @return 提示 message，如果为 "" 表示版本兼容。
     */
    public String getMessageBySDKRelyVersion(String classname, String relatedVersion) {
        if (!TextUtil.isEmpty(classname) && !TextUtil.isEmpty(relatedVersion)) {
            JsonElement jsonElement = new JsonParser().parse(relatedVersion);
            if (null != jsonElement) {
                if (jsonElement.isJsonArray()) {
                    JsonArray jsonArray = jsonElement.getAsJsonArray();
                    if (null != jsonArray && jsonArray.size() > 0) {
                        List<SensorsDataSDKVersionBean> sensorSDKBeanList = new ArrayList<>();
                        for (int i = 0; i < jsonArray.size(); i++) {
                            JsonElement jsonElementSon = jsonArray.get(i);
                            if (jsonElementSon.isJsonObject()) {
                                JsonObject jsonObjectSon = jsonElementSon.getAsJsonObject();
                                SensorsDataSDKVersionBean sensorSDKBean = SensorsDataSDKVersionBean.createSensorDataSDKBean(jsonObjectSon);
                                if (null != sensorSDKBean) {
                                    sensorSDKBeanList.add(sensorSDKBean);
                                }
                            }
                        }
                        if (sensorSDKBeanList.size() > 0) {
                            mDependentVersionMap.put(classname, sensorSDKBeanList);
                        }
                    }
                }
            }
        }
        if (null != mCurrentVersionMap && mCurrentVersionMap.size() > 0) {
            //对比版本了
            return checkSensorsSDKVersionOnAnalytic();
        }
        return "";
    }

    /**
     * 获取版本兼容提示信息（在 SDK 获取版本时调用 ）
     *
     * @param classname 当前 SDK 版本的信息的类路径，每一个 SDK 唯一，用于匹配
     * @param currentVersion 当前的版本
     * @return 提示 message，如果为 "" 表示版本兼容。
     */
    public String getMessageBySDKCurrentVersion(String classname, String currentVersion) {
        mCurrentVersionMap.put(classname, currentVersion);
        return mDependentVersionMap.size() > 0 ? checkSensorsSDKVersionOnAnalytic() : "";
    }

    /**
     * 真正检测 SDK 版本匹配
     *
     * @return 提示 message，如果为 "" 表示版本兼容。
     */
    private String checkSensorsSDKVersionOnAnalytic() {
        for (List<SensorsDataSDKVersionBean> beanList : mDependentVersionMap.values()) {
            if (beanList.size() > 0) {
                Iterator<SensorsDataSDKVersionBean> itList = beanList.iterator();
                while (itList.hasNext()) {
                    SensorsDataSDKVersionBean sensorSDKBean = itList.next();
                    if (mCurrentVersionMap.containsKey(sensorSDKBean.getSensorsDataSDKPath())) {
                        String message = sensorSDKBean.getSensorsDataSDKVersionMessage(mCurrentVersionMap.get(sensorSDKBean.getSensorsDataSDKPath()));
                        itList.remove();
                        if (!TextUtil.isEmpty(message)) {
                            return message;
                        }
                    }
                }
            }
        }
        return "";
    }
}
