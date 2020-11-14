package org.jetbrains.bsp.bazel.server;

public class BazelBspServerConfig {

    private String pathToBazel = null;
    private String projectPath = "//...";

    public BazelBspServerConfig(String pathToBazel) {
        this.pathToBazel = pathToBazel;
    }

    static public BazelBspServerConfig from(String[] args) {
        if(args.length == 0) { throw new IllegalArgumentException("Configuration can't be built without any parameters"); }
        BazelBspServerConfig config = new BazelBspServerConfig(args[0]);
        if(args.length == 2) {
            config.setProjectPath(args[1]);
        }

        return config;
    }

    public String getBazelPath() {
        return this.pathToBazel;
    }

    public String getProjectPath() {
        return this.projectPath;
    }

    public BazelBspServerConfig setProjectPath(String projectPath) {
        this.projectPath = projectPath;
        return this;
    }
}