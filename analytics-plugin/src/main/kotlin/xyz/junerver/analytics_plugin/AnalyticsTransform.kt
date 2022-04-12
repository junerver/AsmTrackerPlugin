package xyz.junerver.analytics_plugin

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import xyz.junerver.analytics_plugin.ext.log
import xyz.junerver.analytics_plugin.vistor.CommonClassVisitor
import java.io.File
import java.io.FileOutputStream
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry
import kotlin.system.measureTimeMillis

/**
 * @Author Junerver
 * @Date 2022/4/11-10:24
 * @Email junerver@gmail.com
 * @Version v1.0
 * @Description
 */
class AnalyticsTransform(val params:AnalyticsExtension) : Transform() {
    override fun getName(): String = "AnalyticsPlugin"

    override fun getInputTypes(): MutableSet<QualifiedContent.ContentType> =
        TransformManager.CONTENT_CLASS

    override fun getScopes(): MutableSet<in QualifiedContent.Scope> =
        TransformManager.SCOPE_FULL_PROJECT

    override fun isIncremental(): Boolean = true

    override fun transform(transformInvocation: TransformInvocation) {
        super.transform(transformInvocation)
        if (!params.enable) {
            //transform的执行是一个链式过程，
            //每个transform都需要正确配置TransformOutputProvider
            disablePlugin(transformInvocation)
            return
        }
        "Analytics 字节码插桩开始>>>>>>>>>>>>>>>>>".log()
        val time = measureTimeMillis {
            //获取输入的文件
            val inputs = transformInvocation.inputs
            //获取输出文件提供器
            val outputProvider = transformInvocation.outputProvider
            //是否是增量
            val incremental = transformInvocation.isIncremental
            //非增量模式下删除全部旧输出目录下的文件
            if (outputProvider != null && !incremental) {
                //删除之前输出的
                outputProvider.deleteAll()
            }
            inputs.forEach {
                it.directoryInputs.forEach { dirInput ->
                    //处理每个输入目录
                    handlerDirInput(dirInput, outputProvider, incremental)
                }
                it.jarInputs.forEach { jarInput ->
                    //处理每个输入jar包
                    if (!params.ignoreJar) {
                        handlerJarInput(jarInput, outputProvider, incremental)
                    } else {
                        transformInvocation.outputProvider.getContentLocation(
                            jarInput.name,
                            jarInput.contentTypes,
                            jarInput.scopes,
                            Format.JAR
                        ).also { dest ->
                            FileUtils.copyFile(jarInput.file, dest)
                        }
                    }
                }
            }
        }
        "Analytics 字节码插桩结束<<<<<<<<<<<<<<<<<<<< $time".log()
    }

    private fun handlerJarInput(
        jarInput: JarInput,
        outputProvider: TransformOutputProvider,
        incremental: Boolean
    ) {
        if (incremental) {//增量
            val dest = outputProvider.getContentLocation(
                jarInput.name,
                jarInput.contentTypes,
                jarInput.scopes,
                Format.JAR
            )
            //根据输入的状态进行判断
            when (jarInput.status) {
                //无变化什么都不做
                Status.NOTCHANGED -> {
                }
                //增加或者修改
                Status.ADDED, Status.CHANGED -> {
                    handlerJarInputInsert(jarInput, outputProvider)
                }
                //移除
                Status.REMOVED -> {
                    if (dest.exists())
                        FileUtils.forceDelete(dest)
                }
            }
        } else {
            handlerJarInputInsert(jarInput, outputProvider)
        }
    }

