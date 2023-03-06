import java.io.*;
import java.net.*;

// Server class
class ServerThreaded {

	public static String initServerDir = System.getProperty("user.dir");

	public static void main(String[] args)
	{


		if(args.length<1)
			{
				System.out.println("Cannot start the server, port number is needed");
				System.exit(0);
			}


		// Here we define Server Socket running on port 8080
		// String currentDir = System.getProperty("user.dir");

		ServerSocket serverSocket = null;

		try {

			// server is listening on port 1234
			serverSocket = new ServerSocket(Integer.parseInt(args[0]));
			serverSocket.setReuseAddress(true);

			// running infinite loop for getting
			// client request
			while (true) {

				System.out.println("Server running on port: "+ args[0]);

				// socket object to receive incoming client
				// requests
				Socket clientSocket = serverSocket.accept();

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
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			if (serverSocket != null) {
				try {
					serverSocket.close();
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
				//getPWD();
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
							 		    System.out.println(currThreadDir+"/".concat(command.split(" ")[1]));
							 		    bool = sendFile(currThreadDir+"/".concat(command.split(" ")[1]),out);
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
											 receiveFile(getThreadLocalVariable()+"/".concat(command.split(" ")[1].substring(command.split(" ")[1].lastIndexOf('/') + 1).trim()), in);
										 }
										 else
										 {
										 receiveFile(getThreadLocalVariable()+"/".concat(command.split(" ")[1]), in);
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

						case "quit":	out.writeUTF("Client Connection Closed");
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
		File file = new File(path);
		FileInputStream fileInputStream = new FileInputStream(file);
        out.writeUTF("Pass");
		// sending the file to client side
		out.writeLong(file.length());
		// breaking the file into byte chunks
		byte[] tmpStorage = new byte[4 * 1024];
		while ((bytes = fileInputStream.read(tmpStorage))!= -1) {
		// sending the file to the client socket
		out.write(tmpStorage, 0, bytes);
			out.flush();
		}
		// closing file

		fileInputStream.close();
		return true;
		}
		catch(FileNotFoundException fnfe)
		{
			System.out.println("File does not exist in the server");
			out.writeUTF("Fail");
			return false;
		}
		}


		//put :  receive file from client, function starts here

		private static void receiveFile(String fileName, DataInputStream in)
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
		long filesize = in.readLong(); // read file size
		byte[] tmpStorage = new byte[4 * 1024];
		while (filesize > 0 && 
			(bytes = in.read(tmpStorage, 0,(int)Math.min(tmpStorage.length, filesize)))!= -1) {
			// writing the file using fileoutputstream.write method
			fileOutputStream.write(tmpStorage, 0, bytes);
			filesize -= bytes; // reading upto file size
		}
		// file received successfully
		System.out.println("File is Received");
		fileOutputStream.close();
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




	}
}
