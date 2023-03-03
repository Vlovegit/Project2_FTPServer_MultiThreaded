# Programming project 1
## _Distributed Computing System_
### Team -13

## Group Members

Kriti Ghosh (kg23166)
email id  : kg23166@uga.edu
UGA ID: 811945814
Vaibhav Goyal (vg80700)
email id : vg80700@uga.edu
UGA ID: 811579798

## Compilation Instruction

## _Steps for running the Server_

1. ssh ugamyid@odin.cs.uga.edu  //(connect to Odin)
2. ssh ugamyid@vcf1.cs.uga.edu  //(connect to vcf )
3. cd DCS_Project1_FTPServer/Server     //(go to server parent directory)
4. javac -cp bin -d bin src/ServerFTP.java      //(compile ServerFTP.java)
5. java -cp bin src/ServerFTP.java <port_number>    //(run ServerFTP.java with port number as commandline argument, example: java -cp bin src/ServerFTP.java 8080)

## _Steps for running the Client_

1. ssh ugamyid@odin.cs.uga.edu //(connect to Odin)
2. ssh ugamyid@vcf2.cs.uga.edu  //(connect to vcf )
3. cd DCS_Project1_FTPServer/Client     //(go to client parent directory)
4. javac -cp bin -d bin src/ClientFTP.java      //(compile ClientFTP.java)
5. java -cp bin src/ClientFTP.java <host> <server_port>     //(run CilentFTP.java with server host and server port number as commandline arguments, example: java -cp bin src/ClientFTP.java vcf1.cs.uga.edu 8080)

## _Commands implemented_

1. get - Fetch remote file from remote server ( get <remote_filename> )
2. put - Send local file to remote server ( put <local_filename> )
3. delete - Delete file from remote server ( delete <remote_filename> )
4. ls - List all files and subdirectories in the remote server ( ls )
5. cd - Change Directory to mentioned path or parent directory in the remote server ( cd <remote_directory_name> or cd .. )
6. mkdir - Create a subdirectory in current working directory in the remote server ( mkdir <remote_directory_name> )
7. pwd - Fetch present working directory in the remote server ( pwd )
8. quit - Close the connection with the remote server ( quit )

## _Note_

For this project we have NOT implemented Multithreading as it is not required by this project. 



## Academic Honesty Declaration

This project was done in its entirety by Vaibhav Goyal and Kriti Ghosh. 
We hereby state that we have not received unauthorized help of any form.







