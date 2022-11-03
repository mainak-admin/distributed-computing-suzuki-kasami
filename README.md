# Suzuki Kasami Algorithm #

Suzuki–Kasami algorithm is a token-based algorithm for achieving mutual exclusion in distributed systems. This is modification of Ricart–Agrawala algorithm, a permission based (Non-token based) algorithm which uses REQUEST and REPLY messages to ensure mutual exclusion.

The program uses Java socket based programming approach and threads for execution. This program will implement Suzuki Kasami algorithm over the network or on a local computer using different available ports. The assumption here is that all IP's and ports are accessible to each node and no firewall or network partition issue occurs.

## Input ##

1. The program requires a file called as **nodes.config** which contain the site number, its IP address and it's port number.
2. All the files should be kept at the same level from where we are running the program
3. All sites id's would be defined sequentially, in a continous manner starting from 1

## Format of nodes.config file

```<site_id> <Ip_of_available_site> <port>```

Example:<br/>
1 127.0.0.1 49788<br/>
2 127.0.0.1 49670<br/>
3 127.0.0.1 49786<br/>
4 127.0.0.1 6942<br/>

To find open ports in comfigured system:<br/>
Windows: netstat -aon<br/>
Ubuntu: netstat -tnlp<br/>

For selecting the node is advisable to use **IP 127.0.0.1**

## Compiling the program: ##

**Step1:** Place Suzuki_kasami.java and nodes.config in a folder at the same level. Open cmd in administrator mode and move to the location where the file is placed.

**Step2:** Compile the code using: javac Suzuki_kasami.java

**Step3:** Run the code using: java Suzuki_kasami

## Executing the program: ##

This program is written in Java 11.0.12<br/>

Once the program starts, it will ask the site number:<br/>

```Enter site number for execution (1-4):```<br/>

-> Enter the site number and press ENTER<br/>
-> Then it will ask whether the site wants to enter the critical section<br/>

```Press ENTER to enter Critical Section:```<br/>
<br/>
**Note:** Before pressing ENTER please ensure all sites are initialized by running the compilation commmands on respective command prompts. For example: If there are 4 sites then the compilation code should be executed on 4 separate command prompts. The basic assumption is that, all the sites are in a running state pre execution. The program will execute successfully once all sites are up.<br/>
Once all sites are up, press ENTER on any site and the algorthm will work. On initialization, site 1 will hold the token.<br/>
Threads are used by the program to handle incoming requests, synchronzing LN and sending token.<br/>

**Output logs:**<br/>

```

Press ENTER to enter CS:
request,4,1

Site has token. Executing Critical Section.
Exiting Critical Section.
Press ENTER to enter CS:
request,2,1
Sending token to site 2
request,3,1
request,4,1
ln,2,1
ln,3,1
ln,4,1
ln,4,1
request,2,2
ln,2,2
request,1,4
Requesting token
Broadcasting request to 3 sites.
49670
49786
6942
Waiting for token..
token
Site has recieved token. Executing Critical Section.
Exiting Critical Section.
Press ENTER to enter CS:
```
## Quiting the program: ##
To quit the program, press Ctrl+C
