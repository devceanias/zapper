/*
 * This file is part of Zapper, licensed under the MIT License.
 *
 *  Copyright (c) Revxrsal <reflxction.github@gmail.com>
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */
package revxrsal.zapper.relocation;

import org.jetbrains.annotations.NotNull;
import revxrsal.zapper.Dependency;
import revxrsal.zapper.classloader.IsolatedClassLoader;
import revxrsal.zapper.repository.Repository;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The relocator utility
 */
public final class Relocator {

    private static boolean initialized = false;

    private Relocator() {
    }

    private static final List<Dependency> dependencies = Arrays.asList(
            new Dependency("org.ow2.asm", "asm", "9.7.1"),
            new Dependency("org.ow2.asm", "asm-commons", "9.7.1"),
            new Dependency("me.lucko", "jar-relocator", "1.7")
    );

    private static Constructor<?> relocatorConstructor;
    private static Method relocateMethod;

    public static void relocate(
            @NotNull File input,
            @NotNull File output,
            @NotNull List<Relocation> relocations
    ) {
        if (!initialized) {
            downloadJarRelocator(input.getParentFile());
            initialized = true;
        }
        try {
            Map<String, String> rules = new LinkedHashMap<>();
            for (Relocation relocation : relocations) {
                rules.put(relocation.getPattern(), relocation.getNewPattern());
            }
            Object relocator = relocatorConstructor.newInstance(input, output, rules);
            relocateMethod.invoke(relocator);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private static void downloadJarRelocator(File dir) {
        try {
            URL[] urls = new URL[3];
            dir.mkdirs();
            for (int i = 0; i < dependencies.size(); i++) {
                Dependency d = dependencies.get(i);
                File file = new File(dir, String.format("%s.%s-%s.jar", d.getGroupId(), d.getArtifactId(), d.getVersion()));
                if (!file.exists())
                    d.download(file, Repository.mavenCentral());
                urls[i] = file.toURI().toURL();
            }
            IsolatedClassLoader classLoader = new IsolatedClassLoader(urls);
            Class<?> jarRelocator = classLoader.loadClass("me.lucko.jarrelocator.JarRelocator");
            relocatorConstructor = jarRelocator.getDeclaredConstructor(File.class, File.class, Map.class);
            relocatorConstructor.setAccessible(true);
            relocateMethod = jarRelocator.getDeclaredMethod("run");
            relocateMethod.setAccessible(true);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }
}
