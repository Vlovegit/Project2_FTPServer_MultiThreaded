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
		private static String currThreadDir = initServerDir;
		// Constructor
		public ClientHandler(Socket socket)
		{
			this.clientSocket = socket;
		}

		public void run()
		{
			DataOutputStream out = null;
			DataInputStream in = null;
			
			try {
					
				// get the outputstream of client
				out = new DataOutputStream(
					clientSocket.getOutputStream());

				// get the inputstream of client
				in = new DataInputStream(
					new DataInputStream(
						clientSocket.getInputStream()));

				

						// Create a BufferedOutputStream object and send the file contents to the client

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
						case "mkdir": 	System.out.println("Making new directory...");
							 		 	bool = mkDir(command.split(" ")[1]);
							  			out.writeBoolean(bool);
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
			System.out.println(currThreadDir);
			return currThreadDir; //returns present user directory
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

		private static boolean mkDir(String dirName){
			File f = new File(dirName);
			return f.mkdir();// making new directory
		}
	}
}
