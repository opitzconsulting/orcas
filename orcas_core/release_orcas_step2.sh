#!/bin/sh

new_orcas_snapshot_version=7.8.7-SNAPSHOT

cd ..

sed -zi -e "s/\(\(orcas.[^\n]*[\n][^\n]*\)\|\(project.version[^\n]*\)\)\([0-9]\+\.[0-9]\+\.[0-9]\+\)\(-SNAPSHOT\)\?/\1$new_orcas_snapshot_version/g" orcas_core/build_source/build.gradle orcas_maven_plugin/pom.xml 

