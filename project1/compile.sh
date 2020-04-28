#!/bin/bash

# compile the classes
SRC_PATH=./src/main/java/edu/rit/cs/cs_word_count
CLASS_PATH=./classes
# javac -d $CLASS_PATH $SRC_PATH/*.java
javac -d $CLASS_PATH \
    $SRC_PATH/AmazonFineFoodReview.java \
    $SRC_PATH/ListReview.java \
    $SRC_PATH/KV.java \
    $SRC_PATH/MyTimer.java \
    $SRC_PATH/Server.java \
    $SRC_PATH/Client.java

# cd $CLASS_PATH
# # run server 
# java edu.rit.cs.cs_word_count.Server 8888

# # run client
# java edu.rit.cs.cs_word_count.Client ../amazon-fine-food-reviews/Reviews.csv
# cd -

