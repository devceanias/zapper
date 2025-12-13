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
package revxrsal.zapper.repository;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import revxrsal.zapper.Dependency;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Represents a Maven repository with a URL
 */
final class MavenRepository implements Repository {
    private static final MavenRepository MAVEN_CENTRAL = new MavenRepository("https://repo1.maven.org/maven2/");
    private static final MavenRepository JITPACK = new MavenRepository("https://jitpack.io/");
    private static final MavenRepository MINECRAFT = new MavenRepository("https://libraries.minecraft.net/");
    private static final MavenRepository PAPER = new MavenRepository("https://papermc.io/repo/repository/maven-public/");

    public static @NotNull MavenRepository mavenCentral() {
        return MAVEN_CENTRAL;
    }

    public static @NotNull MavenRepository jitpack() {
        return JITPACK;
    }

    public static @NotNull MavenRepository minecraft() {
        return MINECRAFT;
    }

    public static @NotNull MavenRepository paper() {
        return PAPER;
    }

    public static @NotNull MavenRepository maven(@NotNull final String url) {
        return new MavenRepository(url);
    }

    private final String repoURL;

    private MavenRepository(@NotNull String repoURL) {
        if (repoURL.charAt(repoURL.length() - 1) != '/')
            repoURL += '/';
        this.repoURL = repoURL;
    }

    public String getRepositoryURL() {
        return repoURL;
    }

    @Override
    public String toString() {
        return getRepositoryURL();
    }

    public @NotNull URL resolveJar(@NotNull final Dependency dependency) throws Exception {
        if (dependency.getVersion().endsWith("SNAPSHOT")) {
            return resolveSnapshotDependency(dependency, "jar");
        }

        return URI.create(repoURL + dependency.getMavenPath() + ".jar").toURL();
    }

    @Override
    public @NotNull URL resolvePom(@NotNull final Dependency dependency) throws Exception {
        if (dependency.getVersion().endsWith("SNAPSHOT")) {
            return resolveSnapshotDependency(dependency, "pom");
        }

        return URI.create(repoURL + dependency.getMavenPath() + ".pom").toURL();
    }

    @Override
    public @NotNull URL resolveChecksum(@NotNull final Dependency dependency) throws Exception {
        if (dependency.getVersion().endsWith("SNAPSHOT")) {
            return URI.create(resolveSnapshotDependency(dependency, "jar") + ".sha1").toURL();
        }

        return URI.create(repoURL + dependency.getMavenPath() + ".jar.sha1").toURL();
    }

    @Override
    public boolean equals(final Object object) {
        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        final MavenRepository that = (MavenRepository) object;

        return Objects.equals(repoURL, that.repoURL);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(repoURL);
    }

    /**
     * Resolves unique snapshot artifacts by reading maven-metadata.xml for the dependency.
     */
    private @NotNull URL resolveSnapshotDependency(
        @NotNull final Dependency dependency, @NotNull final String extension
    ) throws Exception {
        final SnapshotMetadata metadata = loadSnapshotMetadata(dependency);

        final String classifier = dependency.getClassifier();

        final String version = metadata
            .findSnapshotVersion(classifier, extension)
            .orElseGet(() -> metadata.getTimestampedVersion(dependency.getVersion()));

        if (version == null) {
            throw new IllegalStateException("Error finding snapshot version for dependency " + dependency + ".");
        }

        final String artifact = dependency.getArtifactId();

        final String finalClassifier = classifier == null
            ? ""
            : "-" + classifier;

        final String base = dependency
            .getGroupId()
            .replace('.', '/') + "/" + artifact + "/" + dependency.getVersion() + "/";

        final String fileName = artifact + "-" + version + finalClassifier + "." + extension;

        return URI.create(repoURL + base + fileName).toURL();
    }

