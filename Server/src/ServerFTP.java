import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class ServerFTP {

	private static DataOutputStream dataOutputStream = null;
	private static DataInputStream dataInputStream = null;
	public static String initServerDir = System.getProperty("user.dir");
	

	public static void main(String[] args)
	{

		// int clientAllowed = 1;//new
	    // int clientAccepted = 0;//new
		// boolean serverBusy = false;//new
		
		if(args.length<1)
			{
				System.out.println("Cannot start the server, port number is needed");
				System.exit(0);
			}


		// Here we define Server Socket running on port 8080
		String currentDir = System.getProperty("user.dir");
		try (ServerSocket serverSocket
			= new ServerSocket(Integer.parseInt(args[0]))) { //new addition : backlog
			while(true)
			{
			System.out.println("Server running on port: "+ args[0]);

				// if (clientAccepted < clientAllowed){}
				// else{
				// 	System.out.println("Client limit exceeded");

				// }
			// if(clientAccepted == clientAllowed){}
			// listen for incoming requests using accept
			Socket clientSocket = serverSocket.accept();
			// clientAccepted = clientAccepted++; //new
			System.out.println("Connected");
			dataInputStream = new DataInputStream(clientSocket.getInputStream());
			dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());

			// if (clientAccepted != 0){
			// 	serverBusy = true;
			// 	dataOutputStream.writeBoolean(serverBusy);
				
			// }
			String command = "";
			boolean bool = false;
			while(!(command.equals("quit")))
			{
				try{
					command = dataInputStream.readUTF();
				}
				catch(EOFException eof){
					System.out.println("End of file");
				} //TODO change
				System.out.println(command);
			switch(command.split(" ")[0])
			{
				case "put" :if(command.split(" ")[1].contains("/"))
							{
								receiveFile(currentDir+"/".concat(command.split(" ")[1].substring(command.split(" ")[1].lastIndexOf('/') + 1).trim()));
							}
							else
							{
							receiveFile(currentDir+"/".concat(command.split(" ")[1]));
							} 
							 break;

				case "get" : System.out.println("Sending the File to the Client\n");
							 System.out.println(currentDir+"/".concat(command.split(" ")[1]));
							 bool = sendFile(currentDir+"/".concat(command.split(" ")[1]));
							 if(bool)
							 {
								System.out.println("File Sent Successfully");
							 }
							 else
							 {
								System.out.println("File Sending Failed");
							 }
							 dataOutputStream.writeBoolean(bool);
							 break; 

				case "quit" : System.out.println("Client connection closed");
							  dataInputStream.close();
							  dataOutputStream.close();
							  clientSocket.close();
							  break;

				case  "ls" :  System.out.println("Listing directory content");
							  String childs = listContent();
							  dataOutputStream.writeUTF(childs);
							  break;

				case  "pwd":  System.out.println("Present Working Directory:");
							  String pwd = getPWD();
							  dataOutputStream.writeUTF(pwd);
							  break;

				case "mkdir": System.out.println("Making new directory...");
							  bool = mkDir(command.split(" ")[1]);
							  dataOutputStream.writeBoolean(bool);
							  break;

				case  "cd" :  System.out.println("Changing Directory...");
							 if(command.split(" ").length==1)
							 {
								System.setProperty("user.dir", initServerDir);
								dataOutputStream.writeBoolean(true);
								dataOutputStream.writeUTF(getPWD());
							 }
							 else{
								bool = cd(command.split(" ")[1]);
								dataOutputStream.writeBoolean(bool);
								if(bool){
									dataOutputStream.writeUTF(getPWD());
								}	
							 }	 
							 break;

				case "delete"://System.out.println("Deleting file...");
							  bool = delete(currentDir+"/".concat(command.split(" ")[1]));
							  System.out.println(bool);
							  dataOutputStream.writeBoolean(bool);
							  break;

				default 	: System.out.println("Valid command not found");
							  break;
			}	
			}
			
		}
	}
		catch (SocketException se){
			System.out.println("connection closed "); // new exception handled 
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	//put :  receive file from client, function starts here

	private static void receiveFile(String fileName)
		throws Exception
	{
		int bytes = 0;
		if (dataInputStream.readUTF().equals("Fail")){
			System.out.println("File does not exist at client");
			return;
		}
		FileOutputStream fileOutputStream
			= new FileOutputStream(fileName);

		long filesize
			= dataInputStream.readLong(); // read file size
		byte[] tmpStorage = new byte[4 * 1024];
		while (filesize > 0 && 
			(bytes = dataInputStream.read(tmpStorage, 0,(int)Math.min(tmpStorage.length, filesize)))!= -1) {
			//writing the file
			fileOutputStream.write(tmpStorage, 0, bytes);
			filesize -= bytes; // reading upto file size
		}
		// received file successfully
		System.out.println("File is Received");
		fileOutputStream.close();
	}

	//get : sending file to client, function starts here

	private static boolean sendFile(String path) throws Exception
	{
		int bytes = 0;
		try
		{
		File file = new File(path);
		FileInputStream fileInputStream = new FileInputStream(file);
        dataOutputStream.writeUTF("Pass");
		// sending the file to client side
		dataOutputStream.writeLong(file.length());
		// breaking the file into byte chunks
		byte[] tmpStorage = new byte[4 * 1024];
		while ((bytes = fileInputStream.read(tmpStorage))!= -1) {
		// sending the file to the client socket
		dataOutputStream.write(tmpStorage, 0, bytes);
			dataOutputStream.flush();
		}
		// closing file

		fileInputStream.close();
		return true;
		}
		catch(FileNotFoundException fnfe)
		{
			System.out.println("File does not exist in the server");
			dataOutputStream.writeUTF("Fail");
			return false;
		}
	}

	//ls : list current directory content, function starts here

	private static String listContent()
	{
		File dir = new File(System.getProperty("user.dir"));
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

	//pwd : present working directory, function starts here

	private static String getPWD(){
		System.out.println(System.getProperty("user.dir"));
		return System.getProperty("user.dir"); //returns present user directory
	}

	//mkdir: making new directory, function starts here

	private static boolean mkDir(String dirName){
		File f = new File(dirName);
		return f.mkdir();// making new directory
	}

	//cd : changing directory, function starts here

	private static boolean cd(String dirName){
		
		if(dirName.equals(".."))
		{
			
			if(initServerDir.equals(getPWD())){
				System.out.println("Already in home directory");
				
				return true;

			}
			else{
				//System.out.println(currThreadDir.substring(0, currThreadDir.lastIndexOf('/')).trim());
				System.setProperty("user.dir", getPWD().substring(0, getPWD().lastIndexOf('/')).trim());
				return true;
			}
			
		}
		else
		{
			File dir = new File(dirName);
			if(dir.isDirectory()==true) {
				System.setProperty("user.dir", dir.getAbsolutePath());
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