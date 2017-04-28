#!/usr/bin/bash

mvn clean install

#cp res/log4j.properties src/main/resources/log4j.properties
mkdir -p target/deploy target/cfg target/log

cp deploy/* target/deploy
cp cfg/* target/cfg
cp -rf webapps target/webapps
