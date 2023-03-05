import java.io.*;
import java.net.*;
import java.util.*;

// Client class
class ClientThreaded {
	
	private static DataOutputStream dataOutputStream = null;
	private static DataInputStream dataInputStream = null;
	// driver code
	public static void main(String[] args)
	{
		// establish a connection by providing host and port
		// number
		try (Socket socket = new Socket("localhost", 8080)) {
			
			// writing to server
			PrintWriter out = new PrintWriter(
				socket.getOutputStream(), true);

			// reading from server
			BufferedReader in
				= new BufferedReader(new InputStreamReader(
					socket.getInputStream()));


			// object of scanner class
			Scanner sc = new Scanner(System.in);
			String command = null;

			do {
				
				// reading from user
				System.out.print("myftp>");
				command = sc.nextLine();

				// sending the user input to server
				out.println(command);
				out.flush();
				boolean bool = false;
				switch(command.split(" ")[0])
				{
					case  "pwd" :   System.out.println(in.readLine());
								    break;
					case  "get" :   System.out.println("Fetching file from the Server");
								    receiveFile(command.split(" ")[1], in);
					                break;
					case "quit":    System.out.println(in.readLine());
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

	private static void receiveFile(String fileName,BufferedReader in)
		throws Exception
	{
		try
		{
		if(fileName.contains("/"))
			{
				fileName = fileName.substring(fileName.lastIndexOf('/') + 1).trim();
			}
		int bytes = 0;
		// System.out.println("I am here");

		if (in.readLine().equals("Fail")){
			System.out.println("File does not exist at server");
			return;
		}

		
		FileOutputStream fileOutputStream = new FileOutputStream(fileName);
        // Create a FileOutputStream object to write the file on the client side

        int c;
        while ((c = in.read()) != -1) {
            fileOutputStream.write(c);
        }

        // Close the FileOutputStream, PrintWriter, BufferedReader, and the socket
        fileOutputStream.close();
	}
	catch(Exception e)
	{
		e.printStackTrace();
	}
	}
}
