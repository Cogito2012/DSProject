# Introduction

This repo aims to implement client/server communication with docker virtualization methods. Both the original docker container method and docker-compose method are provided as follows. The client program read amazon reviews data from csv file, and sends the parsed data to server program. The server program receives the data and counts the words, then sends back the statistical results to client program. The client receives the results and write them into local device.

# Preparation

### Download the dataset
* Download the ["Amazon fine food reviews"](https://www.kaggle.com/snap/amazon-fine-food-reviews/downloads/amazon-fine-food-reviews.zip/2) dataset
* Extract a file "Reviews.csv" into a folder called "amazon-fine-food-reviews" so that all reviews are in this path 
```
amazon-fine-food-reviews/Reviews.csv
```

# Method 1: Docker container

### Setup docker project image and two containers

1. Build image
```
docker build -t project1:latest .
```

2. Verify the new image with 
```
docker images
```

3. Create subnetwork
```
docker network create --subnet=172.18.0.0/16 project_network
```

4. Start a container for server with IP specified
```
docker run --name wc_server --net project_network --ip 172.18.0.23 -p 8888:8888 -it project1 /bin/bash
```

5. Start a container for client with IP specified in another terminal
```
docker run --name wc_client --net project_network --ip 172.18.0.24 -p 8889:8889 -it project1 /bin/bash
```

### Run the Server
```
# PORT number is required to be given (e.g., 8888)
java -cp target/cs_word_count-1.0-SNAPSHOT.jar edu.rit.cs.cs_word_count.Server 8888
```

### Run the Client
```
# Both server IP and PORT number is required to be given (e.g., 172.18.0.23:8888)
java -cp target/cs_word_count-1.0-SNAPSHOT.jar edu.rit.cs.cs_word_count.Client 172.18.0.23:8888 amazon-fine-food-reviews/Reviews.csv
```

# Method 2: Docker-compose Implementation
### Setup the docker project image

Just follow the step 1 and 2 as mentioned above if you did not done these steps before.

### Build and run the whole program
```
docker-compose up
```

If you have any questions, please feel free to contact me (wb6219@rit.edu).

