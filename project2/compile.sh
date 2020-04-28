#!/bin/bash

SRC_PATH=./src/main/java/edu/rit/cs
CLASS_PATH=./classes

rm -rf $CLASS_PATH/*

javac -d $CLASS_PATH \
    $SRC_PATH/TopicsDB.java \
    $SRC_PATH/EventManager.java \
    $SRC_PATH/Topic.java \
    $SRC_PATH/Event.java \
    $SRC_PATH/Publisher.java \
    $SRC_PATH/Subscriber.java \
    $SRC_PATH/User.java \
    $SRC_PATH/PubSubAgent.java \
    $SRC_PATH/PubClient.java \
    $SRC_PATH/SubClient.java

# run
PORT=$1
cd $CLASS_PATH
java edu.rit.cs.EventManager $PORT
cd ..
