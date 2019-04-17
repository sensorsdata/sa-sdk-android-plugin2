/**Created by wangzhuozhou on 2015/08/01.
 * Copyright © 2015－2019 Sensors Data Inc. All rights reserved. */

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
import com.android.build.api.transform.TransformOutputProvider
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.ide.common.internal.WaitableExecutor
import groovy.io.FileType
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter

import java.util.concurrent.Callable
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

class SensorsAnalyticsTransform extends Transform {
    private SensorsAnalyticsTransformHelper transformHelper
    public static final String VERSION = "3.0.4"
    public static final String MIN_SDK_VERSION = "3.0.0"
    private WaitableExecutor waitableExecutor

    SensorsAnalyticsTransform(SensorsAnalyticsTransformHelper transformHelper) {
        this.transformHelper = transformHelper
        if (!transformHelper.disableSensorsAnalyticsMultiThread) {
            waitableExecutor = WaitableExecutor.useGlobalSharedThreadPool()
        }
    }

    @Override
    String getName() {
        return "SensorsAnalyticsAutoTrack"
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return !transformHelper.disableSensorsAnalyticsIncremental
    }

    /**
     * 打印提示信息
     */
    private static void printCopyRight() {
        println()
        println("\033[40;32m" + "####################################################################" + "\033[0m")
        println("\033[40;32m" + "########                                                    ########" + "\033[0m")
        println("\033[40;32m" + "########                                                    ########" + "\033[0m")
        println("\033[40;32m" + "########     欢迎使用 SensorsAnalytics® (v" + VERSION + ")编译插件    ########" + "\033[0m")
        println("\033[40;32m" + "########          使用过程中碰到任何问题请联系我们          ########" + "\033[0m")
        println("\033[40;32m" + "########                                                    ########" + "\033[0m")
        println("\033[40;32m" + "########                                                    ########" + "\033[0m")
        println("\033[40;32m" + "####################################################################" + "\033[0m")
        println()
    }

