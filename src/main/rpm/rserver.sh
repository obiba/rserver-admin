#!/bin/bash

getPidFile() {
   while getopts ":p:" opt; do
     case $opt in
       p)
         echo $OPTARG
         return 0
         ;;
     esac
   done

   return 1
}

# OS specific support.
cygwin=false
case "`uname`" in
CYGWIN*) cygwin=true;;
esac

if [ -z "$JAVA_OPTS" ]
then
  if [ ! -z "$JAVA_ARGS" ]
  then
    JAVA_OPTS=$JAVA_ARGS
  else
    # Set default JAVA_OPTS
    JAVA_OPTS="-Xmx2G -XX:MaxPermSize=128M"
  fi

  export JAVA_OPTS
fi

# The directory containing the rserver shell script
RSERVER_BIN_DIR=`dirname $0`
# resolve links - $0 may be a softlink
RSERVER_DIST=$(readlink -f $RSERVER_BIN_DIR/..)

export RSERVER_DIST

echo "JAVA_OPTS=$JAVA_OPTS"
echo "RSERVER_HOME=$RSERVER_HOME"
echo "RSERVER_DIST=$RSERVER_DIST"
echo "RSERVER_LOG=$RSERVER_LOG"

if [ -z "$RSERVER_HOME" ]
then
  echo "RSERVER_HOME not set."
  exit 2;
fi

if $cygwin; then
  # For Cygwin, ensure paths are in UNIX format before anything is touched
  [ -n "$RSERVER_DIST" ] && RSERVER_BIN=`cygpath --unix "$RSERVER_DIST"`
  [ -n "$RSERVER_HOME" ] && RSERVER_HOME=`cygpath --unix "$RSERVER_HOME"`

  # For Cygwin, switch paths to Windows format before running java
  export RSERVER_DIST=`cygpath --absolute --windows "$RSERVER_DIST"`
  export RSERVER_HOME=`cygpath --absolute --windows "$RSERVER_HOME"`
fi

# Java 6 supports wildcard classpath entries
# http://download.oracle.com/javase/6/docs/technotes/tools/solaris/classpath.html
CLASSPATH=$RSERVER_HOME/conf:"$RSERVER_DIST/lib/*"
if $cygwin; then
  CLASSPATH=$RSERVER_HOME/conf;"$RSERVER_DIST/lib/*"
fi

[ -e "$RSERVER_HOME/logs" ] || mkdir "$RSERVER_HOME/logs"

JAVA_DEBUG=-agentlib:jdwp=transport=dt_socket,server=y,address=8000,suspend=n

# Add $JAVA_DEBUG to this line to enable remote JVM debugging (for developers)
exec java $JAVA_OPTS -cp "$CLASSPATH" -DRSERVER_HOME="${RSERVER_HOME}" \
  -DRSERVER_DIST=${RSERVER_DIST} -DRSERVER_LOG=${RSERVER_LOG}  org.obiba.rserver.Application "$@" >$RSERVER_LOG/stdout.log 2>&1 &

# On CentOS 'daemon' function does not initialize the pidfile
pidfile=$(getPidFile $@)

if [ ! -z "$pidfile" ]; then
  echo $! > $pidfile
fi