FROM jenkins

USER root

RUN apt-get -y update && apt-get install -y \
  netcat

USER jenkins

RUN mkdir /tmp/jenkins_install

WORKDIR /tmp/jenkins_install

COPY plugins.txt .
COPY job_dsl_seed_job.xml jobs/job_dsl_seed_job/job_dsl_seed_job.xml

RUN /usr/local/bin/plugins.sh /tmp/jenkins_install/plugins.txt

RUN cp -R /tmp/jenkins_install/jobs /var/jenkins_home

WORKDIR /var/jenkins_home


