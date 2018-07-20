# Spark Bitcoin 

## Getting Started

This applciation read rawblock from zmq bitcoind publisher.
Use Spark to manage all transactions of bitcoin's blockchain
It's tested on Ubuntu 17.10

### Prerequisites

 * JDK 1.8
 * Git
 * ZMQ
 * Bitcoind server
 * Maven >= 3.5
 * MongoDB 4.0.0
 * Zookeper
 * Kafka 2.11
 * Neo4j 3.4.4
 
 ### Optional 
 
 * Hadoop 3.1.0

#### Installing JDK 1.8

Install Java Development Kit

```
    sudo apt-get install default-jdk
```

Set JAVA_HOME

```
    sudo nano /etc/environment
```

At the end of this file, add the following line, making sure to replace the highlighted path with your own copied path.

```
    JAVA_HOME="/usr/lib/jvm/java-8-oracle"
```

Save and exit the file, and reload it.

```
    source /etc/environment
```

#### Installing Git

Install Git

```
    sudo apt-get update
    sudo apt-get install git
```

#### Install ZMQ and jZMQ

ZeroMQ (also known as ØMQ, 0MQ, or zmq) looks like an embeddable networking library but acts like a concurrency framework. 
It gives you sockets that carry atomic messages across various transports like in-process, inter-process, TCP, and multicast.

```
    wget http://download.zeromq.org/zeromq-2.1.7.tar.gz
    tar -xzf zeromq-2.1.7.tar.gz
    cd zeromq-2.1.7
    ./configure
    make
    sudo make install
```

To use it in Java applications ZMQ use a wrapper called JZMQ. Follow this steps to install.

```
    git clone https://github.com/nathanmarz/jzmq.git
    cd jzmq
    ./autogen.sh
    ./configure
    make
    sudo make install
    sudo ldconfig
```

If make doesn't work, changing **classdist_noinst.stamp** to **classnoinst.stamp** in **src/Makefile.am**
and then re-running *./autogen.sh* seems to fix the issue.

#### Install bitcoind

Bitcoind is a program that implements the Bitcoin protocol for remote procedure call (RPC) use. 


```
   sudo apt-get install build-essential
   sudo apt-get install libtool autotools-dev autoconf
   sudo apt-get install libssl-dev
   sudo apt-get install libboost-all-dev
   sudo add-apt-repository ppa:bitcoin/bitcoin
   sudo apt-get update
   sudo apt-get install bitcoind
```
This application use testnet network to get transactions from Blockchain.
To start bitcoind with this option follow this steps
```
    mkdir ~/.bitcoin/ && cd ~/.bitcoin/
    nano bitcoind.conf
```

Append this rows
```
    # Run on the test network instead of the real bitcoin network.
    testnet=1
    
    # Enable bitcoind to send blocks without witness 
    # https://github.com/bitcoinj/bitcoinj/issues/1348
    -rpcserialversion=0
    

```

Finally, to enable the zmq bitcoind publisher append into **bitcoind.conf** this row
```
    zmqpubrawblock=tcp://127.0.0.1:28332

``` 
So, when a new transaction is coming, bitcoind publish it on **rawblock** topic

To start bitcoind
```
    bitcoind -daemon
```

To stop bitcoind
 ```
    bitcoind-cli stop
```

#### Install Maven

To install maven use
```
    sudo apt-get install maven
```

#### Install MongoDB

To install MongoDB v. 4.0.0
Run following commands

```

    sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 9DA31620334BD75D9DCB49F368818C72E52529D4
    
    echo "deb [ arch=amd64,arm64 ] https://repo.mongodb.org/apt/ubuntu xenial/mongodb-org/4.0 multiverse" | sudo tee /etc/apt/sources.list.d/mongodb-org-4.0.list
    
    sudo apt-get update
    
    sudo apt-get install -y mongodb-org 

```

#### Install Neo4j

   Follow steps on guide [Install Neo4j on ubuntu] https://datawookie.netlify.com/blog/2016/09/installing-neo4j-on-ubuntu-16.04/
   
   ```
    # Start neo4j
    sudo service neo4j start
    
    # Stop neo4j
    sudo service neo4j stop
    
    # To disable authentication uncomment this row into file /etc/neo4j/neo4j.conf 
        dbms.security.auth_enabled=false 
    # Restart neo4j
    
    
   ```
   

#### Install Hadoop (on single cluster)

 Follow the guide to url [Hadoop Homepage] https://hadoop.apache.org/docs/stable/hadoop-project-dist/hadoop-common/SingleCluster.html

```
    #Start hadoop
    cd /usr/local/hadoop/sbin
    ./start-all.sh
    
    #Stop hadoop
    cd /usr/local/hadoop/sbin
    ./stop-all.sh
    
    #Format HDFS
    hadoop namenode -format
    
    #Web Console
    http://localhost:9870/dfshealth.html
    
```

#### Install Zookeper

To install zookeper run this command

```
    sudo apt-get install zookeeperd
    
    # Check the installation with 
    # netstat -ant | grep :2181
    
```

#### Install Kafka server

 ####TODO
```
    Remember to set 256m to JVM
    
    #List of Topics
    /opt/kafka/bin/kafka-topics.sh --list  --zookeeper localhost:2181
```

### Compile and Start Code

Download source code and import as maven project in Eclipse/IntelliJ

Compilation is made with: ``` mvn clean install ```

Run the application as Java Application.