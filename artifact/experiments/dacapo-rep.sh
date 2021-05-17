#!/bin/bash

if [ -z "$CHRONICLER_SOURCED" ]; then
    echo "Environment not set, please run the comment: source env.sh";
    exit 1
fi

DEBUG="-Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=5555,suspend=n"
$JAVA_HOME/bin/java $DEBUG -cp $CHRONICLER_JAR:$DACAPO_REP_INSTALL edu.columbia.cs.psl.chroniclerj.Main -replay $1

