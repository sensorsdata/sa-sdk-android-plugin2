/**Created by renqingyou on 2018/12/01.
 * Copyright © 2015－2019 Sensors Data Inc. All rights reserved. */

package com.sensorsdata.analytics.android.plugin

class SensorsAnalyticsTransformHelper {

    SensorsAnalyticsExtension extension

    SensorsAnalyticsSDKHookConfig sensorsAnalyticsHookConfig

    boolean disableSensorsAnalyticsMultiThread

    boolean disableSensorsAnalyticsIncremental

    HashSet<String> exclude = ['com.sensorsdata.analytics.android.sdk', 'android.support', 'androidx','com.qiyukf', 'android.arch']
    HashSet<String> include = ['butterknife.internal.DebouncingOnClickListener',
                                              'com.jakewharton.rxbinding.view.ViewClickOnSubscribe',
                                              'com.facebook.react.uimanager.NativeViewHierarchyManager']

    SensorsAnalyticsTransformHelper(SensorsAnalyticsExtension extension) {
        this.extension = extension
    }

    void onTransform() {
        println("sensorsAnalytics {\n"+extension+"\n}")
        ArrayList<String> excludePackages = extension.exclude
        if (excludePackages != null) {
            exclude.addAll(excludePackages)
        }
        ArrayList<String> includePackages = extension.include
        if (includePackages != null) {
            include.addAll(includePackages)
        }
        createSensorsAnalyticsHookConfig()
    }

    private void createSensorsAnalyticsHookConfig() {
        sensorsAnalyticsHookConfig = new SensorsAnalyticsSDKHookConfig()
        List<MetaProperty> metaProperties = SensorsAnalyticsSDKExtension.getMetaClass().properties
        for (it in metaProperties) {
            if (it.name == 'class') {
                continue
            }
            if (extension.sdk."${it.name}") {
                sensorsAnalyticsHookConfig."${it.name}"(it.name)
            }
        }
    }

    ClassNameAnalytics analytics(String className) {

        ClassNameAnalytics classNameAnalytics = new ClassNameAnalytics(className)

        if (classNameAnalytics.isSDKFile()) {
            def cellHashMap = sensorsAnalyticsHookConfig.methodCells
            cellHashMap.each {
                key,value->
                    def methodCellList = value.get(className.replace('.','/'))
                    if (methodCellList != null) {
                        classNameAnalytics.methodCells.addAll(methodCellList)
                    }

            }
            if (classNameAnalytics.methodCells.size() > 0 || classNameAnalytics.isSensorsDataAPI) {
                classNameAnalytics.isShouldModify = true
            }
        } else if (!classNameAnalytics.isAndroidGenerated()) {
            if (extension.useInclude) {
                for (pkgName in include) {
                    if (className.startsWith(pkgName)) {
                        classNameAnalytics.isShouldModify = true
                        break
                    }
                }
            } else {
                classNameAnalytics.isShouldModify = true
                if (!classNameAnalytics.isLeanback()) {
                    for (pkgName in exclude) {
                        if (className.startsWith(pkgName)) {
                            classNameAnalytics.isShouldModify = false
                            break
                        }
                    }
                }
            }
        }
        return classNameAnalytics
    }
}

