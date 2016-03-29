# image with sqlplus(from base image), ant, java and gradle installed
FROM wnameless/oracle-xe-11g

RUN apt-get -y update && apt-get install -y \
  ant \
  ant-contrib \
  default-jdk \
  unzip

# install gradle
ENV gradle_version=2.1
RUN wget -Nnv http://services.gradle.org/distributions/gradle-${gradle_version}-all.zip \
 && unzip gradle-${gradle_version}-all.zip -d /opt/gradle \
 && ln -sfn gradle-${gradle_version} /opt/gradle/latest

ENV GRADLE_HOME=/opt/gradle/latest 
ENV ORACLE_HOME=/u01/app/oracle/product/11.2.0/xe
ENV PATH=$PATH:$GRADLE_HOME/bin:$ORACLE_HOME/bin

