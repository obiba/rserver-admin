#!/bin/sh
# prerm script for rserver
#
# see: dh_installdeb(1)

set -e

# summary of how this script can be called:
#        * <prerm> `remove'
#        * <old-prerm> `upgrade' <new-version>
#        * <new-prerm> `failed-upgrade' <old-version>
#        * <conflictor's-prerm> `remove' `in-favour' <package> <new-version>
#        * <deconfigured's-prerm> `deconfigure' `in-favour'
#          <package-being-installed> <version> `removing'
#          <conflicting-package> <version>
# for details, see http://www.debian.org/doc/debian-policy/ or
# the debian-policy package

NAME=opal

case "$1" in

  0)
    chkconfig --del rserver

    # Read configuration variable file if it is present
    [ -r /etc/default/$NAME ] && . /etc/default/$NAME

    if which service >/dev/null 2>&1; then
            service rserver stop
    elif which invoke-rc.d >/dev/null 2>&1; then
            invoke-rc.d rserver stop
    else
            /etc/init.d/rserver stop
    fi
  ;;

  *)
    echo "prerm called with unknown argument \`$1'" >&2
    exit 1
  ;;
esac


exit 0
