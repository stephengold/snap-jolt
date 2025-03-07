// Gradle script to build the snap-jolt project

import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform

plugins {
    application // to build JVM applications
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType<JavaCompile>().all { // Java compile-time options:
    options.compilerArgs.add("-Xdiags:verbose")
    options.compilerArgs.add("-Xlint:unchecked")
    options.encoding = "UTF-8"
    options.setDeprecation(true) // to provide detailed deprecation warnings
}

// Register tasks to run specific applications:

tasks.register<JavaExec>("HelloWorld") {
    description = "Runs the HelloWorld app."
    mainClass = "com.github.stephengold.snapjolt.HelloWorld"
}
tasks.register<JavaExec>("PrintConfig") {
    description = "Runs the PrintConfig app."
    mainClass = "com.github.stephengold.snapjolt.PrintConfig"
}

val os = DefaultNativePlatform.getCurrentOperatingSystem()
tasks.withType<JavaExec>().all { // Java runtime options:
    classpath = sourceSets.main.get().getRuntimeClasspath()
    enableAssertions = true
    if (os.isMacOsX()) {
        jvmArgs("-XstartOnFirstThread") // required for GLFW on macOS
    }
}

application {
    mainClass = "com.github.stephengold.snapjolt.HelloWorld"
}

configurations.all {
    resolutionStrategy.cacheChangingModulesFor(0, "seconds") // to disable caching of snapshots
}

val btf = "DebugSp"
dependencies {
    implementation(libs.jolt.jni.linux64)
    runtimeOnly(variantOf(libs.jolt.jni.linux64){ classifier(btf) })
    runtimeOnly(variantOf(libs.jolt.jni.linux64fma){ classifier(btf) })
    runtimeOnly(variantOf(libs.jolt.jni.linuxarm32hf){ classifier(btf) })
    runtimeOnly(variantOf(libs.jolt.jni.linuxarm64){ classifier(btf) })
    runtimeOnly(variantOf(libs.jolt.jni.macosx64){ classifier(btf) })
    runtimeOnly(variantOf(libs.jolt.jni.macosxarm64){ classifier(btf) })
    runtimeOnly(variantOf(libs.jolt.jni.windows64){ classifier(btf) })
    runtimeOnly(variantOf(libs.jolt.jni.windows64avx2){ classifier(btf) })

    implementation(libs.jsnaploader)
    implementation(libs.log4j.impl)
    implementation(libs.oshi.core)
}

// Register cleanup tasks:

tasks.named("clean") {
    dependsOn("cleanDLLs", "cleanDyLibs", "cleanLogs", "cleanSOs")
}
tasks.register<Delete>("cleanDLLs") { // extracted Windows native libraries
    delete(fileTree(".").matching{ include("*.dll") })
}
tasks.register<Delete>("cleanDyLibs") { // extracted macOS native libraries
    delete(fileTree(".").matching{ include("*.dylib") })
}
tasks.register<Delete>("cleanLogs") { // JVM crash logs
    delete(fileTree(".").matching{ include("hs_err_pid*.log") })
}
tasks.register<Delete>("cleanSOs") { // extracted Linux and Android native libraries
    delete(fileTree(".").matching{ include("*.so") })
}