    private @NotNull SnapshotMetadata loadSnapshotMetadata(@NotNull final Dependency dependency) throws Exception {
        final String base = dependency
            .getGroupId()
            .replace('.', '/') + "/" + dependency.getArtifactId() + "/" + dependency.getVersion() + "/";

        final URL url = URI.create(repoURL + base + "maven-metadata.xml").toURL();
        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setConnectTimeout((int) TimeUnit.SECONDS.toMillis(5));
        connection.setReadTimeout((int) TimeUnit.SECONDS.toMillis(10));
        connection.setInstanceFollowRedirects(true);

        final int code = connection.getResponseCode();

        if (code >= 400) {
            throw new IllegalStateException("Error fetching snapshot metadata (" + code + "): " + url + ".");
        }

        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setNamespaceAware(false);

        try (final InputStream stream = connection.getInputStream()) {
            final Document document = factory.newDocumentBuilder().parse(stream);

            document.getDocumentElement().normalize();

            return SnapshotMetadata.parse(document);
        }
    }

    private record SnapshotMetadata(String timestamp, String build, List<SnapshotVersion> versions) {
        @Contract("_ -> new")
        private static @NotNull SnapshotMetadata parse(final @NotNull Document document) {
            final NodeList snapshotNodes = document.getElementsByTagName("snapshot");

            String parsedTimestamp = null;
            String parsedBuild = null;

            final List<SnapshotVersion> parsedVersions = new ArrayList<>();
            final NodeList versionNodes = document.getElementsByTagName("snapshotVersion");

            if (snapshotNodes.getLength() > 0) {
                final NodeList children = snapshotNodes.item(0).getChildNodes();

                for (int index = 0; index < children.getLength(); index++) {
                    final Node child = children.item(index);

                    if (child.getNodeType() != Node.ELEMENT_NODE) {
                        continue;
                    }

                    final String name = child.getNodeName();
                    final String content = child.getTextContent();

                    if (name.equals("timestamp")) {
                        parsedTimestamp = content;
                    }

                    if (!name.equals("buildNumber")) {
                        continue;
                    }

                    parsedBuild = content;
                }
            }

            for (int versionIndex = 0; versionIndex < versionNodes.getLength(); versionIndex++) {
                final NodeList children = versionNodes.item(versionIndex).getChildNodes();

                String extension = null;
                String value = null;
                String classifier = null;
                int build = 0;

                for (int childrenIndex = 0; childrenIndex < children.getLength(); childrenIndex++) {
                    final Node child = children.item(childrenIndex);

                    if (child.getNodeType() != Node.ELEMENT_NODE) {
                        continue;
                    }

                    final String name = child.getNodeName();
                    final String content = child.getTextContent();

                    if (name.equals("classifier")) {
                        classifier = content;
                    }

                    if (name.equals("extension")) {
                        extension = content;
                    }

                    if (!name.equals("value")) {
                        continue;
                    }

                    value = content;
                }

                // If value is timestamped, parse the trailing build (e.g., ...-123).
                if (value != null) {
                    final int index = value.lastIndexOf('-');

                    if (index != -1 && index < value.length() - 1) {
                        try {
                            build = Integer.parseInt(value.substring(index + 1));
                        } catch (final NumberFormatException ignored) {}
                    }
                }

                if (extension != null && value != null) {
                    parsedVersions.add(new SnapshotVersion(classifier, extension, value, build));
                }
            }

            return new SnapshotMetadata(parsedTimestamp, parsedBuild, parsedVersions);
        }

        private Optional<String> findSnapshotVersion(final String classifier, final String extension) {
            // Pick the newest snapshot version (highest build/timestamp) if multiple exist.
            return versions.stream()
                .filter(version -> version.matches(classifier, extension))
                .max(Comparator.comparingInt(SnapshotVersion::build).thenComparing(SnapshotVersion::value))
                .map(version -> version.value);
        }

        @Contract(pure = true)
        private @Nullable String getTimestampedVersion(final String version) {
            if (timestamp == null || build == null) {
                return null;
            }

            return version.replace("SNAPSHOT", timestamp + "-" + build);
        }
    }

    private record SnapshotVersion(
        String classifier, String extension, String value, int build
    ) implements Comparable<SnapshotVersion> {
        private boolean matches(final String otherClassifier, final String otherExtension) {
            return Objects.equals(classifier, otherClassifier) && Objects.equals(extension, otherExtension);
        }

        @Override
        public int compareTo(@NotNull final SnapshotVersion version) {
            final int comparison = Integer.compare(build, version.build);

            if (comparison != 0) {
                return comparison;
            }

            return Comparator.nullsLast(String::compareTo).compare(value, version.value);
        }
    }
}