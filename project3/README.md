# Kademlia DHT Project

###  Preparation
Make sure the `resources/testfile.txt` exists.

### Step1: Compile the project
Open a terminal and execute command:

`
rm -rf ./target/* && mvn package

`

### Step2: Execute the program
(1) Run the MiniServer program:
`
java -cp target/distributed_hash_table-1.0.jar edu.rit.cs.MiniServer 

`

(2) Run the ServerAgent program:
`
java -cp target/distributed_hash_table-1.0.jar edu.rit.cs.ServerAgent

`

(3) Run the ClientNode program:
`
java -cp target/distributed_hash_table-1.0.jar edu.rit.cs.ClientNode 127.0.0.11:8090

`

### Step3: Do as the hint by ServerAgent and ClientNode programs.