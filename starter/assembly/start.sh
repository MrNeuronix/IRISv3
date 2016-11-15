#!/bin/sh

DIR=`dirname $0`
cd $DIR
java -jar lib/${project.artifactId}-${project.version}.jar $*