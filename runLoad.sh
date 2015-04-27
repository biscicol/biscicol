#!/bin/sh
#if test $# -eq 0
#then
#    echo " "
#    echo "Enter Properties File"
#    read LOADFILE 
#else
#     LOADFILE=$1
#fi
#
#MacOS Java
#JAVA=/System/Library/Frameworks/JavaVM.framework/Versions/1.6/Home/bin/java
#Redhat Java
JAVA=/usr/local/src/java/jre1.6.0_30/bin/java

#$JAVA -Xmx2048m -Xms1024m -Dfile.encoding=MacRoman -classpath out/production/Biscicol:lib/*:. Loading.Load $LOADFILE
$JAVA -Xmx2048m -Xms1024m -Dfile.encoding=MacRoman -classpath out/production/Biscicol:lib/*:. Loading.Load 
