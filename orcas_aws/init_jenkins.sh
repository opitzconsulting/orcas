#!/bin/sh

sudo yum update -y 

sudo yum install -y docker

sudo service docker start

wget -Nnv https://github.com/opitzconsulting/orcas/archive/master.zip \
 && unzip master.zip -d ~/orcas 

sudo docker build -t orcas_jenkins ~/orcas/orcas-master/orcas_docker/images/orcas_jenkins

sudo docker run -d -p 50000:50000 -p 80:8080 --env JAVA_OPTS=-Dhudson.slaves.WorkspaceList=- orcas_jenkins


