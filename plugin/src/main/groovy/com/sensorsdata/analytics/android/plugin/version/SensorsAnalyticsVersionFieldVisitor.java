package com.sensorsdata.analytics.android.plugin.version;

import com.sensorsdata.analytics.android.plugin.ClassNameAnalytics;
import com.sensorsdata.analytics.android.plugin.utils.Logger;
import com.sensorsdata.analytics.android.plugin.SensorsAnalyticsTransform;
import com.sensorsdata.analytics.android.plugin.SensorsAnalyticsUtil;
import com.sensorsdata.analytics.android.plugin.utils.TextUtil;

import org.objectweb.asm.FieldVisitor;

public class SensorsAnalyticsVersionFieldVisitor extends FieldVisitor {
    private String mName, mClassName;
    private Object mValue;
    private SensorsDataSDKVersionHelper mSdkVersionHelper;
    private ClassNameAnalytics mClassNameAnalytics;

    public SensorsAnalyticsVersionFieldVisitor(int api, FieldVisitor fieldVisitor, String name, Object value, SensorsDataSDKVersionHelper sdkVersionHelper, String className, ClassNameAnalytics classNameAnalytics) {
        super(api, fieldVisitor);
        this.mName = name;
        this.mValue = value;
        this.mSdkVersionHelper = sdkVersionHelper;
        this.mClassName = className;
        this.mClassNameAnalytics = classNameAnalytics;
    }

    @Override
    public void visitEnd() {
        if (mClassNameAnalytics.isSensorsDataAPI) {
            if ("VERSION".equals(mName)) {
                String version = (String) mValue;
                if (SensorsAnalyticsUtil.compareVersion(SensorsAnalyticsTransform.MIN_SDK_VERSION, version) > 0) {
                    String errMessage = String.format("你目前集成的神策埋点 SDK 版本号为 v%s，请升级到 v%s 及以上的版本。详情请参考：https://github.com/sensorsdata/sa-sdk-android", version, SensorsAnalyticsTransform.MIN_SDK_VERSION);
                    Logger.error(errMessage);
                    throw new Error(errMessage);
                }
                String message = mSdkVersionHelper.getMessageBySDKCurrentVersion(mClassName, version);
                if (!TextUtil.isEmpty(message)) {
                    throw new Error(message);
                }
            } else if ("MIN_PLUGIN_VERSION".equals(mName)) {
                String minPluginVersion = (String) mValue;
                if (!TextUtil.isEmpty(minPluginVersion)) {
                    if (SensorsAnalyticsUtil.compareVersion(SensorsAnalyticsTransform.VERSION, minPluginVersion) < 0) {
                        String errMessage = String.format("你目前集成的神策插件版本号为 v%s，请升级到 v%s 及以上的版本。详情请参考：https://github.com/sensorsdata/sa-sdk-android-plugin2", SensorsAnalyticsTransform.VERSION, minPluginVersion);
                        Logger.error(errMessage);
                        throw new Error(errMessage);
                    }
                }
            }
        } else if (mClassNameAnalytics.isSensorsDataVersion) {
            if (SensorsDataSDKVersionHelper.VERSION_KEY_CURRENT_VERSION.equals(mName)) {
                String version = (String) mValue;
                String message = mSdkVersionHelper.getMessageBySDKCurrentVersion(mClassName, version);
                if (!TextUtil.isEmpty(message)) {
                    throw new Error(message);
                }
            } else if (SensorsDataSDKVersionHelper.VERSION_KEY_DEPENDENT_SDK_VERSION.equals(mName)) {
                String relatedOtherSDK = (String) mValue;
                String message = mSdkVersionHelper.getMessageBySDKRelyVersion(mClassName, relatedOtherSDK);
                if (!TextUtil.isEmpty(message)) {
                    throw new Error(message);
                }
            }
        }
        super.visitEnd();
    }
}
