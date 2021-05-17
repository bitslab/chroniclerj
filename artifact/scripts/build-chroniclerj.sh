#!/bin/bash

if [ -z "$CHRONICLER_SOURCED" ]; then
    echo "Environment not set, please run the comment: source env.sh";
    exit 1
fi

pushd $CHRONICLER_DIR
{
    mvn clean
    mvn package
}
popd
