#!/bin/bash

source scripts/machine-specific.sh

setenv() {
    # wget command to download programs/data
    export WGET="$(which wget) -nc"

    if [ -z "$ROOT" ]; then
        echo "Please set project ROOT on file env.sh";
        echo "Or set it up on file scripts/machine-specific.sh";
        return 1
    fi

    # Where everything is downloaded
    export DOWNLOAD_DIR=$ROOT/downloads

    # Where everything is installed
    export INSTALL_DIR=$HOME/software

    #export JAVA_HOME=$INSTALL_DIR/jvm

    if [ -z "$JAVA_HOME" ]; then
        echo "Please export JAVA_HOME (or set it on file scripts/env.sh)";
        return 1
    fi

    export PATH=$JAVA_HOME/bin:$PATH

    export INSTALL_DIR=$ROOT/software

    export CHRONICLER_DIR=$ROOT/../Code/ChroniclerJ/
    export CHRONICLER_JAR=$CHRONICLER_DIR/target/ChroniclerJ-0.43-SNAPSHOT.jar

    export DACAPO_URL=https://sourceforge.net/projects/dacapobench/files/9.12-bach-MR1/dacapo-9.12-MR1-bach.jar
    export DACAPO_JAR=dacapo-9.12-MR1-bach.jar
    export DACAPO_INSTALL=$INSTALL_DIR/dacapo
    export DACAPO_REC_INSTALL=$INSTALL_DIR/dacapo-rec
    export DACAPO_REP_INSTALL=$INSTALL_DIR/dacapo-rep

    export CHRONICLER_SOURCED=1
}

setenv
