package com.illicitonion.bazelbsp;

import com.google.common.base.Splitter;

import java.util.List;

public class Uri implements Comparable<Uri> {
    private final String uri;

    private Uri(String uri) {
        this.uri = uri;
    }

    public static Uri fromFileUri(String uri) {
        String prefix = "file:///";
        if (!uri.startsWith(prefix)) {
            throw new IllegalArgumentException(String.format("%s didn't start with %s", uri, prefix));
        }
        return new Uri(uri);
    }
    public static Uri fromAbsolutePath(String path) {
        return new Uri("file://" + path);
    }

    public static Uri fromExecPath(String execPath, String execRoot) {
        String prefix = "exec-root://";
        if (!execPath.startsWith(prefix)) {
            throw new IllegalArgumentException(String.format("%s didn't start with %s", execPath, prefix));
        }
        return new Uri(String.format("file://%s/%s", execRoot, execPath.substring(prefix.length())));
    }

    public static Uri fromFileLabel(String fileLabel, String workspaceRoot) {
        if (!fileLabel.startsWith("//")) {
            throw new IllegalArgumentException(String.format("%s didn't start with %s", fileLabel, "//"));
        }
        String fileLabelWithoutPrefix = fileLabel.substring(2);
        List<String> parts = Splitter.on(':').splitToList(fileLabelWithoutPrefix);
        if (parts.size() != 2) {
            throw new IllegalArgumentException(String.format("Label %s didn't contain exactly one :", fileLabel));
        }
        return new Uri(String.format("file://%s/%s/%s", workspaceRoot, parts.get(0), parts.get(1)));
    }

    public static Uri packageDirFromLabel(String fileLabel, String workspaceRoot) {
        if (!fileLabel.startsWith("//")) {
            throw new IllegalArgumentException(String.format("%s didn't start with %s", fileLabel, "//"));
        }
        String fileLabelWithoutPrefix = fileLabel.substring(2);
        List<String> parts = Splitter.on(':').splitToList(fileLabelWithoutPrefix);
        if (parts.size() != 2) {
            throw new IllegalArgumentException(String.format("Label %s didn't contain exactly one :", fileLabel));
        }
        return new Uri(String.format("file://%s/%s", workspaceRoot, parts.get(0)));
    }

    public static Uri fromWorkspacePath(String path, String workspaceRoot) {
        return new Uri(String.format("file://%s/%s", workspaceRoot, path));
    }

    public static Uri fromExecOrWorkspacePath(String path, String execRoot, String workspaceRoot) {
        if (path.startsWith("exec-root://")) {
            return fromExecPath(path, execRoot);
        } else if (path.startsWith("workspace-root://")) {
            return fromWorkspacePath(path.substring("workspace-root://".length()), workspaceRoot);
        } else {
            return fromWorkspacePath(path, workspaceRoot);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (!(o instanceof Uri)) {
            return false;
        }
        return uri.equals(((Uri) o).uri);
    }

    @Override
    public int hashCode() {
        return uri.hashCode();
    }

    @Override
    public String toString() {
        return uri;
    }

    @Override
    public int compareTo(Uri other) {
        return this.uri.compareTo(other.uri);
    }
}
