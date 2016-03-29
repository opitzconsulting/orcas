FROM orcas_runtime_deps

RUN adduser --disabled-password --gecos '' jenkins_slave \
 && echo jenkins_slave:jenkins_slave | chpasswd 

COPY distribution /opt/orcas/distribution

RUN chmod -R 777 /opt

CMD ["/usr/sbin/sshd", "-D"]

