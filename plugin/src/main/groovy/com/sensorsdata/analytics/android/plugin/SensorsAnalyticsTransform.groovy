/*
 * Created by wangzhuozhou on 2015/08/12.
 * Copyright 2015－2022 Sensors Data Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sensorsdata.analytics.android.plugin

import com.android.build.api.transform.Context
import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.Format
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Status
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInput
import com.android.build.api.transform.TransformInvocation
import com.android.build.api.transform.TransformOutputProvider
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.ide.common.internal.WaitableExecutor
import com.sensorsdata.analytics.android.plugin.utils.VersionUtils
import com.sensorsdata.analytics.android.plugin.version.SensorsDataSDKVersionHelper
import groovy.io.FileType
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter

import java.lang.reflect.Field
import java.security.CodeSource
import java.security.ProtectionDomain
import java.util.concurrent.Callable
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream

class SensorsAnalyticsTransform extends Transform {
    private SensorsAnalyticsTransformHelper transformHelper
    public static final String VERSION = "3.4.9"
    public static final String MIN_SDK_VERSION = "5.4.3"
    private WaitableExecutor waitableExecutor
    private URLClassLoader urlClassLoader
    // “com.sensorsdata.analytics.android.sdk.SensorsDataAPI” 类所在路径
    private String sensorsSdkJarPath
    private volatile boolean isFoundSDKJar = false
    private boolean isProjectLibrary = false
    private SensorsDataSDKVersionHelper sdkVersionHelper;

    SensorsAnalyticsTransform(SensorsAnalyticsTransformHelper transformHelper, boolean isProjectLibrary) {
        this.transformHelper = transformHelper
        this.isProjectLibrary = isProjectLibrary
        this.sdkVersionHelper = new SensorsDataSDKVersionHelper()
        if (!transformHelper.disableSensorsAnalyticsMultiThread) {
            waitableExecutor = WaitableExecutor.useGlobalSharedThreadPool()
        }
    }

    @Override
    String getName() {
        return "sensorsAnalyticsAutoTrack"
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<QualifiedContent.Scope> getScopes() {
        return isProjectLibrary ? TransformManager.PROJECT_ONLY : TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return !transformHelper.disableSensorsAnalyticsIncremental
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        beforeTransform(transformInvocation)
        transformClass(transformInvocation.context, transformInvocation.inputs, transformInvocation.outputProvider, transformInvocation.incremental)
        afterTransform()
    }

    private void transformClass(Context context, Collection<TransformInput> inputs, TransformOutputProvider outputProvider, boolean isIncremental)
            throws IOException, TransformException, InterruptedException {
        long startTime = System.currentTimeMillis()
        if (!isIncremental) {
            outputProvider.deleteAll()
        }

        //遍历输入文件
        inputs.each { TransformInput input ->
            //遍历 jar
            input.jarInputs.each { JarInput jarInput ->
                if (waitableExecutor) {
                    waitableExecutor.execute(new Callable<Object>() {
                        @Override
                        Object call() throws Exception {
                            forEachJar(isIncremental, jarInput, outputProvider, context)
                            return null
                        }
                    })
                } else {
                    forEachJar(isIncremental, jarInput, outputProvider, context)
                }
            }

            //遍历目录
            input.directoryInputs.each { DirectoryInput directoryInput ->
                if (waitableExecutor) {
                    waitableExecutor.execute(new Callable<Object>() {
                        @Override
                        Object call() throws Exception {
                            forEachDirectory(isIncremental, directoryInput, outputProvider, context)
                            return null
                        }
                    })
                } else {
                    forEachDirectory(isIncremental, directoryInput, outputProvider, context)
                }
            }
        }
        if (waitableExecutor) {
            waitableExecutor.waitForTasksWithQuickFail(true)
        }
        println("[SensorsAnalytics]: 此次编译共耗时:${System.currentTimeMillis() - startTime}毫秒")
    }

    private void beforeTransform(TransformInvocation transformInvocation) {
        //打印提示信息
        Logger.printCopyright()
        Logger.setDebug(transformHelper.extension.debug)
        transformHelper.onTransform()
        println("[SensorsAnalytics]: 是否开启多线程编译:${!transformHelper.disableSensorsAnalyticsMultiThread}")
        println("[SensorsAnalytics]: 是否开启增量编译:${!transformHelper.disableSensorsAnalyticsIncremental}")
        println("[SensorsAnalytics]: 此次是否增量编译:$transformInvocation.incremental")
        println("[SensorsAnalytics]: 是否在方法进入时插入代码:${transformHelper.isHookOnMethodEnter}")

        traverseForClassLoader(transformInvocation)
    }

    private void afterTransform() {
        try {
            if (urlClassLoader != null) {
                urlClassLoader.close()
                urlClassLoader = null
            }
        } catch (Exception e) {
            e.printStackTrace()
        }
    }

    private void traverseForClassLoader(TransformInvocation transformInvocation) {
        def urlList = []
        def androidJar = transformHelper.androidJar()
        urlList << androidJar.toURI().toURL()
        transformInvocation.inputs.each { transformInput ->
            transformInput.jarInputs.each { jarInput ->
                urlList << jarInput.getFile().toURI().toURL()
            }

            transformInput.directoryInputs.each { directoryInput ->
                urlList << directoryInput.getFile().toURI().toURL()
            }
        }
        def urlArray = urlList as URL[]
        urlClassLoader = new URLClassLoader(urlArray)
        transformHelper.urlClassLoader = urlClassLoader
        checkRNState()
        VersionUtils.loadAndroidSDKVersion(urlClassLoader)
        if(!isProjectLibrary) {
            checkSensorsSDK()
        }
    }

    private void checkSensorsSDK() {
        try {
            Class sdkClazz = urlClassLoader.loadClass("com.sensorsdata.analytics.android.sdk.SensorsDataAPI")
            ProtectionDomain pd = sdkClazz.getProtectionDomain()
            CodeSource cs = pd.getCodeSource()
            sensorsSdkJarPath = cs.getLocation().toURI().getPath()
        } catch (ClassNotFoundException ignored) {
            if (!transformHelper.extension.disableCheckSDK) {
                throw new IllegalStateException("未检测到神策 Android SDK，请参考如下文档检查集成步骤是否正确：\n" +
                        "https://manual.sensorsdata.cn/sa/latest/tech_sdk_client_android_basic-32506144.html\n" +
                        "如需关闭此提示，请添加插件配置: disableCheckSDK=true")
            } else {
                Logger.warn("Can not load find SensorsData SDK jar's path.")
            }
        } catch (Throwable ignored) {
            Logger.warn("Can not load find SensorsData SDK jar's path.")
        }
    }

    private void checkRNState() {
        try {
            Class rnClazz = urlClassLoader.loadClass("com.sensorsdata.analytics.RNSensorsAnalyticsPackage")
            try {
                Field versionField = rnClazz.getDeclaredField("VERSION")
                versionField.setAccessible(true)
                transformHelper.rnVersion = versionField.get(null) as String
                transformHelper.rnState = SensorsAnalyticsTransformHelper.RN_STATE.HAS_VERSION
            } catch (Exception e) {
                transformHelper.rnState = SensorsAnalyticsTransformHelper.RN_STATE.NO_VERSION
            }
        } catch (Exception e) {
            transformHelper.rnState = SensorsAnalyticsTransformHelper.RN_STATE.NOT_FOUND
        }
    }

    void forEachDirectory(boolean isIncremental, DirectoryInput directoryInput, TransformOutputProvider outputProvider, Context context) {
        File dir = directoryInput.file
        File dest = outputProvider.getContentLocation(directoryInput.getName(),
                directoryInput.getContentTypes(), directoryInput.getScopes(),
                Format.DIRECTORY)
        FileUtils.forceMkdir(dest)
        String srcDirPath = dir.absolutePath
        String destDirPath = dest.absolutePath
        if (isIncremental) {
            Map<File, Status> fileStatusMap = directoryInput.getChangedFiles()
            for (Map.Entry<File, Status> changedFile : fileStatusMap.entrySet()) {
                Status status = changedFile.getValue()
                File inputFile = changedFile.getKey()
                String destFilePath = inputFile.absolutePath.replace(srcDirPath, destDirPath)
                File destFile = new File(destFilePath)
                switch (status) {
                    case Status.NOTCHANGED:
                        break
                    case Status.REMOVED:
                        Logger.info("目录 status = $status:$inputFile.absolutePath")
                        if (destFile.exists()) {
                            //noinspection ResultOfMethodCallIgnored
                            destFile.delete()
                        }
                        break
                    case Status.ADDED:
                    case Status.CHANGED:
                        Logger.info("目录 status = $status:$inputFile.absolutePath")
                        File modified = modifyClassFile(dir, inputFile, context.getTemporaryDir())
                        if (destFile.exists()) {
                            destFile.delete()
                        }
                        if (modified != null) {
                            FileUtils.copyFile(modified, destFile)
                            modified.delete()
                        } else {
                            FileUtils.copyFile(inputFile, destFile)
                        }
                        break
                    default:
                        break
                }
            }
        } else {
            FileUtils.copyDirectory(dir, dest)
            dir.traverse(type: FileType.FILES, nameFilter: ~/.*\.class/) {
                File inputFile ->
                    forEachDir(dir, inputFile, context, srcDirPath, destDirPath)
            }
        }
    }

    void forEachDir(File dir, File inputFile, Context context, String srcDirPath, String destDirPath) {
        File modified = modifyClassFile(dir, inputFile, context.getTemporaryDir())
        if (modified != null) {
            File target = new File(inputFile.absolutePath.replace(srcDirPath, destDirPath))
            if (target.exists()) {
                target.delete()
            }
            FileUtils.copyFile(modified, target)
            modified.delete()
        }
    }

    void forEachJar(boolean isIncremental, JarInput jarInput, TransformOutputProvider outputProvider, Context context) {
        String destName = jarInput.file.name
        //截取文件路径的 md5 值重命名输出文件，因为可能同名，会覆盖
        def hexName = DigestUtils.md5Hex(jarInput.file.absolutePath).substring(0, 8)
        if (destName.endsWith(".jar")) {
            destName = destName.substring(0, destName.length() - 4)
        }
        //获得输出文件
        File destFile = outputProvider.getContentLocation(destName + "_" + hexName, jarInput.contentTypes, jarInput.scopes, Format.JAR)
        if (isIncremental) {
            Status status = jarInput.getStatus()
            switch (status) {
                case Status.NOTCHANGED:
                    break
                case Status.ADDED:
                case Status.CHANGED:
                    Logger.info("jar status = $status:$destFile.absolutePath")
                    transformJar(destFile, jarInput, context)
                    break
                case Status.REMOVED:
                    Logger.info("jar status = $status:$destFile.absolutePath")
                    if (destFile.exists()) {
                        FileUtils.forceDelete(destFile)
                    }
                    break
                default:
                    break
            }
        } else {
            transformJar(destFile, jarInput, context)
        }
    }

    void transformJar(File dest, JarInput jarInput, Context context) {
        def modifiedJar = null
        if (!transformHelper.extension.disableJar || checkJarValidate(jarInput)) {
            Logger.info("开始遍历 jar：" + jarInput.file.absolutePath)
            modifiedJar = modifyJarFile(jarInput.file, context.getTemporaryDir())
            Logger.info("结束遍历 jar：" + jarInput.file.absolutePath)
        }
        if (modifiedJar == null) {
            modifiedJar = jarInput.file
        }
        FileUtils.copyFile(modifiedJar, dest)
    }

    private boolean checkJarValidate(JarInput jarInput) {
        try {
            if (isFoundSDKJar || sensorsSdkJarPath == null) {
                return false
            }
            def jarLocation = jarInput.file.toURI().getPath()
            if (sensorsSdkJarPath.length() == jarLocation.length() && sensorsSdkJarPath == jarLocation) {
                isFoundSDKJar = true
                return true
            } else {
                return false
            }
        } catch (Throwable throwable) {
            Logger.error("Checking jar's validation error: " + throwable.localizedMessage)
            return false
        }
    }

    /**
     * 修改 jar 文件中对应字节码
     */
    private File modifyJarFile(File jarFile, File tempDir) {
        if (jarFile) {
            return modifyJar(jarFile, tempDir, true)

        }
        return null
    }

    private File modifyJar(File jarFile, File tempDir, boolean isNameHex) {
        //FIX: ZipException: zip file is empty
        if (jarFile == null || jarFile.length() == 0) {
            return null
        }
        //取原 jar, verify 参数传 false, 代表对 jar 包不进行签名校验
        def file = new JarFile(jarFile, false)
        //设置输出到的 jar
        def tmpNameHex = ""
        if (isNameHex) {
            tmpNameHex = DigestUtils.md5Hex(jarFile.absolutePath).substring(0, 8)
        }
        def outputJar = new File(tempDir, tmpNameHex + jarFile.name)
        JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(outputJar))
        Enumeration enumeration = file.entries()

        while (enumeration.hasMoreElements()) {
            JarEntry jarEntry = (JarEntry) enumeration.nextElement()
            InputStream inputStream
            try {
                inputStream = file.getInputStream(jarEntry)
            } catch (Exception e) {
                IOUtils.closeQuietly(inputStream)
                e.printStackTrace()
                return null
            }
            String entryName = jarEntry.getName()
            if (entryName.endsWith(".DSA") || entryName.endsWith(".SF")) {
                //ignore
            } else {
                String className
                JarEntry entry = new JarEntry(entryName)
                byte[] modifiedClassBytes = null
                byte[] sourceClassBytes
                try {
                    jarOutputStream.putNextEntry(entry)
                    sourceClassBytes = SensorsAnalyticsUtil.toByteArrayAndAutoCloseStream(inputStream)
                } catch (Exception e) {
                    Logger.error("Exception encountered while processing jar: " + jarFile.getAbsolutePath())
                    IOUtils.closeQuietly(file)
                    IOUtils.closeQuietly(jarOutputStream)
                    e.printStackTrace()
                    return null
                }
                if (!jarEntry.isDirectory() && entryName.endsWith(".class")) {
                    className = entryName.replace("/", ".").replace(".class", "")
                    ClassNameAnalytics classNameAnalytics = transformHelper.analytics(className)
                    if (classNameAnalytics.isShouldModify) {
                        modifiedClassBytes = modifyClass(sourceClassBytes, classNameAnalytics)
                    }
                }
                if (modifiedClassBytes == null) {
                    jarOutputStream.write(sourceClassBytes)
                } else {
                    jarOutputStream.write(modifiedClassBytes)
                }
                jarOutputStream.closeEntry()
            }
        }
        jarOutputStream.close()
        file.close()
        return outputJar
    }

    /**
     * 真正修改类中方法字节码
     */
    private byte[] modifyClass(byte[] srcClass, ClassNameAnalytics classNameAnalytics) {
        try {
            ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS)
            ClassVisitor classVisitor = new SensorsAnalyticsClassVisitor(classWriter, classNameAnalytics, transformHelper, sdkVersionHelper)
            ClassReader cr = new ClassReader(srcClass)
            cr.accept(classVisitor, ClassReader.EXPAND_FRAMES)
            return classWriter.toByteArray()
        } catch (Exception ex) {
            Logger.error("$classNameAnalytics.className 类执行 modifyClass 方法出现异常")
            ex.printStackTrace()
            if (transformHelper.extension.debug) {
                throw new Error()
            }
            return srcClass
        }
    }

    /**
     * 目录文件中修改对应字节码
     */
    private File modifyClassFile(File dir, File classFile, File tempDir) {
        File modified = null
        FileOutputStream outputStream = null
        try {
            String className = path2ClassName(classFile.absolutePath.replace(dir.absolutePath + File.separator, ""))
            ClassNameAnalytics classNameAnalytics = transformHelper.analytics(className)
            if (classNameAnalytics.isShouldModify) {
                byte[] sourceClassBytes = SensorsAnalyticsUtil.toByteArrayAndAutoCloseStream(new FileInputStream(classFile))
                byte[] modifiedClassBytes = modifyClass(sourceClassBytes, classNameAnalytics)
                if (modifiedClassBytes) {
                    modified = new File(tempDir, UUID.randomUUID().toString() + '.class')
                    if (modified.exists()) {
                        modified.delete()
                    }
                    modified.createNewFile()
                    outputStream = new FileOutputStream(modified)
                    outputStream.write(modifiedClassBytes)
                }
            } else {
                return classFile
            }
        } catch (Exception e) {
            e.printStackTrace()
        } finally {
            IOUtils.closeQuietly(outputStream)
        }
        return modified
    }

    private static String path2ClassName(String pathName) {
        pathName.replace(File.separator, ".").replace(".class", "")
    }
}