    @Override
    void transform(Context context, Collection<TransformInput> inputs, Collection<TransformInput> referencedInputs, TransformOutputProvider outputProvider, boolean isIncremental) throws IOException, TransformException, InterruptedException {
        /**
         * 打印提示信息
         */
        printCopyRight()
        transformHelper.onTransform()
        println("[SensorsAnalytics]: 是否开启多线程编译:${!transformHelper.disableSensorsAnalyticsMultiThread}")
        println("[SensorsAnalytics]: 是否开启增量编译:${!transformHelper.disableSensorsAnalyticsIncremental}")
        println("[SensorsAnalytics]: 此次是否增量编译:$isIncremental")
        long startTime = System.currentTimeMillis()
        if (!isIncremental) {
            outputProvider.deleteAll()
        }
        /**
         * 遍历输入文件
         */
        inputs.each { TransformInput input ->
            /**
             * 遍历 jar
             */
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

            /**
             * 遍历目录
             */
            input.directoryInputs.each { DirectoryInput directoryInput ->
                //Logger.info("||-->开始遍历特定目录  ${dest.absolutePath}")
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
                            if (waitableExecutor) {
                                waitableExecutor.execute(new Callable<Object>() {
                                    @Override
                                    Object call() throws Exception {
                                        forEachDir(dir, inputFile, context, srcDirPath, destDirPath)
                                        return null
                                    }
                                })
                            } else {
                                forEachDir(dir,inputFile,context,srcDirPath,destDirPath)
                            }
                    }
                }

            }
        }
        if (waitableExecutor) {
            waitableExecutor.waitForTasksWithQuickFail(true)
        }

        println("[SensorsAnalytics]: 此次编译共耗时:${System.currentTimeMillis() - startTime}毫秒")
    }

    void forEachDir(File dir , File inputFile, Context context ,String srcDirPath, String destDirPath) {
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

    void forEachJar(boolean isIncremental,JarInput jarInput,TransformOutputProvider outputProvider,Context context){
        String destName = jarInput.file.name
        /**
         * 截取文件路径的md5值重命名输出文件,因为可能同名,会覆盖
         */
        def hexName = DigestUtils.md5Hex(jarInput.file.absolutePath).substring(0, 8)
        if (destName.endsWith(".jar")) {
            destName = destName.substring(0, destName.length() - 4)
        }
        /** 获得输出文件*/
        File destFile = outputProvider.getContentLocation(destName + "_" + hexName, jarInput.contentTypes, jarInput.scopes, Format.JAR)
        if (isIncremental) {
            Status status = jarInput.getStatus()
            switch (status) {
                case Status.NOTCHANGED:
                    break
                case Status.ADDED:
                case Status.CHANGED:
                    Logger.info("jar status = $status:$destFile.absolutePath")
                    transformJar(destFile,jarInput,context)
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
            transformJar(destFile,jarInput,context)
        }
    }

    void transformJar(File dest,JarInput jarInput,Context context) {
        def modifiedJar = null
        if (!transformHelper.extension.disableJar || jarInput.file.absolutePath.contains('SensorsAnalyticsSDK')) {
            Logger.info("开始遍历 jar：" + jarInput.file.absolutePath)
            modifiedJar = modifyJarFile(jarInput.file, context.getTemporaryDir())
            Logger.info("结束遍历 jar：" + jarInput.file.absolutePath)
        }
        if (modifiedJar == null) {
            modifiedJar = jarInput.file
        }
        FileUtils.copyFile(modifiedJar, dest)
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

    private File modifyJar(File jarFile, File tempDir, boolean nameHex) {
        /**
         * 读取原 jar
         */
        def file = new JarFile(jarFile)

        /**
         * 设置输出到的 jar
         */
        def hexName = ""
        if (nameHex) {
            hexName = DigestUtils.md5Hex(jarFile.absolutePath).substring(0, 8)
        }
        def outputJar = new File(tempDir, hexName + jarFile.name)
        JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(outputJar))
        Enumeration enumeration = file.entries()

        while (enumeration.hasMoreElements()) {
            JarEntry jarEntry = (JarEntry) enumeration.nextElement()
            InputStream inputStream = file.getInputStream(jarEntry)

            String entryName = jarEntry.getName()
            if (entryName.endsWith(".DSA") || entryName.endsWith(".SF")) {
                //ignore
            } else {
                String className

                ZipEntry zipEntry = new ZipEntry(entryName)

                jarOutputStream.putNextEntry(zipEntry)

                byte[] modifiedClassBytes = null
                byte[] sourceClassBytes = null
                try {
                    sourceClassBytes = IOUtils.toByteArray(inputStream)
                } catch (Exception e) {
                    return null
                }
                if (entryName.endsWith(".class")) {
                    className = entryName.replace("/", ".").replace(".class", "")
                    ClassNameAnalytics classNameAnalytics = transformHelper.analytics(className)
                    if (classNameAnalytics.isShouldModify) {
                        modifiedClassBytes = modifyClass(sourceClassBytes,classNameAnalytics)
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
            ClassVisitor classVisitor = new SensorsAnalyticsClassVisitor(classWriter, classNameAnalytics, transformHelper)
            ClassReader cr = new ClassReader(srcClass)
            cr.accept(classVisitor, ClassReader.EXPAND_FRAMES)
            return classWriter.toByteArray()
        } catch(Exception ex) {
            println("$classNameAnalytics.className 类执行 modifyClass 方法出现异常")
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
                byte[] sourceClassBytes = IOUtils.toByteArray(new FileInputStream(classFile))
                byte[] modifiedClassBytes = modifyClass(sourceClassBytes, classNameAnalytics)
                if (modifiedClassBytes) {
                    modified = new File(tempDir, className.replace('.', '') + '.class')
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
            try {
                if (outputStream != null) {
                    outputStream.close()
                }
            } catch (Exception e) {
                //ignore
            }
        }
        return modified
    }

    private static String path2ClassName(String pathName) {
        pathName.replace(File.separator, ".").replace(".class", "")
    }
}