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
					case "mkdir" : 	bool = in.readBoolean();
									// System.out.println(bool);
					               	if (bool == true){
									System.out.println("Directory created successfully");
								   	}
								   	else{
									System.out.println("Directory cannot be created");
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
}
