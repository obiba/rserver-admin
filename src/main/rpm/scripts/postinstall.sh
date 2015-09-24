#!/bin/sh
# postinst script for rserver
#

set -e

# summary of how this script can be called:
#        * <postinst> `configure' <most-recently-configured-version>
#        * <old-postinst> `abort-upgrade' <new version>
#        * <conflictor's-postinst> `abort-remove' `in-favour' <package>
#          <new-version>
#        * <postinst> `abort-remove'
#        * <deconfigured's-postinst> `abort-deconfigure' `in-favour'
#          <failed-install-package> <version> `removing'
#          <conflicting-package> <version>
# for details, see http://www.debian.org/doc/debian-policy/ or
# the debian-policy package

NAME=rserver

[ -r /etc/default/$NAME ] && . /etc/default/$NAME

case "$1" in
  1)

    # Create rserver user if it doesn't exist.
    if ! id rserver > /dev/null 2>&1 ; then
      adduser --system --home /var/lib/rserver --no-create-home --disabled-password rserver
    fi

    # RServer file structure on Debian
    # /etc/rserver: configuration
    # /usr/share/rserver: executable
    # /var/lib/rserver: data runtime
    # /var/log: logs

    rm -f /usr/share/rserver
    ln -s /usr/share/rserver-* /usr/share/rserver

    if [ ! -e /var/lib/rserver/conf ] ; then
      ln -s /etc/rserver /var/lib/rserver/conf
    fi

    chown -R rserver:adm /var/lib/rserver /var/log/rserver /etc/rserver /tmp/rserver
    chmod -R 750      /var/lib/rserver /var/log/rserver /etc/rserver/ /tmp/rserver
    find /etc/rserver/ -type f | xargs chmod 640

    # if upgrading to 2.0, delete old log4j config
    if [ -f "/etc/rserver/log4j.properties" ]; then
      mv /etc/rserver/log4j.properties /etc/rserver/log4j.properties.old
    fi

    # Install RServe via R
    Rscript -e "install.packages('Rserve', repos=c('http://cran.rstudio.com'))"

    # auto start on reboot
    chkconfig --add rserver

    # start rserver
    echo "### You can start rserver service by executing:"
    echo "sudo /etc/init.d/rserver start"

  ;;

  *)
    echo "postinst called with unknown argument \`$1'" >&2
    exit 1
  ;;
esac

exit 0
