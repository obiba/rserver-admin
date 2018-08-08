#!/bin/sh

getent group adm >/dev/null || groupadd -r adm

$(getent passwd rserver >/dev/null)

if [ $? != 0 ]; then
    useradd -r -g nobody -d /var/lib/rserver -s /sbin/nologin \
        -c "User for RServer Admin" rserver
else

  # stop the service if running
  if service rserver status > /dev/null; then
    if which service >/dev/null 2>&1; then
      service rserver stop
    elif which invoke-rc.d >/dev/null 2>&1; then
      invoke-rc.d rserver stop
    else
      /etc/init.d/rserver stop
    fi
  fi

  # set the correct home directory
  usermod -d /var/lib/rserver rserver
fi

exit 0