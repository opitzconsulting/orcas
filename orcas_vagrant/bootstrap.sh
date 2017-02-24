#!/usr/bin/env bash

# ip-config for orcale
service iptables save
service iptables stop
chkconfig iptables off

sudo su
. .bash_profile

# somehow oraenv does not set LD_LIBRARY_PATH correctly (used for reverse-engineering jdbc-oci connection)
printf "export LD_LIBRARY_PATH=$ORACLE_HOME/lib" >> .bash_profile

# install java
yum -y install java-1.8.0-openjdk-devel 

# install ant
yum -y install ant
yum -y install ant-contrib

# used for reverse-engineering via xslt
yum -y install ant-trax

# used by integrationtest 
yum -y install ant-apache-regexp

# install gradle
gradle_version=2.1
wget -Nnv http://services.gradle.org/distributions/gradle-${gradle_version}-all.zip 
unzip gradle-${gradle_version}-all.zip -d /opt/gradle
ln -sfn gradle-${gradle_version} /opt/gradle/latest
printf "export GRADLE_HOME=/opt/gradle/latest\nexport PATH=\$PATH:\$GRADLE_HOME/bin" > /etc/profile.d/gradle.sh
. /etc/profile.d/gradle.sh

