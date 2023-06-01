package com.sensorsdata.analytics.android.demo

import android.app.Application
import android.util.Log
import com.sensorsdata.analytics.android.sdk.SAConfigOptions
import com.sensorsdata.analytics.android.sdk.SensorsAnalyticsAutoTrackEventType
import com.sensorsdata.analytics.android.sdk.SensorsDataAPI

class App: Application() {
    /**
     * Sensors Analytics 采集数据的地址
     */
    private val SA_SERVER_URL = "https://sdkdebugtest.datasink.sensorsdata.cn/sa?project=default"

    override fun onCreate() {
        super.onCreate()
        initSensorsDataAPI()
    }

    /**
     * 初始化 Sensors Analytics SDK
     */
    private fun initSensorsDataAPI() {
        val configOptions = SAConfigOptions(SA_SERVER_URL)
        // 打开自动采集, 并指定追踪哪些 AutoTrack 事件
        configOptions.setAutoTrackEventType(
            SensorsAnalyticsAutoTrackEventType.APP_START or
                    SensorsAnalyticsAutoTrackEventType.APP_END or
                    SensorsAnalyticsAutoTrackEventType.APP_VIEW_SCREEN or
                    SensorsAnalyticsAutoTrackEventType.APP_CLICK
        )
            .enableTrackAppCrash()
            .enableJavaScriptBridge(true)
            .enableSaveDeepLinkInfo(true)
            .enableAutoAddChannelCallbackEvent(true)
            .enableVisualizedProperties(true)
            .enableLog(true)
            .enableVisualizedAutoTrack(true)
        SensorsDataAPI.startWithConfigOptions(this, configOptions)
        SensorsDataAPI.sharedInstance(this).trackFragmentAppViewScreen()
        SensorsDataAPI.sharedInstance().trackAppInstall()
        Log.d("SA.Preset", SensorsDataAPI.sharedInstance().getPresetProperties().toString())
    }
}