    //jar包插入代码
    private fun handlerJarInputInsert(jarInput: JarInput, outputProvider: TransformOutputProvider) {
        //筛除非jar文件
        if (!jarInput.file.absolutePath.endsWith(".jar")) return
        //重名名输出文件,因为可能同名,会覆盖，先获取旧名字
        var jarName = jarInput.name
        //获取源文件的MD5
        val md5Name = DigestUtils.md5Hex(jarInput.file.absolutePath)
        //去除名字的尾缀
        if (jarName.endsWith(".jar")) {
            jarName = jarName.substring(0, jarName.length - 4)
        }
        //通过file对象创建一个JarFile对象
        val jarFile = JarFile(jarInput.file)
        //返回JarFile中的所有JarEntry，这是一个Enumeration类似于一个迭代器，
        // 有两个成员函数 一个是hasMoreElements() 另一个是nextElement()
        val enumeration = jarFile.entries()
        //创建一个temp.jar文件用于暂存当前的jar文件
        val tempFile = File(jarInput.file.parent + File.separator + "class_temp.jar")
        //双重保障，确保临时文件不存在
        if (tempFile.exists()) {
            tempFile.delete()
        }
        //创建Jar输出流，jar输出流接受一个文件输出流作为参数
        val jarOutputStream = JarOutputStream(FileOutputStream(tempFile))
        //变量全部jar包下的JarEntry
        while (enumeration.hasMoreElements()) {
            //获取JarEntry
            val jarEntry = enumeration.nextElement() as JarEntry
            //获取JarEntry的名字
            val entryName = jarEntry.name
            //根据jarEntry的名字创建一个ZipEntry
            val zipEntry = ZipEntry(entryName)
            //通过jarFile.getInputStream函数获取对应当前的ZipEntry的输入流
            val inputStream = jarFile.getInputStream(zipEntry)
            //判断是否需要过滤，有些已知类是无需遍历的，可以直接忽略
            if (filterFile(entryName)) {
                //给输出流put当前的zipEntry
                jarOutputStream.putNextEntry(zipEntry)

                //region ASM框架读取类的实例
                //创建reader对象与writer对象
                val classReader = ClassReader(IOUtils.toByteArray(inputStream))
                val classWriter = ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)

                //创建ClassNode实例对象，这个在旧版本中称之为ClassVisitor
                val trackPageClassNode = CommonClassVisitor(classWriter, classReader)
                //为reader设置ClassNode
                classReader.accept(trackPageClassNode, ClassReader.EXPAND_FRAMES)
                //为ClassNode设置writer
                trackPageClassNode.accept(classWriter)
//                val trackMethodClassVisitor = TrackMethodClassVisitor(classReader, classWriter)
//                classReader.accept(trackMethodClassVisitor, ClassReader.EXPAND_FRAMES)
                //endregion

                //将classWriter的内容写入到jarOutputStream中
                jarOutputStream.write(classWriter.toByteArray())
            } else {
                jarOutputStream.putNextEntry(zipEntry)
                jarOutputStream.write(IOUtils.toByteArray(inputStream))
            }
            //每一个ZipEntry使用后需要关闭
            jarOutputStream.closeEntry()
        }

