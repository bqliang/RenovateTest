package com.example.asm.timing

import com.android.build.api.instrumentation.AsmClassVisitorFactory
import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import com.android.build.api.instrumentation.InstrumentationParameters
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.objectweb.asm.ClassVisitor

abstract class TimingClassVisitorFactory :
    AsmClassVisitorFactory<TimingClassVisitorFactory.Parameters> {

  interface Parameters : InstrumentationParameters {
    @get:Input val logTag: Property<String>
  }

  override fun createClassVisitor(
      classContext: ClassContext,
      nextClassVisitor: ClassVisitor,
  ): ClassVisitor {
    val tag: String = parameters.get().logTag.get()

    return TimingClassVisitor(
        apiVersion = instrumentationContext.apiVersion.get(),
        nextClassVisitor = nextClassVisitor,
        className = classContext.currentClassData.className,
        logTag = tag,
    )
  }

  override fun isInstrumentable(classData: ClassData): Boolean {
    return classData.className.startsWith("com.example.aop")
  }
}
