#!/bin/bash
set -e

echo "On activation hook"

mkdir -p tmp

echo "Starting VNC server & noVNC proxy"
.codecanvas/vnc/vnc.sh > /tmp/on-activation-vnc.log 2>&1 &