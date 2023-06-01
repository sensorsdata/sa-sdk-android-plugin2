package com.sensorsdata.analytics.android.plugin.manager

import com.sensorsdata.analytics.android.gradle.replaceDotBySlash
import com.sensorsdata.analytics.android.gradle.replaceSlashByDot
import com.sensorsdata.analytics.android.plugin.extension.SAExtension

/**
 * A manager that handle include、exclude、ignored and special classes.
 */
class SAPackageManager {

    val ignoreClass = mutableSetOf("keyboard")

    val exclude = mutableSetOf(
        "com.sensorsdata.analytics.android.sdk",
        "android.support",
        "androidx",
        "com.qiyukf",
        "android.arch",
        "com.google.android",
        "com.tencent.smtt",
        "com.umeng.message",
        "com.xiaomi.push",
        "com.huawei.hms",
        "cn.jpush.android",
        "cn.jiguang",
        "com.meizu.cloud.pushsdk",
        "com.vivo.push",
        "com.igexin",
        "com.getui",
        "com.xiaomi.mipush.sdk",
        "com.heytap.msp.push",
        "com.bumptech.glide",
        "com.tencent.tinker"
    )

    val include = mutableSetOf(
        "butterknife.internal.DebouncingOnClickListener",
        "com.jakewharton.rxbinding.view.ViewClickOnSubscribe",
        "com.facebook.react.uimanager.NativeViewHierarchyManager"
    )

    private val special = mutableSetOf(
        "android.support.design.widget.TabLayout\$ViewPagerOnTabSelectedListener",
        "com.google.android.material.tabs.TabLayout\$ViewPagerOnTabSelectedListener",
        "android.support.v7.app.ActionBarDrawerToggle",
        "androidx.appcompat.app.ActionBarDrawerToggle",
        "androidx.fragment.app.FragmentActivity",
        "androidx.core.app.NotificationManagerCompat",
        "androidx.core.app.ComponentActivity",
        "android.support.v4.app.NotificationManagerCompat",
        "android.support.v4.app.SupportActivity",
        "cn.jpush.android.service.PluginMeizuPlatformsReceiver",
        "androidx.appcompat.widget.ActionMenuPresenter\$OverflowMenuButton",
        "android.widget.ActionMenuPresenter\$OverflowMenuButton",
        "android.support.v7.widget.ActionMenuPresenter\$OverflowMenuButton",

        //sensorsdata special
        "com.sensorsdata.analytics.android.sdk.util.SensorsDataUtils",
        "com.sensorsdata.analytics.android.sdk.advert.oaid.SAOaidHelper",
        "com.sensorsdata.analytics.android.sdk.SensorsDataAPI",
        "com.sensorsdata.analytics.android.sdk.SALog",
        "com.sensorsdata.analytics.android.sdk.jsbridge.AppWebViewInterface",
        "com.sensorsdata.analytics.android.sdk.visual.WebViewVisualInterface"

    )

    private val specialClassDescriptorSet: MutableSet<String> by lazy {
        mutableSetOf<String>().apply {
            addAll(special.map { it.replaceDotBySlash() }.toSet())
        }
    }

    fun isInstrument(clazzName: String, extension: SAExtension): Boolean {
        val newClazzName = clazzName.replaceSlashByDot()
        //special
        for (item in special) {
            if (newClazzName.startsWith(item)) {
                return true
            }
        }
        //include
        if (extension.useInclude) {
            for (item in include) {
                if (newClazzName.startsWith(item)) {
                    return true
                }
            }
            return false
        }
        //exclude
        else {
            for (item in exclude) {
                if (newClazzName.startsWith(item)) {
                    return false
                }
            }
            //keyboard
            if (extension.disableTrackKeyboard) {
                for (item in ignoreClass) {
                    if (newClazzName.lowercase().contains(item)) {
                        return false
                    }
                }
            }
        }
        return true
    }

    fun isTargetClassInSpecial(className: String): Boolean {
        return specialClassDescriptorSet.contains(className)
    }


    companion object {
        private val targetFragmentClass: MutableSet<String> by lazy {
            mutableSetOf<String>().apply {
                /**
                 * For Android App Fragment
                 */
                add("android/app/Fragment")
                add("android/app/ListFragment")
                add("android/app/DialogFragment")

                /**
                 * For Support V4 Fragment
                 */
                add("android/support/v4/app/Fragment")
                add("android/support/v4/app/ListFragment")
                add("android/support/v4/app/DialogFragment")

                /**
                 * For AndroidX Fragment
                 */
                add("androidx/fragment/app/Fragment")
                add("androidx/fragment/app/ListFragment")
                add("androidx/fragment/app/DialogFragment")
                add("androidx/appcompat/app/AppCompatDialogFragment")

                add("com/google/android/material/bottomsheet/BottomSheetDialogFragment")
            }
        }

        private val targetMenuMethodDesc: MutableSet<String> by lazy {
            mutableSetOf<String>().apply {
                /**
                 * Menu
                 */
                add("onContextItemSelected(Landroid/view/MenuItem;)Z")
                add("onOptionsItemSelected(Landroid/view/MenuItem;)Z")
            }
        }

        private val targetActivityClass: MutableSet<String> by lazy {
            mutableSetOf<String>().apply {
                add("android/app/Activity")
                add("android/support/v7/app/AppCompatActivity")
                add("androidx/appcompat/app/AppCompatActivity")
            }
        }


        fun isInstanceOfActivity(superName: String?): Boolean {
            return targetActivityClass.contains(superName)
        }

        fun isTargetMenuMethodDesc(nameDesc: String?): Boolean {
            return targetMenuMethodDesc.contains(nameDesc)
        }

        fun isInstanceOfFragment(superName: String?): Boolean {
            return targetFragmentClass.contains(superName)
        }
    }
}