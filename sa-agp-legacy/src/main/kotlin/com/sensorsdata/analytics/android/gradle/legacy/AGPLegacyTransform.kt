@file:Suppress("DEPRECATION")

package com.sensorsdata.analytics.android.gradle.legacy

import com.android.build.api.transform.*
import com.android.build.api.transform.TransformException
import com.android.build.gradle.AppExtension
import com.android.build.gradle.internal.pipeline.TransformManager
import com.sensorsdata.analytics.android.gradle.AsmCompatFactory
import com.sensorsdata.analytics.android.gradle.ClassInfo
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.*
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream

@Suppress("DEPRECATION")
class AGPLegacyTransform(
    private val asmWrapperFactory: AsmCompatFactory,
    private val android: AppExtension
) : Transform() {

    private lateinit var inheritance: AGPClassInheritance

    override fun getName(): String {
        return asmWrapperFactory.name
    }

    override fun getInputTypes(): MutableSet<QualifiedContent.ContentType> {
        return TransformManager.CONTENT_CLASS
    }

    override fun getScopes(): MutableSet<in QualifiedContent.Scope> {
        return TransformManager.SCOPE_FULL_PROJECT //TODO 还需要判断当前是不是 Library
    }

    override fun isIncremental(): Boolean {
        return asmWrapperFactory.isIncremental
    }

    @Throws(TransformException::class, InterruptedException::class, IOException::class)
    override fun transform(transformInvocation: TransformInvocation) {
        traverseForClassLoader(transformInvocation)
        asmWrapperFactory.onBeforeTransform()
        if (!transformInvocation.isIncremental) {
            transformInvocation.outputProvider.deleteAll()
        }
        val inputCollection = transformInvocation.inputs
        inputCollection.parallelStream().forEach { transformInput: TransformInput ->
            val jarInputCollection = transformInput.jarInputs
            jarInputCollection.parallelStream()
                .forEach { jarInput: JarInput ->
                    processJarFile(jarInput, transformInvocation)
                }
            val directoryInputCollection = transformInput.directoryInputs
            directoryInputCollection.parallelStream()
                .forEach { directoryInput: DirectoryInput ->
                    processDirectoryFile(directoryInput, transformInvocation)
                }
        }
    }

    private fun traverseForClassLoader(transformInvocation: TransformInvocation) {
        inheritance = AGPClassInheritance()
        AGPLegacyContextImpl.asmCompatFactory = asmWrapperFactory
        inheritance.bootClassPath.addAll(android.bootClasspath)

        val urlList = mutableListOf<File>()
        transformInvocation.inputs.forEach { transformInput ->
            transformInput.jarInputs.forEach { jarInput ->
                urlList.add(jarInput.file)
            }
            transformInput.directoryInputs.forEach { directoryInput ->
                urlList.add(directoryInput.file)
            }
        }
        inheritance.bootClassPath.addAll(urlList)
    }

    /**
     * 处理 Jar 包，我们在这个方法中会获取 Jar 包中的文件，但不对其中的文件做特殊处理，而是直接返回。
     *
     * @param jarInput JarInput
     * @param transformInvocation TransformInvocation
     */
    private fun processJarFile(jarInput: JarInput, transformInvocation: TransformInvocation) {
        val file: File = jarInput.file
        val outputFile: File = transformInvocation.outputProvider.getContentLocation(
            file.absolutePath,
            jarInput.contentTypes, jarInput.scopes, Format.JAR
        )
        try {
            if (transformInvocation.isIncremental) {
                when (jarInput.status) {
                    Status.REMOVED -> FileUtils.forceDelete(outputFile)
                    Status.CHANGED, Status.ADDED -> {
                        FileUtils.copyFile(
                            modifyJar(file, transformInvocation.context.temporaryDir) ?: file,
                            outputFile
                        )
                    }

                    Status.NOTCHANGED -> {}
                }
            } else {
                FileUtils.copyFile(
                    modifyJar(file, transformInvocation.context.temporaryDir) ?: file,
                    outputFile
                )
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * 对 Jar 包中的内容进行处理，通常是处理 class 文件
     *
     * @param file jar 包对应的 File
     * @param tempDir 输出临时文件用的文件夹
     * @return 返回修改过的 Jar 包，如果返回值是 null，表示未成功修改，在这种情况下直接 copy 原有文件即可
     */
    private fun modifyJar(file: File?, tempDir: File): File? {
        if (file == null || file.length() == 0L) {
            return null
        }
        try {
            val jarFile = JarFile(file, false)
            val tmpNameHex: String = DigestUtils.md5Hex(file.absolutePath).substring(0, 8)
            val outputJarFile = File(tempDir, tmpNameHex + file.name)
            val jarOutputStream = JarOutputStream(FileOutputStream(outputJarFile))

            val enumeration: Enumeration<JarEntry> = jarFile.entries()
            while (enumeration.hasMoreElements()) {
                val jarEntry: JarEntry = enumeration.nextElement()
                val entryName: String = jarEntry.name

                if (entryName.endsWith(".DSA") || entryName.endsWith(".SF")) {
                    //do nothing
                } else {
                    val outputEntry = JarEntry(entryName)
                    try {
                        jarOutputStream.putNextEntry(outputEntry)
                        jarFile.getInputStream(jarEntry).use { inputStream ->
                            val sourceBytes = toByteArrayAndAutoCloseStream(inputStream)
                            var outputBytes: ByteArray? = null
                            if (!jarEntry.isDirectory && entryName.endsWith(".class")) {
                                outputBytes = handleBytes(sourceBytes)
                            }
                            jarOutputStream.write(outputBytes ?: sourceBytes)
                            jarOutputStream.closeEntry()
                        }
                    } catch (e: Exception) {
                        System.err.println("Exception encountered while processing jar: " + file.absolutePath)
                        IOUtils.closeQuietly(jarFile)
                        IOUtils.closeQuietly(jarOutputStream)
                        e.printStackTrace()
                        return null
                    }
                }
            }
            IOUtils.closeQuietly(jarFile)
            IOUtils.closeQuietly(jarOutputStream)
            return outputJarFile
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * 用户可以再这里实现具体的处理原始数据的逻辑，例如使用 ASM、Javassit 等工具修改 class 文件，然后返回处理后的结果。
     * 此方法直接返回了输入的值。
     *
     * @param data 原始数据
     * @return 修改后的数据
     */
    private fun handleBytes(data: ByteArray): ByteArray? {
        return try {
            val classWriter = ClassWriter(ClassWriter.COMPUTE_MAXS)
            val cr = ClassReader(data)
            val classInfo =
                ClassInfo(cr.className, cr.interfaces?.asList(), mutableListOf(cr.superName))
            if (asmWrapperFactory.isInstrumentable(classInfo)) {
                val classVisitor = BaseClassVisitor(asmWrapperFactory.asmAPI, classWriter)
                cr.accept(
                    asmWrapperFactory.transform(classVisitor, inheritance),
                    ClassReader.EXPAND_FRAMES
                )
                classWriter.toByteArray()
            } else {
                data
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            data
        }
    }

    /**
     * 对工程源码中生成的 class 文件进行处理
     *
     * @param directoryInput DirectoryInput
     * @param transformInvocation TransformInvocation
     */
    private fun processDirectoryFile(
        directoryInput: DirectoryInput,
        transformInvocation: TransformInvocation
    ) {
        val srcDir: File = directoryInput.file
        val outputDir: File = transformInvocation.outputProvider.getContentLocation(
            srcDir.absolutePath,
            directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY
        )
        try {
            FileUtils.forceMkdir(outputDir)
            if (transformInvocation.isIncremental) {
                val changedFileMap: MutableMap<File, Status>? = directoryInput.changedFiles
                changedFileMap?.forEach { (file: File, status: Status) ->
                    //获取变动的文件对应在 output directory 中的位置
                    val destFilePath: String =
                        outputDir.absolutePath + file.absolutePath
                            .replace(srcDir.absolutePath, "")
                    val destFile = File(destFilePath)
                    try {
                        when (status) {
                            Status.REMOVED -> FileUtils.forceDelete(destFile)
                            Status.ADDED, Status.CHANGED -> {
                                FileUtils.copyFile(file, destFile)
                                val sourceBytes: ByteArray = FileUtils.readFileToByteArray(destFile)
                                val modifiedBytes = handleBytes(sourceBytes)
                                if (modifiedBytes != null) {
                                    FileUtils.writeByteArrayToFile(destFile, modifiedBytes, false)
                                }
                            }

                            Status.NOTCHANGED -> {}
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            } else {
                FileUtils.copyDirectory(srcDir, outputDir)
                FileUtils.listFiles(outputDir, arrayOf("class"), true).parallelStream()
                    .forEach { clazzFile ->
                        try {
                            val sourceBytes: ByteArray = FileUtils.readFileToByteArray(clazzFile)
                            val modifiedBytes = handleBytes(sourceBytes)
                            if (modifiedBytes != null) {
                                FileUtils.writeByteArrayToFile(clazzFile, modifiedBytes, false)
                            }
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /** 将输入流转换成 byte 数组  */
    @Throws(Exception::class)
    fun toByteArrayAndAutoCloseStream(input: InputStream): ByteArray {
        return try {
            IOUtils.toByteArray(input)
        } finally {
            IOUtils.closeQuietly(input)
        }
    }
}