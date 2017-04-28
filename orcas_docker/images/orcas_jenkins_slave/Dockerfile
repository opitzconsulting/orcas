FROM orcas_runtime_deps

COPY distribution /opt/orcas/distribution

WORKDIR /tmp

RUN wget -Nnv http://repo.jenkins-ci.org/releases/org/jenkins-ci/plugins/swarm-client/2.0/swarm-client-2.0-jar-with-dependencies.jar

RUN chmod a+x /opt/orcas/orcas-master/orcas_integrationstest/gradlew

CMD ["java", "-jar", "swarm-client-2.0-jar-with-dependencies.jar", "-master", "http://10.0.1.229:80/"]

