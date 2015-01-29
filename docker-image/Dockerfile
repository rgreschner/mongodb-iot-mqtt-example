FROM tutum/centos:latest

RUN yum install -y wget; cd /tmp; wget http://dl.fedoraproject.org/pub/epel/7/x86_64/e/epel-release-7-5.noarch.rpm; rpm -Uvh epel-release-7*.rpm; rm -rf ./*.rpm; mkdir -p /data/db;

ADD mosquitto.repo /etc/yum.repos.d/mosquitto.repo
ADD mongodb.repo /etc/yum.repos.d/mongodb.repo 

RUN yum install -y mosquitto mosquitto-clients mongodb-org node npm

VOLUME ["/data"]

EXPOSE 1883 3000 27017
