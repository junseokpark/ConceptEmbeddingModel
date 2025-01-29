FROM openjdk:8
MAINTAINER Junseok Park <junseokpark@biosoft.kaist.ac.kr>

USER root
ENV METAMAPDIR=/opt/metamap PATH=/opt/metamap/bin:$PATH:/root/bin
RUN df -h
WORKDIR /opt

RUN apt-get update \
    && apt-get autoremove -y \
    && apt-get clean

ADD metamap.2016.tar /opt
RUN ls -alF
#RUN ls ./metamap.2016
#RUN tar -xvf ./metamap.2016.tar
#RUN ln -s ./metamap.2016 metamap
#RUN rm -rf ./metamap.2016.tar
RUN ln -s /opt/metamap.2016 /opt/metamap
ENV BASEDIR=/opt/metamap

WORKDIR /opt/metamap
RUN printf "\n\n" | ./bin/install.sh

#RUN cd $METAMAPDIR \
#    && echo -e "\n\n" | ./bin/install.sh

WORKDIR /
COPY entrypoint.sh /usr/local/bin/
RUN ln -s /usr/local/bin/entrypoint.sh /entrypoint.sh
RUN chmod +x entrypoint.sh
RUN chmod +x /usr/local/bin/entrypoint.sh

ENTRYPOINT ["entrypoint.sh"]

EXPOSE 8066
CMD ["/opt/metamap/bin/testapi.sh","cancer"]

# COMMANDS to build and run
# ===============================
# mkdir build && cp Dockerfile build/ && cd build
# docker build -t metamap:2016 .
# docker run metamap:2016