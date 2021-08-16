#!/usr/bin/env bash

# script for building and running bazel bsp server locally
# script takes parameter with path to different project (as default this project)

project_path=${1:-$PWD}

echo -e "Building server..."
echo -e "============================"
bazel build //server/src/main/java/org/jetbrains/bsp/bazel:bsp-install
bsp_path="$(bazel info bazel-bin)/server/src/main/java/org/jetbrains/bsp/bazel/bsp-install"
echo -e "============================"
echo -e "Building done."

echo -e "\nInstalling server at '$project_path' ..."
cd "$project_path" || { echo "cd $project_path failed! EXITING"; exit 155; }
$bsp_path

echo -e "\nDone! Enjoy Bazel BSP!"
