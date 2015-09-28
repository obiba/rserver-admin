#!/bin/sh

getent group adm >/dev/null || groupadd -r adm
getent passwd rserver >/dev/null || \
    useradd -r -g adm -d /home/rserver -s /sbin/nologin \
    -c "User for RServer Admin" rserver
exit 0
