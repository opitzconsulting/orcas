#!/bin/sh

new_orcas_version=8.0.1

cd ..

sed -zi -e "s/\(\(orcas.[^\n]*[\n][^\n]*\)\|\(project.version[^\n]*\)\)\([0-9]\+\.[0-9]\+\.[0-9]\+\)\(-SNAPSHOT\)\?/\1$new_orcas_version/g" orcas_core/build_source/build.gradle orcas_maven_plugin/pom.xml orcas_integrationstest/build.gradle orcas_core/upload_maven_central/build.gradle examples/*/build.gradle examples/*/pom.xml

cd orcas_core/build_source

./gradlew clean
./gradlew publishToMavenLocal -Pextensionname_internal=domainextension -Porcas_extension_folder=../../../orcas_domain_extension_java/extensions -Porcas_extension_extract_file=../../../orcas_domain_extension_java/xslt_extract/orcas_domain_extract.xsl
./gradlew publishPlugins -Pextensionname_internal=domainextension -Porcas_extension_folder=../../../orcas_domain_extension_java/extensions -Porcas_extension_extract_file=../../../orcas_domain_extension_java/xslt_extract/orcas_domain_extract.xsl
./gradlew publishToMavenLocal 
./gradlew publishPlugins

#cd ../../orcas_maven_plugin

#./mvnw clean install

cd ../upload_maven_central

./gradlew clean publish


