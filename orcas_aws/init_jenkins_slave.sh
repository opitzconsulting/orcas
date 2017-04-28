#!/bin/sh

sudo yum update -y 

sudo yum install -y docker

sudo yum install java-1.8.0-openjdk

sudo service docker start

wget -Nnv https://github.com/opitzconsulting/orcas/archive/master.zip \
 && unzip master.zip -d ~/orcas 

sudo docker build -t orcas_runtime ~/orcas/orcas-master/orcas_docker/images/orcas_runtime
sudo docker build -t orcas_runtime_deps ~/orcas/orcas-master/orcas_docker/images/orcas_runtime_deps
sudo docker build -t orcas_jenkins_slave ~/orcas/orcas-master/orcas_docker/images/orcas_jenkins_slave

sudo docker run -d --name orcasdb wnameless/oracle-xe-11g

sudo docker run -d --link=orcasdb:orcasdb orcas_jenkins_slave



