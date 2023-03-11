import java.io.*;
import java.net.*;

// Server class
class ServerThreaded {

	private static ServerSocket serverSocket2 = null;

	public static String initServerDir = System.getProperty("user.dir");
	public static ManageLock ml = new ManageLock();
	public static void main(String[] args)
	{


		if(args.length<2)
			{
				System.out.println("Cannot start the server, two port numbers is needed");
				System.exit(0);
			}

			if(args[0].equals(args[1]))
			{
				System.out.println("Cannot start the server, port numbers cannot be same");
				System.exit(0);
			}


		// Here we define Server Socket running on port 8080
		// String currentDir = System.getProperty("user.dir");

		ServerSocket serverSocket1 = null;
		

		try {

			// server is listening on port 1
            serverSocket1 = new ServerSocket(Integer.parseInt(args[0]));
            serverSocket1.setReuseAddress(true);
			
            // server is listening on port 2
            serverSocket2 = new ServerSocket(Integer.parseInt(args[1]));
            serverSocket2.setReuseAddress(true);
			
			// running infinite loop for getting
			// client request
			while (true) {

				System.out.println("Server running on port: "+ args[0]);

				// socket object to receive incoming client
				// requests
				Socket clientSocket = serverSocket1.accept();

				// Displaying that new client is connected
				// to server
				System.out.println("New client connected"
								+ clientSocket.getInetAddress()
										.getHostAddress());

				// create a new thread object
				ClientHandler clientSock
					= new ClientHandler(clientSocket);

				// This thread will handle the client
				// separately
				new Thread(clientSock).start();

				
				
                // This thread will handle the client
                // separately on port 2
                //new Thread(clientSock2).start();
				
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			if (serverSocket1 != null) {
                try {
                    serverSocket1.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (serverSocket2 != null) {
                try {
                    serverSocket2.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
		}
	}

	// ClientHandler class
	private static class ClientHandler implements Runnable {
		private final Socket clientSocket;
		//private static String currThreadDir = null;
		// Constructor
		
		private static ThreadLocal<String> currThreadDir = new ThreadLocal<>();

    	public static void setThreadLocalVariable(String value) {
			currThreadDir.set(value);
   		}

    	public static String getThreadLocalVariable() {
        return currThreadDir.get();
    	}
		
		public ClientHandler(Socket socket)
		{
			this.clientSocket = socket;
			
			//currThreadDir = initServerDir;
		}

		

		public void run()
		{
			DataOutputStream out = null;
			DataInputStream in = null;
			// String currThreadDir = null;
			
			try {
					
				// get the outputstream of client
				out = new DataOutputStream(
					clientSocket.getOutputStream());

				// get the inputstream of client
				in = new DataInputStream(
					new DataInputStream(
						clientSocket.getInputStream()));

				

						// Create a BufferedOutputStream object and send the file contents to the client
				setThreadLocalVariable(initServerDir);
				System.out.println("I am here");
				getPWD();
				String command;
				boolean bool = false;
				while (!(command = in.readUTF()).equals("quit")){
					
					System.out.printf(
						" Sent from the client: %s\n",
						command);
					// writing the received message from
					// client
					switch(command.split(" ")[0])
					{
						case   "pwd":  	System.out.println("Present Working Directory:");
							  		    String pwd = getPWD();
							  		    out.writeUTF(pwd);
							  		    //dataOutputStream.writeUTF(pwd);
							  		    break;
						case "get" :    System.out.println("Sending the File to the Client\n");
							 		    //System.out.println(currThreadDir+"/".concat(command.split(" ")[1]));
							 		    bool = sendFile(getThreadLocalVariable()+"/".concat(command.split(" ")[1]),out);
							 		    if(bool)
							 		    {
											System.out.println("File Sent Successfully");
							 		    }
							 		    else
							 		    {
											System.out.println("File Sending Failed");
							 		    }
							 		    break;


						case "put" :    if(command.split(" ")[1].contains("/"))
										 {
											receiveFile(getThreadLocalVariable()+"/".concat(command.split(" ")[1].substring(command.split(" ")[1].lastIndexOf('/') + 1).trim()), in, out);
										 }
										 else
										 {
										 	receiveFile(getThreadLocalVariable()+"/".concat(command.split(" ")[1]), in, out);
										 } 
										  break;


						case "mkdir": 	System.out.println("Making new directory...");
							 		 	bool = mkDir(command.split(" ")[1]);
							  			out.writeBoolean(bool);
							  			break;

						case  "ls" :    System.out.println("Listing directory content");
										  String childs = listContent();
										  out.writeUTF(childs);
										  break;


						case  "cd" :  System.out.println("Changing Directory...");
										  if(command.split(" ").length==1)
										  {
											 setThreadLocalVariable(initServerDir);
											 out.writeBoolean(true);
											 out.writeUTF(getPWD());
										  }
										  else{
											 bool = cd(command.split(" ")[1]);
											 out.writeBoolean(bool);
											 if(bool){
												 out.writeUTF(getPWD());
											 }	
										  }	 
										break;

						case "delete":	//System.out.println("Deleting file...");
							 			bool = delete(getThreadLocalVariable()+"/".concat(command.split(" ")[1]));
							  			System.out.println(bool);
							  			out.writeBoolean(bool);
							  			break;

						case "quit":	out.writeUTF("Client Connection Closed");
									   	break;
						
						case "terminate": 	// socket object to receive incoming client
											// requests on port 2
											Socket clientSocket2 = serverSocket2.accept();
							
											// Displaying that new client is connected
											// to server on port 2
											System.out.println("Client connected to tport "
													+ clientSocket2.getInetAddress()
													.getHostAddress());
											DataOutputStream terminateOut = new DataOutputStream(clientSocket2.getOutputStream());
											if(ml.releaseLock(Long.parseLong(command.split(" ")[1])))
											{
												terminateOut.writeUTF("Success");
											}
											else{
												terminateOut.writeUTF("Fail");
											}
											terminateOut.close();
											break;

						default     : 	System.out.println("Valid command not found");
							  		  	break;
					}
					//out.println(command);
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			finally {
				try {
					if (out != null) {
						out.close();
					}
					if (in != null) {
						//System.out.println("Client connection closed");
						in.close();
						clientSocket.close();
					}
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		private static String getPWD(){
			System.out.println(getThreadLocalVariable());
			return getThreadLocalVariable(); //returns present user directory
		}

		private static boolean sendFile(String path, DataOutputStream out) throws Exception
		{
		int bytes = 0;
		try
		{
		System.out.println(path);
		File file = new File(path);
		FileInputStream fileInputStream = new FileInputStream(file);
		//System.out.println("I am before setLock");
		long commandID = ml.setLock(path,"get_lock");
		//System.out.println("I am after setLock");
		if(commandID!=0)
		{
			out.writeUTF("Pass-"+commandID);
			// sending the file to client side
			out.writeLong(file.length());
			// breaking the file into byte chunks
			byte[] tmpStorage = new byte[4 * 1024];
			while ((bytes = fileInputStream.read(tmpStorage))!= -1) {
			// sending the file to the client socket
			if(!(ml.getStatus(path,"get_lock")))
			{
				System.out.println("File sending terminated from client side.. Stopping the operation now");
				out.writeUTF("Put operation terminated at server");
				fileInputStream.close();
				return false;
			}
				out.write(tmpStorage, 0, bytes);
				out.flush();
			}
			//System.out.println(commandID);
			ml.releaseLock(commandID);
			// closing file
			fileInputStream.close();
			return true;
		}
		else
		{
			out.writeUTF("File is being used by someone else.. Please try again in sometime");
			fileInputStream.close();
			return false;
		}
		
		}
		catch(FileNotFoundException fnfe)
		{
			System.out.println("File does not exist in the server");
			out.writeUTF("Fail");
			return false;
		}
		}


		//put :  receive file from client, function starts here

		private static void receiveFile(String fileName, DataInputStream in, DataOutputStream out)
		throws Exception
	{
		if(fileName.contains("/"))
			{
				fileName = fileName.substring(fileName.lastIndexOf('/') + 1).trim();
			}
		int bytes = 0;
		// System.out.println("I am here");

		if (in.readUTF().equals("Fail")){
			System.out.println("File does not exist at client");
			return;
		}
		FileOutputStream fileOutputStream = new FileOutputStream(fileName);
		// System.out.println("I am after fileoutput stream");
		long commandID = ml.setLock(fileName,"put_lock");
		out.writeLong(commandID);
		if(commandID!=0)
		{
		long filesize = in.readLong(); // read file size
		byte[] tmpStorage = new byte[4 * 1024];
		while (filesize > 0 && 
			(bytes = in.read(tmpStorage, 0,(int)Math.min(tmpStorage.length, filesize)))!= -1) {
			
			// writing the file using fileoutputstream.write method
			if(!(ml.getStatus(fileName,"put_lock")))
			{
				System.out.println("File recieving terminated from client side.. Stopping the operation now");
				out.writeUTF("Put operation terminated at server");

				fileOutputStream.close();
				return;
			}
			fileOutputStream.write(tmpStorage, 0, bytes);
			filesize -= bytes; // reading upto file size
		}
		ml.releaseLock(commandID);
		// file received successfully
		System.out.println("File is Received");
		fileOutputStream.close();
	}
	else
	{
		out.writeUTF("File is being used by someone else.. Please try again in sometime");
		fileOutputStream.close();
	}
	}

		private static boolean mkDir(String dirName){
			File f = new File(dirName);
			return f.mkdir();// making new directory
		}


		//ls : list current directory content, function starts here

	private static String listContent()
	{
		File dir = new File(getThreadLocalVariable());
        String childs[] = dir.list(); //to get the list of files under the directory
		StringBuilder sb = new StringBuilder();
        for(String child: childs){
            System.out.println(child);
			sb.append(child).append(",");
        }
		//System.out.println("length:"+sb.length());
		if (sb.length() == 0){
			System.out.println("No file in present directory");
			return "No file in present directory";
		}
		else{
			return sb.deleteCharAt(sb.length() - 1).toString();
		}

		
	}


	//cd : changing directory, function starts here

	private static boolean cd(String dirName){
		
		if(dirName.equals(".."))
		{
			
			if(getThreadLocalVariable().equals(initServerDir)){
				System.out.println("Already in home directory");
				
				return true;

			}
			else{
				// System.out.println(currThreadDir.substring(0, currThreadDir.lastIndexOf('/')).trim());
				// System.setProperty("user.dir", getPWD().substring(0, getPWD().lastIndexOf('/')).trim());
				System.out.println(getPWD().substring(0, getPWD().lastIndexOf('/')).trim());
				setThreadLocalVariable(getPWD().substring(0, getPWD().lastIndexOf('/')).trim());
				//currThreadDir = getPWD().substring(0, getPWD().lastIndexOf('/')).trim();
				return true;
			}
			
		}
		else
		{
			File dir = new File(dirName);
			if(dir.isDirectory()==true) {
				// System.setProperty("user.dir", dir.getAbsolutePath());
				setThreadLocalVariable(dir.getAbsolutePath());
				//currThreadDir = dir.getAbsolutePath();
				return true;
			} else {
				return false;
			}
		}
	}

	private static boolean delete(String filename){
		File file = new File(filename);
 
        if (file.delete()) 
		{
            System.out.println("File deleted in the server");
			return true;
        }
        else 
		{
            System.out.println("Failed to delete the file in server");
			return false;
        }
	}


	}
}
