#!/usr/bin/env bash

docker build --build-arg JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64/ --tag antonioengflow/bazel-bsp-rbe:latest .
docker push antonioengflow/bazel-bsp-rbe:latest