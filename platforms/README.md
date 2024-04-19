# RBE Platforms:

In order to update to a new image you need to change the `Dockerfile` in this directory and once it's been changed to the desired image you can run `build-and-push-image.sh` to push it to an OCI registry.

The platforms for RBE are generated via `rbe_configs_gen` in [bazel-toolchains](https://github.com/bazelbuild/bazel-toolchains?tab=readme-ov-file#rbe_configs_gen---cli-tool-to-generate-configs) by running the following command (you will need to replace the image passed to `--toolchain_container`):

```
rbe_configs_gen --output_config_path platforms/linux_x86 --exec_os=linux --target_os=linux --output_src_root=$(pwd) --toolchain_container=docker.io/antonioengflow/bazel-bsp-rbe@sha256:9c8e458c436d59aab58ca86e220587498537f77ddcf20192aebcf4372f69bc2d
```