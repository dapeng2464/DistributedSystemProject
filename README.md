# DistributedSystemProject
Highly Available CORBA Distributed Staff Management System


Implement a highly available CORBA Distributed Staff Management System, which tolerates process crashes using unreliable failure detection. There is a group of three redundant servers, one of them is elected as leader server and the others are slaves. The leader server receives the requests from CORBA FE, broadcast them to slaves, gets the result and sends back to FE. If the leader server is down, it will trig an election of new leader server which is using a bully algorithm. For the clients, they send request to and get replies from FE using CORBA without any concern about the details behind the FE. They treat the FE and server group as a whole system, which achieves the fault tolerance transparency.
