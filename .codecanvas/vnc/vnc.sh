if ! command -v vncserver &> /dev/null
then
    echo "vncserver executable not found, vnc server is not started"
    exit 0
fi

if [ ! -f ~/.vnc/passwd ]; then
    vnc_pass="${JB_VNC_PASSWORD:-vncpassword}"
    /usr/bin/expect <<EOF
spawn /usr/bin/vncpasswd
expect "Password:"
send "$vnc_pass\r"
expect "Verify:"
send "$vnc_pass\r"
expect "Would you like to enter a view-only password (y/n)?"
send "n\r"
expect eof
exit
EOF
fi

vncserver :1 &
/mnt/jetbrains/system/noVNC/utils/novnc_proxy --vnc localhost:5901 --listen localhost:6081
