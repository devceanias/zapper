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
package revxrsal.zapper.classloader;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A wrapper for {@link URLClassLoader} that allows adding URLs to it.
 */
public abstract class URLClassLoaderWrapper {
    private static final Map<Class<?>, Method> ADD_URL_CACHE = new ConcurrentHashMap<>();

    public abstract void addURL(@NotNull URL var1);

    /**
     * Returns a {@link URLClassLoaderWrapper} for the given class loader
     */
    public static @NotNull URLClassLoaderWrapper wrapLoader(@NotNull final URLClassLoader loader) {
        try {
            final Method addUrl = ADD_URL_CACHE.computeIfAbsent(loader.getClass(), clazz -> {
                try {
                    final Method method = clazz.getDeclaredMethod("addURL", URL.class);

                    method.setAccessible(true);

                    return method;
                } catch (final Exception exception) {
                    throw new RuntimeException(exception);
                }
            });

            return new URLClassLoaderWrapper() {
                @Override
                public void addURL(@NotNull final URL url) {
                    try {
                        addUrl.invoke(loader, url);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            };
        } catch (final Throwable throwable) {
            throw new IllegalStateException("Error adding URL to classloader.", throwable);
        }
    }
}