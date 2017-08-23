package com.sensorsdata.analytics.android.plugin

import org.aspectj.bridge.IMessage
import org.aspectj.bridge.MessageHandler
import org.aspectj.tools.ajc.Main
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile

class SensorsAnalyticsPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {

        project.repositories.flatDir { dirs 'libs' }

        final def log = project.logger

        project.dependencies {
            compile 'org.aspectj:aspectjrt:1.8.10'
//      compile(name: 'SensorsAnalytics-runtime-release', ext: 'aar')
            compile 'com.sensorsdata.analytics.android:SensorsAnalyticsSDK-Runtime:1.0.1'
        }

        project.extensions.create("sensorsAnalytics", SensorsAnalyticsExtension)

        project.android.applicationVariants.all { variant ->
            JavaCompile javaCompile = variant.javaCompile

            javaCompile.doLast {
                String[] args = [
                        "-showWeaveInfo",
                        "-1.7",
                        "-inpath", javaCompile.destinationDir.toString(),
                        "-aspectpath", javaCompile.classpath.asPath,
                        "-d", javaCompile.destinationDir.toString(),
                        "-classpath", javaCompile.classpath.asPath,
                        "-bootclasspath", project.android.bootClasspath.join(File.pathSeparator)
                ]
                MessageHandler handler = new MessageHandler(true);
                new Main().run(args, handler);

                println()
                println("####################################################################")
                println("########                                                    ########")
                println("########                                                    ########")
                println("########         欢迎使用 SensorsAnalytics® 编译插件        ########")
                println("########          使用过程中碰到任何问题请联系我们          ########")
                println("########                                                    ########")
                println("########                                                    ########")
                println("####################################################################")
                println()

                for (IMessage message : handler.getMessages(null, true)) {
                    switch (message.getKind()) {
                        case IMessage.ABORT:
                        case IMessage.ERROR:
                        case IMessage.FAIL:
                            log.error message.message, message.thrown
                            break;
                        case IMessage.WARNING:
                            log.warn message.message, message.thrown
                            break;
                        case IMessage.INFO:
                            log.info message.message, message.thrown
                            break;
                        case IMessage.DEBUG:
                            log.debug message.message, message.thrown
                            break;
                    }
                }
            }
        }
    }
}