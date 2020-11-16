package org.jetbrains.bsp.bazel.server;

public class BazelBspServerConfig {

    private String pathToBazel = null;
    private String[] targetProjectPaths = new String[] { "//..." };

    public BazelBspServerConfig(String pathToBazel) {
        this.pathToBazel = pathToBazel;
    }

    static public BazelBspServerConfig from(String[] args) {
        if(args.length == 0) { throw new IllegalArgumentException("Configuration can't be built without any parameters"); }
        BazelBspServerConfig config = new BazelBspServerConfig(args[0]);
        if(args.length == 2) {
            config.setTargetProjectPaths(args[1].split(","));
        }

        return config;
    }

    public String getBazelPath() {
        return this.pathToBazel;
    }

    public String[] getTargetProjectPaths() {
        return this.targetProjectPaths;
    }

    public BazelBspServerConfig setTargetProjectPaths(String[] projectPaths) {
        this.targetProjectPaths = projectPaths;
        return this;
    }
}
