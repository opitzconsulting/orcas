FROM orcas_runtime

RUN wget -Nnv https://github.com/opitzconsulting/orcas/archive/master.zip \
 && unzip master.zip -d /opt/orcas 

RUN mkdir /tmp/orcas

WORKDIR /tmp/orcas

COPY build.xml .

ENV ORCAS_CORE=/opt/orcas/orcas-master/orcas_core
ENV ORCAS_PREBUILDFILE_NOEXTENSIONS=/opt/orcas/orcas-master/prebuildfile_noextensions.zip
ENV ORCAS_PREBUILDFILE_DOMAINEXTENSION=/opt/orcas/orcas-master/prebuildfile_domainextension.zip

RUN ant



