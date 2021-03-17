#!/usr/bin/env bash

# script for building and running bazel bsp server locally
# script takes parameter with path to different project (as default this project)

project_path=${1:-$PWD}

echo -e "Building server..."
echo -e "============================"
bazel run --define "maven_repo=file://$HOME/.m2/repository" //:bsp.publish
bsp_path="$(bazel info bazel-bin)/bsp-project.jar"

echo -e "\n\nRunning server at $project_path"
echo -e "============================"
cd $project_path || { echo "cd $project_path failed! EXITING"; exit 155; }
java -cp "$bsp_path" org.jetbrains.bsp.bazel.install.Install

echo -e "\n\nDone!"
