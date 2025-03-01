/*
Copyright (c) 2025 Stephen Gold

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package com.github.stephengold.snapjolt;

import com.github.stephengold.joltjni.Jolt;
import electrostatic4j.snaploader.LibraryInfo;
import electrostatic4j.snaploader.LoadingCriterion;
import electrostatic4j.snaploader.NativeBinaryLoader;
import electrostatic4j.snaploader.filesystem.DirectoryPath;
import electrostatic4j.snaploader.platform.NativeDynamicLibrary;
//import electrostatic4j.snaploader.platform.util.NativeVariant;
import electrostatic4j.snaploader.platform.util.PlatformPredicate;

/**
 * @author Stephen Gold sgold@sonic.net
 */
public class PrintConfig {

    public static void main(String[] argv) {
//        assert NativeVariant.Cpu.hasExtensions("avx");
//        assert NativeVariant.Cpu.hasExtensions("avx2");
//        assert NativeVariant.Cpu.hasExtensions("bmi1");
//        assert NativeVariant.Cpu.hasExtensions("f16c");
//        assert NativeVariant.Cpu.hasExtensions("fma");
//        assert NativeVariant.Cpu.hasExtensions("sse4_1");
//        assert NativeVariant.Cpu.hasExtensions("sse4_2");

//        assert NativeVariant.Cpu.hasExtensions(
//                "avx", "avx2", "bmi1", "f16c", "fma", "sse4_1", "sse4_2");

        PlatformPredicate linuxWithFma = new PlatformPredicate(
                PlatformPredicate.LINUX_X86_64.evaluatePredicate()
//                && NativeVariant.Cpu.hasExtensions(
//                        "avx", "avx2", "bmi1", "f16c", "fma", "sse4_1", "sse4_2")
        );

        LibraryInfo info = new LibraryInfo(
                new DirectoryPath("linux/x86-64/com/github/stephengold"),
                "joltjni", DirectoryPath.USER_DIR);
        NativeBinaryLoader loader = new NativeBinaryLoader(info);
        NativeDynamicLibrary[] libraries = {
            new NativeDynamicLibrary("linux/aarch64/com/github/stephengold", PlatformPredicate.LINUX_ARM_64),
            new NativeDynamicLibrary("linux/armhf/com/github/stephengold", PlatformPredicate.LINUX_ARM_32),
            new NativeDynamicLibrary("linux/x86-64-fma/com/github/stephengold", linuxWithFma),
            new NativeDynamicLibrary("linux/x86-64/com/github/stephengold", PlatformPredicate.LINUX_X86_64),
            new NativeDynamicLibrary("osx/aarch64/com/github/stephengold", PlatformPredicate.MACOS_ARM_64),
            new NativeDynamicLibrary("osx/x86-64/com/github/stephengold", PlatformPredicate.MACOS_X86_64),
            new NativeDynamicLibrary("windows/x86-64/com/github/stephengold", PlatformPredicate.WIN_X86_64)
        };
        loader.registerNativeLibraries(libraries).initPlatformLibrary();
        loader.setLoggingEnabled(true);
        loader.setRetryWithCleanExtraction(true);
        try {
            loader.loadLibrary(LoadingCriterion.INCREMENTAL_LOADING);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load the joltjni library!");
        }
        System.err.flush();

        String configuration = Jolt.getConfigurationString();
        System.out.println(configuration);
    }
}
