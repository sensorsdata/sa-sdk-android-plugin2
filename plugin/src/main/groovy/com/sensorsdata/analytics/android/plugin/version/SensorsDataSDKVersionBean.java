package com.sensorsdata.analytics.android.plugin.version;

import com.google.gson.JsonObject;
import com.sensorsdata.analytics.android.plugin.utils.TextUtil;


public class SensorsDataSDKVersionBean {
    /**
     * 当前 SDK 要求的最小版本（必须）
     */
    private String mMinSensorsDataSDKVersion;
    /**
     * 其他业务 SDK 的版本信息路径，也就是 SensorDataVersionOptions 类的路径
     * 通过 SensorDataVersionOptions 类的路径匹配，当前依赖 SDK 的版本和已有的 SDK 的关系（每一个 SDK 唯一，用于匹配）
     * （必须）
     */
    private String mSensorsDataSDKPath;
    /**
     * 版本不匹配时，提醒信息，可为 null
     */
    private String mMessage;

    public SensorsDataSDKVersionBean(String sensorsSDKPath, String minVersion, String message) {
        this.mSensorsDataSDKPath = sensorsSDKPath;
        this.mMinSensorsDataSDKVersion = minVersion;
        this.mMessage = message;
    }

    public String getSensorsDataSDKPath() {
        return mSensorsDataSDKPath;
    }

    public String getSensorsDataSDKVersionMessage(String version) {
        return !isVersionValid(version, mMinSensorsDataSDKVersion) ? String.format(TextUtil.isEmpty(mMessage) ? SensorsDataSDKVersionHelper.DEFAULT_MESSAGE : mMessage, version, mMinSensorsDataSDKVersion) : "";
    }

    private boolean isVersionValid(String saVersion, String requiredVersion) {
        try {
            if (saVersion.equals(requiredVersion)) {
                return true;
            } else {
                String[] saVersions = saVersion.split("\\.");
                String[] requiredVersions = requiredVersion.split("\\.");
                for (int index = 0; index < requiredVersions.length; index++) {
                    int saVersionsNum = Integer.parseInt(saVersions[index]);
                    int requiredVersionsNum = Integer.parseInt(requiredVersions[index]);
                    if (saVersionsNum != requiredVersionsNum) {
                        return saVersionsNum > requiredVersionsNum;
                    }
                }
                return false;
            }
        } catch (Exception ex) {
            // ignore
            return false;
        }
    }

    public static SensorsDataSDKVersionBean createSensorDataSDKBean(JsonObject jsonObject) {
        if (null != jsonObject) {
            try {
                String path = jsonObject.get("SDK_VERSION_PATH").getAsString();
                String minVersion = jsonObject.get("DEPENDENT_MIN_SDK_VERSIONS").getAsString();
                if (!TextUtil.isEmpty(path) && !TextUtil.isEmpty(minVersion)) {
                    if (path.contains(".")) {
                        path = path.replaceAll("\\.", "/");
                    }
                    String message = jsonObject.get("ERROR_MESSAGE").getAsString();
                    return new SensorsDataSDKVersionBean(path, minVersion, message);
                }
            } catch (Exception e) {
                // ignore
            }
        }
        return null;
    }

    public String toString() {
        return "\tminSensorsDataSDKVersion=" + mMinSensorsDataSDKVersion + "\n" +
                "\tsensorsDataSDKPath=" + mSensorsDataSDKPath + "\n" +
                "\tmessage=" + mMessage;
    }

}
