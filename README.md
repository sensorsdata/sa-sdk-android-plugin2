<img src="https://ow-file.sensorsdata.cn/www/home/header/sensors_header_icon.svg" width="200" >

## 神策简介

[**神策数据**](https://www.sensorsdata.cn/)
（Sensors Data），隶属于神策网络科技（北京）有限公司，是一家专业的大数据分析服务公司，大数据分析行业开拓者，为客户提供深度用户行为分析平台、以及专业的咨询服务和行业解决方案，致力于帮助客户实现数据驱动。神策数据立足大数据及用户行为分析的技术与实践前沿，业务现已覆盖以互联网、金融、零售快消、高科技、制造等为代表的十多个主要行业、并可支持企业多个职能部门。公司总部在北京，并在上海、深圳、合肥、武汉等地拥有本地化的服务团队，覆盖东区及南区市场；公司拥有专业的服务团队，为客户提供一对一的客户服务。公司在大数据领域积累的核心关键技术，包括在海量数据采集、存储、清洗、分析挖掘、可视化、智能应用、安全与隐私保护等领域。 [**More**](https://www.sensorsdata.cn/about/aboutus.html)


## SDK 简介

SensorsAnalytics SDK 是国内第一家开源商用版用户行为采集 SDK，目前支持代码埋点、全埋点、App 点击图、可视化全埋点等。目前已累计有 1500 多家付费客户，2500+ 的 App 集成使用，作为 App 数据采集利器，致力于帮助客户挖掘更多的商业价值，为其精准运营和业务支撑提供了可靠的数据来源。其采集全面而灵活、性能良好，并一直保持稳定的迭代，经受住了时间和客户的考验。


## 快速集成

__Gradle 编译环境（Android Studio）__

(1）在 <font color=red size=4 >  **project**  </font>级别的 build.gradle 文件中添加 android-gradle-plugin 依赖：

```android
buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:3.2.0'
        //添加 android-gradle-plugin 依赖
        classpath 'com.sensorsdata.analytics.android:android-gradle-plugin2:3.5.4'
    }
}

allprojects {
    repositories {
        mavenCentral()
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
   implementation 'com.sensorsdata.analytics.android:SensorsAnalyticsSDK:6.6.3'
}
```

## To Learn More

See our [full manual](http://www.sensorsdata.cn/manual/android_sdk.html)



## 新书推荐

| [《数据驱动：从方法到实践》](https://item.jd.com/12322322.html) | [《Android 全埋点解决方案》](https://item.jd.com/12574672.html) | [《iOS 全埋点解决方案》](https://item.jd.com/12867068.html)
| ------ | ------ | ------ |

## 感谢
[hugo](https://github.com/JakeWharton/hugo)

[gradle_plugin_android_aspectjx](https://github.com/HujiangTechnology/gradle_plugin_android_aspectjx)

[gradle-android-aspectj-plugin](https://github.com/uPhyca/gradle-android-aspectj-plugin)

[tracklytics](https://github.com/orhanobut/tracklytics)


## License
[License 协议](https://github.com/sensorsdata/sa-sdk-android-plugin2/blob/master/LICENSE)
