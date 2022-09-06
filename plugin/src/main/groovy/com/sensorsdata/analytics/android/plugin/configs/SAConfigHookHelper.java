package com.sensorsdata.analytics.android.plugin.configs;

import com.sensorsdata.analytics.android.plugin.core.HookConstant;
import com.sensorsdata.analytics.android.plugin.viewclick.SensorsAnalyticsMethodCell;

import org.objectweb.asm.ClassVisitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SAConfigHookHelper {
    // 插件配置项，全局共用
    public static HashMap<String, HashMap<String, List<SensorsAnalyticsMethodCell>>> mConfigCells = new HashMap<>();
    // 当前 class 文件对应的控制项，单个 class 文件共用
    public static ArrayList<SensorsAnalyticsMethodCell> sClassInConfigCells = new ArrayList<>();
    // 扫描当前类命中的控制项
    private final ArrayList<SensorsAnalyticsMethodCell> mHookMethodCells = new ArrayList<>();
    public SAConfigHookHelper() {

    }

    public static void initSDKConfigCells(SensorsAnalyticsSDKExtension sdkExtension) {
        mConfigCells.clear();
        if (sdkExtension.disableAndroidID) {
            HashMap<String, List<SensorsAnalyticsMethodCell>> cells = new HashMap<>();
            cells.put(HookConstant.SENSORS_DATA_UTILS, SensorsAnalyticsSDKHookConfig.disableAndroidID());
            mConfigCells.put("disableAndroidID", cells);
        }

        if (sdkExtension.disableCarrier) {
            HashMap<String, List<SensorsAnalyticsMethodCell>> cells = new HashMap<>();
            cells.put(HookConstant.SENSORS_DATA_UTILS, SensorsAnalyticsSDKHookConfig.disableCarrier());
            mConfigCells.put("disableCarrier", cells);
        }

        if (sdkExtension.disableIMEI) {
            HashMap<String, List<SensorsAnalyticsMethodCell>> cells = new HashMap<>();
            cells.put(HookConstant.SENSORS_DATA_UTILS, SensorsAnalyticsSDKHookConfig.disableIMEI());
            mConfigCells.put("disableIMEI", cells);
        }

        if (sdkExtension.disableMacAddress) {
            HashMap<String, List<SensorsAnalyticsMethodCell>> cells = new HashMap<>();
            cells.put(HookConstant.SENSORS_DATA_UTILS, SensorsAnalyticsSDKHookConfig.disableMacAddress());
            mConfigCells.put("disableMacAddress", cells);
        }

        if (sdkExtension.disableOAID) {
            HashMap<String, List<SensorsAnalyticsMethodCell>> cells = new HashMap<>();
            cells.put(HookConstant.OAID_HELPER, SensorsAnalyticsSDKHookConfig.disableOAID());
            mConfigCells.put("disableOAID", cells);
        }

        if (sdkExtension.disableJsInterface) {
            HashMap<String, List<SensorsAnalyticsMethodCell>> cells = new HashMap<>();
            cells.put(HookConstant.SENSORS_DATA_API, SensorsAnalyticsSDKHookConfig.disableJsInterface());
            mConfigCells.put("disableJsInterface", cells);
        }

        if (sdkExtension.disableLog) {
            HashMap<String, List<SensorsAnalyticsMethodCell>> cells = new HashMap<>();
            cells.put(HookConstant.SA_LOG, SensorsAnalyticsSDKHookConfig.disableLog());
            mConfigCells.put("disableLog", cells);
        }
    }

    public static void initConfigCellInClass(String className) {
        sClassInConfigCells.clear();
        for(HashMap<String, List<SensorsAnalyticsMethodCell>> cell : mConfigCells.values()) {
            if (cell.containsKey(className)) {
                sClassInConfigCells.addAll(cell.get(className));
            }
        }
    }

    /**
     * 判断方法是不是 disablexx 配置方法
     */
    public boolean isConfigsMethod(String name, String desc) {
        for (SensorsAnalyticsMethodCell methodCell : sClassInConfigCells) {
            if (methodCell.getName().equals(name) && methodCell.getDesc().equals(desc)) {
                mHookMethodCells.add(methodCell);
                return true;
            }
        }
        return false;
    }

    /**
     * 清空方法体
     */
    public void disableIdentifierMethod(ClassVisitor classVisitor) {
        for (SensorsAnalyticsMethodCell cell : mHookMethodCells) {
            if ("createGetIMEI".equals(cell.getAgentName())) {
                SensorsAnalyticsSDKHookConfig.createGetIMEI(classVisitor, cell);
            } else if ("createGetDeviceID".equals(cell.getAgentName())) {
                SensorsAnalyticsSDKHookConfig.createGetDeviceID(classVisitor, cell);
            } else if ("createGetAndroidID".equals(cell.getAgentName())) {
                SensorsAnalyticsSDKHookConfig.createGetAndroidID(classVisitor, cell);
            } else if ("createGetMacAddress".equals(cell.getAgentName())) {
                SensorsAnalyticsSDKHookConfig.createGetMacAddress(classVisitor, cell);
            } else if ("createGetCarrier".equals(cell.getAgentName())) {
                SensorsAnalyticsSDKHookConfig.createGetCarrier(classVisitor, cell);
            } else if ("createGetOAID".equals(cell.getAgentName())) {
                SensorsAnalyticsSDKHookConfig.createGetOAID(classVisitor, cell);
            } else if ("createSALogInfo".equals(cell.getAgentName())) {
                SensorsAnalyticsSDKHookConfig.createSALogInfo(classVisitor, cell);
            } else if ("createPrintStackTrack".equals(cell.getAgentName())) {
                SensorsAnalyticsSDKHookConfig.createPrintStackTrack(classVisitor, cell);
            } else if ("createShowUpWebViewFour".equals(cell.getAgentName())) {
                SensorsAnalyticsSDKHookConfig.createShowUpWebViewFour(classVisitor, cell);
            } else if ("createShowUpX5WebViewFour".equals(cell.getAgentName())) {
                SensorsAnalyticsSDKHookConfig.createShowUpX5WebViewFour(classVisitor, cell);
            } else if ("createShowUpX5WebViewTwo".equals(cell.getAgentName())) {
                SensorsAnalyticsSDKHookConfig.createShowUpX5WebViewTwo(classVisitor, cell);
            }
        }
    }
}
