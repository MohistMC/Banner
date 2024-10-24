package com.mohistmc.banner.stackdeobf.util;

import com.google.common.base.Preconditions;
import java.net.URI;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

public record MavenArtifactInfo(String repoUrl, String groupId, String artifactId, String classifier) {

    public MavenArtifactInfo(String repoUrl, String groupId, String artifactId, @Nullable String classifier) {
        this.repoUrl = repoUrl.endsWith("/") ? repoUrl : repoUrl + "/";
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.classifier = classifier;
    }

    public static MavenArtifactInfo parse(String repoUrl, String info) {
        String[] split = StringUtils.split(info, ':');
        Preconditions.checkState(split.length == 2 || split.length == 3, "Artifact info is invalid: " + info);

        String groupId = split[0], artifactId = split[1];
        String classifier = split.length > 2 ? split[2] : null;

        return new MavenArtifactInfo(repoUrl, groupId, artifactId, classifier);
    }

    public URI buildMetaUri() {
        return URI.create(this.repoUrl + this.groupId.replace('.', '/') +
                "/" + this.artifactId + "/maven-metadata.xml");
    }

    public URI buildUri(String version, String extension) {
        String fileName = this.artifactId + "-" + version +
                (this.classifier != null ? "-" + this.classifier : "") + "." + extension;
        return URI.create(this.repoUrl + this.groupId.replace('.', '/') +
                "/" + this.artifactId + "/" + version + "/" + fileName);
    }

    @Override
    public @Nullable String classifier() {
        return this.classifier;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof MavenArtifactInfo that)) return false;
        if (!this.repoUrl.equals(that.repoUrl)) return false;
        if (!this.groupId.equals(that.groupId)) return false;
        if (!this.artifactId.equals(that.artifactId)) return false;
        return Objects.equals(this.classifier, that.classifier);
    }

    @Override
    public String toString() {
        return "MavenArtifactInfo{repoUrl='" + this.repoUrl + '\'' + ", groupId='" + this.groupId + '\'' + ", artifactId='" + this.artifactId + '\'' + ", classifier='" + this.classifier + '\'' + '}';
    }
}
