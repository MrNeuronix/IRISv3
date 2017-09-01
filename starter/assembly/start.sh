#!/bin/sh

DIR=`dirname $0`
cd $DIR
java -jar lib/${project.artifactId}-${project.version}.jar $* 2>&1 > logs/console.log