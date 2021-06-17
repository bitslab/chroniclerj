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
        FILE=(avrora-cvs-20091224.jar)
        ;;
    "batik") #xml-apis.jar throws IllegalAccessError / Works for passing objects that implement serializable
        FILE=(batik-all.jar xml-apis-ext.jar crimson-1.1.3.jar xerces_2_5_0.jar xalan-2.6.0.jar)
        ;;
    "eclipse") #Works for passing objects that implement serializable
        FILE=(eclipse.jar)
        ;;
    "fop") #xml-apis-1.3.04.jar throws IllegalAccessError / Works for passing objects that implement serializable
        FILE=(fop.jar avalon-framework-4.2.0.jar batik-all-1.7.jar commons-io-1.3.1.jar commons-logging-1.0.4.jar serializer-2.7.0.jar servlet-2.2.jar xalan-2.7.0.jar xercesImpl-2.7.1.jar xml-apis-ext-1.3.04.jar xmlgraphics-commons-1.3.1.jar)
        ;;
    "h2") #Works for passing objects that implement serializable
        FILE=(dacapo-h2.jar derbyTesting.jar junit-3.8.1.jar h2-1.2.121.jar)
        ;;
    "jython") #Still busted
        FILE=(jython.jar antlr-3.1.3.jar asm-3.1.jar asm-commons-3.1.jar constantine.jar guava-r07.jar jaffl.jar jline-0.9.95-SNAPSHOT.jar jnr-posix.jar)
        ;;
    "luindex") #Works for passing objects that implement serializable
        FILE=(dacapo-luindex.jar lucene-core-2.4.jar lucene-demos-2.4.jar)
        ;;
    "lusearch-fix") #Works for passing objects that implement serializable
        FILE=(dacapo-lusearch-fix.jar lucene-core-2.4.jar lucene-demos-2.4.jar)
        ;;
    "lusearch") #Works for passing objects that implement serializable
        FILE=(dacapo-lusearch.jar lucene-core-2.4.jar lucene-demos-2.4.jar)
        ;;
    "pmd") #xml-apis.jar throws IllegalAccessError / Works for passing objects that implement serializable
        FILE=(pmd-4.2.5.jar jaxen-1.1.1.jar asm-3.1.jar junit-3.8.1.jar xercesImpl.jar)
        ;;
    "sunflow") #Working
        FILE=(sunflow-0.07.2.jar janino-2.5.15.jar)
        ;;
    "tomcat") #Still busted
        FILE=(dacapo-tomcat.jar dacapo-digest.jar bootstrap.jar tomcat-juli.jar commons-daemon.jar commons-httpclient.jar commons-logging.jar commons-codec.jar)
        ;;
    "tradebeans") #Works for passing objects that implement serializable but getting weird log4j errors
        FILE=(daytrader.jar)
        ;;
    "tradesoap") #Same as tradebeans
        FILE=(daytrader.jar)
        ;;
    "xalan") #xml-apis.jar throws IllegalAccessError / Works for passing objects that implement serializable
        FILE=(dacapo-xalan.jar xalan.jar xercesImpl.jar serializer.jar)
        ;;
esac

# The instrumenter needs this dir otherwise it crashes
mkdir tmp2

DEBUG="-Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=5555,suspend=n"
if [ -z "$FILE" ]; then

    # Instrument the whole DaCapo benchmark
    $JAVA_HOME/bin/java $DEBUG -cp $CHRONICLER_JAR:$DACAPO_INSTALL/jar/xml-apis-ext.jar edu.columbia.cs.psl.chroniclerj.Main -instrument $DACAPO_INSTALL $DACAPO_REC_INSTALL $DACAPO_REP_INSTALL

else

    echo "Instrumenting file $DACAPO_INSTALL/Harness"

    $JAVA_HOME/bin/java $DEBUG -cp $CHRONICLER_JAR edu.columbia.cs.psl.chroniclerj.Main -instrument $DACAPO_INSTALL/Harness.class rec rep
    cp rec/Harness.class $DACAPO_REC_INSTALL/Harness.class
    cp rep/Harness.class $DACAPO_REP_INSTALL/Harness.class

    # Instrument the jar with the target code
    for jfile in "${FILE[@]}";
    do
    echo "Instrumenting file $DACAPO_INSTALL/jar/$jfile"
    $JAVA_HOME/bin/java $DEBUG -cp $CHRONICLER_JAR:$DACAPO_INSTALL/jar/xml-apis-ext.jar edu.columbia.cs.psl.chroniclerj.Main -instrument $DACAPO_INSTALL/jar/$jfile rec rep
    cp rec/$jfile $DACAPO_REC_INSTALL/jar/$jfile
    cp rep/$jfile $DACAPO_REP_INSTALL/jar/$jfile
    done
    rm -rf rec rep

fi

rm -rf tmp2
