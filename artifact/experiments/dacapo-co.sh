#!/bin/bash

if [ -z "$CHRONICLER_SOURCED" ]; then
    echo "Environment not set, please run the comment: source env.sh";
    exit 1
fi


DEBUG="-Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=5557,suspend=n"
$JAVA_HOME/bin/java $DEBUG -jar $CHRONICLER_JAR -coordinator
