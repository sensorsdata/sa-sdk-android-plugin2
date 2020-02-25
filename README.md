# sa-sdk-android-plugin2

The official Android SDK Plugin for Sensors Analytics

## 快速集成

__Gradle 编译环境（Android Studio）__

(1）在 <font color=red size=4 >  **project**  </font>级别的 build.gradle 文件中添加 android-gradle-plugin 依赖：

```android
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:3.2.0'
        //添加 android-gradle-plugin 依赖
        classpath 'com.sensorsdata.analytics.android:android-gradle-plugin2:3.1.4'
    }
}

allprojects {
    repositories {
        jcenter()
    }
}
```

（2）在 <font color=red size=4 > **主 module** </font>的 build.gradle 文件中添加 com.sensorsdata.analytics.android 插件、Sensors Analytics SDK 依赖：

```android
apply plugin: 'com.android.application'
//添加 com.sensorsdata.analytics.android 插件
apply plugin: 'com.sensorsdata.analytics.android'

dependencies {
   //添加 Sensors Analytics SDK 依赖
   compile 'com.sensorsdata.analytics.android:SensorsAnalyticsSDK:3.2.4'
}
```

## To Learn More

See our [full manual](http://www.sensorsdata.cn/manual/android_sdk.html)

## 感谢
[hugo](https://github.com/JakeWharton/hugo)

[gradle_plugin_android_aspectjx](https://github.com/HujiangTechnology/gradle_plugin_android_aspectjx)

[gradle-android-aspectj-plugin](https://github.com/uPhyca/gradle-android-aspectj-plugin)

[tracklytics](https://github.com/orhanobut/tracklytics)


## 讨论

| 扫码加入神策数据开源社区 QQ 群<br>群号：785122381 | 扫码加入神策数据开源社区微信群 |
| ------ | ------ |
|![ QQ 讨论群](https://raw.githubusercontent.com/richardhxy/OpensourceQRCode/master/docs/qrCode_for_qq.jpg) | ![ 微信讨论群 ](https://raw.githubusercontent.com/richardhxy/OpensourceQRCode/master/docs/qrcode_for_wechat.JPG) |

## 公众号

| 扫码关注<br>神策数据开源社区 | 扫码关注<br>神策数据开源社区服务号 |
| ------ | ------ |
|![ 微信订阅号 ](https://raw.githubusercontent.com/richardhxy/OpensourceQRCode/master/docs/qrcode_for_wechat_subscription_account.jpg) | ![ 微信服务号 ](https://raw.githubusercontent.com/richardhxy/OpensourceQRCode/master/docs/qrcode_for_wechat_service_account.jpg) |

## License

Copyright 2015－2020 Sensors Data Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.gradle_plugin_android_aspectjx