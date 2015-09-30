#!/bin/sh
sbt compile dist
ln -sf `pwd`/scripts/as_launcher.sh target/universal/
cd target/universal/ && unzip argcv-scholar-1.0.0.zip && ln -sf argcv-scholar-1.0.0 argcv-scholar-current

