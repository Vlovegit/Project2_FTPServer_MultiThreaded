import java.io.*;
import java.net.*;
import java.util.*;

// Client class
class ClientThreaded {
	
	// driver code
	private static String machineip = null;
	private static int nport = 0;
	private static int tport = 0;
	public static void main(String[] args)
	{
		// establish a connection by providing host and port
		// number

		if(args.length<3)
			{
				System.out.println("Cannot connect to the server as machine address, nport or tport missing in the arguements");
				System.exit(0);
			}
		if (args[1].equals(args[2]))
		{
			System.out.println("Cannot connect to the server, nport and tport cannot be same");
			System.exit(0);
		}
		try (Socket socket = new Socket(args[0], Integer.parseInt(args[1]))) {
			
			machineip = args[0];
			nport = Integer.parseInt(args[1]);
			tport = Integer.parseInt(args[2]);

			// writing to server
			DataOutputStream out = new DataOutputStream(
				socket.getOutputStream());

			// reading from server
			DataInputStream in
				= new DataInputStream(new DataInputStream(
					socket.getInputStream()));


			// object of scanner class
			Scanner sc = new Scanner(System.in);
			String command = null;
			String currentUserDir = System.getProperty("user.dir");
			do {
				
				// reading from user
				System.out.print("myftp>");
				command = sc.nextLine();

				handleCommand(command, in, out, currentUserDir);


			}while(!"quit".equalsIgnoreCase(command));
			
			// closing the scanner object
			sc.close();
		}
		catch(ConnectException ce)
		{
			System.out.println("Cannot connect to server. The host or port provided is incorrect. Please check and try again");
		}
		catch(SocketException se)
		{
			System.out.println("Connection to the server lost. Please reconnect.");
		}
	    catch(IOException ioe){
			System.out.println("Timeout: Server busy");//new
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void handleCommand(final String command, DataInputStream in, DataOutputStream out, String currentUserDir)
	{
		if (command.contains("&")) {
			// execute command in a separate thread
			Thread thread = new Thread(() -> {
				try {
					// remove '&' sign from command
					String cmd = command.substring(0, command.length() - 1);
					executeCommand(cmd, currentUserDir, in, out);
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
			thread.start();
		} else {
			executeCommand(command, currentUserDir, in, out);
		}
	}

	private static void executeCommand(String command, String currentUserDir, DataInputStream in, DataOutputStream out) {
	
		try{
				// sending the user input to server
				out.writeUTF(command);
				out.flush();
				boolean bool = false;
				switch(command.split(" ")[0])
				{
					case  "pwd" :   System.out.println(in.readUTF());
									break;
					case  "get" :   System.out.println("\nFetching file from the Server");
									receiveFile(command.split(" ")[1], in);
									break;

					case  "put": 	System.out.println("\nSending the File to the Server\n");
									//System.out.println(currentDir+"Client/Files/".concat(splitCommand[1]));
									sendFile(currentUserDir+"/".concat(command.split(" ")[1]), out, in);
									break;
					case "mkdir" : 	bool = in.readBoolean();
									// System.out.println(bool);
									if (bool == true){
									System.out.println("Directory created successfully");
									}
									else{
									System.out.println("Directory cannot be created");
									}
									break;

					case  "ls" :    String content = in.readUTF();
									String content1[] = content.split(",");
									// System.out.println(content1);
									for (String child : content1){
										System.out.println(child);
									}
									break;

					case  "cd" :   bool = in.readBoolean();
									if (bool == true){
									
									
										System.out.println("Directory Changed");
										System.out.println(in.readUTF());
									
									
									}
									else{
									System.out.println("Not valid directory");
									}
									break;

					case "quit":    System.out.println(in.readUTF());
									break;
					case "terminate":	try (Socket terminateSocket = new Socket(machineip, tport)) {
											DataInputStream terminateIn = new DataInputStream(new DataInputStream(terminateSocket.getInputStream()));
											String message = terminateIn.readUTF();
											if (message.equals("Success")) {
												System.out.println("Terminated successfully");
											} else {
												System.out.println("Failed to terminate");
											}
											terminateIn.close();
											terminateSocket.close();
										} catch (IOException e) {
											System.out.println("Failed to terminate");
										}
										break;
					default 	: 	System.out.println("Please enter a valid command");
									break;
				}
			}
			catch(ConnectException ce)
			{
				System.out.println("Cannot connect to server. The host or port provided is incorrect. Please check and try again");
			}
			catch(SocketException se)
			{
				System.out.println("Connection to the server lost. Please reconnect.");
			}
	    	catch(IOException ioe){
			System.out.println("Timeout: Server busy");//new
			}
			catch (Exception e) {
				e.printStackTrace();
			}		
	}

	private static void receiveFile(String fileName, DataInputStream in)
		throws Exception
	{
		if(fileName.contains("/"))
			{
				fileName = fileName.substring(fileName.lastIndexOf('/') + 1).trim();
			}
		int bytes = 0;
		// System.out.println("I am here");
		String fileStatus = in.readUTF();
		if (fileStatus.equals("Fail")){
			System.out.println("File does not exist at server");
			return;
		}
		System.out.println("Command Id for the Put operation is "+ fileStatus.split("-")[1]);
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

	private static boolean sendFile(String path, DataOutputStream out, DataInputStream in) throws Exception
		{
		int bytes = 0;
		try
		{
		File file = new File(path);
		FileInputStream fileInputStream = new FileInputStream(file);
        out.writeUTF("Pass");
		System.out.println("Command Id for the Get operation is "+ in.readLong());
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
}
