FROM jenkins

USER root

RUN apt-get -y update && apt-get install -y \
  netcat

USER jenkins

RUN mkdir /tmp/jenkins_install

WORKDIR /tmp/jenkins_install

COPY job_dsl_seed_job.xml jobs/job_dsl_seed_job/config.xml
COPY jenkins_startup.groovy /usr/share/jenkins/ref/init.groovy.d/jenkins_startup.groovy

RUN echo job-dsl:1.44 > plugins.txt \
 && echo swarm:2.0 >> plugins.txt \
 && echo structs:1.3 >> plugins.txt \
 && echo junit:1.18 >> plugins.txt \
 && echo ant:1.3 >> plugins.txt \
 && echo gradle:1.26 >> plugins.txt \
 && /usr/local/bin/plugins.sh plugins.txt

RUN cp -R /tmp/jenkins_install/jobs /usr/share/jenkins/ref

WORKDIR /var/jenkins_home


