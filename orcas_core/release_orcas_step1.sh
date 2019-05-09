#!/bin/sh

new_orcas_version=5.6.0

cd ..

sed -zi -e "s/\(\(orcas.[^\n]*[\n][^\n]*\)\|\(project.version[^\n]*\)\)\([0-9]\+\.[0-9]\+\.[0-9]\+\)\(-SNAPSHOT\)\?/\1$new_orcas_version/g" orcas_core/build_source/build.gradle orcas_maven_plugin/pom.xml orcas_integrationstest/build.gradle orcas_core/upload_maven_central/build.gradle examples/*/build.gradle examples/*/pom.xml examples/orderentry/db/build.xml

cd orcas_core/build_source

./gradlew clean
./gradlew publishToMavenLocal -Pextensionname_internal=domainextension -Porcas_extension_folder=../../../orcas_domain_extension_java/extensions -Porcas_extension_extract_file=../../../orcas_domain_extension_java/xslt_extract/orcas_domain_extract.xsl
./gradlew publishToMavenLocal 

cd ../../orcas_maven_plugin

mvn clean install

cd ../orcas_core/upload_maven_central

./gradlew clean publish


