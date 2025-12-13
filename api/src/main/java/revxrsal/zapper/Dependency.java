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
package revxrsal.zapper;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.zapper.repository.Repository;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.Objects;

/**
 * Represents a runtime dependency. Note that this does not include transitive
 * dependencies
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
public final class Dependency {

    private static final String MAVEN_PATH = "%s/%s/%s/%s-%s%s";

    @Getter
    private final String groupId;
    @Getter
    private final String artifactId;
    @Getter
    private final String version;
    private final String classifier;
    @Getter
    private final String mavenPath;

    public Dependency(@NotNull final String groupId, @NotNull final String artifactId, @NotNull final String version) {
        this(groupId, artifactId, version, null);
    }

    public Dependency(@NotNull final String groupId, @NotNull final String artifactId, @NotNull final String version, @Nullable final String classifier) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.classifier = StringUtils.isBlank(classifier) ? null : classifier;
        mavenPath = String.format(MAVEN_PATH,
                this.groupId.replace('.', '/'),
                this.artifactId,
                this.version,
                this.artifactId,
                this.version,
                this.classifier == null ? "" : '-' + this.classifier
        );
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;

        if (!(o instanceof final Dependency that)) {
            return false;
        }

        return Objects.equals(groupId, that.groupId) &&
                Objects.equals(artifactId, that.artifactId) &&
                Objects.equals(version, that.version) &&
                Objects.equals(classifier, that.classifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupId, artifactId, version, classifier);
    }

    @CheckReturnValue
    public @NotNull DependencyDownloadResult download(@NotNull final File file, @NotNull final Repository repository) {
        try {
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }

            final URL url = repository.resolveJar(this);

            String expected = null;

            try (final InputStream stream = repository.resolveChecksum(this).openStream()) {
                expected = new String(stream.readAllBytes(), StandardCharsets.UTF_8).trim();
            } catch (final Throwable ignored) {
                // If checksum is unavailable, proceed without verification.
            }

            final MessageDigest sha1 = expected == null ? null : MessageDigest.getInstance("SHA-1");

            try (final InputStream depIn = url.openStream()) {
                try (final OutputStream outStream = Files.newOutputStream(file.toPath())) {
                    final byte[] buffer = new byte[8 * 1024];
                    int bytesRead;
                    while ((bytesRead = depIn.read(buffer)) != -1) {
                        outStream.write(buffer, 0, bytesRead);

                        if (sha1 != null) {
                            sha1.update(buffer, 0, bytesRead);
                        }
                    }
                }
            }

            if (sha1 != null) {
                final String actual = convertToHex(sha1.digest());

                if (!expected.endsWith(actual)) { // Checksum files may include "SHA1 (file) = ...".
                    file.delete();

                    return DependencyDownloadResult.failure(new IllegalStateException(
                        "Error downloading dependency; checksum mismatch for " +
                        this +
                        ": expected " +
                        expected +
                        " but found " +
                        actual
                    ));
                }
            }

            return DependencyDownloadResult.success();
        } catch (final Throwable t) {
            file.delete();
            return DependencyDownloadResult.failure(t);
        }
    }

    public @Nullable String getClassifier() {
        return this.classifier;
    }

    private static @NotNull String convertToHex(final byte @NotNull [] bytes) {
        final StringBuilder builder = new StringBuilder(bytes.length * 2);

        for (final byte value : bytes) {
            builder.append(String.format("%02x", value));
        }

        return builder.toString();
    }

    @Override
    public String toString() {
        return "Dependency{" +
                "groupId='" + groupId + '\'' +
                ", artifactId='" + artifactId + '\'' +
                ", version='" + version + '\'' +
                ", mavenPath='" + mavenPath + '\'' +
                '}';
    }
}
