package com.sensorsdata.analytics.android.plugin

class ClassNameAnalytics {

    public String className

    boolean isShouldModify = false

    boolean isSensorsDataAPI = false

    boolean isSensorsDataUtils = false

    boolean isSALog = false

    def methodCells = new ArrayList<SensorsAnalyticsMethodCell>()

    ClassNameAnalytics (String className) {
        this.className = className
        isSensorsDataAPI = (className == 'com.sensorsdata.analytics.android.sdk.SensorsDataAPI')
        isSensorsDataUtils = (className == 'com.sensorsdata.analytics.android.sdk.util.SensorsDataUtils')
        isSALog = (className == 'com.sensorsdata.analytics.android.sdk.SALog')
    }

    boolean isSDKFile() {
        return isSALog || isSensorsDataAPI || isSensorsDataUtils
    }

    boolean isViewPager() {
        return className == 'android.support.v4.view.ViewPager' || className == 'androidx.viewpager.widget.ViewPager'
    }

    boolean isAndroidGenerated() {
        return className.contains('R$') ||
                className.contains('R2$') ||
                className.contains('R.class') ||
                className.contains('R2.class') ||
                className.contains('BuildConfig.class')
    }

}