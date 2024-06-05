#!/bin/bash
# Download the plugin
# TODO resolve latest version
PLUGIN_DIR="$CANVAS_IDE_HOME/../ide-plugins"

curl -L -o $PLUGIN_DIR/bsp-plugin.zip "https://plugins.jetbrains.com/plugin/download?rel=true&updateId=548944"
curl -L -o $PLUGIN_DIR/bazel-plugin.zip "https://plugins.jetbrains.com/plugin/download?rel=true&updateId=547765"

# Unzip the plugin to the plugins directory of the IDE
unzip $PLUGIN_DIR/bsp-plugin.zip -d $PLUGIN_DIR/
unzip $PLUGIN_DIR/bazel-plugin.zip -d $PLUGIN_DIR/

# Remove the .zip file
rm $PLUGIN_DIR/bsp-plugin.zip
rm $PLUGIN_DIR/bazel-plugin.zip
