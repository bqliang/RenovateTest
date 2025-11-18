package com.example.asm.timing

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor

class TimingClassVisitor(
    private val apiVersion: Int,
    nextClassVisitor: ClassVisitor,
    private val className: String,
    private val logTag: String,
) : ClassVisitor(apiVersion, nextClassVisitor) {

  override fun visitMethod(
      access: Int,
      name: String?,
      descriptor: String?,
      signature: String?,
      exceptions: Array<out String>?,
  ): MethodVisitor {
    // 先获取原始的 MethodVisitor
    val originalMethodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions)
    // 返回我们自定义的 MethodVisitor 来处理方法
    return TimingMethodVisitor(
        apiVersion,
        originalMethodVisitor,
        access,
        name,
        descriptor,
        className,
        logTag,
    )
  }
}
