# Ubuntu 18.04 with JDK 11
# Build image with:  docker build -t project1:latest .
# Modified from the Dockerfile of course repository.

FROM ubuntu:18.04
MAINTAINER Wentao Bao, Peizhao Hu, http://cs.rit.edu/~ph

# install all dependencies
RUN apt-get update && \
    apt-get upgrade -y && \
    apt-get install -y  software-properties-common && \
    apt-get update && \
    apt-get install -y openjdk-11-jdk && \
    apt-get install -y net-tools iputils-ping maven gradle nmap wget git vim build-essential && \
    apt-get clean

# create a new directory as the working directory
RUN mkdir /project3


# copy files from the directory of the Dockerfile on your computer to this docker build environment.
COPY src /project3/src
COPY pom.xml /project3/
COPY README.md /project3/

# setup working directory in the container
WORKDIR /project3

# go into the working directory and build java package using maven
RUN cd /project3 && mvn package