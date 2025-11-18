package com.example.asm.timing

import com.android.build.api.instrumentation.FramesComputationMode
import com.android.build.api.instrumentation.InstrumentationScope
import com.android.build.api.variant.AndroidComponentsExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Property

class TimingPlugin : Plugin<Project> {

  abstract class Extension {
    abstract val logTag: Property<String>

    init {
      logTag.convention("Timing") // 默认的 LogTag
    }
  }

  override fun apply(project: Project) {
    val extension: Extension =
        project.extensions.create("timing", Extension::class.java)

    val androidComponents: AndroidComponentsExtension<*, *, *> =
        project.extensions.getByType(AndroidComponentsExtension::class.java)

    androidComponents.onVariants { variant ->
      variant.instrumentation.transformClassesWith(
          TimingClassVisitorFactory::class.java,
          InstrumentationScope.PROJECT,
      ) { parameters: TimingClassVisitorFactory.Parameters ->
        parameters.logTag.set(extension.logTag)
      }
      variant.instrumentation.setAsmFramesComputationMode(
          FramesComputationMode.COMPUTE_FRAMES_FOR_INSTRUMENTED_METHODS
      )
    }
  }
}
