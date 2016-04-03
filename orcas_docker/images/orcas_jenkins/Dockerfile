FROM jenkins

USER root

RUN apt-get -y update && apt-get install -y \
  netcat

USER jenkins

RUN mkdir /tmp/jenkins_install

WORKDIR /tmp/jenkins_install

COPY plugins.txt .
COPY job_dsl_seed_job.xml jobs/job_dsl_seed_job/config.xml
COPY jenkins_startup.groovy /usr/share/jenkins/ref/init.groovy.d/jenkins_startup.groovy

RUN /usr/local/bin/plugins.sh /tmp/jenkins_install/plugins.txt

RUN cp -R /tmp/jenkins_install/jobs /usr/share/jenkins/ref

WORKDIR /var/jenkins_home


