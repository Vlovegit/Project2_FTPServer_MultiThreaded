import java.io.*;
import java.net.*;
import java.util.*;

// Client class
class ClientThreaded {
	
	// driver code
	public static void main(String[] args)
	{
		// establish a connection by providing host and port
		// number
		try (Socket socket = new Socket("localhost", 8080)) {
			
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
				
				// sending the user input to server
				out.writeUTF(command);
				out.flush();
				boolean bool = false;
				switch(command.split(" ")[0])
				{
					case  "pwd" :   System.out.println(in.readUTF());
								    break;
					case  "get" :   System.out.println("Fetching file from the Server");
								    receiveFile(command.split(" ")[1], in);
					                break;

					case  "put": 	System.out.println("Sending the File to the Server\n");
									//System.out.println(currentDir+"Client/Files/".concat(splitCommand[1]));
									sendFile(currentUserDir+"/".concat(command.split(" ")[1]), out);
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
					default 	: 	System.out.println("Please enter a valid command");
									break;
				}

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
			System.out.println("File does not exist at server");
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
}
