package com.sensorsdata.analytics.android.demo

import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.sensorsdata.analytics.android.sdk.SALog
import org.json.JSONObject

class H5Activity : BaseActivity() {
    private val TAG: String = "H5Activity"
    private lateinit var androidWebView:AAWebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_h5)
        //x5WebView.addJavascriptInterface(JsObject(), "sensorsDataObj")
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            webView.settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
//        }
        //SensorsDataAPI.sharedInstance().showUpX5WebView(x5WebView, true)


        //SensorsDataAPI.sharedInstance().showUpWebView(androidWebView, true,true)
        //androidWebView.loadUrl("https://869359954.github.io/App_H5_traffic_sdk/index.html")
        //本地新版 H5
        androidWebView = findViewById(R.id.androidWebView)
        androidWebView.loadUrl("file:///android_asset/new_h5_test/index.html")
        //旧版 H5
        //androidWebView.loadUrl("https://fengandyun.github.io/apph5select/index.html")

        //SensorsDataAPI.sharedInstance().showUpWebView(androidWebView, true, true)

    }

    private fun webview() {
        run {
            val webView = AWebView(this)
            val testUrl1 = "hhh"
            webView.loadUrl(testUrl1 + "uiuiuiuiuiuiui")
            webView.loadData("data1", "html", "utf8")
            webView.loadUrl("https://www.ss.cn", mapOf("header" to "h1"))
            webView.loadDataWithBaseURL("http://www.base.cn", "h1=5", "text", "utf8", "http://www.hisotory.cn")
            webView.postUrl("sss" , byteArrayOf())
        }

        run {
            val aaWebView = AAWebView(this)
            val testUrl1 = "hhh"
            aaWebView.loadUrl(testUrl1 + "uiuiuiuiuiuiui")
            aaWebView.loadData("data1", "html", "utf8")
            aaWebView.loadUrl("https://www.ss.cn", mapOf("header" to "h1"))
            aaWebView.loadDataWithBaseURL("http://www.base.cn", "h1=5", "text", "utf8", "http://www.hisotory.cn")
        }

    }

    private class JsObject {
        @JavascriptInterface
        fun track(obj: JSONObject) {
            SALog.i("JsObject", "from h5: $obj")
        }
    }

    private fun testWeb() {
        val webView = WebView(this)
        webView.loadUrl("http://www.baidu.com")
    }

    private fun testWeb2() {
        val webView = AWebView(this)
        webView.loadUrl("http://www.baidu.com")
    }

    private fun testWeb3() {
        val webView = AAWebView(this)
        webView.loadUrl("http://www.baidu.com")
    }
}