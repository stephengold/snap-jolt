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
import electrostatic4j.snaploader.platform.util.NativeVariant;
import electrostatic4j.snaploader.platform.util.PlatformPredicate;

/**
 * Select between native libraries based which ISA extensions the current CPUs
 * support.
 *
 * @author Stephen Gold sgold@sonic.net
 */
final public class PrintConfig {

    public static void main(String[] argv) {
        // Test for each of the relevant CPU features:
        System.out.println("avx    = " + NativeVariant.Cpu.hasExtensions("avx"));
        System.out.println("avx2   = " + NativeVariant.Cpu.hasExtensions("avx2"));
        System.out.println("bmi1   = " + NativeVariant.Cpu.hasExtensions("bmi1"));
        System.out.println("f16c   = " + NativeVariant.Cpu.hasExtensions("f16c"));
        System.out.println("fma    = " + NativeVariant.Cpu.hasExtensions("fma"));
        System.out.println("sse4_1 = " + NativeVariant.Cpu.hasExtensions("sse4_1"));
        System.out.println("sse4_2 = " + NativeVariant.Cpu.hasExtensions("sse4_2"));

        // Define a custom predicate for Linux with all 7 CPU features:
        PlatformPredicate linuxWithFma = new PlatformPredicate(
                PlatformPredicate.LINUX_X86_64,
                "avx", "avx2", "bmi1", "f16c", "fma", "sse4_1", "sse4_2");
        System.out.println("linuxWithFma    = " + linuxWithFma.evaluatePredicate());

        // Define a custom predicate for Windows with 4 CPU features:
        PlatformPredicate windowsWithAvx2 = new PlatformPredicate(
                PlatformPredicate.WIN_X86_64,
                "avx", "avx2", "sse4_1", "sse4_2");
        System.out.println("windowsWithAvx2 = " + windowsWithAvx2.evaluatePredicate());
        System.out.flush();

        LibraryInfo info = new LibraryInfo(
                new DirectoryPath("linux/x86-64/com/github/stephengold"),
                "joltjni", DirectoryPath.USER_DIR);
        NativeBinaryLoader loader = new NativeBinaryLoader(info);
        NativeDynamicLibrary[] libraries = {
            new NativeDynamicLibrary("linux/aarch64/com/github/stephengold", PlatformPredicate.LINUX_ARM_64),
            new NativeDynamicLibrary("linux/armhf/com/github/stephengold", PlatformPredicate.LINUX_ARM_32),
            new NativeDynamicLibrary("linux/x86-64-fma/com/github/stephengold", linuxWithFma), // must precede vanilla LINUX_X86_64
            new NativeDynamicLibrary("linux/x86-64/com/github/stephengold", PlatformPredicate.LINUX_X86_64),
            new NativeDynamicLibrary("osx/aarch64/com/github/stephengold", PlatformPredicate.MACOS_ARM_64),
            new NativeDynamicLibrary("osx/x86-64/com/github/stephengold", PlatformPredicate.MACOS_X86_64),
            new NativeDynamicLibrary("windows/x86-64-avx2/com/github/stephengold", windowsWithAvx2), // must precede vanilla WIN_X86_64
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

        // Invoke native code to obtain the configuration of the native library:
        String configuration = Jolt.getConfigurationString();
        /*
         * Depending which native library was loaded, the configuration string
         * should be one of the following:
         *
         * On LINUX_X86_64 platforms, either
         *  Single precision x86 64-bit with instructions: SSE2 SSE4.1 SSE4.2 AVX AVX2 F16C LZCNT TZCNT FMADD (Debug Renderer) (16-bit ObjectLayer) (Assertions) (ObjectStream) (Debug) (C++ RTTI) (C++ Exceptions)
         * or
         *  Single precision x86 64-bit with instructions: SSE2 (Debug Renderer) (16-bit ObjectLayer) (Assertions) (ObjectStream) (Debug) (C++ RTTI) (C++ Exceptions)
         *
         * On WIN_X86_64 platforms, either
         *  Single precision x86 64-bit with instructions: SSE2 SSE4.1 SSE4.2 AVX AVX2 F16C LZCNT TZCNT (FP Exceptions) (Debug Renderer) (16-bit ObjectLayer) (Assertions) (ObjectStream) (Debug) (C++ RTTI) (C++ Exceptions)
         * or
         *  Single precision x86 64-bit with instructions: SSE2 (FP Exceptions) (Debug Renderer) (16-bit ObjectLayer) (Assertions) (ObjectStream) (Debug) (C++ RTTI) (C++ Exceptions)
         */
        System.out.println(configuration);
    }
}
