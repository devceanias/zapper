package io.papermc.paper.pluginremap.reflect;

import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandles;
import java.nio.ByteBuffer;
import java.security.CodeSource;
import java.security.ProtectionDomain;

// todo proper inheritance handling
@SuppressWarnings("unused")
public final class PaperReflection {

    PaperReflection() {

    }

    private String mapClassName(final String name) {
        throw new RuntimeException("Stub!");
    }

    private String mapDeclaredMethodName(final Class<?> clazz, final String name, final Class<?> @Nullable ... parameterTypes) {
        throw new RuntimeException("Stub!");
    }

    private String mapMethodName(final Class<?> clazz, final String name, final Class<?> @Nullable ... parameterTypes) {
        throw new RuntimeException("Stub!");
    }

    private String mapDeclaredFieldName(final Class<?> clazz, final String name) {
        throw new RuntimeException("Stub!");
    }


    private String mapFieldName(final Class<?> clazz, final String name) {
        throw new RuntimeException("Stub!");
    }

    private @Nullable String findMappedMethodName(final Class<?> clazz, final String name, final Class<?> @Nullable ... parameterTypes) {
        throw new RuntimeException("Stub!");
    }

    private @Nullable String findMappedFieldName(final Class<?> clazz, final String name) {
        throw new RuntimeException("Stub!");
    }

    private static String strippedMethodKey(final String methodName, final Class<?> @Nullable ... parameterTypes) {
        return methodName + parameterDescriptor(parameterTypes);
    }

    private static String parameterDescriptor(final Class<?> @Nullable ... parameterTypes) {
        throw new RuntimeException("Stub!");
    }

    private static String removeCraftBukkitRelocation(final String name) {
        throw new RuntimeException("Stub!");
    }


    public Class<?> defineClass(final Object loader, final byte[] b, final int off, final int len) throws ClassFormatError {
        throw new RuntimeException("Stub!");
    }


    public Class<?> defineClass(final Object loader, final String name, final byte[] b, final int off, final int len) throws ClassFormatError {
        throw new RuntimeException("Stub!");
    }


    public Class<?> defineClass(final Object loader, final @Nullable String name, final byte[] b, final int off, final int len, final @Nullable ProtectionDomain protectionDomain) throws ClassFormatError {
        throw new RuntimeException("Stub!");
    }

    public Class<?> defineClass(final Object loader, final String name, final ByteBuffer b, final ProtectionDomain protectionDomain) throws ClassFormatError {
        throw new RuntimeException("Stub!");
    }

    public Class<?> defineClass(final Object secureLoader, final String name, final byte[] b, final int off, final int len, final CodeSource cs) {
        throw new RuntimeException("Stub!");
    }

    public Class<?> defineClass(final Object secureLoader, final String name, final ByteBuffer b, final CodeSource cs) {
        throw new RuntimeException("Stub!");
    }

    public Class<?> defineClass(final MethodHandles.Lookup lookup, final byte[] bytes) throws IllegalAccessException {
        throw new RuntimeException("Stub!");
    }

    private static byte[] processClass(final byte[] bytes) {
        throw new RuntimeException("Stub!");
    }
}
