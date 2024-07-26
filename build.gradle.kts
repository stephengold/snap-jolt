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
    options.setDeprecation(true) // to provide detailed deprecation warnings
    options.encoding = "UTF-8"
}

// Register tasks to run specific applications:

tasks.register<JavaExec>("HelloWorld") {
    description = "Runs the HelloWorld app."
    mainClass = "com.github.stephengold.snapjolt.HelloWorld"
}

val os = DefaultNativePlatform.getCurrentOperatingSystem()
tasks.withType<JavaExec>().all { // Java runtime options:
    if (os.isMacOsX()) {
        jvmArgs("-XstartOnFirstThread") // required for GLFW on macOS
    }
    classpath = sourceSets.main.get().getRuntimeClasspath()
    enableAssertions = true
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
    runtimeOnly(variantOf(libs.jolt.jni.macosx64){ classifier(btf) })
    runtimeOnly(variantOf(libs.jolt.jni.macosxarm64){ classifier(btf) })
    runtimeOnly(variantOf(libs.jolt.jni.windows64){ classifier(btf) })

    implementation(libs.jsnaploader)
}
