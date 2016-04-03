FROM orcas_runtime_deps

COPY distribution /opt/orcas/distribution

WORKDIR /tmp

RUN wget -Nnv http://repo.jenkins-ci.org/releases/org/jenkins-ci/plugins/swarm-client/2.0/swarm-client-2.0-jar-with-dependencies.jar

CMD ["java", "-jar", "swarm-client-2.0-jar-with-dependencies.jar"]

