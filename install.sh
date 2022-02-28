#!/usr/bin/env bash

# script for building and running bazel bsp server locally
# it takes a single parameter with the path to project which you want to import with bsp
# (by default it "installs" the bsp server in the current directory)

project_path=${1:-$PWD}

echo -e "Building server..."
echo -e "============================"
bazel build //server/src/main/java/org/jetbrains/bsp/bazel:bsp-install
bsp_path="$(bazel info bazel-bin)/server/src/main/java/org/jetbrains/bsp/bazel/bsp-install"
echo -e "============================"
echo -e "Building done."

echo -e "\nInstalling server in '$project_path' ..."
cd "$project_path" || { echo "cd $project_path failed! EXITING"; exit 155; }

rm -rf .bsp/
rm -rf .bazelbsp/

$bsp_path
exit_code=$?

if [ $exit_code -eq 0 ]; then
    echo -e "Done! Enjoy Bazel BSP!"
else
    echo -e "Installation failed!"
    exit $exit_code
fi

