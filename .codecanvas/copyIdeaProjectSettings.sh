#!/bin/bash
set -e

# Source and Destination directories
src_dir=".codecanvas/res/idea/project"
dest_dir=".idea"

# Use cp command to copy all files from source to destination
cp "$src_dir"/* "$dest_dir"
