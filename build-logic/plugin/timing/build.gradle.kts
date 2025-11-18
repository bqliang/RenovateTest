import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins { `kotlin-dsl` }

java {
  sourceCompatibility = JavaVersion.VERSION_17
  targetCompatibility = JavaVersion.VERSION_17
}

kotlin { compilerOptions { jvmTarget = JvmTarget.JVM_17 } }

dependencies {
  // AGP API，用于访问 androidComponents 扩展
  implementation("com.android.tools.build:gradle-api:8.13.1")
  // ASM 核心库
  implementation("org.ow2.asm:asm:9.9")
  // ASM 工具库，提供了一些方便的适配器类
  implementation("org.ow2.asm:asm-commons:9.9")
}

gradlePlugin {
  plugins {
    register("TimingPlugin") {
      id = "timing-plugin"
      implementationClass = "com.example.asm.timing.TimingPlugin"
    }
  }
}
