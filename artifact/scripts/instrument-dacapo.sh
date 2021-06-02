#!/bin/bash

if [ -z "$CHRONICLER_SOURCED" ]; then
    echo "Environment not set, please run the comment: source env.sh";
    exit 1
fi

mkdir -p $DACAPO_INSTALL
pushd $DACAPO_INSTALL
{
    cp $DOWNLOAD_DIR/$DACAPO_JAR .
    jar xf $DACAPO_JAR
}
popd

cp -r $DACAPO_INSTALL $DACAPO_REC_INSTALL
cp -r $DACAPO_INSTALL $DACAPO_REP_INSTALL

FILE=""
case $1 in
    "avrora")   #Working
        FILE=avrora-cvs-20091224.jar
        ;;
    "batik")
        FILE=batik-all.jar
        ;;
    "eclipse")
        FILE=eclipse.jar
        ;;
    "fop")
        FILE=fop.jar
        ;;
    "h2")
        FILE=dacapo-h2.jar
        ;;
    "jython")
        FILE=jython.jar
        ;;
    "luindex")
        FILE=dacapo-luindex.jar
        ;;
    "lusearch-fix")
        FILE=dacapo-lusearch-fix.jar
        ;;
    "lusearch")
        FILE=dacapo-lusearch.jar
        ;;
    "pmd")
        FILE=pmd-4.2.5.jar
        ;;
    "sunflow")
        FILE=sunflow-0.07.2.jar
        ;;
    "tomcat")
        FILE=dacapo-tomcat.jar
        ;;
    "tradebeans")
        FILE=daytrader.jar
        ;;
    "tradesoap")
        FILE=daytrader.jar
        ;;
    "xalan")
        FILE=dacapo-xalan.jar
        ;;
esac

# The instrumenter needs this dir otherwise it crashes
mkdir tmp2

DEBUG="-Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=5555,suspend=n"
if [ -z "$FILE" ]; then

    # Instrument the whole DaCapo benchmark
    $JAVA_HOME/bin/java $DEBUG -cp $CHRONICLER_JAR:$DACAPO_INSTALL/jar/xml-apis-ext.jar edu.columbia.cs.psl.chroniclerj.Main -instrument $DACAPO_INSTALL $DACAPO_REC_INSTALL $DACAPO_REP_INSTALL

else

    echo "Instrumenting file $DACAPO_INSTALL/jar/$FILE"

    $JAVA_HOME/bin/java $DEBUG -cp $CHRONICLER_JAR edu.columbia.cs.psl.chroniclerj.Main -instrument $DACAPO_INSTALL/Harness.class rec rep
    cp rec/Harness.class $DACAPO_REC_INSTALL/Harness.class
    cp rep/Harness.class $DACAPO_REP_INSTALL/Harness.class

    # Instrument the jar with the target code
    $JAVA_HOME/bin/java $DEBUG -cp $CHRONICLER_JAR:$DACAPO_INSTALL/jar/xml-apis-ext.jar edu.columbia.cs.psl.chroniclerj.Main -instrument $DACAPO_INSTALL/jar/$FILE rec rep
    cp rec/$FILE $DACAPO_REC_INSTALL/jar/$FILE
    cp rep/$FILE $DACAPO_REP_INSTALL/jar/$FILE
    rm -rf rec rep

fi

rm -rf tmp2
