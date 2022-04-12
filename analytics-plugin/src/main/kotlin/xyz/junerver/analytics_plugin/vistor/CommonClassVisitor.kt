package xyz.junerver.analytics_plugin.vistor


import org.objectweb.asm.*
import org.objectweb.asm.tree.ClassNode
import xyz.junerver.analytics_plugin.config.AnnotationReflect

/**
 * File TrackClassNode.kt
 * Date 2021/6/11
 * Author lucas
 * Introduction 扫描class的节点
 */
class CommonClassVisitor(val classWriter: ClassWriter, val classReader: ClassReader) :
    ClassNode(Opcodes.ASM7), Opcodes {

    private var clazzName: String = "undefined"
    //类的注解访问器
    private var clazzAnnotationVisitor: CommonAnnotationVisitor? = null

    init {
        cv = classWriter
    }

    override fun visit(
        version: Int,
        access: Int,
        name: String,
        signature: String?,
        superName: String?,
        interfaces: Array<out String>?
    ) {
        super.visit(version, access, name, signature, superName, interfaces)
        this.clazzName = name
    }

    //访问注解，传入描述符与可见性，返回注解访问器
    override fun visitAnnotation(descriptor: String?, visible: Boolean): AnnotationVisitor {
        val annotationVisitor = super.visitAnnotation(descriptor, visible)
        //判断注解的描述符是否为我们自定义的注解，如果是的话则返回我们自己定义的注解Visitor
        if (AnnotationReflect.trackAnnotation.contains(descriptor)) {
            clazzAnnotationVisitor = CommonAnnotationVisitor(annotationVisitor, descriptor!!)
            return clazzAnnotationVisitor!!
        }
        return annotationVisitor
    }


    //访问方法，返回方法访问器
    override fun visitMethod(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        var visitMethod = super.visitMethod(access, name, descriptor, signature, exceptions)
        visitMethod = TrackMethodAdviceAdapter(clazzName, visitMethod, access, name, descriptor,clazzAnnotationVisitor)
        return visitMethod
    }


}