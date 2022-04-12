package xyz.junerver.analytics_plugin.vistor

import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.commons.AdviceAdapter
import xyz.junerver.analytics_plugin.config.ActivityLifecycle
import xyz.junerver.analytics_plugin.config.AnnotationReflect
import xyz.junerver.analytics_plugin.ext.log

class TrackMethodAdviceAdapter(
    //类名
    private val clazzName: String,
    //类访问器传递过来的super返回的MethodVisitor，无法直接在方法中使用
    methodVisitor: MethodVisitor,
    access: Int,
    private val methodName: String?,
    //描述符：例如无参方法描述符为：()V
    descriptor: String?,
    //类的注解访问器，只会在存在我们的自定义注解时调用
    private val clazzAnnotationVisitor: CommonAnnotationVisitor? = null,
) : AdviceAdapter(
    Opcodes.ASM7,
    methodVisitor,
    access, methodName, descriptor
) {
    //方法的注解访问器
    private var methodAnnotationVisitor: CommonAnnotationVisitor? = null

    override fun visitAnnotation(descriptor: String?, visible: Boolean): AnnotationVisitor {
        val annotationVisitor = super.visitAnnotation(descriptor, visible)
        //判断注解的描述符是否为我们自定义的注解，如果是的话则返回我们自己定义的注解Visitor
        if (AnnotationReflect.trackAnnotation.contains(descriptor)) {
            methodAnnotationVisitor = CommonAnnotationVisitor(annotationVisitor, descriptor!!)
            return methodAnnotationVisitor!!
        }
        return annotationVisitor
    }

    override fun onMethodExit(opcode: Int) {
        super.onMethodExit(opcode)
        //有自定义的注解访问器说明是我们需要注入的方法
        methodAnnotationVisitor?.apply {
            "注入事件onMethodExit(opcode: Int)->methodName:$methodName,使用的注解为-> ${this.annoDescriptor}".log()
            "注解参数：${annoParam.map { "${it.key}:${it.value}" }.joinToString()}".log()
            "".log()
            //按照完全版的参数传参
            when (this.annoDescriptor) {
                AnnotationReflect.trackEvent -> {
                    val label1 = Label()
                    mv.visitLabel(label1)
                    mv.visitFieldInsn(
                        GETSTATIC,
                        "xyz/junerver/analytics_utils/Analytics",
                        "INSTANCE",
                        "Lxyz/junerver/analytics_utils/Analytics;"
                    )
                    //category: String, action: String, name: String, path: String
                    mv.visitLdcInsn(annoParam["category"] ?: "")
                    mv.visitLdcInsn(annoParam["action"] ?: "")
                    mv.visitLdcInsn(annoParam["name"] ?: "")
                    mv.visitLdcInsn(annoParam["path"] ?: "/")
                    mv.visitMethodInsn(
                        INVOKEVIRTUAL,
                        "xyz/junerver/analytics_utils/Analytics",
                        "trackEvent",
                        "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V",
                        false
                    )
                }
                AnnotationReflect.trackDimension -> {
                    val label1 = Label()
                    mv.visitLabel(label1)
                    mv.visitFieldInsn(
                        GETSTATIC,
                        "xyz/junerver/analytics_utils/Analytics",
                        "INSTANCE",
                        "Lxyz/junerver/analytics_utils/Analytics;"
                    )
                    //dimension: String, value: String, path: String
                    mv.visitLdcInsn(annoParam["dimension"] ?: "")
                    mv.visitLdcInsn(annoParam["value"] ?: "")
                    mv.visitLdcInsn(annoParam["path"] ?: "/")
                    mv.visitMethodInsn(
                        INVOKEVIRTUAL,
                        "xyz/junerver/analytics_utils/Analytics",
                        "trackDimension",
                        "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V",
                        false
                    )
                }
                else -> {
                    "未知注解".log()
                }
            }

        }
        //存在自定义的类注解访问器说明需要对类中注入trackScreenView函数
        clazzAnnotationVisitor?.apply {
            //可以在注解中指定注入的函数名称，然后在该名称函数的尾部进行注入，如果没有注明，则注入onResume
            val onTrack = annoParam["onTrack"] ?: ActivityLifecycle.onCreate.methodName
            if (methodName == onTrack) {
                "存在自定义的类注解：${this.annoDescriptor}\n类名称为：${clazzName}\n函数名称为：$methodName".log()
                "注解参数：${annoParam.map { "${it.key}:${it.value}" }.joinToString()}".log()
                "".log()
                val label1 = Label()
                mv.visitLabel(label1)
                mv.visitFieldInsn(
                    GETSTATIC,
                    "xyz/junerver/analytics_utils/Analytics",
                    "INSTANCE",
                    "Lxyz/junerver/analytics_utils/Analytics;"
                )
                //path: String, moduleName: String, pageName: String
                mv.visitLdcInsn(annoParam["path"] ?: "/")
                mv.visitLdcInsn(annoParam["moduleName"] ?: "undefined")
                mv.visitLdcInsn(annoParam["pageName"] ?: "undefined")
                mv.visitMethodInsn(
                    INVOKEVIRTUAL,
                    "xyz/junerver/analytics_utils/Analytics",
                    "trackScreenView",
                    "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V",
                    false
                )

            }
        }
    }

}