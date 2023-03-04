import java.io.*;
import java.net.*;

// Server class
class ServerThreaded {

	// private static DataOutputStream dataOutputStream = null;
	// private static DataInputStream dataInputStream = null;
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
			PrintWriter out = null;
			BufferedReader in = null;
			try {
					
				// get the outputstream of client
				out = new PrintWriter(
					clientSocket.getOutputStream(), true);

				// get the inputstream of client
				in = new BufferedReader(
					new InputStreamReader(
						clientSocket.getInputStream()));

				String command;
				while (!(command = in.readLine()).equals("quit")){
					
					System.out.printf(
						" Sent from the client: %s\n",
						command);
					// writing the received message from
					// client
					switch(command.split(" ")[0])
					{
						case   "pwd":  System.out.println("Present Working Directory:");
							  		   String pwd = getPWD();
							  		   out.println(pwd);
							  		   //dataOutputStream.writeUTF(pwd);
							  		   break;
						default     : System.out.println("Valid command not found");
							  		  break;
					}
					
					//out.println(command);
				}
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			finally {
				try {
					if (out != null) {
						out.close();
					}
					if (in != null) {
						System.out.println("Client connection closed");
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
	}
}
