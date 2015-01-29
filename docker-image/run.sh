#!/bin/bash
if [ ! -f /.root_pw_set ]; then
        /set_root_pw.sh
fi
/usr/sbin/mosquitto -d &
/usr/bin/mongod --fork --syslog &
if [ -f /data/run-user.sh ]; then
        /data/run-user.sh
fi
exec /usr/sbin/sshd -D