        //complete全部遍历关闭后关闭jarOutputStream与jarFile
        jarOutputStream.close()
        jarFile.close()
        //通过输出文件提供器获取输出文件路径，但是jar改名为原名称+md5
        val dest = outputProvider.getContentLocation(
            jarName + md5Name,
            jarInput.contentTypes,
            jarInput.scopes,
            Format.JAR
        )
        FileUtils.copyFile(tempFile, dest)
        tempFile.delete()
    }

    /**
     * @Description 处理文件夹输入
     * @param dirInput
     * @param outputProvider
     * @param incremental
     * @return
     */
    private fun handlerDirInput(
        dirInput: DirectoryInput,
        outputProvider: TransformOutputProvider,
        incremental: Boolean
    ) {
        if (incremental) {
            //增量插入
            //调用getContentLocation方法,获取输出文件的路径用File包装，该路径下没有任何文件
            val dest = outputProvider.getContentLocation(
                dirInput.name,
                dirInput.contentTypes,
                dirInput.scopes,
                Format.DIRECTORY
            )
            //创建该路径对应的文件夹
            FileUtils.forceMkdir(dest)
            //源文件路径
            val srcDirPath = dirInput.file.absolutePath
            //目标文件路径
            val destDirPath = dest.absolutePath
            //调用DirectoryInput的getChangedFiles()方法获取改变的文件，进行遍历
            dirInput.changedFiles.forEach { changeFile ->
                val destFile = File(changeFile.key.absolutePath.replace(srcDirPath, destDirPath))
                when (changeFile.value) {
                    Status.NOTCHANGED -> {
                    }
                    Status.REMOVED -> {
                        if (destFile.exists())
                            destFile.delete()
                    }
                    Status.ADDED, Status.CHANGED -> {
                        try {
                            FileUtils.touch(destFile)
                        } catch (e: Exception) {
                        }
                        if (filterFile(changeFile.key.name)) {
                            "dir file->${changeFile.key.absolutePath}".log()
                            val classReader = ClassReader(changeFile.key.readBytes())
                            val classWriter = ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)

                            val trackPageClassNode = CommonClassVisitor(classWriter, classReader)
                            classReader.accept(trackPageClassNode, ClassReader.EXPAND_FRAMES)
                            trackPageClassNode.accept(classWriter)

//                            val trackMethodClassVisitor = TrackMethodClassVisitor(classReader, classWriter)
//                            classReader.accept(trackMethodClassVisitor, ClassReader.EXPAND_FRAMES)

                            FileOutputStream(destFile).apply {
                                write(classWriter.toByteArray())
                                close()
                            }

                        } else {

                            if (changeFile.key.isFile) {
                                //无需改动，复制文件
                                FileUtils.touch(destFile)
                                FileUtils.copyFile(changeFile.key, destFile)
                            }
                        }
                    }
                }
            }
        } else {//全量插入
            handlerFullDirInput(dirInput, outputProvider)
        }
    }

    //全量插入
    private fun handlerFullDirInput(
        dirInput: DirectoryInput,
        outputProvider: TransformOutputProvider
    ) {
        if (dirInput.file.isDirectory) {
            var index = 0
            com.android.utils.FileUtils.getAllFiles(dirInput.file).forEach { file ->
                if (filterFile(file.name)) {
                    val classReader = ClassReader(file.readBytes())
                    val classWriter = ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)

                    val trackPageClassNode = CommonClassVisitor(classWriter, classReader)
                    classReader.accept(trackPageClassNode, ClassReader.EXPAND_FRAMES)
                    trackPageClassNode.accept(classWriter)

//                    val trackMethodClassVisitor = TrackMethodClassVisitor(classReader, classWriter)
//                    classReader.accept(trackMethodClassVisitor, ClassReader.EXPAND_FRAMES)

                    FileOutputStream(file.parentFile.absolutePath + File.separator + file.name).apply {
                        write(classWriter.toByteArray())
                        close()
                    }
                }
            }
        }
        //处理完输入文件之后，要把输出给下一个任务
        val dest = outputProvider.getContentLocation(
            dirInput.name,
            dirInput.contentTypes,
            dirInput.scopes,
            Format.DIRECTORY
        )
        FileUtils.copyDirectory(dirInput.file, dest)
    }

    /**
     * 关闭插件，文件原封不动输出
     * Transform 是中间产物链中的一环，它的输出，会影响到下一个 Transform 的输入，
     *  而 transform 方法是空函数，如果我们没有正确设置 TrannsformOutputProvider，
     *  那么，下一个环节『Task :app:transformClassesWithDexBuilderForDebug』
     *  无法将 classes 转成 dex 文件，也就最终导致无法生成 apk 文件。
     */
    private fun disablePlugin(transformInvocation: TransformInvocation) {
        //遍历全部的inputs
        transformInvocation.inputs.forEach {
            //每个transformInput都有两个不容的输入
            // 一个是：DirectoryInput，一个是JarInput
            //DirectoryInput是一个目录，JarInput是一个jar包
            //他们都实现了QualifiedContent接口
            it.directoryInputs.forEach { dirInput ->
                //输入文件如何输出： 其实，文件的去向，
                // transformInvocation 对象已经告诉我们了，
                // 我们通过 getOutputProvider.getContentLocation ，
                // 传入正确的 contentType， scope 和 format ，
                // 就能够获取正确的目标目录绝对路径：
                transformInvocation.outputProvider.getContentLocation(
                    dirInput.name,
                    dirInput.contentTypes,
                    dirInput.scopes,
                    Format.DIRECTORY
                ).also { dest ->
                    //调用
                    FileUtils.copyDirectory(dirInput.file, dest)
                }
            }
            it.jarInputs.forEach { jarInput ->
                transformInvocation.outputProvider.getContentLocation(
                    jarInput.name,
                    jarInput.contentTypes,
                    jarInput.scopes,
                    Format.JAR
                ).also { dest ->
                    FileUtils.copyFile(jarInput.file, dest)
                }
            }
        }
    }

    /**
     * @Description 过滤文件有的文件不需要处理
     * @param fileName
     * @return
     */
    private fun filterFile(fileName: String) =
        fileName.endsWith(".class") &&
                !fileName.startsWith("R\$") &&
                !fileName.startsWith("android/support") &&
                !fileName.startsWith("androidx/") &&
                !fileName.startsWith("com/google") &&
                fileName != "R.class" &&
                fileName != "R2.class" &&
                fileName != "BuildConfig.class"
}