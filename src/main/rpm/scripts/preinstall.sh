#!/bin/sh

getent group adm >/dev/null || groupadd -r adm

$(getent passwd rserver >/dev/null)

if [ $? != 0 ]; then
    useradd -r -g adm -d /var/lib/rserver -s /sbin/nologin \
        -c "User for RServer Admin" rserver
else
    usermod -d /var/lib/rserver rserver
fi

exit 0