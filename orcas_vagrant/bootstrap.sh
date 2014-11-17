#!/usr/bin/env bash

service iptables save
service iptables stop
chkconfig iptables off

sudo su
. .bash_profile

yum -y install ant
yum -y install ant-contrib
yum -y install java-1.7.0-openjdk-devel
echo "export JAVA_HOME=/etc/alternatives/java_sdk" >> /home/vagrant/.bashrc

gradle_version=2.1
wget -Nnv http://services.gradle.org/distributions/gradle-${gradle_version}-all.zip 
unzip gradle-${gradle_version}-all.zip -d /opt/gradle
ln -sfn gradle-${gradle_version} /opt/gradle/latest
printf "export GRADLE_HOME=/opt/gradle/latest\nexport PATH=\$PATH:\$GRADLE_HOME/bin" > /etc/profile.d/gradle.sh
. /etc/profile.d/gradle.sh

